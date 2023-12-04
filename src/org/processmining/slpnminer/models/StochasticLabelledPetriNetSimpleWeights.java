package org.processmining.slpnminer.models;

public interface StochasticLabelledPetriNetSimpleWeights extends StochasticLabelledPetriNet {

	/**
	 * 
	 * @param transition
	 * @return the weight of the transition.
	 */
	public double getTransitionWeight(int transition);

}