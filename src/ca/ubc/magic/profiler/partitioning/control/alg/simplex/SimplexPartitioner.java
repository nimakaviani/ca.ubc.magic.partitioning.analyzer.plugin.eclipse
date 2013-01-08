/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.alg.simplex;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.SimplexSolver;

import ca.ubc.magic.profiler.dist.model.Host;
import ca.ubc.magic.profiler.dist.model.HostModel;
import ca.ubc.magic.profiler.dist.model.HostPair;
import ca.ubc.magic.profiler.dist.model.Module;
import ca.ubc.magic.profiler.dist.model.ModuleHost;
import ca.ubc.magic.profiler.dist.model.ModuleModel;
import ca.ubc.magic.profiler.dist.model.ModulePair;
import ca.ubc.magic.profiler.dist.model.ModulePairHostPair;
import ca.ubc.magic.profiler.dist.model.TwoHostHelper;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionCost;
import ca.ubc.magic.profiler.dist.model.interaction.InteractionCostHelper;
import ca.ubc.magic.profiler.dist.transform.IColocationFilter;
import ca.ubc.magic.profiler.dist.transform.IInteractionFilter;
import ca.ubc.magic.profiler.dist.transform.IModuleFilter;
import ca.ubc.magic.profiler.partitioning.control.alg.AbstractPartitioner;
import ca.ubc.magic.profiler.partitioning.control.alg.helper.CutFinderHelper;

/**
 *
 * @author nima
 */
public class SimplexPartitioner extends AbstractPartitioner {
    
    protected SimplexModel mSimplexModel;  
    protected SimplexSolver solver;
    protected RealPointValuePair solution;
    
    public static final int MAX_ITERATIONS = 2500;
    
    @Override
    public void init (ModuleModel mModel, HostModel hModel){
        super.init(mModel, hModel);      
        if (mHostModel.getNumberOfHosts() != 2)
            throw new RuntimeException("Simplex partitioner can be applied to 2 hosts only");
        mSimplexModel = new SimplexModel(mSize);
    }

    @Override
    public void doPartition() {
        try {
            solver = new SimplexSolver();
            solver.setMaxIterations(getMaxIterations());
            solution = solver.optimize(
                    mSimplexModel.getObjectiveFunction(), mSimplexModel.getConstraints(), 
                    GoalType.MINIMIZE, true);
            
            int i = 0;            
            for (double d : solution.getPoint()){
                if (i >= mSize)
                    break;
                Module m = mModuleModel.getModuleMap().get(mSimplexModel.getNode(i++));
                m.setPartitionId(2 - (new Double(d)).intValue());                    
            }
            
            try {
            	CutFinderHelper.findCuts(mModuleModel);
            }catch (Exception e){
            	System.err.println("Finding cut faied in the model: " + e.getMessage());
            }
            
            System.out.println("Solution: (" + solution.getValue() + ")");
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }    
    
    public String getSolution() {
        if (solver != null)
            return "Solution: " + solution.getValue();
        return "";
    }
    
    protected void initNodes() {
        for (Module m : mModuleModel.getModuleMap().values()){         
            ModuleHost baseMH = new ModuleHost(m, getDefaultHost());
            ModuleHost targetMH = new ModuleHost(m, TwoHostHelper.getTargetHost(mHostModel));
           
            double localWeight = mExecutionCostMap.get(baseMH);
            double remoteWeight = mExecutionCostMap.get(targetMH);
           
            mSimplexModel.addNode(m.getName(), localWeight, remoteWeight);
        }
    }        
    
    protected void initEdges(){                
        for (ModulePair mPair : mModuleModel.getModuleExchangeMap().keySet()){                                   
            ModulePairHostPair baseMHP = new ModulePairHostPair(
                    mPair, new HostPair(getDefaultHost(), TwoHostHelper.getTargetHost(mHostModel)));
            ModulePairHostPair targetMHP = new ModulePairHostPair(
                    mPair, new HostPair(TwoHostHelper.getTargetHost(mHostModel), getDefaultHost()));
            InteractionCost cost = InteractionCostHelper.mergeCostsIgnoreHost(
                    mInteractionCostMap.get(baseMHP),
                    mInteractionCostMap.get(targetMHP));
            Module[] mArray = mPair.getModules();
            mSimplexModel.addEdge(mArray[0].getName(),
                    mArray[1].getName(), cost.getTotalCost());
        }
    }

    protected Host getDefaultHost(){
        return mHostModel.getDefaultHost();
    }
    
    protected int getMaxIterations(){
        return SimplexPartitioner.MAX_ITERATIONS;
    }
    
    protected void filterHostExecution(IModuleFilter filter, ModuleHost mh){
        if (filter.isFilterable(mh)){
            if (mh.getHost().equals(TwoHostHelper.getSourceHost(mHostModel)))
                mSimplexModel.pinToTarget(mh.getModule().getName());
            else if (mh.getHost().equals(TwoHostHelper.getTargetHost(mHostModel)))
                mSimplexModel.pinToSource(mh.getModule().getName());
        }
    }
    
    /**
     * The following sets the interaction filter for the modules in the application
     * so that two modules stay together. 
     * 
     * @param filter    the filter to be applied to the modules.
     * @param mhp       the pair for which filtering will be applied
     */
    protected void filterHostInteraction(IInteractionFilter filter, ModulePairHostPair mhp) {
       if (filter.isFilterable(mhp))
           mSimplexModel.pinTogether(mhp.getModulePair().getModules()[0].getName(), 
                   mhp.getModulePair().getModules()[1].getName());
    }

    /**
     * The filterHostColocation filter chooses to have modules colocated on the same host.
     */
	@Override
	protected void filterHostColocation(IColocationFilter filter, ModuleHost mh) {
		// if the set of filters in the filter is not null, there are 
		// module pairs to be filtered.
		if (filter.isFilterable(null))
			// retrieves the list of to-be-colocated filters and pins them
			// together.
			for (ModulePair mp : filter.getFilterSet())
				mSimplexModel.pinTogether(mp.getModules()[0].getName(), 
						mp.getModules()[1].getName());
	}

	@Override
	protected void filterModifyVertexCost(IModuleFilter filter, ModuleHost mh) {
		String nodeName = mh.getModule().getName();
		mSimplexModel.updateNodeWeight(nodeName, 
				filter.filter(mh) * mSimplexModel.getNodeWeights(nodeName)[0], 
				filter.filter(mh) * mSimplexModel.getNodeWeights(nodeName)[1]);
	}

	@Override
	protected void filterModifyEdgeCost(IInteractionFilter filter, ModulePairHostPair mhp, InteractionCost cost) {

		if(filter.isFilterable(mhp)){
			InteractionCost newCost = new InteractionCost(cost.getHostPair(),
					cost.getH1toH2Cost() * filter.filter(mhp), 
					cost.getH2toH1Cost() * filter.filter(mhp), 
					Boolean.FALSE);
			mhp.setCost(newCost);
			mInteractionCostMap.put(mhp, newCost);
		}
	}
}
