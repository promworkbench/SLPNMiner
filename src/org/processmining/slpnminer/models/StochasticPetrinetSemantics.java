package org.processmining.slpnminer.models;


import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.Marking;

public interface StochasticPetrinetSemantics extends Semantics<Marking, TimedTransition> {

}