package au.edu.qut.pm.spn_estimator;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.ExecutionPolicy;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.slpnminer.models.StochasticNetImpl;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class FrequencyEstimator extends AbstractFrequencyEstimator{
	
	StochasticNetImpl result = new StochasticNetImpl("target net");
	
	@Override
	public void estimateWeights(StochasticNet net) {
//		for (Transition tran: net.getTransitions()) {
//			System.out.println("get trans: ");
//			System.out.println("get input place: "+net.getInEdges(tran));
//
//			TimedTransition transition = (TimedTransition)tran;
//			Double freq = activityFrequency.get(tran.getLabel());
//			if (freq == null){
//				freq = 1.0;
//			}
//			transition.setWeight( freq );
//			transition.setDistributionType(DistributionType.IMMEDIATE);
//		}
//		
//		
		TObjectIntMap<Transition> transition2occurrence = new TObjectIntHashMap<Transition>(10, 0.5f, 0);
		result.setExecutionPolicy(ExecutionPolicy.RACE_ENABLING_MEMORY);
		result.setTimeUnit(TimeUnit.HOURS);
		Map<PetrinetNode, PetrinetNode> input2result = new THashMap<>();
		for (Place inputPlace : net.getPlaces()) {
			Place resultPlace = result.addPlace(inputPlace.getLabel());
			input2result.put(inputPlace, resultPlace);
		}
		for (Transition tran : net.getTransitions()) {
//			get transition weight
			Double freq = activityFrequency.get(tran.getLabel());
			if (freq == null){
				freq = 1.0;
			}
			TimedTransition transition = (TimedTransition)tran;
			transition.setWeight( freq );
			transition.setDistributionType(DistributionType.IMMEDIATE);
			Transition resultTransition = result.addTimedTransition(
					tran.getLabel(),
					freq, DistributionType.UNIFORM, 0.0, 200.0);				
			resultTransition.setInvisible(tran.isInvisible());
			input2result.put(tran, resultTransition);
		}

		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getEdges()) {
			PetrinetNode resultSource = input2result.get(edge.getSource());
			PetrinetNode resultTarget = input2result.get(edge.getTarget());
			if (resultSource instanceof Place) {
				result.addArc((Place) resultSource, (Transition) resultTarget);
			} else {
				result.addArc((Transition) resultSource, (Place) resultTarget);
			}
		}

	}

	@Override
	public String getShortID() {
		return "fe";
	}

	@Override
	public String getReadableID() {
		return "Frequency Estimator";
	}
	
	public StochasticNetImpl getResult() {
		return result;
	}

}
