package entropic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.deckfour.xes.model.XTrace;

import entropic.SimpleBackgroundModel;

public class EventFrequencyBasedBackgroundModel
extends SimpleBackgroundModel {
    Map<String, Integer> freqActionInLog = new HashMap<String, Integer>();
    Map<String, Map<String, Integer>> freqActionInTrace = new HashMap<String, Map<String, Integer>>();
    Map<String, Integer> tempFreqActionInLog;
    boolean nonFittingSubLog;
    int lengthOfLog = 0;

    public EventFrequencyBasedBackgroundModel(boolean nonFittingSubLog) {
        this.nonFittingSubLog = nonFittingSubLog;
    }

    @Override
    public void openTrace(XTrace trace) {
        super.openTrace(trace);
        this.tempFreqActionInLog = new HashMap<String, Integer>();
    }

    @Override
    public void processEvent(String eventLabel, double probability) {
        super.processEvent(eventLabel, probability);
        if (!this.nonFittingSubLog) {
            this.freqActionInLog.put(eventLabel, this.freqActionInLog.getOrDefault(eventLabel, 0) + 1);
        }
        this.tempFreqActionInLog.put(eventLabel, this.tempFreqActionInLog.getOrDefault(eventLabel, 0) + 1);
    }

    @Override
    public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
        super.closeTrace(trace, fitting, finalStateProb);
        if (!this.freqActionInTrace.containsKey(this.largeString)) {
            this.freqActionInTrace.put(this.largeString, this.tempFreqActionInLog);
        }
        if (this.nonFittingSubLog && !fitting) {
            for (Map.Entry<String, Integer> eventLabel : this.tempFreqActionInLog.entrySet()) {
                this.freqActionInLog.put(eventLabel.getKey(), this.freqActionInLog.getOrDefault(eventLabel.getKey(), 0) + eventLabel.getValue());
            }
        }
    }

    protected int actionsInLog(Map<String, Integer> freqActionInLog) {
        return freqActionInLog.values().stream().mapToInt(i -> i).sum() + this.lengthOfLog;
    }

    protected double p(String element, Map<String, Integer> freqActionInLog) {
        return (double)freqActionInLog.get(element).intValue() / (double)this.actionsInLog(freqActionInLog);
    }

    @Override
    protected double costBitsUnfittingTraces(String traceId) {
        double bits = 0.0;
        this.lengthOfLog = this.nonFittingSubLog ? this.totalNumberOfNonFittingTraces : this.totalNumberOfTraces;
        for (Map.Entry<String, Integer> eventFrequency : this.freqActionInTrace.get(traceId).entrySet()) {
            bits -= EventFrequencyBasedBackgroundModel.log2(this.p(eventFrequency.getKey(), this.freqActionInLog)) * (double)eventFrequency.getValue().intValue();
        }
        return bits -= EventFrequencyBasedBackgroundModel.log2((double)this.lengthOfLog / (double)this.actionsInLog(this.freqActionInLog));
    }

    @Override
    protected double costFrequencyDistribution() {
        double bits = 0.0;
        this.lengthOfLog = this.nonFittingSubLog ? this.totalNumberOfNonFittingTraces : this.totalNumberOfTraces;
        for (String label : this.labels) {
            bits += 2.0 * Math.floor(EventFrequencyBasedBackgroundModel.log2(this.freqActionInLog.getOrDefault(label, 0) + 1)) + 1.0;
        }
        return bits += 2.0 * Math.floor(EventFrequencyBasedBackgroundModel.log2(this.lengthOfLog + 1)) + 1.0;
    }
}

    