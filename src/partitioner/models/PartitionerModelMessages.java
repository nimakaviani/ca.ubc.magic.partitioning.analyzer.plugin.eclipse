package partitioner.models;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory.InteractionCostType;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory.PartitionerType;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import partitioner.models.PartitionerModel.State;
import plugin.mvc.messages.DataEvent;
import plugin.mvc.messages.FromModelEvent;
import plugin.mvc.messages.PropertyEvent;
import plugin.mvc.messages.ToModelEvent;
import plugin.mvc.messages.ViewsEvent;

public class 
PartitionerModelMessages 
{
	public static final FromModelEvent MODEL_CREATED
		= new FromModelEvent("ModelCreation", null);
	public static final FromModelEvent VIEW_CREATE_TEST_FRAMEWORK
		= new FromModelEvent("ViewCreateTestFramework", null);
	public static DataEvent PARTITIONING_COMPLETE
		= new FromModelEvent("PartitioningComplete", null);
	
	public static final ViewsEvent EDITOR_CLOSED
		= new ViewsEvent("EditorClosed", null);
	
	public static final ToModelEvent GENERATE_MODEL
		= new ToModelEvent("ModelGeneration");
	public static final ToModelEvent GENERATE_PARTITION
		= new ToModelEvent("PartitionGeneration");
	

	public static final PropertyEvent MODULE_EXPOSER
		= new PropertyEvent("ModuleExposer", String.class );
	public static final PropertyEvent HOST_CONFIGURATION
		= new PropertyEvent("HostConfiguration", String.class);
	public static final PropertyEvent SET_MODULE_EXPOSURE
		= new PropertyEvent("ModuleExposure", Boolean.class);
	public static final PropertyEvent SET_SYNTHETIC_NODE
		= new PropertyEvent("SyntheticNode", Boolean.class);
	public static final PropertyEvent MODULE_COARSENER
		= new PropertyEvent("ModuleCoarsener", ModuleCoarsenerType.class);
	public static final PropertyEvent PERFORM_PARTITIONING
		= new PropertyEvent("PerformPartitioning", Boolean.class );
	public static final PropertyEvent GENERATE_TEST_FRAMEWORK
		= new PropertyEvent("GenerateTestFramework", Boolean.class);
	public static final PropertyEvent ACTIVATE_HOST_COST_FILTER
		= new PropertyEvent("ActivateHostFilter", Boolean.class);
	public static final PropertyEvent ACTIVATE_INTERACTION_COST_FILTER
		= new PropertyEvent("ActivateInteractionCostFilter", Boolean.class);
	public static final PropertyEvent PARTITIONER_TYPE
		= new PropertyEvent("PartitionerType", PartitionerType.class );
	public static final PropertyEvent INTERACTION_COST
		= new PropertyEvent("InteractionCost", InteractionCostType.class );
	public static final PropertyEvent EXECUTION_COST
		= new PropertyEvent("ExecutionCost", ExecutionCostType.class);
	
	public static final PropertyEvent PROFILER_TRACE
		= new PropertyEvent("ProfilerTracePath", String.class);
	
	public static final PropertyEvent SET_PRESET_MODULE_GRAPH
		= new PropertyEvent("PresetModuleGraph", Boolean.class);
	public static final PropertyEvent MODULE_MODEL
		= new PropertyEvent("ModuleModel", ModuleModel.class);
	public static final PropertyEvent HOST_MODEL
		= new PropertyEvent("HostModel", HostModel.class);
	
	public static final PropertyEvent SIMULATION_FRAMEWORK
		= new PropertyEvent("SimulationFramework", SimulationFramework.class);
	
	public static final PropertyEvent SOLUTION
		= new PropertyEvent("Solution", String.class );
	public static final PropertyEvent ALGORITHM
		= new PropertyEvent("Algorithm", String.class );
	
	public static final PropertyEvent MODEL_STATE
		= new PropertyEvent("ModelState", State.class);
	public static final FromModelEvent MODEL_EXCEPTION 
		= new FromModelEvent("ModelException", Exception.class);
}
