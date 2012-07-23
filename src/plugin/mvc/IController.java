package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.util.Map;

import plugin.mvc.messages.IndexEvent;
import plugin.mvc.messages.PropertyEvent;
import plugin.mvc.messages.ToModelEvent;
import plugin.mvc.messages.ViewsEvent;


public interface
IController
extends PropertyChangeListener 
{
	
	public void addModel(IModel model);
	public void removeModel();
	public void addView(IView view);
	public void removeView(IView view);
	
//	public void 			publish( Class<?> sender_class, Publications modelEditorClosed, Object packet);
//	public ServiceRegistration<EventHandler> 				registerPublicationListener( 
//		Class<?> listener_class, 
//		Publications publication, 
//		final PublicationHandler publication_handler 
//	);
//	public void 
//	unregisterPublicationListener
//	( Publications publication, ServiceRegistration<EventHandler> refresh_snapshot_event_registration );
	
	public void 				notifyPeers(ViewsEvent event, Object source, Object new_value);
	public void 				notifyModel(ToModelEvent event);
	public Object 				index(IndexEvent event, Object key);
	
	public Map<String, Object> 	requestProperties( String[] property_names );
	public void 				updateModel( PropertyEvent event, Object contribution );
}