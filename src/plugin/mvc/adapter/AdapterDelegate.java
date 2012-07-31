package plugin.mvc.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// the adapter is supposed to help us change
// the model without changing the view; we still
// have the problem of making sure we can change the view
// without affecting the model; because it can be initialized
// based on the interface of a view and the messages allowed
// by a model, the adapter can be injected as a dependency to
// the view, and modified or substituted afterwards
public class 
AdapterDelegate 
{
	private Map<String, Callback> 	callback_map;
	private Map<String, IAdapter> 	method_to_adapter_map;
	
	private Map<String, String> 	property_to_method_map;
	private Map<String, String> 	event_to_method_map;
	
	public 
	AdapterDelegate()
	{
		this.callback_map
			= new HashMap<String, Callback>();
		this.method_to_adapter_map
			= new HashMap<String, IAdapter>();
		this.property_to_method_map
			= new HashMap<String, String>();
		
		this.event_to_method_map
			= new HashMap<String, String>();
	}
	
	public String[] 
	getKeys
	( String method_name ) 
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback.");
		}
		else {
			return this.method_to_adapter_map.get(method_name).getKeys();
		}
	}

	public void
	unregisterDepositCallback
	( String method_name )
	{
		unregisterCallback( method_name );
	}
	
	public void
	unregisterPropertyCallback
	( String method_name )
	{
		unregisterCallback( method_name );
		
		String[] keys
			= this.getKeys( method_name);
		for( String s : keys ){
			this.property_to_method_map.remove(s);
		}
	}
	
	public void
	unregisterEventCallback
	( String method_name )
	{
		unregisterCallback( method_name );
		
		String[] keys
			= this.getKeys( method_name );
		for( String s : keys ){
			this.event_to_method_map.remove(s);
		}
	}
	
	private void
	unregisterCallback
	( 	String method_name )
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback");
		}
		else {
			this.method_to_adapter_map.remove(method_name);
		}
	}
	
	public void
	registerDepositCallback
	( Callback callback, IAdapter adapter )
	{
		this.callback_map.put(callback.getName(), callback);
		this.method_to_adapter_map.put(callback.getName(), adapter );
	}
	
	public void
	registerPropertyCallback
	( Callback callback, IAdapter adapter )
	{
		if( this.callback_map.containsKey(callback.getName())){
			System.err.println("Callback for " + callback.getName() + " is already contained. Not added.");
		}
		else {
			this.callback_map.put(callback.getName(), callback);
			this.method_to_adapter_map.put( callback.getName(), adapter);
			String[] keys 
				= this.getKeys( callback.getName() );
			for( String s : keys ){
				this.property_to_method_map.put(s, callback.getName());
			}
		}
	}
	
	public void
	registerEventCallback
	( Callback callback, IAdapter adapter)
	{
		if( this.callback_map.containsKey(callback.getName())){
			System.err.println("Callback for " + callback.getName() + " is already contained");
		}
		else {
			this.callback_map.put( callback.getName(), callback);
			this.method_to_adapter_map.put( callback.getName(), adapter);
			String[] keys
				= this.getKeys( callback.getName());
			for( String s : keys ){
				this.event_to_method_map.put(s, callback.getName());
			}
		}
	}
	
	public Object[]
	getMethodParameters
	( String method_name, Map<String, Object> objs )
	{
		return this.getMethodParameters(method_name, objs, null);
	}
	
	public Object[]
	getMethodParameters
	( String method_name, Map<String, Object> objs, Object args )
	{
		if( !this.method_to_adapter_map.containsKey(method_name)){
			throw new IllegalArgumentException(
				"Method " + method_name + " is not associated with a property adapter"
			);
		}
		return this.method_to_adapter_map.get(method_name).adapt(objs, args);
	}

	public Class<?>[] 
	getParameterTypes
	( String method_name ) 
	{
		if( !this.callback_map.containsKey(method_name) ){
			throw new IllegalArgumentException(
				"Method " + method_name + " is not registered as a callback"
			);
		}
		List<Class<?>> parameters 
			= this.callback_map.get(method_name).getParameters();
		return parameters.toArray( new Class<?>[parameters.size()] );
	}
	
	public String
	getPropertyMethodName
	( String property )
	{
		return getMethodName(property, this.property_to_method_map );
		
	}
	
	public String
	getEventMethodName
	( String property )
	{
		return getMethodName(property, this.event_to_method_map);
	}
	
	private String 
	getMethodName
	( String property, Map<String, String> map ) 
	{
		if( map.containsKey(property)){
			return map.get(property);
		}
		else {
			System.out.println("This adapter does not contain property " + property);
			return null;
		}
	}
}
