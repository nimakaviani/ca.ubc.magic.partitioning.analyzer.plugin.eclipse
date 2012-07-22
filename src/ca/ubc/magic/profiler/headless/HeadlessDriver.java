//package ca.ubc.magic.profiler.headless;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.SortedSet;
//import java.util.TreeSet;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//import ca.ubc.magic.profiler.dist.model.HostModel;
//import ca.ubc.magic.profiler.dist.model.Module;
//import ca.ubc.magic.profiler.dist.model.ModuleModel;
//import ca.ubc.magic.profiler.dist.model.ModulePair;
//import ca.ubc.magic.profiler.dist.model.execution.CloudExecutionMonetaryCostModel;
//import ca.ubc.magic.profiler.dist.model.interaction.CloudMonetaryCostModel;
//import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;
//import ca.ubc.magic.profiler.dist.model.report.ReportModel;
//import ca.ubc.magic.profiler.dist.transform.FrameBasedModuleCoarsener;
//import ca.ubc.magic.profiler.dist.transform.IModuleCoarsener;
//import ca.ubc.magic.profiler.parser.EntityConstraintParser;
//import ca.ubc.magic.profiler.parser.HostParser;
//import ca.ubc.magic.profiler.parser.JipParser;
//import ca.ubc.magic.profiler.parser.JipRun;
//import ca.ubc.magic.profiler.partitioning.control.alg.simplex.LpSolvePartitionerExtended4Cost;
//
//public class HeadlessDriver {
//	private static int NUM_OF_CORES = 4;
//	private static String profilerTraceXML = "resources/dist-model/20120625-225911.xml";//
//	private static JipRun run;
//	static {
//		try {
//			run = JipParser.parse(profilerTraceXML);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	private static String modExposerXML = "resources/dist-model/moduleconstraints-jforum.xml";
//	private static String hostConfigXML = "resources/dist-model/host-magic2.xml";
//	
//	private static String[] ariesTables = {	"DBBundle:MySQL_orderejb", 
//											"DBBundle:MySQL_accountejb", 
//											"DBBundle:MySQL_quoteejb",
//											"DBBundle:MySQL_holdingejb", 
//											"DBBundle:MySQL_accountprofileejb"};
//	
//	private static String[] tpcwTables = {	
//		"DBBundle:MySQL_address", 
//		"DBBundle:MySQL_author", 
//		"DBBundle:MySQL_cc_xacts",
//		"DBBundle:MySQL_country", 
//		"DBBundle:MySQL_customer",
//		"DBBundle:MySQL_item", 
//		"DBBundle:MySQL_order_line", 
//		"DBBundle:MySQL_orders",
//		"DBBundle:MySQL_shopping_cart", 
//		"DBBundle:MySQL_shopping_cart_line"};
//	
//	private static String[] rubisTables = {	
//		"DBBundle:MySQL_bids", 
//		"DBBundle:MySQL_buy_now", 
//		"DBBundle:MySQL_categories",
//		"DBBundle:MySQL_comments", 
//		"DBBundle:MySQL_ids",
//		"DBBundle:MySQL_items", 
//		"DBBundle:MySQL_old_items", 
//		"DBBundle:MySQL_regions",
//		"DBBundle:MySQL_users"};
//	
//	private static String[] jforumTables = {	
//		"DBBundle:MySQL_jforum_forums", 
//		"DBBundle:MySQL_jforum_groups", 
//		"DBBundle:MySQL_jforum_posts",
//		"DBBundle:MySQL_jforum_users", 
//		"DBBundle:MySQL_jforum_topics",
//		"DBBundle:MySQL_jforum_posts_text", 
//		"DBBundle:MySQL_jforum_roles", 
//		"DBBundle:MySQL_jforum_user_groups",
//		"DBBundle:MySQL_jforum_words"};
//	
//	private static String[] tables = jforumTables;
//	private static SortedSet<ClusterSet> results = new TreeSet<ClusterSet>();
//	private static double better;
//
//	public static void main(String[] args) throws Exception {
//		Set<String> tableSet = new HashSet<String>();
//		tableSet.addAll(Arrays.asList(tables));
//		ReportModel empty = doExperiment(new LinkedList<String>());
//		ReportModel full = doExperiment(Arrays.asList(tables));
//		better = Math.min(empty.getCostModel().getTotalCost(), full.getCostModel().getTotalCost());
//		final ConcurrentLinkedQueue<Set<String>> q = new ConcurrentLinkedQueue<Set<String>>();
//		q.addAll(powerSet(tableSet));
//		List<Thread> threads = new LinkedList<Thread>();
//		for(int i=0; i < NUM_OF_CORES; i++) {
//			threads.add(new Thread(new Runnable() {
//				public void run() {
//					while(!q.isEmpty()) {
//						Set<String> set = q.poll();
//						if(set == null) {
//							break;
//						} else {
//							try {
//								List<String> tableList = new LinkedList<String>();
//								tableList.addAll(set);
//								doExperiment(tableList);
//							} catch(Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}));
//		}
//		for(Thread thread : threads) {
//			thread.start();
//		}
//		for(Thread thread : threads) {
//			thread.join();
//		}
//		for(ClusterSet clusterSet : results) {
//			System.err.println("----------");
//			System.err.println(clusterSet.toString());
//		}
//		toDot("C:/Temp/results.dot", results.last().results);
//	}
//	
//	public static ReportModel doExperiment(List<String> pinTables) throws Exception {
//		ClusterSet clusters = new ClusterSet(pinTables);
//		LpSolvePartitionerExtended4Cost partitioner = new LpSolvePartitionerExtended4Cost();
//		HostParser hParser = new HostParser();
//		EntityConstraintParser eParser = new EntityConstraintParser();
//		
//		IModuleCoarsener fbmc = new FrameBasedModuleCoarsener(eParser.parse(modExposerXML));
//		ModuleModel mModuleModel = fbmc.getModuleModelFromParser(run);
//		HostModel hModel = hParser.parse(hostConfigXML);
//
//		hModel.setNumberOfHosts(2);
//		hModel.setExecutionCostModel(new CloudExecutionMonetaryCostModel());
//		hModel.setInteractionCostModel(new CloudMonetaryCostModel());
//		
//		partitioner.init(mModuleModel, hModel);
//		partitioner.prePartition();
//		
//		for(String table : tables) {
//			partitioner.pinPatternTogether('('+table+").*$");
//		}
//		
//		for(String pinTable : pinTables) {
//			partitioner.pinPatternToSource("(" + pinTable + ").*$");
//		}
//		
//		ReportModel rm = partitioner.partition();
//	
//		clusters.setResults(mModuleModel);
//		Map<ModulePair, InteractionData> map = mModuleModel.getModuleExchangeMap();
//		for(Entry<ModulePair, InteractionData> entry : map.entrySet()) {
//			long count = entry.getValue().getTotalCount();
//			if(count != 0) {
//				Module[] pair = entry.getKey().getModules();
//				Module fst = pair[0];
//				Module snd = pair[1];
//				Cluster fstCluster = clusters.clusters.get(fst);
//				Cluster sndCluster = clusters.clusters.get(snd);
//				
//				if((fstCluster == null) && (sndCluster == null)) {
//					Cluster newCluster = clusters.createCluster();
//					newCluster.addMember(fst);
//					newCluster.addMember(snd);
//					clusters.clusters.put(fst, newCluster);
//					clusters.clusters.put(snd, newCluster);
//				} else if((fstCluster == null) && (sndCluster != null)) {
//					sndCluster.addMember(fst);
//					clusters.clusters.put(fst, sndCluster);
//				} else if((fstCluster != null) && (sndCluster == null)) {
//					fstCluster.addMember(snd);
//					clusters.clusters.put(snd, fstCluster);
//				} else if((fstCluster != null) && (sndCluster != null) && (fstCluster != sndCluster)) {
//					fstCluster.merge(sndCluster);
//				} 
//			}
//		}
//		synchronized(HeadlessDriver.class) {
//			System.err.println("Relative cost: " + ((better - rm.getCostModel().getTotalCost())/better) );
//			System.err.println(clusters.toString());
//			
//			results.add(clusters);
//		}
//		return rm;
//	}
//	
//	private static class ClusterSet implements Comparable<ClusterSet> {
//		Map<Module, Cluster> clusters = new ConcurrentHashMap<Module, Cluster>();
//		Set<Cluster> liveClusters = new HashSet<Cluster>();
//		List<String> pinned;
//		ModuleModel results;
//		
//		public void setResults(ModuleModel results) {
//			this.results = results;
//		}
//		
//		public ClusterSet(List<String> pinned) {
//			this.pinned = pinned;
//		}
//		
//		public Cluster createCluster() {
//			Cluster cluster = new Cluster(this);
//			liveClusters.add(cluster);
//			return cluster;
//		}
//		
//		public double getDiversity() {
//			double size = liveClusters.size();
//			double total = 0; 
//			for(Cluster cluster : liveClusters) {
//				total += cluster.getDiversity();
//			}
//			return total/size;
//		}
//
//		@Override
//		public int compareTo(ClusterSet arg0) {
//			if(this.getDiversity() > arg0.getDiversity()) {
//				return 1;
//			} else if(this.getDiversity() < arg0.getDiversity()) {
//				return -1;
//			} else {
//				return 0;
//			}
//		}
//		
//		public String toString() {
//			return pinned.toString() + "\n\tDiversity: " + String.format("%.4g%n", getDiversity());
//		}
//	}
//	
//	private static class Cluster implements Iterable<Module> {
//		
//		ClusterSet mySet;
//		
//		Cluster(ClusterSet set) {
//			mySet = set;
//		}
//		
//		private Set<Module> members = new HashSet<Module>();
//		
//		public int size() {
//			return members.size();
//		}
//		
//		public void addMember(Module module) {
//			members.add(module);
//		}
//		
//		public void merge(Cluster cluster) {
//			mySet.liveClusters.remove(cluster);
//			members.addAll(cluster.members);
//			for(Module module : cluster) {
//				mySet.clusters.put(module, this);
//			}
//		}
//
//		@Override
//		public Iterator<Module> iterator() {
//			return members.iterator();
//		}
//		
//		public double getDiversity() {
//			double total = members.size();
//			double premise = 0;
//			double cloud = 0;
//			for(Module member : members) {
//				if(member.getPartitionId() == 1) {
//					cloud++;
//				} else if(member.getPartitionId() == 2) {
//					premise++;
//				}
//			}
//			return (total - Math.max(premise, cloud))/total;
//		}
//		
//	}
//	
//	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
//	    Set<Set<T>> sets = new HashSet<Set<T>>();
//	    if (originalSet.isEmpty()) {
//	        sets.add(new HashSet<T>());
//	        return sets;
//	    }
//	    List<T> list = new ArrayList<T>(originalSet);
//	    T head = list.get(0);
//	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
//	    for (Set<T> set : powerSet(rest)) {
//	        Set<T> newSet = new HashSet<T>();
//	        newSet.add(head);
//	        newSet.addAll(set);
//	        sets.add(newSet);
//	        sets.add(set);
//	    }           
//	    return sets;
//	}
//
//	public static void toDot(String fileName, ModuleModel results) throws FileNotFoundException {
//		PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
//		pw.println("graph foo {");
//		printNodes(pw, results);
//		printEdges(pw, results);
//		pw.println("}");
//		pw.close();
//	}
//	
//	private static void printNodes(PrintWriter pw, ModuleModel results) {
//		for(Map.Entry<String, Module> entry : results.getModuleMap().entrySet()) {
//			pw.print("\""+ entry.getKey() +"\"");
//			if(entry.getValue().getPartitionId() == 2) {
//				pw.print(" [style=filled]");
//			}
//			pw.println(";");
//		}
//	}
//	
//	private static void printEdges(PrintWriter pw, ModuleModel results) {
//		for(ModulePair pair : results.getModuleExchangeMap().keySet()) {
//			pw.println("\"" + pair.getModules()[0].getName() + "\" -- \"" + pair.getModules()[1].getName() + "\";");
//		}
//	}
//}
