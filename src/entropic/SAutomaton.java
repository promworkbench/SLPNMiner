package entropic;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import entropic.SATransition;

public class SAutomaton {
    private final Integer initialState;
    private final List<SATransition> transitions;
    private transient Set<Integer> states;
    private transient Table<Integer, String, Pair<Integer, Double>> transTable;
    private transient Map<Integer, Double> finalStates;

    public SAutomaton(List<SATransition> saTransitions, Integer initialState) {
        this.transitions = saTransitions;
        this.initialState = initialState;
        this.complete();
    }

    private SAutomaton complete() {
        return this.complete(1.0E-6);
    }

    private SAutomaton complete(double epsilon) {
        HashBasedTable<Integer, String, Pair<Integer, Double>> table = HashBasedTable.create();
        HashSet<Integer> stateSet = new HashSet<Integer>();
        HashMap<Integer, Double> outgoingProb = new HashMap<Integer, Double>();
        HashMap<Integer, Double> sinkAbsorvingProb = new HashMap<Integer, Double>();
        for (SATransition stTransition : this.transitions) {
            table.put(stTransition.getFrom(), stTransition.getLabel(), Pair.of(stTransition.getTo(), Math.log(stTransition.getProb())));
            stateSet.add(stTransition.getFrom());
            stateSet.add(stTransition.getTo());
            outgoingProb.put(stTransition.getFrom(), outgoingProb.getOrDefault(stTransition.getFrom(), 0.0) + stTransition.getProb());
        }
        for (Integer state : stateSet) {
            if (outgoingProb.containsKey(state) && !(1.0 - (Double)outgoingProb.get(state) > epsilon)) continue;
            sinkAbsorvingProb.put(state, Math.log(1.0 - outgoingProb.getOrDefault(state, 0.0)));
        }
        this.finalStates = sinkAbsorvingProb;
        this.transTable = table;
        this.states = stateSet;
        return this;
    }

    public static SAutomaton of(List<SATransition> saTransitions, Integer initialState) {
        return new SAutomaton(saTransitions, initialState);
    }

    public Integer getInitialState() {
        return this.initialState;
    }

    public Table<Integer, String, Pair<Integer, Double>> getTransitions() {
        return this.transTable;
    }

    public Set<Integer> getStates() {
        return this.states;
    }

    public static SAutomaton readJSON(String fileName) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader((InputStream)new FileInputStream(fileName), "UTF-8"));
        Gson gson = new Gson();
        SAutomaton automaton = (SAutomaton)gson.fromJson(reader, (Type)((Object)SAutomaton.class));
        automaton.complete();
        return automaton;
    }

    public void toJSON(String filename) throws Exception {
        FileWriter writer = new FileWriter(filename);
        Gson gson = new Gson();
        IOUtils.write(gson.toJson(this), (Writer)writer);
        writer.flush();
        writer.close();
    }

    public boolean isFinalState(Integer state) {
        return this.finalStates.containsKey(state);
    }

    public double getFinalStateProb(Integer state) {
        return this.finalStates.get(state);
    }
}

