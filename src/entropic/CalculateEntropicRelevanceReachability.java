package entropic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.slpnminer.models.StochasticNetImpl;
import org.processmining.slpnminer.models.StochasticPetrinetSemantics;
import org.processmining.slpnminer.models.StochasticPetrinetSemanticsFactory;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.State;


@Plugin(name = "Compute Entropic Relevance stochastic reachability",
        returnLabels = { "entropic relevance value" },
        returnTypes = { ReachabilityGraph.class},
        parameterLabels = { "slpn", "log"})
public class CalculateEntropicRelevanceReachability {
	
    private static final int MAXSTATES = 25000;
    
    private HashMap<Object, Integer> stateToId= null;

    private Integer idx;
	
    @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Tian Li", email = "t.li@bpm.rwth-aachen.de", pack = "WeightEstimation")
    @PluginVariant(requiredParameterLabels = {0, 1})
    public ReachabilityGraph computeER(PluginContext context, XLog log, StochasticNetImpl net){
    	Marking initialMarking = guessInitialMarking(net);
    	
        stateToId = new HashMap<Object, Integer>();
        idx = 0;
        StochasticPetrinetSemantics semantics = StochasticPetrinetSemanticsFactory.stochasticNetSemantics(StochasticNetImpl.class);
        semantics.initialize(net.getTimedTransitions(), initialMarking);
        

        ReachabilityGraph ts = doBreadthFirst(
        		net.getLabel(), 
        		initialMarking, 
        		semantics,
        		MAXSTATES);
        
    	return ts;
    }
    
    private List<SATransition> getSATransitions(ReachabilityGraph ts) {
    	List<SATransition> lsat = new ArrayList<SATransition>();
    	
    	for(State state: ts.getNodes()) {
			// for each outgoing edges, get the probability
			Double totalOutWeights = 0.0;
			for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getOutEdges(state)) {
				totalOutWeights = totalOutWeights + t.getWeight();
			}
			
			for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getOutEdges(state)) {
				SATransition trans = new SATransition(
						stateToId.get(state.getIdentifier()),
						stateToId.get(t.getTarget().getIdentifier()), t.getLabel(),
						t.getWeight()/totalOutWeights);
				System.out.print("weight: "+ t.getLabel() +" "+
				t.getWeight()/totalOutWeights);
				lsat.add(trans);
			}	
		}
    	return lsat;
	}

	private Marking guessInitialMarking(StochasticNet net) {
		Marking result = new Marking();
		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty()) {
				result.add(p);
			}
		}
		return result;
	}

    
    private ReachabilityGraph doBreadthFirst(
    		String label, Marking state,
	        Semantics<Marking, TimedTransition> semantics, 
	        int max) {
    	
			ReachabilityGraph ts = new ReachabilityGraph("StateSpace of " + label);
			ts.addState(state);
			stateToId.put(state,idx);
			Queue<Marking> newStates = new LinkedList<Marking>();
			newStates.add(state);
			do {
				newStates.addAll(extend(ts, newStates.poll(), semantics));
			} while (!newStates.isEmpty() && (ts.getStates().size() < max));
			if (!newStates.isEmpty()) {
			// This net has been shows to be unbounded on this marking
				return null;
			}
			return ts;
		}
			
    private Set<Marking> extend(ReachabilityGraph ts,
            Marking state,
            Semantics<Marking, TimedTransition> semantics) {
				Set<Marking> newStates = new HashSet<Marking>();
				semantics.setCurrentState(state);
				for (TimedTransition t : semantics.getExecutableTransitions()) {
					semantics.setCurrentState(state);
				try {
					semantics.executeExecutableTransition(t);
				} catch (IllegalTransitionException e) {
					assert (false);
				}
				Marking newState = semantics.getCurrentState();
				
				if (ts.addState(newState)) {
					newStates.add(newState);
					stateToId.put(newState,idx++);
				}
				ts.addTransition(state, newState, t.getLocalID(), t.getLabel(), t.isInvisible(), t.getWeight());
				semantics.setCurrentState(state);
			}
		return newStates;			
    }
    
    public static void main(String[] args) throws Exception {
    	XLog log = XLogReader.openLog("/Applications/Programming/Artem-Entropic-Relevance/jbpt-pm/examples/log2.xes");
    	
		SAutomaton sa = SAutomaton.readJSON("/Applications/Programming/Artem-Entropic-Relevance/jbpt-pm/examples/automaton.sdfa");
		Map<String, Object> relevance = Relevance.compute(log, sa, false);
    	System.out.println(1/(Double)relevance.get("relevance"));
    }
}