package plugin.mvc.adapter;

import java.util.Map;

public interface 
IAdapter 
{
	// in the case of a property adapter the key is the property name
	// for the property; in the case of a query, it is the query string
	public Object[] adapt( Map<String, Object> objs, Object arg );
	String[] getKeys();
}
