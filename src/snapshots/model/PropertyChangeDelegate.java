package snapshots.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class 
PropertyChangeDelegate 
{
	protected transient PropertyChangeSupport listeners 
		= new PropertyChangeSupport(this);
	
	Map<String, Object> property_map
		= new HashMap<String, Object>();

	public void
	registerProperty
	( String property_name, Object reference )
	{
		if(!this.property_map.containsKey(property_name)){
			this.property_map.put(property_name, reference);
		}
	}
	
    public void 
    addPropertyChangeListener
    ( PropertyChangeListener l )
    {
        if (l == null) {
            throw new IllegalArgumentException();
        }
        this.listeners.addPropertyChangeListener(l);
    }
    
    
    public void 
    removePropertyChangeListener
    (PropertyChangeListener l)
    {
        this.listeners.removePropertyChangeListener(l);
    }
  
    public void 
    firePropertyChange
    ( String prop, Object old, Object newValue )
    {
    	// should always be 1
    	System.err.println( this.listeners.getPropertyChangeListeners().length );
    	
    	// to deal with reference switches
    	this.property_map.put(prop, newValue);
    	
        if (this.listeners.hasListeners(prop)) {
            this.listeners.firePropertyChange(prop, old, newValue);
        }
    }
    
    public void
    firePropertyChange
    ( String property_name, Object old_value )
    {
    	if( this.listeners.hasListeners(property_name) ) {
    		if(this.property_map.containsKey(property_name)){
    			Object new_value
    				= this.property_map.get(property_name);
    			this.listeners.firePropertyChange(
    				property_name, 
    				old_value, 
    				new_value
    			);
    		}
        }
    }

	public Object[] 
	getAll
	( String[] property_names ) 
	{
		List<Object> return_values 
		= new ArrayList<Object>(property_names.length);
	
		for( String property_name : property_names ){
			if(this.property_map.get(property_name) == null){
			//	System.out.println("Null " + property_name);
			}
			//System.out.println( property_name + " " + this.property_map.get(property_name).toString() );
			return_values.add( 
				this.property_map.get(property_name) 
			);
		}
		
		return return_values.toArray(new Object[0]);
	}

}
