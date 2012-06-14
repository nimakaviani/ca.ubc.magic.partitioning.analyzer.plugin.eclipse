package snapshots.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.eclipse.jface.action.Action;

import plugin.Activator;
import plugin.Constants;




import snapshots.com.mentorgen.tools.util.profile.Start;
import snapshots.controller.IController;
import snapshots.events.ISnapshotEventListener;
import snapshots.events.SnapshotEvent;
import snapshots.events.SnapshotEventManager;
import snapshots.events.logging.EventLogActionHandler;
import snapshots.events.logging.EventLogEvent;
import snapshots.events.logging.EventLogger;
import snapshots.model.ISnapshotInfoModel;
import snapshots.model.Snapshot;
import snapshots.views.SnapshotView;

public class 
StartAction 
extends Action 
implements ISnapshotEventListener
{
	private final SnapshotEventManager 	snapshot_event_manager;
	private IController 				controller;
	private EventLogger 				event_logger;
	private SnapshotView				snapshot_view;
	
	public 
	StartAction
	(	SnapshotEventManager snapshot_event_manager, IController controller, SnapshotView view)
	{
		this.snapshot_view 
			= view;
		this.controller
			= controller;
		this.event_logger
			= new EventLogger();
		
		this.snapshot_event_manager 
			= snapshot_event_manager;
		this.snapshot_event_manager.addSnapshotEventListener(
			this
		);
		this.setToolTipText
		("Connect and profile to produce snapshot.");
		this.setEnabled(true);
	}
	
	@Override
	public void
	setEnabled
	(boolean enabled)
	{
		super.setEnabled(enabled);
		
		String image_path
			= enabled 
			? "icons/run_exc_active.gif"
			: "icons/run_exc.gif";
	
		this.setImageDescriptor
		(Activator.getImageDescriptor(image_path));
	}
	
	@Override
	public void 
	run()
	{
		// the start action is now also responsible for
		// implementing the data check functionality
		
		if(!this.snapshot_view.valid_inputs())
			return;

		this.controller.setModelProperty(
			Constants.PATH_PROPERTY, this.snapshot_view.getPreviousPath()
		);
		this.controller.setModelProperty(
			Constants.NAME_PROPERTY, this.snapshot_view.getPreviousName()
		);
		this.controller.setModelProperty(
			Constants.HOST_PROPERTY, this.snapshot_view.getPreviousHost()
		);
		this.controller.setModelProperty(
			Constants.PORT_PROPERTY, this.snapshot_view.getPreviousPort()
		);
		
	    Snapshot snapshot 
	    	= this.getSnapshot();
	    
	    try{
	    	this.inner_run(snapshot);
	    }
	    catch(IOException ex){
        	ex.printStackTrace();
        }
	}

	private void 
	inner_run
	(Snapshot snapshot) 
	throws IOException 
	{
	    if (snapshot != null) {
	    	boolean gotException = false;
	    	try {
	    		snapshots.com.mentorgen.tools.util.profile.File.doFile(
	    			snapshot.getHost(),
	    			snapshot.getPort(),
	    			snapshot.getPathAndName());
	    	}
	    	catch (IOException ioex) {
	    		gotException = true;
	        	this.event_logger.updateConsoleLog(ioex);
	    	}
			      
	    	// if no exception, ie the above succeeded, then go to start
	    	// command and report file success
	    	if (!gotException) {
	    		this.event_logger.updateForSuccessfulCall(
	    			"file"
	    		);
	    		try {
	    			Start.doStart(snapshot.getHost(),
	    					snapshot.getPort());
	    		}
	    		catch (IOException ioex) {
	    			gotException = true;
	    			this.event_logger.updateConsoleLog(ioex);
	    		}
	    	}
	    	// if no exception, ie the above succeeded, then
	    	//report start success and send event
	    	if (!gotException) {
	    	  	this.event_logger.updateForSuccessfulCall(
	    	  		"start"
	    	  	);
				this.snapshot_event_manager.fireSnapshotEvent(
						new SnapshotEvent(
				            SnapshotEvent.ID_SNAPSHOT_STARTED, 
				            snapshot
				        )
					);
				this.controller.setModelProperty(
						Constants.NAME_PROPERTY, 
						snapshot.getName()
					);
				this.setEnabled(false);
	    	}
	    } 
	}

	private Snapshot 
	getSnapshot() 
	{
		ISnapshotInfoModel info_model
			= Activator.getDefault().getActiveSnapshotModel();
		
	    // check path specified
	    String path = info_model.getSnapshotPath();	    
	    if (path == null || path.trim().length() == 0) {
	    	EventLogEvent event
	    		= this.event_logger.getErrorEvent();
	    	event.addProperty(
	    		Constants.KEY_ERR_MSSG, 
	    		"A folder in which to store the snapshot " 
	    		+ "must be specified."
	    	);
	    	EventLogActionHandler action_handler
	    		= Activator.getDefault().getActionHandler();
	    	action_handler.performActionByKey(
	    		Constants.ACTKEY_ERROR_DISPLAY, 
	    		event
	    	);
	    	return null; 
	    }
	    
	    File pathFile = new File(path);
	    if (!pathFile.exists() || !pathFile.isDirectory()) {
	    	EventLogEvent event
	    		= this.event_logger.getErrorEvent();
	    	event.addProperty(
	    		Constants.KEY_ERR_MSSG,
	    		"The folder ({0}) does not exist."
	    	);
	    	event.addProperty(
	    		Constants.KEY_ERR_VALUES,
	    		new Object[]{ pathFile.getPath() }
	    	);
	    	EventLogActionHandler action_handler
	    		= Activator.getDefault().getActionHandler();
	    	action_handler.performActionByKey(
	    		Constants.ACTKEY_ERROR_DISPLAY,
	    		event
	    	);
	    	
	    	return null; 
	    }
	    
	    if (!pathFile.canWrite() || !pathFile.canRead()) {
	    	EventLogEvent event
	    		= this.event_logger.getErrorEvent();
	    	event.addProperty(
	    		Constants.KEY_ERR_MSSG, 
	    		"The folder ({0}) must have both read and "
	    		+ "write access."
	    	);
	    	event.addProperty(
	    		Constants.KEY_ERR_VALUES, 
	    		new Object[]{pathFile.getPath()}
	    	);
	    	EventLogActionHandler event_handler
	    		= Activator.getDefault().getActionHandler();
	    	event_handler.performActionByKey(
	    		Constants.ACTKEY_ERROR_DISPLAY,
	    		event
	    	);
	    	
	    	return null;
	    }
	    
	    String port 		
	    	= info_model.getSnapshotPort();
	    String host 		
	    	= info_model.getSnapshotHost();
	    final String origName 	
	    	= info_model.getSnapshotName();
	    
	    String newName 				
	    	=  this.setNewName(origName, pathFile);
	   
	    return new Snapshot(
	        pathFile.getPath(),
	        newName,
	        origName,
	        port,
	        host
	    );
	  }
	
	private String 
	setNewName
	(String origName, File pathFile) 
	{
		String newName;
		
		 // modify name if it already exists and is not empty
	    if (origName.length() > 0) {
	      boolean nameExists = true;
	      String tempName = origName;
	      String nameSuffix = ".txt";
	      if (	origName.endsWith(".txt") 
	    		|| origName.endsWith(".xml")) 
	      {
	        tempName 
	        	= origName.substring(0, origName.length() - 4);
	        nameSuffix 
	        	= origName.substring(origName.length() - 4);
	      }
	      StringBuilder buf = new StringBuilder(tempName);
	      int baseLength = buf.length();
	      for (int cnt = 0; nameExists; cnt++) {
	        buf.setLength(baseLength);
	        if (cnt > 0) {
	          buf.append(cnt);
	        }
	        buf.append(nameSuffix);
	        File txtPath = new File(pathFile, buf.toString());
	        nameExists = txtPath.exists();
	      }
	      newName = buf.toString();
	    }
	    // name is empty, create name based on current time
	    // but leave origName empty
	    else {
	      StringBuilder buf = new StringBuilder();
	      buf.append(
	    	new SimpleDateFormat("yyyyMMdd-HHmmss").format(
	    		new Date()
	    	)
	    	);
	      buf.append(".txt");
	      newName = buf.toString();
	    }
	    return newName;
	}
	
	 /* ---------------- from SnapshotEventListener -------- */

	@Override
	public void
	handleSnapshotEvent
	(SnapshotEvent event) 
	{
		switch (event.getId()) {
		    case SnapshotEvent.ID_SNAPSHOT_CAPTURED:
		    case SnapshotEvent.ID_SNAPSHOT_CAPTURE_FAILED:
			    this.setEnabled(true);
			    break;
		}
	}
}
