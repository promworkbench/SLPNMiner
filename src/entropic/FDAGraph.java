package entropic;


import com.google.gson.Gson;

import entropic.FDAGArc;
import entropic.FDAGNode;

import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;

public class FDAGraph {
    private List<FDAGNode> nodes;
    private List<FDAGArc> arcs;

    public List<FDAGNode> getNodes() {
        return this.nodes;
    }

    public List<FDAGArc> getArcs() {
        return this.arcs;
    }

    public void toDot(PrintStream out) {
        out.println("digraph G {");
        this.nodes.forEach(n -> {
            if (n != null) {
                out.println(n.toDot());
            }
        });
        this.arcs.forEach(a -> {
            if (a != null) {
                out.println(a.toDot());
            }
        });
        out.println("}");
    }

    public static FDAGraph readJSON(String fileName) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson((Reader)new FileReader(fileName), FDAGraph.class);
    }
}

