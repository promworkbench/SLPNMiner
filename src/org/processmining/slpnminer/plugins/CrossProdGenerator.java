package org.processmining.slpnminer.plugins;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.models.connections.petrinets.behavioral.BehavioralAnalysisInformationConnection;
import org.processmining.models.connections.petrinets.behavioral.BoundednessInfoConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.ReachabilitySetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation;
import org.processmining.models.graphbased.directed.petrinet.analysis.ReachabilitySet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.CTMarking;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.slpnminer.connections.DeadMarkingConnection;
import org.processmining.slpnminer.connections.ReachabilityConnection;
import org.processmining.slpnminer.connections.StateSpaceConnection;
import org.processmining.slpnminer.helpers.StrToExp;
import org.processmining.slpnminer.models.BoundednessAnalyzer;
import org.processmining.slpnminer.models.reachabilitygraph.AcceptStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.CoverabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.CrossProductImpl;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.StartStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.State;


@Plugin(name = "Discover reachability graph",
        returnLabels = { "stochastic labelled Petri net" },
        returnTypes = { ReachabilityGraph.class},
        parameterLabels = { "net", "log"})
public class CrossProdGenerator {

    private static final int MAXSTATES = 25000;
        
    private static int modelTransitionNum;

    private static int logSize;

    private static HashMap<String, Double> strToDouble = new HashMap<>();
    
    private static HashMap<String, Double> traceVariantMap;
    
    private static HashMap<String, String> isolatedVariantMap = new HashMap<String, String>();

    private static Boolean mapFlag = true;
        

    @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Tian Li", email = "t.li@bpm.rwth-aachen.de", pack = "WeightEstimation")
    @PluginVariant(requiredParameterLabels = {0, 1})
    public ReachabilityGraph calculateTS(PluginContext context, XLog log, Petrinet net) throws ConnectionCannotBeObtained, ParseException {
		Marking initialMarking = guessInitialMarking(net);
		logSize = log.size();
		
        return calculateTS(context, log, net, initialMarking, PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class));
    }
    
	public static Marking guessInitialMarking(Petrinet net) {
		Marking result = new Marking();
		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty()) {
				result.add(p);
			}
		}
		return result;
	}

    public ReachabilityGraph calculateTS(PluginContext context, XLog log, Petrinet net, Marking state, PetrinetSemantics semantics)
            throws ConnectionCannotBeObtained, ParseException {
    	semantics.initialize(net.getTransitions(), new Marking(state));
        return buildAndConnect(context, log, net, state, semantics, null);
    }

    private ReachabilityGraph buildAndConnect(PluginContext context,
                                     XLog log,
                                     PetrinetGraph net,
                                     Marking initial,
                                     Semantics<Marking, Transition> semantics, CoverabilityGraph coverabilityGraph)
            throws ConnectionCannotBeObtained, ParseException {
        context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net, initial);

        ReachabilityGraph ts = null;
        
        
        NetAnalysisInformation.BOUNDEDNESS info = null;

        try {
            BoundednessInfoConnection analysis = context.getConnectionManager().getFirstConnection(
                    BoundednessInfoConnection.class, context, net, initial, semantics);
            info = analysis.getObjectWithRole(BehavioralAnalysisInformationConnection.NETANALYSISINFORMATION);
        } catch (Exception e) {
            // No connections available
        }

        if ((info != null) && info.getValue().equals(NetAnalysisInformation.UnDetBool.FALSE)) {
            // This net has been shows to be unbounded on this marking
            context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
                    Logger.MessageLevel.ERROR);
            context.getFutureResult(0).cancel(true);
            // unreachable statement, but safe.
            return null;
        }

        if (coverabilityGraph != null) {// && !bounded) {
            if (!BoundednessAnalyzer.isBounded(coverabilityGraph)) {
                // This net has been shows to be unbounded on this marking
                context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
                        Logger.MessageLevel.ERROR);
                context.getFutureResult(0).cancel(true);
                // unreachable statement, but safe.
                return null;
            }
            // clone the graph and return
            Map<CTMarking, Marking> mapping = new HashMap<CTMarking, Marking>();

            ts = new ReachabilityGraph("StateSpace of " + net.getLabel());
            for (Object o : coverabilityGraph.getStates()) {
                CTMarking m = (CTMarking) o;
                Marking tsm = new Marking(m);
                ts.addState(tsm);
                mapping.put(m, tsm);
            }
            for (org.processmining.slpnminer.models.reachabilitygraph.Transition e : coverabilityGraph
                    .getEdges()) {
                Marking source = mapping.get(e.getSource().getIdentifier());
                Marking target = mapping.get(e.getTarget().getIdentifier());
                ts.addTransition(source, target, e.getIdentifier(),e.getVisibility());
            }
        }

        StartStateSet startStates = new StartStateSet();
        startStates.add(initial);

        if (ts == null) {
            ts = doBreadthFirst(context, net.getLabel(), initial, semantics, MAXSTATES);
        }
        if (ts == null) {
            // Problem with the reachability graph.
            context.getFutureResult(0).cancel(true);
            return null;
        }

        AcceptStateSet acceptingStates = new AcceptStateSet();
        for (State state : ts.getNodes()) {
            if (ts.getOutEdges(state).isEmpty()) {
                acceptingStates.add(state.getIdentifier());
            }
        }

        Marking[] markings = ts.getStates().toArray(new Marking[0]);
        ReachabilitySet rs = new ReachabilitySet(markings);

        context.addConnection(new ReachabilitySetConnection(net, initial, rs, semantics, "Reachability Set"));
        context.addConnection(new StateSpaceConnection(net, initial, ts, semantics));
        context.addConnection(new ReachabilityConnection(ts, startStates, acceptingStates));
        context.addConnection(new DeadMarkingConnection(net, initial, acceptingStates, semantics));

        context.log("Statespace size: " + ts.getStates().size() + " states and " + ts.getEdges().size()
                + " transitions.", Logger.MessageLevel.DEBUG);

//        get all the transitions
        int transIdx = 0;
        HashMap<Object, String> tm = new HashMap<>();
        HashMap<String, String> tmap = new HashMap<>();

        
        for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getEdges()){
            tm.put(t.getIdentifier(), "t" + transIdx);
            tmap.put(t.getLabel(), "t" + transIdx);
            transIdx ++;
        }
        
//        get the probability of transition in the reachability graph of Petri net
        HashMap<String, HashMap<String,String>> tProbMap = getProbabilityFromPetriRG(ts, tm);
        

        Object[] traceObj;
        traceVariantMap = new HashMap<String, Double>();

        System.out.println("1. Start log iteration");

        int j = 0;
        for (XTrace trace : log) {
            Petrinet traceNet = new PetrinetImpl("trace net");
            Marking initialMarking = new Marking();
        	String traceStr = "";

            if (!trace.isEmpty()) {
//            	get trace variant
            	XAttribute tempEv;
            	for(XEvent e: trace) {
                    tempEv = e.getAttributes().get("concept:name");
                    traceStr = traceStr.concat(tempEv.toString()+" ");
            	}
                
                if(traceVariantMap.containsKey(traceStr)) {
                	Double count = traceVariantMap.get(traceStr) + 1;
                	traceVariantMap.put(traceStr,count);
                	continue;
                }
                else {
                	traceVariantMap.put(traceStr, 1.0);
                }
                Place currentPlace = traceNet.addPlace("p" + String.valueOf(0));
                initialMarking.add(currentPlace);
                for (int i = 0; i < trace.size(); i++) {
                    Place nextPlace = traceNet.addPlace("p" + String.valueOf(i + 1));
                    Transition t = traceNet.addTransition(String.valueOf(trace.get(i).getAttributes().get("concept:name")));
                    t.setInvisible(false);
                    traceNet.addArc(currentPlace, t);
                    traceNet.addArc(t, nextPlace);
                    currentPlace = nextPlace;
                }
            }
            
            traceObj = calculateTSForTrace(context, traceNet, initialMarking, PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class));
                    State initStateForPetri = new State(initial,ts);
            CrossProductImpl CP = new CrossProductImpl();

            
            Object[] obj = CP.getCrossProduct(
                    ts,
                    initStateForPetri,
                    acceptingStates,
                    (ReachabilityGraph) traceObj[0],
                    (State) traceObj[2],
                    (AcceptStateSet) traceObj[3]);
            
            if(obj!=null) {
	            ReachabilityGraph another_ts = (ReachabilityGraph)obj[0];
	            return another_ts;
            }
        }
        System.out.println("It is null");
        return null;
    }
    
    
    private static String getTransformedString(String expression) {
		 // Define the pattern for detecting variables with exponentiation
       Pattern pattern = Pattern.compile("([a-zA-Z]\\d*)\\^\\d+");

       // Create a matcher with the given expression
       Matcher matcher = pattern.matcher(expression);

       String expression2 = expression;
       // Find and print all matches
       while (matcher.find()) {
//           System.out.println("Variable with exponentiation: " + transformPowerExpression(matcher.group()));
           expression2 = expression2.replace(matcher.group(), transformPowerExpression(matcher.group()));
       }
       System.out.println("\n the expression after transformation: "+expression2);

       return expression2;
	}
	
   private static String transformPowerExpression(String powerExpression) {
       // Split the expression into variable and exponent parts
       String[] parts = powerExpression.split("\\^");

       // Extract variable and exponent
       String variable = parts[0];
       int exponent = Integer.parseInt(parts[1]);

       // Create the transformed expression
       StringBuilder transformedExpression = new StringBuilder();
       for (int i = 0; i < exponent; i++) {
           transformedExpression.append(variable);
           if (i < exponent - 1) {
               transformedExpression.append("*");
           }
       }

       return transformedExpression.toString();
   }

    private HashMap<String, HashMap<String, String>> getProbabilityFromPetriRG(
    		ReachabilityGraph ts,
    		HashMap<Object, String> tm) {
    	
    	HashMap<String, HashMap<String, String>> tProbMap = new HashMap<String, HashMap<String, String>>();
    	
		// TODO Auto-generated method stub
		for(State state: ts.getNodes()) {
			// for each outgoing edges, get the probability
			String result = "(";
			for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getOutEdges(state)) {
				result = result.concat(tm.get(t.getIdentifier())+"+");
			}
			result = result.substring(0, result.length()-1);
			result = result.concat(")");
			for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getOutEdges(state)) {
				if(tProbMap.containsKey(tm.get(t.getIdentifier()))) {
					tProbMap.get(tm.get(t.getIdentifier())).put(state.getLabel(), tm.get(t.getIdentifier())+"/"+result);
					
				}
				else {
					HashMap<String, String> tempMap = new HashMap<String, String>();
					tempMap.put(state.getLabel(), tm.get(t.getIdentifier())+"/"+result);
					tProbMap.put(tm.get(t.getIdentifier()), tempMap);
				}
			}
			
			
		}
		
//		
        System.out.println("Get all probability map");
        for(String key1: tProbMap.keySet()){
        	for(Object obj1: tProbMap.get(key1).keySet()) {
        		System.out.println("key: "+ key1 +" state: "+ obj1+" trans prob: "+tProbMap.get(key1).get(obj1));
        	}
        }

		return tProbMap;
	}

	public Object[] calculateTSForTrace(PluginContext context, Petrinet net, Marking state, PetrinetSemantics semantics)
            throws ConnectionCannotBeObtained {
        semantics.initialize(net.getTransitions(), new Marking(state));
        return buildAndConnectForTrace(context, net, state, semantics, null);
    }


    private Object[] buildAndConnectForTrace(PluginContext context, PetrinetGraph net, Marking initial,
                                          Semantics<Marking, Transition> semantics, CoverabilityGraph coverabilityGraph)
            throws ConnectionCannotBeObtained {
        context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net, initial);
        ReachabilityGraph ts = null;
        NetAnalysisInformation.BOUNDEDNESS info = null;

        try {
            BoundednessInfoConnection analysis = context.getConnectionManager().getFirstConnection(
                    BoundednessInfoConnection.class, context, net, initial, semantics);
            info = analysis.getObjectWithRole(BehavioralAnalysisInformationConnection.NETANALYSISINFORMATION);
        } catch (Exception e) {
            // No connections available
        }

        if ((info != null) && info.getValue().equals(NetAnalysisInformation.UnDetBool.FALSE)) {
            // This net has been shows to be unbounded on this marking
            context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
                    Logger.MessageLevel.ERROR);
            context.getFutureResult(0).cancel(true);
            // unreachable statement, but safe.
            return null;
        }

        if (coverabilityGraph != null) {// && !bounded) {
            if (!BoundednessAnalyzer.isBounded(coverabilityGraph)) {
                // This net has been shows to be unbounded on this marking
                context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
                        Logger.MessageLevel.ERROR);
                context.getFutureResult(0).cancel(true);
                // unreachable statement, but safe.
                return null;
            }
            // clone the graph and return
            Map<CTMarking, Marking> mapping = new HashMap<CTMarking, Marking>();

            ts = new ReachabilityGraph("StateSpace of " + net.getLabel());
            for (Object o : coverabilityGraph.getStates()) {
                CTMarking m = (CTMarking) o;
                Marking tsm = new Marking(m);
                ts.addState(tsm);
                mapping.put(m, tsm);
            }
            for (org.processmining.slpnminer.models.reachabilitygraph.Transition e : coverabilityGraph.getEdges()) {
                Marking source = mapping.get(e.getSource().getIdentifier());
                Marking target = mapping.get(e.getTarget().getIdentifier());
                ts.addTransition(source, target, e.getIdentifier(), e.getVisibility());
            }

        }

        StartStateSet startStates = new StartStateSet();
        startStates.add(initial);

        if (ts == null) {
            ts = doBreadthFirst(context, net.getLabel(), initial, semantics, MAXSTATES);
        }
        if (ts == null) {
            // Problem with the reachability graph.
            context.getFutureResult(0).cancel(true);
            return null;
        }

        AcceptStateSet acceptingStates = new AcceptStateSet();
        for (State state : ts.getNodes()) {
            if (ts.getOutEdges(state).isEmpty()) {
                state.setAccepting(true);
                acceptingStates.add(state.getIdentifier());
            }
        }

        Marking[] markings = ts.getStates().toArray(new Marking[0]);
        ReachabilitySet rs = new ReachabilitySet(markings);

        context.addConnection(new ReachabilitySetConnection(net, initial, rs, semantics, "Reachability Set"));
        context.addConnection(new StateSpaceConnection(net, initial, ts, semantics));
        context.addConnection(new ReachabilityConnection(ts, startStates, acceptingStates));
        context.addConnection(new DeadMarkingConnection(net, initial, acceptingStates, semantics));

//        context.getFutureResult(0).setLabel("Reachability graph of " + net.getLabel());
//        context.getFutureResult(1).setLabel("Reachability set of " + net.getLabel());
//        context.getFutureResult(2).setLabel("Initial states of " + ts.getLabel());
//        context.getFutureResult(3).setLabel("Accepting states of " + ts.getLabel());

        context.log("Statespace size: " + ts.getStates().size() + " states and " + ts.getEdges().size()
                + " transitions.", Logger.MessageLevel.DEBUG);
        State initStateForTrace = new State(initial,ts);

        return new Object[]{ts, rs, initStateForTrace, acceptingStates};
    }

    private ReachabilityGraph doBreadthFirst(PluginContext context, String label, Marking state,
                                             Semantics<Marking, Transition> semantics, int max) {
        ReachabilityGraph ts = new ReachabilityGraph("StateSpace of " + label);
        ts.addState(state);
        Queue<Marking> newStates = new LinkedList<Marking>();
        newStates.add(state);
        do {
            newStates.addAll(extend(ts, newStates.poll(), semantics, context));
        } while (!newStates.isEmpty() && (ts.getStates().size() < max));
        if (!newStates.isEmpty()) {
            // This net has been shows to be unbounded on this marking
            context.log("The behaviour of the given net is has over " + max + " states. Aborting...",
                    Logger.MessageLevel.ERROR);
            context.getFutureResult(0).cancel(true);
            return null;
        }
        return ts;

    }


    private Set<Marking> extend(ReachabilityGraph ts,
                                Marking state,
                                Semantics<Marking, Transition> semantics,
                                PluginContext context) {
        Set<Marking> newStates = new HashSet<Marking>();
        semantics.setCurrentState(state);
        for (Transition t : semantics.getExecutableTransitions()) {
            semantics.setCurrentState(state);
            try {
                /*
                 * [HV] The local variable info is never read
                 * ExecutionInformation info =
                 */
                semantics.executeExecutableTransition(t);
                // context.log(info.toString(), MessageLevel.DEBUG);
            } catch (IllegalTransitionException e) {
                context.log(e);
                assert (false);
            }
            Marking newState = semantics.getCurrentState();

            if (ts.addState(newState)) {
                newStates.add(newState);
                int size = ts.getEdges().size();
                if (size % 1000 == 0) {
                    context.log("Statespace size: " + ts.getStates().size() + " states and " + ts.getEdges().size()
                            + " transitions.", Logger.MessageLevel.DEBUG);
                }
            }
            ts.addTransition(state, newState, t, t.getLabel(), t.isInvisible());

            semantics.setCurrentState(state);
        }
        return newStates;
    }
    
    public static double getUEMSC(double[] t){
        for(int i=0;i < modelTransitionNum; i++) {
        	strToDouble.put("t"+String.valueOf(i), t[i]);
        }

        if (mapFlag) {
	        for(int i=0;i < modelTransitionNum; i++) {
	        	strToDouble.put("t"+String.valueOf(i), t[i]); 
	        	}
	        for(String s:traceVariantMap.keySet()) {
				if (!isolatedVariantMap.get(s).equals("0")) {
			        System.out.println("find trace: "+s+" with freq: "+(double)traceVariantMap.get(s)/logSize);
			  }
			}
	        mapFlag = false;
        }
        
        double val = 1;
        
        for(String s:traceVariantMap.keySet()) {
			if (!isolatedVariantMap.get(s).equals("0")) {
		        val -= Math.max(StrToExp.converStringToMathExp(isolatedVariantMap.get(s),strToDouble) - (double)traceVariantMap.get(s)/logSize, 0);
		  }
		}
        return val;
    }
}