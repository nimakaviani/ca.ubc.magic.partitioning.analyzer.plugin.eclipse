/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.simulator.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import ca.ubc.magic.profiler.dist.control.Constants;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.HostPair;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleHost;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.TwoHostHelper;
import ca.ubc.magic.profiler.dist.model.cost.conversion.CostConversionSingleton;
import ca.ubc.magic.profiler.dist.model.execution.CloudExecutionMonetaryCostModel;
import ca.ubc.magic.profiler.dist.model.execution.ExecutionTimeCostModel;
import ca.ubc.magic.profiler.dist.model.interaction.AvgTransmissionTimeCostModel;
import ca.ubc.magic.profiler.dist.model.interaction.CloudMonetaryCostModel;
import ca.ubc.magic.profiler.dist.model.interaction.IInteractionCostModel;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionCost;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionData;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionRate;
import ca.ubc.magic.profiler.dist.model.report.ReportModel;

/**
 *
 * @author nima
 */
public class CostAnalyzerHelper {
    
    /**
     * The analyzeCosts method is a analytic method helping to measure how much the deployment costs
     * and what are the factors affecting the cost of deployment. This gives a better look into the
     * internals of the deployment when the partitioning is performed.
     */
    public static ReportModel analyzeCosts(String name, final ModuleModel moduleModel, final HostModel hostModel){
        
        ReportModel report = new ReportModel(hostModel.getHostMap().values());
        
        long   total2PremiseData = 0,  total2CloudData = 0;
        
        List<IInteractionCostModel> costModelList = new ArrayList<IInteractionCostModel> (
        		(Collection<? extends IInteractionCostModel>) Arrays.asList(new IInteractionCostModel[] {
        			new AvgTransmissionTimeCostModel(),
        			new CloudMonetaryCostModel()
        		}));
        
        // costs for each of the cost models provided in the cost model list above.
        // The second dimension indicates the two values for cost2Cloud(index 0), and cost2Premise(index1)
        double[][] costs = new double[costModelList.size()][2];
        
        final IInteractionCostModel actualHostCostModel = hostModel.getInteractionCostModel();
        
        // measuring cost for each of the cloud monetary cost (index 0) and transmission time (index 1)
        for (IInteractionCostModel costModel : costModelList){
        	
        	int costIndex = costModelList.indexOf(costModel);
        	
        	// settting the new CostModel for the HostModel to assess the costs
        	hostModel.setInteractionCostModel(costModel);
        	
	        // updating the partition ids for the module exchange map if it is not already updated
	        for (Entry<ModulePair, InteractionData> e : moduleModel.getModuleExchangeMap().entrySet()){
	            
	            ModulePair mp = e.getKey();
	            
	            mp.getModules()[0].setPartitionId(moduleModel.getModuleMap().get(mp.getModules()[0].getName()).getPartitionId());
	            mp.getModules()[1].setPartitionId(moduleModel.getModuleMap().get(mp.getModules()[1].getName()).getPartitionId());
	            
	            if (mp.getModules()[0].getPartitionId() == Constants.INVALID_PARTITION_ID ||
	                    mp.getModules()[1].getPartitionId() == Constants.INVALID_PARTITION_ID)
	                continue;
	            
	            // Calculating the amount of data going over the wire for the deployment.
	            InteractionData iData = e.getValue();
	            HostPair hph1h2 = new HostPair(TwoHostHelper.getSourceHost(hostModel), TwoHostHelper.getTargetHost(hostModel));
	            HostPair hph2h1 = new HostPair(TwoHostHelper.getTargetHost(hostModel), TwoHostHelper.getSourceHost(hostModel));
	            
	            if (!mp.getModules()[0].getPartitionId().equals(mp.getModules()[1].getPartitionId())){
	                if (iData == null)
	                    throw new RuntimeException("Error! There is no interaction data for the pair: " + mp.toString());
	                if (mp.getModules()[0].getPartitionId() > mp.getModules()[1].getPartitionId()){
	                	InteractionCost cost = hph2h1.getInteractionCost(hostModel, iData);
	                    total2PremiseData += CostConversionSingleton.getInstance().interactionConvert(
	                                iData.getFromParentData(), iData.getFromParentCount());
	                    total2CloudData   += CostConversionSingleton.getInstance().interactionConvert(
	                            iData.getToParentData(), iData.getToParentCount()); 
	                    
	                    // total2CloudCost
	                    costs[costIndex][0] += cost.getH2toH1Cost();
	                    // total2PremiseCost 
	                    costs[costIndex][1] += cost.getH1toH2Cost();
	                } else{
	                	InteractionCost cost = hph1h2.getInteractionCost(hostModel, iData);
	                    total2CloudData   += CostConversionSingleton.getInstance().interactionConvert(
	                            iData.getFromParentData(), iData.getFromParentCount());
	                    total2PremiseData += CostConversionSingleton.getInstance().interactionConvert(
	                            iData.getToParentData(), iData.getToParentCount());
	                    
	                    // total2CloudCost
	                    costs[costIndex][0] += cost.getH1toH2Cost();
	                    // total2PremiseCost 
	                    costs[costIndex][1] += cost.getH2toH1Cost();
	                }
	            }
	        }
        }
        
        // total2CloudData and total2PremiseData are calculated for as many times
        // as the number of cost models in the array so we divide the final values
        // by the size of CostModelList
        total2CloudData   /= costModelList.size();
        total2PremiseData /= costModelList.size();
        
        // resetting the CostModel for the HostModel to what it was prior to any 
        // extra cost assessment.
        hostModel.setInteractionCostModel(actualHostCostModel);
                
        
        
        // Execution time based on simulation results.
        double totalExecTime = 0.0;
        // Execution cost based on the cost model provided from the cloud
        double totalExecCost = 0.0;
        // getting the percentage of partitions
        double totalCount = 0.0;
        double p1Count = 0.0;
        double p2Count = 0.0;
        for(Module m : moduleModel.getModuleMap().values()){
            totalCount++;
            
            if (m.getPartitionId() == Constants.INVALID_PARTITION_ID)
                continue;
            else if (m.getPartitionId() == 1)
                p1Count++;
            else if (m.getPartitionId() == 2)
                p2Count++;
            else
                throw new RuntimeException("Invalid partition id for module " + m.getName());
            
            // calculating the overall time of execution
            hostModel.setExecutionCostModel(new  ExecutionTimeCostModel());
            ModuleHost mh = new ModuleHost(m, hostModel.getHostMap().get(m.getPartitionId()));
            totalExecTime += mh.setExecutionCost(hostModel, hostModel.getDefaultHost());
            
            hostModel.setExecutionCostModel(new CloudExecutionMonetaryCostModel());
            totalExecCost += mh.setExecutionCost(hostModel, hostModel.getDefaultHost());
        }
        System.out.println(name + " (" + p1Count + " / " + totalCount + ") :: part1% = " + (p1Count / totalCount));
        System.out.println(name + " (" + p2Count + " / " + totalCount + ") :: part2% = " + (p2Count / totalCount));
        System.out.println("\t Total data going to Cloud     : " + total2CloudData + "\t\t(time: " + costs[0][0] + "\t\t money: " + costs[1][0] + " $)" );
        System.out.println("\t Total data going to Premise   : " + total2PremiseData +  "\t\t(time: " + costs[0][1] + "\t\t money: " + costs[1][1] + " $)" );
        System.out.println("\t Total data going over the wire: " + (total2CloudData + total2PremiseData) +  
        		"\t\t(time: " + (costs[0][0] + costs[0][1]) + "\t\t money: " + (costs[1][0] + costs[1][1])  + " $)");
        System.out.println("\t Total Execution               : " + "\t\t(time: " + totalExecTime + "\t money: " + totalExecCost  + " $)");
        System.out.println("\t Total Execution + DataTransfer: " + "\t\t(time: " + (totalExecTime + costs[0][0] + costs[0][1]) + 
        		"\t money: " + (totalExecCost + costs[1][0] + costs[1][1])  + " $)");
        
        if (actualHostCostModel instanceof CloudMonetaryCostModel){
	        report.getCostModel().setCommunicationCost(costs[1][0] + costs[1][1]);
	        report.getCostModel().setExecutionCost(totalExecCost);
        }else if (actualHostCostModel instanceof AvgTransmissionTimeCostModel){
        	report.getCostModel().setCommunicationCost(costs[0][0] + costs[0][1]);
	        report.getCostModel().setExecutionCost(totalExecTime);
        }
        
        return report;
    }
    
    public static void main(String args[]) {
    	
    	double baseHost   = 2.5 * 10E9;
    	double targetHost = 3.3 * 10E9;
    	
    	double baseHostCost = 0.5 / (3600 * 1000);
    	double targetHostCost = 0.12 / (3600 * 1000);
    	
    	double interactionCost = 0.12 / (1000 * 1.0E6);
    	
    	double bandwidth = 100 * 1E6;
    	double handshakecost = 1;
    	double liftinglowering = 0;
    	double latency = 80;
    	
    	double scale = 1.0E3;
    	
    	double ratioP = 1;
	    double ratioC = 1; //baseHost / targetHost;     
    	
    	double usageP = 3.4;
    	double usageC = 1452;
    	long   bytes = 17873;
    	long   count = 1;
	    
	    double cpuCost = (usageP * ratioP) * baseHostCost + (usageC * ratioC) * targetHostCost;
	    
	    double dtCpuCost = InteractionRate.getCloudCPUMonetaryCost(bytes, count, bandwidth, handshakecost, liftinglowering, latency, targetHostCost, scale, interactionCost);
	    double dtTrfCost = InteractionRate.getCloudDataMonetaryCost(bytes, count, interactionCost);
	    
	    System.out.println("CPU Usage Cost: " + cpuCost);
	    System.out.println("Transfer CPU  Cost: " + dtCpuCost);
	    System.out.println("Transfer Data Cost: " + dtTrfCost);
    	
    }
}
