package plugin;

public class 
Constants
{
	// the following are used to communicate with the
	// active snapshot model
	public static final String PATH_PROPERTY 				
		= "SnapshotPath";
	public static final String NAME_PROPERTY
		= "SnapshotName";
	public static final String HOST_PROPERTY 				
		= "SnapshotHost";
	public static final String PORT_PROPERTY 				
		= "SnapshotPort";	
	
	public static final String KEY_ERR_DLGTITLE 			
		= "err_dlgttle";
	public static final String KEY_ERR_MSSG 				
		= "err_mssg";
	public static final String KEY_ERR_VALUES 				
		= "err_values";
	public static final String KEY_LOGS_DATA 				
		= "logdata";
	
    public static final String ACTKEY_ERROR_DISPLAY 		
    	= "err_display";
    public static final String ACTKEY_LOG_DISPLAY 			
    	= "log_action";
	public static final String EVENT_LIST_PROPERTY 			
		=  "AdditionalEvent";
	public static final String SELECTED_ENTRY_PROPERTY 		
		= "EntrySelected";
	
	public static final int SNAPSHOT_VIEW_UPDATE_MODEL_NAME
		= 0;
	
	public static final String PERSPECTIVE_SNAPSHOTS_VIEW_ID
		= "plugin.views.snapshot_view";
	public static final String ERROR_LOG_VIEW_ID 
		= "org.eclipse.pde.runtime.LogView";
	
	public static final String SNAPSHOT_CAPTURED_PROPERTY	
		= "snapshot_captured_message";
	public static final String SNAPSHOT_CAPTURE_FAILED 
		= "snapshot_failed_message";
	public static final String SNAPSHOT_STARTED 
		= "snapshot_started_message";
	
	public static final String GENERATE_MODEL_EVENT 
		= "ModelGeneration";
	public static final String MODULE_EXCHANGE_MAP 
		= "ModuleExchageMap";
	public static final String SOLUTION 
		= "Solution";
	public static final String ALGORITHM 
		= "Algorithm";
	public static final String PERSPECTIVE_CONFIGURATION_VIEW_ID 
		= "plugin.views.partitioner_configuration_view";
	
	public static final String INCREMENT_ID 
		= "IncrementID";
	public static final String SIMULATION_TABLE_RUN_UPDATE 
		= "RunSimulationTableUpdate";
	public static final String BEST_RUN_NAME 
		= "BestRunName";
	public static final String BEST_RUN_ALGORITHM 
		= "BestAlgorithmName";
	public static final String BEST_RUN_COST 
		= "BestRunCost";
	public static final String SIMULATION_UNITS 
		= "InSimulationUnits";
	
	// belonging to the test framework model
	public static final String GUI_SIMULATION_TYPE 
		= "SimulationType";
	public static final String EVENT_RUN_SIMULATION 
		= "RunSimulation";
	public static final String GUI_SIMULATION_ADDED 
		= "SimulationAdded";
	public static final String GUI_SIMULATION_REMOVED 
		= "SimulationRemoved";
	public static final String GUI_UPDATE_SIMULATION_REPORT 
		= "UpdateSimulationReport";
	public static final String GUI_UPDATE_BEST_SIMULATION_REPORT 
		= "UpdateBestSimulationReport";
	public static final String AFTER_PARTITIONING_CREATE_TEST_FRAMEWORK 
		= "AfterPartitioningCreateTestFramework";

}
