package snapshots.controller;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;


import snapshots.model.IModel;
import snapshots.views.IView;


public class 
ControllerDelegate 
implements IController
{
	private Set<IModel>	registered_models;
	private List<IView> registered_views;
		
	public 
	ControllerDelegate()
	{
		this.registered_models
			= new CopyOnWriteArraySet<IModel>();
		this.registered_views
			= new CopyOnWriteArrayList<IView>();
	}
	
	@Override
	public void 
	propertyChange
	( PropertyChangeEvent evt ) 
	{
		System.out.println("Property Changed");
		for(IView view : registered_views){
			view.modelPropertyChange(evt);
		}
	}
	
	@Override
	public void 
	addModel
	(IModel model) 
	{
		if(registered_models.add(model)){
			model.addPropertyChangeListener(this);
		}
	}

	@Override
	public void 
	removeModel
	(IModel model) 
	{
		if( registered_models.remove(model) ){
			model.removePropertyChangeListener(this);
		}
	}

	@Override
	public void 
	addView
	(IView view) 
	{
		registered_views.add(view);
	}

	@Override
	public void 
	removeView
	(IView view)
	{
		registered_views.remove(view);
	}
	
	@Override
	public void 
	setModelProperty
	(String property_name, Object new_value) 
	// I should really mention that the following technique is
	// taken from the tutorial at:
	// http://www.oracle.com/technetwork/articles/javase/index-142890.html
	{
		for ( IModel model: registered_models ) {
            try {
                Method method 
                	= model.getClass().getMethod( 
                		"set" + property_name, 
                		new Class[] { new_value.getClass() }
                	);
                method.invoke(model, new_value);
                System.out.printf("Calling method set%s() in class %s\n",
                	property_name, model.getClass()
                );
            } catch (NoSuchMethodException ex) {
            	System.err.printf( 
            		"No method set%s() in class %s\n", 
            		property_name, model.getClass()
            	);
            } catch (Exception ex) {
            	ex.printStackTrace();
			}
        }
	}
	
	@Override
	public void
	alertPeers
	( String property_name, Object source, Object new_value )
	{
		PropertyChangeEvent evt 
			= new PropertyChangeEvent(source, property_name, null, new_value);
		this.propertyChange(evt);
	}

	@Override
	public void 
	notifyModel
	( String event_name ) 
	{
		for ( IModel model: registered_models ) {
            try {
                Method method 
                	= model.getClass().getMethod( 
                		"do" + event_name
                	);
                method.invoke(model);
                System.out.printf(
                	"Calling method do%s() in class %s\n",
                	event_name, 
                	model.getClass()
                );
            } catch (NoSuchMethodException ex) {
            	System.err.printf( 
            		"No method do%s() in class %s\n", 
            		event_name, model.getClass()
            	);
            } catch (Exception ex) {
            	ex.printStackTrace();
			}
        }
	}
}
