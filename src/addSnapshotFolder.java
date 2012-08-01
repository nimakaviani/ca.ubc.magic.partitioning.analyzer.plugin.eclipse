import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import snapshots.views.SnapshotView;

public class 
addSnapshotFolder 
extends AbstractHandler 
{

	@Override
	public Object 
	execute
	( ExecutionEvent event ) 
	throws ExecutionException 
	{
		DirectoryDialog file_dialog 
			= new DirectoryDialog( 
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				SWT.OPEN
			);
		file_dialog.setText("Select Directory");
		String path = file_dialog.open();
			
		if(HandlerUtil.getActivePart(event) instanceof SnapshotView)
		{
			SnapshotView snapshot_view 
				= (SnapshotView) HandlerUtil.getActivePart(event);
			if(path != null){
				snapshot_view.addFolder(path);
			}
		}
		else {
			throw new ExecutionException(
				"The parent widget is not of type SnapshotView"
			);
		}
		
		return null;
	}
}
