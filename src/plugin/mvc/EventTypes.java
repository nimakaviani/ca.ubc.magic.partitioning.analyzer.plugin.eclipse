package plugin.mvc;

public final class 
EventTypes 
{
	private EventTypes(){}
	
	public static abstract class 
	TimingEvent 
	{
		final public String 	NAME;
		
		public 
		TimingEvent
		( String name )
		{
			this.NAME
				= name;
		}
	}
	
	public static abstract class 
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
			else if( !this.CLASS.isAssignableFrom( data_package.getClass() )){
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
	
	
	
	public static class 
	FromModelEvent
	extends DataEvent
	{
		public 
		FromModelEvent
		( String name, Class<?> event_class ) 
		{
			super(name, event_class);
		}
	}
	
	public static class 
	PropertyEvent
	extends DataEvent
	{
		public
		PropertyEvent
		( String name, Class<?> event_class ) 
		{
			super(name, event_class);
		}
	}
	
	public static class 
	ToModelEvent
	extends TimingEvent
	{
		public 
		ToModelEvent
		( String name ) 
		{
			super(name);
		}
	}
	
	public static class 
	ViewsEvent
	extends DataEvent
	{
		public 
		ViewsEvent
		( String name, Class<?> event_class ) 
		{
			super(name, event_class);
		}
	}

}
