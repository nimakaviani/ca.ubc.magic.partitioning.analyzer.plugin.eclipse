package ca.ubc.magic.profiler.partitioning.control.filters;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleHost;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.granularity.CodeEntity;
import ca.ubc.magic.profiler.dist.model.granularity.ColocationFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IColocationFilter;

public class ConstrainedColocationFilter implements IColocationFilter {
	
	 public static final String COMMA_CHAR = ",";
    public static final String MODULE_PAIR_BOUNDARY_CHAR = "{";
    public static final String MODULE_PAIR_BOUNDARY_CHAR_2 = "}";
    public static final String MODULE_PAIR_DELIMITER = ":";
    public static final String SPACE_CHAR = " ";

	private String mName = null;
	Set<ModulePair> mModulePairSet = null;
	
	public ConstrainedColocationFilter (
	    		ModuleModel moduleModel, 
	    		ColocationFilterConstraint colocationConstraint){
	    	
	        mName = colocationConstraint.getName();
	        initFilter(moduleModel, colocationConstraint);
	    }
	
	@Override
	public boolean isFilterable(Object mh) {
		return (mModulePairSet == null || mModulePairSet.isEmpty()) ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public double filter(ModuleHost mh) {
		return Constants.INFINITE_WEIGHT;
	}
	
	public void setFilterName(String name){
        mName = name;
    }
    
    public String getFilterName(){
        return mName;
    }
    
    @Override
    public Set<ModulePair> getFilterSet() {
        return mModulePairSet;
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
//		throw new UnsupportedOperationException();
	}
	
	private void initFilter(ModuleModel moduleModel, ColocationFilterConstraint colocationConstraint){
		
		if (moduleModel == null)
            throw new RuntimeException("Module model is not defined");
        if (colocationConstraint == null)
            throw new RuntimeException("Filter constraint is not available");
        
        mModulePairSet = new HashSet<ModulePair>();
        
        // The following is the pointer for keeping track of the last module matching
        // the filters in the filter set. This filter tries to create a connection 
        // chain for modules so that A<->B<->C<->D. This way location of B or A is
        // constrained with one another's location, B and C depend on one another, etc.
        // so for n modules, with n-1 constraints you can get all the modules to be
        // placed together.
        Module headModule = null;
        
        for (Module m : moduleModel.getModuleMap().values()){
        	for (CodeEntity entity : colocationConstraint.getEntities()){
        		if (!entity.getEntityPattern().matches(m.getName(), null, null))
        			continue;
	        	if (headModule == null){
	        		headModule = m;
	        	}else {
	        		mModulePairSet.add(new ModulePair(headModule, m));
	        		headModule = m;
	        	}
	        	break;
        	}
        }
	}
}
