package plugin.mvc;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class 
PublisherDelegate
implements IPublisher
{
	// the following code is a view-communication solution
	// found in:
	// http://tomsondev.bestsolution.at/2011/01/03/enhanced-rcp-how-views-can-communicate/
	public void
	publish
	( Class<?> sender_class, String property_name, Object packet)
	{
		System.err.println("Publishing: ");
		if( packet == null){
			System.out.println("Null packet");
		}
		BundleContext context 
			= FrameworkUtil.getBundle(
				sender_class
			).getBundleContext();
	    ServiceReference<EventAdmin> ref 
	    	= context.getServiceReference(EventAdmin.class);
	    EventAdmin eventAdmin 
	    	= context.getService( ref );
	    Map<String,Object> properties 
	    	= new HashMap<String, Object>();
	    
	    properties.put( property_name, packet );
	    Event event = new Event("viewcommunication/asyncEvent", properties);
	    eventAdmin.postEvent(event);
	}
	
	public void
	registerPublicationListener
	( Class<?> listener_class, final String property_name, final PublicationHandler publication_handler	)
	{
		BundleContext context 
			= FrameworkUtil.getBundle(listener_class).getBundleContext();
		EventHandler handler 
			= new EventHandler() {
				public void handleEvent
				( final Event event )
				{
					System.err.println("Event received");
					// acceptable alternative, given that we run only
					// one display
					if( event.containsProperty(property_name)){
						Display display 
							= Display.getDefault();
						if( display.getThread() == Thread.currentThread() ){
							publication_handler.handle( event.getProperty(property_name));
						}
						else {
							display.syncExec( 
								new Runnable() {
									public void 
									run()
									{
										publication_handler.handle( event.getProperty(property_name));
									}
								}
							);
						}
					}
					
				}
			};
			
		Dictionary<String,String> properties 
			= new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunication/*");
		context.registerService(EventHandler.class, handler, properties);
	}
}
