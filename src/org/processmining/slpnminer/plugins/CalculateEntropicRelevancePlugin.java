package org.processmining.slpnminer.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.math3.util.Pair;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.AbstractPetriNet;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
//import org.jbpt.petri.io.Attributes;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;
import org.processmining.slpnminer.algorithms.EntropyPrecisionRecallMeasure;
import org.processmining.slpnminer.algorithms.Utils;
import org.processmining.statisticaltests.test.FakeContext;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;


@Plugin(name = "Compute Entropic Relevance (log-model)",
        returnLabels = { "entropic relevance value" },
        returnTypes = { String.class},
        parameterLabels = { "slpn", "log"})
public class CalculateEntropicRelevancePlugin {

	private static Object relevantTraces	= null;
	private static Object retrievedTraces = null;
	private static boolean bSilent = false;
	private static boolean bTrust = false;
	private NetSystem pn = new NetSystem();
	private HashMap<String, Node> nodes = new HashMap<String, Node>();

	
    @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Tian Li", email = "t.li@bpm.rwth-aachen.de", pack = "WeightEstimation")
    @PluginVariant(requiredParameterLabels = {0, 1})
    public String computeER(PluginContext context, XLog log, Petrinet net) throws Exception{
    	
    	XFactory factory= XFactoryRegistry.instance().currentDefault();
    	for (Iterator<XTrace> iterator = log.iterator(); iterator.hasNext();) {
			XTrace t = iterator.next();
			XTrace newTrace = factory.createTrace((XAttributeMap) t.getAttributes().clone());
			for (XEvent e : t) {
		        System.out.println("event: "+e.getAttributes());

//				XAttributeMap xm = new XAttributeMap();
//				XEvent newEv = factory.createEvent((XAttributeMap) e.getAttributes().get("concept:name"));				
//				newTrace.add(newEv);
			}
//	        System.out.println("trace: "+t);
		}
    	
		XLog log1 = (XLog) new OpenLogFileLiteImplPlugin().importFile(context, new File("/Applications/Programming/xes/er.xes"));
    	// construct relevant traces
	    relevantTraces = log1;

    	// construct net system from 	    	    
	    Integer idForNode = 0;
	    HashMap<Place, org.jbpt.petri.Place> placeMap = new HashMap<Place, org.jbpt.petri.Place>();
		for (Place place : net.getPlaces()) {
			org.jbpt.petri.Place p = new org.jbpt.petri.Place();
			p.setId(idForNode.toString());
			p.setName("p"+idForNode.toString());
			nodes.put(p.getId(), p);
			pn.addPlace(p);
			placeMap.put(place, p);
			idForNode++;
		}
		
	    HashMap<Transition, org.jbpt.petri.Transition> transitionMap = new HashMap<Transition, org.jbpt.petri.Transition>();
		for (Transition transition : net.getTransitions()) {
			org.jbpt.petri.Transition t = new org.jbpt.petri.Transition();
			t.setId("t"+idForNode.toString());
			t.setName(transition.getLabel());
			nodes.put(t.getId(), t);
			pn.addTransition(t);
			transitionMap.put(transition,t);
			idForNode++;
		}
		
		for (Transition Transition : net.getTransitions()) {
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(Transition)) {
				Place oldSource = (Place) edge.getSource();
		        System.out.println("place to: "+nodes.get(placeMap.get(oldSource).getId()));
		        System.out.println("transition: "+nodes.get(transitionMap.get(Transition).getId()));
				pn.addFlow(nodes.get(placeMap.get(oldSource).getId()), nodes.get(transitionMap.get(Transition).getId()));
			}
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net
					.getOutEdges(Transition)) {
				Place oldTarget = (Place) edge.getTarget();
		        System.out.println("transition to: "+nodes.get(transitionMap.get(Transition).getId()));
		        System.out.println("place: "+nodes.get(placeMap.get(oldTarget).getId()));
				pn.addFlow(nodes.get(transitionMap.get(Transition).getId()), nodes.get(placeMap.get(oldTarget).getId()));
			}
		}
		
	    PNMLSerializer PNML = new PNMLSerializer();
		retrievedTraces = PNML.parse("/Applications/Programming/xes/er.pnml");
		
	    System.out.println("log size: "+relevantTraces);
        System.out.println("model trace size: "+((AbstractPetriNet<Flow, Node, org.jbpt.petri.Place, org.jbpt.petri.Transition>) retrievedTraces).getTransitions());

	    boolean bPrecision = true, bRecall = true;
		EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(
				relevantTraces, 
				retrievedTraces, 
				0, 
				0, 
				bPrecision, 
				bRecall, 
				false);

    	// calculate entropic relevance  
    	Pair<Double, Double> result = null;
		try {
			result = epr.computeMeasure();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        System.out.println(result);
		
    	return result.toString();    	
    }
    
    
    public static void main(String[] args) throws Exception {
		PluginContext context = new FakeContext();
		XLog log = (XLog) new OpenLogFileLiteImplPlugin().importFile(context, new File(
				"/Applications/Programming/xes/Road_Traffic_Fine_Management_Process.xes"));
    	XFactory factory= XFactoryRegistry.instance().currentDefault();
	    relevantTraces = log;
	    PNMLSerializer PNML = new PNMLSerializer();
		retrievedTraces = PNML.parse("/Applications/Programming/xes/rtf_spn.pnml");
		System.out.println("log size: "+relevantTraces);
        System.out.println("model size: "+retrievedTraces);

    
        boolean bPrecision = true, bRecall = true;
		EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(
				relevantTraces, 
				retrievedTraces, 
				0, 
				0, 
				bPrecision, 
				bRecall, 
				false);

    	// calculate entropic relevance  
    	Pair<Double, Double> result = null;
		try {
			result = epr.computeMeasure();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println(result);
    }
}