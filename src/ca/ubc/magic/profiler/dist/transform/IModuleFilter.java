/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.dist.transform;

import ca.ubc.magic.profiler.dist.model.ModuleHost;

/**
 * initFilter for each filter is the important part about module filters. 
 * The main thing with initFilter is that it changes the state of modules
 * from ModuleModel for their host information to be updated to the host
 * where they should be placed on according to the filter.
 *
 * @author nima
 */
public interface IModuleFilter extends IFilter {
    
    public double  filter (ModuleHost mh);
    
}
