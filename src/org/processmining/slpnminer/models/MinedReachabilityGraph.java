package org.processmining.slpnminer.models;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;

import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;
import org.processmining.slpnminer.models.reachabilitygraph.AcceptStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.ReachabilityGraph;
import org.processmining.slpnminer.models.reachabilitygraph.StartStateSet;
import org.processmining.slpnminer.models.reachabilitygraph.State;
import org.processmining.slpnminer.models.reachabilitygraph.Transition;

public class MinedReachabilityGraph {

	private static final float[] INITIAL_DASH_PATTERN = { 7f };

	@Plugin(name = "@0 Show Mined Reachability Graph", 
			returnLabels = { "Visualization of Mined Reachability Graph" },
			returnTypes = { JComponent.class },
			parameterLabels = { "Reachability graph" }, userAccessible = true)
	@Visualizer
	public JComponent visualize(PluginContext context, ReachabilityGraph ts) {
		/**
		 * Will hold the weights, start states, and accept states.
		 */
		DirectedGraphElementWeights weights = new DirectedGraphElementWeights();
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accepts = new AcceptStateSet();
		TSMinerInput settings;
		ProMJGraphPanel mainPanel;

		settings = null;

		/**
		 * 1. Tries to get connected transition weights from the framework.
		 */
		ConnectionManager cm = context.getConnectionManager();
		try {
			//			System.out.println("Checking for connection");
			TransitionSystemConnection tsc = cm.getFirstConnection(TransitionSystemConnection.class, context, ts);
			//			System.out.println("Checked for connection: " + settings);
			if (tsc.hasWeights()) {
				weights = tsc.getObjectWithRole(TransitionSystemConnection.WEIGHTS);
			}
			starts = tsc.getObjectWithRole(TransitionSystemConnection.STARTIDS);
			accepts = tsc.getObjectWithRole(TransitionSystemConnection.ACCEPTIDS);
			if (tsc.hasSettings()) {
				settings = (TSMinerInput) tsc.getObjectWithRole(TransitionSystemConnection.SETTINGS);
			}

		} catch (ConnectionCannotBeObtained e) {
			/**
			 * No connected transition weights found, no problem.
			 */
		}

		/**
		 * 2. Based on the connected objects found: updates visualization.
		 */
		if (!weights.isEmpty()) {
			/**
			 * Set the line widths according to the weights. To avoid getting
			 * ridiculous line widths: linewidth=ln(weight).
			 */
			for (State state : ts.getNodes()) {
				state.getAttributeMap().put(AttributeMap.LINEWIDTH,
						new Float(1 + Math.log(Math.E) * Math.log(weights.get(state.getIdentifier(), 1))));
			}
			for (Transition trans : ts.getEdges()) {
				trans.getAttributeMap().put(
						AttributeMap.LINEWIDTH,
						new Float(1
								+ Math.log(Math.E)
								* Math.log(weights.get(trans.getSource().getIdentifier(), trans.getTarget()
										.getIdentifier(), trans.getIdentifier(), 1))));
			}
		}
		if (!starts.isEmpty() || !accepts.isEmpty()) {
			for (State state : ts.getNodes()) {

				/**
				 * Note that, in fact, the set of start states is the the set of
				 * start state ids.
				 */
				if (starts.contains(state.getIdentifier())) {
					/**
					 * This state is a start state.
					 */
					state.getAttributeMap().put(AttributeMap.DASHPATTERN, INITIAL_DASH_PATTERN);
				}
				if (accepts.contains(state.getIdentifier())) {
					/**
					 * This state is an accept state.
					 */
					state.setAccepting(true);
				}
			}
		}

		mainPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, ts);
		mainPanel.getGraph().setEditable(false);


		if (settings != null) {
			mainPanel.addViewInteractionPanel(new MinedRg(context, ts, settings, mainPanel),
					SwingConstants.SOUTH);
		}

		return mainPanel;
	}
}
