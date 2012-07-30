package snapshots.views;

import java.io.File;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class 
VirtualModelFileInput 
extends File
implements IStorageEditorInput 
// TODO: concerns about threading: all methods dealing with names
// must be synchronized, since they are updated by the editor
// and then must be visible to the snapshot view
{
	private static final long serialVersionUID = 6830081047819279737L;
	transient private IStorage storage;
	private String secondary_name;
	
	public
	VirtualModelFileInput
	( String parent, IStorage storage )
	{
		super(parent);
		System.out.println(parent);
		this.storage
			= storage;
	}
	
	@Override
	public boolean 
	exists()
	{
		return true;
	}

	@Override
	public ImageDescriptor 
	getImageDescriptor() 
	{
		return null;
	}

	@Override
	synchronized public String 
	getName() 
	{
		if( this.secondary_name == null){
			return this.storage.getName();
		}
		else {
			return this.secondary_name;
		}
	}
	
	synchronized public void
	setSecondaryName
	( String secondary_name )
	{
		this.secondary_name
			= secondary_name;
	}

	@Override
	public IPersistableElement 
	getPersistable() 
	{
		return null;
	}

	@Override
	synchronized public String 
	getToolTipText() 
	{
		return this.storage.getName() + " " + this.format(this.storage.getFullPath().toString());
	}

	private String 
	format
	( String string ) 
	{
		return string.replace("\\", "/");
	}

	@Override
	public Object 
	getAdapter
	( @SuppressWarnings("rawtypes") Class adapter )
	{
		return null;
	}

	@Override
	public IStorage 
	getStorage() 
	throws CoreException 
	{
		return this.storage;
	}

	@Override
	public boolean
	isDirectory()
	{ 
		return false;
	}
	
	@Override
	public boolean
	isFile()
	{
		return false;
	}
}
