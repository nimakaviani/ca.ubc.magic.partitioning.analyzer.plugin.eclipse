package plugin.mvc;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

import snapshots.views.VirtualModelFileInput;

public interface 
IPublisher 
{
	public void
	publish
	( Class<?> sender_class, Publications publication, Object packet);
	
	public ServiceRegistration<EventHandler>
	registerPublicationListener
	( Class<?> listener_class, final Publications activeEditorChanged, final PublicationHandler publication_handler	);

	void unregisterPublicationListener(Publications publication, ServiceRegistration<EventHandler> id);

	public interface 
	PublicationHandler 
	{
		public void handle( Object obj );
	}
	
	public enum 
	Publications 
	// the following technique is borrowed from
	//		http://stackoverflow.com/questions/4290878/why-shouldnt-java-enum-literals-be-able-to-have-generic-type-parameters
	{
		// first entry is event name, second is the class type of the associated packet
		MODEL_EDITOR_CLOSED( "ModelCreationEditorClosed", VirtualModelFileInput.class ),
		REFRESH_SNAPSHOT_TREE( "REFRESH", Boolean.class),
		ACTIVE_EDITOR_CHANGED("ACTIVE_EDITOR", DefaultTranslator.class);
		
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
}
