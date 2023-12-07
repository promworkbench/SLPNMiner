package org.processmining.slpnminer.models;

import java.util.Collection;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.slpnminer.models.reachabilitygraph.State;
import org.processmining.slpnminer.models.reachabilitygraph.Transition;

public interface TransitionSystem extends DirectedGraph<State, Transition> {

    String getLabel();

    // transitions
    boolean addTransition(Object fromState, Object toState, Object identifier, Boolean isInvisible);

    Object removeTransition(Object fromState, Object toState, Object identifier);

    Collection<Object> getTransitions();

    Collection<Transition> getEdges(Object identifier);

    // states
    boolean addState(Object identifier);

    Object removeState(Object identifier);

    Collection<? extends Object> getStates();

    State getNode(Object identifier);

    Transition findTransition(Object fromState, Object toState, Object identifier);

}
