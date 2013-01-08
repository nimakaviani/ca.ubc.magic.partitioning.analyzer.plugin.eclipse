package partitioner.models;

import java.util.Map;

import plugin.mvc.EventTypes.FromModelEvent;
import plugin.mvc.EventTypes.PropertyEvent;
import plugin.mvc.EventTypes.ToModelEvent;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

public class 
TestFrameworkMessages 
{
	public static final FromModelEvent SIMULATION_TABLE_RUN_UPDATE
		= new FromModelEvent("RunSimulationTableUpdate", Object[].class );
	public static final FromModelEvent UPDATE_ID
		= new FromModelEvent("IncrementID", Integer.class);
	public static final FromModelEvent UPDATE_BEST_RUN_NAME
		= new FromModelEvent("BestRunName", String.class);
	public static final FromModelEvent UPDATE_BEST_RUN_ALGORITHM
		= new FromModelEvent("BestAlgorithmName", String.class);
	public static final FromModelEvent UPDATE_BEST_RUN_COST
		= new FromModelEvent("BestRunCost", String.class);
	
	public static final ToModelEvent RUN_SIMULATION
		= new ToModelEvent("RunSimulation");
	
	public static final PropertyEvent SIMULATION_ADDED
		= new PropertyEvent("SimulationAdded", SimulationUnit.class);
	public static final PropertyEvent SIMULATION_TYPE
		= new PropertyEvent("SimulationType", String.class);
	public static final PropertyEvent SIMULATION_REMOVED
		= new PropertyEvent("SimulationRemoved", SimulationUnit.class);
	public static final PropertyEvent UPDATE_SIMULATION_REPORT
		= new PropertyEvent("UpdateSimulationReport", Object[].class);
	public static final PropertyEvent UPDATE_BEST_SIMULATION_REPORT
		= new PropertyEvent("UpdateBestSimulationReport", SimulationUnit.class);
	
	public static final PropertyEvent UNIT_MAP
		= new PropertyEvent("UnitMap", Map.class);
	public static final PropertyEvent TEST_SIMULATION_FRAMEWORK
		= new PropertyEvent("SimulationFramework", SimulationFramework.class);
	public static final PropertyEvent TEST_MODULE_MODEL
		= new PropertyEvent("ModuleModel", ModuleModel.class);
	public static final PropertyEvent TEST_HOST_MODEL
		= new PropertyEvent("HostModel", HostModel.class);
}
