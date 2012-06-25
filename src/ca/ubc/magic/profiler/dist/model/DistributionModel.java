 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.dist.model;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.magic.profiler.dist.control.Util;

/**
 *
 * @author nima
 */
public class DistributionModel {
    
    Map<String, Module> mModuleMap;
    HostModel    mHostModel;
    
    int mNumberOfPartitions = 0;    
    
    public DistributionModel(){
        mModuleMap = new HashMap<String, Module>();    
        mHostModel = null;
    }
    
    public DistributionModel(ModuleModel moduleModel, HostModel hostModel){
        this(moduleModel.getModuleMap(), hostModel);
    }
    
    public DistributionModel(Map moduleMap, HostModel hostModel){
        mModuleMap = Util.deepClone(moduleMap);
        mHostModel = hostModel;
        mNumberOfPartitions = mHostModel.getNumberOfHosts();
    }
    
    public Map<String, Module> getModuleMap(){
        return mModuleMap;
    }          
    
    public HostModel getHostModel(){
        if (mHostModel == null)
            throw new RuntimeException("Host model is not initialized");
        return mHostModel;
    }
    
    public void setHostModel(HostModel hostModel){
        if (mHostModel.getNumberOfHosts() != mNumberOfPartitions)
            throw new RuntimeException("Mismatch between number of hosts "
                    + "and number of partitions");
        mHostModel = hostModel;
    }
        
    public void addModule(Module module){
        mModuleMap.put(module.getName(), module);
    }
    
    public void updateModulePartition(String partitionName, int partitionId){
        Module m = mModuleMap.get(partitionName);
        m.setPartitionId(partitionId);   
    }       
    
    public int getNumberOfPartitions(){
        return mNumberOfPartitions;
    }
    
    public void setNumberOfPartitions(int numberOfPartitions){
        mNumberOfPartitions = numberOfPartitions;
    }   
}
