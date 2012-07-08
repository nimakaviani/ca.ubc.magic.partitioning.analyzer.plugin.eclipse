package ca.ubc.magic.profiler.dist.model.granularity;

import java.util.Arrays;

public class InteractionFilterConstraint extends FilterConstraint {

	private CodeEntity[] mEntities = new CodeEntity[2];
	
	public void addEntity(CodeEntity entity){
		if (mEntities[0] == null)
			mEntities[0] = entity;
		else if (mEntities[1] == null)
			mEntities[1] = entity;
		else 
			throw new RuntimeException("Too many interaction filter entities for pairing");
	}
	
	public CodeEntity[] getEntities(){
		return mEntities;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mEntities);
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
		InteractionFilterConstraint other = (InteractionFilterConstraint) obj;
		if (!Arrays.equals(mEntities, other.mEntities))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}

	@Override
	public void setHostId(long id) {}

	@Override
	public long getHostId() {
		throw new UnsupportedOperationException("Not applicable.");
	}
}