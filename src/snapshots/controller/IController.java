package snapshots.controller;

import java.beans.PropertyChangeListener;



import snapshots.model.IModel;
import snapshots.views.IView;

public interface
IController
extends PropertyChangeListener 
{
	public void addModel(IModel model);
	public void removeModel(IModel model);
	public void addView(IView view);
	public void removeView(IView view);
	public void setModelProperty(String property_name, Object new_value);
	public void alertPeers(String event_name, Object source, Object new_value);
	public void notifyModel(String event_name);
}