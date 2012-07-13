/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.dist.transform;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.granularity.CodeEntity;
import ca.ubc.magic.profiler.dist.model.granularity.CodeUnitType;
import ca.ubc.magic.profiler.dist.model.granularity.EntityConstraintModel;
import ca.ubc.magic.profiler.dist.transform.CoarseRequestBasedBundleModuleCoarsener.ExtendedNodeObj;
import ca.ubc.magic.profiler.dist.transform.model.NodeObj;
import ca.ubc.magic.profiler.parser.JipFrame;
import ca.ubc.magic.profiler.parser.JipRun;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nima
 */
public class ThreadBasedBundleModuleCoarsener extends BundleModuleCoarsener implements IModuleCoarsener {
    
    protected Long mInteractionId = 0L;
    protected Long mThreadId = 0L;
    
    NodeObj mStart = new NodeObj("start", CodeUnitType.DEFAULT, 0L, 0L);
    NodeObj mEnd = new NodeObj("end", CodeUnitType.DEFAULT, 0L, 0L);
    
    NodeObj mParentNode;
    
    public ThreadBasedBundleModuleCoarsener(EntityConstraintModel constraintModel){
        super(constraintModel);
    }
    
    @Override
    public ModuleModel getModuleModelFromParser(JipRun jipRun) {
        
        if (mConstraintModel == null)
            throw new RuntimeException("No constraint model is defined. Make sure you active module exposing!");
        
        mIgnoreSet = mConstraintModel.getIgnoreSet();
        
        mModuleModel.setName("Profile " + jipRun.getDate());
        for(Long threadId: jipRun.threads()) {
            for (JipFrame f: jipRun.interactions(threadId)) {
                try{
                    // setting up threadId and interactionId to be able to separate
                    // threads and interaction from one another in the dependency graph
                    ++mInteractionId;
                    mThreadId = threadId;
                    
                    // creating the stringbuffer to record the communication between modules
                    // in the application
                    mParentNode = new NodeObj(getFrameModuleName(f), 
                            getFrameModuleType(f), mThreadId, mInteractionId);
                    if (!mStart.getChildSet().contains(mParentNode)) {
                            mStart.getChildSet().add(mParentNode);
                    }else {
                            mParentNode = mStart.getChild(mParentNode);
                    }
                    visitFrameForModuling(f);
                }catch(Exception e){
                        System.out.println(e.getMessage());
                }
            }
        }
      
        //recursively initializing modules and interactions in the graph model
        initializeModels();
        
        return mModuleModel;
    }        
    
    @Override
    public void visitFrameForModuling(JipFrame frame){
        StringBuffer b = new StringBuffer(getFrameModuleName(frame)+"\n");
        mParentNode.addVertex(Double.valueOf(frame.getNetTime()), frame.getCount());
        
        populateFrame(frame, mParentNode, mThreadId, mInteractionId, 0, b);
    }
    
    protected String populateFrame(JipFrame f, NodeObj rootNode, Long threadId, Long interactionId, int level, StringBuffer b) {
        
        Iterator<JipFrame> itr = f.getChildren().iterator();

        while (itr.hasNext()){

            NodeObj nextNode  = rootNode;
            int newLevel = level;

            JipFrame childFrame = itr.next();
            NodeObj childNode = new NodeObj(getFrameModuleName(childFrame), 
                    getFrameModuleType(childFrame), threadId, interactionId);
            
            
            // the following few lines makes sure we do not consider the time spent on compiling
            // the jsp files as part of the time needed to respond to application requests. It 
            // ignores all calls compiling JSP documents.
            boolean ignoreCompiler = Boolean.FALSE;
            for (String filter : Constants.JSP_COMPILER_FILTER_SET)
                if (childFrame.getMethod().toString().contains(filter)){
                    ignoreCompiler = Boolean.TRUE;
                    break;
                }
            if (ignoreCompiler)
                continue;
            
            if (!childNode.equals(rootNode)){

                // going one level deeper in the tree.
                newLevel += 1;

                // proceeding to the next level in the call tree
                nextNode = childNode;

                // collecting size of communicated data and the number of times data is communicated
                double toParentWeight = childFrame.getDataToParent();
                double fromParentWeight = childFrame.getDataFromParent();
                long toParentCount  = childFrame.getCountToParent();
                long fromParentCount = childFrame.getCountFromParent();

                if (rootNode.getChildSet().contains(childNode)){
                    NodeObj nd = rootNode.getChild(childNode);
                    nd.addEdge(fromParentWeight, toParentWeight, fromParentCount, toParentCount);
                    nd.addVertex(Double.valueOf(childFrame.getNetTime()),
                            childFrame.getCount());
                    nextNode = nd;
                } else {
                    // registering the child node in the set of nodes visited
                    childNode.addEdge(fromParentWeight, toParentWeight, fromParentCount, toParentCount);
                    childNode.addVertex(Double.valueOf(childFrame.getNetTime()),
                            childFrame.getCount());
                    rootNode.getChildSet().add(childNode);

                    // The following adds to the depth of the graph
                    getSubTree(rootNode, nextNode, level, b);
                }
            }else {
                rootNode.addVertex(Double.valueOf(childFrame.getNetTime()), childFrame.getCount());
            }
            populateFrame(childFrame, nextNode, threadId, interactionId, newLevel, b);
        }
        return b.toString();
    }
    
   protected void getSubTree(NodeObj parentNode, NodeObj childNode, int level, StringBuffer b){
        for (int i=0; i<level; i++) 
                b.append("| ");
        b.append("+--"); 			
        b.append(childNode.toString()).append( 
        		((childNode.getId() != null) ? (" :: t=" + childNode.getId()) : "")).append(
        		((childNode.getInteractionId() != null) ? (" :: i=" + childNode.getInteractionId()) : "")).append(
        		" --> w:").append(childNode.getVertexWeight()).append( 
                ", count:").append(childNode.getCount()).append( 
                ", dfp:").append(childNode.getEdge4ParentWeight()).append( 
                ", dtp:").append(childNode.getEdge2ParentWeight()).append(
                ", cfp:").append(childNode.getEdge4ParentCount()).append( 
                ", ctp:").append(childNode.getEdge2ParentCount()).append(
                ", visit:").append(childNode.getNodeVisit()).append(" \n");
    }
   
   protected boolean recursiveWriteNode(NodeObj node, int id) {
        Set<NodeObj> childSet = node.getChildSet();
        
        //checks to see if any subnode is remained in the list
        if (childSet == null || childSet.isEmpty()){
           
           // if there is no subnode and this node should be ignored, ignore it
           if (shouldIgnore(node))
               return true;
            
           // otherwise add it to the module map and indicate that it hasn't
           // been ignored by returning false
           Module m = mModuleModel.getModuleMap().get(node.getName()+"_"+id);
           if (m == null){
               m = new Module(node.getName()+"_"+id, node.getType());
               m.setExecutionCost(node.getVertexWeight());
               m.setExecutionCount(node.getCount());
               mModuleModel.getModuleMap().put(m.getName(), m);
           }else {
               m.addExecutionCost(node.getVertexWeight());
               m.addExecutionCount(node.getCount());
           }
                
           return false;
        }
        
        // if there are subnodes in the graph assume that they are all going to
        // be ignored unless otherwise said.
        boolean descendentIgnore = true;
        
        // for all descendents check whether or not they should be ignored.
        for (NodeObj childNode : childSet){
           // we && the return value from the descendents with the current
           // decision flag. If it changes to false, the node should not be 
           // ignored.
           boolean descendentIgnoreImmediate = recursiveWriteNode(childNode, id);
           
           descendentIgnore =  descendentIgnoreImmediate && descendentIgnore;
        }
        descendentIgnore = shouldIgnore(node) && descendentIgnore;
          
        // if there is a subnode not ignored, the descendent is not ignored either
        if (!descendentIgnore){
               Module m = mModuleModel.getModuleMap().get(node.getName()+"_"+id);
               if (m == null){
                   m = new Module(node.getName()+"_"+id, node.getType());
                   m.setExecutionCost(node.getVertexWeight());
                   m.setExecutionCount(node.getCount());
                   mModuleModel.getModuleMap().put(m.getName(), m);
               }else {
                   m.addExecutionCost(node.getVertexWeight());
                   m.addExecutionCount(node.getCount());
               }
        }
        
        return descendentIgnore;
    }

    protected boolean recursiveWriteEdge(NodeObj node, int id){
        
        Set<NodeObj> childSet = node.getChildSet();
        if (childSet == null || childSet.isEmpty()){
            
            if (shouldIgnore(node))
                return true;
            return false;
        }
        
        // if there are subnodes in the graph assume that they are all going to
        // be ignored unless otherwise said.
        boolean descendentIgnoreAll = true;
        for (NodeObj childNode : childSet){
            
            boolean descendentIgnore = recursiveWriteEdge(childNode, id);
            
            descendentIgnoreAll = descendentIgnoreAll && descendentIgnore;
            
            if (!descendentIgnore){
                Module m1 = mModuleModel.getModuleMap().get(node.getName()+"_"+id);
                Module m2 = mModuleModel.getModuleMap().get(childNode.getName()+"_"+id);
                if (m1 == null || m2 == null)
                	continue;
                addDataExchange(new ModulePair(m1, m2), childNode.getEdge4ParentWeight().longValue(), 
                    childNode.getEdge2ParentWeight().longValue(), 
                    childNode.getEdge4ParentCount().longValue(),
                    childNode.getEdge2ParentCount().longValue()); 
            }
        }
        if (shouldIgnore(node) && descendentIgnoreAll)
            return true;
        return false;
    }
    
    protected void printTree(NodeObj parentNode, int level, StringBuffer b){
        
        Set<NodeObj> childSet = parentNode.getChildSet();
        if (childSet == null || childSet.isEmpty())
                return;
        for (NodeObj childNode : childSet) {
        
            getSubTree(parentNode, childNode, level, b);
            printTree(childNode, level+1, b);
        }
    }
    
    protected boolean shouldIgnore(NodeObj node){
         if (mIgnoreSet != null)
                for(CodeEntity entity : mIgnoreSet)
                    if (entity.getEntityPattern().matches(node.getName(), null, null))
                        return true;
         return false;
    }
    
    protected void initializeModels(){
        NodeObj rootNode = mStart;
        
        try {
	        if (!mConstraintModel.getRootEntityList().isEmpty())
	        	for (List<CodeEntity> l : mConstraintModel.getRootEntityList()){
	        		for (NodeObj node : rootNode.getChildSet()) {
			            extractChildNodeAndApplyRecursion(node, l);
	        		}
	        	}
	        else
	        	applyRecursion(rootNode);
        }catch (Exception e){
        	e.printStackTrace();
        	throw new RuntimeException("Initializing the module models failed");
        }
        
        postRecursion();
    }
    
    /**
     * A method allowing for post analysis of the NodeObj model generated
     * from the set of all frame coming into the model.
     */
    protected void postRecursion(){}

    protected void applyRecursion(NodeObj rootNode) {
        int i = 0;
        
//        StringBuffer b = new StringBuffer(i + " :: " + rootNode.getName()+ " visit: " + rootNode.getNodeVisit() + "\n");
//        printTree(rootNode, 0, b);
//        System.out.println(b.toString()+"\n\n\n");
        
        rootNode = checkForAddingSyntheticNode(i++, rootNode);
       
        recursiveWriteNode(rootNode, 0);
        recursiveWriteEdge(rootNode, 0);
    }
    
    protected NodeObj checkForAddingSyntheticNode(int i, NodeObj returnedNode) {
        if (mConstraintModel.getConstraintSwitches().isSyntheticNodeActivated()){
        	NodeObj syntheticNodeObj = new NodeObj(Constants.SYNTHETIC_NODE + "_" + i, 
                    CodeUnitType.DEFAULT, 0L, 0L);
            syntheticNodeObj.getChildSet().add(returnedNode);
            returnedNode.addEdge(0.0, 0.0, 1L, 1L);
            returnedNode = syntheticNodeObj;
        }
        return returnedNode;
    }
    
    private NodeObj extractChildNodeAndApplyRecursion(NodeObj rtNode, List<CodeEntity> rootEntityList){
        
        if (rootEntityList.get(0).getEntityPattern().matches(rtNode.getName(), null, null))
        	return rtNode;
        
        NodeObj rootNode = null;
        for (NodeObj node : rtNode.getChildSet()){
        	rootNode = extractChildNodeAndApplyRecursion(node, rootEntityList);
        	if (rootNode != null)
        		applyRecursion(rootNode);;
        }
        return null;
    }
    
    protected boolean isNonReplicable(String nodeName){
        return isNodeNameMatchedInEntitySet(mConstraintModel.getNonReplicableSet(), nodeName);
    }
    
    protected boolean isReplicable(String nodeName){
        boolean isReplicable = isNodeNameMatchedInEntitySet(mConstraintModel.getReplicableSet(), nodeName);
        if (isNonReplicable(nodeName) && isReplicable)
            throw new RuntimeException("Node cannot be both replicable and non-replicable: " + nodeName);
        return isReplicable;
    }
    
    protected boolean isNodeNameMatchedInEntitySet(Set<CodeEntity> entitySet, String nodeName){
        for (CodeEntity entity : entitySet)
            if (entity.getEntityPattern().matches(nodeName, null, null))
                return true;
        return false;
    }
}
