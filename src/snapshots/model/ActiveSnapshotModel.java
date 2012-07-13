package snapshots.model;

import java.beans.PropertyChangeListener;
import java.util.Map;

import plugin.Constants;
import plugin.mvc.PropertyChangeDelegate;

public class
ActiveSnapshotModel 
implements ISnapshotInfoModel
// if you intend to use the request function, or to use
// the more convenient form of the firePropertyChange function,
// you must register any visible fields with the property
// change delegate
{
	private String path 
		= new String("");
	private String name 
		= new String("");
	private String port 
		= new String("15599");
	private String host 
		= new String("localhost");
	
	private PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	
	public 
	ActiveSnapshotModel()
	{
		String[] property_names
			= {
				Constants.PATH_PROPERTY,
				 Constants.NAME_PROPERTY,
				 Constants.PORT_PROPERTY,
				 Constants.HOST_PROPERTY
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

	@Override
	public void 
	setSnapshotPath
	(String path) 
	{
		System.out.println("New path: " + path);
		String old_path = this.path;
		this.path = path;
		
		System.out.println(this.path);
		this.property_change_delegate.
			firePropertyChange(Constants.PATH_PROPERTY, old_path);
	}
	
	@Override
	public void
	setSnapshotName
	(String name)
	{
		String old_name = this.name;
		this.name 		= name;
		
		this.property_change_delegate.firePropertyChange(
			Constants.NAME_PROPERTY, old_name
		);
	}

	@Override
	public void
	setSnapshotPort
	(String port)
	{
		String old_port = this.port;
		this.port = port;
		
		this.property_change_delegate.firePropertyChange(
			Constants.PORT_PROPERTY, old_port
		);
	}

	@Override
	public void
	setSnapshotHost
	(String host)
	{
		String old_host = this.host;
		this.host = host;
		
		this.property_change_delegate
			.firePropertyChange(
				Constants.HOST_PROPERTY, old_host
			);
	}

	@Override
	public void 
	addPropertyChangeListener
	( PropertyChangeListener l) 
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
