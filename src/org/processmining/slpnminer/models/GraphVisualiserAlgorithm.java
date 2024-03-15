package org.processmining.slpnminer.models;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.slpnminer.parameters.GraphVisualiserParameters;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.shapes.Diamond;
import org.processmining.models.shapes.Ellipse;
import org.processmining.models.shapes.Hexagon;
import org.processmining.models.shapes.Octagon;
import org.processmining.models.shapes.Polygon;
import org.processmining.models.shapes.Rectangle;
import org.processmining.models.shapes.RoundedRect;
import org.processmining.models.shapes.Shape;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class GraphVisualiserAlgorithm {

	public static String GVPLACELABEL = "[GV]PlaceLabel";

	/**
	 * Create a JComponent using dot from the given graph.
	 * 
	 * @param context
	 *            The plug-in context. Not that relevant.
	 * @param graph
	 *            The graph to visualize using dot.
	 * @return The JComponent containing the dot visualization of the graph.
	 */
	public JComponent apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph) {
		return apply(((PluginContext) context), graph);

	}

	public JComponent apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph) {
		return apply(context, graph, new ViewSpecificAttributeMap(), new GraphVisualiserParameters());

	}

	public JComponent apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			GraphVisualiserParameters parameters) {
		return apply(((PluginContext) context), graph, parameters);

	}

	public JComponent apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			GraphVisualiserParameters parameters) {
		return apply(context, graph, new ViewSpecificAttributeMap(), parameters);

	}

	public JComponent apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map) {
		return apply(((PluginContext) context), graph, map);
	}

	public JComponent apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map) {
		return apply(context, graph, map, new GraphVisualiserParameters());
	}

	public JComponent apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, GraphVisualiserParameters parameters) {
		return apply(((PluginContext) context), graph, map, parameters);
	}

	public JComponent apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, GraphVisualiserParameters parameters) {
		Dot dot = new Dot();
		Map<DirectedGraphNode, DotNode> nodeMap = new HashMap<DirectedGraphNode, DotNode>();
		for (DirectedGraphNode node : graph.getNodes()) {
			DotNode dotNode = dot.addNode(node.getLabel());
			nodeMap.put(node, dotNode);
			apply(node, dotNode, map, parameters);
		}
		for (DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode> edge : graph.getEdges()) {
			DotEdge dotEdge = dot.addEdge(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()));
			apply(edge, dotEdge, map, parameters);
		}
		//		NavigableSVGPanel panel = new AnimatableSVGPanel(DotPanel.dot2svg(dot));
		return new DotPanel(dot);
	}

	/*
	 * Copy (as much as possible) the attributes from the JGraph node to the dot
	 * node.
	 */
	private void apply(DirectedGraphNode node, DotNode dotNode, ViewSpecificAttributeMap map,
			GraphVisualiserParameters parameters) {
		AttributeMap attMap = node.getAttributeMap();
		Shape shape = getShape(attMap, AttributeMap.SHAPE, null, node, map);
		String style = "filled";
		if (shape != null) {
			if (shape instanceof RoundedRect) {
				dotNode.setOption("shape", "box");
				style = style + ",rounded";
			} else if (shape instanceof Rectangle) {
				dotNode.setOption("shape", "box");
			} else if (shape instanceof Ellipse) {
				Boolean isSquare = getBoolean(attMap, AttributeMap.SQUAREBB, false, node, map);
				dotNode.setOption("shape", isSquare ? "circle" : "ellipse");
			} else if (shape instanceof Diamond) {
				dotNode.setOption("shape", "diamond");
			} else if (shape instanceof Hexagon) {
				dotNode.setOption("shape", "hexagon");
			} else if (shape instanceof Octagon) {
				dotNode.setOption("shape", "octagon");
			} else if (shape instanceof Polygon) {
				//				attMap.get(AttributeMap.POLYGON_POINTS);
				dotNode.setOption("shape", "polygon");
			}
			dotNode.setOption("style", style);
		}
		Boolean showLabel = getBoolean(attMap, AttributeMap.SHOWLABEL, true, node, map);
		// HV: Setting a tooltip seems to have no effect.
		String label = getString(attMap, AttributeMap.LABEL, "", node, map);
		String placeLabel = getString(attMap, GVPLACELABEL, "", node, map);
		String tooltip = getString(attMap, AttributeMap.TOOLTIP, "", node, map);
		String internalLabel = getFormattedString(parameters.getInternalLabelFormat(), label, placeLabel, tooltip);
		String externalLabel = getFormattedString(parameters.getExternalLabelFormat(), label, placeLabel, tooltip);
		tooltip = getFormattedString(parameters.getToolTipFormat(), label, placeLabel, tooltip);
		if (showLabel) {
			dotNode.setLabel(internalLabel);
		} else {
			dotNode.setLabel("");
		}
		dotNode.setOption("xlabel", externalLabel);
		dotNode.setOption("tooltip", tooltip);

		Float penWidth = getFloat(attMap, AttributeMap.LINEWIDTH, 1.0F, node, map);
		dotNode.setOption("penwidth", "" + penWidth);
		Color strokeColor = getColor(attMap, AttributeMap.STROKECOLOR, Color.BLACK, node, map);
		dotNode.setOption("color", ColourMap.toHexString(strokeColor));
		Color labelColor = getColor(attMap, AttributeMap.LABELCOLOR, Color.BLACK, node, map);
		dotNode.setOption("fontcolor", ColourMap.toHexString(labelColor));
		Color fillColor = getColor(attMap, AttributeMap.FILLCOLOR, Color.WHITE, node, map);
		Color gradientColor = getColor(attMap, AttributeMap.GRADIENTCOLOR, fillColor, node, map);
		if (gradientColor == null || gradientColor.equals(fillColor)) {
			dotNode.setOption("fillcolor", ColourMap.toHexString(fillColor));
		} else {
			dotNode.setOption("fillcolor",
					ColourMap.toHexString(fillColor) + ":" + ColourMap.toHexString(gradientColor));
		}

	}

	/*
	 * Copy (as much as possible) the attributes from the JGraph edge to the dot
	 * edge.
	 */
	private void apply(DirectedGraphEdge<?, ?> edge, DotEdge dotEdge, ViewSpecificAttributeMap map,
			GraphVisualiserParameters parameters) {
		AttributeMap attMap = edge.getAttributeMap();
		Boolean showLabel = getBoolean(attMap, AttributeMap.SHOWLABEL, false, edge, map);
		String label = getString(attMap, AttributeMap.LABEL, "", edge, map);
		dotEdge.setLabel(showLabel ? label : "");
		dotEdge.setOption("dir", "both");
		ArrowType endArrowType = getArrowType(attMap, AttributeMap.EDGEEND, ArrowType.ARROWTYPE_CLASSIC, edge, map);
		Boolean endIsFilled = getBoolean(attMap, AttributeMap.EDGEENDFILLED, false, edge, map);
		switch (endArrowType) {
			case ARROWTYPE_SIMPLE :
			case ARROWTYPE_CLASSIC :
				dotEdge.setOption("arrowhead", "open");
				break;
			case ARROWTYPE_TECHNICAL :
				dotEdge.setOption("arrowhead", endIsFilled ? "normal" : "empty");
				break;
			case ARROWTYPE_CIRCLE :
				dotEdge.setOption("arrowhead", endIsFilled ? "dot" : "odot");
				break;
			case ARROWTYPE_LINE :
				dotEdge.setOption("arrowhead", "tee");
				break;
			case ARROWTYPE_DIAMOND :
				dotEdge.setOption("arrowhead", endIsFilled ? "diamond" : "odiamond");
				break;
			case ARROWTYPE_NONE :
				dotEdge.setOption("arrowhead", "none");
				break;
			default :
				dotEdge.setOption("arrowhead", endIsFilled ? "box" : "obox");
				break;
		}
		ArrowType startArrowType = getArrowType(attMap, AttributeMap.EDGESTART, ArrowType.ARROWTYPE_NONE, edge, map);
		Boolean startIsFilled = getBoolean(attMap, AttributeMap.EDGESTARTFILLED, false, edge, map);
		dotEdge.setOption("arrowtail", "none");
		switch (startArrowType) {
			case ARROWTYPE_SIMPLE :
			case ARROWTYPE_CLASSIC :
				dotEdge.setOption("arrowtail", "open");
				break;
			case ARROWTYPE_TECHNICAL :
				dotEdge.setOption("arrowtail", startIsFilled ? "normal" : "empty");
				break;
			case ARROWTYPE_CIRCLE :
				dotEdge.setOption("arrowtail", startIsFilled ? "dot" : "odot");
				break;
			case ARROWTYPE_LINE :
				dotEdge.setOption("arrowtail", "tee");
				break;
			case ARROWTYPE_DIAMOND :
				dotEdge.setOption("arrowtail", startIsFilled ? "diamond" : "odiamond");
				break;
			case ARROWTYPE_NONE :
				dotEdge.setOption("arrowtail", "none");
				break;
			default :
				dotEdge.setOption("arrowtail", startIsFilled ? "box" : "obox");
				break;
		}
		Float penWidth = getFloat(attMap, AttributeMap.LINEWIDTH, 1.0F, edge, map);
		dotEdge.setOption("penwidth", "" + penWidth);
		Color edgeColor = getColor(attMap, AttributeMap.EDGECOLOR, Color.BLACK, edge, map);
		dotEdge.setOption("color", ColourMap.toHexString(edgeColor));
		Color labelColor = getColor(attMap, AttributeMap.LABELCOLOR, Color.BLACK, edge, map);
		dotEdge.setOption("fontcolor", ColourMap.toHexString(labelColor));
	}

	/*
	 * The following methods get the attribute value from the JGraph object with
	 * a given default value. If the object has no such attribute, the default
	 * value will be returned.
	 */

	private Boolean getBoolean(AttributeMap map, String key, Boolean value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Boolean) {
			return (Boolean) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Boolean) {
			return (Boolean) obj;
		}
		return value;
	}

	private Float getFloat(AttributeMap map, String key, Float value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Float) {
			return (Float) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Float) {
			return (Float) obj;
		}
		return value;
	}

	private String getString(AttributeMap map, String key, String value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof String) {
			/*
			 * Some labels contain HTML mark-up. Remove as much as possible.
			 */
			String s1 = ((String) obj).replaceAll("<br>", "\\\\n");
			String s2 = s1.replaceAll("<[^>]*>", "");
			return s2;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof String) {
			/*
			 * Some labels contain HTML mark-up. Remove as much as possible.
			 */
			String s1 = ((String) obj).replaceAll("<br>", "\\\\n");
			String s2 = s1.replaceAll("<[^>]*>", "");
			return s2;
		}
		return value;
	}

	private Color getColor(AttributeMap map, String key, Color value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Color) {
			return (Color) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Color) {
			return (Color) obj;
		}
		return value;
	}

	private ArrowType getArrowType(AttributeMap map, String key, ArrowType value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof ArrowType) {
			return (ArrowType) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof ArrowType) {
			return (ArrowType) obj;
		}
		return value;
	}

	private Shape getShape(AttributeMap map, String key, Shape value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Shape) {
			return (Shape) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Shape) {
			return (Shape) obj;
		}
		return value;
	}

	private String getFormattedString(String format, String label, String placeLabel, String tooltip) {
		String shortPlaceLabel = (placeLabel.length() > 5 ? placeLabel.substring(0, 4) : placeLabel);
		return format.replace("%l", label).replace("%p", placeLabel).replace("%s", shortPlaceLabel).replace("%t",
				tooltip);
	}
}
