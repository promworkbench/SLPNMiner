package org.processmining.slpnminer.models;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public class StochasticPetrinetSemanticsFactory{

	StochasticPetrinetSemanticsFactory(){	
	}
	
	public static StochasticPetrinetSemantics stochasticNetSemantics(Class<? extends StochasticNet> net) {
		return new StochasticPetrinetSemanticsImpl();
	}

}
