package entropic;

import org.deckfour.xes.model.XLog;

public class Test {
	
	public static void main(String[] args) throws Exception {
		XLog log = XLogReader.openLog("/Applications/Programming/Artem-Entropic-Relevance/jbpt-pm/examples/log2.xes");
    	String relevance = "";
    	
		SAutomaton sa = SAutomaton.readJSON("/Applications/Programming/Artem-Entropic-Relevance/jbpt-pm/examples/automaton.sdfa");
		relevance = Relevance.compute(log, sa, false).toString();
    	System.out.println(relevance);
    	
	}

}
