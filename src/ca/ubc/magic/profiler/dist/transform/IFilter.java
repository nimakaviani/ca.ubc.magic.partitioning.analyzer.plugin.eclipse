/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.dist.transform;

import java.util.Set;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;

/**
 *
 * @author nima
 */
public interface IFilter {
	/**
	 * 
	 * @return				A String representation of the set of modules the filter is applicable to
	 */
    public String 			getFilterAsString();
    
    /**
     * Receives the module model and the host model and a string formatted filter and converts
     * it to the filter representation.
     * 
     * @param moduleModel	The module model for the dependency graph
     * @param hostModel		The host model fro the dependency graph
     * @param stringFilter	The string representation of the filter
     */
    public void   			setStringToFilter(ModuleModel moduleModel, HostModel hostModel, String stringFilter);
    
    /**
     * Checks whether or not a given module object is filterable with the given filter.
     * @param moduleObj		The module object passed for checking
     * @return				TRUE if the module is filterable and FALSE otherwise.
     */
    public boolean 		isFilterable(Object moduleObj);
    
    /**
     * The set of modules from the module model that are selected and have received filtering by the filter.
     * 
     * @return		The list of modules the filter is applicable to.
     */
    public Set<ModulePair> getFilterSet();
}
