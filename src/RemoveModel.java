import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import snapshots.views.SnapshotView;
import snapshots.views.VirtualModelFileInput;


public class 
RemoveModel 
extends AbstractHandler 
{
	@Override
	public Object 
	execute
	( ExecutionEvent event ) 
	throws ExecutionException 
	{
		boolean execution_success = false;
		
		if( HandlerUtil.getActivePart(event) instanceof SnapshotView ) {
			SnapshotView snapshot_view 
				= (SnapshotView) HandlerUtil.getActivePart(event);
			ISelection selection 
				= HandlerUtil.getCurrentSelection(event);
			if( selection instanceof IStructuredSelection ){
				Object[] objects 
					= ((IStructuredSelection) selection).toArray();
				if(objects.length > 0){
					System.out.println(objects[0].getClass());
					if(objects[0] instanceof VirtualModelFileInput){
						VirtualModelFileInput model 
							= (VirtualModelFileInput) objects[0];
						snapshot_view.removeModel( model );
						snapshot_view.refresh();
						
						IWorkbenchPage editor_page 
							= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						
						IEditorReference[] editor_references	
							= editor_page.getEditorReferences();
						for( IEditorReference ref : editor_references ){
							try {
								if(ref.getEditorInput() == model ){
									editor_page.closeEditor(ref.getEditor(false), false);
									// one option: get all editors and find the one with the 
									// right tooltip
									execution_success 
										= true;
									break;
								}
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
					} 
				}
			}
	
			if( !execution_success ){
				throw new ExecutionException(
					"The parent widget is not of type SnapshotView"
				);
			}
			
		}
		
		return null;
	}

}
