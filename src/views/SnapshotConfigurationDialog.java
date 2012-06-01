package views;

import java.beans.PropertyChangeEvent;

import jipplugin.Activator;


import model_controllers.Constants;
import model_controllers.IController;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;





public class 
SnapshotConfigurationDialog 
extends Dialog
implements IView
{
	Text 		path_text;
	Text		name_text;
	Text		port_text;
	Text 		host_text;
	IController	controller;
	
	public 
	SnapshotConfigurationDialog
	(Shell parentShell, IController controller) 
	{
		super( parentShell );
		this.controller = controller;
		this.controller.addView(this);
	}
	
	// set the title of the dialog by overriding configureShell
	@Override
	protected void 
	configureShell
	( Shell shell)
	{
		super.configureShell(shell);
		shell.setText("Configure Snapshot");
	}
	
	@Override
	public Control
	createDialogArea
	( Composite parent )
	{
		Composite container 
			= (Composite) super.createDialogArea(parent);
		
		this.initializeWidgets(container);
		this.setWidgetText();
		
		return parent;
	}

	private void 
	initializeWidgets
	(Composite container) 
	{
		initializeGridLayout(container);
		initializeRows(container);
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

	@SuppressWarnings("unused")
	private void 
	initializeRows
	(Composite container) 
	{
		Label path_label 
			= createLabel( container, "Path: " );
		this.path_text 
			= createPath( container, 1 );
		this.path_text.setEditable(false);
		
		Button browse_button 
			= new Button( container, SWT.NONE );
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
			}
		});
		
		Label name_label
			= createLabel( container, "Name: " );
		this.name_text 
			= createPath( container, 2 );
		
		Label port_label 
			= createLabel(container, "Port: " );
		this.port_text 
			= createPath( container, 2 );
		
		Label host_label 
			= createLabel( container, "Host: " );
		this.host_text 
			= createPath( container, 2 );
	}

	private void 
	initializeGridLayout
	( Composite container)
	{
		final GridLayout grid_layout
			= new GridLayout();
		grid_layout.numColumns = 3;
		container.setLayout(grid_layout);
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

	private String 
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
	
	private String
	getPreviousName()
	{
		String prevName = null;
		prevName = Activator.getDefault().getActiveSnapshotModel().getSnapshotName();
		
		if(prevName == null){
			prevName = "";
		}
		
		return prevName;
	}
	
	private String
	getPreviousPort()
	{
		String prevPort = null;
		
		prevPort = Activator.getDefault().getActiveSnapshotModel().getSnapshotPort();
		if(prevPort == null || prevPort.equals("") ){
			prevPort = "15599";
		}
		
		return prevPort;
	}
	
	private String
	getPreviousHost()
	{
		String prevHost = null;
		
		prevHost = Activator.getDefault().getActiveSnapshotModel().getSnapshotHost();
		
		if(prevHost == null || prevHost.equals("") ){
			prevHost = "localhost";
		}
		
		return prevHost;
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

	@Override
	protected boolean
	isResizable()
	{
		return true;
	}
	
	@Override
	protected void
	okPressed()
	{
		
		if(!this.valid_inputs())
			return;

		this.controller.setModelProperty(
			Constants.PATH_PROPERTY, this.path_text.getText()
		);
		this.controller.setModelProperty(
			Constants.NAME_PROPERTY, this.name_text.getText()
		);
		this.controller.setModelProperty(
			Constants.HOST_PROPERTY, this.host_text.getText()
		);
		this.controller.setModelProperty(
			Constants.PORT_PROPERTY, this.port_text.getText()
		);
		
		super.okPressed();
	}

	private boolean 
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
		/*
		if(this.path_text.getText() == ""){
			this.displayErrorDialog(shell, "The directory path cannot be empty");
			valid_inputs = false;
		}*/
		
		return valid_inputs;
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

	@Override
	public void 
	modelPropertyChange
	(PropertyChangeEvent evt) 
	{
		if(evt.getPropertyName().equals(Constants.PATH_PROPERTY)){
			this.path_text.setText( (String)evt.getNewValue());
		}
	}
}
