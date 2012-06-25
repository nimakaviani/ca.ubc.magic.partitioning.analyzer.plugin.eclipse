/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.dist.model;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.granularity.CodeUnitType;

/**
 *
 * @author nima
 */
public class Module {
    
    String  mModuleName;
    CodeUnitType mType;
    Integer mPartitionId;
    
    Double[]  mExecutionCost;
    Long[]    mExecutionCount;
    
    boolean mIgnoreRate = Boolean.FALSE;
    boolean mIsRoot = Boolean.FALSE;
    boolean mIsNonReplicable = Boolean.FALSE;
    
    // The default size for the number of elements to be kept for 
    // execution cost and execution count of a module with respect
    // to individual partitions.
    public static final int SIZE = 5;
    
    // the default placeholer in the cost and count arrays to keep the 
    // default values for the elements under module.
    public static final int DEFAULT = 0;
 
    public Module(String name, CodeUnitType type){
        this(name, Constants.INVALID_PARTITION_ID, type, 0.0, 0,  
        		Boolean.FALSE,  Boolean.FALSE,  Boolean.FALSE);
    }
    
    public Module(String name, int partitionId){
       this(name, partitionId, CodeUnitType.DEFAULT, 0.0, 0, 
    		   Boolean.FALSE,  Boolean.FALSE,  Boolean.FALSE);
    }
    
    public Module(Module m){
        this(m.getName(), m.getPartitionId(), m.getType(),
                m.getExecutionCost(), m.getExecutionCount(), 
                m.isIgnoreRate(), m.isRoot(), m.isNonReplicable());
    }
    
    public Module(String name, int partitionId, CodeUnitType type,
            double executionCost, long executionCount, 
            boolean ignoreRate, boolean isRoot, boolean isNonReplicable){
        mModuleName = name;
        mPartitionId = partitionId;
        mType = type;
        mExecutionCost  = new Double[Module.SIZE]; 
        mExecutionCost[Module.DEFAULT] = executionCost;
        mExecutionCount = new Long[Module.SIZE]; 
        mExecutionCount[Module.DEFAULT] = executionCount;
        mIgnoreRate = ignoreRate;
        mIsRoot = isRoot;
        mIsNonReplicable = isNonReplicable;
    }
    
    public void setName(String moduleName){
        mModuleName = moduleName;
    }
    
    public String getName(){
        return mModuleName;
    }
    
    public void setPartitionId(Integer partitionId){
        mPartitionId = partitionId;
    }
    
    public Integer getPartitionId(){
        return mPartitionId;
    }
    
    public void setType(CodeUnitType type){
        mType = type;
    }
    
    public CodeUnitType getType(){
        return mType;
    }
    
    public void setExecutionCost(Double mExecutionCost) {
        this.mExecutionCost[0] = mExecutionCost;
    }
    
    public void setExecutionCost(Double mExecutionCost, int partitionId){
        if (partitionId >= Module.SIZE)
            throw new RuntimeException("Module size is smaller than the"
                    + "number of partitions");
        this.mExecutionCost[partitionId] = mExecutionCost;
    }
    
    public void setExecutionCount(Long mExecutionCount, int partitionId){
        if (partitionId >= Module.SIZE)
            throw new RuntimeException("Module size is smaller than the"
                    + "number of partitions");
        this.mExecutionCount[partitionId] = mExecutionCount;
    }

    public void setExecutionCount(Long mExecutionCount) {
        this.mExecutionCount[Module.DEFAULT] = mExecutionCount;
    }    

    public Double getExecutionCost() {
        return mExecutionCost[Module.DEFAULT];
    }

    public Long getExecutionCount() {
        return mExecutionCount[Module.DEFAULT];
    }
    
    public void addExecutionCost(Double mExecutionCost) {
        this.mExecutionCost[Module.DEFAULT] += mExecutionCost;
    }

    public void addExecutionCount(Long mExecutionCount) {
        this.mExecutionCount[Module.DEFAULT] += mExecutionCount;
    }    
    
    public void setIgnoreRate(boolean ignoreRate){
        mIgnoreRate = ignoreRate;
    }
    
    public boolean isIgnoreRate(){
        return mIgnoreRate;
    }
    
    public void setIsRoot(boolean isRoot){
    	mIsRoot = isRoot;
    }
    
    public boolean isRoot(){
    	return mIsRoot;
    }
    
    public boolean isNonReplicable(){
    	return mIsNonReplicable;
    }
    
    public void setNonReplicable(boolean isNonReplicable){
    	mIsNonReplicable = isNonReplicable;
    }
            
    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Module))
            return false;
        if (getName().equals(((Module)obj).getName()) && 
                getPartitionId().equals(((Module)obj).getPartitionId()))
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.mModuleName != null ? this.mModuleName.hashCode() : 0);
        hash = 67 * hash + (this.mPartitionId != null ? this.mPartitionId.hashCode() : 0);
        return hash;
    }
    
    public boolean nameEquals(Object obj){
         if (!(obj instanceof Module))
            return false;
        if (mModuleName.equals(((Module)obj).getName()))
                return true;
        return false;
    }
    
    @Override
    public String toString(){
        return "[id:" + mPartitionId + "  " + mModuleName + "]";
    }
    
    public String costToString(){
        
        // it is a hack for cases where only the first element in the array
        // is set. This ensures that it returns the cumulative value as 
        // expected in to be seen in the graph.
        if (mExecutionCost[1] == null)
            return Double.toString(mExecutionCost[0] / 1.0E6);
        
        StringBuilder buffer = new StringBuilder();
        int id = 0;
        for (Double exec : mExecutionCost){
            if (id == 0){
                id++;
                continue;
            }
            if (exec == null)
                break;
            if (id == mPartitionId){
                buffer.append("\"" + exec + "\"");
            }else
                buffer.append(exec);
            buffer.append(", ");
            id++;
        }
        return buffer.toString().trim();
    }
}
