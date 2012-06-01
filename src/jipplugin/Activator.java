package jipplugin;


import model_controllers.ActiveSnapshotModelController;
import model_controllers.EventLogListModelController;
import model_controllers.SnapshotsListModelController;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


import events.logging.EventLogActionHandler;

public class 
Activator 
extends AbstractUIPlugin
{
	public static final String PLUGIN_ID 
		= "JipPlugin"; 

	private static Activator plugin;
	
	ActiveSnapshotModelController 		active_snapshot_model;
	SnapshotsListModelController		snapshots_list_model;
	EventLogActionHandler 				action_handler;

	private EventLogListModelController event_log_list_model;
	
	public 
	Activator() 
	{}

	public void 
	start
	(BundleContext context) 
	throws Exception 
	{
		super.start(context);
		plugin = this;
		
		this.active_snapshot_model 
			= new ActiveSnapshotModelController();
		this.snapshots_list_model
			= new SnapshotsListModelController();
		this.action_handler 
			= new EventLogActionHandler();
		this.event_log_list_model
			= new EventLogListModelController();
	}

	public void 
	stop
	(BundleContext context) 
	throws Exception 
	{
		plugin = null;
		super.stop(context);
	}

	public static 
	Activator getDefault() 
	{
		return plugin;
	}

	public static 
	ImageDescriptor 
	getImageDescriptor
	(String path) 
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public ActiveSnapshotModelController 
	getActiveSnapshotModel() 
	{
		return this.active_snapshot_model;
	}
	
	public SnapshotsListModelController
	getSnapshotsListModel()
	{
		return this.snapshots_list_model;
	}
	
	public EventLogListModelController
	getEventLogListModel()
	{
		return this.event_log_list_model;
	}
	
	public EventLogActionHandler
	getActionHandler()
	{
		return this.action_handler;
	}
}
