package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import plugin.mvc.messages.DataEvent;
import plugin.mvc.messages.PropertyEvent;

public class 
PropertyChangeDelegate 
// any model which wishes to make use of the getAll() functionality must
// register the available properties with the delegate
{
	protected transient PropertyChangeSupport listeners 
		= new PropertyChangeSupport(this);
	
	Map<String, Object> property_map
		= new HashMap<String, Object>();
	
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
    ( PropertyEvent event, Object old_value, Object new_value )
    {
    	String property_name
    		= event.NAME;
    	
    	event.validatePackage(new_value);
    	this.firePropertyChange(property_name, old_value, new_value);
    }
    
    private void
    firePropertyChange
    ( String property_name, Object old_value, Object new_value )
    {
    	// to deal with reference switches
    	if( this.property_map.containsKey(property_name)){
        	this.property_map.put(property_name, new_value);
    	}
    	
        if (this.listeners.hasListeners(property_name)) {
            this.listeners.firePropertyChange(
            	property_name, 
            	old_value, 
            	new_value
            );
        }
    }
    
//    public void
//    firePropertyChange
//    ( String property_name, Object new_value )
//    {
//    	if( this.listeners.hasListeners(property_name) ) {
//    		if(this.property_map.containsKey(property_name)){
//    			Object old_value
//    				= this.property_map.get(property_name);
//    			this.listeners.firePropertyChange(
//    				property_name, 
//    				old_value, 
//    				new_value
//    			);
//    		}
//    		else {
//    			throw new IllegalArgumentException(
//    				"You assumed that the property "
//    				+ property_name 
//    				+ " has been registered with the "
//    				+ "property change delegate, but it has not."
//    				+ " Please correct your code."
//    			);
//    		}
//        }
//    }

	public Map<String, Object> 
	getAll
	( String[] property_names ) 
	{
		Map<String, Object> return_values 
			= new HashMap<String, Object>( property_names.length );
	
		for( String property_name : property_names ){
			if( this.property_map.containsKey( property_name ) ){
				return_values.put(
					property_name,
					this.property_map.get( property_name ) 
				);
			}
			else {
				throw new IllegalArgumentException(
					"The " + property_name + " property has not been "
					+ "registered with the property change delegate. "
					+ "Please go and fix your code."
				);
			}
		}
		
		return return_values;
	}
	
	public void
	notifyViews
	( DataEvent model_event, Object data_package )
	{
		String event_name
			= model_event.NAME;
		
		model_event.validatePackage( data_package );
		
		this.firePropertyChange( 
			event_name, 
			ControllerDelegate.EVENT_SENTINEL, 
			data_package
		);
	}
}
