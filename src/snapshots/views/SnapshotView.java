package snapshots.views;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;

import plugin.Activator;
import plugin.Constants;


import snapshots.action.AboutAction;
import snapshots.action.FinishAction;
import snapshots.action.JIPViewerAction;
import snapshots.action.PartitionerViewerAction;
import snapshots.action.StartAction;
import snapshots.controller.ControllerDelegate;
import snapshots.controller.IController;
import snapshots.events.SnapshotEventManager;
import snapshots.events.logging.ErrorDisplayAction;
import snapshots.events.logging.EventLogActionHandler;
import snapshots.events.logging.LogAction;

public class 
SnapshotView 
extends ViewPart 
implements IView
{
	public static final String ID 
		= "snapshots.views.SampleView";

	Text 		path_text;
	Text		name_text;
	Text		port_text;
	Text 		host_text;
	
	private IController controller 
		= new ControllerDelegate();
	EventLogTable		log_console_table;
	private	TreeViewer	snapshot_tree_viewer;
	private FileTreeContentProvider file_tree_content_provider;

	public 
	SnapshotView() 
	{
	}

	public void 
	createPartControl
	( Composite parent ) 
	{
		GridLayout parent_layout
			= new GridLayout(1, true);
		parent.setLayout(parent_layout);
		
		Group configuration_group
			= new Group(parent, SWT.SHADOW_ETCHED_IN | SWT.FILL);
		initializeGridLayout(configuration_group);
		configuration_group.setText("Snapshot Properties");
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
		
		this.file_tree_content_provider
			= new FileTreeContentProvider(this.getPreviousPath(), this.snapshot_tree_viewer); 
		snapshot_tree_viewer.setContentProvider( 
				this.file_tree_content_provider
		);
		
		snapshot_tree_viewer.setLabelProvider( new FileTreeLabelProvider() );
		snapshot_tree_viewer.getTree().setLayoutData(grid_data);
		snapshot_tree_viewer.getTree().pack();
		// the following line generates an error
		snapshot_tree_viewer.setInput("hello");
		
		// from example code found in the eclipse plugins book
		// pp. 202
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
							} catch (PartInitException e1) {
								e1.printStackTrace();
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
									} catch (PartInitException e1) {
										e1.printStackTrace();
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
		SnapshotEventManager snapshot_event_manager
			= new SnapshotEventManager();
		this.initializeToolbar(
			actionBars.getToolBarManager(), 
			snapshot_event_manager
		);
			
		this.initializeDropDownMenu(actionBars.getMenuManager());
		
		this.controller.addView(this);
		this.controller.addModel( Activator.getDefault().getActiveSnapshotModel());
		
		this.log_console_table
			= new EventLogTable(parent, "Event Log", null);
		this.log_console_table.setContents(
			Activator.getDefault().getEventLogListModel().getEventLogList()
		);
		controller.addView(this);
		
		this.initializeEventLogActionHandler();
		this.initializeContextMenu();
	}
	
	private void 
	setSnapshotPath
	( File potential_directory ) 
	// currently private
	// may become public if we have a handler call this
	// or not: I wonder if I can make a handler a private class...
	{
		SnapshotView.this.controller.setModelProperty(
			Constants.PATH_PROPERTY, 
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
		
		//getSite().registerContextMenu(menuManager, this);
		// Make the selection available
		getSite().setSelectionProvider(
			this.snapshot_tree_viewer
		);
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
	
	private void 
	initializeInputRows
	( Composite parent ) 
	{
		Label path_label 
			= createLabel( parent, "Path: " );
		path_label.pack();
		
		this.path_text 
			= createPath( parent, 1 );
		this.path_text.setLocation(80, 20);
		this.path_text.pack();
		this.path_text.setEditable(false);
		
		Button browse_button 
			= new Button( parent, SWT.NONE );
		browse_button.setText( "Browse" );
		browse_button.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected( SelectionEvent event ){
				DirectoryDialog file_dialog 
					= new DirectoryDialog( 
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						SWT.OPEN
					);
				file_dialog.setText("Select Directory");
				file_dialog.setFilterPath( path_text.getText() );
				String selected = file_dialog.open();
				if(selected != null){
					path_text.setText(selected);
				}
				
				// add the new directory to the list of directories in the
				// tree viewer
				SnapshotView.this.addFolder(selected);
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
	/*
	private void 
	initializeToolbar
	(IToolBarManager toolBar, SnapshotEventManager snapshot_event_manager) 
	{	
		/*
		IController snapshots_list_model 
			= Activator.getDefault().getSnapshotsListModel().getController();
		IController active_snapshot_model
			= Activator.getDefault().getActiveSnapshotModel().getController();
			
		snapshots_list_model.addView(this);		
		active_snapshot_model.addView(this);
		
		IAction details	
			= new ConfigureAction(this, snapshot_event_manager);
		IAction finish	
			= new FinishAction(
				snapshot_event_manager,
				active_snapshot_model,
				snapshots_list_model
			);
		IAction start	
			= new StartAction(
				snapshot_event_manager,
				active_snapshot_model			
			);
				
		toolBar.add(details);
		toolBar.add(start);
		toolBar.add(finish);
	}*/
	
	private void 
	initializeDropDownMenu
	(IMenuManager dropDownMenu) 
	{
		IAction about		
			= new AboutAction();
		IAction jip_viewer
			= new JIPViewerAction();
		IAction partitioner_viewer
			= new PartitionerViewerAction();
		
		dropDownMenu.add(about);
		dropDownMenu.add(jip_viewer);
		dropDownMenu.add(partitioner_viewer);
	}
	
	private void 
	setWidgetText()
	{
		this.path_text.setText(this.getPreviousPath());
		if(this.getPreviousName() == null){
			System.out.println("Null name odd");
		}
		this.name_text.setText(this.getPreviousName());
		this.port_text.setText(this.getPreviousPort());
		this.host_text.setText(this.getPreviousHost());
	}
	
	public String 
	getPreviousPath() 
	{
		String prevPath = null;
		
		prevPath 
			= Activator.getDefault().getActiveSnapshotModel().
				getSnapshotPath();
		if(prevPath == null || prevPath.equals("")){
			prevPath = "C:\\Users\\dillesca\\Desktop\\tmp";
		}
		
		System.out.println("Previous Path " + prevPath);
		return prevPath;		
	}
	
	public String
	getPreviousName()
	{
		String prevName = null;
		prevName = Activator.getDefault().getActiveSnapshotModel().getSnapshotName();
		
		if(prevName == null){
			prevName = "";
		}
		
		return prevName;
	}
	
	public String
	getPreviousPort()
	{
		String prevPort = null;
		
		prevPort = Activator.getDefault().getActiveSnapshotModel().getSnapshotPort();
		if(prevPort == null || prevPort.equals("") ){
			prevPort = "15599";
		}
		
		return prevPort;
	}
	
	public String
	getPreviousHost()
	{
		String prevHost = null;
		
		prevHost = Activator.getDefault().getActiveSnapshotModel().getSnapshotHost();
		
		if(prevHost == null || prevHost.equals("") ){
			prevHost = "localhost";
		}
		
		return prevHost;
	}

	@Override
	public void 
	modelPropertyChange
	( PropertyChangeEvent evt ) 
	{
		System.out.println("modelPropertyChange(): Property changed");
		switch(evt.getPropertyName()){
		case Constants.PATH_PROPERTY:
			this.path_text.setText((String) evt.getNewValue());
			break;
		case Constants.HOST_PROPERTY:
			this.host_text.setText((String) evt.getNewValue());
			break;
		case Constants.NAME_PROPERTY:
			this.name_text.setText( (String) evt.getNewValue());
			break;
		case Constants.PORT_PROPERTY:
			this.port_text.setText( (String) evt.getNewValue() );
			System.out.println(this.port_text.getText());
			System.out.println(this.host_text.getText());
			break;
		case Constants.SNAPSHOT_PROPERTY:
			System.err.println("inside snapshotview model property change");
			this.snapshot_tree_viewer.setInput("hello");
			this.snapshot_tree_viewer.refresh();
			break;
		case Constants.EVENT_LIST_PROPERTY:
			this.log_console_table.refresh();
			break;
		}
		this.refresh();
	}
	
	public void
	refresh()
	// the following code is from:
	// http://cvalcarcel.wordpress.com/tag/setexpandedelements/
	{
		Object[] treePaths 
			= this.snapshot_tree_viewer.getExpandedElements();
		this.snapshot_tree_viewer.refresh();
		this.snapshot_tree_viewer.setExpandedElements(treePaths);
	}
	
	private void 
	initializeToolbar
	(IToolBarManager toolBar, SnapshotEventManager snapshot_event_manager) 
	{		
		this.controller.addView(this);	
		this.controller.addModel(Activator.getDefault().getSnapshotsListModel());
		this.controller.addModel(Activator.getDefault().getEventLogListModel());
		
		IAction finish	
			= new FinishAction(
				snapshot_event_manager,
				this.controller,
				this.controller
			);
		IAction start	
			= new StartAction(
				snapshot_event_manager,
				this.controller,
				this
			);
				
		toolBar.add(start);
		toolBar.add(finish);
	}
	
	private void 
	initializeEventLogActionHandler() 
	{
		EventLogActionHandler action_handler
			= Activator.getDefault().getActionHandler();
		
		action_handler.registerAction(
			Constants.ACTKEY_ERROR_DISPLAY,
			new ErrorDisplayAction()
		);
		action_handler.registerAction(
			Constants.ACTKEY_LOG_DISPLAY, 
			new LogAction()
		);
	}
	
	public boolean 
	valid_inputs() 
	{
		boolean valid_inputs = true;
		
		Shell shell
			= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		if(!this.validPortEntry(this.port_text.getText())){
			this.displayErrorDialog(
				shell, 
				"The port entry cannot be empty and must contain only numbers"
			);
			
			valid_inputs = false;
		}
		if(this.host_text.getText() == ""){
			this.displayErrorDialog(shell, "The host name cannot be empty");
			valid_inputs = false;
		}
		
		if(this.path_text.getText() == ""){
			this.displayErrorDialog(shell, "The directory path cannot be empty");
			valid_inputs = false;
		}
		
		return valid_inputs;
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
	displayErrorDialog
	(Shell shell, String string) 
	{
		ErrorDialog.openError(
				shell, 
				null, 
				null,
				new Status(
					IStatus.ERROR, 
					Activator.PLUGIN_ID, 
					IStatus.OK, 
					string,
					null
				)
			);
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
}

// note : the following class and the next modeled themselves
// after the examples provided in 
// http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DemonstratesTreeViewer.htm
class 
FileTreeContentProvider 
implements ITreeContentProvider 
// TODO: Let the user add a folder, or automatically add a folder when it is used 
// in the browser
// TODO: Let the user remove a folder
// TODO: let the user double click a file to open it in the model editor
// TODO: try to remove the snapshot events classes and have a pure MVC (pure is good)
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
		this.snapshot_folders.add( default_directory );
		this.snapshot_models
			= new HashMap<String, Set<File>>();
	}
	
	public VirtualModelFileInput 
	addVirtualModelInput
	( String string ) 
	{
		int count = 1;
		Set<File> models = null;
		
		// if contained, 
		if( this.snapshot_models.containsKey(string) ){
			count = this.snapshots_map.get(string) + 1;
			models = this.snapshot_models.get(string);
		}
		else {
			models = new TreeSet<File>( 
				new Comparator<File>(){
					@Override
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareTo(arg1.getName());
					}
				});
			this.snapshot_models.put(string, models);
		}
		
		VirtualModelFile virtual_file 
			= new VirtualModelFile(string, "Model " + count);
		VirtualModelFileInput return_value
			= new VirtualModelFileInput(string, virtual_file);
	
		models.add(return_value);
		this.snapshots_map.put(string, count);
		
		return return_value;
	}

	public void 
	add
	( String path ) 
	{
		File directory
			= new File( path );
		snapshot_folders.add(directory);
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
				children = child_list.toArray(new File[0]);
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
		// Nothing to dispose
	}

	@Override
	public void 
	inputChanged
	( Viewer arg0, Object arg1, Object arg2 ) 
	{
	}
}

class 
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

  /* keep as an example
  public void 
  setPreserveCase
  ( boolean preserveCase ) 
  {
    // Since this attribute affects how the labels are computed,
    // notify all the listeners of the change.
    LabelProviderChangedEvent event = new LabelProviderChangedEvent(this);
    for (int i = 0, n = listeners.size(); i < n; i++) {
      ILabelProviderListener ilpl = (ILabelProviderListener) listeners
          .get(i);
      ilpl.labelProviderChanged(event);
    }
  }*/

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
  {}

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
    listeners.add(arg0);
  }

  @Override
  public void 
  removeListener
  ( ILabelProviderListener arg0 ) 
  {
    listeners.remove(arg0);
  }
}