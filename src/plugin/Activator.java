package plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import snapshots.events.logging.EventLogActionHandler;
import snapshots.model.ActiveSnapshotModel;
import snapshots.model.EventLogListModel;
import snapshots.model.SnapshotsListModel;

public class 
Activator 
extends AbstractUIPlugin 
// TODO: get rid of the global models
{
	public static final String PLUGIN_ID 
		= "plugin";
	private static Activator plugin;
	
	private ActiveSnapshotModel active_snapshot_model;
	private EventLogActionHandler action_handler;
	private EventLogListModel event_log_list_model;
	private SnapshotsListModel snapshots_list_model;
	
	public 
	Activator() 
	{}

	public void 
	start
	( BundleContext context ) 
	throws Exception 
	{
		super.start(context);
		plugin = this;
		
		this.active_snapshot_model 
			= new ActiveSnapshotModel();
		this.action_handler 
			= new EventLogActionHandler();
		this.event_log_list_model
			= new EventLogListModel();
		this.snapshots_list_model
			= new SnapshotsListModel();
	}

	public void 
	stop
	( BundleContext context ) 
	throws Exception 
	{
		plugin = null;
		super.stop(context);
	}

	public static 
	Activator 
	getDefault() 
	{
		return plugin;
	}

	public static ImageDescriptor 
	getImageDescriptor
	( String path ) 
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public ActiveSnapshotModel 
	getActiveSnapshotModel() 
	{
		return this.active_snapshot_model;
	}
	
	public EventLogActionHandler
	getActionHandler()
	{
		return this.action_handler;
	}
	
	public EventLogListModel
	getEventLogListModel()
	{
		return this.event_log_list_model;
	}
	
	public SnapshotsListModel
	getSnapshotsListModel()
	{
		return this.snapshots_list_model;
	}
}