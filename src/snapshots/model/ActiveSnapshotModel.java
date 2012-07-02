package snapshots.model;

import java.beans.PropertyChangeListener;

import plugin.Constants;

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
		this.property_change_delegate
			.registerProperty( Constants.PATH_PROPERTY, this.path );
		this.property_change_delegate
			.registerProperty( Constants.NAME_PROPERTY, this.name );
		this.property_change_delegate
			.registerProperty( Constants.PORT_PROPERTY, this.port );
		this.property_change_delegate
			.registerProperty( Constants.HOST_PROPERTY, this.host );
	}
	
	/*
	@Override
	public String
	getSnapshotPath() 
	{
		return this.path;
	}
	
	@Override
	public String 
	getSnapshotPort() 
	{
		return this.port;
	}
	
	@Override
	public String 
	getSnapshotName() 
	{
		return this.name;
	}
	
	@Override
	public String 
	getSnapshotHost() 
	{
		return this.host;
	} */

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
	public Object[] 
	request
	( String[] property_names ) 
	{
		return this.property_change_delegate.getAll(property_names);
	}
}
