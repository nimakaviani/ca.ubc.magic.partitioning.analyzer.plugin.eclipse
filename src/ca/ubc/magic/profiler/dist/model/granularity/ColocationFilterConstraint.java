package ca.ubc.magic.profiler.dist.model.granularity;

import java.util.HashSet;
import java.util.Set;

public class ColocationFilterConstraint extends FilterConstraint {

	Set<CodeEntity> mCodeEntitySet = new HashSet<CodeEntity>();
	
	@Override
	public void addEntity(CodeEntity entity) {
		mCodeEntitySet.add(entity);
	}
	
	public Set<CodeEntity> getEntities(){
		return mCodeEntitySet;
	}

	@Override
	public void setHostId(long id) {}

	@Override
	public long getHostId() {
		throw new UnsupportedOperationException("Not applicable.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mCodeEntitySet == null) ? 0 : mCodeEntitySet.hashCode());
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
		ColocationFilterConstraint other = (ColocationFilterConstraint) obj;
		if (mCodeEntitySet == null) {
			if (other.mCodeEntitySet != null)
				return false;
		} else if (!mCodeEntitySet.equals(other.mCodeEntitySet))
			return false;
		return true;
	}
}
