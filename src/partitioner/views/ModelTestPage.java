package partitioner.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import plugin.Constants;
import snapshots.controller.IController;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;

public class 
ModelTestPage 
extends ScrolledForm
{
	private Combo 					simulation_type_combo;
	
	private Label 					best_run_result_label;
	private Label 					best_run_algorithm_label;
	private Label 					best_run_cost_label;
	private Label 					total_simulation_units;

	private IController 			controller;

	private Composite 				root_parent;

	private Section 				table_composite;

	private Section 				control_composite;
	
	public ModelTestPage
	( FormToolkit toolkit, Composite parent, IController controller ) 
	{
		super( parent );
		
		this.root_parent
			= parent;
		
		this.controller
			= controller;
		this.setText(
			"Test and Simulation Framework"
		);
		
		TableWrapLayout layout 
			= new TableWrapLayout();
		layout.numColumns = 1;
		this.getBody().setLayout(layout);
		
		this.control_composite
			= toolkit.createSection(
				this.getBody(),
				Section.TITLE_BAR 
					| Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		this.control_composite.setText("Run a Simulation");
				
		Composite control_client
			= toolkit.createComposite( this.control_composite );

		this.initializeControlGrid( control_client );
		this.initializeControlWidgets( control_client, toolkit );
		
		this.control_composite.setClient(control_client);
	
		this.table_composite
			= toolkit.createSection(
					this.getBody(),
					Section.TITLE_BAR 
						| Section.EXPANDED 
						| Section.DESCRIPTION 
						| Section.TWISTIE
				);
		this.table_composite.setText("View Simulations");
				
		Composite table_client
			= toolkit.createComposite(this.table_composite);
		this.initializeTableClientGrid(table_client);
		this.initializeTableClientWidgets( table_client );
		
		this.table_composite.setClient(table_client);
	}
	
	private void 
	initializeControlGrid
	( Composite parent ) 
	{
		final GridLayout test_framework_page_grid_layout
			= new GridLayout();
		test_framework_page_grid_layout.numColumns 
			= 3;
		parent.setLayout( test_framework_page_grid_layout );
	}
	
	private void 
	initializeControlWidgets
	( Composite parent, FormToolkit toolkit ) 
	{
		// we do not add a layoutdata to the Label since that screws
		// up the vertical spacing
		toolkit.createLabel(parent, "Simulation Type: " );
		
		this.simulation_type_combo
			= new Combo(parent, SWT.NONE);
		this.initialize_simulation_types_combo(
			parent, 
			this.simulation_type_combo
		);
		
		Button run_simulation_button
			= toolkit.createButton(
				parent, 
				"Run", 
				SWT.PUSH
			);
		GridData grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 1;
		run_simulation_button.setLayoutData(grid_data);
		
		run_simulation_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					// call the model and ask them to perform simulation
					// functionality; worry about displaying the table later
				}
			}
		);
		
		toolkit.createLabel(parent, "Best Run Name: " );
		this.best_run_result_label
			= toolkit.createLabel(parent, "" );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_result_label.setLayoutData(grid_data);
		
		toolkit.createLabel(parent, "Best Run Algorithm: " );
		this.best_run_algorithm_label
			= toolkit.createLabel(parent, "" );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_algorithm_label.setLayoutData(grid_data);
		
		toolkit.createLabel(parent, "Best Run Cost: " );
		this.best_run_cost_label
			= toolkit.createLabel(parent, "" );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_cost_label.setLayoutData(grid_data);
	
		toolkit.createLabel(parent, "Total Number: " );
		this.total_simulation_units
			= toolkit.createLabel(parent, "" );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.total_simulation_units.setLayoutData(grid_data);
	}
	
	private void 
	initializeTableClientGrid
	( Composite parent ) 
	{
		final GridLayout test_framework_page_grid_layout
			= new GridLayout();
		test_framework_page_grid_layout.numColumns 
			= 1;
		parent.setLayout( test_framework_page_grid_layout );
	}
	
	private void 
	initializeTableClientWidgets
	( final Composite parent ) 
	{
		final int style
			= SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION 
			| SWT.BORDER | SWT.FULL_SELECTION ;
		
		final Table table	
		 	= new Table( parent, style );
		final String[] column_names
			= new String[] {
				"ID",
				"Name",
				"Algorithm",
				"Execution Cost",
				"Communication Cost",
				"Total Cost",
			};
		
		for(int i = 0; i < column_names.length; ++i ){
			TableColumn col 
				= new TableColumn( table, SWT.NONE, i );
			col.setText( column_names[i]);
			col.setWidth( 780/column_names.length );
		}
		TableViewer simulations_table
			= new TableViewer(table);
		simulations_table.setColumnProperties(column_names);
		GridData grid_data 
			= new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(grid_data);
		
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		// if we want to set the size of each column relative to the parent
		// or the table, we have to wait until the table is drawn or resized
		// to get a value; if we simply call getClientArea() in the constructor
		// we will get a value of 0,0
		this.root_parent.addControlListener( 
			new ControlAdapter(){
				public void controlResized
				( ControlEvent e)
				{
					// will need to ask about the correct way to do this
					// and why 6 works
					
					// ask about how one detects when the scroll bar is visible
					// or not
					int original_width 
						= ModelTestPage.this
							.root_parent.getClientArea().width 
						- 6*table.getBorderWidth();
					if(original_width < 800){
						int reduced_width 
							= original_width - table.getVerticalBar().getSize().x;
						
						for(int i = 0; i < column_names.length; ++i){
							table.getColumn(i).setWidth( 
								reduced_width/ column_names.length
							); 
						}
							
						table.setSize( reduced_width, table.getSize().y );
						ModelTestPage.this.table_composite.setSize(
							original_width,
							ModelTestPage.this.table_composite.getSize().y
						);
						
						ModelTestPage.this.control_composite.setSize(
							original_width,
							ModelTestPage.this.control_composite.getSize().y
						);
						ModelTestPage.this.setSize(
							original_width,
							ModelTestPage.this.getSize().y
						);
					}
				}
			});
	}

	private void
	initialize_simulation_types_combo
	( Composite parent, final Combo simulation_type_combo ) 
	{
		for( SimulatorType type : SimulatorFactory.SimulatorType.values()){
            simulation_type_combo.add(type.getText());
        }
	
	    simulation_type_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					ModelTestPage.this.controller.setModelProperty(
						Constants.GUI_SIMULATION_TYPE,
						SimulatorType.fromString(
							simulation_type_combo.getText()
						)
					);
				}
			}
		);
	
	    simulation_type_combo.select(0);
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	///	Logic for populating the table
	///		1) First: functionality for adding a new entry
	//////////////////////////////////////////////////////////////////////////////////
}
