package ca.ubc.magic.profiler.partitioning.control.filters;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.ModulePairHostPair;
import ca.ubc.magic.profiler.dist.transform.IInteractionFilter;

public class DistributedQueryDbFilter implements IInteractionFilter {

	private String mName = null;
	private Set<ModulePair> mModulePairSet = null;
	private static final String DB_BUNDLE_NAME = "DBBundle";
	
	public static final String COMMA_CHAR = ",";
    public static final String MODULE_PAIR_BOUNDARY_CHAR = "{";
    public static final String MODULE_PAIR_BOUNDARY_CHAR_2 = "}";
    public static final String MODULE_PAIR_DELIMITER = ":";
    public static final String SPACE_CHAR = " ";
	
	public DistributedQueryDbFilter (
    		ModuleModel moduleModel,
    		String name){
    	
        mName = name;
        initFilter(moduleModel);
    }
	
	@Override
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

	@Override
	public void setStringToFilter(ModuleModel moduleModel, HostModel hostModel,
			String stringFilter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFilterable(Object mph) {
		if (!(mph instanceof ModulePairHostPair))
			return Boolean.FALSE;
		
		ModulePair mp = ((ModulePairHostPair) mph).getModulePair();
		return isFilterableInternal(mp);
		
	}

	private boolean isFilterableInternal(ModulePair mp) {
		return (mp.getModules()[0].getName().startsWith(DistributedQueryDbFilter.DB_BUNDLE_NAME) &&
				mp.getModules()[1].getName().startsWith(DistributedQueryDbFilter.DB_BUNDLE_NAME));
	}

	@Override
	public Set<ModulePair> getFilterSet() {
		return mModulePairSet;
	}

	@Override
	public double filter(ModulePairHostPair mph) {
		return Constants.DB_EDGE_WEIGHT_INCREASE_FACTOR;
	}
	
	private void initFilter(ModuleModel moduleModel){
		
		if (moduleModel == null)
            throw new RuntimeException("Module model is not defined");
        
        mModulePairSet = new HashSet<ModulePair>();
        
        for (ModulePair mp : moduleModel.getModuleExchangeMap().keySet())
        	if (isFilterableInternal(mp))
        		mModulePairSet.add(mp);
	}

}
