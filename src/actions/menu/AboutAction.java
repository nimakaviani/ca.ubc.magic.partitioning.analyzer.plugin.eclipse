package actions.menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AboutAction 
extends Action 
{
	public
	AboutAction()
	{
		this.setToolTipText(
			"Disconnect from application to produce snapshot."
		);
		this.setText("About");
	}
	
	@Override
	public void 
	run()
	{
		Shell shell
			= PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell();
		
		MessageDialog.openInformation( 
			shell, 
			"First plug-in", 
			"About!" 
		);
	}
}
