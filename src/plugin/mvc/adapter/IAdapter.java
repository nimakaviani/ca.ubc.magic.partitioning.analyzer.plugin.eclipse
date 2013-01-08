package plugin.mvc.adapter;

import java.util.Map;

public interface 
IAdapter 
{
	// in the case of a property adapter the key is the property name
	// and the value is the data packet
	// for the property; in the case of a query, it is all the query 
	// strings ( the values are the associated packets )
	// in the case of the event, it is the event name (the value is the
	// data packet)
	public Object[] adapt( Map<String, Object> objs, Object arg );
	public String[] getKeys();
}
