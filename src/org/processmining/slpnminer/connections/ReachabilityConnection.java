package org.processmining.slpnminer.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.slpnminer.models.reachabilitygraph.AcceptStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.StartStateSet;

public class ReachabilityConnection extends AbstractConnection {
    public final static String TS = "TS";
    public final static String WEIGHTS = "Weights";
    public final static String STARTIDS = "Start ids";
    public final static String ACCEPTIDS = "Accept ids";
    public final static String SETTINGS = "Settings";
    private boolean hasWeights;
    private boolean hasSettings;

    public ReachabilityConnection(ReachabilityGraph ts, DirectedGraphElementWeights weights, StartStateSet starts,
                                      AcceptStateSet accepts) {
        super(ts.getLabel() + " and related mined information");
        init(ts, weights, starts, accepts, null);
    }

    public ReachabilityConnection(ReachabilityGraph ts, StartStateSet starts, AcceptStateSet accepts) {
        super(ts.getLabel() + " and related mined information");
        init(ts, null, starts, accepts, null);
    }

    public ReachabilityConnection(ReachabilityGraph ts, DirectedGraphElementWeights weights, StartStateSet starts,
                                      AcceptStateSet accepts, Object settings) {
        super(ts.getLabel() + " and related mined information");
        init(ts, weights, starts, accepts, settings);
    }

    private void init(ReachabilityGraph ts, DirectedGraphElementWeights weights, StartStateSet starts,
                      AcceptStateSet accepts, Object settings) {
        put(TS, ts);
        if (weights != null) {
            put(WEIGHTS, weights);
            hasWeights = true;
        } else {
            hasWeights = false;
        }
        put(STARTIDS, starts);
        put(ACCEPTIDS, accepts);
        if (settings != null) {
            put(SETTINGS, settings);
            hasSettings = true;
        } else {
            hasSettings = false;
        }
    }

    public boolean hasWeights() {
        return hasWeights;
    }

    public boolean hasSettings() {
        return hasSettings;
    }
}

