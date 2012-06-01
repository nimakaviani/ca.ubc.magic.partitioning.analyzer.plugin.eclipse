package examples;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class 
SnapshotsList 
extends ListViewer
{
	@SuppressWarnings("rawtypes")
	Vector 	languages;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public 
	SnapshotsList
	(Composite parent) 
	{
		super(parent);
		
		this.getList().setLayoutData( 
			new GridData(SWT.FILL, SWT.FILL, true, false)
		);
			
		this.setContentProvider( new IStructuredContentProvider() {
			public Object[] getElements
			(Object inputElement) 
			{
				Vector v = (Vector)inputElement;
				return v.toArray();
			}
				  
			public void 
			inputChanged
			( Viewer viewer, Object oldInput, Object newInput)
			{
				System.out.println(
					"Input changed: old=" + oldInput + ", new=" + newInput
				);
			}
				
			@Override
			public void dispose() {
				System.out.println("Disposing...");
			}
		});
			    
		languages = new Vector();
		languages.add("java");
		languages.add("c");
		languages.add("c++");
		languages.add("smalltalk");
		
		this.setInput(languages);
		
		// the default LabelProvider, on a call to GetText()
		// will return the element's toString() string; in our case
		// that is okay
		this.setLabelProvider(new LabelProvider());
		 
		this.addSelectionChangedListener( new ISelectionChangedListener() {
				public void 
				selectionChanged
				(SelectionChangedEvent event) 
				{}
			});		
		
		this.setSorter( new ViewerSorter(){
			public int 
			compare
			(Viewer viewer, Object e1, Object e2) 
			{
				return ((String)e1).compareTo((String)e2);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void
	addSnapshot
	(String string) 
	{
		languages.add(string);
	}
}
