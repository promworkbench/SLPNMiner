package entropic;

public class SATransition {
    private Integer from;
    private Integer to;
    private String label;
    private Double prob;

    public Integer getFrom() {
        return this.from;
    }

    public Integer getTo() {
        return this.to;
    }

    public String getLabel() {
        return this.label;
    }

    public Double getProb() {
        return this.prob;
    }

    public SATransition(Integer from, Integer to, String label, Double probability) {
        this.from = from;
        this.to = to;
        this.label = label;
        this.prob = probability;
    }

    public String toString() {
        return String.format("(%d) - %s [%10.8f] -> (%d)", this.from, this.label, this.prob, this.to);
    }
}

