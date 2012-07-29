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
	Map<String, IAdapter> adapter_map;
	
	public 
	AdapterDelegate()
	{
		this.callback_map
			= new HashMap<String, Callback>();
		this.adapter_map
			= new HashMap<String, IAdapter>();
	}
	
	public void
	registerCallback
	( Callback callback, IAdapter adapter )
	{
		this.callback_map.put(callback.getName(), callback);
		this.adapter_map.put(callback.getName(), adapter );
	}
	
	public void
	unregisterCallback
	( String method_name )
	{
		if( !this.callback_map.containsKey(method_name)){
			throw new IllegalArgumentException("That method is not registered as a callback.");
		}
		else {
			this.callback_map.remove(method_name);
			this.adapter_map.remove(method_name);
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
			return this.adapter_map.get(method_name).getQueryKeys();
		}
	}
	
	public Object[]
	getMethodParameters
	( String method_name, Map<String, Object> objs )
	{
		return this.adapter_map.get(method_name).adapt(objs);
	}

	@SuppressWarnings("rawtypes")
	public Class[] 
	getParameterTypes
	( String method_name ) 
	{
		return this.callback_map.get(method_name).getParameters().toArray( new Class[0] );
	}
}
