package snapshots.model;

import java.beans.PropertyChangeListener;
import java.util.Map;

import plugin.mvc.DefaultPropertyDelegate;
import plugin.mvc.ITranslator.IModel;

public class
ActiveSnapshotModel 
// implements ISnapshotInfoModel
implements IModel
{
	private String path 
		= "";
	private String name 
		= "";
	private String port 
		= "15599";
	private String host 
		= "localhost";
	
	private DefaultPropertyDelegate property_change_delegate
		= new DefaultPropertyDelegate();
	
	public 
	ActiveSnapshotModel()
	{
		String[] property_names
			= {
				SnapshotModelMessages.PATH.NAME,
				SnapshotModelMessages.NAME.NAME,
				SnapshotModelMessages.PORT.NAME,
				SnapshotModelMessages.HOST.NAME
			};
		Object[] properties
			= {
				this.path,
				this.name,
				this.port,
				this.host
			};
		
		this.property_change_delegate.registerProperties( 
			property_names, 
			properties 
		);
	}

	// the reason that synchronized methods are used, rather
	// than finer per-field locks, is that chains of property
	// updates could cause a thread to acquire multiple locks;
	// if multiple threads do this in the wrong order, deadlock
	// will occur;
	//
	// the reason we synchronize is to ensure that when multiple
	// threads are accessing a model, we want to ensure that the
	// last value recorded in the model is the last value seen by
	// the view
	synchronized public void 
	setSnapshotPath
	(String path) 
	{
		String old_path = this.path;
		this.path = path;
		
		this.property_change_delegate.firePropertyChange(
			SnapshotModelMessages.PATH, 
			old_path,
			this.path
		);
	}
	
	synchronized public void
	setSnapshotName
	(String name)
	{
		String old_name = this.name;
		this.name 		= name;
		
		this.property_change_delegate.firePropertyChange(
			SnapshotModelMessages.NAME, 
			old_name,
			this.name
		);
	}

	synchronized public void
	setSnapshotPort
	(String port)
	{
		String old_port = this.port;
		this.port = port;
		
		this.property_change_delegate.firePropertyChange(
			SnapshotModelMessages.PORT, 
			old_port,
			this.port
		);
	}

	synchronized public void
	setSnapshotHost
	(String host)
	{
		String old_host = this.host;
		this.host = host;
		
		this.property_change_delegate
			.firePropertyChange(
				SnapshotModelMessages.HOST,
				old_host,
				this.host
			);
	}

	@Override
	public void 
	addPropertyChangeListener
	( PropertyChangeListener l ) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
	}

	@Override
	public void 
	removePropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}

	@Override
	public Map<String, Object>
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}
}
