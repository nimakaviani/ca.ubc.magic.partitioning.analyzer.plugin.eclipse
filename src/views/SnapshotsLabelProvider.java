package views;

import model_controllers.Snapshot;

import org.eclipse.jface.viewers.LabelProvider;


public class 
SnapshotsLabelProvider 
extends LabelProvider 
{
	@Override
	public String
	getText
	( Object element )
	{
		Snapshot snapshot 
		 	= (Snapshot) element;
		
		return snapshot.getPathAndName() 
			+ "   (" + snapshot.getHost()
			+ ":" + snapshot.getPort() 
			+ ")";
	}
}
