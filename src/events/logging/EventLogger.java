package events.logging;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import model_controllers.Constants;

import jipplugin.Activator;


public class
EventLogger 
{
	// log any exceptions thrown
	public void 
	updateConsoleLog
	(Exception ex)
	throws IOException 
	{
		Activator.getDefault().getLog().log(
			new Status( 
				IStatus.ERROR, 
				Activator.PLUGIN_ID, 
				IStatus.OK, 
				"Unexpected Exception", ex
			)
		);
		String output 
			= "Exception thrown (see Error Log View for details): " + ex.getMessage();
		updateConsoleLog(output);
	}
		
	// log missing data dialog window errors
	public EventLogEvent 
	getErrorEvent() 
	{
		EventLogEvent event 
			= new EventLogEvent(
				this,
				EventLogEvent.ACTION_PERFORMED,
				""
			);
		event.addProperty(Constants.KEY_ERR_DLGTITLE,"dlg.title.missing");
		
		return event;
	}
	
	// log a successful event
	public void 
	updateForSuccessfulCall
	(String call) 
	throws IOException 
	{
		StringBuffer buf
			= new StringBuffer();
		buf.append(MessageFormat.format(
			"The {0} call was successful.",
			new Object[] {call})
		);
		buf.append('\n');
		
		updateConsoleLog(buf.toString());
	}
	
	// write to the event log: main work horse
	public void 
	updateConsoleLog
	(String message) 
	throws IOException
	{
		System.out.println("Updating the console log");
		
		EventLogEvent event 
			= new EventLogEvent(
				this,
				EventLogEvent.ACTION_PERFORMED,
				""
			);
		event.addProperty(Constants.KEY_LOGS_DATA, message);
		EventLogActionHandler action_handler
			= Activator.getDefault().getActionHandler();
		action_handler.performActionByKey(
			Constants.ACTKEY_LOG_DISPLAY,
			event
		);
	}

}
