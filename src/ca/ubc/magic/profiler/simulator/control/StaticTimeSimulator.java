/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.simulator.control;

import ca.ubc.magic.profiler.dist.model.DistributionModel;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.report.ReportModel;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author nima
 */
public class StaticTimeSimulator extends AbstractSimulator implements Runnable {
    
    SimulationUnit mUnit = null;
    ModuleModel mModuleModel;
    
     public StaticTimeSimulator(){        
        listeners = new HashSet<ISimulatorListener>();        
    }

    public void init(ModuleModel moduleModel) {
        mModuleModel = moduleModel;
    }

    public void simulate(SimulationUnit unit) throws RuntimeException {
        mUnit = unit;        
    }

    public void 
    run() 
    throws RuntimeException 
    {
    	System.err.println("Inside static time simulator");
    	
        if (mUnit == null)
            throw new RuntimeException("No Simulation unit is provided for simulation purposes");
        
        ReportModel localReport = new ReportModel(mUnit.getDistModel().getHostModel().getHostMap().values());
        
        for (String moduleName : mUnit.getDistModel().getModuleMap().keySet()){
        	Map<String, Module> module_map
        		=  mModuleModel.getModuleMap();
        	
        	Module module 
          		= module_map.get(moduleName);
          
          	DistributionModel distribution_model
          		= mUnit.getDistModel();
          	Map<String, Module> module_map_two
          		= distribution_model.getModuleMap();
          	Module module_two
          		= module_map_two.get(moduleName);
          	
          	module.setPartitionId(
        		  module_two.getPartitionId()
        	);
        }
        localReport = CostAnalyzerHelper.analyzeCosts(mUnit.getName(), mModuleModel,mUnit.getDistModel().getHostModel());
        
         for (ISimulatorListener l : listeners){
            l.valueChanged(localReport);    
            l.unitSimulationOver(localReport);
         }
    }
}
