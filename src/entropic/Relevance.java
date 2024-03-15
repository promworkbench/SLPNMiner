package entropic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import com.google.common.collect.Table;

public class Relevance {
    public static void scanAndProcess(XLog log, SAutomaton automaton, ReplayInformationGatherer infoGatherer) {
        Table<Integer, String, Pair<Integer, Double>> transitions = automaton.getTransitions();
        Integer initialState = automaton.getInitialState();
        for (XTrace trace : log) {
            Integer curr = initialState;
            boolean nonfitting = false;
            infoGatherer.openTrace(trace);
            for (XEvent event : trace) {
                if (event.getAttributes().get("concept:name") == null || event.getAttributes().containsKey("lifecycle:transition") && !((XAttribute)event.getAttributes().get("lifecycle:transition")).toString().toUpperCase().equals("COMPLETE")) continue;
                String label = ((XAttribute)event.getAttributes().get("concept:name")).toString();
                double prob = 0.0;
                if (!nonfitting && transitions.contains(curr, label)) {
                    Pair<Integer, Double> pair = transitions.get(curr, label);
                    curr = pair.getLeft();
                    prob = pair.getRight();
                } else {
                    nonfitting = true;
                }
                infoGatherer.processEvent(label, prob);
            }
            if (!nonfitting && automaton.isFinalState(curr)) {
                infoGatherer.closeTrace(trace, true, Optional.of(automaton.getFinalStateProb(curr)));
                continue;
            }
            infoGatherer.closeTrace(trace, false, Optional.empty());
        }
    }

    private static Map<String, Object> run(XLog log, SAutomaton automaton, boolean full, SimpleBackgroundModel analyzer) {
        Relevance.scanAndProcess(log, automaton, analyzer);
        HashMap<String, Object> result = new HashMap<String, Object>(analyzer.computeRelevance(full));
        if (full) {
            // empty if block
        }
        return result;
    }

    public static Map<String, Object> compute(XLog log, SAutomaton automaton, boolean full) {
        return Relevance.run(log, automaton, full, new SimpleBackgroundModel());
    }

    public static Map<String, Object> computeNew(XLog log, SAutomaton automaton, boolean full, boolean nonFittingSubLog) {
        return Relevance.run(log, automaton, full, new EventFrequencyBasedBackgroundModel(nonFittingSubLog));
    }
}