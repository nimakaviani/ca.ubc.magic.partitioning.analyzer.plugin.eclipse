package snapshots.views;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import org.eclipse.jface.action.Action;

import plugin.Activator;
import plugin.Constants;
import plugin.mvc.IController;
import plugin.mvc.IView;

import snapshots.com.mentorgen.tools.util.profile.Finish;
import snapshots.events.logging.EventLogger;
import snapshots.model.Snapshot;
import snapshots.model.SnapshotModelMessages;

public class 
FinishAction 
extends Action 
implements IView
{
	private Snapshot 					current_snapshot;
	private EventLogger					event_logger;
	private IController 				active_snapshot_controller;
	
	public
	FinishAction
	( IController active_snapshot_controller )
	{
		this.event_logger 
			= new EventLogger();
		this.active_snapshot_controller
			= active_snapshot_controller;
		
		this.active_snapshot_controller.addView( this );
		
		this.setToolTipText
		("Disconnect from application to produce snapshot.");
		
		this.setEnabled(false);
	}
	
	@Override
	public void
	setEnabled
	(boolean enabled)
	{
		super.setEnabled(enabled);
		
		String image_path
			= enabled 
			? "icons/disconnect_co_active.gif"
			: "icons/disconnect_co2.gif";
		
		this.setImageDescriptor
		(Activator.getImageDescriptor(image_path));
	}
	
	@Override
	public void run()
	{
		try{
			inner_run();
		}
		catch( IOException ex ){
			ex.printStackTrace();
		}
	}
	
	private void 
	inner_run()
	throws IOException 
	{
		boolean got_exception = false;
		try {
			Finish.doFinish(
				this.current_snapshot.getHost(), 
				this.current_snapshot.getPort()
			);
		}
		catch(IOException ioex){
			got_exception = true;
			this.event_logger.updateConsoleLog(ioex);
		}
		
		if (!got_exception) {
			this.event_logger.updateForSuccessfulCall(
				"finish"
			);
			this.active_snapshot_controller.notifyPeers(
					SnapshotModelMessages.SNAPSHOT_CAPTURED,
					//Constants.EVENT_SNAPSHOT_CAPTURED_PROPERTY,
					this,
					null
			);
	    }
	    else {
	    	this.active_snapshot_controller.notifyPeers(
	    		SnapshotModelMessages.SNAPSHOT_CAPTURE_FAILED,
	    		//Constants.EVENT_SNAPSHOT_CAPTURE_FAILED,
	    		this,
	    		null
	    	);
	    }
		this.active_snapshot_controller.updateModel(
			SnapshotModelMessages.NAME, ""
		);
		this.active_snapshot_controller.notifyPeers(
			SnapshotModelMessages.SNAPSHOT_CAPTURED,
			// Constants.EVENT_SNAPSHOT_CAPTURED_PROPERTY, 
			this, null
		);
	    this.setEnabled(false);
	}

	@Override
	public void 
	modelPropertyChange
	( PropertyChangeEvent evt ) 
	{}
	
	@Override
	public void 
	modelEvent
	( PropertyChangeEvent evt ) 
	{
		String property
			= evt.getPropertyName();
		
		if( property.equals(SnapshotModelMessages.SNAPSHOT_STARTED.NAME)){
			this.setEnabled(true);
		      this.current_snapshot 
		      	= (Snapshot) evt.getNewValue();
		}
		else {
			System.out.println(
				"FinishAction swallowing event: " 
				+ evt.getPropertyName()
			);
		}
	}
}
