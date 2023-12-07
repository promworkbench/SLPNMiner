package entropic;

public class FDAGNode {
    private Integer id;
    private String label;
    private Integer freq;

    public Integer getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public Integer getFreq() {
        return this.freq;
    }

    public FDAGNode(Integer id, String label, Integer freq) {
        this.id = id;
        this.label = label;
        this.freq = freq;
    }

    public String toString() {
        return "{id=" + this.id + ", label='" + this.label + '\'' + ", freq=" + this.freq + '}';
    }

    public String toDot() {
        return String.format("\tn%d [label=\"%s\\n%d\"];", this.id, this.label, this.freq);
    }
}

