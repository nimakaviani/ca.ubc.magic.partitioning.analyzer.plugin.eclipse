package sketches;

import java.beans.PropertyChangeListener;

import model_controllers.Constants;
import model_controllers.ControllerDelegate;
import model_controllers.IController;
import model_controllers.IModelController;
import model_controllers.PropertyChangeDelegate;

public class 
SketchTableEntrySelectedModelController
implements IModelController
{
	private PropertyChangeDelegate property_change_delegate
		= new PropertyChangeDelegate();
	private IController controller 
		= new ControllerDelegate();
	
	boolean entry_selected;
	
	public 
	SketchTableEntrySelectedModelController()
	{
		this.controller.addModel(this);
		this.entry_selected = false;
	}
	
	public void
	setEntrySelected
	( boolean selected )
	{
		boolean old_entry_selection 
			= this.entry_selected;
		this.entry_selected = selected;
		
		this.property_change_delegate.firePropertyChange(
			Constants.SELECTED_ENTRY_PROPERTY, old_entry_selection, this.entry_selected
		);
	}

	@Override
	public IController 
	getController() 
	{
		return this.controller;
	}

	@Override
	public void 
	addPropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.addPropertyChangeListener(l);
	}

	@Override
	public void 
	removePropertyChangeListener
	( PropertyChangeListener l) 
	{
		this.property_change_delegate.removePropertyChangeListener(l);
	}
}
