package plugin;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class 
AnalysisPerspectiveFactory 
implements IPerspectiveFactory 
{

	@Override
	public void 
	createInitialLayout
	( IPageLayout layout ) 
	{
		String editorArea 
			= layout.getEditorArea();
		
		layout.addView(
			"org.eclipse.jdt.ui.PackageExplorer",
			IPageLayout.LEFT,
			0.20f,
			editorArea
		);
		
		IFolderLayout bottom
			= layout.createFolder(
				"bottom",
				IPageLayout.BOTTOM,
				0.65f,
				editorArea
			);
		bottom.addView(Constants.PERSPECTIVE_CONFIGURATION_VIEW_ID);
		bottom.addView(Constants.ERROR_LOG_VIEW_ID);
		
		layout.addView(
			Constants.PERSPECTIVE_SNAPSHOTS_VIEW_ID, 
			IPageLayout.RIGHT, 
			// take up 25 percent of the horizontal area within window 
			0.75f, 	
			editorArea
		);
	}
}
