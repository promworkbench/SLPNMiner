//package org.processmining.slpnminer.plugins;
//
//import javax.swing.JComponent;
//import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
//import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
//import org.processmining.contexts.uitopia.annotations.Visualizer;
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.framework.plugin.annotations.Plugin;
//import org.processmining.framework.plugin.annotations.PluginLevel;
//import org.processmining.framework.plugin.annotations.PluginVariant;
//import org.processmining.slpnminer.algorithms.GraphVisualiserAlgorithm;
//import org.processmining.slpnminer.models.MinedReachabilityGraph;
//import org.processmining.slpnminer.models.ReachabilityGraph;
//import org.processmining.slpnminer.parameters.GraphVisualiserParameters;
//import org.processmining.models.graphbased.ViewSpecificAttributeMap;
//import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
//import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
//import org.processmining.models.graphbased.directed.petrinet.ResetNet;
//import org.processmining.models.graphbased.directed.petrinet.elements.Place;
//import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
//import org.processmining.models.jgraph.ProMJGraph;
//import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
//import org.processmining.plugins.petrinet.PetriNetVisualization;
//import org.processmining.plugins.transitionsystem.MinedTSVisualization;
//
//public class ReachabilityGraphVisualiserPlugin extends GraphVisualiserAlgorithm {
//
//	@Plugin(name = "Visualize Transition System (Dot)",returnLabels = {
//			"Visualized Transition System" }, returnTypes = { JComponent.class }, parameterLabels = {
//					"Causal Activity Matrix" }, userAccessible = true)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
//	public JComponent runUI(PluginContext context, ReachabilityGraph rg) {
//		/*
//		 * Get a hold on the view specific attributes.
//		 */
//
//		System.out.println("use the vis for ts");
//		ProMJGraphPanel panel = (ProMJGraphPanel) (new MinedReachabilityGraph()).visualize(context, rg);
//		ProMJGraph jGraph = panel.getGraph();
//		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
//		/*
//		 * Got it. Now create the dot panel.
//		 */
//		return apply(context, rg, map);
//	}
//
//}
