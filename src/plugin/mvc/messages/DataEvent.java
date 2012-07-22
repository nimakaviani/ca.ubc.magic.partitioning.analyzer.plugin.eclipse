package plugin.mvc.messages;

public abstract class 
DataEvent 
extends TimingEvent
{
	public final Class<?> 	CLASS;
	
	public
	DataEvent
	( String name, Class<?> event_class )
	{
		super(name);
		
		this.CLASS
			= event_class;
	}

	public void 
	validatePackage
	( Object data_package ) 
	{
		boolean valid_package
			= true;
		
		if( data_package == null){
			if( this.CLASS != null ){
				valid_package = false;
			}
		}
		else if( !data_package.getClass().isAssignableFrom(this.CLASS)){
			valid_package = false;
		}
		
		if( !valid_package ){
			throw new RuntimeException(
				"The data packet is not of the right type."
				+ " Please look at the documentation:" 
				+ "Data Package class: " + data_package.getClass().toString()
				+ " Correct class: " + this.CLASS.toString()
			);
		}
	}
}
