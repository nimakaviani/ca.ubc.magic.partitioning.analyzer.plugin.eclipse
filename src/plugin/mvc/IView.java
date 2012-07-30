package plugin.mvc;

import java.beans.PropertyChangeEvent;

public interface 
IView 
{
	public void modelPropertyChange(PropertyChangeEvent evt);
	public void modelEvent(PropertyChangeEvent evt);
}
