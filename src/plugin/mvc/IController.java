package plugin.mvc;

import java.beans.PropertyChangeListener;

import plugin.mvc.adapter.AdapterDelegate;
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
	
	public void 				notifyPeers(ViewsEvent event, Object source, Object new_value);
	public void 				notifyModel(ToModelEvent event);
	// public Object 				index(IndexEvent event, Object key);
	
	public void 				updateModel( PropertyEvent event, Object contribution );
	void registerAdapter(IView view, AdapterDelegate adapter);
	void unregisterAdapter(IView view);
	void requestReply( IView view, String method_name, Object args);
}