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
import org.eclipse.swt.graphics.Rectangle;
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

import partitioner.models.PartitionerModel;
import partitioner.models.PartitionerModelMessages;
import plugin.mvc.ControllerDelegate;
import plugin.mvc.IController;
import plugin.mvc.IModel;
import plugin.mvc.IPublisher;
import plugin.mvc.IView;
import plugin.mvc.Publications;
import plugin.mvc.PublisherDelegate;
import plugin.mvc.adapter.AdapterDelegate;
import plugin.mvc.adapter.Callback;
import plugin.mvc.adapter.DefaultAdapter;
import plugin.mvc.adapter.IAdapter;
import snapshots.views.VirtualModelFileInput;

import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;
import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory
	.ModuleCoarsenerType;
import ca.ubc.magic.profiler.partitioning.view.VisualizePartitioning;

public class 
ModelCreationEditor 
extends MultiPageEditorPart 
implements IView
{
	protected static final int MODEL_ANALYSIS_PAGE 
		= 0;

	private FormToolkit toolkit;
	
    VisualizePartitioning currentVP;
    Object current_vp_lock 
    	= new Object();
    
    private volatile Boolean 	perform_partitioning 
    	= false;
    
    private Frame 				frame;
	private IController 		controller;
	private IPublisher			publisher;
	
	private String 				algorithm;
	private String 				solution;
	private ModelTestPage 		test_page;

	public 
	ModelCreationEditor() 
	{
		this.controller 
			= new ControllerDelegate();
		this.controller.addView( this );
		this.controller.registerAdapter(this, getAdapterDelegate() );
	    
		this.controller.addModel( new PartitionerModel() );
		
		this.publisher	
			= new PublisherDelegate();
	}
	
	private void 
	initialize_view_communication() 
	{
		IWorkbenchPage page 
			= this.getSite().getPage();
		   
		IPartListener2 pl 
			= new ModelCreationEditor.ActiveEditorEventListener();
	
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

	  	try {
			this.initialize_model_analysis_page( model_analysis_composite );
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.updateTitle();

		// this should happen after the controller is assigned
		// to so we don't get any null pointer surprises
		this.initialize_view_communication();
	}

	private void 
	initialize_model_analysis_page
	( Composite model_analysis_composite ) 
	throws InvocationTargetException, InterruptedException 
	// example code for dealing with swt/swing integration provided by
	// http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
	{
		SwingUtilities.invokeAndWait( 
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
					SwingUtilities.updateComponentTreeUI(frame);	
				}
			}
		);
	    
		int index
			= super.addPage( model_analysis_composite );
		super.setPageText(index, "Model Analysis");
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
		final IEditorInput input
			= super.getEditorInput();
		
		Display.getDefault().asyncExec( new Runnable(){
			@Override
			public void
			run()
			{
				ModelCreationEditor.super.setPartName( input.getName() );
				ModelCreationEditor.super.setTitleToolTip( 
					ModelCreationEditor.this.getTitleToolTip() 
				);
			}
		});
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
		
		ModelCreationEditor.this.controller.updateModel(
			PartitionerModelMessages.PROFILER_TRACE, 
			return_value
		);
		
		return return_value;
	}

	@Override
	public void 
	modelEvent
	( final PropertyChangeEvent evt ) 
	{
		// event may be triggered by a process in a non-SWT thread
		Display.getDefault().asyncExec( new Runnable(){
			@Override
			public void run() 
			{
				System.err.println(
					"Event generated in ModelCreationEditor: " 
					+ evt.getPropertyName()
				);
				
				String property
					= evt.getPropertyName();
				if( property.equals(PartitionerModelMessages.VIEW_CREATE_TEST_FRAMEWORK.NAME)){
					// this is when the initialization must occur
					//
					// the most important thing is to know that the requested
					// object will not be created until after the partitioning is
					// performed; we can assume by the property name that the object
					// will only be non-null after this event fires
					ModelCreationEditor.this.controller.requestReply(
						ModelCreationEditor.this, 
						activate_test_page.getName(), 
						null
					);
					try {
						SwingUtilities.invokeAndWait( 
							// the following does not work if we try to pack or make visible
							new Runnable(){
								@Override
								public void run() {
									SwingUtilities.updateComponentTreeUI(frame);	
								}
								
							}
						);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				else if( property.equals( PartitionerModelMessages.MODEL_CREATED.NAME)){
					ModelCreationEditor.this.visualizeModuleModel();
				}
				else if( property.equals( PartitionerModelMessages.PARTITIONING_COMPLETE.NAME)){
					Display.getDefault().asyncExec(
							new Runnable(){
								@Override
								public void run() {
									if(ModelCreationEditor.this.perform_partitioning){
										ModelCreationEditor.this.currentVP
						            		.setAlgorithm( ModelCreationEditor.this.algorithm );
										System.out.println(
											"Algorithm: "+ ModelCreationEditor.this.algorithm 
										);
										ModelCreationEditor.this.currentVP
						            		.setSolution( ModelCreationEditor.this.solution );
										System.out.println(
											"Solution: " + ModelCreationEditor.this.algorithm 
										);
									}
								}
							}
						);
				}
				else {
					System.out.println("Swallowing Message");
				}
			}
		});
	}
	
	void 
	visualizeModuleModel() 
	{
		Rectangle bounds 
			= this.getContainer().getBounds();
		final int width
			= bounds.width;
		final int height
			= bounds.height;

		ModelCreationEditor.this
			.controller.requestReply(
				ModelCreationEditor.this, 
				create_visualize_partitioning.getName(), 
				new Object[]{ 
					ModelCreationEditor.this.frame, 
					Integer.valueOf(width), 
					Integer.valueOf(height),
				}
			);
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
		
		// notify peers on the controller
		this.controller.notifyPeers(
			PartitionerModelMessages.EDITOR_CLOSED,
			this, 
			null
		);
		
		// make system wide publication
		VirtualModelFileInput input
			= (VirtualModelFileInput) this.getEditorInput();
		
		this.publisher.publish(
			this.getClass(), 
			Publications.MODEL_EDITOR_CLOSED, 
			input
		);
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
	
	private class
	ActiveEditorEventListener
	implements IPartListener2
	{

		@Override
		public void 
		partActivated
		( IWorkbenchPartReference part_ref ) 
		{
			if(part_ref.getPart(false) instanceof ModelCreationEditor ){
				ModelCreationEditor editor
					= (ModelCreationEditor) part_ref.getPart(false);
				
				System.out.println( "Active: " + part_ref.getTitle() );
				
				ModelCreationEditor.this.publisher.publish( 
					this.getClass(), 
					Publications.ACTIVE_EDITOR_CHANGED, 
					editor.getController()
				);
			}
		}

		@Override
		public void 
		partBroughtToTop(IWorkbenchPartReference partRef) {}

		@Override
		public void 
		partClosed
		( IWorkbenchPartReference partRef ) {}

		@Override
		public void 
		partDeactivated
		( IWorkbenchPartReference partRef ) {}

		@Override
		public void 
		partOpened
		( IWorkbenchPartReference partRef ) {}

		@Override
		public void 
		partHidden
		( IWorkbenchPartReference partRef ) {}

		@Override
		public void 
		partVisible
		( IWorkbenchPartReference partRef ) {}

		@Override
		public void 
		partInputChanged
		( IWorkbenchPartReference partRef ) {}
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	///	Code for dealing with adapters
	///////////////////////////////////////////////////////////////////////////////////
	
	private AdapterDelegate adapter_delegate;
	
	private static Callback create_visualize_partitioning
		= new Callback("createVisualizePartitioning", VisualizePartitioning.class);
	private static Callback activate_test_page
		= new Callback("activateTestPage", IModel.class);
	
	private AdapterDelegate
	getAdapterDelegate()
	{
		if(this.adapter_delegate == null){
			this.adapter_delegate 
				= new AdapterDelegate();
			
			this.adapter_delegate.registerDepositCallback(
				activate_test_page, 
				new DefaultAdapter( PartitionerModel.AFTER_PARTITIONING_COMPLETE_TEST_FRAMEWORK )
			);
			this.adapter_delegate.registerDepositCallback(
				create_visualize_partitioning, 
				new IAdapter(){
					VisualizePartitioning vp = null;
					
					String[] keys
						= new String[]{ 
							PartitionerModel.AFTER_MODEL_CREATION_MODULE_EXCHANGE_MAP,
							PartitionerModelMessages.MODULE_COARSENER.NAME
						};
					
					@SuppressWarnings("unchecked")
					@Override
					public Object[] 
					adapt
					( final Map<String, Object> objs, final Object arg )
					{
						try {
							SwingUtilities.invokeAndWait( new Runnable(){
								@Override
								public void
								run()
								{
									Object[] args = (Object[]) arg;
									Frame frame = (Frame) args[0];
									int width = (int) args[1];
									int height = (int) args[2];
									ModuleCoarsenerType coarsener_type 
										= (ModuleCoarsenerType) objs.get(
											PartitionerModelMessages.MODULE_COARSENER.NAME
										);
									
									vp = new VisualizePartitioning( 
											frame, 
											width, 
											height, 
											coarsener_type 
										);

									vp.drawModules(
											(Map<ModulePair, InteractionData>) objs.get(
												PartitionerModel.AFTER_MODEL_CREATION_MODULE_EXCHANGE_MAP
											)
										); 
									}
								});
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						return new Object[]{ vp };
					}

					@Override
					public String[] 
					getKeys() 
					{
						return keys;
					}
					
				}
			);
		}
		
		return this.adapter_delegate;
	}
	
	public void
	createVisualizePartitioning
	( final VisualizePartitioning vp )
	{
		SwingUtilities.invokeLater(
			new Runnable(){
				@Override
				public void 
				run() 
				{
					synchronized( ModelCreationEditor.this.current_vp_lock ){
						ModelCreationEditor.this.currentVP
							= vp;
						
						try {
							ModelCreationEditor.this.frame.pack();
							SwingUtilities.updateComponentTreeUI(
								ModelCreationEditor.this.frame
							);
							
							Display.getDefault().syncExec( new Runnable(){
								@Override
								public void run() {
									ModelCreationEditor.this
										.getControl(ModelCreationEditor.MODEL_ANALYSIS_PAGE)
										.pack(true);
									ModelCreationEditor.this.getContainer().layout();
								}
								
							});
						
						} catch (Exception e) {
							e.printStackTrace();
						};
					}
				}
			}
		);
	}
	
	public void 
	activateTestPage
	( IModel test_framework_model ) 
	{
		Composite parent 
			= super.getContainer();
			
		this.test_page
			= new ModelTestPage(
				this.toolkit, 
				parent,
				this,
				test_framework_model
			);
		this.toolkit.adapt( this.test_page );
			
		int index
			= super.addPage( this.test_page );
		super.setPageText( 
			index, 
			"Simulate and Test" 
		);
	}
}
