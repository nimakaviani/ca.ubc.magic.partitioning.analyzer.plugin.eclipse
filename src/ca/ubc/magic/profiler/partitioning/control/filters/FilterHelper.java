/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.filters;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.ColocationFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel.FilterType;
import ca.ubc.magic.profiler.dist.model.granularity.HostFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.InteractionFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IFilter;
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
    
    public static Map<String, IFilter> setFilter(
    		ModuleModel moduleModel, 
    		HostModel hostModel,
    		FilterConstraintModel filterModel,
    		FilterType type){
    	
    	Map<String, IFilter> filterMap = new HashMap<String, IFilter>();
    	
    	for (FilterConstraint constraint : filterModel.getFilterSet(type)){
    		IFilter filter = FilterFactory.getFilter(type, moduleModel,	hostModel, constraint);
    		if (filter.getFilterSet() == null || filter.getFilterSet().isEmpty())
    			continue;
    		if (filterMap.containsKey(constraint.getName()))
    			System.err.println("WARN: a filter with the given name already " +
    					"exists in the map for filter name: " + constraint.getName());
    		filterMap.put(constraint.getName(), filter);
    	}
    	
    	return filterMap;
    }
    
    public static IModuleFilter setModuleFilterThread(ModuleModel moduleModel, HostModel hostModel){
        String[] moduleNames = {
//                // The following filters are for filtering Rubis
//                "com.notehive.osgi.rubis.hibernate-osgi-rubis-user-session",
//                "com.notehive.osgi.rubis.hibernate-osgi-rubis-buy-session",                
//                "com.notehive.osgi.rubis.hibernate-osgi-rubis-bid-session",
//                
//                // The following are for filtering ariestrader deplyed on osgi and jetty
//                "org.apache.aries.samples.ariestrader.beans:AccountProfileDataBeanImpl",
//                "org.apache.aries.samples.ariestrader.beans:QuoteDataBeanImpl",
//                "org.apache.aries.samples.ariestrader.beans:HoldingDataBeanImpl",
                
                // The following are for filtering ariestrader deployed on tomcat
//                "DBBundle:MySQL_holdingejb",
//                "DBBundle:MySQL_quoteejb",
//                "DBBundle:MySQL_orderejb",
//                "DBBundle:MySQL_accountejb",
//                "DBBundle:MySQL_accountprofileejb",
//                "DBBundle:MySQL_"
        		
        		// The following are for filtering jforum deployed on tomcat
        		"DBBundle:MySQL_jforum_forums", 
        		"DBBundle:MySQL_jforum_groups", 
        		"DBBundle:MySQL_jforum_posts",
        		"DBBundle:MySQL_jforum_users", 
        		"DBBundle:MySQL_jforum_topics",
        		"DBBundle:MySQL_jforum_posts_text", 
        		"DBBundle:MySQL_jforum_roles", 
        		"DBBundle:MySQL_jforum_user_groups",
        		"DBBundle:MySQL_jforum_words"
        		
        };
        return new InfeasibleHostFilterRegEx(moduleModel, hostModel, 
                FilterHelper.INFEASIBLE_HOST_THREAD, moduleNames, H2_INFEASIBLE_HOST);
    }
    
    public static IModuleFilter setSyntheticNodeModuleFitler(ModuleModel moduleModel, HostModel hostModel){
        
        String[] moduleNames = {
            Constants.SYNTHETIC_NODE
        };
        
        return new InfeasibleHostFilterRegEx(moduleModel, hostModel, 
                FilterHelper.INFEASIBLE_HOST_THREAD, moduleNames, H1_INFEASIBLE_HOST);
    }
    
    
    public static IInteractionFilter setInterctionFilterThread(ModuleModel moduleModel){
        String[][] modulePairNames = {
            {"org.apache.aries.samples.ariestrader.beans",
            "org.apache.aries.samples.ariestrader.persist.jdbc"}
        };
        
        return new InfeasibleEdgeCutFilterRegEx(moduleModel, INFEASIBLE_SPLIT_THREAD, modulePairNames);
    }
}
