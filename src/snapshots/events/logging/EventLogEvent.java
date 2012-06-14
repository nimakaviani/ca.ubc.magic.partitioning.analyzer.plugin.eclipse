package snapshots.events.logging;

import java.awt.event.ActionEvent;
import java.util.HashMap;

@SuppressWarnings("serial")
public class 
EventLogEvent
extends ActionEvent
{
	HashMap<String, Object> hash_map;

	
	public 
	EventLogEvent
	(Object source, int id, String command)
	{
		super(source, id, command);
		
		this.hash_map 
			= new HashMap<String, Object>();
	}

	public Object 
	getProperty
	(String key) 
	{
		return this.hash_map.get(key);
	}
	
	public void
	addProperty
	(String key, Object property)
	{
		this.hash_map.put(key, property);
	}
}
