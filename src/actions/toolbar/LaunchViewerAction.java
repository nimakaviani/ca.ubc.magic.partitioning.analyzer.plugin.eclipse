package actions.toolbar;

import jipplugin.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class 
LaunchViewerAction 
extends Action 
{
	public
	LaunchViewerAction()
	{
		this.setToolTipText("Launch JIP Viewer in order to....");
		this.setImageDescriptor(
			Activator.getImageDescriptor("icons/image_obj.gif")
		);
	}
	
	@Override
	public void 
	run()
	{
		Shell shell
			= PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation( 
			shell, "First plug-in", "Launch!" 
		);
		this.setImageDescriptor(
			Activator.getImageDescriptor(
				"icons/disconnect_co.gif"
			)
		);
	}
}
