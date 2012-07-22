package ca.ubc.magic.profiler.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InteractionFilterConstraint extends FilterConstraint {

	private CodeEntity[] mEntities = new CodeEntity[2];
	
	public void addEntity(CodeEntity entity){
		if (mEntities[0] == null)
			mEntities[0] = entity;
		else if (mEntities[1] != null)
			throw new RuntimeException("Too many interaction filter entities for pairing");
		else if (mEntities[0].equals(mEntities[1]))
			throw new RuntimeException("Same modules cannot be considered for interaction filter.");
		else 
			mEntities[1] = entity;
	}
	
	public Set<CodeEntity> getEntities(){
		return new HashSet<CodeEntity>(Arrays.asList(mEntities));
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
		result = prime * result + Arrays.hashCode(mEntities);
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
		InteractionFilterConstraint other = (InteractionFilterConstraint) obj;
		if (!Arrays.equals(mEntities, other.mEntities))
			return false;
		return true;
	}
}