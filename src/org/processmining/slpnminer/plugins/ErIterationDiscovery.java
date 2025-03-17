package org.processmining.slpnminer.plugins;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.math4.legacy.analysis.MultivariateFunction;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.SimpleBounds;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.PopulationSize;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.Sigma;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
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
import org.processmining.models.connections.petrinets.behavioral.*;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
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
import org.processmining.slpnminer.connections.ReachabilityConnection;
import org.processmining.slpnminer.models.BoundednessAnalyzer;
import org.processmining.slpnminer.models.reachabilitygraph.AcceptStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.CoverabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.CrossProductImpl;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.StartStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.State;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;
import org.processmining.slpnminer.connections.DeadMarkingConnection;
import org.processmining.slpnminer.connections.StateSpaceConnection;
import org.processmining.slpnminer.helpers.EquationSystems;
import org.processmining.slpnminer.helpers.IsolateVariable;
import org.processmining.slpnminer.helpers.StrToExp;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Plugin(name = "Discover SLPN with Entropic Relevance",
        returnLabels = { "stochastic labelled Petri net" },
        returnTypes = { StochasticLabelledPetriNetSimpleWeightsEditable.class},
        parameterLabels = { "net", "log"})
public class ErIterationDiscovery {

    private static final int MAXSTATES = 25000;
        
    private static int modelTransitionNum;

    private static int logSize;

    private static HashMap<String, Double> strToDouble = new HashMap<>();
    
    private static HashMap<String, Double> traceVariantMap;
    
    private static HashMap<String, String> isolatedVariantMap = new HashMap<String, String>();

    private static Boolean mapFlag = true;
    
    private static Collection<Transition> netTransitionCollection;
        
    private static   HashMap<String,String> tTsIdToPetriIdMap = new HashMap<String,String>();
    


    @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Tian Li", email = "t.li@bpm.rwth-aachen.de", pack = "WeightEstimation")
    @PluginVariant(requiredParameterLabels = {0, 1})
    public StochasticLabelledPetriNetSimpleWeightsEditable calculateTS(PluginContext context, XLog log, Petrinet net) throws ConnectionCannotBeObtained, ParseException {
		
    	
    	Marking initialMarking = guessInitialMarking(net);
		logSize = log.size();
		modelTransitionNum = net.getTransitions().size();
		netTransitionCollection = net.getTransitions();
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

    public StochasticLabelledPetriNetSimpleWeightsEditable calculateTS(PluginContext context, XLog log, Petrinet net, Marking state, PetrinetSemantics semantics)
            throws ConnectionCannotBeObtained, ParseException {
    	semantics.initialize(net.getTransitions(), new Marking(state));
        return buildAndConnect(context, log, net, state, semantics, null);
    }

    private StochasticLabelledPetriNetSimpleWeightsEditable buildAndConnect(PluginContext context,
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
        HashMap<Object, String> tm = new HashMap<>();
        HashMap<String, String> tmap = new HashMap<>();
        
        int transIdx = 0;
        for(org.processmining.slpnminer.models.reachabilitygraph.Transition t: ts.getEdges()){
        
//            System.out.println("iterate transition in ts: " + t.getIdentifier() + "   " + t.getLabel());
        	if(tmap.containsKey(t.getIdentifier().toString())) {
            	tm.put(t.getIdentifier(), tmap.get(t.getIdentifier().toString()));
        	}
        	else {
        		tm.put(t.getIdentifier(), "t" + transIdx);
        		tmap.put(t.getIdentifier().toString(), "t" + transIdx);
                transIdx ++;
        	}
        }
//        get the probability of transition in the reachability graph of Petri net
        Object[] obj2 = getProbabilityFromPetriRG(ts, tm);
        HashMap<String, HashMap<String,String>> tProbMap = (HashMap<String, HashMap<String, String>>) obj2[0];
        HashMap<String,String> x2ProbMap = (HashMap<String, String>) obj2[1];


        Object[] traceObj;
        traceVariantMap = new HashMap<String, Double>();

        Integer j = 0;
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
	            HashSet<State> reachableState = another_ts.getReachableState();

	            Boolean noAcceptState = true;
	            for(State s: another_ts.getNodes()) {
	            	if(s.isAccepting()) {
	            		noAcceptState = false;
	            	}
	            }
	            if(noAcceptState) {
		            isolatedVariantMap.put(traceStr,"0");
	            	j++;
	            	continue;
	            }
	          
	            @SuppressWarnings("unchecked")
				HashMap<String, String> combiToPetri = (HashMap<String, String>)obj[1];

	            EquationSystems eq = new EquationSystems();
	            String assis1 = "Solve";	           
	            Object[] resultObj = eq.getEqStr(another_ts, tm, tProbMap, combiToPetri, reachableState); 
	            String eqSys = (String) resultObj[0];
	            @SuppressWarnings("unchecked")
				HashMap<String, String> replTransProb = (HashMap<String, String>)resultObj[1];

	            try {
	            	ExecutorService executorService = Executors.newSingleThreadExecutor();
	        		String isolatedVar = null;
	        		Future<String> future = executorService.submit(() ->  IsolateVariable.getIsolatedVar(assis1+eqSys));
	        		
	        		try {
	        			isolatedVar = future.get(30, TimeUnit.SECONDS);
	        		} catch (TimeoutException e) {
	        			System.out.println("Timeout computing the probability");
	    	            isolatedVariantMap.put(traceStr,"0");
	        		} catch(Exception e){
	        			
	        		}finally {
	        		    executorService.shutdown();
	        		}
	            	
	            	if (isolatedVar==null) {
		            	j++;
	    	            isolatedVariantMap.put(traceStr,"0");
	            		continue;
	            	}
//	                isolatedVar = isolatedVar.replaceAll("\\s+","");
		            String transformedIsolatedVar = replaceExpressions(isolatedVar, replTransProb);   
//		            System.out.println("the isolatated var before: "+isolatedVar);
//		            System.out.println("the "+String.valueOf(j+1)+"-th trace: " + traceStr);
//		            System.out.println("the isolatated var after: "+transformedIsolatedVar);
		            isolatedVariantMap.put(traceStr,transformedIsolatedVar);
	            }
	            catch(Exception e){
	            	
	            }
            }
            else {
	            isolatedVariantMap.put(traceStr,"0");
            }
            j++;
        }
        
        mapFlag = true;
        MultivariateFunction fER = new MultivariateFunction() {
            public double value(double[] x) {
                return getER(x);
            }
        };

        double[] lowerBound = new double[modelTransitionNum];
        double[] upperBound = new double[modelTransitionNum];
        double[] initGuess = new double[modelTransitionNum];
		double[] sigma = new double[modelTransitionNum];
		  Arrays.fill(lowerBound, 0.0001);
	        Arrays.fill(upperBound, 1.0000);
	        Arrays.fill(initGuess, 1);
			Arrays.fill(sigma, 0.1);
			UniformRandomProvider rngG = RandomSource.MT_64.create();
	        CMAESOptimizer optimizer = new CMAESOptimizer(
	        		1000000, 
	        		0, 
	        		true, 
	        		modelTransitionNum,
	                100, 
	                rngG,
	                true, 
	                null);

			double[] result1 = optimizer.optimize(
					new MaxEval(10000),
			   new ObjectiveFunction(fER),
			   GoalType.MINIMIZE,
			   new PopulationSize((int) (4+3*Math.log(modelTransitionNum))),
			   new Sigma(sigma),
			   new InitialGuess(initGuess),
			   new SimpleBounds(lowerBound, upperBound)).getPoint();
			
		StochasticLabelledPetriNetSimpleWeightsEditable result = new StochasticLabelledPetriNetSimpleWeightsImpl();

		TObjectIntMap<Place> oldPlace2place = new TObjectIntHashMap<>();
		for (Place oldPlace : net.getPlaces()) {
			int place = result.addPlace();
			oldPlace2place.put(oldPlace, place);
		}

		for (Place oldPlace : initial) {
			result.addPlaceToInitialMarking(oldPlace2place.get(oldPlace), initial.occurrences(oldPlace));
		}

		TObjectIntMap<Transition> oldTransition2newTransition = new TObjectIntHashMap<>();
		for (Transition oldTransition : net.getTransitions()) {
			int newTransition;
			for(String tId:tmap.keySet()) {
				if(tId.equals(oldTransition.getLocalID().toString())) {
					String tr = tmap.get(tId);
			        String tr2 = tr.substring(tr.length()-1);
					Integer idx = Integer.valueOf(tr2);
					double weight = result1[idx];
		
					if (oldTransition.isInvisible()) {
						newTransition = result.addTransition(weight);
					} else {
						newTransition = result.addTransition(oldTransition.getLabel(), weight);
					}
					oldTransition2newTransition.put(oldTransition, newTransition);
					break;
				}
			}
		}
		for (Transition oldTransition : net.getTransitions()) {
			int newTransition = oldTransition2newTransition.get(oldTransition);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(oldTransition)) {
				Place oldSource = (Place) edge.getSource();
				result.addPlaceTransitionArc(oldPlace2place.get(oldSource), newTransition);
			}
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(oldTransition)) {
				Place oldTarget = (Place) edge.getTarget();
				result.addTransitionPlaceArc(newTransition, oldPlace2place.get(oldTarget));
			}
		}
		return result;
    }
    
	
    private static String replaceExpressions(String input,HashMap<String,String> varReplace) {
    	
//    	 String input_without_power = transformPowerExpression(input);
//         System.out.println("the var without exp: "+input_without_power);

    	 
    	 StringBuffer sb = new StringBuffer();
         Pattern pattern = Pattern.compile("x\\d+");
         Matcher matcher = pattern.matcher(input);

         while (matcher.find()) {
             String match = matcher.group();
             String replacement = varReplace.getOrDefault(match, match);
             matcher.appendReplacement(sb, replacement);
         }

         matcher.appendTail(sb);
         return sb.toString();
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

    private Object[] getProbabilityFromPetriRG(
    		ReachabilityGraph ts,
    		HashMap<Object, String> tm) {
    	
    	HashMap<String, HashMap<String, String>> tProbMap = new HashMap<String, HashMap<String, String>>();
		Integer i = 0;
        HashMap<String,String> x2ProbMap = new HashMap<String,String>();


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
					
					if(ts.getOutEdges(state).size()==1) {//if transition t is the only edge
						tProbMap.get(tm.get(t.getIdentifier())).put(state.getLabel(), "1");
						i++;
					}
					else {
						tProbMap.get(tm.get(t.getIdentifier())).put(state.getLabel(), tm.get(t.getIdentifier())+"/"+result);
						i++;
					}
				}
				else {
					HashMap<String, String> tempMap = new HashMap<String, String>();
					if(ts.getOutEdges(state).size()==1) {//if transition t is the only edge
						tempMap.put(state.getLabel(), "1");	
						}
					else {
						tempMap.put(state.getLabel(), tm.get(t.getIdentifier())+"/"+result);						
					}
					i++;
					tProbMap.put(tm.get(t.getIdentifier()), tempMap);
				}
			}				
		}

	    Object[] obj = new Object[2];
	    obj[0] = tProbMap;
	    obj[1] = x2ProbMap;
		return obj;
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
//		        System.out.println("get id of trans: "+t.getLocalID()+"  "+t.getLabel());

				ts.addTransition(state, newState, t.getLocalID(), t.getLabel(), t.isInvisible());
				
				semantics.setCurrentState(state);
			}
			return newStates;
		}

    public static double getER(double[] t){
        for(int i=0;i < modelTransitionNum; i++) {
        	strToDouble.put("t"+String.valueOf(i), t[i]); 
        	}
    	
        if (mapFlag) {
	        for(String s:traceVariantMap.keySet()) {
				if (!isolatedVariantMap.get(s).equals("0")) {
			        System.out.println("Trace: "+s+", with frequency: "+(double)traceVariantMap.get(s)/logSize);
			  }
			}
	        mapFlag = false;
        }
        double val = 0; 
		for(String s:traceVariantMap.keySet()) {
			if (!isolatedVariantMap.get(s).equals("0")) {
		        val -= Math.log(StrToExp.converStringToMathExp(isolatedVariantMap.get(s),strToDouble)) * (double)traceVariantMap.get(s)/logSize;
		  }
		}
        return val;
    }

}