package snapshots.views;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;

import plugin.Activator;
import plugin.LogUtilities;
import plugin.mvc.DefaultTranslator;
import plugin.mvc.IPublisher.PublicationHandler;
import plugin.mvc.IPublisher.Publications;
import plugin.mvc.ITranslator;
import plugin.mvc.IPublisher;
import plugin.mvc.DefaultPublisher;
import plugin.mvc.ITranslator.IModel;
import plugin.mvc.ITranslator.IView;
import plugin.mvc.adapter.AdapterDelegate;
import plugin.mvc.adapter.Callback;
import plugin.mvc.adapter.DefaultAdapter;
import plugin.mvc.adapter.EmptyAdapter;

import snapshots.com.mentorgen.tools.util.profile.Finish;
import snapshots.com.mentorgen.tools.util.profile.Start;
import snapshots.model.ActiveSnapshotModel;
import snapshots.model.Snapshot;
import snapshots.model.SnapshotModelMessages;

public class 
SnapshotView 
extends ViewPart 
implements IView
{
	public static final String ID 
		= "snapshots.views.SampleView";

	Text 							path_text;
	Text							name_text;
	Text							port_text;
	Text 							host_text;
	
	private ITranslator 			active_snapshot_controller 
		= new DefaultTranslator();
	private IPublisher				publisher
		= new DefaultPublisher();
	private	TreeViewer				snapshot_tree_viewer;
	private FileTreeContentProvider file_tree_content_provider;

	public 
	SnapshotView() 
	{ }

	@Override
	public void 
	dispose()
	{
		// temporary solution: implement true persistence later
		Activator.getDefault().persistTreeContentProvider(
			this.file_tree_content_provider
		);
		super.dispose();
	}
	
	public void 
	createPartControl
	( Composite parent ) 
	{
		IModel active_snapshot_model
			= new ActiveSnapshotModel();
		this.active_snapshot_controller.addView(this);
		this.active_snapshot_controller.addModel( active_snapshot_model );
		this.active_snapshot_controller.registerAdapter(this, this.getAdapterDelegate());
	
		this.file_tree_content_provider
			= (FileTreeContentProvider)
				Activator.getDefault().getTreeContentProvider();
		
		GridLayout parent_layout
			= new GridLayout(1, true);
		parent.setLayout(parent_layout);
		
		Group configuration_group
			= new Group(parent, SWT.SHADOW_ETCHED_IN | SWT.FILL);
		initializeGridLayout(configuration_group);
		configuration_group.setText("New Snapshot Properties");
		// the following also initializes the text values used
		// in calls to other constructors, so it must happen
		// very early
		initializeInputRows(configuration_group);
		
		GridData grid_data 
			= new GridData(GridData.FILL_HORIZONTAL);
		configuration_group.setLayoutData(grid_data);
		
		Group tree_group
			= new Group(parent, SWT.SHADOW_ETCHED_IN);
		tree_group.setLayout(new GridLayout(1, false));
		grid_data
			= new GridData(GridData.FILL_BOTH);
		tree_group.setLayoutData(grid_data);
		tree_group.setText("Snapshots");
		
		this.snapshot_tree_viewer
			= new TreeViewer(tree_group, SWT.FILL);
		grid_data 
			= new GridData(GridData.FILL_BOTH);
		
		if( this.file_tree_content_provider == null ){
			this.file_tree_content_provider
				= new FileTreeContentProvider(
					this.path_text.getText(), 
					this.snapshot_tree_viewer
				); 
		}
		this.snapshot_tree_viewer.setContentProvider( 
			this.file_tree_content_provider
		);
		
		this.snapshot_tree_viewer.setLabelProvider( new FileTreeLabelProvider() );
		this.snapshot_tree_viewer.getTree().setLayoutData(grid_data);
		this.snapshot_tree_viewer.getTree().pack();
		this.snapshot_tree_viewer.setInput("hello");
		
		// from example code found in the eclipse plugins book
		// pp. 202
		// TODO the following is something that would traditionally be handled by the
		// controller: the view detects the event, but does not decide how to handle it
		// only how to notify the controller
		this.snapshot_tree_viewer.addDoubleClickListener(
			new IDoubleClickListener() 
			{
				@Override
				public void 
				doubleClick
				( DoubleClickEvent event ) 
				{
					IStructuredSelection selection 
						= (IStructuredSelection) event.getSelection();
					Object[] objects 
						= selection.toArray();
					if(objects.length > 0){
						if( objects[0] instanceof VirtualModelFileInput){
							System.out.println("Opening model");
									
							IWorkbenchWindow window 
								= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							IWorkbenchPage page = window.getActivePage();
							try {
								page.openEditor(
									(VirtualModelFileInput) objects[0],
									"plugin.views.model_creation_editor"
								);
							} catch (PartInitException ex) {
								ex.printStackTrace();
								LogUtilities.logError(ex);
							}
						}
						else if( objects[0] instanceof File ){
							File potential_directory
								= (File) objects[0];
							if(potential_directory.isDirectory()){
								SnapshotView.this.setSnapshotPath(potential_directory);
							}
							else if(!potential_directory.isDirectory()){
								
								// files not in the workspace are not IFiles
								// the following example code is from 
								// http://www.eclipsezone.com/eclipse/forums/t102821.html
								File file = new File(potential_directory.getAbsolutePath());
								if( file.exists() && file.isFile() ){
									VirtualModelFileInput input 
										= SnapshotView.this.file_tree_content_provider
											.addVirtualModelInput( file.getAbsolutePath() );
									IWorkbenchWindow window 
										= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
									IWorkbenchPage page = window.getActivePage();
									try {
										page.openEditor(
											input,
											"plugin.views.model_creation_editor"
										);
									} catch (PartInitException ex) {
										ex.printStackTrace();
										LogUtilities.logError(ex);
									}
									
									SnapshotView.this.refresh();
								}
							}
						}
					}
				}
			}
		);
		
		IActionBars actionBars 	
			= super.getViewSite().getActionBars();
		this.initializeToolbar(
			actionBars.getToolBarManager()
		);
			
		this.initializeDropDownMenu(actionBars.getMenuManager());
		
		this.initializeContextMenu();
		
		this.publisher.registerPublicationListener(
				this.getClass(), 
				Publications.REFRESH_SNAPSHOT_TREE, 
				new PublicationHandler(){
					@Override
					public void 
					handle
					( Object obj ) 
					{
						Boolean refresh
							= (Boolean) obj;
						if(refresh != null && refresh == true){
							SnapshotView.this.refresh();
						}
					}
				}
			);
		
		this.publisher.registerPublicationListener(
			this.getClass(),
			Publications.MODEL_EDITOR_CLOSED,
			new PublicationHandler(){
				@Override
				public void 
				handle
				( Object obj ) 
				{
					VirtualModelFileInput editor_input
						= (VirtualModelFileInput) obj;
					
					// worry about a memory leak
					SnapshotView.this.file_tree_content_provider
						.remove_model(editor_input);
					SnapshotView.this.refresh();
				}
			}
		);
	}
	
	@Override
	public void 
	setFocus() 
	{
	}
	
	private void 
	setSnapshotPath
	( File potential_directory ) 
	// currently private
	// may become public if we have a handler call this
	// or not: I wonder if I can make a handler a private class...
	{
		this.active_snapshot_controller.updateModel(
			SnapshotModelMessages.PATH, 
			potential_directory.getAbsolutePath()
		);
	}

	public void
	initializeContextMenu()
	{
		// First we create a menu Manager
		MenuManager menuManager 
			= new MenuManager();
		Menu menu 
			= menuManager.createContextMenu(
				this.snapshot_tree_viewer.getTree()
			);
		// Set the MenuManager
		this.snapshot_tree_viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(
			menuManager, 
			this.snapshot_tree_viewer
		);
		
		// Make the selection available
		getSite().setSelectionProvider(
			this.snapshot_tree_viewer
		);
	}
	
	private void 
	initializeToolbar
	(IToolBarManager toolBar) 
	{		
		IAction finish	
			= new FinishAction(
				this.active_snapshot_controller
			);
		IAction start	
			= new StartAction(
				this.active_snapshot_controller
			);
				
		toolBar.add(start);
		toolBar.add(finish);
	}
	
	private void 
	initializeInputRows
	( Composite parent ) 
	{
		Label path_label 
			= createLabel( parent, "Path: " );
		path_label.pack();
		
		this.path_text 
			= createPath( parent,1 );
		this.path_text.setLocation(80, 20);
		this.path_text.pack();
		this.path_text.setEditable(true);
		
		Button browse_button 
			= new Button( parent, SWT.NONE );
		browse_button.setText( "Browse" );
		browse_button.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected( SelectionEvent event ){
				DirectoryDialog file_dialog 
					= new DirectoryDialog( 
						PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), 
						SWT.OPEN
					);
				file_dialog.setText("Select Directory");
				file_dialog.setFilterPath( path_text.getText() );
				String selected = file_dialog.open();
				if(selected != null){
					path_text.setText(selected);
					SnapshotView.this
						.active_snapshot_controller
						.updateModel( SnapshotModelMessages.PATH, selected );
				}
				
				if(selected != null ){
					// add the new directory to the list of directories in the
					// tree viewer
					SnapshotView.this.addFolder( selected );
				}
			}
		});
		
		createLabel( parent, "Name: " );
		this.name_text 
			= createPath( parent, 2 );
		
		createLabel(parent, "Port: " );
		this.port_text 
			= createPath( parent, 2 );
		
		createLabel( parent, "Host: " );
		this.host_text 
			= createPath( parent, 2 );
		
		setWidgetText();
	}

	private void 
	initializeGridLayout
	(Composite parent) 
	{
		GridLayout grid_layout 
			= new GridLayout(3, false);
		parent.setLayout(grid_layout);
	}
	
	private Label 
	createLabel
	(Composite container, String label_text) 
	{
		Label local_label
			= new Label(container, SWT.LEFT);
		GridData grid_data 
			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		local_label.setLayoutData(grid_data);
		local_label.setText(label_text);
		
		return local_label;
	}
	
	private Text 
	createPath
	(Composite container, int num_columns) 
	{
		Text local_text
			= new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData grid_data 
			= new GridData( SWT.FILL, SWT.FILL, false, false, num_columns, 1);
		
		grid_data.grabExcessHorizontalSpace = true;
		grid_data.widthHint = 200;
		local_text.setLayoutData(grid_data);
		
		return local_text;
	}
	
	private void 
	initializeDropDownMenu
	(IMenuManager dropDownMenu) 
	{}
	
	private void 
	setWidgetText()
	{
		this.active_snapshot_controller
			.requestReply(
				this, SnapshotView.set_display.getName(),
				null
			);
	}
	
	public void
	refresh()
	// the following code is from:
	// http://cvalcarcel.wordpress.com/tag/setexpandedelements/
	{
		System.out.println("Refreshing the snapshot view");
		
		// TODO: the following should work, but the real problem is that
		// 	a message is being fired after an unregister event...
		if( !this.snapshot_tree_viewer.getTree().isDisposed() ){
			Object[] treePaths 
				= this.snapshot_tree_viewer.getExpandedElements();
			this.snapshot_tree_viewer.refresh();
			this.snapshot_tree_viewer.setExpandedElements(treePaths);
		}
	}
	
	public void
	clearSnapshots()
	{
		FileTreeContentProvider file_tree_content_provider 
			= this.getSnapshotTreeContentProvider();
		file_tree_content_provider.clear();
		this.refresh();
	}

	public void 
	removeFolder
	(File folder)
	{
		FileTreeContentProvider file_tree_content_provider
			= this.getSnapshotTreeContentProvider();
		file_tree_content_provider.remove(folder);
		this.refresh();
	}
	
	public void
	removeModel
	( VirtualModelFileInput model )
	{
		this.file_tree_content_provider.remove_model( model );
	}
	
	private FileTreeContentProvider
	getSnapshotTreeContentProvider()
	{
		IContentProvider cp 
			= this.snapshot_tree_viewer.getContentProvider();
		FileTreeContentProvider fcp
			= (FileTreeContentProvider) cp;
		
		return fcp;
	}

	public void
	addFolder
	( String path )
	{
		FileTreeContentProvider file_tree_content_provider
			= this.getSnapshotTreeContentProvider();
		file_tree_content_provider.add(path);
		this.refresh();
	}
	
	public static class 
	FinishAction 
	extends Action 
	implements IView
	{
		private Snapshot 					current_snapshot;
		private ITranslator 				active_snapshot_controller;
		
		public
		FinishAction
		( ITranslator active_snapshot_controller )
		{
			this.active_snapshot_controller
				= active_snapshot_controller;
			
			this.active_snapshot_controller.addView( this );
			this.active_snapshot_controller.registerAdapter(this, getAdapterDelegate());
			
			this.setToolTipText
			("Disconnect from application to produce snapshot.");
			
			this.setEnabled(false);
		}
		
		@Override
		public void
		setEnabled
		(boolean enabled)
		{
			super.setEnabled(enabled);
			
			String image_path
				= enabled 
				? "icons/disconnect_co_active.gif"
				: "icons/disconnect_co2.gif";
			
			this.setImageDescriptor
			(Activator.getImageDescriptor(image_path));
		}
		
		@Override
		public void run()
		{
			try{
				inner_run();
			}
			catch( IOException ex ){
				ex.printStackTrace();
				LogUtilities.logError(ex);
			}
		}
		
		private void 
		inner_run()
		throws IOException 
		{
			boolean got_exception = false;
			try {
				Finish.doFinish(
					this.current_snapshot.getHost(), 
					this.current_snapshot.getPort()
				);
			}
			catch(IOException ioex){
				got_exception = true;
				LogUtilities.logError(ioex);
			}
			
			if (!got_exception) {
				LogUtilities.logInfo("Finished Obtaining Snapshot");
				this.active_snapshot_controller.notifyPeers(
						SnapshotModelMessages.SNAPSHOT_CAPTURED,
						//Constants.EVENT_SNAPSHOT_CAPTURED_PROPERTY,
						this,
						null
				);
		    }
		    else {
		    	this.active_snapshot_controller.notifyPeers(
		    		SnapshotModelMessages.SNAPSHOT_CAPTURE_FAILED,
		    		//Constants.EVENT_SNAPSHOT_CAPTURE_FAILED,
		    		this,
		    		null
		    	);
		    }
			this.active_snapshot_controller.updateModel(
				SnapshotModelMessages.NAME, ""
			);
			this.active_snapshot_controller.notifyPeers(
				SnapshotModelMessages.SNAPSHOT_CAPTURED,
				// Constants.EVENT_SNAPSHOT_CAPTURED_PROPERTY, 
				this, null
			);
		    this.setEnabled(false);
		}

		
		private AdapterDelegate adapter_delegate;
		private final static Callback snapshot_started
			= new Callback("snapshotStarted", Snapshot.class);
		
		private AdapterDelegate
		getAdapterDelegate()
		{
			if(this.adapter_delegate == null){
				this.adapter_delegate
					= new AdapterDelegate();
				
				this.adapter_delegate.registerEventCallback(
					snapshot_started,
					new DefaultAdapter(
						SnapshotModelMessages.SNAPSHOT_STARTED.NAME
					)
				);
			}
			
			return this.adapter_delegate;
		}
		
		public void 
		snapshotStarted
		( Snapshot snapshot )
		{
			System.out.println("FinishAction: Inside snapshotStarted");
			this.setEnabled(true);
		    this.current_snapshot 
		    	= snapshot;
		}
	}

	public class 
	StartAction 
	extends Action 
	implements IView
	{
		private ITranslator 				controller;
		
		public 
		StartAction
		(	ITranslator controller )
		{
			this.controller
				= controller;
			
			this.setToolTipText
			("Record a profile snapshot.");
			this.setEnabled(true);
			this.controller.addView(this);
			this.controller.registerAdapter(this, getAdapterDelegate());
		}
		
		@Override
		public void
		setEnabled
		(boolean enabled)
		{
			super.setEnabled(enabled);
			
			String image_path
				= enabled 
				? "icons/record.png"
				: "icons/record_inactive.png";
		
			this.setImageDescriptor
			(Activator.getImageDescriptor(image_path));
		}
		
		@Override
		public void 
		run()
		{
			// the start action is now also responsible for
			// implementing the data check functionality
			if( !this.values_present() )
				return;

			// returned array should be ordered as in gui layout
			this.controller.updateModel(
				SnapshotModelMessages.PATH, SnapshotView.this.path_text.getText()
			);
			this.controller.updateModel(
				SnapshotModelMessages.NAME, SnapshotView.this.name_text.getText()
			);
			this.controller.updateModel(
				SnapshotModelMessages.HOST, SnapshotView.this.host_text.getText()
			);
			this.controller.updateModel(
				SnapshotModelMessages.PORT, SnapshotView.this.port_text.getText()
			);
			
		    Snapshot snapshot 
		    	= this.getSnapshot();
		    
		    try{
		    	this.inner_run(snapshot);
		    }
		    catch(IOException ex){
	        	ex.printStackTrace();
	        	LogUtilities.logError(ex);
	        }
		}
		
		public boolean 
		values_present() 
		// the system will also validate the inputed data when it
		// attempts to create the snapshot
		{
			Shell shell
				= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			
			StringBuilder error_message 
				= new StringBuilder();
			
			if( SnapshotView.this.path_text.getText().equals("") ){
				error_message.append("The directory path cannot be empty\n");
			}
			if( !this.validPortEntry( SnapshotView.this.port_text.getText()) ){
				error_message.append(
					"The port entry cannot be empty and must contain only numbers\n"
				);
			}
			if( SnapshotView.this.host_text.getText().equals("") ){
				error_message.append("The host name cannot be empty\n");
			}
			
			if(error_message.length() != 0 ){
				LogUtilities.displayErrorDialog(
					shell, error_message.toString()
				);
				LogUtilities.logInfo(error_message.toString());
			}
			return error_message.length() == 0;
		}
		
		private boolean 
		validPortEntry
		(String str) 
		{
			 if(str != null && str.length() > 0) {
	            int length = str.length();
	            boolean accept = true;
	            for(int idx = 0; idx < length && accept; idx++) {
	                accept = Character.isDigit(str.charAt(idx));
	            }
	            if(accept) {
	                return true;
	            }
	        }
			 
			return false;
		}

		private void 
		inner_run
		(Snapshot snapshot) 
		throws IOException 
		{
			Shell shell
				= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		    if (snapshot != null) {
		    	boolean gotException = false;
		    	try {
		    		snapshots.com.mentorgen.tools.util.profile.File.doFile(
		    			snapshot.getHost(),
		    			snapshot.getPort(),
		    			snapshot.getPathAndName());
		    	}
		    	catch (ConnectException ioex) {
		    		gotException = true;
		    		String message
		    			= "Unable to connect to a profiled application on the given port";
		    		LogUtilities.logError(message, ioex);
					LogUtilities.displayErrorDialog(
		    			shell,
		    			message
		    		);
		    	}
		    	catch( IOException ioex){
		    		gotException = true;
		    		LogUtilities.logError(ioex);
		    	}
				      
		    	// if no exception, ie the above succeeded, then go to start
		    	// command and report file success
		    	if (!gotException) {
		    		LogUtilities.logInfo("Successfully obtained file handle");
		    		try {
		    			Start.doStart(snapshot.getHost(),
		    					snapshot.getPort());
		    		}
		    		catch (IOException ioex) {
		    			gotException = true;
		    			LogUtilities.logError("Unable to Start collecting profile information", ioex);
		    		}
		    	}
		    	// if no exception, ie the above succeeded, then
		    	//report start success and send event
		    	if (!gotException) {
		    		LogUtilities.logInfo("Successfully started collecting profile info");
					this.controller.notifyPeers(
						SnapshotModelMessages.SNAPSHOT_STARTED,
						this,
					    snapshot
					);
					this.controller.updateModel(
						SnapshotModelMessages.NAME, 
						snapshot.getName()
					);
					this.setEnabled(false);
		    	}
		    } 
		}

		private Snapshot 
		getSnapshot() 
		{
		    // check path specified
		    String path 
		    	= SnapshotView.this.path_text.getText();	    
		    Shell shell
		    	= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		    
		    if (path == null || path.trim().length() == 0) {
		    	String message 
		    		= "A folder in which to store the snapshot " 
			    	+ "must be specified.";
				LogUtilities.displayErrorDialog(
		    		shell, 
		    		message
		    	);
		    	LogUtilities.logInfo(message);
		    	
		    	return null; 
		    }
		    
		    File pathFile = new File(path);
		    if (!pathFile.exists() || !pathFile.isDirectory()) {
		    	String message
		    		= "The folder " 
		    		+ pathFile.getPath() 
		    		+ " does not exist.";
				LogUtilities.displayErrorDialog(
		    		shell, 
    				message
		    	);
		    	LogUtilities.logInfo(message);
		    	return null; 
		    }
		    
		    if (!pathFile.canWrite() || !pathFile.canRead()) {
		    	String message
		    		= "The folder "
		    		+ pathFile.getPath()
		    		+ " must have both read and "
				    + "write access.";
				LogUtilities.displayErrorDialog(
		    		shell, 
    				message
		    	);
		    	LogUtilities.logInfo(message);
		    	return null;
		    }
		    
		    String newName 				
		    	=  this.setNewName( 
		    		SnapshotView.this.name_text.getText(),
		    		pathFile
		    	);
		   
		    return new Snapshot(
		        pathFile.getPath(),
		        newName,
		        SnapshotView.this.name_text.getText(),
		        SnapshotView.this.port_text.getText(),
		        SnapshotView.this.host_text.getText()
		    );
		  }
		
		private String 
		setNewName
		(String origName, File pathFile) 
		{
			String newName;
			
			// modify name if it already exists and is not empty
		    if (origName.length() > 0) {
		      boolean nameExists = true;
		      String tempName = origName;
		      String nameSuffix = ".txt";
		      if (	origName.endsWith(".txt") 
		    		|| origName.endsWith(".xml")) 
		      {
		        tempName 
		        	= origName.substring(0, origName.length() - 4);
		        nameSuffix 
		        	= origName.substring(origName.length() - 4);
		      }
		      StringBuilder buf = new StringBuilder(tempName);
		      int baseLength = buf.length();
		      for (int cnt = 0; nameExists; cnt++) {
		        buf.setLength(baseLength);
		        if (cnt > 0) {
		          buf.append(cnt);
		        }
		        buf.append(nameSuffix);
		        File txtPath = new File(pathFile, buf.toString());
		        nameExists = txtPath.exists();
		      }
		      newName = buf.toString();
		    }
		    // name is empty, create name based on current time
		    // but leave origName empty
		    else {
		      StringBuilder buf = new StringBuilder();
		      buf.append(
		    	new SimpleDateFormat("yyyyMMdd-HHmmss").format(
		    		new Date()
		    	)
		    	);
		      buf.append(".txt");
		      newName = buf.toString();
		    }
		    return newName;
		}

		private AdapterDelegate adapter_delegate;
		private final Callback snapshot_capture_failed
			= new Callback("snapshotCaptureFailed");
		private final Callback snapshot_captured
			= new Callback("snapshotCaptured");
		
		private AdapterDelegate
		getAdapterDelegate()
		{
			if(this.adapter_delegate == null ){
				this.adapter_delegate
					= new AdapterDelegate();
				
				this.adapter_delegate.registerEventCallback(
					snapshot_capture_failed,
					new EmptyAdapter(SnapshotModelMessages.SNAPSHOT_CAPTURE_FAILED.NAME)
				);
				this.adapter_delegate.registerEventCallback(
					snapshot_captured, 
					new EmptyAdapter(SnapshotModelMessages.SNAPSHOT_CAPTURED.NAME)
				);
			}
			
			return this.adapter_delegate;
		}
		
		public void
		snapshotCaptureFailed()
		{
			this.setEnabled(true);
		}
		
		public void
		snapshotCaptured()
		{
			System.out.println("Inside StartAction: snapshotCaptured");
			this.setEnabled(true);
		}
	}
	
	// note : the following class and the next modeled themselves
	// after the examples provided in 
	// http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DemonstratesTreeViewer.htm
	private static class 
	FileTreeContentProvider 
	implements ITreeContentProvider
	{
		Set<File> snapshot_folders
			= new CopyOnWriteArraySet<File>();
		Map<String, Integer> snapshots_map
			= new HashMap<String, Integer>();
		Map<String, Set<File>> snapshot_models;
		
		public
		FileTreeContentProvider
		( String path, TreeViewer tree_viewer )
		{
			File default_directory
				= new File(path);
			if(default_directory.exists()){
				this.snapshot_folders.add( default_directory );
			}
			this.snapshot_models
				= new HashMap<String, Set<File>>();
		}
		
		public void 
		remove_model
		( VirtualModelFileInput model ) 
		{
			// to delete we need the path
			String full_path
				= model.getAbsolutePath();
			System.out.println(full_path);
			if( this.snapshot_models.containsKey( full_path )){
				System.out.println("Contained.");
				Set<File> models
					= this.snapshot_models.get( full_path );
				models.remove(model);
			} else {
				System.out.println("That is not contained.");
			}
		}

		public VirtualModelFileInput 
		addVirtualModelInput
		( String absolute_path ) 
		{
			int count = 1;
			Set<File> models = null;
			
			// if contained, 
			if( this.snapshot_models.containsKey(absolute_path) ){
				count = this.snapshots_map.get(absolute_path) + 1;
				models = this.snapshot_models.get(absolute_path);
			}
			else {
				models = new TreeSet<File>( 
					new Comparator<File>(){
						@Override
						public int compare(File arg0, File arg1) {
							return arg0.getName().compareTo(arg1.getName());
						}
					});
				this.snapshot_models.put(absolute_path, models);
			}
			
			String virtual_file_name
				= "Model " + count;
			VirtualModelFile virtual_file 
				= new VirtualModelFile(absolute_path, virtual_file_name );
			VirtualModelFileInput return_value
				= new VirtualModelFileInput(absolute_path, virtual_file);
		
			models.add(return_value);
			this.snapshots_map.put(absolute_path, count);
			
			return return_value;
		}

		public void 
		add
		( String path ) 
		{
			File directory
				= new File( path );
			this.snapshot_folders.add(directory);
		}

		public void 
		remove
		( File folder ) 
		{
			this.snapshot_folders.remove(folder);
		}

		public void 
		clear()
		{
			this.snapshot_folders.clear();
		}

		public Object[] 
		getChildren
		( Object arg0 ) 
		{
			File parent
				= (File) arg0;
			List<File> xml_files
				= new ArrayList<File>();
			File[] children = parent.listFiles();
			if(children != null){
				for(File f : children){
					if(f.getName().endsWith(".xml") || f.getName().endsWith(".XML") || f.isDirectory()){
						xml_files.add(f);
					}
				}
				return xml_files.toArray();
			}
			else if(parent.isFile()){
				Set<File> child_list 
					= this.snapshot_models.get(parent.getAbsolutePath());
				if(child_list != null){
					children = child_list.toArray(new File[child_list.size()]);
				}
			}
			return children;
		}
		
		public Object 
		getParent
		( Object arg0 ) 
		{
			return ((File) arg0).getParentFile();
		}
		
		public boolean 
		hasChildren
		( Object arg0 ) 
		{
			Object[] obj = getChildren( arg0 );
		
			return obj == null ? false : obj.length > 0;
		}

		public Object[] 
		getElements
		( Object arg0 ) 
		{
			return this.snapshot_folders.toArray();
		}

		public void 
		dispose() 
		{
		}

		@Override
		public void 
		inputChanged
		( Viewer arg0, Object arg1, Object arg2 ) 
		{
		}
	}

	private static class 
	FileTreeLabelProvider 
	implements ILabelProvider 
	{
	  private List<ILabelProviderListener> listeners;

	  private Image file;
	  private Image dir;

	  public 
	  FileTreeLabelProvider() 
	  {
		this.listeners 
			= new ArrayList<ILabelProviderListener>();
	  }

	  public Image 
	  getImage
	  ( Object arg0 ) 
	  {
		  if( this.file == null){
			  this.file 
		    	= PlatformUI.getWorkbench()
		    	.getSharedImages()
		    	.getImage(ISharedImages.IMG_OBJ_FILE); 
		  }
		  if( this.dir == null ){
			  this.dir 
		    	= PlatformUI.getWorkbench()
		    	.getSharedImages()
		    	.getImage(ISharedImages.IMG_OBJ_FOLDER); 
		  }
		  
	    return ((File) arg0).isDirectory() ? dir : file;
	  }

	  public String 
	  getText
	  ( Object arg0 ) 
	  {
	    String text 
	    	= ((File) arg0).getName();

	    if (text.length() == 0) {
	      text = ((File) arg0).getPath();
	    }

	    return text;
	  }
	 
	  public void 
	  dispose() 
	  {
	  }

	  public boolean 
	  isLabelProperty
	  ( Object arg0, String arg1 ) 
	  {
	    return false;
	  }

	  @Override
	  public void 
	  addListener
	  ( ILabelProviderListener arg0 ) 
	  {
		  this.listeners.add( arg0 );
	  }

	  @Override
	  public void 
	  removeListener
	  ( ILabelProviderListener arg0 ) 
	  {
		  this.listeners.remove( arg0 );
	  }
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//	Code for dealing with adapters
	////////////////////////////////////////////////////////////////////////////////////////
	
	private AdapterDelegate adapter_delegate;
	
	// it may be possible to use annotations to automate this
	private static final Callback set_display
		= new Callback("setDisplayValues", String.class, String.class, String.class, String.class );
	private static final Callback set_path
		= new Callback("setPath", String.class );
	private static final Callback set_name
		= new Callback("setName", String.class );
	private static final Callback set_host
		= new Callback("setHost", String.class );
	private static final Callback set_port	
		= new Callback("setPort", String.class );
	private static final Callback capture_snapshot
		= new Callback("captureSnapshot");
	
	private AdapterDelegate 
	getAdapterDelegate() 
	{
		if( this.adapter_delegate == null ){
			this.adapter_delegate
				= new AdapterDelegate();
			this.adapter_delegate.registerDepositCallback(
				set_display, 
				new DefaultAdapter( 
					SnapshotModelMessages.PATH.NAME,
					SnapshotModelMessages.NAME.NAME,
					SnapshotModelMessages.HOST.NAME,
					SnapshotModelMessages.PORT.NAME
				)
			);
	
			this.adapter_delegate.registerPropertyCallback(
				set_path,
				new DefaultAdapter(
					SnapshotModelMessages.PATH.NAME
				)
			);
			this.adapter_delegate.registerPropertyCallback(
				set_name, 
				new DefaultAdapter(
					SnapshotModelMessages.NAME.NAME
				)
			);
			this.adapter_delegate.registerPropertyCallback(
				set_port, 
				new DefaultAdapter(
					SnapshotModelMessages.PORT.NAME
				)
			);
			this.adapter_delegate.registerPropertyCallback(
				set_host,
				new DefaultAdapter(
					SnapshotModelMessages.HOST.NAME
				)
			);
			
			// model events
			this.adapter_delegate.registerEventCallback(
				capture_snapshot,
				new EmptyAdapter(
					SnapshotModelMessages.SNAPSHOT_CAPTURED.NAME
				)
			);
		}
	
		return adapter_delegate;
	}
	
	public void 
	setDisplayValues
	(final String path, final String name, final String host, final String port)
	{
		System.out.println("Inside setDisplayValues");
		Display.getDefault().asyncExec(
			new Runnable(){
				@Override
				public void
				run(){
					SnapshotView.this.path_text.setText(path);
					SnapshotView.this.name_text.setText(name);
					SnapshotView.this.host_text.setText(host);
					SnapshotView.this.port_text.setText(port);
				}
			}
		);
	}
	
	public void
	setPath
	( final String path)
	{
		System.out.println("Inside setPath");
		Display.getDefault().asyncExec(
			new Runnable(){
				@Override
				public void
				run(){
					SnapshotView.this.path_text.setText(path);
				}
			}
		);
	}
	
	public void
	setName
	( final String name )
	{
		System.out.println("Inside setName");
		Display.getDefault().asyncExec(
		new Runnable(){
				@Override
				public void
				run(){
					SnapshotView.this.name_text.setText(name);
				}
			}
		);
	}
	
	public void
	setHost
	( final String host )
	{
		System.out.println("Inside setHost");
		Display.getDefault().asyncExec(
			new Runnable(){
				@Override
				public void
				run(){
					SnapshotView.this.host_text.setText( host );
				}
			}
		);
	}
	
	public void
	setPort
	( final String port )
	{
		System.out.println("Inside setPort");
		Display.getDefault().asyncExec(
			new Runnable(){
				@Override
				public void
				run(){
					SnapshotView.this.port_text.setText( port );
				}
			}
		);
	}
	
	public void
	captureSnapshot()
	{
		System.out.println("Inside captured snapshot");
		SnapshotView.this.snapshot_tree_viewer.setInput("hello");
		SnapshotView.this.snapshot_tree_viewer.refresh();
	}
}


