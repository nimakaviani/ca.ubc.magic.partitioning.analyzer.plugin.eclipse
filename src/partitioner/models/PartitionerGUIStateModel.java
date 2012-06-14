package partitioner.models;

import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import plugin.Constants;

import ca.ubc.magic.profiler.dist.model.DistributionModel;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
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
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

import snapshots.model.IModel;
import snapshots.model.PropertyChangeDelegate;

// TODO: make the default paths on a click to browse dependent on the
//		 current path 
// TODO: this object now needs to be made thread safe
//		try to split the parts that are accessed from multiple threads into
//		a separate class; distinguish between read-from-multiple and
//		written-from-multiple
public class 
PartitionerGUIStateModel 
implements IModel
{
	private PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	
	private ModuleCoarsenerType mModuleType 
		= ModuleCoarsenerType.BUNDLE;

	private String profiler_trace  
		= "C:/cygwin/home/dillesca/eece_496_workspace/plugin/resources/dist-model/Profile-20120504-190723.xml";
	private String module_exposer
		= "C:/cygwin/home/dillesca/eece_496_workspace/plugin/resources/dist-model/moduleconstraints-aries3.xml";
	private String host_configuration 
		= "C:/cygwin/home/dillesca/eece_496_workspace/plugin/resources/dist-model/host-magic.xml";
	
	private ModuleModelHandler mmHandler;
	private SimulationFramework mSimFramework  
		= new SimulationFramework(Boolean.FALSE);;
	
	 private ModuleModel 			mModuleModel;
	 private HostModel   			mHostModel;    
	 private EntityConstraintModel 	mConstraintModel;

	private boolean enable_module_exposure;
	private boolean enable_synthetic_node;
	private boolean preset_module_graph;
	
	 Map<String, IFilter> mFilterMap 
	 	= new HashMap<String, IFilter>();;
	 	
	String active_host_filter;
	String interaction_model;
	String execution_model;
	
	public String
	getHostConfigurationPath()
	{
		return this.host_configuration;
	}
	
	public String
	getModuleExposerPath()
	{
		return this.module_exposer;
	}
	
	// TODO called from swingworker
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
			model_coarsener
		);
		
		this.mModuleModel
		 	= null;
		
	}
	
	public void
	setProfilerTrace
	( String profiler_trace )
	{
		String old_trace = this.profiler_trace;
		this.profiler_trace = profiler_trace;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_PROFILER_TRACE, 
			old_trace, 
			profiler_trace
		);
	}
	
	public void
	setModuleExposer
	( String module_exposer )
	{
		String old_exposer = this.module_exposer;
		this.module_exposer = module_exposer;
		
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
			= this.enable_module_exposure;
		this.enable_module_exposure 
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
			= this.preset_module_graph;
		this.preset_module_graph
			= preset_module_graph;
		
		this.property_change_delegate.firePropertyChange(
			Constants.GUI_SET_PRESET_MODULE_GRAPH,
			old_preset,
			preset_module_graph
		);
	}
	
    private void 
    initFilters() 
    { 
    	mFilterMap 
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
    
    private void 
    initModuleCoarseners()
    {
    	this.mModuleType 
    		= ModuleCoarsenerType.BUNDLE;
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

	// called from swingworker
	public void 
	createModuleModel
	( InputStream in ) 
	{
		try{
		  // If the Profile XML file belongs to a real trace of the application
	        // (i.e., the "Preset Module Placement" checkbox is checked),
	        // create the ModuleModel from the collected traces.
	        if ( !PartitionerGUIStateModel.this.preset_module_graph ){     
	            if( PartitionerGUIStateModel.this.enable_module_exposure ){
	                // parsing the entity constraints to be 
	            	// exposed in the dependency graph
	                EntityConstraintParser ccParser 
	                	= new EntityConstraintParser();
	                PartitionerGUIStateModel.this.mConstraintModel 
	                	= ccParser.parse(
	                		PartitionerGUIStateModel.this.module_exposer
	                	);
	            }else {
	                PartitionerGUIStateModel.this.mConstraintModel 
	                	= null;
	            }
	            
	            // here we set the list of extra switch constraints 
	            // that would affect the parsing of the model
	            if (  PartitionerGUIStateModel.this.mConstraintModel != null ){
	            	 PartitionerGUIStateModel.this.mConstraintModel
	            	 	.getConstraintSwitches().setSyntheticNodeActivated(
	            	 			PartitionerGUIStateModel.this.enable_synthetic_node   
	            	 	);
	            }
	            
	            JipRun jipRun 
	            	= JipParser.parse(in);             
	            IModuleCoarsener moduleCoarsener 
	            	= ModuleCoarsenerFactory.getModuleCoarsener(
	            		PartitionerGUIStateModel.this.mModuleType, 
	            		PartitionerGUIStateModel.this.mConstraintModel
	            	);                                 
	            PartitionerGUIStateModel.this.mModuleModel 
	            	= moduleCoarsener.getModuleModelFromParser(jipRun);
	        }
	        // If the Profile XML file carries only the hypothetical information for
	        // potential module placements use the ModuleModelParser together with
	        // host information in order to derive the ModuleModel, the ModuleHost
	        // placement and the ModulePairHostPair interactions.
	        else {
	            ModuleModelParser mmParser 
	            	= new ModuleModelParser();
	            PartitionerGUIStateModel.this.mmHandler 
	            	= mmParser.parse(in);
	            PartitionerGUIStateModel.this.mModuleModel 
	            	= mmHandler.getModuleModel();
	        }
		} catch( Exception ex ){
			ex.printStackTrace();
		}
	}

	// called from swingworker
	public void 
	finished() 
	{
		if( PartitionerGUIStateModel.this.mModuleModel == null ){
			throw new RuntimeException("No module model can be retrieved.");                                              
		}
		
		// After parsing the input of a profiling trace, a template is added
		// to the simulation framework to be later on used to create multiple
		// instances of the test units for testing against the distribution.
		assert PartitionerGUIStateModel.this.mSimFramework != null : "mSimFramework needs to be initialized!";
		assert PartitionerGUIStateModel.this.mModuleModel != null : "mModuleModel needs to be initialized!";
		assert PartitionerGUIStateModel.this.mModuleType != null : "mModuleType needs to be initialized!";
		
		PartitionerGUIStateModel.this.mSimFramework.addTemplate(
			new SimulationUnit(
				PartitionerGUIStateModel.this.mModuleModel.getName(), 
				PartitionerGUIStateModel.this.mModuleType.getText(), 
				new DistributionModel(mModuleModel, mHostModel))
			);
	}
	
	// called from swing worker-> obtains the reference itself
	// big problem
	public ModuleModel
	getModuleModel()
	{
		return this.mModuleModel;
	}

	public void 
	initializeForActivation() 
	{
	 	this.mHostModel 
	   		= null;
	   	this.mModuleModel 
	   		= null;
	   	this.mConstraintModel 
	   		= null;
   
	   	// reset the filters for the system during each parsing of a new
	   	// profiling input and also reset the cost models after each
	   	// iteration of profiling
	   	initFilters();
	   	initCostModels();
	   	initModuleCoarseners();
	
	   	// parsing the configuration for hosts involved in the system
	   	HostParser hostParser 
	   		= new HostParser();
	   	try {
			this.mHostModel 
				= hostParser.parse(
					this.getHostConfigurationPath()
				);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String
	AbbreviatePath
	( String path_arg )
	{
		String path = path_arg.replace("\\", "/");
		
		String[] portions
			= path.split("/");
		StringBuilder sb = new StringBuilder();
		
		int start 
			= portions.length > 2 
			? portions.length - 3 
			: 0;
		for( int i = start; i < portions.length; ++i ){
			sb.append("/" + portions[i]);
		}
		
		return sb.toString();
	}
}
