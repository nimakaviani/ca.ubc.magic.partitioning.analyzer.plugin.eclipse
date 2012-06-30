package partitioner.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.MultiPageEditorPart;

import partitioner.models.PartitionerGUIStateModel;
import plugin.Constants;
import snapshots.controller.ControllerDelegate;
import snapshots.views.IView;

import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory
	.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.view.VisualizePartitioning;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory;
import ca.ubc.magic.profiler.simulator.control.SimulatorFactory.SimulatorType;

// TODO: problem: the controller is responsible for deciding how
//		events are handled, but there is view code that directly calls
//		the model. This is not such a problem, since the most important
//		thing is to to have a model that supports multiple views...
//		but it is annoying...get rid of it.
// 		-> reconsideration: this is a major problem and interferes with
//		the use of a controller as a communication mechanism between views
// TODO: replace all initializeGrid functions with a function that
//		takes the number of desired columns
public class 
ModelCreationEditor 
extends MultiPageEditorPart 
implements IView
{
	private static final int MODEL_CONFIGURATION_PAGE 	
		= 0;
	private static final int MODEL_ANALYSIS_PAGE 		
		= 1;
	
	private FormToolkit toolkit;
	
    VisualizePartitioning currentVP;
    Object current_vp_lock 
    	= new Object();
    
    private Frame 				frame;
	private ControllerDelegate 	controller;
	
//	final private PartitionerGUIStateModel partitioner_gui_state_model
//		= new PartitionerGUIStateModel();
	
	private Combo simulation_type_combo;
	private Label best_run_result_label;
	private Label best_run_algorithm_label;
	private Label best_run_cost_label;
	private Label total_simulation_units;
	private ModelConfigurationPage model_configuration_page;

	public 
	ModelCreationEditor() 
	{}

	@Override
	public void
	doSave
	( IProgressMonitor monitor ) 
	{}

	@Override
	public void 
	doSaveAs() 
	{}

	@Override
	public void
	init
	( IEditorSite site, IEditorInput input )
	throws PartInitException 
	{
		super.init(site, input);
	}

	@Override
	public boolean 
	isDirty() 
	{
		return false;
	}

	@Override
	public boolean 
	isSaveAsAllowed() 
	{
		return false;
	}

	@Override
	public void 
	setFocus() 
	// according to the plugins book, the setFocus method
	// should have the following form
	// 
	// this method shows why the primary widgets on each
	// page need to be class fields
	{
		switch(super.getActivePage())
		{
		case MODEL_CONFIGURATION_PAGE:
			//his.inner_composite.setFocus();
			break;
		case MODEL_ANALYSIS_PAGE:
			//this.tv2.setFocus();
			break;
		}
	}

	@Override
	protected void 
	createPages() 
	{
		Composite parent 
			= ModelCreationEditor.super.getContainer();
		this.toolkit 
			= new FormToolkit( parent.getDisplay() );
		
		Composite model_analysis_composite 
			= new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		model_analysis_composite.setLayout(new FillLayout());
		
		try {
			this.setSwingLookAndFeel();
		} catch( InvocationTargetException | InterruptedException e ) {
			e.printStackTrace();
		}
		
	    this.frame 
	    	= SWT_AWT.new_Frame(model_analysis_composite);
    
	    PartitionerGUIStateModel partitioner_gui_state_model
	    	= new PartitionerGUIStateModel();
	    
	    this.controller = new ControllerDelegate();
		this.controller.addView(this);
		
		this.model_configuration_page
			= new ModelConfigurationPage( 
				parent, 
				this.toolkit, 
				partitioner_gui_state_model,
				this.controller,
				this.currentVP,
				this.current_vp_lock,
				this.frame,
				this
			);
		
		this.toolkit.adapt(this.model_configuration_page);
		
		int index
			= super.addPage( this.model_configuration_page );
		super.setPageText( index, "Model Configuration" );
		
		this.createModelAnalysisPage( model_analysis_composite );
		this.createModelTestPage();
		this.updateTitle();
	}

	private void 
	createModelAnalysisPage
	( Composite model_analysis_composite ) 
	// example code for dealing with swt/swing integration provided by
	// http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
	{
		// the following should be set before any SWING widgets are
		// instantiated it reduces flicker on a resize
		//System.setProperty("sun.awt.noerasebackground", "true");
		model_analysis_composite.addControlListener(new CleanResizeListener(frame));

		SwingUtilities.invokeLater( 
			new Runnable(){
				@Override
				public void run() {
					ModelCreationEditor.this.frame.setLayout(
						new BorderLayout()
					);
					ModelCreationEditor.this.frame.setExtendedState(
						JFrame.MAXIMIZED_BOTH
					);
					ModelCreationEditor.this.frame.setTitle(
						"Another Text Editor"
					);
					ModelCreationEditor.this.frame.setBackground(
						Color.white
					);
					ModelCreationEditor.this.frame.pack();
					ModelCreationEditor.this.frame.setVisible(true);
					SwingUtilities.updateComponentTreeUI(frame);	
				}
			}
		);
	    
		int index
			= super.addPage(model_analysis_composite);
		super.setPageText(index, "Model Analysis");
	}
	
	private void 
	createModelTestPage() 
	{
		Composite parent 
			= super.getContainer();
		
		ScrolledForm model_test_form 
			= this.toolkit.createScrolledForm(parent);
		model_test_form.setText(
			"Test and Simulation Framework"
		);
		
		TableWrapLayout layout 
			= new TableWrapLayout();
		layout.numColumns = 1;
		model_test_form.getBody().setLayout(layout);
		
		Section control_composite
			= this.toolkit.createSection(
				model_test_form.getBody(),
				Section.TITLE_BAR 
					| Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		control_composite.setText("Run a Simulation");
				
		Composite control_client
			= this.toolkit.createComposite(control_composite);
		this.initializeControlGrid(control_client);
		this.initializeControlWidgets( control_client );
		
		control_composite.setClient(control_client);
	
		Section table_composite
			= this.toolkit.createSection(
					model_test_form.getBody(),
					Section.TITLE_BAR 
						| Section.EXPANDED 
						| Section.DESCRIPTION 
						| Section.TWISTIE
				);
		table_composite.setText("View Simulations");
				
		Composite table_client
			= this.toolkit.createComposite(table_composite);
		this.initializeTableClientGrid(table_client);
		this.initializeTableClientWidgets( table_client );
		
		table_composite.setClient(table_client);
	
		int index
			= super.addPage( model_test_form );
		super.setPageText( index, "Simulate and Test" );	}
	
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
	( Composite parent ) 
	{
		
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
	( Composite parent ) 
	{
		// we do not add a layoutdata to the Label since that screws
		// up the vertical spacing
		this.toolkit.createLabel(parent, "Simulation Type: " );
		
		this.simulation_type_combo
			= new Combo(parent, SWT.NONE);
		this.initialize_simulation_types_combo(
			parent, 
			this.simulation_type_combo
		);
		
		Button run_simulation_button
			= this.toolkit.createButton(
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
		
		this.toolkit.createLabel(parent, "Best Run Name: " );
		this.best_run_result_label
			= this.toolkit.createLabel(parent, "Simulation Type: " );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_result_label.setLayoutData(grid_data);
		
		this.toolkit.createLabel(parent, "Best Run Algorithm: " );
		this.best_run_algorithm_label
			= toolkit.createLabel(parent, "Simulation Type: " );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_algorithm_label.setLayoutData(grid_data);
		
		this.toolkit.createLabel(parent, "Best Run Cost: " );
		this.best_run_cost_label
			= toolkit.createLabel(parent, "Simulation Type: " );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.best_run_cost_label.setLayoutData(grid_data);
	
		this.toolkit.createLabel(parent, "Total Number: " );
		this.total_simulation_units
			= toolkit.createLabel(parent, "Simulation Type: " );
		grid_data  
			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
		grid_data.horizontalSpan = 2;
		this.total_simulation_units.setLayoutData(grid_data);
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
					ModelCreationEditor.this.controller.setModelProperty(
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

	private void 
	setSwingLookAndFeel() 
	throws InvocationTargetException, InterruptedException 
	{
		SwingUtilities.invokeAndWait(
			new Runnable(){
				public void run(){
					try {
						UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName() 
						);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
	}
	
	public void
	updateTitle()
	// the following method is recommended by the eclipse
	// plugins book
	{
		IEditorInput input
			= super.getEditorInput();
		super.setPartName( input.getName() );
		super.setTitleToolTip(this.getTitleToolTip());
	}
	
	@Override
	public String
	getTitleToolTip()
	{
		IEditorInput input
			= super.getEditorInput();
		String return_value = null;
		
		String path = input.getToolTipText();
		int count = 0;
		for(int i = 0; i < path.length(); ++i){
			if(path.charAt(i) == ' '){
				count++;
			}
			if( count == 2){
				System.out.println(path.substring(i+1));
				return_value = path.substring(i+1);
				break;
			}
		}
		if(return_value == null){
			return_value = "";
		}
		
		ModelCreationEditor.this.controller.setModelProperty(
			Constants.GUI_PROFILER_TRACE, 
			return_value
		);
		
		return return_value;
	}

	@Override
	public void 
	modelPropertyChange
	( PropertyChangeEvent evt ) 
	{
		switch(evt.getPropertyName())
		{
		case Constants.GUI_MODULE_COARSENER:
			ModuleCoarsenerType mc 
				= (ModuleCoarsenerType) evt.getNewValue();
			System.out.println(
				"The module coarsener was modified to " + mc.getText()
			);
			break;
		case Constants.GUI_PROFILER_TRACE:
			this.model_configuration_page.setProfilerTracePath(
				(String) evt.getNewValue()
			);
		
			break;
		case Constants.GUI_MODULE_EXPOSER:
			this.model_configuration_page.setModuleExposerPath(
				(String) evt.getNewValue()
			);
			break;
		case Constants.GUI_HOST_CONFIGURATION:
			this.model_configuration_page.setHostConfigurationPath(
				(String) evt.getNewValue()
			);
			break;
		case Constants.GUI_PERFORM_PARTITIONING:
			{
				boolean enabled 
					= (boolean) evt.getNewValue();
				this.model_configuration_page
					.set_partitioning_widgets_enabled( enabled );
			}
			break;
		case Constants.ACTIVE_CONFIGURATION_PANEL:
			boolean enabled
				= (boolean) evt.getNewValue();
			this.model_configuration_page.visualizeModuleModel();
			this.model_configuration_page.set_configuration_widgets_enabled( enabled );
			break;
		case Constants.MODULE_EXCHANGE_MAP:
			Map<ModulePair, InteractionData> map
				= (Map<ModulePair, InteractionData>) evt.getNewValue();
			this.model_configuration_page.setModuleMap(map);
			break;
		case Constants.ALGORITHM:
			String algorithm
				= (String) evt.getNewValue();
			this.model_configuration_page.setAlgorithm( algorithm );
			break;
		case Constants.SOLUTION:
			String solution
				= (String) evt.getNewValue();
			this.model_configuration_page.setSolution( solution );
			break;
		default:
			System.out.println("Swallowing message.");
		};
	}
	
	@Override
	public void 
	dispose()
	{
		synchronized(this.current_vp_lock){
			if(this.currentVP != null){
				this.currentVP.destroy();
			}
		}
	}
}
