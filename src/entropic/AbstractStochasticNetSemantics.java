package entropic;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetExecutionInformation;

public class AbstractStochasticNetSemantics {

	protected Marking state;
	private Collection<TimedTransition> transitions;

	public AbstractStochasticNetSemantics() {
		this(null);
	}

	public AbstractStochasticNetSemantics(Marking state) {
		this.state = state;

	}

	public void initialize(Collection<TimedTransition> transitions, Marking state) {
		this.transitions = transitions;
		setCurrentState(state);
	}

	protected Collection<TimedTransition> getTransitions() {
		return Collections.unmodifiableCollection(transitions);
	}
	
	protected boolean isEnabled(Marking state, Marking required, TimedTransition t) {
		if (required.isLessOrEqual(state)) {
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : t.getGraph().getInEdges(t)) {
				if (e instanceof InhibitorArc) {
					InhibitorArc arc = (InhibitorArc) e;
					if (state.occurrences(arc.getSource()) > 0) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public Marking getCurrentState() {
		return state;
	}

	public void setCurrentState(Marking currentState) {
		state = currentState;
	}

	public PetrinetExecutionInformation executeExecutableTransition(TimedTransition toExecute)
			throws IllegalTransitionException {
		Marking required = getRequired(toExecute);
		Marking newState = new Marking(state);
		if (!isEnabled(state, required, toExecute)) {
			throw new IllegalTransitionException(toExecute, newState);
		}
		Marking produced = getProduced(toExecute);
		newState.addAll(produced);
		Marking toRemove = getRemoved(toExecute);
		newState.removeAll(toRemove);
		state = newState;
		return new PetrinetExecutionInformation(required, toRemove, produced, toExecute);
	}

	public Collection<TimedTransition> getExecutableTransitions() {
		if (state == null) {
			return null;
		}
		// the tokens are divided over the places according to state
		Collection<TimedTransition> enabled = new ArrayList<TimedTransition>();
		for (TimedTransition trans : getTransitions()) {
			
			if (isEnabled(state, getRequired(trans), trans)) {
				enabled.add(trans);
			}
		}
		return enabled;
	}

	protected Marking getRequired(TimedTransition trans) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = trans.getGraph().getInEdges(
				trans);
		Marking required = new Marking();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : edges) {
			if (e instanceof Arc) {
				Arc arc = (Arc) e;
				required.add((Place) arc.getSource(), arc.getWeight());
			}
		}
		return required;

	}

	protected Marking getProduced(TimedTransition trans) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = trans.getGraph().getOutEdges(
				trans);
		Marking produced = new Marking();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : edges) {
			if (e instanceof Arc) {
				Arc arc = (Arc) e;
				produced.add((Place) arc.getTarget(), arc.getWeight());
			}
		}

		return produced;

	}

	protected Marking getRemoved(TimedTransition trans) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = trans.getGraph().getInEdges(
				trans);
		Marking removed = new Marking(getRequired(trans));
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : edges) {
			if (e instanceof ResetArc) {
				ResetArc arc = (ResetArc) e;
				removed.add(arc.getSource(), state.occurrences(arc.getSource()));
			}
		}
		return removed;
	}

	public String toString() {
		return "Regular Semantics";
	}

	public int hashCode() {
		return getClass().hashCode();
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return this.getClass().equals(o.getClass());
	}
}
