package views;

import model_controllers.Constants;
import model_controllers.Snapshot;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class 
SnapshotListTable
extends BasicListTable
{
	public 
	SnapshotListTable
	( Composite parent, String header, ILabelProvider label_provider) 
	{
		super(parent, header, label_provider);
	}
	
	@Override
	protected void 
	initializeContextMenu() 
	{
		Menu menu = super.getMenu();
		
		final MenuItem remove_snapshot
			= new MenuItem(menu, SWT.NONE);
		remove_snapshot.addSelectionListener(
			new SelectionAdapter() { 
				public void 
				widgetSelected
				(final SelectionEvent e) 
				{ 
					
				} 
			}); 
		remove_snapshot.setText("Remove snapshot"); 
		remove_snapshot.setEnabled(false);
		
		final MenuItem add_snapshot
			= new MenuItem(menu, SWT.NONE);
		add_snapshot.addSelectionListener(
			new SelectionAdapter() { 
				public void 
				widgetSelected
				(final SelectionEvent e) 
				{ 
					
				} 
			}); 
		add_snapshot.setText("Add snapshot from filesystem"); 
		
		final MenuItem clear_snapshots
			= new MenuItem(menu, SWT.NONE);
		clear_snapshots.addSelectionListener(
			new SelectionAdapter() { 
				public void 
				widgetSelected
				(final SelectionEvent e) 
				{ 
					
				} 
			}); 
		clear_snapshots.setText("Clear snapshots"); 
		clear_snapshots.setEnabled(false);
		
		final MenuItem jip_viewer 
		= new MenuItem(menu, SWT.NONE); 
		jip_viewer.addSelectionListener(
			new SelectionAdapter() { 
				public void 
				widgetSelected
				(final SelectionEvent e) 
				{ 
					Snapshot snapshot 
						= getSelectedSnapshot();
					
					Shell shell
						= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openInformation( shell, snapshot.getPath(), "Launch!" );
				} 
			}); 
		jip_viewer.setText("Set selection in JIP Viewer"); 
		jip_viewer.setEnabled(false);
		
		final MenuItem partitioner
			= new MenuItem(menu, SWT.NONE);
		partitioner.addSelectionListener(
			new SelectionAdapter() { 
				public void 
				widgetSelected
				(final SelectionEvent e) 
				{ 
				} 
			}); 
		partitioner.setText("Set selection in Partitioning Analyzer"); 
		partitioner.setEnabled(false);
	}
	
	@Override
	protected void
	setContextMenu
	( String event )
	{
		switch(event){
		case Constants.SELECTED_ENTRY_PROPERTY:
			Menu menu = super.getMenu();
			for( MenuItem mi : menu.getItems()){
				mi.setEnabled(true);
			}
			break;
		default:
			break;
		}
	}
}
