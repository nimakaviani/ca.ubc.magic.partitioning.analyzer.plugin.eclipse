package ca.ubc.magic.profiler.dist.model.granularity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilterConstraintModel {
	
	private Map<String, Set<FilterConstraint>> mFilterConstraintMap;
	
	public FilterConstraintModel(){
		mFilterConstraintMap = new HashMap<String, Set<FilterConstraint>>();
		for (FilterType type : FilterType.values()){
			mFilterConstraintMap.put(type.getText(), new HashSet<FilterConstraint>());
		}
	}
	
	public Set<FilterConstraint> getFilterSet(FilterType type){
		Set<FilterConstraint> s = mFilterConstraintMap.get(type.getText());
		if (s != null)
			return s;
		throw new RuntimeException("No proper list for the given filter type is found");
	}
	
	public FilterConstraint newInstanceForType(FilterType type){
		switch (type){
		case HOST_CUT:
			return new HostFilterConstraint();
		case INTERACTION_CUT:
			return new InteractionFilterConstraint();
		default:
			throw new RuntimeException("Unknown filter type for instantiation");
		}
	}
	
	public enum FilterType{
		HOST_CUT("HOST_CUT"),
        INTERACTION_CUT ("INTERACTION_CUT");
        
         private String text;

         FilterType(String text) {
            this.text = text;
         }

         public String getText() {
            return this.text;
         }

          public static FilterType fromString(String text) {
            if (text != null) {
                for (FilterType b : FilterType.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                      return b;
                    }
                }
            }
            return null;
         }
    }   
}
