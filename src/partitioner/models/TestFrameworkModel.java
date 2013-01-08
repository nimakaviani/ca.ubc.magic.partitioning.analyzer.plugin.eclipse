package partitioner.models;

import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ProgressMonitorInputStream;

import org.apache.commons.collections15.keyvalue.DefaultKeyValue;
import org.eclipse.swt.widgets.Display;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
import ca.ubc.magic.profiler.dist.model.report.ReportModel;
import ca.ubc.magic.profiler.dist.transform.IModuleCoarsener;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory.ModuleCoarsenerType;
import ca.ubc.magic.profiler.parser.JipParser;
import ca.ubc.magic.profiler.parser.JipRun;
import ca.ubc.magic.profiler.simulator.control.ISimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.StaticTimeSimulator;
import ca.ubc.magic.profiler.simulator.control.TimeSimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

import plugin.LogUtilities;
import plugin.mvc.DefaultPropertyDelegate;
import plugin.mvc.ITranslator.IModel;

public class 
TestFrameworkModel 
implements IModel
{
	private DefaultPropertyDelegate 						property_change_delegate;
	private Map<String, DefaultKeyValue<Integer, String>> 	unitMap;
	
	private volatile ModuleModel 			mModuleModel;
	private SimulationFramework	 			mSimFramework; 
	private volatile HostModel   			mHostModel; 
	
	private ISimulator 						mSim; 
	private SimulationUnit 					mBestSimUnit;
	private volatile String					profiler_trace; 
	private JipRun 							jipRun;
	private IModuleCoarsener 				module_coarsener;

	private int latest_simulation_index;
	
	public 
	TestFrameworkModel
	( 	ModuleModel module_model, 
		SimulationFramework simulation_framework,
		HostModel host_model,
		String profiler_trace,
		ModuleCoarsenerType module_type,
		EntityConstraintModel constraint_model )
	{
		this.property_change_delegate
			= new DefaultPropertyDelegate();
		this.unitMap 
			= new HashMap<String, DefaultKeyValue<Integer, String>>();
		
		if(module_model == null){
			throw new NullPointerException();
		}
		if(simulation_framework == null){
			throw new NullPointerException();
		}
		if(host_model == null){
			throw new NullPointerException();
		}
		if( profiler_trace == null || profiler_trace.equals("")){
			throw new IllegalArgumentException();
		}
		if(module_type == null){
			throw new NullPointerException();
		}
		
		// the constraint model is allowed to be null
		
		this.mModuleModel
			= module_model;
		this.mSimFramework
			= simulation_framework;
		this.mHostModel
			= host_model;
		this.mBestSimUnit
			= null;
		this.jipRun
			= null;
		this.module_coarsener
			= ModuleCoarsenerFactory.getModuleCoarsener(
				module_type, constraint_model
			);
		this.profiler_trace 
			= profiler_trace;
		
		this.registerProperties();
		
		// also make a call to the combo box function to properly
		// initialize now that the right fields are set
		// ( profiler trace, mSim, mModuleModel )
		this.setSimulationType( SimulatorType.TIME_SIMULATOR.getText() );
	}
	
	////////////////////////////////////////////////////////////////////////
	///	Property Change Event Handlers
	////////////////////////////////////////////////////////////////////////
	
	synchronized public void
	setSimulationAdded
	( SimulationUnit unit )
	{
		System.out.println("Inserting " + unit.getKey());
		this.unitMap.put(
			unit.getKey(), 
			new DefaultKeyValue<Integer, String>(
				Integer.valueOf(this.latest_simulation_index), 
				unit.getSignature()
			)
		);
		System.out.println("Extracting " + unit.getKey());
	
		this.property_change_delegate.firePropertyChange(
			TestFrameworkMessages.SIMULATION_ADDED, null, unit
		);
	
		this.incrementID();
	}
	
	synchronized public void
	setSimulationRemoved
	( SimulationUnit unit )
	{
		unitMap.remove(unit.getKey());
	}

	private void 
	incrementID() 
	{
		this.latest_simulation_index++;
		System.err.println("Updating the id");
		this.property_change_delegate.notifyViews(
			TestFrameworkMessages.UPDATE_ID, 
			Integer.valueOf(this.latest_simulation_index)
		);
	}
	
	synchronized public void
	setUpdateSimulationReport
	( Object[] args )
	{
		System.err.println("Inside setUpdateSimulationReport with array");
		for( String key : this.unitMap.keySet()){
			System.err.println(key);
		}
		SimulationUnit unit
			= (SimulationUnit) args[0];
		ReportModel report
			= (ReportModel) args[1];
		
		System.out.println( "Extracting " + unit.getKey() );
		String key
			= unit.getKey();
		System.out.println(
			"Size " + this.unitMap.size() 
		);
		DefaultKeyValue<Integer, String> key_value
			= this.unitMap.get( key );
		
		assert key_value != null 
			: "The key value in the updateSimulationReport function should not be null";
		Integer num = (Integer) key_value.getKey();
		int id = num;
		
		this.property_change_delegate.notifyViews(
			TestFrameworkMessages.SIMULATION_TABLE_RUN_UPDATE,
			new Object[] { 
				id,
				report.getCostModel().getExecutionCost(),
				report.getCostModel().getCommunicationCost(),
				report.getCostModel().getTotalCost()
			}
		);
	} 

	synchronized public void
	setUpdateBestSimulationReport
	( SimulationUnit unit )
	{
		System.err.println("Inside setBestSimulationReport ");
		if( unit != this.mBestSimUnit ){
		
			System.err.println("Firing events in updateBestSimulationReport ");
			this.mBestSimUnit = unit;
			
			this.property_change_delegate.notifyViews(
				TestFrameworkMessages.UPDATE_BEST_RUN_NAME,
				((Integer) (this.unitMap.get(unit.getKey()).getKey())) + ": " + unit.getName()
			);
			
			this.property_change_delegate.notifyViews(
				TestFrameworkMessages.UPDATE_BEST_RUN_ALGORITHM,
				unit.getAlgorithmName()
			);
			
			this.property_change_delegate.notifyViews(
				TestFrameworkMessages.UPDATE_BEST_RUN_COST,
				Double.toString( unit.getUnitCost() )
			);
		}
	}
	
	synchronized public void
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
	                            	// taking out the fileChoose...ask Nima about this
	                            	null,
	                                "Reading " +  this.profiler_trace,
	                                new FileInputStream( this.profiler_trace )
	                            )
	                        );   

	                    this.jipRun = JipParser.parse(in);       
	                    ((TimeSimulator) this.mSim).init(this.jipRun, this.module_coarsener);
	                }catch(Exception ex){
	                    ex.printStackTrace();
	                    LogUtilities.logError(ex);
	                }
	                break;
	            case STATIC_TIME_SIMULATOR:
	                ((StaticTimeSimulator) this.mSim).init( this.mModuleModel );
	                break;
	            default:
	                throw new RuntimeException("A simulator needs to be selected.");
	        }
	}

	synchronized public void
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
					TestFrameworkModel.this
						.mSimFramework.run(
							TestFrameworkModel.this.mSim
						);
				}
			});
		}catch( Exception ex ){
			ex.printStackTrace();
			LogUtilities.logError(ex);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	///	IModel Functions
	//////////////////////////////////////////////////////////////////////////
	
	@Override
	synchronized public void 
	addPropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
	}
	
	@Override
	synchronized public void 
	removePropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}
	
	@Override
	synchronized public Map<String, Object> 
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}
	
	private void 
	registerProperties() 
	{
		String[] property_names
			= {
				TestFrameworkMessages.TEST_SIMULATION_FRAMEWORK.NAME,
				TestFrameworkMessages.TEST_MODULE_MODEL.NAME,
				TestFrameworkMessages.TEST_HOST_MODEL.NAME,
				TestFrameworkMessages.UNIT_MAP.NAME,
			};
		Object[] properties
			= {
				this.mSimFramework,
				this.mModuleModel,
				this.mHostModel,
				this.unitMap
			};
		
		this.property_change_delegate.registerProperties(
			property_names, 
			properties
		);
	}
}
