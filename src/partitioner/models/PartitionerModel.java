
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

import plugin.Constants;
import plugin.mvc.IModel;
import plugin.mvc.PropertyChangeDelegate;

import ca.ubc.magic.profiler.dist.model.DistributionModel;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
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
import ca.ubc.magic.profiler.simulator.control.ISimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

// this object is for sure accessed from the swt and swing threads
// it must be made thread safe!!!!!!!!!
public class 
PartitionerModel 
implements IModel
// TODO: write an adapter around the Model so were more cleanly separate
//		the mvc interface from the program logic
{
	public static final String	SIMULATION_FRAMEWORK
		= "SimulationFramework";
	public static final String	MODULE_MODEL
		= "ModuleModel";
	public static final String	HOST_MODEL
		= "HostModel";
	
	public static final String GUI_MODULE_COARSENER 		
		= "ModuleCoarsener";
	public static final String GUI_PROFILER_TRACE   		
		= "ProfilerTracePath";
	public static final String GUI_MODULE_EXPOSER 			
		= "ModuleExposer";
	public static final String GUI_HOST_CONFIGURATION 		
		= "HostConfiguration";
	public static final String GUI_SET_MODULE_EXPOSURE 		
		= "ModuleExposure";
	public static final String GUI_SET_SYNTHETIC_NODE 		
		= "SyntheticNode";
	public static final String GUI_SET_PRESET_MODULE_GRAPH 	
		= "PresetModuleGraph";
	public static final String GUI_PERFORM_PARTITIONING 
		= "PerformPartitioning";
	public static final String GUI_PARTITIONER_TYPE 
		= "PartitionerType";
	public static final String GUI_INTERACTION_COST 
		= "InteractionCost";
	public static final String GUI_EXECUTION_COST
		= "ExecutionCost";
	
	public static final String AFTER_MODEL_CREATION_MODULE_EXCHANGE_MAP 
		= "ModuleExchangeMap";
	
	public static final String GUI_ACTIVATE_HOST_COST_FILTER 
		= "ActivateHostFilter";
	public static final String GUI_ACTIVATE_INTERACTION_COST_FILTER
		= "ActivateInteractionCostFilter";
	
	public static final String GUI_GENERATE_TEST_FRAMEWORK 
		= "GenerateTestFramework";
	
	public static final String EVENT_MODEL_CREATED 
		= "ModelCreation";
	public static final String EVENT_GENERATE_MODEL 
		= "ModelGeneration";
	public static final String EVENT_GENERATE_PARTITION 
		= "PartitionGeneration";
	
	public static final String EVENT_PARTITIONING_COMPLETE 
		= "PartitioningComplete";
	public static final String AFTER_PARTITIONING_COMPLETE_TEST_FRAMEWORK 
		= "AfterPartitioningCreateTestFramework";
	
	 public static final String GUI_DISABLE_CONFIGURATION_PANEL 
		 = "ActiveConfigurationPanel";
	public static final String GUI_DISABLE_PARTITIONING_PANEL 
		= "ActivePartitioningPanel";
	
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
	 	
	private String partitioner_solution;
	
	private ISimulator mSim 
		= SimulatorFactory.getSimulator(
			SimulatorType.fromString(
				"Static Time Simulator (No Trace Replay)"
			)
	  	);
	// private javax.swing.JFileChooser fileChooser;

	//private JipRun jipRun;

	private Boolean activate_host_cost_filter = false;
	private Boolean activate_interaction_cost_filter = false;
	private Boolean generate_test_framework = false;
	private Boolean partitioning_panel_enabled = true;
	
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
			= new PropertyChangeDelegate();		
		
		this.mFilterMap
	 		= new HashMap<String, IFilter>();
		
		this.registerProperties();
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
			PartitionerModel.GUI_MODULE_COARSENER,
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
				PartitionerModel.GUI_PROFILER_TRACE, 
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
				PartitionerModel.GUI_MODULE_EXPOSER, 
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
				PartitionerModel.GUI_HOST_CONFIGURATION, 
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
				PartitionerModel.GUI_SET_MODULE_EXPOSURE, 
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
				PartitionerModel.GUI_SET_SYNTHETIC_NODE,
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
				PartitionerModel.GUI_SET_PRESET_MODULE_GRAPH,
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
			PartitionerModel.GUI_PERFORM_PARTITIONING,
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
			PartitionerModel.MODULE_MODEL, 
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
			PartitionerModel.HOST_MODEL,
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
			PartitionerModel.GUI_PARTITIONER_TYPE,
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
			PartitionerModel.GUI_INTERACTION_COST,
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
			PartitionerModel.GUI_EXECUTION_COST,
			old_execution,
			execution_cost
		);
	}
	
	public void
	setActivateHostFilter
	( Boolean activate )
	{
		Boolean old_activate
			= this.activate_host_cost_filter;
		this.activate_host_cost_filter
			= activate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModel.GUI_ACTIVATE_HOST_COST_FILTER,
			old_activate,
			this.activate_host_cost_filter
		);
	}
	
	public void
	setGenerateTestFramework
	( Boolean generate )
	{
		Boolean old_generate
			= this.generate_test_framework ;
		this.generate_test_framework
			= generate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModel.GUI_GENERATE_TEST_FRAMEWORK,
			old_generate,
			this.generate_test_framework
		);
	}
	
	public void
	setActivateInteractionCostFilter
	( Boolean activate )
	{
		Boolean old_activate
			= this.activate_interaction_cost_filter;
		this.activate_interaction_cost_filter
			= activate;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModel.GUI_ACTIVATE_INTERACTION_COST_FILTER,
			old_activate,
			this.activate_interaction_cost_filter
		);
	}
	
	public void 
	setActiveConfigurationPanel
	( Boolean active ) 
	{
		Boolean old_active
			= this.configuration_panel_enabled;
		this.configuration_panel_enabled
			= active;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModel.GUI_DISABLE_CONFIGURATION_PANEL,
			old_active,
			this.configuration_panel_enabled
		);
	}
	
	public void
	setActivePartitioningPanel
	( Boolean active )
	{
		Boolean old_active
			= this.partitioning_panel_enabled;
		this.partitioning_panel_enabled
			= active;
		
		this.property_change_delegate.firePropertyChange(
			PartitionerModel.GUI_DISABLE_PARTITIONING_PANEL,
			old_active,
			this.partitioning_panel_enabled
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
				PartitionerModel.AFTER_MODEL_CREATION_MODULE_EXCHANGE_MAP
			},
			new Object[]{
				this.mModuleModel.getModuleExchangeMap()
			}
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
           	PartitionerModel.GenerateModelJob job 
           		= new PartitionerModel.GenerateModelJob(
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
	
	public void
	doPartitionGeneration()
	{
		PartitionerModel.GeneratePartitionJob job 
   			= new PartitionerModel.GeneratePartitionJob();
	   	job.setUser(true);
	   	job.setPriority(GenerateModelJob.SHORT);
	   	job.schedule();
	}
	
	///////////////////////////////////////////////////////////////////
	///
	///////////////////////////////////////////////////////////////////
	
	class 
	GenerateModelJob 
	extends org.eclipse.core.runtime.jobs.Job
	{
		private String profiler_trace_path;
		private PartitionerModel gui_state_model;
		
		GenerateModelJob
		( String profiler_trace_path, PartitionerModel gui_state_model )
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
				
				PartitionerModel.this.setActiveConfigurationPanel( false );
				PartitionerModel.this
					.property_change_delegate.notifyViews(
						PartitionerModel.EVENT_MODEL_CREATED, null
					);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return Status.OK_STATUS;
		}
	}
	
	class 
	GeneratePartitionJob
	extends org.eclipse.core.runtime.jobs.Job
	{
		public 
		GeneratePartitionJob() 
		{
			super("Perform Model Partition");
		}

		@Override
		protected IStatus 
		run
		( IProgressMonitor monitor ) 
		{
			System.out.println("Partitioning the model");
			
			/// during finished
			System.out.println( 
				PartitionerModel.this
					.perform_partitioning.toString() 
				);
			if( PartitionerModel.this.perform_partitioning ){
				System.out.println("Performing partition");
				PartitionerModel.this.runPartitioningAlgorithm();
			}
			
			/// after finished
			// load the test framework if partitioning was set
			// I wonder what this was originally for
			if( PartitionerModel.this.generate_test_framework ){
				PartitionerModel.this.property_change_delegate
					.registerProperties(
						new String[]{ 
							PartitionerModel.AFTER_PARTITIONING_COMPLETE_TEST_FRAMEWORK
						},
						new Object[]{
							new TestFrameworkModel(
								PartitionerModel.this.partitioner_type,
								PartitionerModel.this.mModuleModel,
								PartitionerModel.this.mSimFramework,
								PartitionerModel.this.mHostModel,
								PartitionerModel.this.profiler_trace,
								PartitionerModel.this.mModuleType,
								PartitionerModel.this.mConstraintModel
							)
						}
					);
			}

			PartitionerModel.this
				.property_change_delegate.notifyViews(
					PartitionerModel.EVENT_PARTITIONING_COMPLETE, 
					null
				);
			
			return Status.OK_STATUS;
		}
		
	}
	
	@Override
	public Map<String, Object> 
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
				PartitionerModel.GUI_MODULE_COARSENER,
				PartitionerModel.GUI_PROFILER_TRACE,
				PartitionerModel.GUI_MODULE_EXPOSER,
				PartitionerModel.GUI_HOST_CONFIGURATION,
				PartitionerModel.GUI_SET_MODULE_EXPOSURE,
				PartitionerModel.GUI_SET_SYNTHETIC_NODE,
				PartitionerModel.GUI_SET_PRESET_MODULE_GRAPH,
				PartitionerModel.GUI_PERFORM_PARTITIONING,
				PartitionerModel.GUI_PARTITIONER_TYPE,
				PartitionerModel.GUI_INTERACTION_COST,
				PartitionerModel.GUI_EXECUTION_COST,
				PartitionerModel.GUI_DISABLE_CONFIGURATION_PANEL,
				PartitionerModel.GUI_DISABLE_PARTITIONING_PANEL,
				PartitionerModel.SIMULATION_FRAMEWORK,
				PartitionerModel.MODULE_MODEL,
				PartitionerModel.HOST_MODEL,
				PartitionerModel.GUI_ACTIVATE_HOST_COST_FILTER,
				PartitionerModel.GUI_ACTIVATE_INTERACTION_COST_FILTER,
				PartitionerModel.GUI_GENERATE_TEST_FRAMEWORK,
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
			this.partitioning_panel_enabled,
			this.mSimFramework,
			this.mModuleModel,
			this.mHostModel,
			
			this.activate_host_cost_filter,
			this.activate_interaction_cost_filter,
			this.generate_test_framework,
		};
		
		this.property_change_delegate.registerProperties(
			property_names,
			properties
		);
	}
}
