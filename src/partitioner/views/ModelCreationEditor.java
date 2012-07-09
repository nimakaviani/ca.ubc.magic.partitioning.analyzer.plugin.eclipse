package partitioner.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import partitioner.models.PartitionerGUIStateModel;
import plugin.Constants;
import snapshots.controller.ControllerDelegate;
import snapshots.controller.IController;
import snapshots.views.IView;

import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory
	.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.view.VisualizePartitioning;

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
    
    private volatile Boolean 				perform_partitioning 
    	= false;
    
	private Map<ModulePair, InteractionData> module_exchange_map;
	
    private Frame 				frame;
	private ControllerDelegate 	controller;
	
	private PartitionerGUIStateModel partitioner_gui_state_model;
	
	private String algorithm;
	private String solution;
	private ModelTestPage test_page;

	public 
	ModelCreationEditor() 
	{
		this.controller = new ControllerDelegate();
		this.controller.addView(this);
		
	    this.partitioner_gui_state_model
    		= new PartitionerGUIStateModel();
	    
		this.controller.addModel( this.partitioner_gui_state_model );
	}
	
	private void 
	setupViewCommunication() 
	{
		IWorkbenchPage page = this.getSite().getPage();
		   
		//adding a listener
		IPartListener2 pl = new IPartListener2() {
			public void partActivated( IWorkbenchPartReference part_ref ){
				if(part_ref.getPart(false) instanceof ModelCreationEditor ){
					ModelCreationEditor editor
						= (ModelCreationEditor) part_ref.getPart(false);
					
					System.out.println( "Active: " + part_ref.getTitle() );
					
					BundleContext context 
						= FrameworkUtil.getBundle(
							ModelCreationEditor.class
						).getBundleContext();
			        ServiceReference<EventAdmin> ref 
			        	= context.getServiceReference(EventAdmin.class);
			        EventAdmin eventAdmin 
			        	= context.getService( ref );
			        Map<String,Object> properties 
			        	= new HashMap<String, Object>();
			        properties.put( "ACTIVE_EDITOR", editor.getController() );
			        Event event 
			        	= new Event("viewcommunication/syncEvent", properties);
			        eventAdmin.sendEvent(event);
			        event = new Event("viewcommunication/asyncEvent", properties);
			        eventAdmin.postEvent(event);
				}
			}
		
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {	}
			
			@Override
			public void partClosed(IWorkbenchPartReference partRef) { }
			
			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) { }
			
			@Override
			public void partOpened(IWorkbenchPartReference partRef) { } 
			
			@Override
			public void partHidden(IWorkbenchPartReference partRef) { }
			
			@Override
			public void partVisible(IWorkbenchPartReference partRef) { }
			
			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {	}
		};
	
		page.addPartListener(pl);
	}

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
			break;
		case MODEL_ANALYSIS_PAGE:
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

	  	this.createModelAnalysisPage( model_analysis_composite );
		this.createModelTestPage();
		this.updateTitle();

		// this should happen after the controller is assigned
		// to so we don't get any null pointer surprises
		setupViewCommunication();
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
			= super.addPage( model_analysis_composite );
		super.setPageText(index, "Model Analysis");
	}
	
	private void 
	createModelTestPage() 
	{
		Composite parent 
			= super.getContainer();
		
		this.test_page
			= new ModelTestPage(
				this.toolkit, 
				parent,
				this.controller,
				this
			);
		this.toolkit.adapt(test_page);
		
		int index
			= super.addPage( test_page );
		super.setPageText( index, "Simulate and Test" );	}
	
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
		super.setTitleToolTip( this.getTitleToolTip() );
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
		case Constants.MODEL_CREATION:
			this.visualizeModuleModel();
			break;
		case Constants.MODULE_EXCHANGE_MAP:
			@SuppressWarnings("unchecked")
			Map<ModulePair, InteractionData> map
				= (Map<ModulePair, InteractionData>) evt.getNewValue();
			this.setModuleMap(map);
			break;
		case Constants.GUI_PERFORM_PARTITIONING:
			this.perform_partitioning
				= (Boolean) evt.getNewValue();
			break;
		default:
			System.out.println("Swallowing message.");
		};
	}
	
	void 
	visualizeModuleModel() 
	{
		SwingUtilities.invokeLater( new Runnable(){
			@Override
			public void
			run()
			{
				synchronized( ModelCreationEditor.this.current_vp_lock ){
					ModelCreationEditor.this.currentVP
						= new VisualizePartitioning( frame );

					// problem: drawModules is both a view type component and
					// a model type component: where does it go? 
					ModelCreationEditor.this.currentVP.drawModules(
							ModelCreationEditor.this.module_exchange_map
					);  
					
					try {
						ModelCreationEditor.this.frame.pack();
						SwingUtilities.updateComponentTreeUI(
								ModelCreationEditor.this.frame
						);
						
					} catch (Exception e) {
						e.printStackTrace();
					};
					
					// the following can be done after the creation of the VP, but can be
					// done easily using the existing
					// framework; so we may treat it as a something unrelated to the above
					Display.getDefault().asyncExec(
						new Runnable(){
							@Override
							public void run() {
								if(ModelCreationEditor.this.perform_partitioning){
									ModelCreationEditor.this.currentVP
					            		.setAlgorithm( ModelCreationEditor.this.algorithm );
									System.out.println("Algorithm: "+ ModelCreationEditor.this.algorithm );
									ModelCreationEditor.this.currentVP
					            		.setSolution( ModelCreationEditor.this.solution );
									System.out.println("Solution: " + ModelCreationEditor.this.algorithm );
								}
							}
						}
					);
				}
			}
		});
	}	
	
	public void 
	setModuleMap
	( Map<ModulePair, InteractionData> map ) 
	// TODO: EXTREMELY dangerous to pass a reference 
	//	from the model like this: see if you can create a copy
	// 	or refactor (split the VP object into parts, or something)
	{
		this.module_exchange_map
			= map;
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

	public IController 
	getController() 
	// this is going to create problems when we switch references
	{
		return this.controller;
	}
	
	public void 
	setAlgorithm
	( String algorithm ) 
	{
		this.algorithm = algorithm;
	}

	public void 
	setSolution
	( String solution ) 
	{
		this.solution = solution;
	}
	
	public void
	addSimulation()
	{
		this.test_page.customSimulationButtonActionPerformed(null);
	}

	public void 
	simTableMouseClicked
	( Integer id ) 
	{
		this.test_page.simTableMouseClicked(id);
	}
	
}
