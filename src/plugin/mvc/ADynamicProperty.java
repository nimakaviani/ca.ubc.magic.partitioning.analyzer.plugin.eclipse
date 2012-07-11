package plugin.mvc;

public abstract class 
ADynamicProperty 
{
	private Object property;
	
	final public Object 
	getProperty(){
		if( property == null){
			property = this.instantiate();
		}
		return property;
	}
	
	public abstract Object instantiate();
}
