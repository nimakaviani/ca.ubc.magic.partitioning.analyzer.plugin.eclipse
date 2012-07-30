package partitioner.models;

import java.util.Map;

import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;
import plugin.mvc.messages.FromModelEvent;
import plugin.mvc.messages.IndexEvent;
import plugin.mvc.messages.PropertyEvent;
import plugin.mvc.messages.ToModelEvent;

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
	
	public static final IndexEvent SIMULATION_UNITS
		= new IndexEvent("InSimulationUnits", Integer.class);
	
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
}
