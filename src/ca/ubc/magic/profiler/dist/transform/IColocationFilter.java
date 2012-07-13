package ca.ubc.magic.profiler.dist.transform;

import java.util.Set;

import ca.ubc.magic.profiler.dist.model.ModuleHost;
import ca.ubc.magic.profiler.dist.model.ModulePair;


/**
 * The difference between this filter and IModuleFilter is that this filter
 * receives a list of two or more filters for the components in the graph
 * and generates filters for the partitioner to ensure they will be co-located
 * on the same host. The behaviour of this filter is thus closer to the IInteractionFilter
 * as it cares about module co-location. However, it deals with individual modules
 * when building the list of modules to be filtered. That is why it has to check
 * filterability of modules only by looking at individual modules.
 *   
 * @author nima
 *
 */
public interface IColocationFilter extends IFilter {
	
	public double  filter (ModuleHost mh);
	
	public Set<ModulePair> getFilterSet();
}
