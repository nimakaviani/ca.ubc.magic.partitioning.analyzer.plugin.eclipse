package plugin.mvc;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import snapshots.views.IView;

public class 
ControllerDelegate 
implements IController
{
	private IModel 		model;
	private List<IView> registered_views;
		
	public 
	ControllerDelegate()
	{
		this.registered_views
			= new CopyOnWriteArrayList<IView>();
	}
	
	@Override
	public void 
	addModel
	( IModel model ) 
	{
		if(model == null){
			throw new IllegalArgumentException(
				"The model passed to the delegate cannot be null"
			);
		}
		
		if(this.model == null){
			this.model = model;
			this.model.addPropertyChangeListener(this);
		}
		else {
			throw new RuntimeException("You can only assign one model to a controller.");
		}
	}

	@Override
	public void 
	removeModel() 
	{
		this.model.removePropertyChangeListener(this);
		this.model = null;
	}

	@Override
	public void 
	addView
	(IView view) 
	{
		this.registered_views.add(view);
	}

	@Override
	public void 
	removeView
	(IView view)
	{
		this.registered_views.remove(view);
	}
	
	///////////////////////////////////////////////////////////////////////
	/// From the view
	///////////////////////////////////////////////////////////////////////
	
	@Override
	public void 
	setModelProperty
	(String property_name, Object new_value) 
	// I should really mention that the following technique is
	// taken from the tutorial at:
	// http://www.oracle.com/technetwork/articles/javase/index-142890.html
	{
        try {
            Method method 
            	= this.model.getClass().getMethod( 
            		"set" + property_name, 
            		new Class[] { new_value.getClass() }
            	);
            method.invoke(this.model, new_value);
            System.out.printf("Calling method set%s() in class %s\n",
            	property_name, this.model.getClass()
            );
        } catch (NoSuchMethodException ex) {
        	System.err.printf( 
        		"No method set%s() in class %s\n", 
        		property_name, this.model.getClass()
        	);
        } catch (Exception ex) {
        	ex.printStackTrace();
		}
	}
	
	@Override
	public void
	notifyPeers
	( String property_name, Object source, Object new_value )
	{
		PropertyChangeEvent evt 
			= new PropertyChangeEvent(
				source, 
				property_name, 
				null, 
				new_value
			);
		this.propertyChange(evt);
	}

	@Override
	public void 
	notifyModel
	( String event_name ) 
	{
        try {
            Method method 
            	= this.model.getClass().getMethod( 
            		"do" + event_name
            	);
            method.invoke(this.model);
            System.out.printf(
            	"Calling method do%s() in class %s\n",
            	event_name, 
            	this.model.getClass()
            );
        } catch (NoSuchMethodException ex) {
        	System.err.printf( 
        		"No method do%s() in class %s\n", 
        		event_name, this.model.getClass()
        	);
        } catch (Exception ex) {
        	ex.printStackTrace();
		}
	}
	
	@Override
	public Object[] 
	requestProperties
	( String[] property_names ) 
	{
		return this.model.request( property_names );
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// From the Model
	//////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void 
	propertyChange
	( PropertyChangeEvent evt ) 
	{
		//System.out.println("Property Changed: " + evt.getPropertyName());
		for(IView view : this.registered_views){
			view.modelPropertyChange(evt);
		}
	}

	@Override
	public Object 
	index
	( String event_name, Object key ) 
	{
        Object obj
        	= null;
        
		 try {
	            Method method 
	            	= this.model.getClass().getMethod( 
	            		"find" + event_name,
	            		new Class[]{ key.getClass() }
	            	);
	            	obj = method.invoke(this.model, key);
	            System.out.printf(
	            	"Calling method find%s() in class %s\n",
	            	event_name, 
	            	this.model.getClass()
	            );
	        } catch (NoSuchMethodException ex) {
	        	System.err.printf( 
	        		"No method find%s() in class %s\n", 
	        		event_name, this.model.getClass()
	        	);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
			}
         return obj;
	}
}
