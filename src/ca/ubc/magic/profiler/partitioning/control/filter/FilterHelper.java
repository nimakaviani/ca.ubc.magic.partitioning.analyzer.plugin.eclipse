/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.filter;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel.FilterType;
import ca.ubc.magic.profiler.dist.model.granularity.HostFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.InteractionFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IInteractionFilter;
import ca.ubc.magic.profiler.dist.transform.IModuleFilter;

/**
 *
 * @author nima
 */
public class FilterHelper {
    public static final int H2_INFEASIBLE_HOST = 2;
    public static final int H1_INFEASIBLE_HOST = 1;
    
    public static final String INFEASIBLE_HOST  = "Infeasible Host Filter";
    public static final String INFEASIBLE_HOST_THREAD  = "Infeasible Host Filter Threaded";
    public static final String BIND_SYNTHETIC_NODE  = "Bind Synthetic Node";
    public static final String INFEASIBLE_SPLIT = "Infeasible Split Filter";
    public static final String INFEASIBLE_SPLIT_THREAD = "Infeasible Split Filter Threaded";
    public static final String INFEASIBLE_SYNTHETIC = "Infeasible Synthetic Node";
    
    public static Map<String, IModuleFilter> setModuleFilter(
    		ModuleModel moduleModel, 
    		HostModel hostModel,
    		FilterConstraintModel filterModel){
    	
    	Map<String, IModuleFilter> hostFilterMap = new HashMap<String, IModuleFilter>();
    	
    	for (FilterConstraint hostFilter : filterModel.getFilterSet(FilterType.HOST_CUT))
    		hostFilterMap.put(hostFilter.getName(), new ConstrainedInfeasibleHostFilter(
        		moduleModel, 
        		hostModel, 
                (HostFilterConstraint) hostFilter));
    	
    	return hostFilterMap;
    }
    
    public static  Map<String, IInteractionFilter> setInteractionFilter(
    		ModuleModel moduleModel, 
    		FilterConstraintModel filterModel){
    	
		Map<String, IInteractionFilter> interactionFilterMap = new HashMap<String, IInteractionFilter>();
		    	
		    	for (FilterConstraint edgeFilter : filterModel.getFilterSet(FilterType.INTERACTION_CUT))
		    		interactionFilterMap.put(edgeFilter.getName(), 
		    				new ConstrainedInfeasibleEdgeCutFilter(
		    						moduleModel,
		    						(InteractionFilterConstraint) edgeFilter));
		    	
		    	return interactionFilterMap;
    }
}
