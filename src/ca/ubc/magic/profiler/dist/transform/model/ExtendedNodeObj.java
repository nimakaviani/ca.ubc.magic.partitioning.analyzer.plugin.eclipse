package ca.ubc.magic.profiler.dist.transform.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.control.Util;
import ca.ubc.magic.profiler.dist.model.granularity.CodeUnitType;

public class ExtendedNodeObj extends NodeObj {
	
	private static final String ARIES_HEAD_DATA_ROOT = "org.apache.aries.samples.ariestrader.core";
    private static final String ARIES_HEAD_DATA_ROOT_TOMCAT = "jdbcwrapper";
    private static final String RUBIS_HEAD_DATA_ROOT = "com.notehive.osgi.hibernate-samples.hibernate-classes";
    private static final String HEAD_DATA_ROOT = ARIES_HEAD_DATA_ROOT_TOMCAT;
    
    private static final Set<String> ARIES_FIXED_DATA_NODE_SET = new HashSet<String>(
            Arrays.asList(new String[]{
                "AccountDataBeanImpl",
                "QuoteDataBeanImpl",
                "AccountProfileDataBeanImpl",
                "OrderDataBeanImpl",
                "HoldingDataBeanImpl"
            }));
    private static final Set<String> ARIES_FIXED_DATA_NODE_SET_TOMCAT = new HashSet<String>(
            Arrays.asList(new String[]{
//                "accountejb",
//                "quoteejb",
//                "accountprofileejb",
//                "orderejb",
//                "holdingejb"
            	"DBBundle:MySQL"
            }));
    private static final Set<String> RUBIS_FIXED_DATA_NODE_SET = new HashSet<String>(
            Arrays.asList(new String[]{
                "user-session",
                "categories-session",
                "item-session",
                "region-session",
                "comment-session",
                "bid-session",
                "buy-session",
                "hibernate-classes"
            }));
    private static final Set<String> FIXED_DATA_NODE_SET = ARIES_FIXED_DATA_NODE_SET_TOMCAT;
    
    private static final Set<String> ARIES_FIXED_LOGIC_NODE_SET = new HashSet<String>(
            Arrays.asList(new String[]{
                "TradeServletAction:doSell",
                "TradeServletAction:doBuy",
                "TradeServletAction:doWelcome",
                "TradeServletAction:doHome",
                "TradeServletAction:doLogin",
                "TradeServletAction:doLogout",
                "TradeServletAction:doQuotes",
                "TradeServletAction:doProfile"
            }));
    private static final Set<String> RUBIS_FIXED_LOGIC_NODE_SET = new HashSet<String>(
            Arrays.asList(new String[]{
                "web-searchbycat",
                "web-sellitemform",
                "web-aboutme",
                "web-searchbyreg",
                "web-viewitem",
                "web-putcomment",
                "web-viewbidhistory",
                "web-viewuserinfo",
                "web-browsereg",
                "web-browsecat"
            }));
    private static final Set<String> FIXED_LOGIC_NODE_SET = ARIES_FIXED_LOGIC_NODE_SET;
    
    protected static boolean mIsDataRoot = Boolean.FALSE;
    
    protected NodeType  mNodeType  = NodeType.NULL;
	protected NodeState mNodeState = NodeState.LOOSE;
    
//    protected Set<? extends NodeObj> mChildSet;
    protected Set<String> mNameSet;
    
    public ExtendedNodeObj(NodeObj node){
    	this(node, Boolean.TRUE);
    }
    
    public ExtendedNodeObj(NodeObj node, boolean copyChildren){
        this(node.getName(), 
                node.getType(), 0L, 0L);            
        this.addEdge(node.getEdge4ParentWeight(), node.getEdge2ParentWeight(),
                node.getEdge4ParentCount(), node.getEdge2ParentCount());
        this.setVertex(node.getVertexWeight(), node.getCount());
        this.setIsRoot(node.isRoot());
        
        _childNodeSet  = new HashSet<NodeObj>();
        if (copyChildren)
        	_childNodeSet.addAll(node.getChildSetAsList());
        mNameSet   = new HashSet<String>();
        
        mNameSet.add(node.getName());
        mNodeState = updateState(node.getName());
        if (mNodeState == NodeState.FIXED)
            _name = node.getName();
    }
    
    private NodeState updateState(String name){
        for (String s : FIXED_DATA_NODE_SET)
            if (name.contains(s))
                return NodeState.FIXED;
        for (String s : FIXED_LOGIC_NODE_SET)
            if (name.contains(s))
                return NodeState.FIXED;
        return NodeState.LOOSE;
    }
    
    private NodeState updateState(ExtendedNodeObj p, ExtendedNodeObj c){
        if (p.mNodeState == NodeState.FIXED || c.mNodeState == NodeState.FIXED)
            return NodeState.FIXED;
        return NodeState.LOOSE;
    }
    
    private ExtendedNodeObj(String name, CodeUnitType type, Long id, Long interactionId){
		super(name, type, id, interactionId);
	}
    
    @SuppressWarnings("unchecked")
	@Override
    public Set getChildSet(){
        return _childNodeSet;
    }
    
    public void merge(ExtendedNodeObj node){
        this.addVertex(node.getVertexWeight(), node.getCount());
        this.mNameSet.addAll(node.mNameSet);
        // removing the node from the list of child nodes of a method
        this._childNodeSet.remove(node);                
        // adding all the childs of the child as the childs of the parent
        // thus merging the child node into the original node.
        mergeChilds(node.getChildSet());
        
        if (isDataNode(node) && !isDataNode(this) && mIsDataRoot){
            this.mNodeState = this.updateState(this, node);
            this.mNodeType  = node.mNodeType;
            this._name = node.getName();
        }
        if (this.mNodeType == NodeType.LOGIC)
            this._type = CodeUnitType.METHOD;
        else if (this.mNodeType == NodeType.DATA)
            this._type = CodeUnitType.CLASS;
        else
            this._type = CodeUnitType.COMPONENT;
    }
    
    public void merge(Set<NodeObj> childNodes){
        Set<ExtendedNodeObj> tmpChildDataSet = new HashSet<ExtendedNodeObj>();
        Set<ExtendedNodeObj> tmpChildNonDataSet = new HashSet<ExtendedNodeObj>();
        for (NodeObj child : childNodes){
            ExtendedNodeObj childObj = (ExtendedNodeObj) child;
            if (childObj.mNodeType == NodeType.DATA)
                tmpChildDataSet.add(childObj);
            else
                tmpChildNonDataSet.add(childObj);
        }
        for (ExtendedNodeObj tmpObj : tmpChildNonDataSet)
            this.merge(tmpObj);
        
        if (tmpChildDataSet.size() > 0 && tmpChildDataSet.size() < 2 && !this.getName().contains(HEAD_DATA_ROOT))
            for (ExtendedNodeObj childObj : tmpChildDataSet){
                
//                Printouts for the purpose of testing node merges
//                System.out.println(childObj.getName() + " -----> " + this.getName() + 
//                      " dfp: " + childObj.getEdge4ParentWeight() +
//                      " dtp: " + childObj.getEdge2ParentWeight());
                
                this.merge(childObj);
                this.addEdge(childObj.getEdge4ParentWeight(), childObj.getEdge2ParentWeight(),
                        childObj.getEdge4ParentCount(), childObj.getEdge2ParentCount());
            }
    }
    
    public void mergeChilds(Set<? extends NodeObj> otherChildSet){
        for (NodeObj otherChild : otherChildSet){
            
            if (otherChild == null)
                continue;
            
            boolean found = Boolean.FALSE;
            Set<NodeObj> tmpChildSet = new HashSet<NodeObj>();
            tmpChildSet.addAll(getChildSet());
            for (NodeObj child : tmpChildSet){
                
                if (child == otherChild)
                    continue;
                
                if (child.getName().equals(otherChild.getName())){
                    ExtendedNodeObj childObj = new ExtendedNodeObj(child);
                    
//                    Printouts for the purpose of testing node merges                        
//                    System.out.println(otherChild.getName() + " -----> " + childObj.getName() + 
//                        " dfp: " + otherChild.getEdge4ParentWeight() +
//                        " dtp: " + otherChild.getEdge2ParentWeight());
                    
                    childObj.merge(new ExtendedNodeObj(otherChild));
                    childObj.addEdge(otherChild.getEdge4ParentWeight(), otherChild.getEdge2ParentWeight(),
                        otherChild.getEdge4ParentCount(), otherChild.getEdge2ParentCount());
                    
                    getChildSet().remove(child);
                    getChildSet().add(childObj);
                    
                    found = Boolean.TRUE;
                    break;
                }
            }
            if (!found)
                getChildSet().add(otherChild);
        }
    }
    
    public void mergeChild(ExtendedNodeObj nodeObj){   
        if (nodeObj != null){
            Set tmpSet = new HashSet();
            tmpSet.add(nodeObj);
            mergeChilds(tmpSet);
        }
    }
    
    public String getCoarsenedNodeNames(){
        StringBuilder builder = new StringBuilder();
        for (String s : mNameSet)
            builder.append("\t").append(s).append("<->");
        return builder.toString();
    }        
    
    public NodeObj mergeWebNodes(){
    	ExtendedNodeObj root = new ExtendedNodeObj(this, Boolean.FALSE);
    	Set<NodeObj> tmpSet = new HashSet<NodeObj>(getChildSet());
        Set<NodeObj> tmpChildSet = new HashSet<NodeObj>();
        for (NodeObj childObj : tmpSet){
        	if (root.getChildSet().contains(childObj))
        		continue;
        	ExtendedNodeObj tmpObj  = new ExtendedNodeObj(childObj);
        	ExtendedNodeObj retObj  = (ExtendedNodeObj) tmpObj.mergeWebNodes();
            if (isWebNode(retObj) && isWebNode(root))
                root.merge(retObj);
            else 
            	tmpChildSet.add(retObj);            	
        }
        root.mergeChilds(tmpChildSet);
        return root;
    }
    
    private static boolean isWebNode(NodeObj node){
    	for (String s : Constants.WEB_NODES){
    		if (node.getName().contains(s))
    			return Boolean.TRUE;
    	}
    	return false;
    }
    
    public static ExtendedNodeObj getExtendedNodeObj(NodeObj node){
        ExtendedNodeObj nodeObj = new ExtendedNodeObj(node);
        if (isDataNode(nodeObj))
            nodeObj.mNodeType = NodeType.DATA;
        else 
            nodeObj.mNodeType = NodeType.LOGIC;
        return nodeObj;
    }
    
    public static boolean isDataNode(final ExtendedNodeObj nodeObj){
        for (String s : FIXED_DATA_NODE_SET)
            if (nodeObj.getName().contains(s))
                return true;
        return false;
    }
    
    public NodeType getNodeType() {
		return mNodeType;
	}

	public void setNodeType(NodeType mNodeType) {
		this.mNodeType = mNodeType;
	}

	public NodeState getNodeState() {
		return mNodeState;
	}

	public void setNodeState(NodeState mNodeState) {
		this.mNodeState = mNodeState;
	}
    
    protected enum NodeType {
        NULL,
        DATA,
        LOGIC
    }

    protected enum NodeState {
        LOOSE,
        FIXED
    }
}

