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
		
		this.property_change_delegate
			.registerProperties( 
				property_names, 
				properties 
			);
	}

	public void 
	setSnapshotPath
	(String path) 
	{
		System.out.println("New path: " + path);
		String old_path = this.path;
		this.path = path;
		
		System.out.println(this.path);
		this.property_change_delegate
			.firePropertyChange(
				SnapshotModelMessages.PATH, 
				old_path,
				this.path
			);
	}
	
	public void
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

	public void
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

	public void
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
