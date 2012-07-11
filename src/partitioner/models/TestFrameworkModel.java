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
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory.PartitionerType;
import ca.ubc.magic.profiler.simulator.control.ISimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.StaticTimeSimulator;
import ca.ubc.magic.profiler.simulator.control.TimeSimulator;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationFrameworkHelper;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

import plugin.Constants;
import plugin.mvc.IModel;
import plugin.mvc.PropertyChangeDelegate;

public class 
TestFrameworkModel 
implements IModel
{
	private PropertyChangeDelegate 			property_change_delegate;
	private Map<String, DefaultKeyValue> 	unitMap;
	private int id;
	
	private PartitionerType 				partitioner_type;
	private volatile ModuleModel 			mModuleModel;
	private SimulationFramework	 			mSimFramework; 
	private volatile HostModel   			mHostModel; 
	
	private ISimulator mSim 
		= SimulatorFactory.getSimulator(
			SimulatorType.fromString("Static Time Simulator (No Trace Replay)")
	  	);
	private SimulationUnit mBestSimUnit;
	private volatile String			profiler_trace; 
	private JipRun jipRun;
	private IModuleCoarsener module_coarsener;

	public 
	TestFrameworkModel
	( 	PartitionerType partitioner_type, 
		ModuleModel module_model, 
		SimulationFramework simulation_framework,
		HostModel host_model,
		String profiler_trace,
		ModuleCoarsenerType module_type,
		EntityConstraintModel constraint_model )
	{
		this.property_change_delegate
			= new PropertyChangeDelegate();
		this.unitMap 
			= new HashMap<String, DefaultKeyValue>();
		
		if(partitioner_type == null){
			throw new NullPointerException();
		}
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
		
		this.partitioner_type
			= partitioner_type;
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
		
		this.registerProperties();
	}
	
	private void 
	registerProperties() 
	{
		String[] property_names
			= {
				Constants.SIMULATION_FRAMEWORK,
				Constants.MODULE_MODEL,
				Constants.HOST_MODEL,
			};
		Object[] properties
			= {
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

	@Override
	public void 
	addPropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
	}

	@Override
	public void 
	removePropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}

	@Override
	public Object[] 
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}

	///////////////////////////////////////////////////////////
	///	TODO: Test framework classes; to be repositioned later
	///////////////////////////////////////////////////////////
	
	public void
	setSimulationAdded
	( SimulationUnit unit )
	{
		System.out.println("Inserting " + unit.getKey());
		this.unitMap.put(
			unit.getKey(), 
			new DefaultKeyValue(
				new Integer(this.id), 
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
					TestFrameworkModel.this
						.mSimFramework.run(
							TestFrameworkModel.this.mSim
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
		DefaultKeyValue key_value
			= this.unitMap.get( key );
		
		assert key_value != null 
			: "The key value in the updateSimulationReport function should not be null";
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
				unit 
					= SimulationFrameworkHelper.getUnitFromSig(
						( String ) keyVal.getValue(), 
						mSimFramework.getTemplate(), 
						Boolean.TRUE
					);
			}
		}

		return unit;
	}
	
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
	                            	// TODO taking out the fileChoose...ask Nima about this
	                            	null,
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
}
