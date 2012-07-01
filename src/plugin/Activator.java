package plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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
	
	// to implement persistence in the snapshot view I have...options:
	//		1) store the necessary data in the activator when it needs to 
	//		be saved, and restore upon load
	//		2) store in the file system
	//		3) 
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