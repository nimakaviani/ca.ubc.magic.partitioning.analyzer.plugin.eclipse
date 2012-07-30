package plugin.mvc.adapter;

import java.util.HashMap;
import java.util.Map;

// the adapter is supposed to help us change
// the model without changing the view; we still
// have the problem of making sure we can change the view
// without affecting the model
public class 
AdapterDelegate 
{
	Map<String, Callback> callback_map;
	Map<String, IAdapter> query_adapter_map;
	private Map<String, String> property_to_method_map;
	private Map<String, IAdapter> property_adapter_map;
	
	public 
	AdapterDelegate()
	{
		this.callback_map
			= new HashMap<String, Callback>();
		this.query_adapter_map
			= new HashMap<String, IAdapter>();
		this.property_to_method_map
			= new HashMap<String, String>();
		this.property_adapter_map
			= new HashMap<String, IAdapter>();
	}
	
	public void
	registerDepositCallback
	( Callback callback, IAdapter adapter )
	{
		this.callback_map.put(callback.getName(), callback);
		this.query_adapter_map.put(callback.getName(), adapter );
	}
	
	public void
	unregisterDepositCallback
	( String method_name )
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback.");
		}
		else {
			this.query_adapter_map.remove(method_name);
		}
		
		if( !this.property_adapter_map.containsKey(method_name)){
			this.callback_map.remove(method_name);
		}
	}
	
	public void
	registerPropertyCallback
	( Callback callback, IAdapter adapter )
	{
		if( this.callback_map.containsKey(callback)){
			System.err.println("Callback for " + callback.getName() + " is already contained. Not added.");
		}
		else {
			this.callback_map.put(callback.getName(), callback);
			this.property_adapter_map.put( callback.getName(), adapter);
			String[] keys 
				= this.getPropertyKeys( callback.getName() );
			for( String s : keys ){
				this.property_to_method_map.put(s, callback.getName());
			}
		}
	}
	
	public void
	unregisterPropertyCallback
	( String method_name )
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback");
		}
		else {
			String[] keys
				= this.getPropertyKeys(method_name);
			for( String s : keys ){
				this.property_to_method_map.remove(s);
			}
			this.property_adapter_map.remove(method_name);
		}
		
		if( !this.query_adapter_map.containsKey(method_name)){
			// only remove callback if it is missing from both maps
			this.callback_map.remove(method_name);
		}
	}
	
	public String[]
	getQueryKeys
	( String method_name )
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback.");
		}
		else {
			return this.query_adapter_map.get(method_name).getQueryKeys();
		}
	}
	
	private String[] 
	getPropertyKeys
	( String method_name ) 
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback");
		}
		else {
			return this.property_adapter_map.get(method_name).getQueryKeys();
		}
	}
	
	public Object[]
	getQueryMethodParameters
	( String method_name, Map<String, Object> objs )
	{
		return this.query_adapter_map.get(method_name).adapt(objs);
	}
	
	public Object[]
	getPropertyMethodParameters
	( String method_name, Map<String, Object> objs )
	{
		return this.property_adapter_map.get(method_name).adapt(objs);
	}

	@SuppressWarnings("rawtypes")
	public Class[] 
	getParameterTypes
	( String method_name ) 
	{
		return this.callback_map.get(method_name).getParameters().toArray( new Class[0] );
	}
	
	public String
	getMethodName
	( String property )
	{
		if( this.property_to_method_map.containsKey(property)){
			return this.property_to_method_map.get(property);
		}
		else {
			System.out.println("This adapter does not contain property " + property);
			return null;
		}
	}
}
