package partitioner.models;

@SuppressWarnings("serial")
public class 
PartitionerModelExceptions 
{
	private PartitionerModelExceptions(){
		throw new UnsupportedOperationException();
	}
	
	public static class
	ModuleExposureException
	extends Exception
	{
		public
		ModuleExposureException
		( String string )
		{
			super(string);
		}
	}
	
	public static class
	FilterHostColocationException
	extends UnsupportedOperationException
	{
		public
		FilterHostColocationException
		( String string )
		{
			super(string);
		}
	}
}
