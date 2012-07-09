package partitioner.views;

import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class 
ModelView 
extends ViewPart 
{
	private TableViewer viewer;

	public ModelView() {
	}

	class 
	ViewLabelProvider 
	extends LabelProvider 
	implements ITableLabelProvider 
	{
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	class 
	ViewContentProvider 
	implements IStructuredContentProvider 
	{
		public void 
		inputChanged
		( Viewer v, Object oldInput, Object newInput ) 
		{
		}

		public void 
		dispose() 
		{
		}

		public Object[] 
		getElements
		( Object parent ) 
		{
			if (parent instanceof Object[]) {
				return (Object[]) parent;
			}
	        return new Object[0];
		}
	}
	
	@Override
	public void 
	createPartControl
	( Composite parent ) 
	{
		viewer
			= new TableViewer(
				parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
			);
		viewer.setContentProvider( new ArrayContentProvider() );
		viewer.setLabelProvider( new ViewLabelProvider() );
		viewer.setInput(getViewSite());
		
		Vector<String> languages = new Vector<String>();
		languages.add("java");
		languages.add("c");
		languages.add("c++");
		languages.add("smalltalk");
		
		this.viewer.setInput(languages);
		// This is new code
		// First we create a menu Manager
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getTable());
		// Set the MenuManager
		viewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
		
		//getSite().registerContextMenu(menuManager, this);
		// Make the selection available
		getSite().setSelectionProvider(viewer);
		
		viewer.getTable().addMouseListener(
			new MouseAdapter() 
			{
                public void 
                mouseDown
                ( MouseEvent e ) 
                {
                    if( viewer.getTable().getItem(new Point(e.x,e.y)) == null ) {
                            viewer.setSelection(StructuredSelection.EMPTY);
                    }
                }
	                
	        });
	}

	@Override
	public void setFocus() {
	}
}
