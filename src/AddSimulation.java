import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import partitioner.views.ModelCreationEditor;

public class 
AddSimulation 
extends AbstractHandler 
{
	@Override
	public Object 
	execute
	( ExecutionEvent event ) 
	throws ExecutionException 
	{
		HandlerUtil.getActiveWorkbenchWindow(event).getShell();
	    IWorkbenchPage page 
	    	= HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
	    IEditorPart editor_part
	    	= page.getActiveEditor();
	    System.err.println(editor_part.toString());
		
		if( editor_part instanceof ModelCreationEditor)
		{
			ModelCreationEditor model_creation_editor 
				= (ModelCreationEditor) HandlerUtil.getActivePart(event);
			
			model_creation_editor.addSimulation();
		}
		else {
			throw new ExecutionException(
				"The parent widget is not of type ModelTestPage"
			);
		}
		
		return null;
	}

}
