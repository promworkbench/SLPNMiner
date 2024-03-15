package entropic;

public class FDAGArc {
    private Integer from;
    private Integer to;
    private Integer freq;

    public Integer getFrom() {
        return this.from;
    }

    public Integer getTo() {
        return this.to;
    }

    public Integer getFreq() {
        return this.freq;
    }

    public FDAGArc(Integer from, Integer to, Integer freq) {
        this.from = from;
        this.to = to;
        this.freq = freq;
    }

    public String toString() {
        return String.format("(%d) - [%d] -> (%d)", this.from, this.freq, this.to);
    }

    public String toDot() {
        return String.format("\tn%d -> n%d [label=\"%d\"];", this.from, this.to, this.freq);
    }
}

