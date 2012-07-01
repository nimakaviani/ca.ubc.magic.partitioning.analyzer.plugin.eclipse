package snapshots.model;

import java.beans.PropertyChangeListener;

public interface 
IModel
{
	void addPropertyChangeListener(PropertyChangeListener controllerDelegate);
	void removePropertyChangeListener(PropertyChangeListener controllerDelegate);
	Object[] request(String[] property_names);
}
