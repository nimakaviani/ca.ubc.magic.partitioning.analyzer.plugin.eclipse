package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.util.Map;


public interface
IController
extends PropertyChangeListener 
{
	
	public void addModel(IModel model);
	public void removeModel();
	public void addView(IView view);
	public void removeView(IView view);
	
	public Map<String, Object> 	requestProperties( String[] property_names );
	public void 				setModelProperty(String property_name, Object new_value);
	public void 				notifyPeers(String event_name, Object source, Object new_value);
	public void 				notifyModel(String event_name);
	public Object 				index(String simulationUnits, Object key);
	public void 				publish( Class<?> sender_class, String property_name, Object packet);
	public void 				registerPublicationListener( 
		Class<?> listener_class, 
		final String property_name, 
		final PublicationHandler publication_handler 
	);
}