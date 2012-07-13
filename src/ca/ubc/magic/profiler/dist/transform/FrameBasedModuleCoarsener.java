package ca.ubc.magic.profiler.dist.transform;

import java.util.HashSet;
import java.util.Set;

import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
import ca.ubc.magic.profiler.dist.transform.model.ExtendedNodeObj;
import ca.ubc.magic.profiler.dist.transform.model.NodeObj;

public class FrameBasedModuleCoarsener extends ThreadBasedBundleModuleCoarsener {

	private static int mCounter = 0;
	
	private static Long mCurrentHeadNode = -1L;
	
	private Set<String> mModuleNameSet = new HashSet<String>();
	
	public FrameBasedModuleCoarsener(EntityConstraintModel constraintModel) {
		super(constraintModel);
	}
	
    private Module writeNode(NodeObj node, int id) {
		
       Module m = new Module(getConstrainedNodeName(node, id), node.getType());
       m.setNonReplicable(isNonReplicable(node.getName()));
       m.setExecutionCost(Math.round(node.getVertexWeight() / mCurrentHeadNode) * 1.0);
       m.setExecutionCount(((long) Math.max(node.getCount() / mCurrentHeadNode, 1.0)));
       m.setIsRoot(node.isRoot());
       mModuleModel.getModuleMap().put(m.getName(), m);
       return m;
	}
	
	protected Module recursiveWriteGraph(NodeObj node){
		Set<NodeObj> childSet = (Set<NodeObj>) node.getChildSet();
        int nodeId = mCounter++;
        
        Module m1 = writeNode(node, nodeId);
        if (childSet.isEmpty())
        	return m1;

        for (NodeObj childNode : childSet){
        	Module m2 = recursiveWriteGraph(childNode);
            addDataExchange(new ModulePair(m1, m2), childNode.getEdge4ParentWeight().longValue(), 
                childNode.getEdge2ParentWeight().longValue(), 
                (long) Math.max(childNode.getEdge4ParentCount().longValue()  / mCurrentHeadNode, 1.0),
                (long) Math.max(childNode.getEdge2ParentCount().longValue()  / mCurrentHeadNode, 1.0)); 
        }
        return m1;
	}
    
    private  String getConstrainedNodeName(NodeObj node, int id){
    	if (isNonReplicable(node.getName()))
            return node.getName();
    	return node.getName()+"-"+id;
    }
    
	private boolean recursiveSanitizeGraph(NodeObj node){
    	Set<NodeObj> childSet = (Set<NodeObj>) node.getChildSet();
        
        //checks to see if any subnode is remained in the list
        if (childSet == null || childSet.isEmpty()){
           
           // if there is no subnode and this node should be ignored, ignore it
           if (shouldIgnore(node))
               return true;
           return false;
        }
        
        // if there are subnodes in the graph assume that they are all going to
        // be ignored unless otherwise said.
        boolean descendentIgnore = true;
        
        Set<NodeObj> tmpSet = new HashSet<NodeObj>();
        tmpSet.addAll(childSet);
        // for all descendents check whether or not they should be ignored.
        for (NodeObj childNode : tmpSet){
           // we && the return value from the descendents with the current
           // decision flag. If it changes to false, the node should not be 
           // ignored.
           boolean descendentIgnoreImmediate = recursiveSanitizeGraph(childNode);
           if (descendentIgnoreImmediate)
        	   childSet.remove(childNode);
           
           descendentIgnore =  descendentIgnoreImmediate && descendentIgnore;
        }
        return shouldIgnore(node) && descendentIgnore;
    }

	
	@Override
	protected void applyRecursion(NodeObj rootNode) {
		
		if (mModuleNameSet.contains(rootNode.getName()))
			return;
		
		mModuleNameSet.add(rootNode.getName());
		
		NodeObj modelNodeObj = new NodeObj(rootNode);

		mCurrentHeadNode = modelNodeObj.getNodeVisit();
		System.out.print("Found refs for " + 
				modelNodeObj.getName() + ": " + mCurrentHeadNode);
		
		
		recursiveSanitizeGraph(modelNodeObj);
				
//			StringBuffer b = new StringBuffer(mModelNodeObj.getName()+ " visit: " + mModelNodeObj.getNodeVisit() + "\n");
//	    	printTree(mModelNodeObj, 0, b);
//	    	System.out.println(b.toString()+"\n\n\n");
    	
		int startCounter = mCounter;
		
		ExtendedNodeObj modelNodeObjExtended = new ExtendedNodeObj(modelNodeObj);
		NodeObj mergedNodeObj = modelNodeObjExtended.mergeWebNodes();
		mergedNodeObj.setIsRoot(Boolean.TRUE);
		recursiveWriteGraph(mergedNodeObj);
		
		System.out.println(" -- size (" + (mCounter - startCounter + 1) + ")");
		
    }
}
