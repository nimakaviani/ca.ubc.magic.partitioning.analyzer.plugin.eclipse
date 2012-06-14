package partitioner.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.ProgressMonitorInputStream;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import EDU.oswego.cs.dl.util.concurrent.misc.SwingWorker;

import partitioner.models.PartitionerGUIStateModel;
import plugin.Constants;
import snapshots.controller.ControllerDelegate;
import snapshots.views.IView;

import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory
	.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.view.VisualizePartitioning;

// classes extending IEditorPart contain the Java
// code defining the editor's behaviour
// TODO: the internal swing worker accesses class fields, so this object
// must be thread safe too.
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
	private Form form;
	
    VisualizePartitioning currentVP;
    List<VisualizePartitioning> vpList 
    	= new ArrayList<VisualizePartitioning>();
    private JDesktopPane desktopPane;
    private Frame frame;
	
	private ControllerDelegate controller;
	private Label profiler_trace_text;
	
	final private PartitionerGUIStateModel partitioner_gui_state_model
		= new PartitionerGUIStateModel();
	private Text host_config_text;
	private Text module_exposer_text;
	
	
	public 
	ModelCreationEditor() 
	{}

	@Override
	public void
	doSave
	( IProgressMonitor monitor ) 
	// save the contents of this editor
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
		// typically we also want to perform a sanity check on the
		// input parameter (using instanceof)
		super.init(site, input);
		this.controller = new ControllerDelegate();
		this.controller.addModel(this.partitioner_gui_state_model);
		this.controller.addView(this);
		this.desktopPane
			= new JDesktopPane();
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
		this.createModelConfigurationPage();
		this.createModelAnalysisPage();
		this.updateTitle();
		
		// the following code is a view-communication solution
		// found in:
		// http://tomsondev.bestsolution.at/2011/01/03/enhanced-rcp-how-views-can-communicate/
		// it may not work given that this is not a view but an editor
		BundleContext context 
			= FrameworkUtil.getBundle(ModelCreationEditor.class).getBundleContext();
		EventHandler handler 
			= new EventHandler() {
				public void handleEvent
				( final Event event )
				{
					System.out.println("Calling event handler");
					
					Display parent 
						= Display.getCurrent();
					if( parent.getThread() == Thread.currentThread() ){
						String profiler_trace 
							= (String) event.getProperty("PROFILER_TRACE");
						ModelCreationEditor.this.controller.setModelProperty(
							Constants.GUI_PROFILER_TRACE, 
							profiler_trace
						);
					}
					else {
						parent.syncExec( 
							new Runnable() {
								public void 
								run()
								{
									String profiler_trace 
										= (String) event.getProperty("PROFILER_TRACE");
									ModelCreationEditor.this.controller.setModelProperty(
										Constants.GUI_PROFILER_TRACE, 
										profiler_trace
									);
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

	}

	private void 
	createModelConfigurationPage() 
	// the following code follows the example provide in 
	// http://www.eclipse.org/articles/Article-Forms/article.html
	// it is my first stab at working with eclipse forms
	{
		Composite parent 
			= super.getContainer();
		
		this.toolkit 
			= new FormToolkit(parent.getDisplay());
		this.form 
			= this.toolkit.createForm(parent);
		this.form.setText("Configure and Create a Model");
		this.toolkit.decorateFormHeading( this.form );
		
		TableWrapLayout layout 
			= new TableWrapLayout();
		layout.numColumns = 1;
		this.form.getBody().setLayout(layout);
		
		Section set_paths_composite
			= this.toolkit.createSection(
				this.form.getBody(),
				Section.TITLE_BAR 
					|Section.EXPANDED 
					| Section.DESCRIPTION 
					| Section.TWISTIE
			);
		set_paths_composite.setText("Set the File Paths");
		set_paths_composite.setDescription(
			"Set the files from which the model shall be built."
		);
		
		Composite set_paths_client
			= this.toolkit.createComposite(set_paths_composite);
		this.initializeSetPathsBarGrid(set_paths_client);
		this.initializeSetPathsBarWidgets( set_paths_client );
		
		TableWrapData td 
			= new TableWrapData(TableWrapData.FILL);
		set_paths_composite.setLayoutData(td);
		set_paths_composite.addExpansionListener(
			new ExpansionAdapter() {
				public void 
				expansionStateChanged
				( ExpansionEvent e ) 
				{
					//form.reflow(true);
				}
			}
		);
		
		set_paths_composite.setClient(set_paths_client);
		
		Section configure_composite
			= this.toolkit.createSection(
				form.getBody(),
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
			= this.toolkit.createComposite(configure_composite);
		this.initializeConfigurationGrid(configure_client);
		this.initializeConfigurationWidgets( configure_client );
		td 
			= new TableWrapData(TableWrapData.FILL);
		set_paths_composite.setLayoutData(td);
		set_paths_composite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				//form.reflow(true);
			}
		});
		configure_composite.setClient(configure_client);
		
		int index
			= super.addPage( form );
		super.setPageText( index, "Model Configuration" );
	}
	
	private void 
	initializeSetPathsBarGrid
	(Composite parent) 
	{
		final GridLayout model_configuration_page_grid_layout
			= new GridLayout();
		model_configuration_page_grid_layout.numColumns = 3;
		parent.setLayout( model_configuration_page_grid_layout );
	}
	
	private void 
	initializeSetPathsBarWidgets
	( Composite parent ) 
	{
		toolkit.createLabel(parent, "Profiler Trace XML: " );
		
		this.profiler_trace_text 
			= toolkit.createLabel( 
				parent, 
				PartitionerGUIStateModel.AbbreviatePath(
					this.partitioner_gui_state_model.getProfilerTracePath()
				)
			);
		toolkit.createLabel(parent, "");
		
		toolkit.createLabel(parent, "Mod Exposer XML: " );
		this.module_exposer_text 
			= toolkit.createText( 
				parent, 
				PartitionerGUIStateModel.AbbreviatePath(
					partitioner_gui_state_model
						.getModuleExposerPath() 
				)
			);
		this.module_exposer_text.setEditable(false);
		GridData grid_data 
			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
		
		grid_data.grabExcessHorizontalSpace = true;
		// hack: will need to fix
		grid_data.widthHint = 300;
		this.module_exposer_text.setLayoutData(grid_data);
		
		Button mod_exposer_browse_button 
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
							ModelCreationEditor.this.profiler_trace_text.getText() 
						);
						String selected
							= file_dialog.open();
						if(selected != null){
							ModelCreationEditor.this.module_exposer_text.setText(selected);
							ModelCreationEditor.this.controller
								.setModelProperty(
									Constants.GUI_MODULE_EXPOSER, 
									selected
								);
						}
					}
			}
		);
		
		this.toolkit.createLabel(parent, "Host Config. XML: " );
		this.host_config_text
			=  toolkit.createText( 
				parent, 
				PartitionerGUIStateModel.AbbreviatePath(
					this.partitioner_gui_state_model.getHostConfigurationPath()
				)
			);
		host_config_text.setEditable(false);
		grid_data 
			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
		
		grid_data.grabExcessHorizontalSpace = true;
		// hack: will need to fix
		grid_data.widthHint = 300;
		host_config_text.setLayoutData(grid_data);
		
		Button host_config_browse 
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
					ModelCreationEditor.this.controller.setModelProperty(
						Constants.GUI_HOST_CONFIGURATION, 
						selected
					);
				}
			}
		});
		
		this.addPropertyListener(
			new IPropertyListener(){
				@Override
				public void 
				propertyChanged
				( Object source, int propId ) {
					switch( propId ){
					case MultiPageEditorPart.PROP_TITLE:
						ModelCreationEditor.this.controller.setModelProperty(
							Constants.GUI_PROFILER_TRACE, 
							ModelCreationEditor.this.getTitleToolTip()
						);
					default:
						System.out.println(
							"ModelCreationEditor is swallowing the event."
						);
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
	
	private HashMap<String, ModuleCoarsenerType> coarsener_hash
		= new HashMap<String, ModuleCoarsenerType>();
	
	private void 
	initializeConfigurationWidgets
	( Composite parent ) 
	{
		toolkit.createLabel(parent, "Coarsener: " );

		this.initialize_coarsener_combo_box(parent);
		
		final Button exposure_button
			= toolkit.createButton(
				parent, 
				"Activate Module Exposure", 
				SWT.CHECK
			);
		GridData grid_data 
			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		grid_data.horizontalSpan = 2;
		exposure_button.setLayoutData(grid_data);
		
		exposure_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					ModelCreationEditor.this.controller.setModelProperty(
						Constants.GUI_SET_MODULE_EXPOSURE, 
						new Boolean(exposure_button.getSelection())
					);
				}
			}
		);
		
		final Button synthetic_node_button
			= toolkit.createButton(parent, "Add Synthetic Node", SWT.CHECK);
		grid_data 
			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		grid_data.horizontalSpan = 2;
		synthetic_node_button.setLayoutData( grid_data );
		
		synthetic_node_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					ModelCreationEditor.this.controller.setModelProperty(
						Constants.GUI_SET_SYNTHETIC_NODE,
						new Boolean(synthetic_node_button.getSelection())
					);
				}
			}
		);
		final Button preset_module_graph_button
			= toolkit.createButton(parent, "Preset Module Graph", SWT.CHECK);
		grid_data 
			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		grid_data.horizontalSpan = 2;
		synthetic_node_button.setLayoutData( grid_data );
		
		preset_module_graph_button.addSelectionListener(
			new SelectionAdapter()
			{
				@Override
				public void
				widgetSelected
				( SelectionEvent e )
				{
					ModelCreationEditor.this.controller.setModelProperty(
						Constants.GUI_SET_PRESET_MODULE_GRAPH,
						new Boolean(
							preset_module_graph_button.getSelection()
						)
					);
				}
			}
		);
	
	}

	private void
	initialize_coarsener_combo_box
	( Composite parent ) 
	{
		final Combo set_coarsener_combo
			= new Combo(parent, SWT.NONE);
		
        for( final ModuleCoarsenerType mcType 
        		: ModuleCoarsenerFactory.ModuleCoarsenerType.values())
        {
        	this.coarsener_hash.put(mcType.getText(), mcType);
            set_coarsener_combo.add(mcType.getText());
        }
		
		set_coarsener_combo.addSelectionListener( 
			new SelectionAdapter(){
				public void 
				widgetSelected( SelectionEvent se )
				{
					ModelCreationEditor.this.controller.setModelProperty(
						Constants.GUI_MODULE_COARSENER,
						ModelCreationEditor.this.coarsener_hash
							.get(set_coarsener_combo.getText())
					);
				}
			}
		);
		
		set_coarsener_combo.select(0);
	}

	private void 
	createModelAnalysisPage() 
	// example code for dealing with swt/swing integration provided by
	// http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
	{
		Composite parent
			= super.getContainer();
		
		// the following should be set before any SWING widgets are
		// instantiated it reduces flicker on a resize
		//System.setProperty("sun.awt.noerasebackground", "true");
		
		Composite composite 
			= new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite.setLayout(new FillLayout());
		
		try {
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName() 
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    this.frame 
	    	= SWT_AWT.new_Frame(composite);
		composite.addControlListener(new CleanResizeListener(frame));

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
					ModelCreationEditor.this.createApplet(frame);
					ModelCreationEditor.this.frame.pack();
					ModelCreationEditor.this.frame.setVisible(true);
					SwingUtilities.updateComponentTreeUI(frame);	
				}
			}
		);
	    
		int index
			= super.addPage(composite);
		super.setPageText(index, "Model Analysis");
	}
	
	private void
	createApplet
	( Frame frame ) 
	{
		assert EventQueue.isDispatchThread();    // On AWT event thread
		
		// first widget inside the frame must be a heavyweight container 
		// and must implement RootContainer: JApplet is the only available 
		// choice since it is the only heavyweight Swing container that 
		// can be embedded
		JApplet inner_frame 
			= new JApplet();
		
		// recommended by example code on website
		inner_frame.setFocusCycleRoot(false);
		inner_frame.setSize(frame.getSize());
		frame.add(inner_frame);
		
	    this.desktopPane.setBorder(
	    	javax.swing.BorderFactory.createEtchedBorder(
	    		new Color(226, 221, 221), 
	    		new Color(154, 150, 150)
	    	)
	    );
        this.desktopPane.setName("desktopPane"); // NOI18N
        this.desktopPane.setPreferredSize(new java.awt.Dimension(300, 300));
        inner_frame.add(this.desktopPane, java.awt.BorderLayout.CENTER);
	    // test code do not keep!
	    // this.example_test_code(frame);
	    
	    this.desktopPane.setVisible(true);
	    inner_frame.setVisible(true);
	}

	private void
	updateTitle()
	// the following method is recommended by the eclipse
	// plugins book
	{
		IEditorInput input
			= super.getEditorInput();
		super.setPartName( input.getName() );
		super.setTitleToolTip( input.getToolTipText() );
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
			this.profiler_trace_text.setText( 
				PartitionerGUIStateModel.AbbreviatePath(
					(String) evt.getNewValue()
				)
			);
			break;
		case Constants.GUI_MODULE_EXPOSER:
			this.module_exposer_text.setText(
				PartitionerGUIStateModel.AbbreviatePath(
					(String) evt.getNewValue()
				)
			);
			break;
		case Constants.GUI_HOST_CONFIGURATION:
			this.host_config_text.setText(
				PartitionerGUIStateModel.AbbreviatePath(
					(String) evt.getNewValue()
				)
			);
			break;
		default:
			System.out.println("Swallowing message.");
			this.setVisualizationAction( new Object() );
		};
	}
	
	public void
	setVisualizationAction
	( Object unused )
	// TODO: also, be careful about tasks which need to take place on the awt
	// thread
	{
		try{       
			if( partitioner_gui_state_model.getProfilerTracePath() == null 
				|| partitioner_gui_state_model.getProfilerTracePath()
					.equals("") )
			{
				throw new Exception("No profiler dump data is provided.");   
			}
           	if( partitioner_gui_state_model.getHostConfigurationPath()
           			.equals("") )
           	{
           		throw new Exception ("No host layout is provided.");
           	}
           	
           	this.partitioner_gui_state_model.initializeForActivation();
           
           	// reading the input stream for the profiling XML document 
           	// provided to the tool.
           	SwingWorker worker 
           		= new SwingWorker()
           		{
           			public Object 
           			construct()
           			{
           				try{
           					final InputStream in 
           						=  new BufferedInputStream(
           							new ProgressMonitorInputStream(
           								null,
           								"Reading " 
           								+  ModelCreationEditor.this.partitioner_gui_state_model
           									.getProfilerTracePath(),           								
           								new FileInputStream(
           									ModelCreationEditor.this.partitioner_gui_state_model
           										.getProfilerTracePath()
           								)
           							)
           						);
                        
           					ModelCreationEditor.this.partitioner_gui_state_model
           						.createModuleModel(in);
           					in.close();
           				} catch(Exception e){
           					e.printStackTrace();
	                      //statusMessageLabel.setText("Error: " + e.getMessage());                      
           				}
           				return "Done!";
           			}
	               
					@Override
					public void finished(){
						ModelCreationEditor.this.partitioner_gui_state_model
							.finished();
						// visualize the model for the parsed module model
						visualizeModuleModel();  
					}
					
					private void 
					visualizeModuleModel() 
					{
						ModelCreationEditor.this.currentVP 
							= new VisualizePartitioning(
						        "Visualization for graph: " 
						        + ModelCreationEditor.this.partitioner_gui_state_model
						        	.getModuleModel().getName(), 
						        true, true, true, true
						    );
						ModelCreationEditor.this.vpList.add(
							ModelCreationEditor.this.currentVP
						);
						
						ModelCreationEditor.this.desktopPane.add(
							ModelCreationEditor.this.currentVP
						);     
						ModelCreationEditor.this.desktopPane.setVisible(true);
						
						ModelCreationEditor.this.currentVP.drawModules(
								ModelCreationEditor.this.partitioner_gui_state_model
									.getModuleModel().getModuleExchangeMap()
						);  
						ModelCreationEditor.this.currentVP.setLocation(
							30 * vpList.indexOf(currentVP), 
							30 * vpList.indexOf(currentVP)
						);
						ModelCreationEditor.this.currentVP.setVisible(true);
						
						try {
							ModelCreationEditor.this.currentVP.setMaximum(true);
							ModelCreationEditor.this.frame.pack();
							SwingUtilities.updateComponentTreeUI(
								ModelCreationEditor.this.frame
							);
						} catch (Exception e) {
							e.printStackTrace();
						};
					
					}
           		};
	        worker.start();
        }catch(Exception e){    
        	e.printStackTrace();
        }
	}
}
