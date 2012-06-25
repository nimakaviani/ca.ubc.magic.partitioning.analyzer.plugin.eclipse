package ca.ubc.magic.profiler.dist.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.transform.model.NodeObj;

public class Util {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap deepClone(Map map) {
        HashMap newone = (HashMap) ((HashMap)map).clone();
        Iterator it = newone.keySet().iterator();
        while (it.hasNext()) {
            Object newkey = it.next();
            Object deepobj = null, newobj = newone.get(newkey);
            if (newobj instanceof HashMap)
                deepobj = deepClone((HashMap)newobj);
            else if (newobj instanceof String)
                deepobj = (Object)new String((String)newobj);
            else if (newobj instanceof Vector)
                deepobj = ((Vector)newobj).clone();
            else if (newobj instanceof Module)
                deepobj = new Module((Module) newobj);
            else if (newobj instanceof Double)
            	deepobj = new Double((Double) newobj);
            else if (newobj instanceof Long)
            	deepobj = new Long((Long) newobj);
            else if (newobj instanceof Integer)
            	deepobj = new Integer((Integer) newobj);
            newone.put(newkey, deepobj);
        }
        return newone;
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList deepClone(List list){
		ArrayList newone = new ArrayList();
		Iterator it = list.iterator();
		while (it.hasNext()) {
            Object newobj = it.next();
            Object deepobj = null;
            if (newobj instanceof String)
                deepobj = (Object)new String((String)newobj);
            else if (newobj instanceof Vector)
                deepobj = ((Vector)newobj).clone();
            else if (newobj instanceof Module)
                deepobj = new Module((Module) newobj);
            else if (newobj instanceof NodeObj)
            	deepobj = new NodeObj((NodeObj) newobj);
            else if (newobj instanceof Double)
            	deepobj = new Double((Double) newobj);
            else if (newobj instanceof Long)
            	deepobj = new Long((Long) newobj);
            else if (newobj instanceof Integer)
            	deepobj = new Integer((Integer) newobj);
            newone.add(deepobj);
		}
		return newone;
	}
}