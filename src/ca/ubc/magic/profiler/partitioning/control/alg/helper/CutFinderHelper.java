package ca.ubc.magic.profiler.partitioning.control.alg.helper;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.keyvalue.DefaultKeyValue;

import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;

public class CutFinderHelper {
	
	/**
	 * ----------------------------------------------------------------------------------
	 * The following is the piece of code that prints cuts in the graph when the model 
	 * is a tree model
	 */
	
	private static int mCounter = 0;
	
	public static void findCuts(final ModuleModel moduleModel) throws Exception {
		findCuts(moduleModel, Boolean.FALSE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void findCuts(final ModuleModel moduleModel, boolean ignoreDBTableCuts) throws Exception {
		
		mCounter = 0;
		Set<Module> rootSet = new HashSet<Module>();
		for (Module m : moduleModel.getModuleMap().values())
        	if (m.isRoot())
    			rootSet.add(m);
        if (rootSet.isEmpty())
        	throw new RuntimeException("No root is defined in the graph, the model potentially the wrong model");
		
		Map<Module, Set<DefaultKeyValue<Module, InteractionData>>> moduleMap = new HashMap<Module, Set<DefaultKeyValue<Module, InteractionData>>>();
        for (Entry<ModulePair, InteractionData> entry : moduleModel.getModuleExchangeMap().entrySet()){
            Module[] mArray = entry.getKey().getModules();
            if (!moduleMap.keySet().contains(mArray[0]))                    
                moduleMap.put(mArray[0], new HashSet<DefaultKeyValue<Module, InteractionData>>());
            if (!moduleMap.keySet().contains(mArray[1]))                    
                moduleMap.put(mArray[1], new HashSet<DefaultKeyValue<Module, InteractionData>>());
            moduleMap.get(mArray[0]).add(new DefaultKeyValue(mArray[1], entry.getValue()));
            moduleMap.get(mArray[1]).add(new DefaultKeyValue(mArray[0], entry.getValue()));
        }
        
        File cutFile = new File("resources/cuts.xml");
        PrintWriter pw = new PrintWriter(cutFile);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        pw.println("<cuts>");
        
        System.out.println(" \n -------------- Cuts -------------- ");
        for (Module root : rootSet){
        	printCuts(moduleMap, root, ignoreDBTableCuts);
//        	printXMLCuts(pw, moduleMap, root, ignoreDBTableCuts);
        }
        System.out.println(" ---------------------------------- \n");
        
        pw.println("</cuts>");
        pw.close();
    }
	
	private static void printCuts(final Map<Module, Set<DefaultKeyValue<Module, InteractionData>>> moduleMap, 
			Module root, boolean ignoreDBTableCuts){
		Set<Module> usedSet = new HashSet<Module>();
        List<Module> waitSet = new ArrayList<Module>();
        Map<String, String> parentMap = new HashMap<String, String>();
        
        Module mainRoot = root;
        waitSet.add(root);
        while (!waitSet.isEmpty()){
        	root = waitSet.get(0);
        	if (moduleMap.get(root) != null){
	        	for (DefaultKeyValue<Module, InteractionData> m : moduleMap.get(root)){
	        		waitSet.add(m.getKey());
	        		parentMap.put(m.getKey().getName(), root.getName());
	        		if (!usedSet.contains(m.getKey())){
	        			if (ignoreDBTableCuts 
	        					&& (root.getName().contains("DBBundle:MySQL_") 
	        						||	m.getKey().getName().contains("DBBundle:MySQL_")))
	        				continue;
	        			if (m.getKey().getPartitionId() != root.getPartitionId()){
	        				System.out.println("Cut (" + (++mCounter) + ") -> Root: " + mainRoot.getName());
	        				System.out.println("\t pr: " + parentMap.get(root.getName()));
	        				System.out.println("\t m1: " + root.getName());
	        				System.out.println("\t m2: " + m.getKey().getName());
	        			}
	        		}
	        	}
	        	
	        	moduleMap.remove(root);
	        	usedSet.add(root);
        	}
        	waitSet.remove(root);
        }
	}
	
	@SuppressWarnings("unused")
	private static void printXMLCuts(PrintWriter pw, final Map<Module, Set<DefaultKeyValue<Module, InteractionData>>> moduleMap, 
			Module root, boolean ignoreDBTableCuts){
		Set<Module> usedSet = new HashSet<Module>();
        List<Module> waitSet = new ArrayList<Module>();
        Map<String, String> parentMap = new HashMap<String, String>();
        
        Module mainRoot = root;
        waitSet.add(root);
        while (!waitSet.isEmpty()){
        	root = waitSet.get(0);
        	if (moduleMap.get(root) != null){
	        	for (DefaultKeyValue<Module, InteractionData> m : moduleMap.get(root)){
	        		waitSet.add(m.getKey());
	        		parentMap.put(m.getKey().getName(), root.getName());
	        		if (!usedSet.contains(m.getKey())){
	        			if (ignoreDBTableCuts 
	        					&& (root.getName().contains("DBBundle:MySQL_") 
	        						||	m.getKey().getName().contains("DBBundle:MySQL_")))
	        				continue;
	        			if (m.getKey().getPartitionId() != root.getPartitionId()){
	        				pw.println("\t<cut id=\"" + (++mCounter) + "\">");
	        				pw.println("\t\t<root>" + mainRoot.getName().substring(0, mainRoot.getName().indexOf("-")) + "</root>");
	        				pw.println("\t\t<pr>" + (parentMap.get(root.getName()) != null ? 
	        						parentMap.get(root.getName()).substring(0, parentMap.get(root.getName()).indexOf("-")) : "null") +"</pr>");
	        				pw.println("\t\t<m1>" + root.getName().substring(0, root.getName().indexOf("-")) + "</m1>");
	        				pw.println("\t\t<m2>" + m.getKey().getName().substring(0, m.getKey().getName().indexOf("-"))+"</m2>");
	        				pw.println("\t</cut>");
	        			}
	        		}
	        	}
	        	
	        	moduleMap.remove(root);
	        	usedSet.add(root);
        	}
        	waitSet.remove(root);
        }
	}
}
