/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.filter;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.util.StringUtils;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.Host;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleHost;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.HostFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IModuleFilter;

/**
 *
 * @author nima
 */
public class ConstrainedInfeasibleHostFilter implements IModuleFilter {
    public static final String HOST_DELIMITER = "::";
        
    protected Set<Module> mModuleSet;
    protected Host        mHost;
    protected String mName;

    public ConstrainedInfeasibleHostFilter(
    		ModuleModel moduleModel, 
    		HostModel hostModel,
    		HostFilterConstraint hostFilter){     
        
        // initializing the filter
        initFilter(moduleModel, hostModel, hostFilter);
    }
    
    public boolean isFilterable(ModuleHost mh) {
        
        // finds the corresponding modules from the modulemodel and updates
        // their edge weights to infinite
        if (mh.getHost().equals(mHost)){
            Module m = mh.getModule();
            for (Module tmpM : mModuleSet){
                if (m.equals(tmpM)){
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
    
    @Override
    public double filter (ModuleHost mh){
        return Constants.INFINITE_WEIGHT;
    }
    
    public Set getFilterSet(){
        return mModuleSet;
    }
    
    public String getFilterAsString() {
        String[] moduleNames = new String[mModuleSet.size()];
        int index = 0;
        for (Module m : mModuleSet){
            moduleNames[index++] = m.getName();
        }
        return (StringUtils.arrayToCommaDelimitedString(moduleNames) + 
                HOST_DELIMITER + mHost.getId()).replaceAll(",", ", ");
    }

    public void setStringToFilter(ModuleModel moduleModel, HostModel hostModel, String stringFilter) {
//    	throw new UnsupportedOperationException("Not applicable.");
    }
    
    protected void initFilter(ModuleModel moduleModel, HostModel hostModel, HostFilterConstraint hostFilter){        
        if (hostModel != null && moduleModel != null){
           mModuleSet = new HashSet<Module>(); 
           for (Entry<String, Module> moduleEntry : moduleModel.getModuleMap().entrySet()){
               if (hostFilter.getEntity().getEntityPattern().matches(moduleEntry.getKey(), null, null))
                    mModuleSet.add(new Module(moduleEntry.getValue()));
           }
           mHost = hostModel.getHostMap().get((int) hostFilter.getHostId());
           
           // when applying the filter, first sets the partition Id for all
           // the modules stored in the filter to the partition Id of the host
           // these partitions need to reside on.
           for (Module m : mModuleSet){
               if (m == null)
                   throw new RuntimeException("ModuleSet for the filter contains"
                           + "a null element");
               m.setPartitionId(mHost.getId());
           }
        }else
            throw new RuntimeException("No proper setting of the module model or the host models");
    }
}
