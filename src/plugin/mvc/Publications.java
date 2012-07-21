package plugin.mvc;

import snapshots.views.VirtualModelFileInput;

public enum 
Publications 
// the following technique is borrowed from
//	http://stackoverflow.com/questions/4290878/why-shouldnt-java-enum-literals-be-able-to-have-generic-type-parameters
{
	// first entry is event name, second is the class type of the associated packet
	MODEL_EDITOR_CLOSED( "ModelCreationEditorClosed", VirtualModelFileInput.class ),
	REFRESH_SNAPSHOT_TREE( "REFRESH", Boolean.class),
	ACTIVE_EDITOR_CHANGED("ACTIVE_EDITOR", ControllerDelegate.class);
	
	private Class<?> 	packet_class;
	private String		event_name;
	
	private 
	Publications
	(String event_name, Class<?> packet_class)
	{
		this.event_name
			= event_name;
		this.packet_class	
			= packet_class;
	}

	public String 
	getEventName()
	{
		return this.event_name;
	}
	
	public Class<?>
	getPacketClass()
	{
		return this.packet_class;
	}
}
