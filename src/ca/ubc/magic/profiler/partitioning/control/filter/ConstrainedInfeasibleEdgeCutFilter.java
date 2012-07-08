/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.filter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.ModulePairHostPair;
import ca.ubc.magic.profiler.dist.model.granularity.InteractionFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IInteractionFilter;

/**
 *
 * @author nima
 */
public class ConstrainedInfeasibleEdgeCutFilter implements IInteractionFilter {
   
    public static final String COMMA_CHAR = ",";
    public static final String MODULE_PAIR_BOUNDARY_CHAR = "{";
    public static final String MODULE_PAIR_BOUNDARY_CHAR_2 = "}";
    public static final String MODULE_PAIR_DELIMITER = ":";
    public static final String SPACE_CHAR = " ";
   
    Set<ModulePair> mModulePairSet;
    
    String mName;
    
    public ConstrainedInfeasibleEdgeCutFilter (
    		ModuleModel moduleModel, 
    		InteractionFilterConstraint edgeFilter){
    	
        mName = edgeFilter.getName();
        initFilter(moduleModel, edgeFilter);
    }
    
    public boolean isFilterable(ModulePairHostPair mph) {
        for (ModulePair mp : mModulePairSet){
            if (mph.getModulePair().equals(mp)){
                return true;
            }
        }
        return false;
    }
    
    public double filter(ModulePairHostPair mph){
        return Constants.INFINITE_WEIGHT;
    }
    
    public Set getFilterSet() {
        return mModulePairSet;
    }
    
    public void setFilterName(String name){
        mName = name;
    }
    
    public String getFilterName(){
        return mName;
    }
    
    public String getFilterAsString() {
        StringBuilder strBldr = new StringBuilder();
        for (ModulePair mp : mModulePairSet){
            String[] strArry = new String[] {mp.getModules()[0].getName(), mp.getModules()[1].getName()};
            strBldr.append(MODULE_PAIR_BOUNDARY_CHAR).append(
                    StringUtils.arrayToDelimitedString(strArry, MODULE_PAIR_DELIMITER)).append(
                    MODULE_PAIR_BOUNDARY_CHAR_2).append(COMMA_CHAR);
        }
        return strBldr.toString().trim().replaceAll(COMMA_CHAR, COMMA_CHAR + SPACE_CHAR);
    }

    public void setStringToFilter(ModuleModel moduleModel, HostModel hostModel, String stringFilter) {
//    	throw new UnsupportedOperationException("Not applicable.");
    }
    
    protected void initFilter(ModuleModel moduleModel, InteractionFilterConstraint iFilter){
        if (moduleModel == null)
            throw new RuntimeException("Module model is not defined");
        if (iFilter == null)
            throw new RuntimeException("Pair names for the filter is not available");
        
        mModulePairSet = new HashSet<ModulePair>();
                
        for (ModulePair mp : moduleModel.getModuleExchangeMap().keySet())
            if ((iFilter.getEntities()[0].getEntityPattern().matches(mp.getModules()[0].getName(), null, null) &&
            	 iFilter.getEntities()[1].getEntityPattern().matches(mp.getModules()[1].getName(), null, null)) ||
           		(iFilter.getEntities()[0].getEntityPattern().matches(mp.getModules()[1].getName(), null, null) &&
                 iFilter.getEntities()[1].getEntityPattern().matches(mp.getModules()[0].getName(), null, null)))
                mModulePairSet.add(mp);
    }
}
