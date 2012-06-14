import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import snapshots.views.SnapshotView;

public class 
ClearFolders 
extends AbstractHandler 
{
	@Override
	public Object 
	execute
	( ExecutionEvent event ) 
	throws ExecutionException 
	{
		if(HandlerUtil.getActivePart(event) instanceof SnapshotView)
		{
			SnapshotView snapshot_view 
				= (SnapshotView) HandlerUtil.getActivePart(event);
			snapshot_view.clearSnapshots();
			snapshot_view.refresh();
		}
		else {
			throw new ExecutionException(
				"The parent widget is not of type SnapshotView"
			);
		}
		
		return null;
	}
}
