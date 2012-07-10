
package partitioner.models;

import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ProgressMonitorInputStream;

import org.apache.commons.collections15.keyvalue.DefaultKeyValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import plugin.Constants;

import ca.ubc.magic.profiler.dist.model.DistributionModel;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory.InteractionCostType;
import ca.ubc.magic.profiler.dist.model.report.ReportModel;
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
import ca.ubc.magic.profiler.simulator.control.ISimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.StaticTimeSimulator;
import ca.ubc.magic.profiler.simulator.control.TimeSimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationFrameworkHelper;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

import snapshots.model.IModel;
import snapshots.model.PropertyChangeDelegate;

// this object is for sure accessed from the swt and swing threads
// it must be made thread safe!!!!!!!!!
public class 
PartitionerGUIStateModel 
implements IModel
// TODO: write an adapter around the Model so were more cleanly separate
//		the mvc interface from the program logic
// TODO: it is only by luck that the values in the model match the initial
//		values in the view; they must be synchronized at the outset through
//		some mechanism
{
	private PropertyChangeDelegate 	property_change_delegate;
	
	private ModuleCoarsenerType 	mModuleType; 
	private PartitionerType 		partitioner_type;
	private InteractionCostType 	interaction_cost_type;
	private ExecutionCostType 		execution_cost_type;

	private volatile String			profiler_trace;  
	private volatile String 		constraint_xml_path;
	private volatile String 		host_configuration; 
	
	private volatile Boolean		configuration_panel_enabled = true;
	
	private ModuleModelHandler 		mmHandler;
	private SimulationFramework	 	mSimFramework; 
	
	// the following is set once in the SwingWorker thread
	private volatile ModuleModel 			mModuleModel;
	// the following is set once in the SwingWorker thread
	private volatile HostModel   			mHostModel; 
	private volatile EntityConstraintModel	mConstraintModel;

	// volatile due to visibility concerns
	private volatile Boolean active_exposing_menu = false;
	private volatile Boolean enable_synthetic_node = false;
	private volatile Boolean module_model_checkbox = false;
	 
	private volatile Boolean perform_partitioning = false;
	
	private Map<String, IFilter> mFilterMap; 
	 	
	private String active_host_filter;
	private String interaction_model;
	private String execution_model;
	private String partitioner_solution;
	
	private ISimulator mSim 
		= SimulatorFactory.getSimulator(
			SimulatorType.fromString("Static Time Simulator (No Trace Replay)")
	  	);
    private javax.swing.JFileChooser fileChooser;

	private JipRun jipRun;

	private IModuleCoarsener module_coarsener;
	
	private SimulationUnit mBestSimUnit;
	
	public
	PartitionerGUIStateModel()
	{
		this.profiler_trace = "";
		this.constraint_xml_path	= "";
		this.host_configuration = "";
       	
		this.mSimFramework
       		= new SimulationFramework(Boolean.FALSE);
		
		this.mModuleType	
			= ModuleCoarsenerType.BUNDLE;
		this.interaction_cost_type
			= InteractionCostType.IGNORE;
		this.execution_cost_type
			= ExecutionCostType.EXECUTION_TIME;
		this.partitioner_type
			= PartitionerType.MIN_MAX_PREFLOW_PUSH;
		
		this.property_change_delegate
			= new PropertyChangeDelegate();		
		
		this.mFilterMap
	 		= new HashMap<String, IFilter>();
		
		this.unitMap
			= new HashMap<String, DefaultKeyValue>();
		
		this.initializeForActivation();
		
		this.registerProperties();
	}

	public void 
	initializeForActivation() 
	{
	   	initFilters();
	   	initCostModels();
	}
	
	private void 
    initFilters() 
    { 
    	this.mFilterMap 
    		= new HashMap<String, IFilter>();
    	this.active_host_filter
    		= null;
    }
	
	 private void 
    initCostModels()
    {
    	// TODO: ask Nima if there are defaults for these
    	// also, keep an eye out for any clues that the following should
    	// reference non-String objects
    	this.interaction_model 
    		= null;
    	this.execution_model 
    		= null;
    }
	
	public String
	getHostConfigurationPath()
	{
		return this.host_configuration;
	}
	
	public String
	getModuleExposerPath()
	{
		return this.constraint_xml_path;
	}
	
	public String
	getProfilerTracePath()
	{
		return this.profiler_trace;
	}
	
	public void
	setModuleCoarsener
	( ModuleCoarsenerType model_coarsener )
	{
		System.out.println("Inside setModuleCoarsener");
		ModuleCoarsenerType old_coarsener 
			= this.mModuleType;
		this.mModuleType 
			= model_coarsener;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_MODULE_COARSENER,
			old_coarsener, 
			this.mModuleType
		);
	}
	
	public void
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
			Constants.GUI_PROFILER_TRACE, 
			old_trace, 
			this.profiler_trace
		);
	}
	
	public void
	setModuleExposer
	( String module_exposer )
	{
		String old_exposer = this.constraint_xml_path;
		this.constraint_xml_path = module_exposer;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_MODULE_EXPOSER, 
			old_exposer, 
			module_exposer
		);
	}
	
	public void
	setHostConfiguration
	( String host_configuration )
	{
		String old_configuration = this.host_configuration;
		this.host_configuration = host_configuration;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_HOST_CONFIGURATION, 
			old_configuration, 
			host_configuration
		);
	}
	
	public void
	setModuleExposure
	( Boolean expose )
	{
		boolean old_expose 
			= this.active_exposing_menu;
		this.active_exposing_menu 
			= expose;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_SET_MODULE_EXPOSURE, 
			old_expose, 
			expose
		);
	}
	
	public void
	setSyntheticNode
	( Boolean synthetic )
	{
		boolean old_synthetic
			= this.enable_synthetic_node;
		this.enable_synthetic_node
			= synthetic;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_SET_SYNTHETIC_NODE,
			old_synthetic,
			synthetic
		);
	}
	
	public void
	setPresetModuleGraph
	( Boolean preset_module_graph )
	{
		boolean old_preset
			= this.module_model_checkbox;
		this.module_model_checkbox
			= preset_module_graph;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_SET_PRESET_MODULE_GRAPH,
			old_preset,
			preset_module_graph
		);
	}
	
	public void
	setPerformPartitioning
	( Boolean perform_partitioning )
	{
		boolean old_perform
			= this.perform_partitioning;
		this.perform_partitioning
			= perform_partitioning;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_PERFORM_PARTITIONING,
			old_perform,
			perform_partitioning
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
			Constants.MODULE_MODEL, 
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
			Constants.HOST_MODEL,
			old_host_model,
			this.mHostModel
		);
	}
	
	public void
	setPartitionerType
	( PartitionerType partitioner_type )
	{
		PartitionerType old_partitioner
			= this.partitioner_type;
		this.partitioner_type
			= partitioner_type;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_PARTITIONER_TYPE,
			old_partitioner,
			partitioner_type
		);
	}
	
	public void
	setInteractionCost
	( InteractionCostType interaction_cost )
	{
		InteractionCostType old_interaction
			= this.interaction_cost_type;
		this.interaction_cost_type
			= interaction_cost;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_INTERACTION_COST,
			old_interaction,
			interaction_cost
		);
	}
	
	public void
	setExecutionCost
	( ExecutionCostType execution_cost )
	{
		ExecutionCostType old_execution
			= this.execution_cost_type;
		this.execution_cost_type
			= execution_cost;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_EXECUTION_COST,
			old_execution,
			execution_cost
		);
	}
	
    @Override
	public void 
	addPropertyChangeListener
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
		
	}

	@Override
	public void 
	removePropertyChangeListener
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}
	
	public void 
	initializeHostModel() 
	{
	 	// parsing the configuration for hosts involved in the system
	   	HostParser hostParser 
	   		= new HostParser();
	   	
		try {
			HostModel host_model 
				= hostParser.parse(
					this.getHostConfigurationPath()
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
	
	public void
	setSimulationType
	( String simulation_type )
	{
		 SimulatorType type 
		 	= SimulatorType.fromString( simulation_type );
		 this.mSim = SimulatorFactory.getSimulator(type);
	        switch (type){
	            case TIME_SIMULATOR:
	                try{
	                    if( this.profiler_trace == null || this.profiler_trace.equals("")){
	                        throw new RuntimeException("No profiler dump data is provided.");
	                    }
	                    InputStream in 
	                    	= new BufferedInputStream(
	                            new ProgressMonitorInputStream(
	                            	this.fileChooser,
	                                "Reading " +  this.profiler_trace,
	                                new FileInputStream( this.profiler_trace )
	                            )
	                        );   

	                    this.jipRun = JipParser.parse(in);       
	                    ((TimeSimulator) this.mSim).init(this.jipRun, this.module_coarsener);
	                }catch(Exception ex){
	                    ex.printStackTrace();
	                }
	                break;
	            case STATIC_TIME_SIMULATOR:
	                ((StaticTimeSimulator) this.mSim).init( this.mModuleModel );
	                break;
	            default:
	                throw new RuntimeException("A simulator needs to be selected.");
	        }
	}
	
	public void 
	createModuleModel
	( InputStream in ) 
	{
		try{
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
	        if ( !this.module_model_checkbox ){  
	        	
	            if( this.active_exposing_menu ){
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
	            	 		this.enable_synthetic_node   
	            	 	);
	            }
	            	 
            	JipRun jipRun 
	            	= JipParser.parse(in);             
		        IModuleCoarsener moduleCoarsener 
		        	= ModuleCoarsenerFactory.getModuleCoarsener(
		        		this.mModuleType, 
		            	// david - the constraint model could be null here
		            	// in which case the program will crash
		        		// TODO: ask nima to fix this: it basically means 
		        		// enable module exposure must be set
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
	        PartitionerGUIStateModel.this.setModuleModel( 
	        	module_model
	        );
	        this.mModuleModel
	        	= module_model;
	        this.mmHandler
	        	= module_handler;
	        
		} catch( Exception ex ){
			ex.printStackTrace();
		}
	}

	public void 
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
		SimulationFramework simulation_framework 
			= this.mSimFramework;
		ModuleCoarsenerType module_type
			= this.mModuleType;
		
		assert simulation_framework != null : "mSimFramework needs to be initialized!";
		assert module_type != null : "mModuleType needs to be initialized!";
		
		simulation_framework.addTemplate(
			new SimulationUnit(
				module_model.getName(), 
				module_type.getText(), 
				new DistributionModel(module_model, mHostModel))
			);
		
		this.prepare_test_framework();
		
		System.out.println( this.perform_partitioning.toString() );
		if(this.perform_partitioning){
			System.out.println("Performing partition");
			this.runPartitioningAlgorithm();
		}
		
		this.property_change_delegate.firePropertyChange(
			Constants.MODULE_EXCHANGE_MAP, 
			null, 
			this.mModuleModel.getModuleExchangeMap()
		);
		this.property_change_delegate.firePropertyChange(
			Constants.ALGORITHM,
			null,
			this.partitioner_type.getText()
		);
		
		this.property_change_delegate.firePropertyChange(
			Constants.SOLUTION,
			null,
			this.partitioner_solution
		);
	}
	
	private void 
	prepare_test_framework() 
	{
		// also create the actual module coarsener object
		// used by the test framework
		this.module_coarsener
			= ModuleCoarsenerFactory.getModuleCoarsener(
				mModuleType, mConstraintModel
		);
		
		// also make a call to the combo box function to properly
		// initialize now that the right fields are set
		// ( profiler trace, mSim, mModuleModel )
		this.setSimulationType( SimulatorType.TIME_SIMULATOR.getText() );
	}

	// called from swing worker-> obtains the reference itself
	// big problem
	public ModuleModel
	getModuleModel()
	{
		return this.mModuleModel;
	}

	// called from swing worker 
	// TODO: thread safety concerns have been reintroduced in full force
	private void 
	runPartitioningAlgorithm() 
	{
		
		try{
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
            this.partitioner_solution
            	= partitioner.getSolution();
        }catch( Exception ex ){
        	ex.printStackTrace();
        }
	}
	
	public void
	doModelGeneration()
	{
		System.out.println("Generating Model");
		
		try{ 
			// for concurrency, we cache the references we will work with
			final String profiler_trace_path
				 = this.getProfilerTracePath();
			
			if(  	profiler_trace_path == null 
					|| profiler_trace_path.equals("") ) {
				throw new Exception("No profiler dump data is provided.");   
			}
			
           	if( this.getHostConfigurationPath()
           			.equals("") ) {
           		throw new Exception ( "No host layout is provided." );
           	}
           	
           	this.initializeHostModel();
           
           	// reading the input stream for the profiling XML document 
           	// provided to the tool.
           	PartitionerGUIStateModel.GenerateModelJob job 
           		= new PartitionerGUIStateModel.GenerateModelJob(
           			profiler_trace_path,
           			this
           		);
           	
           	job.setUser(true);
           	job.setPriority(GenerateModelJob.SHORT);
           	job.schedule();
        }catch(Exception e){    
        	e.printStackTrace();
        }
	}
	
	///////////////////////////////////////////////////////////////////
	///
	///////////////////////////////////////////////////////////////////
	
	class 
	GenerateModelJob 
	extends org.eclipse.core.runtime.jobs.Job
	{
		private String profiler_trace_path;
		private PartitionerGUIStateModel gui_state_model;
		
		GenerateModelJob
		( String profiler_trace_path, PartitionerGUIStateModel gui_state_model )
		{
			super("Create Model");
			
			this.profiler_trace_path
				= profiler_trace_path;
			this.gui_state_model
				= gui_state_model;
		}
		@Override
		protected IStatus 
		run
		( IProgressMonitor monitor ) 
		{
			try {
				final InputStream in 
					=  new BufferedInputStream(
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
				
				PartitionerGUIStateModel.this
					.property_change_delegate.firePropertyChange(
						Constants.ACTIVE_CONFIGURATION_PANEL, true, false
					);
				PartitionerGUIStateModel.this
					.property_change_delegate.firePropertyChange(
						Constants.MODEL_CREATION, null, null
					);
				
				// load the test framework if partitioning was set
				if(PartitionerGUIStateModel.this.perform_partitioning){
					PartitionerGUIStateModel.this.addSimulationUnit();
				}
				else { System.err.println("Not performing partitining."); }
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return Status.OK_STATUS;
		}
	}
	
	@Override
	public Object[] 
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}
	
	private void 
	registerProperties() 
	{
		// any registered properties should only be updated
		// through a call to a set... function, where the
		// ending is the string behind the constant
		String[] property_names 
			= {
			Constants.GUI_MODULE_COARSENER,
			Constants.GUI_PROFILER_TRACE,
			Constants.GUI_MODULE_EXPOSER,
			Constants.GUI_HOST_CONFIGURATION,
			Constants.GUI_SET_MODULE_EXPOSURE,
			Constants.GUI_SET_SYNTHETIC_NODE,
			Constants.GUI_SET_PRESET_MODULE_GRAPH,
			Constants.GUI_PERFORM_PARTITIONING,
			Constants.GUI_PARTITIONER_TYPE,
			Constants.GUI_INTERACTION_COST,
			Constants.GUI_EXECUTION_COST,
			Constants.ACTIVE_CONFIGURATION_PANEL,
			Constants.SIMULATION_FRAMEWORK,
			Constants.MODULE_MODEL,
			Constants.HOST_MODEL,
		};
		
		Object[] properties
			= {
			this.mModuleType,
			// problem: profiler trace is not being modified
			this.profiler_trace,
			this.constraint_xml_path,
			this.host_configuration,
			this.active_exposing_menu,
			this.enable_synthetic_node,
			this.module_model_checkbox,
			this.perform_partitioning,
			this.partitioner_type,
			this.interaction_cost_type,
			this.execution_cost_type,
			// does not need to be a member field, since now
			// it is in the map
			this.configuration_panel_enabled,
			this.mSimFramework,
			this.mModuleModel,
			this.mHostModel
		};
		
		assert property_names.length == properties.length 
			: "The property names list must match the properties list in length";
		
		for( int i = 0; i < property_names.length; ++i ){
			this.property_change_delegate.registerProperty(
				property_names[i], 
				properties[i]
			);
		}
	}
		
	///////////////////////////////////////////////////////////
	///	TODO: Test framework classes; to be repositioned later
	///////////////////////////////////////////////////////////
	
	private Map<String, DefaultKeyValue> unitMap;

	private int id;
	  
	private void 
	addSimulationUnit()
	{
		// note: the test framework should only be active if partitioning
		//		 is performed
		
		// assume partitioning is performed for now...
		String partitioner_name
			= this.partitioner_type.getText();
		try{
	         if (mModuleModel == null){
	             throw new Exception("moduleModel is empty, no partitioning performed");
	         }
	         if (!mModuleModel.isPartitioned()){
	             throw new Exception("moduleModel is not partitioned yet.");
	         }
	         mSimFramework.addUnit( 
	        	new SimulationUnit(
	                 mModuleModel.getName(), 
	                 partitioner_name, 
	                 new DistributionModel(mModuleModel, mHostModel)), Boolean.FALSE
	            );
		}catch (Exception e){
			e.printStackTrace();
		}
		
		System.err.println("Loaded test framework");
		
		// now it should be possible to use the test framework
	}
	
	///////////////////////////////////////////////////////////////////////
	
	public void
	setSimulationAdded
	( SimulationUnit unit )
	{
		System.out.println("Inserting " + unit.getKey());
        this.unitMap.put(
        	unit.getKey(), 
        	new DefaultKeyValue(
	        	this.id, 
	        	unit.getSignature()
        	)
        );
    	System.out.println("Extracting " + unit.getKey());
	    	
	    this.property_change_delegate.firePropertyChange(
	    	Constants.GUI_SIMULATION_ADDED, null, unit
	    );
	    
	    this.property_change_delegate.firePropertyChange(
				Constants.INCREMENT_ID, this.id, ++this.id
			);
	}
		private void 
	setIncrementID
	( int id ) 
	{
		int old_id 
			= this.id;
		this.id
			= id;
		
		this.property_change_delegate.firePropertyChange(
			Constants.INCREMENT_ID, old_id, this.id
		);
	}
	
	public void
	doRunSimulation()
	{
		
		try{
			System.err.println("Running simulation");
			
            Display.getDefault().syncExec(
            	new Runnable(){
					@Override
					public void 
					run() 
					{
	                   PartitionerGUIStateModel.this
	                   		.mSimFramework.run(
	                   			PartitionerGUIStateModel.this.mSim
	                   		);
					}
	           });
       }catch( Exception ex ){
           ex.printStackTrace();
       }
	}
	
	public void
	setSimulationRemoved
	( SimulationUnit unit )
	{
        unitMap.remove(unit.getKey());
	}
	
	
	public void
	setUpdateSimulationReport
	( Object[] args )
	{
		System.err.println("Inside setUpdateSimulationReport with array");
		
		//System.err.println("Inside set simulation report");
		SimulationUnit unit
			= (SimulationUnit) args[0];
		ReportModel report
			= (ReportModel) args[1];
		
		System.out.println( "Extracting " + unit.getKey() );
    	String key
    		= unit.getKey();
    	System.out.println("Size " + unitMap.size() );
    	DefaultKeyValue key_value
    		= unitMap.get( key );
    	Integer num = (Integer) key_value.getKey();
        int id = num;
        
        this.property_change_delegate.firePropertyChange(
        	Constants.SIMULATION_TABLE_RUN_UPDATE,
        	null,
        	new Object[] { 
        		id,
        		report.getCostModel().getExecutionCost(),
        		report.getCostModel().getCommunicationCost(),
        		report.getCostModel().getTotalCost()
        	}
        );
        
        /*
        TableColumn tcol = simTable.getColumnModel().getColumn(5);
        
        tcol.setCellRenderer(new CustomTableCellRenderer(id, Color.RED));
         ((DefaultTableModel)simTable.getModel()).setValueAt(
                report.getCostModel().getExecutionCost(), id, 3);
          ((DefaultTableModel)simTable.getModel()).setValueAt(
                report.getCostModel().getCommunicationCost(), id, 4);
        ((DefaultTableModel)simTable.getModel()).setValueAt(
                report.getCostModel().getTotalCost(), id, 5); */
	} 
	
	public void
	setUpdateBestSimulationReport
	( SimulationUnit unit )
	{
		System.err.println("Inside setBestSimulationReport ");
		 if( unit != this.mBestSimUnit ){
			 
			System.err.println("Firing events in updateBestSimulationReport ");
            this.mBestSimUnit = unit;
            
            this.property_change_delegate.firePropertyChange(
            	Constants.BEST_RUN_NAME,
            	null,
            	((Integer) (this.unitMap.get(unit.getKey()).getKey())) + ": " + unit.getName()
            );
            
            this.property_change_delegate.firePropertyChange(
            	Constants.BEST_RUN_ALGORITHM,
            	null,
            	unit.getAlgorithmName()
            );
            
            this.property_change_delegate.firePropertyChange(
            	Constants.BEST_RUN_COST, 
            	null,
            	Double.toString( unit.getUnitCost() )
            );
        }
	}
	
	public SimulationUnit
	findInSimulationUnits
	( Integer id )
	{
		SimulationUnit unit
			= null;
		
		for( DefaultKeyValue keyVal : unitMap.values() ){
			System.err.println("Key: "+ (Integer) keyVal.getKey());
            if (((Integer) keyVal.getKey()).equals(id)){
                unit = SimulationFrameworkHelper.getUnitFromSig(
                	( String ) keyVal.getValue(), mSimFramework.getTemplate(), Boolean.TRUE
                );
            }
		}
		
		return unit;
	}
}
