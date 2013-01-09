package partitioner.models;

import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import partitioner.models.PartitionerModel.State;
import plugin.LogUtilities;
import plugin.mvc.DefaultPropertyDelegate;
import plugin.mvc.ITranslator.IModel;

import ca.ubc.magic.profiler.dist.model.DistributionModel;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel.FilterType;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory.InteractionCostType;
import ca.ubc.magic.profiler.dist.transform.IFilter;
import ca.ubc.magic.profiler.dist.transform.IModuleCoarsener;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory.ModuleCoarsenerType;
import ca.ubc.magic.profiler.parser.EntityConstraintParser;
import ca.ubc.magic.profiler.parser.HostParser;
import ca.ubc.magic.profiler.parser.JipParser;
import ca.ubc.magic.profiler.parser.JipRun;
import ca.ubc.magic.profiler.parser.ModuleModelHandler;
import ca.ubc.magic.profiler.parser.ModuleModelParser;
import ca.ubc.magic.profiler.partitioning.control.alg.IPartitioner;
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory;
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory.PartitionerType;
import ca.ubc.magic.profiler.partitioning.control.filters.FilterHelper;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

// this object is for sure accessed from the swt and swing threads
// it must be made thread safe!!!!!!!!!
public class 
PartitionerModel 
implements IModel
{
	public enum State {
		NO_MODEL, MODEL_BEFORE_PARTITION, PARTITIONED
	}
	
	private State state 
		= State.NO_MODEL;
	
	private DefaultPropertyDelegate property_change_delegate;
	
	private ModuleCoarsenerType 	mModuleType; 
	private PartitionerType 		partitioner_type;
	private InteractionCostType 	interaction_cost_type;
	private ExecutionCostType 		execution_cost_type;

	private volatile String			profiler_trace;  
	private volatile String 		constraint_xml_path;
	private volatile String 		host_configuration; 
	
	private ModuleModelHandler 		mmHandler;
	private SimulationFramework	 	mSimFramework; 
	
	// the following is set once in the SwingWorker thread
	private volatile ModuleModel 	mModuleModel;
	// the following is set once in the SwingWorker thread
	private volatile HostModel   	mHostModel; 
	private volatile EntityConstraintModel	mConstraintModel;

	private Map<String, IFilter> 	mFilterMap; 
	private IPartitioner 			partitioner;
	
	private volatile Boolean 	use_synthetic_node_filters = false;
	private Boolean 			use_host_cost_filter = false;
	private Boolean 			use_interaction_cost_filter = false;
	private Boolean 			generate_test_framework = false;
	private volatile Boolean 	activate_module_exposure = false;
	private volatile Boolean	use_module_model_parser = false;
	
	public
	PartitionerModel()
	{
		this.profiler_trace = "";
		this.constraint_xml_path = "";
		this.host_configuration = "";
       	
		this.mSimFramework
       		= new SimulationFramework( Boolean.FALSE );
		
		this.mModuleType	
			= ModuleCoarsenerType.BUNDLE;
		this.interaction_cost_type
			= InteractionCostType.IGNORE;
		this.execution_cost_type
			= ExecutionCostType.EXECUTION_TIME;
		this.partitioner_type
			= PartitionerType.MIN_MAX_PREFLOW_PUSH;
		
		this.property_change_delegate
			= new DefaultPropertyDelegate();		
		
		this.mFilterMap
	 		= new HashMap<String, IFilter>();
		
		String[] property_names 
			= {
				PartitionerModelMessages.MODULE_COARSENER.NAME,
				PartitionerModelMessages.PROFILER_TRACE.NAME,
				PartitionerModelMessages.MODULE_EXPOSER.NAME,
				PartitionerModelMessages.HOST_CONFIGURATION.NAME,
				PartitionerModelMessages.SET_MODULE_EXPOSURE.NAME,
				PartitionerModelMessages.SET_SYNTHETIC_NODE.NAME,
				PartitionerModelMessages.SET_PRESET_MODULE_GRAPH.NAME,
				PartitionerModelMessages.PARTITIONER_TYPE.NAME,
				PartitionerModelMessages.INTERACTION_COST.NAME,
				PartitionerModelMessages.EXECUTION_COST.NAME,
				PartitionerModelMessages.SIMULATION_FRAMEWORK.NAME,
				PartitionerModelMessages.MODULE_MODEL.NAME,
				PartitionerModelMessages.HOST_MODEL.NAME,
				PartitionerModelMessages.ACTIVATE_HOST_COST_FILTER.NAME,
				PartitionerModelMessages.ACTIVATE_INTERACTION_COST_FILTER.NAME,
				PartitionerModelMessages.GENERATE_TEST_FRAMEWORK.NAME,
				PartitionerModelMessages.MODEL_STATE.NAME,
				
				PartitionerModelMessages.PARTITIONER.NAME
			};
		
		Object[] properties
			= {
			this.mModuleType,
			this.profiler_trace,
			this.constraint_xml_path,
			this.host_configuration,
			this.activate_module_exposure,
			this.use_synthetic_node_filters,
			this.use_module_model_parser,
			this.partitioner_type,
			this.interaction_cost_type,
			this.execution_cost_type,
			// does not need to be a member field, since now
			// it is in the map
			this.mSimFramework,
			null, //this.mModuleModel,
			null, //this.mHostModel,
			
			this.use_host_cost_filter,
			this.use_interaction_cost_filter,
			this.generate_test_framework,
			
			this.state,
			
			null // this.partitioner
		};
		
		this.property_change_delegate.registerProperties(
			property_names,
			properties
		);
	}

	synchronized public void
	setModuleCoarsener
	( ModuleCoarsenerType model_coarsener )
	{
		System.out.println("Inside setModuleCoarsener");
		ModuleCoarsenerType old_coarsener 
			= this.mModuleType;
		this.mModuleType 
			= model_coarsener;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.MODULE_COARSENER,
			old_coarsener, 
			this.mModuleType
		);
	}
	
	synchronized public void
	setProfilerTracePath
	( String profiler_trace )
	{
		System.out.println("Inside partitioner gui state model.");
		
		String old_trace = this.profiler_trace;
		String formatted_profiler_trace 
		 	= profiler_trace.replace("/", System.getProperty("file.separator"));
		this.profiler_trace = formatted_profiler_trace;
		
		System.out.println( old_trace );
		System.out.println( this.profiler_trace + " " );
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.PROFILER_TRACE, 
			old_trace, 
			this.profiler_trace
		);
	}

	synchronized public void
	setModuleExposer
	( String module_exposer )
	{
		String old_exposer = this.constraint_xml_path;
		this.constraint_xml_path = module_exposer;
		
		this.property_change_delegate.firePropertyChange(
				PartitionerModelMessages.MODULE_EXPOSER, 
			old_exposer, 
			module_exposer
		);
	}
	
	synchronized public void
	setHostConfiguration
	( String host_configuration )
	{
		String old_configuration = this.host_configuration;
		this.host_configuration = host_configuration;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.HOST_CONFIGURATION, 
			old_configuration, 
			host_configuration
		);
	}
	
	synchronized public void
	setModuleExposure
	( Boolean expose )
	{
		boolean old_expose 
			= this.activate_module_exposure;
		this.activate_module_exposure 
			= expose;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.SET_MODULE_EXPOSURE, 
			old_expose, 
			expose
		);
	}
	
	synchronized public void
	setPresetModuleGraph
	( Boolean preset_module_graph )
	{
		boolean old_preset
			= this.use_module_model_parser;
		this.use_module_model_parser
			= preset_module_graph;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.SET_PRESET_MODULE_GRAPH,
			old_preset,
			preset_module_graph
		);
	}
	
	private void 
	setModuleModel
	( ModuleModel module_model ) 
	{
		ModuleModel old_module_model
			= this.mModuleModel;
		this.mModuleModel
			= module_model;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.MODULE_MODEL, 
			old_module_model, 
			this.mModuleModel
		);
	}
	
	private void
	setHostModel
	( HostModel host_model )
	{
		HostModel old_host_model
			= this.mHostModel;
		this.mHostModel
			= host_model;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.HOST_MODEL,
			old_host_model,
			this.mHostModel
		);
	}
	
	synchronized public void
	setPartitionerType
	( PartitionerType partitioner_type )
	{
		PartitionerType old_partitioner
			= this.partitioner_type;
		this.partitioner_type
			= partitioner_type;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.PARTITIONER_TYPE,
			old_partitioner,
			partitioner_type
		);
	}
	
	synchronized public void
	setInteractionCost
	( InteractionCostType interaction_cost )
	{
		InteractionCostType old_interaction
			= this.interaction_cost_type;
		this.interaction_cost_type
			= interaction_cost;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.INTERACTION_COST,
			old_interaction,
			interaction_cost
		);
	}
	
	synchronized public void
	setExecutionCost
	( ExecutionCostType execution_cost )
	{
		ExecutionCostType old_execution
			= this.execution_cost_type;
		this.execution_cost_type
			= execution_cost;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.EXECUTION_COST,
			old_execution,
			execution_cost
		);
	}
	
	synchronized public void
	setGenerateTestFramework
	( Boolean generate )
	{
		Boolean old_generate
			= this.generate_test_framework ;
		this.generate_test_framework
			= generate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.GENERATE_TEST_FRAMEWORK,
			old_generate,
			this.generate_test_framework
		);
	}
	
	synchronized public void
	setSyntheticNode
	( Boolean synthetic )
	{
		boolean old_synthetic
			= this.use_synthetic_node_filters;
		this.use_synthetic_node_filters
			= synthetic;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.SET_SYNTHETIC_NODE,
			old_synthetic,
			this.use_synthetic_node_filters
		);
	}
	
	synchronized public void
	setActivateHostFilter
	( Boolean activate )
	{
		Boolean old_activate
			= this.use_host_cost_filter;
		this.use_host_cost_filter
			= activate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.ACTIVATE_HOST_COST_FILTER,
			old_activate,
			this.use_host_cost_filter
		);
	}
	
	synchronized public void
	setActivateInteractionCostFilter
	( Boolean activate )
	{
		Boolean old_activate
			= this.use_interaction_cost_filter;
		this.use_interaction_cost_filter
			= activate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.ACTIVATE_INTERACTION_COST_FILTER,
			old_activate,
			this.use_interaction_cost_filter
		);
	}
	
    @Override
    synchronized public void 
	addPropertyChangeListener
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
		
	}

	@Override
	synchronized public void 
	removePropertyChangeListener
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}
	
	synchronized public void 
	initializeHostModel() 
	{
	 	// parsing the configuration for hosts involved in the system
	   	HostParser hostParser 
	   		= new HostParser();
	   	
		try {
			HostModel host_model 
				= hostParser.parse(
					this.host_configuration
				);
			this.setHostModel(host_model);
			
			this.mHostModel.setInteractionCostModel(
				InteractionFactory.getInteractionCostModel(
					this.interaction_cost_type
				)
			);
			this.mHostModel.setExecutionCostModel(
				ExecutionFactory.getInteractionCostModel(
					this.execution_cost_type
				)
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	synchronized public void
	changeState
	( State state )
	{
		State old_state = this.state;
		this.state = state;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.MODEL_STATE, 
			old_state,
			this.state
		);
	}

	////////////////////////////////////////////////////////////////////////////////////
	///	The following functions are called from a swing worker
	/// 		-> several variables are used to communicate across the functions:
	///				-> mModuleModel, set in createModuleModel()
	///				-> mModuleType, read twice
	///				-> mHostModel, set in initializeHostModel
	///			we should be safe from concurrency problems given that mModuleModel
	///			and mHostModel are set once and never modified
	///	What makes these functions interesting, other than the issue of thread safety
	///	is that they are a single operation that we want to be able to backtrack or
	/// recover from when it fails. This is an interesting problem.
	////////////////////////////////////////////////////////////////////////////////////
	
	synchronized public void 
	createModuleModel
	( InputStream in ) 
	throws Exception 
	{
		// don't assign to class variables until we know that no exception
		// is being thrown
		EntityConstraintModel new_constraint_model 
			= null;
		ModuleModel module_model
			= null;
		ModuleModelHandler module_handler
			= null;
		
		// If the Profile XML file belongs to a real trace of the application
        // (i.e., the "Preset Module Placement" checkbox is checked),
        // create the ModuleModel from the collected traces.
        if ( !this.use_module_model_parser ){  
        	
            if( this.activate_module_exposure ){
                // parsing the entity constraints to be 
            	// exposed in the dependency graph
                EntityConstraintParser ccParser 
                	= new EntityConstraintParser();
                
                // for concurrency we create a temporary
                // constraint model and assign at the end
                new_constraint_model
                	= ccParser.parse(
                		this.constraint_xml_path
	                );
            }
            
            // here we set the list of extra switch constraints 
            // that would affect the parsing of the model
            if (  new_constraint_model != null ){
            	new_constraint_model
            	 	.getConstraintSwitches().setSyntheticNodeActivated(
            	 		this.use_synthetic_node_filters   
            	 	);
            }
            else {
            	new_constraint_model = null;
            	if( this.mModuleType != ModuleCoarsenerType.BUNDLE ){
            		PartitionerModelExceptions.ModuleExposureException ex
            			= new PartitionerModelExceptions.ModuleExposureException(
	            			"Module Exposure must be activated in order to use a "
	            			+ "coarsener other than " 
	            			+ ModuleCoarsenerType.BUNDLE.getText()
	            		);
            		LogUtilities.logError(ex);
            		throw ex;
            	}
            }
            	 
        	JipRun jipRun 
            	= JipParser.parse(in);             
	        IModuleCoarsener moduleCoarsener 
	        	= ModuleCoarsenerFactory.getModuleCoarsener(
	        		this.mModuleType, 
	            	new_constraint_model
	            );     
            
            module_model
            	= moduleCoarsener.getModuleModelFromParser( jipRun );
           assert module_model != null : "Module model should not be null";
        }
        
        // If the Profile XML file carries only the hypothetical information for
        // potential module placements use the ModuleModelParser together with
        // host information in order to derive the ModuleModel, the ModuleHost
        // placement and the ModulePairHostPair interactions.
        else {
            ModuleModelParser mmParser 
            	= new ModuleModelParser();
            module_handler
            	= mmParser.parse(in);
            module_model
            	= module_handler.getModuleModel();
            
            assert module_model != null : "Module model should not be null";
        }
        
        this.mConstraintModel 
        	= new_constraint_model;
        PartitionerModel.this.setModuleModel( 
        	module_model
        );
        this.mModuleModel
        	= module_model;
        this.mmHandler
        	= module_handler;
	}

	synchronized public void 
	finished() 
	{
		final ModuleModel module_model 
			= this.mModuleModel;
		
		// this threw and exception
		if( module_model == null ){
			throw new RuntimeException("No module model can be retrieved.");                                              
		}
		
		// After parsing the input of a profiling trace, a template is added
		// to the simulation framework to be later on used to create multiple
		// instances of the test units for testing against the distribution.
		assert this.mSimFramework != null : "mSimFramework needs to be initialized!";
		assert this.mModuleType != null : "mModuleType needs to be initialized!";
		
		this.mSimFramework.addTemplate(
			new SimulationUnit(
				module_model.getName(), 
				this.mModuleType.getText(), 
				new DistributionModel(module_model, mHostModel))
			);
		
		this.property_change_delegate.registerProperties(
			new String[]{
				PartitionerModelMessages.AFTER_MODEL_CREATION_MODULE_EXCHANGE_MAP.NAME
			},
			new Object[]{
				this.mModuleModel.getModuleExchangeMap()
			}
		);
	
		this.property_change_delegate.firePropertyChange(
			PartitionerModelMessages.ALGORITHM,
			null,
			this.partitioner_type.getText()
		);
	}
	
	// called from swing worker-> obtains the reference itself
	// big problem
	synchronized public ModuleModel
	getModuleModel()
	{
		return this.mModuleModel;
	}

	// called from swing worker 
	synchronized public void 
	runPartitioningAlgorithm() 
	{
        if (this.mHostModel.getInteractionCostModel() == null)
            throw new RuntimeException("No Interaction Cost Model is set");   
         if (this.mHostModel.getExecutionCostModel() == null)
            throw new RuntimeException("No Execution Cost Model is set"); 
        
        assert( this.partitioner_type != null)
        	: "The partitioner algorithm should not be null";
        
        IPartitioner partitioner 
        	= PartitionerFactory.getPartitioner(
        		this.partitioner_type
        	);        

        if (this.mModuleModel.isSimulation()){
            partitioner.init(
            	this.mmHandler.getModuleModel(), 
            	this.mHostModel, 
            	this.mmHandler.getModuleHostPlacementList()
            );
        } else if (!this.mModuleModel.isSimulation()){                    
            partitioner.init(this.mModuleModel, this.mHostModel);                    
        }
        
        for (IFilter f : this.mFilterMap.values()){
            partitioner.addFilter(f);    
        }
        partitioner.partition();
        
        // david - it may be better to store the entire 
        // IPartitioner instead
        this.updatePartitioner( partitioner );
        
	}
	
	private void 
	updatePartitioner
	( IPartitioner partitioner ) 
	{
		this.partitioner = partitioner;
		this.property_change_delegate.update_property(
	    	PartitionerModelMessages.PARTITIONER.NAME, 
	    	this.partitioner
	    );
	}

	synchronized public void
	doModelGeneration()
	{
		System.out.println("Generating Model");
		
		try{ 
			// for concurrency, we cache the references we will work with
			final String profiler_trace_path
				 = this.profiler_trace;
			
			if(  	profiler_trace_path == null 
					|| profiler_trace_path.equals("") ) {
				throw new Exception("No profiler dump data is provided.");   
			}
			
           	if( this.host_configuration.equals("") ) {
           		throw new Exception ( "No host layout is provided." );
           	}
           	
           	this.initializeHostModel();
           
           	// reading the input stream for the profiling XML document 
           	// provided to the tool.
           	GenerateModelJob job 
           		= new GenerateModelJob(
           			profiler_trace_path,
           			this,
           			this.property_change_delegate
           		);
           	
           	job.setUser(true);
           	job.setPriority(GenerateModelJob.SHORT);
           	job.schedule();
        }catch(Exception e){    
        	e.printStackTrace();
        }
	}
	
	synchronized public void 
	initializeFilters() 
	{
		// we clear in case we are trying the operation a second time
		// because the first time failed
		this.mFilterMap.clear();
		
		Map<String, IFilter> map = null;
		if( this.use_synthetic_node_filters ){
			
			// not the right place for this filter
//				map = FilterHelper.setFilter(
//					this.mModuleModel, 
//					this.mHostModel, 
//					this.mConstraintModel.getFilterConstraintModel(),
//					FilterType.COLOCATION_CUT
//				);
//				
//			this.mFilterMap.putAll(map);
//			System.err.println("Number of synthetic filters: " + map.size());
		}
		
		if( this.use_host_cost_filter ){
				map = FilterHelper.setFilter(
					this.mModuleModel, 
					this.mHostModel, 
					this.mConstraintModel.getFilterConstraintModel(),
					FilterType.HOST_CUT
				);
				
			this.mFilterMap.putAll(map);
			System.err.println("Number of host filters: " + map.size());
		}
		
		if( this.use_interaction_cost_filter ){
				map = FilterHelper.setFilter(
					this.mModuleModel, 
					this.mHostModel, 
					this.mConstraintModel.getFilterConstraintModel(),
					FilterType.COLOCATION_CUT
				);
				
			this.mFilterMap.putAll(map);
			System.err.println("Number of interaction filters: " + map.size());
		}
	}

	synchronized public void
	doPartitionGeneration()
	{
		GeneratePartitionJob job 
   			= new GeneratePartitionJob(this, this.property_change_delegate);
	   	job.setUser(true);
	   	job.setPriority(GenerateModelJob.SHORT);
	   	job.schedule();
	}
	
	
	
	@Override
	synchronized public Map<String, Object> 
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}
	
	synchronized public void 
	createTestFrameworkModel()
	{
		this.property_change_delegate.registerProperties(
			new String[]{ 
				PartitionerModelMessages.AFTER_PARTITIONING_COMPLETE_TEST_FRAMEWORK.NAME
			},
			new Object[]{
				new TestFrameworkModel(
					PartitionerModel.this.mModuleModel,
					PartitionerModel.this.mSimFramework,
					PartitionerModel.this.mHostModel,
					PartitionerModel.this.profiler_trace,
					PartitionerModel.this.mModuleType,
					PartitionerModel.this.mConstraintModel
				)
			}
		);
		this.property_change_delegate.notifyViews(
			PartitionerModelMessages.VIEW_CREATE_TEST_FRAMEWORK, 
			null
		);
	}

	synchronized public boolean 
	generateTestFramework() 
	{
		return this.generate_test_framework;
	}
}

///////////////////////////////////////////////////////////////////
///
///////////////////////////////////////////////////////////////////

class 
GenerateModelJob 
extends Job
{
	private String profiler_trace_path;
	private PartitionerModel gui_state_model;
	private DefaultPropertyDelegate property_change_delegate;
	
	GenerateModelJob
	( 	String profiler_trace_path, 
		PartitionerModel gui_state_model, 
		DefaultPropertyDelegate property_change_delegate )
	{
		super("Create Model");
		
		this.profiler_trace_path
			= profiler_trace_path;
		this.gui_state_model
			= gui_state_model;
		this.property_change_delegate
			= property_change_delegate;
	}
	
	@Override
	public IStatus 
	run
	( IProgressMonitor monitor ) 
	{
		InputStream in = null; 
		try {
			in =  new BufferedInputStream(
					new FileInputStream(
						this.profiler_trace_path
					)
			);
			if( monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			assert in != null : "The input stream should have been generated correctly";
			this.gui_state_model.createModuleModel( in );
			in.close();
			this.gui_state_model.finished();
			
			this.gui_state_model.changeState(State.MODEL_BEFORE_PARTITION);
			this.property_change_delegate.notifyViews(
					PartitionerModelMessages.MODEL_CREATED, 
					null
			);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (PartitionerModelExceptions.ModuleExposureException e){
			e.printStackTrace();
			this.property_change_delegate.notifyViews(
					PartitionerModelMessages.MODEL_EXCEPTION, 
					e
				);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if( in != null ){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		return Status.OK_STATUS;
	}
}

class 
GeneratePartitionJob
extends org.eclipse.core.runtime.jobs.Job
{
	PartitionerModel gui_state_model;
	private DefaultPropertyDelegate property_change_delegate;
	
	public 
	GeneratePartitionJob
	(PartitionerModel model, DefaultPropertyDelegate property_change_delegate ) 
	{
		super("Perform Model Partition");
		this.gui_state_model = model;
		this.property_change_delegate
			= property_change_delegate;
	}
	
	@Override
	protected IStatus 
	run
	( IProgressMonitor monitor ) 
	{
		System.out.println("Partitioning the model");
		
		try { 
			// I am doing this as late as possible
			this.gui_state_model.initializeFilters();
		
		System.out.println("Performing partition");
		this.gui_state_model.runPartitioningAlgorithm();
		
		/// after finished
		// load the test framework if partitioning was set
		// I wonder what this was originally for
		if( this.gui_state_model.generateTestFramework() ){
			this.gui_state_model.createTestFrameworkModel();
		}
		
		this.property_change_delegate.notifyViews(
			PartitionerModelMessages.PARTITIONING_COMPLETE, 
			null
		);
		this.gui_state_model
			.changeState(State.PARTITIONED);
		
		}
		catch( PartitionerModelExceptions.FilterHostColocationException ex){
			ex.printStackTrace();
			this.property_change_delegate.notifyViews(
					PartitionerModelMessages.MODEL_EXCEPTION, ex
				);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return Status.OK_STATUS;
	}
}
