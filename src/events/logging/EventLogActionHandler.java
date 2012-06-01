package events.logging;

import java.util.HashMap;

public class 
EventLogActionHandler
{
	HashMap<String, IEventLogAction> actions_map
		= new HashMap<String, IEventLogAction>();

	public void
	registerAction
	( String key, IEventLogAction action)
	{
		this.actions_map.put(key, action);
	}
	public void 
	performActionByKey
	(String key, EventLogEvent event)
	{
		IEventLogAction action 
			= this.actions_map.get(key);
		
		if(action != null){
			action.performAction(event);
		}
	}
}
