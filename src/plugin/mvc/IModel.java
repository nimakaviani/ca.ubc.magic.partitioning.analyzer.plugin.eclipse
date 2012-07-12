package plugin.mvc;

import java.beans.PropertyChangeListener;
import java.util.Map;

public interface 
IModel
{
	void addPropertyChangeListener(PropertyChangeListener controllerDelegate);
	void removePropertyChangeListener(PropertyChangeListener controllerDelegate);
	Map<String, Object> request(String[] property_names);
}
