package snapshots.model;

import plugin.mvc.EventTypes.PropertyEvent;
import plugin.mvc.EventTypes.ViewsEvent;

public class 
SnapshotModelMessages 
{
	public static final ViewsEvent SNAPSHOT_CAPTURED
		= new ViewsEvent("snapshot_captured_message", null);
	public static final ViewsEvent SNAPSHOT_CAPTURE_FAILED
		= new ViewsEvent("snapshot_failed_message", null);
	public static final ViewsEvent SNAPSHOT_STARTED
		= new ViewsEvent("snapshot_started_message", Snapshot.class);
	
	public static final PropertyEvent PATH
		= new PropertyEvent("SnapshotPath", String.class );
	public static final PropertyEvent NAME
		= new PropertyEvent("SnapshotName", String.class);
	public static final PropertyEvent PORT
		= new PropertyEvent("SnapshotPort", String.class);
	public static final PropertyEvent HOST
		= new PropertyEvent( "SnapshotHost", String.class);
}
