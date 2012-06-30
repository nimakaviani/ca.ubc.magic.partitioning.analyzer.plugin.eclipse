package snapshots.controller;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import org.eclipse.jface.action.Action;

import plugin.Activator;
import plugin.Constants;

import snapshots.com.mentorgen.tools.util.profile.Finish;
import snapshots.events.logging.EventLogger;
import snapshots.model.Snapshot;
import snapshots.views.IView;

public class 
FinishAction 
extends Action 
implements IView
{
	private Snapshot 					current_snapshot;
	private EventLogger					event_logger;
	private IController 				controller;
	
	public
	FinishAction
	( IController controller )
	{
		this.event_logger 
			= new EventLogger();
		this.controller
			= controller;
		
		this.controller.addView(this);
		
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
			this.controller.alertPeers(
				//new SnapshotEvent(
					Constants.SNAPSHOT_CAPTURED_PROPERTY,
					this,
					//this.current_snapshot
					null
				//)
			);
	    }
	    else {
	    	this.controller.alertPeers(
	    		Constants.SNAPSHOT_CAPTURE_FAILED,
	    		this,
	    		null
	    	);
	    }
		this.controller.setModelProperty(
			Constants.NAME_PROPERTY, ""
		);
		this.controller.alertPeers(
			Constants.SNAPSHOT_CAPTURED_PROPERTY, 
			this, null
		);
	    this.setEnabled(false);
	}

	@Override
	public void 
	modelPropertyChange
	( PropertyChangeEvent evt ) 
	{
		switch(evt.getPropertyName()){
		case Constants.SNAPSHOT_STARTED:
			this.setEnabled(true);
		      this.current_snapshot 
		      	= (Snapshot) evt.getNewValue();
			break;
		default:
			System.out.println("FinishAction swallowing event: " + evt.getPropertyName());
			break;
		}
	}
}
