import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import partitioner.views.ModelCreationEditor;

public class 
ModifySimulation 
extends AbstractHandler 
{

	@Override
	public Object 
	execute
	( ExecutionEvent event )
	throws ExecutionException 
	{
		if(HandlerUtil.getActivePart( event ) instanceof ModelCreationEditor){
			ModelCreationEditor model_creation_editor 
				= (ModelCreationEditor) HandlerUtil.getActivePart(event);
			ISelection selection 
				= HandlerUtil.getCurrentSelection(event);
			if( selection instanceof IStructuredSelection ){
				IStructuredSelection row_selection
					= (IStructuredSelection) selection;
				Object[] obj
					= (Object[]) row_selection.getFirstElement();
				if( obj != null ){
					for( int i = 0; i < obj.length; ++i ) {
						System.out.println( obj[i] );
					}
					
					// call function to open a new customization dialog
					// given that we have the index
					model_creation_editor.simTableMouseClicked( (Integer) obj[0] );
				}
				else {
					throw new RuntimeException("Nothing has been selected");
				}
			}
		}
		else {
			throw new RuntimeException(
				"The selected part is of the wrong type: " 
				+ HandlerUtil.getActivePart( event ).getClass().toString() 
			);
		}
		
		return event;
	}
}
