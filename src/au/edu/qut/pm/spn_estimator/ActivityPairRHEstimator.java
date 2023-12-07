package au.edu.qut.pm.spn_estimator;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllSuccessors;

import java.util.Collection;
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

public class ActivityPairRHEstimator extends AbstractFrequencyEstimator {
	
	org.processmining.slpnminer.models.StochasticNetImpl result = new org.processmining.slpnminer.models.StochasticNetImpl("target net");

	@Override
	public String getShortID() {
		return "aprh";
	}

	@Override
	public String getReadableID() {
		return "Activity Pair Right-Handed Estimator";
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		edgePairWeights(net);
	}

	private void edgePairWeights(StochasticNet net ) {
//		for (Transition tran: net.getTransitions()) {
//			TimedTransition transition = (TimedTransition)tran;
//			Collection<Transition> successors = findAllSuccessors(transition);
//			double successorWeight  = 0;
//			for (Transition succ: successors) {
//				successorWeight += loadFollowFrequency(tran,succ);
//			}
//			double weight = successorWeight 
//					+ loadZeroableFrequency(tran, startFrequency)
//					+ loadZeroableFrequency(tran, endFrequency);
//			transition.setWeight(weight > 0.0 ? weight: 1.0);
//		}
		
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
			TimedTransition transition = (TimedTransition)tran;
			Collection<Transition> successors = findAllSuccessors(transition);
			double successorWeight  = 0;
			for (Transition succ: successors) {
				successorWeight += loadFollowFrequency(tran,succ);
			}
			double weight = successorWeight 
					+ loadZeroableFrequency(tran, startFrequency)
					+ loadZeroableFrequency(tran, endFrequency);
			
			transition.setWeight( weight );
			transition.setDistributionType(DistributionType.IMMEDIATE);
			Transition resultTransition = result.addTimedTransition(
					tran.getLabel(),
					weight, DistributionType.UNIFORM, 0.0, 200.0);				
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
	public StochasticNetImpl getResult() {
		// TODO Auto-generated method stub
		return result;
	}

}
