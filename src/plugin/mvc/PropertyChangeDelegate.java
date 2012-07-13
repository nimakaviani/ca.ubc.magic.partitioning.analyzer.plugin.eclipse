package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
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
	
	Map<String, ADynamicProperty> dynamic_property_map
		= new HashMap<String, ADynamicProperty>();

	public void
	registerProperties
	( String[] property_names, Object[] properties )
	{
		if( property_names.length != properties.length){
			throw new IllegalArgumentException(
				"The property names list must match the "
				+ "properties list in length"	
			);
		}
		
		for( int i = 0; i < property_names.length; ++i ){
			if(!this.property_map.containsKey(property_names[i])){
				this.property_map.put(property_names[i], properties[i]);
			}
			else {
				StringBuilder sb
					= new StringBuilder();
				for( String s : this.property_map.keySet() ) {
					sb.append(s + " ");
				}
				throw new IllegalArgumentException(
					"You have tried to register the same static property ("
					+ property_names[i] + ", " + i + ") twice. This is probably a "
					+ "bug in your code.\n"
					+ sb.toString()
				);
			}
		}
	}
	
	// the following is used to register properties that must be
	// created upon the first call;
	public void
	registerDynamicProperties
	( 	String[] dynamic_property_names, 
		ADynamicProperty[] dynamic_property )
	{
		for( int i = 0; i < dynamic_property_names.length; ++i ){
			if( !this.dynamic_property_map.containsKey(dynamic_property_names) ){
				this.dynamic_property_map.put(
					dynamic_property_names[i],
					dynamic_property[i]
				);
			}
			else {
				throw new IllegalArgumentException(
					"You have tried to register the same dynamic property ("
					+ dynamic_property_names + ") twice. This is probably a "
					+ "bug in your code."
				);
			}
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
    	if( l == null){
    		throw new IllegalArgumentException();
    	}
        this.listeners.removePropertyChangeListener(l);
    }
  
    public void 
    firePropertyChange
    ( String property_name, Object old_value, Object new_value )
    {
    	// to deal with reference switches
    	this.property_map.put(property_name, new_value);
    	
        if (this.listeners.hasListeners(property_name)) {
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
    		else {
    			throw new IllegalArgumentException(
    				"You assumed that the property "
    				+ property_name 
    				+ " has been registered with the "
    				+ "property change delegate, but it has not."
    				+ " Please correct your code."
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
			else if( this.property_map.containsKey( property_name ) ){
				return_values.put(
					property_name,
					this.property_map.get( property_name ) 
				);
			}
			else {
				throw new IllegalArgumentException(
					"The " + property_names + " property has not been "
					+ "registered with the property change delegate. "
					+ "Please go and fix your code."
				);
			}
		}
		
		return return_values;
	}
	
	public void
	notifyViews
	( String event_name, Object data_package )
	// the following is sketchy...
	// it basically means I have to throw another layer somewhere
	// either behind the controller delegate but between the model
	// and the delegate, or between the view and the controller
	// or side by side as a partner object
	{
		this.firePropertyChange( event_name, null, data_package);
	}
}
