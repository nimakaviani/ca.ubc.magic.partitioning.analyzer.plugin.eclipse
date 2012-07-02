package partitioner_configuration.views;

import java.beans.PropertyChangeEvent;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory.InteractionCostType;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory;
import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory.PartitionerType;

import partitioner.views.ModelCreationEditor;
import plugin.Constants;

import snapshots.controller.IController;
import snapshots.views.IView;

public class 
PartitionerConfigurationView 
extends ViewPart
implements IView
{
	private FormToolkit toolkit;
	
	private Section actions_composite;
	private Label 	profiler_trace_text;

	private Text 	module_exposer_text;
	private Text 	host_config_text;
	
	private Button 	mod_exposer_browse_button;
	private Button 	host_config_browse;
	
	private Button 	exposure_button;
	private Button 	synthetic_node_button;
	private Button	perform_partitioning_button;

	private IController controller;
	
	private Combo 	set_coarsener_combo;
	
	private Combo 	partitioning_algorithm_combo;
	private Combo 	interaction_model_combo;
	private Combo 	execution_model_combo;

	private Object controller_lock = new Object();
	
	@Override
	public void 
	createPartControl
	( Composite parent ) 
	{
		// the following code is a view-communication solution
			// found in:
			// http://tomsondev.bestsolution.at/2011/01/03/enhanced-rcp-how-views-can-communicate/
			BundleContext context 
				= FrameworkUtil.getBundle(ModelCreationEditor.class).getBundleContext();
			EventHandler handler 
				= new EventHandler() {
					public void handleEvent
					( final Event event )
					{
						// acceptable alternative, given that we run only
						// one display
						Display display 
							= Display.getDefault();
						assert( display != null) : "Display is null";
						if( display.getThread() == Thread.currentThread() ){
							IController controller 
								= (IController) event.getProperty("ACTIVE_EDITOR");
							PartitionerConfigurationView.this.setValues( controller );
						}
						else {
							display.syncExec( 
								new Runnable() {
									public void 
									run()
									{
										IController controller 
											= (IController) event.getProperty("ACTIVE_EDITOR");
										PartitionerConfigurationView.this.setValues(controller);
									}
								}
							);
						}
					}
				};
				
			Dictionary<String,String> properties 
				= new Hashtable<String, String>();
			properties.put(EventConstants.EVENT_TOPIC, "viewcommunication/*");
			context.registerService(EventHandler.class, handler, properties);
				
		this.toolkit 
			= new FormToolkit( parent.getDisplay() );
				
		ScrolledForm sf 
			= this.toolkit.createScrolledForm(parent);
		
		TableWrapLayout layout 
			= new TableWrapLayout();
		layout.numColumns = 1;
		sf.getBody().setLayout(layout);
		sf.setText("Configure the Model");
		
		Section set_paths_composite
			= toolkit.createSection(
				sf.getBody(),
				Section.TITLE_BAR 
					| Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		set_paths_composite.setText("Set the File Paths");
		set_paths_composite.setDescription(
			"Set the files from which the model shall be built."
		);
		set_paths_composite.setSize( new Point(400,400) );
		
		Composite set_paths_client
			= this.toolkit.createComposite( set_paths_composite, SWT.WRAP);
		this.initializeSetPathsBarGrid( set_paths_client );
		this.initializeSetPathsBarWidgets( set_paths_client, toolkit );
		
		this.toolkit.paintBordersFor(set_paths_client);

		TableWrapData td 
			= new TableWrapData(TableWrapData.FILL);
		set_paths_composite.setLayoutData(td);
		
		set_paths_composite.setClient(set_paths_client);
		
		Section configure_composite
			= toolkit.createSection(
				sf.getBody(),
				Section.TITLE_BAR 
					|Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		configure_composite.setText("Configure");
		configure_composite.setDescription(
			"Configure the settings for the model."
		);
		
		Composite configure_client
			= toolkit.createComposite(configure_composite);
		this.initializeConfigurationGrid(configure_client);
		this.initializeConfigurationWidgets( configure_client, toolkit );
		td 
			= new TableWrapData(TableWrapData.FILL);
		configure_composite.setLayoutData(td);
		configure_composite.setClient(configure_client);
		
		Section partitioning_composite
			= toolkit.createSection(
				sf.getBody(),
				Section.TITLE_BAR 
					|Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		partitioning_composite.setText("Partition");
		partitioning_composite.setDescription(
			"Configure the cost model and the partitioning algorithm."
		);
		
		Composite partitioning_client
			= toolkit.createComposite(partitioning_composite);
		this.initializePartitioningGrid(partitioning_client);
		this.initializePartitioningWidgets( partitioning_client, toolkit );
		td 
			= new TableWrapData(TableWrapData.FILL);
		partitioning_composite.setLayoutData(td);
		partitioning_composite.setClient(partitioning_client);
		
		this.actions_composite
			= toolkit.createSection(
				sf.getBody(),
				Section.TITLE_BAR 
					| Section.EXPANDED 
					| Section.DESCRIPTION
					| Section.TWISTIE
			);
		actions_composite.setText("Activate");
		actions_composite.setDescription(
			"Activate the model"
		);
		
		Composite actions_client
			= toolkit.createComposite(actions_composite);
		td 
			= new TableWrapData(TableWrapData.FILL);
		actions_composite.setLayoutData(td);
		actions_composite.setClient(actions_client);	
		
		this.initializeActionsGrid(actions_client);
		this.initializeActionsWidgets(actions_client, toolkit);
		
		this.set_partitioning_widgets_enabled(false); 
		
	}
	

	private int 
	findIndex
	( Combo set_coarsener_combo, String string ) 
	{
		String[] items
			= set_coarsener_combo.getItems();
		for( int i = 0; i < items.length; ++i ){
			if( items[i].equals(string)){
				return i;
			}
		}
		throw new IllegalArgumentException(
			"The string is not contained in the combo box."
		);
	}
	
	private void 
	initializeSetPathsBarGrid
	( Composite parent ) 
	{
		final GridLayout model_configuration_page_grid_layout
			= new GridLayout();
		model_configuration_page_grid_layout.numColumns = 3;
		parent.setLayout( model_configuration_page_grid_layout );
	}
	
	private void 
	initializeSetPathsBarWidgets
	( Composite parent, FormToolkit toolkit  ) 
	{
		toolkit.createLabel(parent, "Profiler Trace XML: " );
		
		this.profiler_trace_text 
			= toolkit.createLabel( 
				parent, 
				""
			);
		toolkit.createLabel(parent, "");
		
		toolkit.createLabel(parent, "Mod Exposer XML: " );
		this.module_exposer_text 
			= toolkit.createText(parent, "");
		this.module_exposer_text.setEditable( true );
		this.module_exposer_text.setSize( 
			150, 
			this.module_exposer_text.getSize().y
		);
		GridData grid_data 
			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
		
		grid_data.grabExcessHorizontalSpace = true;
		// hack: will need to fix
		grid_data.widthHint = 600;
		this.module_exposer_text.setLayoutData(grid_data);
		
		mod_exposer_browse_button 
			= toolkit.createButton(parent, "Browse", SWT.PUSH);
		mod_exposer_browse_button.addSelectionListener( 
			new SelectionAdapter(){
				public void widgetSelected
				( SelectionEvent event )
				{
					FileDialog file_dialog 
						= new FileDialog( 
							PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(), 
							SWT.OPEN
						);
					file_dialog.setText("Select File");
					file_dialog.setFilterPath( 
						PartitionerConfigurationView.this
							.profiler_trace_text.getText() 
					);
					String selected
						= file_dialog.open();
					if(selected != null){
						PartitionerConfigurationView.this
							.module_exposer_text.setText(selected);
					}
				}
			}
		);
		
		toolkit.createLabel(parent, "Host Config. XML: " );
		this.host_config_text
			=  toolkit.createText(parent,"");
		host_config_text.setEditable( true );
		grid_data 
			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
		
		grid_data.grabExcessHorizontalSpace = true;
		// hack: will need to fix
		grid_data.widthHint = 600;
		host_config_text.setLayoutData(grid_data);
		
		this.host_config_browse 
			= toolkit.createButton(parent, "Browse", SWT.PUSH);
		
		host_config_browse.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected( SelectionEvent event ){
				FileDialog file_dialog 
					= new FileDialog( 
						PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), 
						SWT.OPEN
					);
				file_dialog.setText("Select File");
				file_dialog.setFilterPath( profiler_trace_text.getText() );
				String selected
					= file_dialog.open();
				if(selected != null){
					host_config_text.setText(selected);
				}
			}
		});
	}
	
	private void 
	initializeConfigurationGrid
	( Composite parent ) 
	{
		final GridLayout model_configuration_page_grid_layout
			= new GridLayout();
		model_configuration_page_grid_layout.numColumns 
			= 2;
		parent.setLayout( model_configuration_page_grid_layout );
	}
	
	private void 
	initializeConfigurationWidgets
	( Composite parent, FormToolkit toolkit ) 
	{
		toolkit.createLabel(parent, "Coarsener: " );

		this.initialize_coarsener_combo_box(parent);
		
		this.exposure_button
			= toolkit.createButton(
				parent, 
				"Activate Module Exposure", 
				SWT.CHECK
			);
		GridData grid_data 
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 1;
		exposure_button.setLayoutData(grid_data);
		
		exposure_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_SET_MODULE_EXPOSURE, 
						new Boolean(exposure_button.getSelection())
					);
				}
			}
		);
		
		this.createDummyLabel(parent, toolkit);
		
		this.synthetic_node_button
			= toolkit.createButton(parent, "Add Synthetic Node", SWT.CHECK);
		grid_data 
			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		grid_data.horizontalSpan = 1;
		synthetic_node_button.setLayoutData( grid_data );
		
		synthetic_node_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_SET_SYNTHETIC_NODE,
						new Boolean(synthetic_node_button.getSelection())
					);
				}
			}
		);
	}
	
	private void
	initialize_coarsener_combo_box
	( Composite parent ) 
	{
		this.set_coarsener_combo
			= new Combo(parent, SWT.NONE);
		
        for( final ModuleCoarsenerType mcType 
        		: ModuleCoarsenerFactory.ModuleCoarsenerType.values())
        {
            set_coarsener_combo.add(mcType.getText());
        }
		
		set_coarsener_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_MODULE_COARSENER,
						ModuleCoarsenerType.fromString(
							set_coarsener_combo.getText()
						)
					);
				}
			}
		);
		
		set_coarsener_combo.select(0);
	}
	
	private void 
	initializePartitioningGrid
	( Composite parent ) 
	{
		final GridLayout model_configuration_page_grid_layout
			= new GridLayout();
		model_configuration_page_grid_layout.numColumns 
			= 2;
		parent.setLayout( model_configuration_page_grid_layout );
	}

	private void 
	initializePartitioningWidgets
	( Composite parent, FormToolkit toolkit ) 
	{
		this.perform_partitioning_button
			= toolkit.createButton(
				parent, 
				"Perform Partitioning", 
				SWT.CHECK
			);
		GridData grid_data 
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 1;
		this.perform_partitioning_button.setLayoutData(grid_data);
		
		this.perform_partitioning_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_PERFORM_PARTITIONING, 
						new Boolean(
							PartitionerConfigurationView.this
								.perform_partitioning_button.getSelection()
						)
					);
				}
			}
		);
		
		this.createDummyLabel(parent, toolkit);
	
		toolkit.createLabel(parent, "Execution Cost Model: ");
		this.initialize_execution_model_combo_box(parent);
		
		toolkit.createLabel(parent, "Interaction Cost Model: ");
		this.initialize_interaction_model_combo_box(parent);
		
		toolkit.createLabel(parent, "Partitioning Algorithm");
		this.initilize_partitioning_algorithm_combo_box(parent);
	}
	
	private void 
	initilize_partitioning_algorithm_combo_box
	( Composite parent ) 
	{
		this.partitioning_algorithm_combo
			= new Combo(parent, SWT.NONE);
		
	    for( final PartitionerType partitioner_type 
	    		: PartitionerFactory.PartitionerType.values())
	    {
	    	this.partitioning_algorithm_combo.add(
	    		partitioner_type.getText()
	    	);
	    }
		
	    this.partitioning_algorithm_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					System.out.println("Selected partitioner");
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_PARTITIONER_TYPE,
						PartitionerType.fromString(
							PartitionerConfigurationView.this.
								partitioning_algorithm_combo.getText()
						)
					);
				}
			}
		);
		
	    this.partitioning_algorithm_combo.select(0);
	}
	
	private void 
	initialize_interaction_model_combo_box
	( Composite parent ) 
	{
		this.interaction_model_combo
			= new Combo(parent, SWT.NONE);
		
	    for( final InteractionCostType interaction_cost_type 
	    		: InteractionFactory.InteractionCostType.values())
	    {
	    	this.interaction_model_combo.add(
	    		interaction_cost_type.getText()
	    	);
	    }
		
	    this.interaction_model_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_INTERACTION_COST,
						InteractionCostType.fromString(
							PartitionerConfigurationView.this
								.interaction_model_combo.getText()
						)
					);
				}
			}
		);
	
	    this.interaction_model_combo.select(0);
	}
	
	private void 
	initialize_execution_model_combo_box
	( Composite parent ) 
	{
		this.execution_model_combo
			= new Combo(parent, SWT.NONE);
		
	    for( final ExecutionCostType execution_cost_type 
	    		: ExecutionCostType.values())
	    {
	    	this.execution_model_combo.add(
	    		execution_cost_type.getText()
	    	);
	    }
		
	    this.execution_model_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_EXECUTION_COST,
						ExecutionCostType.fromString(
							PartitionerConfigurationView.this
								.execution_model_combo.getText()
						)
					);
				}
			}
		);
	
	    this.execution_model_combo.select(0);
	}
	
	
	private void 
	initializeActionsGrid
	( Composite parent ) 
	{
		final GridLayout model_configuration_page_grid_layout
			= new GridLayout();
		model_configuration_page_grid_layout.numColumns 
			= 2;
		parent.setLayout( model_configuration_page_grid_layout );
	}
	
	private void 
	initializeActionsWidgets
	( Composite parent, FormToolkit toolkit ) 
	{
		final Button exposure_button
			= toolkit.createButton(
				parent, 
				"Generate Model", 
				SWT.PUSH
			);
		GridData grid_data 
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false);
		grid_data.horizontalSpan = 1;
		exposure_button.setLayoutData( grid_data );
		
		exposure_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_HOST_CONFIGURATION, 
						PartitionerConfigurationView.this.host_config_text.getText()
					);
					PartitionerConfigurationView.this.controller.setModelProperty(
						Constants.GUI_MODULE_EXPOSER, 
						PartitionerConfigurationView.this.module_exposer_text.getText()
					);
					
					PartitionerConfigurationView.this.setVisualizationAction();
				}
			}
		);
	}
	
	public void
	setVisualizationAction()
	{
		this.controller.notifyModel(
			Constants.GENERATE_MODEL_EVENT
		);
	}
	
	void
	set_partitioning_widgets_enabled
	( boolean enabled )
	{
		this
			.partitioning_algorithm_combo.setEnabled(enabled);
		this.execution_model_combo.setEnabled(enabled);
		this.interaction_model_combo.setEnabled(enabled);
	}
	
	private Label 
	createDummyLabel
	( Composite parent, FormToolkit toolkit ) 
	{
		Label return_value = null;
		
		if( toolkit != null ){
			return_value
				= toolkit.createLabel(parent, "", SWT.NONE);
			GridData grid_data 
				= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
			grid_data.horizontalSpan = 1;
			return_value.setLayoutData( grid_data );
		}
		
		return return_value;
	}
	
	@Override
	public void 
	modelPropertyChange
	( PropertyChangeEvent evt ) 
	{
		switch(evt.getPropertyName())
		{
		// TODO: for every change in the input, we have to trigger
		// 	a change in the model: this will ensure persistence;
		// 	we also need to move the handlers from model events into here
		case Constants.GUI_MODULE_COARSENER:
			ModuleCoarsenerType mc 
				= (ModuleCoarsenerType) evt.getNewValue();
			System.out.println(
				"The module coarsener was modified to " + mc.getText()
			);
			break;
		
		case Constants.GUI_PROFILER_TRACE:
			this.setProfilerTracePath(
				(String) evt.getNewValue()
			);
			break;
		case Constants.GUI_MODULE_EXPOSER:
			this.setModuleExposerPath(
				(String) evt.getNewValue()
			);
			break;
		case Constants.GUI_HOST_CONFIGURATION:
			this.setHostConfigurationPath(
				(String) evt.getNewValue()
			);
			break; 
		case Constants.GUI_PERFORM_PARTITIONING:
			{
				Boolean enabled 
					= (Boolean) evt.getNewValue();
				System.out.println("The paritioner is " + enabled.toString() );
				this.set_partitioning_widgets_enabled( enabled );
			}
			break; 
		case Constants.MODEL_CREATION_AND_ACTIVE_CONFIGURATION_PANEL:
			boolean enabled
				= (boolean) evt.getNewValue();
			this.set_configuration_widgets_enabled( enabled );
			break;
		default:
			System.out.println("Swallowing message.");
		};
	}

	@Override
	public void 
	setFocus() 
	{
		
	}
	
	protected void 
	setConfigurationWidgetsEnabled
	( Boolean enabled ) 
	{
		PartitionerConfigurationView.this.host_config_text.setEditable( enabled );
		PartitionerConfigurationView.this.module_exposer_text.setEditable(enabled);
		
		PartitionerConfigurationView.this.actions_composite.setVisible(enabled);
		
		PartitionerConfigurationView.this.synthetic_node_button.setEnabled(enabled);
		PartitionerConfigurationView.this.exposure_button.setEnabled(enabled);
		
		PartitionerConfigurationView.this.mod_exposer_browse_button.setVisible(enabled);
		PartitionerConfigurationView.this.host_config_browse.setVisible(enabled);
		PartitionerConfigurationView.this.set_coarsener_combo.setEnabled(enabled);
		PartitionerConfigurationView.this.perform_partitioning_button.setEnabled(enabled);
	}

	protected void 
	setValues
	( IController controller ) 
	// this function should only be called from swt thread,
	// again though, we want to be concerned about threading
	// issues
	//
	// problem: whenever we change anything we have to send a message
	// to update the model; this basically means we need to controller
	{
		synchronized(this.controller_lock){
			assert controller != null : "The controller argument should not be null";
			
			if(this.controller != null){
				this.controller.removeView(this);
				// this will need to be controlled by a lock
			}
			this.controller = controller;
			this.controller.addView(this);
			
			String[] args 
				= new String[]{
				Constants.GUI_PROFILER_TRACE,
				Constants.GUI_MODULE_EXPOSER,
				Constants.GUI_HOST_CONFIGURATION,
				Constants.GUI_MODULE_COARSENER,
				Constants.GUI_SET_MODULE_EXPOSURE,
				Constants.GUI_SET_SYNTHETIC_NODE,
				Constants.GUI_PERFORM_PARTITIONING,
				Constants.GUI_EXECUTION_COST,
				Constants.GUI_INTERACTION_COST,
				Constants.GUI_PARTITIONER_TYPE,
				Constants.MODEL_CREATION_AND_ACTIVE_CONFIGURATION_PANEL
			};
			
			Object[] properties 
				= this.controller.requestProperties(args);
			
			//System.err.println("Profiler trace: " + (String) properties[0]);
			this.profiler_trace_text.setText( (String) properties[0]);
			this.profiler_trace_text.setSize(400, this.profiler_trace_text.getSize().y);
			//System.out.println("x : " + this.profiler_trace_text.getSize().x +
			//		"y " + this.profiler_trace_text.getSize().y );
			//System.out.println("From label: " + this.profiler_trace_text.getText());
			this.module_exposer_text.setText( (String) properties[1]);
			this.host_config_text.setText( (String) properties[2]);
			
			int index 
				= this.findIndex(
					this.set_coarsener_combo, 
					((ModuleCoarsenerType) properties[3]).getText()
				);
			this.set_coarsener_combo.select( index);
			//System.out.println( "exposure_button" + properties[4].toString());
			this.exposure_button.setSelection( (Boolean) properties[4]);
			//System.out.println( "synthetic_node_button" + properties[5].toString());
			this.synthetic_node_button.setSelection( (Boolean) properties[5]);
			
			this.perform_partitioning_button.setSelection( (Boolean) properties[6]);
			//System.err.println(((ExecutionCostType) properties[7]).getText());
			index 
				= this.findIndex(
					this.execution_model_combo, 
					((ExecutionCostType) properties[7]).getText()
				);
			this.execution_model_combo.select( index );
			index 
				= this.findIndex(
					this.interaction_model_combo, 
					((InteractionCostType) properties[8]).getText()
				);
			this.interaction_model_combo.select( index );
			index 
				= this.findIndex(
					this.partitioning_algorithm_combo, 
					((PartitionerType) properties[9]).getText()
				);
			
			this.partitioning_algorithm_combo.select( index ); 
			this.setConfigurationWidgetsEnabled((Boolean) properties[10]);
			
		}
	}
	
	public void
	setProfilerTracePath
	( String path )
	{
		this.profiler_trace_text.setText( path );
	}
	
	public void 
	setModuleExposerPath
	( String new_value ) 
	{
		this.module_exposer_text.setText( new_value );
	}
	
	public void
	setHostConfigurationPath
	( String path )
	{
		this.host_config_text.setText( path );
	}
	
	public void 
	set_configuration_widgets_enabled
	( boolean enabled ) 
	{
		Display.getDefault().asyncExec( 
			new Runnable(){
				@Override
				public void run() 
				{
					PartitionerConfigurationView.this.host_config_text.setEditable(false);
					PartitionerConfigurationView.this.module_exposer_text.setEditable(false);
					
					PartitionerConfigurationView.this.actions_composite.setVisible(false);
					
					PartitionerConfigurationView.this.synthetic_node_button.setEnabled(false);
					PartitionerConfigurationView.this.exposure_button.setEnabled(false);
					
					PartitionerConfigurationView.this.mod_exposer_browse_button.setVisible(false);
					PartitionerConfigurationView.this.host_config_browse.setVisible(false);
					PartitionerConfigurationView.this.set_coarsener_combo.setEnabled(false);
					
					PartitionerConfigurationView.this
						.perform_partitioning_button.setEnabled(false);
					PartitionerConfigurationView.this.set_partitioning_widgets_enabled(false);
				}
			});
	}

}