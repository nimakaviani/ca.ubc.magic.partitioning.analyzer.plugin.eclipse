package ca.ubc.magic.profiler.dist.model.granularity;

public abstract class FilterConstraint {
	protected String mName;
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public abstract void addEntity(CodeEntity entity);
	public abstract void setHostId(long id);
	public abstract long  getHostId();
}