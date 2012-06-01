package model_controllers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class 
PropertyChangeDelegate 
{
	protected transient PropertyChangeSupport listeners 
		= new PropertyChangeSupport(this);

    public void 
    addPropertyChangeListener
    ( PropertyChangeListener l )
    {
        if (l == null) {
            throw new IllegalArgumentException();
        }
        this.listeners.addPropertyChangeListener(l);
    }
    
    
    public void 
    removePropertyChangeListener
    (PropertyChangeListener l)
    {
        this.listeners.removePropertyChangeListener(l);
    }
  
    public void 
    firePropertyChange
    ( String prop, Object old, Object newValue )
    {
        if (this.listeners.hasListeners(prop)) {
            this.listeners.firePropertyChange(prop, old, newValue);
        }
    }

}
