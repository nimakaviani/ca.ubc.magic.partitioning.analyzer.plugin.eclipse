package model_controllers;

import java.beans.PropertyChangeListener;


public interface 
IModelController
{
	IController getController();
	void addPropertyChangeListener(PropertyChangeListener controllerDelegate);
	void removePropertyChangeListener(PropertyChangeListener controllerDelegate);
}
