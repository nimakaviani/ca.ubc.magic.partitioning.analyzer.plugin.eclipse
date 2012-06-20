package snapshots.views;

import java.io.File;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

@SuppressWarnings("serial")
public class 
VirtualModelFileInput 
extends File
implements IStorageEditorInput 
{
	private IStorage storage;
	
	public
	VirtualModelFileInput
	( String parent, IStorage storage )
	{
		super(parent, parent);
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
	public String 
	getName() 
	{
		return this.storage.getName();
	}

	@Override
	public IPersistableElement 
	getPersistable() 
	{
		return null;
	}

	@Override
	public String 
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
	
	@Override
	public boolean
	equals
	( Object file)
	// this is used by the IWorkBenchPage.openEditor function
	// to determine when two editors are the same
	{
		if(file instanceof VirtualModelFileInput){
			return file == this;
		}
		else {
			return false;
		}
	}
}
