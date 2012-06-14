package snapshots.model;

import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import plugin.Constants;



public class
SnapshotsListModel 
implements IModel
{
	private List<Object> snapshots_list 
		= new LinkedList<Object>();
	
	private PropertyChangeDelegate property_change_delegate
		 = new PropertyChangeDelegate();
	
	public
	SnapshotsListModel()
	{
	}
	
    public void 
	setAdditionalSnapshot
	(Snapshot snapshot) 
	{
		boolean should_add 
			= this.snapshots_list.isEmpty() 
			|| this.snapshots_list.get(this.snapshots_list.size() - 1) != snapshot;
		
		if(should_add){
			this.snapshots_list.add(snapshot);
			this.property_change_delegate.
				firePropertyChange(Constants.SNAPSHOT_PROPERTY, null, this.snapshots_list);
			System.out.println("setAdditionalSnapshot(): snapshots_list length: " + this.snapshots_list.size());
		}
	}

	public List<Object> 
	getSnapshotsList() 
	{
		return this.snapshots_list;
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
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.
			removePropertyChangeListener(l);
	}
}
