package plugin.mvc;

import java.beans.PropertyChangeEvent;

public interface 
IView 
{
	void modelPropertyChange(PropertyChangeEvent evt);
	void modelEvent(PropertyChangeEvent evt);
}
