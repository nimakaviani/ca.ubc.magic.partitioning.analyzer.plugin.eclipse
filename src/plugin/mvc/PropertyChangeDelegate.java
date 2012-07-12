package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class 
PropertyChangeDelegate 
// any model which wishes to make use of the getAll() functionality must
// register the available properties with the delegate
{
	protected transient PropertyChangeSupport listeners 
		= new PropertyChangeSupport(this);
	
	Map<String, Object> property_map
		= new HashMap<String, Object>();
	
	// david experiment
	Map<String, ADynamicProperty> dynamic_property_map
		= new HashMap<String, ADynamicProperty>();

	public void
	registerProperties
	( String[] property_names, Object[] properties )
	{
		assert property_names.length == properties.length 
				: "The property names list must match the properties list in length";
		
		for( int i = 0; i < property_names.length; ++i ){
			if(!this.property_map.containsKey(property_names[i])){
				this.property_map.put(property_names[i], properties[i]);
			}
		}
	}
	
	// david experiment
	public void
	registerDynamicProperty
	( String property_name, ADynamicProperty dynamic_property )
	{
		if(!this.dynamic_property_map.containsKey(property_name)){
			this.dynamic_property_map.put(
				property_name,
				dynamic_property
			);
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
    ( String property_name, Object old_value, Object new_value )
    {
    	// to deal with reference switches
    	this.property_map.put(property_name, new_value);
    	
        if (this.listeners.hasListeners(property_name)) {
        	// we have a problem: the following should fire but
        	// nothing happens
            this.listeners.firePropertyChange(
            	property_name, 
            	old_value, 
            	new_value
            );
        }
    }
    
    public void
    firePropertyChange
    ( String property_name, Object new_value )
    {
    	if( this.listeners.hasListeners(property_name) ) {
    		if(this.property_map.containsKey(property_name)){
    			Object old_value
    				= this.property_map.get(property_name);
    			this.listeners.firePropertyChange(
    				property_name, 
    				old_value, 
    				new_value
    			);
    		}
        }
    }

	public Map<String, Object> 
	getAll
	( String[] property_names ) 
	{
		Map<String, Object> return_values 
			= new HashMap<String, Object>( property_names.length );
	
		for( String property_name : property_names ){
			if( this.dynamic_property_map.containsKey( property_name )){
				ADynamicProperty dynamic_property
					= this.dynamic_property_map.get( property_name );
				Object property
					= dynamic_property.getProperty();
				return_values.put(property_name, property);
			}
			else {
				return_values.put(
					property_name,
					this.property_map.get( property_name ) 
				);
			}
		}
		
		return return_values;
	}
}
