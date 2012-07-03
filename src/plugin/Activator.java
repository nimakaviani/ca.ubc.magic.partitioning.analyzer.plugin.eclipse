package plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import snapshots.events.logging.EventLogActionHandler;
import snapshots.model.EventLogListModel;

public class 
Activator 
extends AbstractUIPlugin 
// TODO: get rid of the global models
{
	public static final String PLUGIN_ID 
		= "plugin";
	private static Activator plugin;
	
	private EventLogActionHandler action_handler;
	private EventLogListModel event_log_list_model;
	private Object file_tree_content_provider;
	
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
		
		this.action_handler 
			= new EventLogActionHandler();
		this.event_log_list_model
			= new EventLogListModel();
		
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
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

	public void 
	persistTreeContentProvider
	( Object missing ) 
	{
		this.file_tree_content_provider
			= missing;
	}
	
	public Object
	getTreeContentProvider()
	{
		return this.file_tree_content_provider;
	}
}