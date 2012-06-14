import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import snapshots.views.SnapshotView;

public class 
RemoveSnapshotFolder 
extends AbstractHandler 
{
	@Override
	public Object 
	execute
	( ExecutionEvent event ) 
	throws ExecutionException 
	{
		boolean execution_success = false;
		
		if( HandlerUtil.getActivePart(event) instanceof SnapshotView )
		{
			SnapshotView snapshot_view 
				= (SnapshotView) HandlerUtil.getActivePart(event);
			
			// following the example in the eclipse plugins book
			// pp. 324
			ISelection selection 
				= HandlerUtil.getCurrentSelection(event);
			if( selection instanceof IStructuredSelection ){
				Object[] objects 
					= ((IStructuredSelection) selection).toArray();
				if(objects.length > 0){
					System.out.println(objects[0].getClass());
					if(objects[0] instanceof File){
						File folder = (File) objects[0];
						snapshot_view.removeFolder( folder );
						execution_success = true;
					} 
				}
			}
		}
		
		if( !execution_success ){
			throw new ExecutionException(
				"The parent widget is not of type SnapshotView"
			);
		}
		
		return null;
	}
}
