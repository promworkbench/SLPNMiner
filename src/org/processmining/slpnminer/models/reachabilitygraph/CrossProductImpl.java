package org.processmining.slpnminer.models.reachabilitygraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class CrossProductImpl implements CrossProduct{

    public ReachabilityGraph crossPetriTrace = new ReachabilityGraph(
            "Cross product of trace and model");

    public HashSet<State> tempPetriStateToVisitSet = new HashSet<>();
    
    public HashSet<State> tempCurrentStateSet = new HashSet<>();
    
    public HashMap<Object, Object> combiToPetri = new HashMap<Object, Object>();
    
    public HashSet<Object> visitedStateSet = new HashSet<>();
    
    @Override
    public Object[] getCrossProduct(ReachabilityGraph rgFromPetri,
                                             State initStateFromPetri,
                                             AcceptStateSet finFromPetri,
                                             ReachabilityGraph rgFromTrace,
                                             State initStateFromTrace,
                                             AcceptStateSet finFromTrace) {
        HashSet<State> petriStateLst = new HashSet<>();
        ArrayList<State> traceStateLst = new ArrayList<>();

        ArrayList<Transition> traceTransLst = new ArrayList<>();
        State currentState = rgFromTrace.getNode(initStateFromTrace.getIdentifier());
        traceStateLst.add(currentState);        

        // get the initial state from trace
        while (!currentState.isAccepting()) {
            for(Transition t: rgFromTrace.getOutEdges(currentState)){
                traceTransLst.add(t);
                State nextStateInTrace = t.getTarget();
                traceStateLst.add(nextStateInTrace);
                currentState = nextStateInTrace;
                break;
            }
        }
        // get the initial state from petri
        petriStateLst.add(initStateFromPetri);
        Transition t = null;

//        set up initial trace for the cross product
        Boolean canDoCross = false;
        
        Transition initTransInTrace = traceTransLst.get(0);
        
        State initStateForCross = null;

        for(Transition t1: rgFromPetri.getOutEdges(initStateFromPetri)) {
        	
        	
        	
            if(t1.getVisibility() || t1.getLabel().equals(initTransInTrace.getLabel())){
                canDoCross = true;
                initStateForCross = new State(initStateFromTrace.getIdentifier()+","+initStateFromPetri.getIdentifier(), crossPetriTrace);
                crossPetriTrace.addState(initStateForCross,true,false);
                visitedStateSet.add(initStateFromTrace.getIdentifier() + "," + initStateFromPetri.getIdentifier());

                combiToPetri.put(initStateForCross.getLabel(), initStateFromPetri.getLabel());

                break;
            }
        }
        if(!canDoCross) {
        	return null;
        }
        
//        add state to currentStateSet
        HashSet<State> currentStateSet = new HashSet<>();
        currentStateSet.add(initStateForCross);
        
        for(int i=0; i<traceStateLst.size()-1;i++) {
//            get state from trace state list
            State currentStateFromTrace = traceStateLst.get(i);
//            save all future states from petri
            tempPetriStateToVisitSet = new HashSet<>();
//            get current transition from trace
            t = traceTransLst.get(i);
            
            
//            System.out.println("\ncurrent transition label: "+ t.getLabel() + "， and current state from trace:" + currentStateFromTrace.getIdentifier());
//          With current state from trace and corresponding transition, get all matching states from petri
            for(State currentStateFromPetri: petriStateLst) {
                getMatchingStates(
                        rgFromPetri,
                        rgFromTrace,
                        t.getLabel(),
                        currentStateFromTrace,
                        currentStateFromPetri,
                        0);
            }
            // update the current boundary to visit
            petriStateLst = tempPetriStateToVisitSet;
            
            
            if (i<traceStateLst.size()-1 && petriStateLst.size()==0) {
            	return null;
            }

            tempCurrentStateSet = new HashSet<>(); 
        }
        
        Object[] obj = new Object[2];
        obj[0] = crossPetriTrace;
        obj[1] = combiToPetri;

        return obj;
    }

    // pinpoint whether current combi of currentStateFrom Trace and Petri exist.
//    If No, construct, otherwise, continue.
    public void getMatchingStates(ReachabilityGraph rgFromPetri,
                                                ReachabilityGraph rgFromTrace,
                                                String targetTransLabel,
                                                State currentStateFromTrace,
                                                State currentStateFromPetri,
                                                Integer visCount){
//        System.out.println("all outgoing edge of state in petri:" +currentStateFromPetri+" "+ rgFromPetri.getOutEdges(currentStateFromPetri));
        for(Transition t: rgFromPetri.getOutEdges(currentStateFromPetri)) {

            if (visCount == 0 && t.getLabel().equals(targetTransLabel)) {
                State crossState = new State(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier(),crossPetriTrace);
                if(rgFromPetri.getOutEdges(currentStateFromPetri).size()==0 && rgFromTrace.getOutEdges(currentStateFromTrace).size()==0){
                    crossState.setAccepting(true);
                    crossPetriTrace.addState(crossState,false, true);
                }
                else {
                    crossPetriTrace.addState(crossState);
                }
                
//                System.out.println("current transition to visit from petri: "+t.getLabel() + "  "+targetTransLabel);

                visitedStateSet.add(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier());

                combiToPetri.put(crossState.getLabel(), currentStateFromPetri.getLabel());

                tempCurrentStateSet.add(crossState);
//                System.out.println("With " + t.getLabel()+": add one state to cross system: "+currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier());
                tempPetriStateToVisitSet.add(t.getTarget());

                for(Transition t2: rgFromTrace.getOutEdges(currentStateFromTrace)) {
                    getMatchingStatesForOneSubState(
                            rgFromPetri,
                            rgFromTrace,
                            targetTransLabel,
                            t2.getTarget(),
                            t.getTarget(),
                            crossState,
                            t,
                            visCount+1,
                            0);
                    break;
                }
            }
            else if (t.getVisibility()) {
                State crossState = new State(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier(),crossPetriTrace);
                if(rgFromPetri.getOutEdges(currentStateFromPetri).size()==0 && rgFromTrace.getOutEdges(currentStateFromTrace).size()==0){
                    crossState.setAccepting(true);
//                    System.out.println("find acc state: "+crossState.isAccepting());
                    crossPetriTrace.addState(crossState,false, true);
                }
                else {
                    crossPetriTrace.addState(crossState);
                }
                combiToPetri.put(crossState.getLabel(), currentStateFromPetri.getLabel());

//                tempCurrentStateSet.add(crossState);
//                System.out.println("With" + t.getLabel()+", add one state to cross system: "+currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier());
//                tempPetriStateToVisitSet.add(t.getTarget());
//                should I go further?
                getMatchingStatesForOneSubState(
                        rgFromPetri,
                        rgFromTrace,
                        targetTransLabel,
                        currentStateFromTrace,
                        t.getTarget(),
                        crossState,
                        t,
                        visCount,
                        0);
            }
        }
    }

    public void getMatchingStatesForOneSubState(ReachabilityGraph rgFromPetri,
                                  ReachabilityGraph rgFromTrace,
                                  String targetTransLabel,
                                  State currentStateFromTrace,
                                  State currentStateFromPetri,
                                  State previousCrossState,
                                  Transition previousT,
                                  Integer visCount,
                                  Integer visCount2){
    	
    	if(visCount2 >0 && visitedStateSet.contains(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier())) {
//            System.out.println("visit the node for the second time:" +currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier());
    		return;
    	}
//        System.out.println("all outgoing edge of state in petri:" +currentStateFromPetri+" "+ rgFromPetri.getOutEdges(currentStateFromPetri));
        State currentCrossState = new State(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier(), crossPetriTrace);
        if(rgFromPetri.getOutEdges(currentStateFromPetri).size()==0 && rgFromTrace.getOutEdges(currentStateFromTrace).size()==0){
            currentCrossState.setAccepting(true);
            crossPetriTrace.addState(currentCrossState, false, true);
        }
        else{
            crossPetriTrace.addState(currentCrossState);
        }
        visitedStateSet.add(currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier());

        combiToPetri.put(currentCrossState.getLabel(), currentStateFromPetri.getLabel());
        crossPetriTrace.addTransition(previousCrossState, currentCrossState, previousT.getIdentifier(), previousT.getLabel(), previousT.getVisibility());
        for(Transition t: rgFromPetri.getOutEdges(currentStateFromPetri)) {
            if (visCount == 0 && t.getLabel().equals(targetTransLabel)) {
            	tempCurrentStateSet.add(currentCrossState);
                combiToPetri.put(currentCrossState.getLabel(), currentStateFromPetri.getLabel());

                tempPetriStateToVisitSet.add(t.getTarget());
                
//                System.out.println("With " + t.getLabel()+", add one state to cross system: "+currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier()+"  "+t.getLabel());
                
                for(Transition t2: rgFromTrace.getOutEdges(currentStateFromTrace)) {
                    State nextStateInTrace = rgFromTrace.getNode(t2.getTarget().getIdentifier());
                    getMatchingStatesForOneSubState(
                            rgFromPetri,
                            rgFromTrace,
                            targetTransLabel,
                            nextStateInTrace,
                            t.getTarget(),
                            currentCrossState,
                            t,
                            visCount+1,
                            visCount2 +1);
                    break;
                }
            }
            else if (t.getVisibility()) {
                crossPetriTrace.addTransition(previousCrossState, currentCrossState, previousT.getIdentifier(),previousT.getLabel(), previousT.getVisibility());
                
//                System.out.println("With " + t.getLabel()+", add one state to cross system: "+currentStateFromTrace.getIdentifier() + "," + currentStateFromPetri.getIdentifier()+"  "+t.getLabel());
                
                if(visCount == 1) {
                	tempPetriStateToVisitSet.add(t.getTarget());
                    tempCurrentStateSet.add(currentCrossState);
                    combiToPetri.put(currentCrossState.getLabel(), currentStateFromPetri.getLabel());
                }

                getMatchingStatesForOneSubState(
                        rgFromPetri,
                        rgFromTrace,
                        targetTransLabel,
                        currentStateFromTrace,
                        t.getTarget(),
                        currentCrossState,
                        t,
                        visCount,
                        visCount2 +1);
            }
        }
    }


    @Override
    public String getProbability(ReachabilityGraph r) {
        return null;
    }
}
