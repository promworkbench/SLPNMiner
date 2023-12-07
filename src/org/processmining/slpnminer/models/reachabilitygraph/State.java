package org.processmining.slpnminer.models.reachabilitygraph;

import org.processmining.framework.util.Cast;
import org.processmining.framework.util.HTMLToString;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.shapes.Decorated;
import org.processmining.models.shapes.Ellipse;
import org.processmining.slpnminer.models.AttributeMap;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;

public class State extends AbstractDirectedGraphNode implements Decorated {

    /**
     * this object identifies the state.
     */
    private final Object identifier;
    private final ReachabilityGraph graph;

    /**
     * This accepting is stored for painting the state in the graph (see method
     * decorate).
     */
    private boolean accepting;
    private boolean initiating;
    private HashSet<Transition> preTransSet;  // store the previous transition that lead to this state

    public State(Object identifier, ReachabilityGraph graph) {
        super();
        this.identifier = identifier;
        this.graph = graph;
        this.preTransSet = new HashSet<Transition>();
        getAttributeMap().put(AttributeMap.SHAPE, new Ellipse());
        getAttributeMap().put(AttributeMap.SQUAREBB, false);
        getAttributeMap().put(AttributeMap.RESIZABLE, true);
        getAttributeMap().put(AttributeMap.SIZE, new Dimension(100, 60));
        getAttributeMap().put(AttributeMap.FILLCOLOR, Color.LIGHT_GRAY);
        if (identifier instanceof HTMLToString) {
            getAttributeMap().put(AttributeMap.LABEL, Cast.<HTMLToString>cast(identifier).toHTMLString(true));
        } else {
            getAttributeMap().put(AttributeMap.LABEL, identifier.toString());
        }
        getAttributeMap().put(AttributeMap.SHOWLABEL, false);
        getAttributeMap().put(AttributeMap.AUTOSIZE, false);
        setAccepting(false);
    }

    public Object getIdentifier() {
        return identifier;
    }

    public boolean equals(Object o) {
        return (o instanceof State ? identifier.equals(((State) o).identifier) : false);
    }

    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public ReachabilityGraph getGraph() {
        return graph;
    }

    public void setInitiating(boolean initiating) {
        this.initiating = initiating;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public boolean isInitiating() {
        return initiating;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setLabel(String label) {
        getAttributeMap().put(AttributeMap.LABEL, label);
    }

    public String getLabel() {
        return (String) getAttributeMap().get(AttributeMap.LABEL);
    }
    
    public void addPreTrans(Transition t) {
    	preTransSet.add(t);
    }
    
    public HashSet<Transition> getPreTransSet(){
    	return preTransSet;
    }

    /**
     * If this state is an accepting state, then an extra line is painted in the
     * GUI as the border of this state.
     */
    public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
        if (isAccepting()) {
            Float line = getAttributeMap().get(AttributeMap.LINEWIDTH, 1f);
            int pointOffset = 3 + line.intValue() / 2;
            int sizeOffset = (2 * pointOffset) + 1;
            // Remember current stroke.
            Stroke stroke = g2d.getStroke();
            // Use thin line for extra line.
            g2d.setStroke(new BasicStroke(1f));
            // Draw extra line.
            g2d.draw(new Ellipse2D.Double(x + pointOffset, y + pointOffset, width - sizeOffset, height - sizeOffset));
            // Reset remembered stroke.
            g2d.setStroke(stroke);
        }
    }
}
