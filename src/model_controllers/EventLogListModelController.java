package model_controllers;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class 
EventLogListModelController
implements IModelController
{
	IController controller 
		= new ControllerDelegate();
	PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	
	// not sure if copy on write is the best choice here
	private List<Object> event_log_list
		= new CopyOnWriteArrayList<Object>();
	
	public 
	EventLogListModelController()
	{
		this.controller.addModel(this);
	}
	
	public void 
	updateLog
	(String property) 
	{
		this.event_log_list.add(property);
		
		this.property_change_delegate.firePropertyChange(
			Constants.EVENT_LIST_PROPERTY, null, this.event_log_list
		);
	}
	
	public List<Object>
	getEventLogList()
	{
		return this.event_log_list;
	}
	
	@Override
	public IController 
	getController() 
	{
		return this.controller;
	}

	@Override
	public void
	addPropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.
			addPropertyChangeListener(l);
	}

	@Override
	public void 
	removePropertyChangeListener
	( PropertyChangeListener l)
	{
		this.property_change_delegate.
			removePropertyChangeListener(l);
	}
}
