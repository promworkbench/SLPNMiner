package entropic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.deckfour.xes.model.XTrace;

import entropic.ReplayInformationGatherer;

public class SimpleBackgroundModel
implements ReplayInformationGatherer {
    int numberOfEvents = 0;
    int totalNumberOfTraces = 0;
    int totalNumberOfNonFittingTraces = 0;
    Set<String> labels = new HashSet<String>();
    Map<String, Integer> traceFrequency = new HashMap<String, Integer>();
    Map<String, Integer> traceSize = new HashMap<String, Integer>();
    Map<String, Double> log2OfModelProbability = new HashMap<String, Double>();
    double lprob = 0.0;
    String largeString = "";

    public static double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    public static double h0(int accumulated_rho, double totalNumberOfTraces) {
        if (accumulated_rho == 0 || (double)accumulated_rho == totalNumberOfTraces) {
            return 0.0;
        }
        double p = (double)accumulated_rho / totalNumberOfTraces;
        return -p * SimpleBackgroundModel.log2(p) - (1.0 - p) * SimpleBackgroundModel.log2(1.0 - p);
    }

    @Override
    public void openTrace(XTrace trace) {
        this.lprob = 0.0;
        this.largeString = "";
    }

    @Override
    public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
        this.traceSize.put(this.largeString, trace.size());
        ++this.totalNumberOfTraces;
        if (fitting) {
            this.log2OfModelProbability.put(this.largeString, (this.lprob + finalStateProb.get()) / Math.log(2.0));
        } else {
            ++this.totalNumberOfNonFittingTraces;
        }
        this.traceFrequency.put(this.largeString, this.traceFrequency.getOrDefault(this.largeString, 0) + 1);
    }

    @Override
    public void processEvent(String eventLabel, double probability) {
        this.largeString = this.largeString + eventLabel;
        ++this.numberOfEvents;
        this.labels.add(eventLabel);
        this.lprob += probability;
    }

    protected double costBitsUnfittingTraces(String traceId) {
        return (double)(1 + this.traceSize.get(traceId)) * SimpleBackgroundModel.log2(1 + this.labels.size());
    }

    protected double costFrequencyDistribution() {
        return 0.0;
    }

    public Map<String, Object> computeRelevance(boolean full) {
        int accumulated_rho = 0;
        double accumulated_cost_bits = 0.0;
        double accumulated_temp_cost_bits = 0.0;
        double accumulated_prob_fitting_traces = 0.0;
        double costFreqDistribuPerTrace = 0.0;
        for (String traceString : this.traceFrequency.keySet()) {
            double traceFreq = this.traceFrequency.get(traceString).intValue();
            double cost_bits = 0.0;
            double nftrace_cost_bits = 0.0;
            if (this.log2OfModelProbability.containsKey(traceString)) {
                cost_bits = -this.log2OfModelProbability.get(traceString).doubleValue();
                accumulated_rho = (int)((double)accumulated_rho + traceFreq);
            } else {
                nftrace_cost_bits = cost_bits = this.costBitsUnfittingTraces(traceString);
            }
            accumulated_temp_cost_bits += nftrace_cost_bits * traceFreq;
            accumulated_cost_bits += cost_bits * traceFreq / (double)this.totalNumberOfTraces;
            if (!this.log2OfModelProbability.containsKey(traceString)) continue;
            accumulated_prob_fitting_traces += traceFreq / (double)this.totalNumberOfTraces;
        }
        costFreqDistribuPerTrace = this.costFrequencyDistribution() / (double)this.totalNumberOfTraces;
        HashMap<String, Object> result = new HashMap<String, Object>();
        if (full) {
            result.put("coverage", accumulated_prob_fitting_traces);
            result.put("costOfBackgroundModel", accumulated_temp_cost_bits / (double)this.totalNumberOfTraces);
            result.put("costOfFrequencyDistribution", costFreqDistribuPerTrace);
        }
        result.put("relevance", SimpleBackgroundModel.h0(accumulated_rho, this.totalNumberOfTraces) + accumulated_cost_bits + costFreqDistribuPerTrace);
        return result;
    }
}