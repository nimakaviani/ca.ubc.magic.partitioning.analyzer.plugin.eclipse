package plugin.mvc;

import java.beans.PropertyChangeEvent;

import plugin.mvc.adapter.AdapterDelegate;

public interface 
IView 
{
	public void modelPropertyChange(PropertyChangeEvent evt);
	public void modelEvent(PropertyChangeEvent evt);
}
