package plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class 
Activator 
extends AbstractUIPlugin 
{
	public static final String PLUGIN_ID 
		= "plugin";
	private static Activator plugin;
	
	private Object 					file_tree_content_provider;
	
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