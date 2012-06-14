package snapshots.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class 
EventLogTable 
extends BasicListTable 
{
	public 
	EventLogTable
	(Composite parent, String header, ILabelProvider label_provider) 
	{
		super(parent, header, label_provider);
	}
	
	@Override
	protected void
	initializeContextMenu()
	{
		Menu menu = super.getMenu();
		
		final MenuItem clear_log
			= new MenuItem(menu, SWT.NONE);
		clear_log.setText("Clear log"); 
	}
}
