package plugin.mvc;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

import plugin.mvc.messages.IndexEvent;
import plugin.mvc.messages.PropertyEvent;
import plugin.mvc.messages.ToModelEvent;
import plugin.mvc.messages.ViewsEvent;

public class 
ControllerDelegate 
implements IController,
	IPublisher
{
	private IModel 		model;
	private List<IView> registered_views;
	private IPublisher	publisher_delegate;
	
	public final static String EVENT_SENTINEL
		= "Event";
		
	public 
	ControllerDelegate()
	{
		this.registered_views
			= new CopyOnWriteArrayList<IView>();
		this.publisher_delegate
			= new PublisherDelegate();
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
			throw new RuntimeException(
				"You can only assign one model to a controller."
			);
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
	updateModel
	( PropertyEvent event, Object new_value) 
	// I should really mention that the following technique is
	// taken from the tutorial at:
	// http://www.oracle.com/technetwork/articles/javase/index-142890.html
	{
		String property_name
			= event.NAME;
		
		event.validatePackage( new_value );
		
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
	( ViewsEvent event, Object source, Object new_value )
	{
		String property_name
			= event.NAME;
		
		event.validatePackage(new_value);
		
		PropertyChangeEvent evt 
			= new PropertyChangeEvent(
				source, 
				property_name, 
				ControllerDelegate.EVENT_SENTINEL, 
				new_value
			);
		this.propertyChange(evt);
	}

	@Override
	public void 
	notifyModel
	( ToModelEvent event ) 
	{
		String event_name
			= event.NAME;
		
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
	public Map<String, Object> 
	requestProperties
	( String[] property_names ) 
	{
		return this.model.request( property_names );
	}
	
	@Override
	public Object 
	index
	( IndexEvent event, Object key ) 
	{
        Object obj
        	= null;
        String event_name
        	= event.NAME;
        event.validatePackage(key);
        
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
	
	//////////////////////////////////////////////////////////////////////////////
	/// From the Model
	//////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void 
	propertyChange
	( PropertyChangeEvent evt ) 
	{
		if( evt.getOldValue() == ControllerDelegate.EVENT_SENTINEL ){
			for(IView view : this.registered_views){
				view.modelEvent(evt);
			}
		}
		else {
			for(IView view : this.registered_views){
				view.modelPropertyChange(evt);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	///	Publisher interface
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public void 
	publish 
	( Class<?> sender_class, Publications publication, Object packet) 
	{
		this.publisher_delegate.publish(
			sender_class, 
			publication, 
			packet
		);
	}

	@Override
	public ServiceRegistration<EventHandler> 
	registerPublicationListener
	( Class<?> listener_class, Publications publication, PublicationHandler publication_handler ) 
	{
		return this.publisher_delegate.registerPublicationListener(
			listener_class,
			publication,
			publication_handler
		);
	}

	@Override
	public void 
	unregisterPublicationListener
	( Publications publication, ServiceRegistration<EventHandler> service ) 
	{
		this.publisher_delegate.unregisterPublicationListener(
			publication,
			service
		);
	}
}
