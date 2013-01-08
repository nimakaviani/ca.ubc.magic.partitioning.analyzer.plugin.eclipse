package ca.ubc.magic.profiler.dist.model.granularity;

import java.util.Set;

public abstract class FilterConstraint {
	protected String mName;
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterConstraint other = (FilterConstraint) obj;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}

	public abstract void  addEntity(CodeEntity entity);
	public abstract Set<CodeEntity> getEntities(); 
	public abstract void setHostId(long id);
	public abstract long  getHostId();
}