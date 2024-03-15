package org.processmining.slpnminer.connections;


import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;

public class StateSpaceConnection extends AbstractSemanticConnection {

    public final static String STATEPACE = "Statespace";


    public StateSpaceConnection(PetrinetGraph net, Marking marking, ReachabilityGraph statespace,
                                Semantics<Marking, Transition> semantics) {
        super("Connection to statespace of " + net.getLabel(), net, marking, semantics);
        put(STATEPACE, statespace);
    }

}
