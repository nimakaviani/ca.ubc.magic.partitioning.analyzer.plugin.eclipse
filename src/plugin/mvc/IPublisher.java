package plugin.mvc;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

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
}
