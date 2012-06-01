package actions.menu;

import jipplugin.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class 
PartitionerViewerAction 
extends Action
{
	public
	PartitionerViewerAction()
	{
		this.setToolTipText(
			"Select snapshot to run from file system."
		);
		this.setText("Select snapshot for Partitioner");
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
		String base_path 
			= Activator.getDefault().getActiveSnapshotModel().
					getSnapshotPath();
		file_dialog.setFilterPath( base_path );
		String selected = file_dialog.open();
		if(selected != null){
			System.err.println("Selected Partitioner: " + selected);
		}
		// Take the selected entry and use it as the active
		// partitioner entry
	}
}
