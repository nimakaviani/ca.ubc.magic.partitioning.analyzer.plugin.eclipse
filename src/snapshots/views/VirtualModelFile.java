package snapshots.views;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class 
VirtualModelFile 
implements IStorage
{
	private String path;
	private String name;
	
	public
	VirtualModelFile
	( String path, String name )
	{
		this.path = path;
		this.name = name;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object 
	getAdapter
	( Class adapter ) 
	{
		return null;
	}

	@Override
	public InputStream 
	getContents() 
	throws CoreException 
	{
		return null;
	}

	@Override
	public IPath 
	getFullPath() 
	{
		return new Path(this.path);
	}

	@Override
	public String 
	getName() 
	{
		return name;
	}

	@Override
	public boolean 
	isReadOnly() 
	{
		return true;
	}
}
