package org.processmining.slpnminer.models.reachabilitygraph;

import org.apache.commons.collections15.MapIterator;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.processmining.models.graphbased.directed.*;
import org.processmining.slpnminer.models.AttributeMap;
import org.processmining.slpnminer.models.TransitionSystem;

import javax.swing.*;
import java.util.*;


public class ReachabilityGraph extends AbstractDirectedGraph<State, Transition> implements TransitionSystem {

    private final Map<Object, State> states = new LinkedHashMap<Object, State>();

    @SuppressWarnings("rawtypes")
    private final MultiKeyMap t = new MultiKeyMap(); //3 keys: <State, State, Object, String, Boolean> and a value <Transition>, but is set to generics

    private Map<Object, Object> proxyMap;

    public ReachabilityGraph(String label) {
        super();
        getAttributeMap().put(AttributeMap.LABEL, label);
        getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);

        proxyMap = new HashMap<Object, Object>();
    }

    @Override
    protected synchronized Map<DirectedGraphElement, DirectedGraphElement> cloneFrom(
            DirectedGraph<State, Transition> graph) {
        assert (graph instanceof ReachabilityGraph);
        Map<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

        ReachabilityGraph ts = (ReachabilityGraph) graph;
        for (Object identifier : ts.states.keySet()) {
            addState(identifier);
            mapping.put(ts.states.get(identifier), getNode(identifier));
        }
//		for (Transition trans : getEdges()) {
        for (Transition trans : ts.getEdges()) {
            addTransition(trans.getSource().getIdentifier(), trans.getTarget().getIdentifier(), trans.getIdentifier(),
                    trans.getVisibility());
            mapping.put(trans, findTransition(trans.getSource().getIdentifier(), trans.getTarget().getIdentifier(),
                    trans.getIdentifier()));
        }
        return mapping;
    }

    @Override
    protected synchronized AbstractDirectedGraph<State, Transition> getEmptyClone() {
        return new ReachabilityGraph(getLabel());
    }

    /**
     * Node/State Handling
     *
     */

    public synchronized boolean addState(Object identifier) {
        if (!states.containsKey(identifier)) {
            State state = new State(identifier, this);
            states.put(identifier, state);
            graphElementAdded(state);
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean addState(Object identifier, Boolean isInitial,Boolean isAccept) {
        if (!states.containsKey(identifier)) {
            State state = new State(identifier, this);
            state.setInitiating(isInitial);
            state.setAccepting(isAccept);
            states.put(identifier, state);
            graphElementAdded(state);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void removeNode(DirectedGraphNode node) {
        if (node instanceof State) {
            removeState(node);
        } else {
            assert (false);
        }
    }

    public synchronized Set<State> getNodes() {
        Set<State> nodes = new HashSet<State>();
        nodes.addAll(states.values());
        return nodes;
    }

    public synchronized Collection<?> getStates() {
        return states.keySet();
    }
    

    public synchronized Object removeState(Object state) {
        if (state instanceof State) {
            Object removed = states.get(((State) state).getIdentifier());
            states.remove(((State) state).getIdentifier());
            graphElementRemoved(removed);
            return removed;
        }
        else return null;
    }

    public synchronized State getNode(Object identifier) {
        return states.get(getProxy(identifier));
    }

    public synchronized Boolean containsNode(Object identifier) {
        return states.containsKey(getProxy(identifier));
    }

    /**
     * Edge/Transition Handling
     *
     */

    @SuppressWarnings("unchecked")
    public synchronized boolean addTransition(Object fromState, Object toState, Object identifier, String label, Boolean isInvisible) {
        State source = getNode(fromState);
        State target = getNode(toState);
        checkAddEdge(source, target);

        if (!t.containsKey(source, target, identifier)) {
            Transition transition = new Transition(source, target, identifier, label, isInvisible);
            t.put(source, target, identifier, transition); //Stores the Source State, Target State and Object identifier as keys of a Transition.
            graphElementAdded(transition);
        	target.addPreTrans(transition); // add this previous transition to target state.
            return true;
        } else {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean addTransition(Object fromState, Object toState, Object identifier,
    		String label, Boolean isInvisible, Double weight) {
        State source = getNode(fromState);
        State target = getNode(toState);
        checkAddEdge(source, target);

        if (!t.containsKey(source, target, identifier)) {
            Transition transition = new Transition(source, target, identifier, label, weight, isInvisible);
            t.put(source, target, identifier, transition); //Stores the Source State, Target State and Object identifier as keys of a Transition.
            graphElementAdded(transition);
        	target.addPreTrans(transition); // add this previous transition to target state.
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized Transition addAndReturnTransition(Object fromState, Object toState, Object identifier, String label, Boolean isInvisible) {
        State source = getNode(fromState);
        State target = getNode(toState);
        checkAddEdge(source, target);

        if (!t.containsKey(source, target, identifier)) {
            Transition transition = new Transition(source, target, identifier, label, isInvisible);
            t.put(source, target, identifier, transition); //Stores the Source State, Target State and Object identifier as keys of a Transition.
            graphElementAdded(transition);
        	target.addPreTrans(transition); // add this previous transition to target state.
            return transition;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized Object removeTransition(Object fromState, Object toState, Object identifier) {
        Object removed = t.get(getNode(fromState), getNode(toState), identifier);
        t.remove(getNode(fromState), getNode(toState), identifier);
        graphElementRemoved(removed);
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void removeEdge(@SuppressWarnings("rawtypes") DirectedGraphEdge edge) {
        assert (edge instanceof Transition);
        Transition transition = (Transition) edge;
        t.remove(transition.getSource(),transition.getTarget(),transition.getIdentifier());
    }

    public synchronized Collection<Object> getTransitions() {
        Set<Object> keys = new LinkedHashSet<Object>();
        for(Object trans : t.values())
            keys.add(((Transition) trans).getIdentifier());
        return keys;
    }

    @SuppressWarnings("unchecked")
    public synchronized Collection<Object> getTransitionObjects() {
        return t.values();
    }

    @SuppressWarnings("unchecked")
    public synchronized Set<Transition> getEdges() {
        Set<Transition> result = new HashSet<Transition>();
        result.addAll(t.values());
        return Collections.unmodifiableSet(result);
    }

    @SuppressWarnings("rawtypes")
	public Collection<Transition> getEdges(Object identifier) {
        Set<Transition> collection = new LinkedHashSet<Transition>();
        MapIterator iterator = t.mapIterator();
        while(iterator.hasNext())
        {
            iterator.next();
            MultiKey key = (MultiKey) iterator.getKey();
            if(identifier.equals(key.getKey(2))){
                collection.add((Transition) t.get(key));
            }
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
	public synchronized Transition findTransition(Object fromState, Object toState, Object identifier) {
        return (Transition) t.get(getNode(fromState),getNode(toState),identifier);
    }


    public void putProxy(Object obj, Object proxy) {
        proxyMap.put(obj, proxy);
    }

    private Object getProxy(Object obj) {
        while (proxyMap.containsKey(obj)) {
            obj = proxyMap.get(obj);
        }
        return obj;
    }

    public void addProxyMap(ReachabilityGraph ts) {
        for (Object key: ts.proxyMap.keySet()) {
            proxyMap.put(key, ts.proxyMap.get(key));
        }
    }
    
    public HashSet<State> getReachableState(){
    	
    	
    	HashSet<State> reachableStateSet = new HashSet<State>();
    
    	for (State state : getNodes()) {
		     if(state.isAccepting()) {
		    	 reachableStateSet.add(state);
		    	 reachableStateSet = getPrecedenceState(state, reachableStateSet);
		     }
		 }    	
    	return reachableStateSet;
    }
    
    public HashSet<State> getPrecedenceState(State state, HashSet<State> reachableStateSet){
       	for (Transition t: state.getPreTransSet()) {  	
       	 if(reachableStateSet.contains(t.getSource())) {
    		 continue;
    	 }
	    	 reachableStateSet.add(t.getSource());
	    
		     if(!t.getSource().isInitiating()) {
		    	 reachableStateSet = getPrecedenceState(t.getSource(), reachableStateSet);
		     }	   
		 }    	
    	return reachableStateSet;
    }

	@Override
	public boolean addTransition(Object fromState, Object toState, Object identifier, Boolean isInvisible) {
		// TODO Auto-generated method stub
		return false;
	}

}
