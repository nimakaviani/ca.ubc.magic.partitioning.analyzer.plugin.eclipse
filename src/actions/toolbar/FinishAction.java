package actions.toolbar;

import java.io.IOException;

import jipplugin.Activator;


import model_controllers.Constants;
import model_controllers.IController;
import model_controllers.Snapshot;

import org.eclipse.jface.action.Action;

import com.mentorgen.tools.util.profile.Finish;

import events.logging.EventLogger;
import events.snapshots.ISnapshotEventListener;
import events.snapshots.SnapshotEvent;
import events.snapshots.SnapshotEventManager;

public class 
FinishAction 
extends Action 
implements ISnapshotEventListener
{
	private final SnapshotEventManager 	snapshot_event_manager;
	private Snapshot 					current_snapshot;
	private EventLogger					event_logger;
	private IController 				active_snapshot_controller;
	private IController 				snapshot_list_controller;
	
	public
	FinishAction
	( 	SnapshotEventManager snapshot_event_manager, 
		IController active_snapshot_controller,
		IController snapshot_list_controller )
	{
		this.event_logger 
			= new EventLogger();
		this.active_snapshot_controller
			= active_snapshot_controller;
		this.snapshot_list_controller
			= snapshot_list_controller;
		
		// we'll see if something else needs a reference to this
		this.snapshot_event_manager
			= snapshot_event_manager;
		this.snapshot_event_manager.addSnapshotEventListener(
			this
		);
		
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
			snapshot_event_manager.fireSnapshotEvent(
				new SnapshotEvent(
					SnapshotEvent.ID_SNAPSHOT_CAPTURED,
					this.current_snapshot
				)
			);
	    }
	    else {
	    	snapshot_event_manager.fireSnapshotEvent(
	    		new SnapshotEvent(
	    			SnapshotEvent.ID_SNAPSHOT_CAPTURE_FAILED,
	    			this.current_snapshot
	    		)
	    	);
	    }
		this.active_snapshot_controller.setModelProperty(
			Constants.NAME_PROPERTY, ""
		);
		this.snapshot_list_controller.setModelProperty(
			Constants.SNAPSHOT_PROPERTY, this.current_snapshot
		);
	    this.setEnabled(false);
	}

	 /* ---------------- from SnapshotEventListener ------ */

	@Override
	public void 
	handleSnapshotEvent
	(SnapshotEvent event) 
	{
		if (event.getId() == SnapshotEvent.ID_SNAPSHOT_STARTED) {
		      this.setEnabled(true);
		      this.current_snapshot 
		      	= event.getSnapshot();
		    }
	}
}
