package org.processmining.slpnminer.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.processmining.framework.providedobjects.SubstitutionType;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.semantics.ExecutionInformation;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetExecutionInformation;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;

import entropic.AbstractStochasticNetSemantics;

@SubstitutionType(substitutedType = StochasticPetrinetSemantics.class)
public class StochasticPetrinetSemanticsImpl extends AbstractStochasticNetSemantics implements StochasticPetrinetSemantics {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1863753685175892937L;

	public StochasticPetrinetSemanticsImpl() {
	}

	public ExecutionInformation executeTransition(TimedTransition toExecute) {
		Marking required = getRequired(toExecute);
		Marking removed = state.minus(required);
		Marking produced = getProduced(toExecute);
		state.addAll(produced);

		return new PetrinetExecutionInformation(required, removed, produced, toExecute);
	}
}
