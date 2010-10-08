package org.ow2.proactive.resourcemanager.gui.topology;

import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


public class ColorStrokeEdgePredicate extends AbstractPredicate {

    private Visualization vis;
    private String group;
    private boolean fixed = false;

    /**
     * Creates a new ValidEdgePredicate.
     * @param group the group to process. This group should resolve to a
     * Graph instance, otherwise exceptions will be thrown when this
     * Action is run.
     * @param g the backing graph, the node table of this graph will be used
     * to check for valid edges.
     */
    public ColorStrokeEdgePredicate(String group, Visualization g) {
        this.group = group;
        this.vis = g;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Indicates if the given tuple can be used as a valid edge for
     * the nodes of the backing graph.
     * @param tpl a data tuple from a potential edge table
     * @return true if the tuple contents allow it to serve as a valid
     * edge of between nodes in the backing graph
     */
    public boolean getBoolean(Tuple tpl) {
        VisualItem item = vis.getVisualItem(group, tpl);
        if (item instanceof EdgeItem) {
            EdgeItem ei = (EdgeItem) item;
            NodeItem n1 = ei.getSourceItem();
            NodeItem n2 = ei.getTargetItem();
            return n1.isFixed() && n2.isFixed() && !n1.isHover() && !n2.isHover();
        }
        return false;
    }
}
