package ca.ubc.magic.profiler.partitioning.control.filters;

import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.InteractionFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.HostFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.ColocationFilterConstraint;
import ca.ubc.magic.profiler.dist.model.granularity.FilterConstraintModel.FilterType;
import ca.ubc.magic.profiler.dist.transform.IFilter;

public class FilterFactory {
	
	public static IFilter getFilter(FilterType type, 
			ModuleModel moduleModel, 
    		HostModel hostModel, 
			FilterConstraint filter){
		switch (type){
		case HOST_CUT:
			return new ConstrainedInfeasibleHostFilter(
					moduleModel, hostModel, (HostFilterConstraint) filter);
		case INTERACTION_CUT:
			return new ConstrainedInfeasibleEdgeCutFilter(moduleModel, (InteractionFilterConstraint) filter);
		case COLOCATION_CUT:
			return new ConstrainedColocationFilter(moduleModel, (ColocationFilterConstraint) filter);
		default:
				throw new RuntimeException("Filter type not found");
		}
	}

}
