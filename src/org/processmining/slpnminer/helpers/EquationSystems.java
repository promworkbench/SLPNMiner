package org.processmining.slpnminer.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.State;
import org.processmining.slpnminer.models.reachabilitygraph.Transition;

public class EquationSystems {

    public String getVarString(ReachabilityGraph rg){
        Set<Transition> transSet = rg.getEdges();

        String varStr="{";

        for(Transition eachTrans: transSet){
            varStr.concat(eachTrans.getLabel());
        }

        varStr.concat("})");
        return varStr;
    }

    public Object[] getEqStr(
    		ReachabilityGraph rg, 
    		HashMap<Object, String> tm,
    		HashMap<String, HashMap<String,String>> tProbMap,
    		HashMap<String, String> combiToPetri,
    		HashSet<State> reachableState){
    	Set<Transition> transSet = rg.getEdges();
        String varStr="({";
        HashMap<State,String> stateToVar= new HashMap<>();
        HashMap<Transition, String> transToVar= new HashMap<>();

        Integer stateIdx = 1;
        for(State state: reachableState){
        	if(state.isInitiating()) {
        		 stateToVar.put(state, "a0");
        	}
	        else {
	            String stateName = "a" + stateIdx++;
	            stateToVar.put(state, stateName);
	        }
        }
    
        for(Transition trans: transSet){
            String transName = tm.get(trans.getIdentifier());
            transToVar.put(trans, transName);
        }

        int stateNum = 0;
        
        HashMap<String, String> replTransProb = new HashMap<String, String>();
        Integer idx4Repl = 0;
        
        for(State currentState: reachableState)
        {        
            String eq="";

            // if the the current state is accepting state
            if(currentState.isAccepting()){
                eq = stateToVar.get(currentState)+"=="+"1";
            }

            // if the current state if just transient state
            else{
                String subVarStr = stateToVar.get(currentState)+"==";
                int outEdgeNum = rg.getOutEdges(currentState).size();
                if(outEdgeNum > 1){
                    int i = 0;
                    for (Transition trans : rg.getOutEdges(currentState)) {
                    
                    	if(reachableState.contains(trans.getTarget())) { 
                    		Object stateInPetri = combiToPetri.get(currentState.getLabel());                       		
                        	String transProb = tProbMap.get(transToVar.get(trans)).get(stateInPetri.toString());    
                        	replTransProb.put("x"+idx4Repl.toString(), transProb);
//                        	subVarStr = subVarStr.concat(stateToVar.get(trans.getTarget()) + "*" + transProb);

                    		subVarStr = subVarStr.concat(stateToVar.get(trans.getTarget()) + "*" + "x"+idx4Repl.toString());
                        	idx4Repl++;

                    		if(i < outEdgeNum-1) {
                                subVarStr = subVarStr.concat("+");
                            	}
                    		}
                        i++;
                    }
                    if(subVarStr.substring(subVarStr.length() - 1).equals("+")) {
                    	subVarStr = subVarStr.substring(0, subVarStr.length() - 1);
                    }
                    eq = eq.concat(subVarStr);
                }
                else if(outEdgeNum == 1){
                    for (Transition trans : rg.getOutEdges(currentState)) {
                    	                   	
                    	Object stateInPetri = combiToPetri.get(currentState.getLabel());
//	        			System.out.println("get the current state:"+stateInPetri);

                    	String transProb = tProbMap.get(transToVar.get(trans)).get(stateInPetri.toString());

                    	replTransProb.put("x"+idx4Repl.toString(), transProb);
                    	
            			if(transProb.equals("1")) {
            				subVarStr = subVarStr.concat(stateToVar.get(trans.getTarget()));
            			}
            			else {
            				subVarStr = subVarStr.concat(stateToVar.get(trans.getTarget()) + "*" + "x"+idx4Repl.toString());
            			}
                    	idx4Repl++;

            			
                        eq = eq.concat(subVarStr);                    	
                    }
                }
            }
            varStr = varStr.concat(eq);
            if(stateNum < reachableState.size()-1){
                varStr = varStr.concat(",");
            }
            stateNum++;

        }
        varStr = varStr.concat("},{");
        String originalString = getStateVarLst(stateToVar);
        // Remove square brackets and spaces
        varStr = varStr.concat(originalString);
        varStr = varStr.concat("})");
        Object[] obj = new Object[2];
        obj[0] = varStr;
        obj[1] = replTransProb;       
        return obj;
    }



	private String getStateVarLst(HashMap<State, String> stateToVar) {
		
		String result = "";
		// TODO Auto-generated method stub
		for(State s:stateToVar.keySet()) {
		
			result = result.concat(stateToVar.get(s)+",");
			
		}

		return result.substring(0, result.length()-1);
	}

	public Set<State> getSubStates(ReachabilityGraph rg,
                                   State currentState){
        Set<State> subStateSet = new HashSet<>();
        Collection<Transition> allOutEdges = rg.getOutEdges(currentState);
        for(Transition trans: allOutEdges){
            subStateSet.add(trans.getTarget());
        }
        return subStateSet;
    }
}
