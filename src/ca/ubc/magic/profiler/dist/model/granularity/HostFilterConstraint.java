package ca.ubc.magic.profiler.dist.model.granularity;

public class HostFilterConstraint extends FilterConstraint {
	
	private long mHostId;		
	private CodeEntity mEntity;
	
	public void setEntity(CodeEntity entity){
		if (mEntity == null)
			mEntity = entity;
		else
			throw new RuntimeException("Too many host filter entities");
	}
	
	public void setHostId(long id){
		mHostId = id;
	}
	
	public long getHostId(){
		return mHostId;
	}
	
	public CodeEntity getEntity(){
		return mEntity;
	}
	
	public void addEntity(CodeEntity entity){
		mEntity = entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mEntity == null) ? 0 : mEntity.hashCode());
		result = prime * result + (int) mHostId;
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
		HostFilterConstraint other = (HostFilterConstraint) obj;
		if (mEntity == null) {
			if (other.mEntity != null)
				return false;
		} else if (!mEntity.equals(other.mEntity))
			return false;
		if (mHostId != other.mHostId)
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}
}
