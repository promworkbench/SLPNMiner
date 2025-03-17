package org.processmining.slpnminer.models.slpn;

public interface StochasticLabelledPetriNetSimpleWeightsEditable
		extends StochasticLabelledPetriNetSimpleWeights, StochasticLabelledPetriNetEditable {

	public void setTransitionWeight(int transition, double weight);

}