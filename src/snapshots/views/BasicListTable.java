package snapshots.views;

import java.util.List;



import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import plugin.Constants;

import snapshots.model.Snapshot;

public class 
BasicListTable 
{
	private Table 			table;
	private TableViewer 	table_viewer;
	private List<Object> 	contents;
	private Menu			menu;

	public BasicListTable
	( final Composite parent, String header, ILabelProvider label_provider )
	{
		initializeTable( parent, header );
		initializeTableViewer( header );
		initializeContextMenu();
		
		this.table_viewer.setContentProvider(
			new ArrayContentProvider()
		);
		if(label_provider != null){
			this.table_viewer.setLabelProvider(label_provider);
		}
	}
	
	protected Menu
	getMenu()
	{
		if(this.menu == null){
			this.menu = new Menu(table);
			table.setMenu(menu);
		}
		
		return this.menu;
	}
	
	// meant to be overriden by subclasses
	protected void 
	initializeContextMenu()
	{}
	
	// meant to be overridden by subclasses
	protected void
	setContextMenu(String event)
	{}

	protected Snapshot 
	getSelectedSnapshot() 
	{
		Snapshot snapshot 
			= (Snapshot) table.getSelection()[0].getData();
		
		return snapshot;
	}

	private void 
	initializeTable
	(final Composite parent, String column_name) 
	{
		final int style
			= SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION 
			| SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
	
		this.table 
			= new Table(parent, style);
		
		TableColumn col 
			= new TableColumn( table, SWT.LEFT, 0 );
		col.setText(column_name);
	
		//this.setContentProvider(new ArrayContentProvider());
		GridData grid_data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(grid_data);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		table.addListener(
			SWT.Selection, 
			new Listener(){
				@Override
				public void 
				handleEvent
				(Event event) 
				{ setContextMenu(Constants.SELECTED_ENTRY_PROPERTY); }
			});
		
		// if we want to set the size of each column relative to the parent
		// or the table, we have to wait until the table is drawn or resized
		// to get a value; if we simply call getClientArea() in the constructor
		// we will get a value of 0,0
		parent.addControlListener( 
			new ControlAdapter(){
				public void controlResized
				( ControlEvent e)
				{
					// will need to ask about the correct way to do this
					// and why 6 works
					
					// ask about how one detects when the scroll bar is visible
					// or not
					int width 
						= parent.getClientArea().width - 6*table.getBorderWidth();
					width -= table.getVerticalBar().getSize().x;
					table.getColumn(0).setWidth( width );
				}
			});
	}
	
	private void 
	initializeTableViewer
	(String column_name) 
	{
		this.table_viewer 
			= new TableViewer(this.table);
		this.table_viewer.setColumnProperties(new String[]{column_name});
	}

	public void
	setContents
	(List<Object> contents)
	{
		this.contents = contents;
		this.table_viewer.setInput(this.contents);
	}
	
	public void 
	refresh() 
	{
		this.table_viewer.refresh(false);
	}

	public void 
	addEntry
	(String string) 
	{
		this.contents.add(string);
	}
	
	public void
	setComparator
	( ViewerComparator sorter )
	{
		this.table_viewer.setComparator(sorter);
	}
}
