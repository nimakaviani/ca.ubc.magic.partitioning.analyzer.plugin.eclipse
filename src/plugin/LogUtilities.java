package plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

// the following class is modelled after the class
// described in page 129 of the Eclipse Plugins book
public class 
LogUtilities 
{
	private LogUtilities(){}

	public static void 
	displayErrorDialog
	(Shell shell, String string) 
	{
		ErrorDialog.openError(
				shell, 
				null, 
				null,
				new Status(
					IStatus.ERROR, 
					Activator.PLUGIN_ID, 
					IStatus.OK, 
					string,
					null
				)
			);
	}
	
	public static void 
	logInfo
	( String message )
	{
		log( IStatus.INFO, IStatus.OK, message, null );
	}
	
	public static void 
	logError
	( Throwable exception )
	{
		logError("Unexpected Exception", exception);
	}
	
	public static void 
	logError
	( String message, Throwable exception )
	{
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}
	
	public static void
	log
	( int severity, int code, String message, Throwable exception) 
	{
		log(createStatus(severity, code, message, exception));
	}
	
	public static IStatus
	createStatus
	( int severity, int code, String message, Throwable exception )
	{
		return new Status( 
			severity, 
			Activator.PLUGIN_ID, 
			code, message, exception
		);
	}
	
	public static void 
	log
	( IStatus status )
	{
		Activator.getDefault().getLog().log(status);
	}
}
