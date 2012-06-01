package model_controllers;

import java.beans.PropertyChangeListener;


public class
ActiveSnapshotModelController 
implements ISnapshotInfoModel
{
	private String path = new String("");
	private String name = new String("");
	private String port = new String("");
	private String host = new String("");
	
	private PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	private IController controller 
		= new ControllerDelegate();
	
	public 
	ActiveSnapshotModelController()
	{
		this.controller.addModel(this);
	}
	
	@Override
	public String
	getSnapshotPath() 
	{
		return this.path;
	}

	@Override
	public void 
	setSnapshotPath
	(String path) 
	{
		String old_path = this.path;
		this.path = path;
		
		System.out.println(this.path);
		this.property_change_delegate.
			firePropertyChange(Constants.PATH_PROPERTY, old_path, this.path);
	}
	
	@Override
	public String 
	getSnapshotName() 
	{
		return this.name;
	}
	
	@Override
	public void
	setSnapshotName
	(String name)
	{
		String old_name = this.name;
		this.name 		= name;
		
		this.property_change_delegate.firePropertyChange(
			Constants.NAME_PROPERTY, old_name, this.name
		);
	}

	@Override
	public String 
	getSnapshotPort() 
	{
		return this.port;
	}
	
	@Override
	public void
	setSnapshotPort
	(String port)
	{
		String old_port = this.port;
		this.port = port;
		
		this.property_change_delegate.firePropertyChange(
			Constants.PORT_PROPERTY, old_port, this.port
		);
	}

	@Override
	public String 
	getSnapshotHost() 
	{
		return this.host;
	}
	
	@Override
	public void
	setSnapshotHost
	(String host)
	{
		String old_host = this.host;
		this.host = host;
		
		this.property_change_delegate.firePropertyChange(
			Constants.HOST_PROPERTY, old_host, this.host
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
	public IController 
	getController() 
	{
		return this.controller;
	}
}
