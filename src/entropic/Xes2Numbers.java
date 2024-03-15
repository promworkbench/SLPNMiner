package entropic;

import java.util.HashMap;
import java.util.stream.Collectors;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.pm.relevance.utils.XLogReader;

public class Xes2Numbers {
    public static void main(String[] args) throws Exception {
        XLog log = XLogReader.openLog(args[0]);
        HashMap<String, Integer> label2index = new HashMap<String, Integer>();
        for (XTrace trace : log) {
            for (XEvent event : trace) {
                String label = ((XAttribute)event.getAttributes().get("concept:name")).toString();
                if (label2index.containsKey(label)) continue;
                label2index.put(label, label2index.size());
            }
        }
        for (XTrace trace : log) {
            System.out.println(trace.stream().map(e -> ((Integer)label2index.get(((XAttribute)e.getAttributes().get("concept:name")).toString())).toString()).collect(Collectors.joining(",")));
        }
        System.out.println("done");
    }
}