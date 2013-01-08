package ca.ubc.magic.partitioning.analyzer.plugin.eclipse;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.debug.ui.ILaunchShortcut;

// the following is modelled after the code in:
//	http://www.eclipse.org/articles/Article-Launch-Framework/launch.html
public class 
LaunchShortcut1 
implements ILaunchShortcut 
{
	@Override
	public void 
	launch
	( ISelection selection, String mode ) 
	{
		
	}

	@Override
	public void 
	launch
	( IEditorPart editor, String mode ) 
	{
		
	}
}
