package ca.ubc.magic.profiler.partitioning.control.filters;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.InteractionFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.HostFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.ColocationFilterConstraint;
import ca.ubc.magic.profiler.dist.transform.IFilter;

public class FilterFactory {
	
	public static IFilter getFilter( 
			ModuleModel moduleModel, 
    		HostModel hostModel, 
			FilterConstraint filter){
		
		if (filter instanceof HostFilterConstraint)
			return new ConstrainedInfeasibleHostFilter(
					moduleModel, hostModel, (HostFilterConstraint) filter);
		else if (filter instanceof InteractionFilterConstraint)
			return new ConstrainedInfeasibleEdgeCutFilter(moduleModel, (InteractionFilterConstraint) filter);
		else if (filter instanceof ColocationFilterConstraint)
			return new ConstrainedColocationFilter(moduleModel, (ColocationFilterConstraint) filter);
		else
			throw new RuntimeException("Filter type not found");
	}

}
