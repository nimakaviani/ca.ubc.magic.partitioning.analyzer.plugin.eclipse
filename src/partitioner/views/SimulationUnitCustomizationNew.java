package partitioner.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.simulator.framework.SimulationFramework;
import ca.ubc.magic.profiler.simulator.framework.SimulationFrameworkHelper;
import ca.ubc.magic.profiler.simulator.framework.SimulationUnit;

public class 
SimulationUnitCustomizationNew 
extends Dialog
{
	private SimulationUnit 		mTemplate;
    private SimulationFramework mSimFramework;
    private TableViewer			simulation_table_viewer;
    private WritableList		table_input;
	    
	public 
	SimulationUnitCustomizationNew
	( SimulationFramework simulation_framework, SimulationUnit template )
	{
		super( 
			PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell()
		);
		
		this.mSimFramework
			= simulation_framework;
		this.mTemplate
			= new SimulationUnit(template);
	}
	
	@Override
	protected Control 
	createDialogArea
	( Composite parent )
	{
		parent.setSize(new Point(500,700));
		
		GridLayout grid
			= new GridLayout();
		grid.numColumns = 1;
		parent.setLayout(grid);
		
		
		List<Object[]> obj 
			= new ArrayList<Object[]>();
		this.table_input 
			= new WritableList( obj, Object[].class);
		
		final int style
			= SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION 
			| SWT.BORDER | SWT.FULL_SELECTION ;
		
		final Table table	
		 	= new Table( parent, style );
		
		this.simulation_table_viewer
			= new TableViewer(table);
		
		this.simulation_table_viewer.setContentProvider(
			new ObservableListContentProvider()
		);
		
		final String[] column_names
			= new String[] {
				"ID",
				"Module Name",
				"Module Partition",
			};
		final int[] column_widths
			= new int[] {
				30,
				700,
				110
			};
		
		for(int i = 0; i < column_names.length; ++i )
		{
			final int index = i;
			TableViewerColumn table_viewer_column 
				= new TableViewerColumn( 
					this.simulation_table_viewer, 
					SWT.NONE 
				);
			TableColumn col 
				= table_viewer_column.getColumn();
			
			col.setText( column_names[i]);
			col.setWidth( column_widths[i] );
			table_viewer_column.setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String 
					getText
					( Object element )
					{
						Object[] array 
							= (Object[]) element;
						return array[index].toString();
					}
				}
			);
		}
		this.simulation_table_viewer.setInput( this.table_input );
		
		GridData grid_data 
			= new GridData( SWT.FILL, SWT.FILL, true, true );
		table.setLayoutData(grid_data);
		
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		// example taken from
		// http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet88.java?view=co
		final TableEditor editor
			= new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 100;
		final int EDITABLECOLUMN = 2;
		
		table.addSelectionListener(new SelectionAdapter(){
			@Override
			public void
			widgetSelected( SelectionEvent e){
				Control oldEditor 
					= editor.getEditor();
				if( oldEditor != null){
					oldEditor.dispose();
				}
				
				TableItem item
					= (TableItem) e.item;
				
				if(item == null) {
					return;
				}
				
				Text newEditor
					= new Text(table, SWT.NONE);
				newEditor.setText( item.getText(EDITABLECOLUMN));
				newEditor.addModifyListener( 
					new ModifyListener() {
						@Override
						public void
						modifyText
						( ModifyEvent me )
						{
							Text text = (Text) editor.getEditor();
							editor.getItem().setText(EDITABLECOLUMN, text.getText() );
						}
					}
				);
				
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
			}
		});
	
		generateModuleRows();
		 
		return parent;
	}
	
	@Override
	protected void
	okPressed()
	{
		StringBuilder builder = new StringBuilder();
		// iterate over every row in the table viewer
		// build string
		TableItem[] table_items
			= this.simulation_table_viewer.getTable().getItems();
		for( TableItem item : table_items){
			Integer partitionId
				= Integer.parseInt(item.getText(2));
			 if (partitionId == -1){
	             builder.append(Constants.DONT_CARE_CHAR);
			 }
	         else {
	        	 builder.append(Integer.toString(partitionId));
	         }
		}

        SimulationUnit unit 
        	= SimulationFrameworkHelper.getUnitFromSig(
        		builder.toString(), 
        		mTemplate, 
        		Boolean.FALSE
        	);
        mSimFramework.addUnit(unit, Boolean.FALSE);
        
        this.close();
        
        // now we have a problem: how does updating the mSimFramework 
        // notify the parent's table to update the display
	}
	
	private void 
	generateModuleRows()
	{
		int id = 0;
        for (Module m : mTemplate.getDistModel().getModuleMap().values()){
        	this.table_input.add(
            	new Object[]{
        			id++, 
        			m.getName(), 
        			m.getPartitionId()}
        	);
        }
        this.simulation_table_viewer.refresh();
	};
	
	@Override
	protected void
	configureShell
	( Shell shell )
	{
		super.configureShell( shell );
		
		this.setShellStyle(
			SWT.Close | SWT.TITLE | SWT.BORDER 
			| SWT.APPLICATION_MODAL | SWT.RESIZE
		);
		
		// the following solution was found in 
		// http://sureshkrishna.wordpress.com/2007/09/05/
		// 		make-your-eclipse-dialogs-and-messages-to-center-of-screen/
		Point size 
			= shell.computeSize(-1, -1);
		Display display
			= PlatformUI.getWorkbench().getDisplay();
		
		Rectangle screen = display.getMonitors()[0].getBounds();
		shell.setBounds( 
			(screen.width-size.x)/2, 
			(screen.height-size.y)/2, 
			900,800
		);
	}
}
