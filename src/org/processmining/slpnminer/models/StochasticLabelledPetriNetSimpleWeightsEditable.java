package org.processmining.slpnminer.models;

public interface StochasticLabelledPetriNetSimpleWeightsEditable
		extends StochasticLabelledPetriNetSimpleWeights, StochasticLabelledPetriNetEditable {

	public void setTransitionWeight(int transition, double weight);

}