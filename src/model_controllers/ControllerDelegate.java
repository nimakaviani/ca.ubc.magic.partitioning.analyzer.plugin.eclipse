package model_controllers;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import views.IView;

public class 
ControllerDelegate 
implements IController
{
	private Set<IModelController>	registered_models;
	private List<IView> 			registered_views;
		
	public 
	ControllerDelegate()
	{
		this.registered_models
			= new CopyOnWriteArraySet<IModelController>();
		this.registered_views
			= new CopyOnWriteArrayList<IView>();
	}
	
	@Override
	public void 
	propertyChange
	( PropertyChangeEvent evt ) 
	{
		for(IView view : registered_views){
			view.modelPropertyChange(evt);
		}
	}
	
	@Override
	public void 
	addModel
	(IModelController model) 
	{
		if(registered_models.add(model)){
			model.addPropertyChangeListener(this);
		}
	}

	@Override
	public void 
	removeModel
	(IModelController model) 
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
	{
		for ( IModelController model: registered_models ) {
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
}
