package jipplugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

// the following technique is borrowed from the
// Eclipse Plug-ins book
public class 
ActivatorLog 
{
	public static void
	logMessage
	( String message )
	{
		logStatus(new Status(IStatus.INFO, Activator.PLUGIN_ID, IStatus.OK, message, null));
	}
	
	public static void
	logError
	( String message, Throwable exception )
	{
		logStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, exception));
	}
	
	public static void
	logError
	( Throwable exception )
	{
		logError("Unexpected Exception", exception);
	}
	
	private static void 
	logStatus
	(IStatus status)
	{
		Activator.getDefault().getLog().log(status);
	}
}
