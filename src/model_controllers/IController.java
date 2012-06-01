package model_controllers;

import java.beans.PropertyChangeListener;


import views.IView;

public interface
IController
extends PropertyChangeListener 
{
	public void addModel(IModelController model);
	public void removeModel(IModelController model);
	public void addView(IView view);
	public void removeView(IView view);
	public void setModelProperty(String property_name, Object new_value);
}