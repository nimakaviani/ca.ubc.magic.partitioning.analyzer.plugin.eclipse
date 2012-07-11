package snapshots.model;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import plugin.Constants;
import plugin.mvc.IModel;
import plugin.mvc.PropertyChangeDelegate;


public class 
EventLogListModel
implements IModel
{
	PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	
	// not sure if copy on write is the best choice here
	private List<Object> event_log_list
		= new CopyOnWriteArrayList<Object>();
	
	public 
	EventLogListModel()
	{
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

	@Override
	public Object[] 
	request
	( String[] property_names ) 
	{
		// not implementing this functionality here
		return new Object[]{};
	}
}
