package ca.ubc.magic.partitioning.analyzer.plugin.eclipse;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.*;

public class 
LaunchConfigurationTabGroup 
extends AbstractLaunchConfigurationTabGroup 
{
	public 
	LaunchConfigurationTabGroup() 
	{
	}

	@Override
	public void 
	createTabs
	( ILaunchConfigurationDialog dialog, String mode ) 
	{
		ILaunchConfigurationTab[] tabs
			= new ILaunchConfigurationTab[] {
				new JavaMainTab(),
				new JavaArgumentsTab(),
				new JavaJRETab(),
				new JavaClasspathTab(),
				new CommonTab()
			};
		
		setTabs( tabs );
	}
}
