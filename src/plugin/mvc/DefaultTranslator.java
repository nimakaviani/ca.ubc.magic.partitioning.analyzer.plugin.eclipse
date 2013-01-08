package plugin.mvc;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import plugin.LogUtilities;
import plugin.mvc.EventTypes.PropertyEvent;
import plugin.mvc.EventTypes.ToModelEvent;
import plugin.mvc.EventTypes.ViewsEvent;
import plugin.mvc.adapter.AdapterDelegate;

// all views must register an adapterDelegate with the
// controller that they are interacting with
//
// a view cannot register a callback with the same
// method name as another callback registered by the
// same view (no adding overloaded methods)
// ( this means unique method names across all registered callbacks)
public class 
DefaultTranslator 
implements ITranslator
{
	private IModel 						model;
	private List<IView> 				registered_views;
	private Map<IView, AdapterDelegate> adapter_map;
	// the following is used and reused in a few functions
	// it should always be cleared before being used
	private Map<String, Object> 		adapter_buffer_map;
	
	public final static String EVENT_SENTINEL
		= "Event";
		
	public 
	DefaultTranslator()
	{
		this.registered_views
			= new CopyOnWriteArrayList<IView>();
		this.adapter_map
			= new HashMap<IView, AdapterDelegate>();
		this.adapter_buffer_map	
			= new HashMap<String, Object>();
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
	removeViewAndAdapter
	(IView view)
	{
		this.registered_views.remove(view);
		if( this.adapter_map.containsKey(view)){
			this.adapter_map.remove(view);
		}
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
            System.out.printf("Calling method set%s() in class %s%n",
            	property_name, this.model.getClass()
            );
        } catch (NoSuchMethodException ex) {
        	String message
        	 	= String.format(
	        		"No method set%s() in class %s%n", 
	        		property_name, this.model.getClass()
	        	);
        	System.err.println(message);
        	LogUtilities.logError(message, ex);
        } catch (Exception ex) {
        	ex.printStackTrace();
        	LogUtilities.logError(ex);
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
				DefaultTranslator.EVENT_SENTINEL, 
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
            	"Calling method do%s() in class %s%n",
            	event_name, 
            	this.model.getClass()
            );
        } catch (NoSuchMethodException ex) {
        	String message = String.format(
        		"No method do%s() in class %s%n", 
        		event_name, this.model.getClass()
        	);
        	System.err.println(message);
        	LogUtilities.logError(message, ex);
        } catch (Exception ex) {
        	ex.printStackTrace();
        	LogUtilities.logError(ex);
		}
	}
	
	private Map<String, Object> 
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
		// we use the event sentinel to indicate that an event is being 
		// generated
		if( isEvent(evt) ){
			for(IView view : this.registered_views){
				this.callDesignatedMethod(view, evt);
			}
		}
		else {
			for(IView view : this.registered_views){
				System.out.println("PropertyChange: " + evt.getPropertyName());
				this.callDesignatedMethod( view, evt );
			}
		}
	}
	
	private boolean
	isEvent
	( PropertyChangeEvent evt )
	{
		return evt.getOldValue() != null && evt.getOldValue().equals( DefaultTranslator.EVENT_SENTINEL);
	}
	/////////////////////////////////////////////////////////////////////////////////
	//	Work with adapter
	/////////////////////////////////////////////////////////////////////////////////

	@Override
	public void
	registerAdapter
	( IView view, AdapterDelegate adapter )
	{
		if( this.adapter_map.containsKey(view)){
			throw new IllegalArgumentException(
				"That view is already contained in this map.");
		}
		this.adapter_map.put(view, adapter);
	}
	
	@Override
	public void
	unregisterAdapter
	( IView view )
	{
		if( !this.adapter_map.containsKey(view)){
			throw new IllegalArgumentException("That view is not contained in this map");
		}
		this.adapter_map.remove(view);
	}
	
	@Override
	public void
	requestReply
	( IView view, String method_name, Object args )
	{
		if( view == null ){
			throw new IllegalArgumentException(
				"The view argument cannot be null"
			);
		}
		if( method_name == null ){
			throw new IllegalArgumentException(
				"The method name cannot be null"
			);
		}
		if( method_name.trim().equals("") ){
			throw new IllegalArgumentException(
				"The method name argument cannot be empty"
			);
		}
		
		AdapterDelegate adapter
			= this.adapter_map.get(view);
		String[] query_keys
			= adapter.getQueryKeys(method_name);
		
		System.err.println("Query Keys: ");
		for(String s: query_keys){
			System.err.println(s);
		}
		
		Map<String, Object> objs
			= this.requestProperties(query_keys);
		Object[] parameters
			= adapter.getQueryMethodParameters(method_name, objs, args);
		System.err.println("Parameters: ");
		for(Object s: parameters){
			System.err.println(s);
		}
		
		Class<?>[] parameter_types
			= adapter.getParameterTypes(method_name);
		System.err.println("Types: ");
		for(Object s: parameter_types){
			System.err.println(s);
		}
		try {
			Method method 
				= view.getClass().getMethod( 
		     		method_name,
		     		parameter_types
		     	);
			method.invoke(view, parameters );
			
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
			LogUtilities.logError(ex);
		} catch (SecurityException ex) {
			ex.printStackTrace();
			LogUtilities.logError(ex);
		} catch (InvocationTargetException ex){
			ex.printStackTrace();
			LogUtilities.logError(ex);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			LogUtilities.logError(ex);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			LogUtilities.logError(ex);
		}
	}

	private void 
	callDesignatedMethod
	( IView view, PropertyChangeEvent evt ) 
	{
		AdapterDelegate adapter
			= this.adapter_map.get(view);
		if( adapter == null ){
			throw new IllegalArgumentException(
				"The calling view "
				+ view.getClass().getName()
				+ " has not registered an adapter"
			);
		}
		String method_name
			= isEvent(evt)
			? adapter.getEventMethodName(evt.getPropertyName())
			: adapter.getPropertyMethodName(evt.getPropertyName());
			
		if( method_name != null ){
			Class<?>[] parameter_types
				= adapter.getParameterTypes(method_name);
			this.adapter_buffer_map.clear();
			this.adapter_buffer_map.put(evt.getPropertyName(), evt.getNewValue());
			Object[] parameters
				= isEvent(evt)
				? adapter.getEventMethodParameters(
					method_name,
					this.adapter_buffer_map
				)
				: adapter.getPropertyMethodParameters(
					method_name,
					this.adapter_buffer_map
				);
			if( !method_name.equals("") ){
				Method method;
				try {
					method = view.getClass().getMethod( 
		            	method_name,
		            	parameter_types
		            );
					method.invoke(view, parameters);
				} catch (NoSuchMethodException ex) {
					ex.printStackTrace();
					LogUtilities.logError(ex);
				} catch (SecurityException ex) {
					ex.printStackTrace();
					LogUtilities.logError(ex);
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
					LogUtilities.logError(ex);
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
					LogUtilities.logError(ex);
				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
					LogUtilities.logError(ex);
				}
			}
		}
	}
}
