package ca.ubc.magic.profiler.dist.model.granularity;

import java.util.HashSet;
import java.util.Set;

public class HostFilterConstraint extends FilterConstraint {
	
	private long mHostId;		
	private Set<CodeEntity> mEntities = new HashSet<CodeEntity>();
	
	public void setHostId(long id){
		mHostId = id;
	}
	
	public long getHostId(){
		return mHostId;
	}
	
	public Set<CodeEntity> getEntities(){
		return mEntities;
	}
	
	public void addEntity(CodeEntity entity){
		mEntities.add(entity);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mEntities == null) ? 0 : mEntities.hashCode());
		result = prime * result + (int) (mHostId ^ (mHostId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HostFilterConstraint other = (HostFilterConstraint) obj;
		if (mEntities == null) {
			if (other.mEntities != null)
				return false;
		} else if (!mEntities.equals(other.mEntities))
			return false;
		if (mHostId != other.mHostId)
			return false;
		return true;
	}
}
