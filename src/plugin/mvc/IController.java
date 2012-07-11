package plugin.mvc;

import java.beans.PropertyChangeListener;

import snapshots.views.IView;

public interface
IController
extends PropertyChangeListener 
{
	
	public void addModel(IModel model);
	public void removeModel();
	public void addView(IView view);
	public void removeView(IView view);
	
	public Object[] requestProperties(String[] property_names);
	public void setModelProperty(String property_name, Object new_value);
	public void notifyPeers(String event_name, Object source, Object new_value);
	public void notifyModel(String event_name);
	public Object index(String simulationUnits, Object key);
}