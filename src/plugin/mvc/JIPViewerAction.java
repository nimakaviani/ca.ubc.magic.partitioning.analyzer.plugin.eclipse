package plugin.mvc;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class 
JIPViewerAction 
extends Action
{
	public
	JIPViewerAction()
	{
		this.setToolTipText(
			"Select snapshot to run from file system."
		);
		this.setText("Select snapshot for JIP Viewer");
	}
	
	@Override
	public void 
	run()
	{
		Shell shell
			= PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell();
		
		FileDialog file_dialog 
			= new FileDialog( shell, SWT.OPEN );
		file_dialog.setText("Select Snapshot");
		
		/*
		String base_path 
			= Activator.getDefault().getActiveSnapshotModel().
					getSnapshotPath();
		file_dialog.setFilterPath( base_path );
		String selected = file_dialog.open();
		if(selected != null){
			System.err.println("Selected JIP: " + selected);
		}
		// launch JIP Viewer with snapshot selected*/
	}
}
