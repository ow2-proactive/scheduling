package org.ow2.proactive.resourcemanager.gui.topology.prefuse;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 * A ControlListener that sets the highlighted status for edges neighboring the
 * node currently under the mouse pointer. The highlight flag might then be used
 * by a color function to change node appearance as desired.
 */
public class NeighborEdgesHighlightControl extends ControlAdapter {

    private String activity = null;

    /**
     * Creates a new highlight control.
     */
    public NeighborEdgesHighlightControl() {
        this(null);
    }

    /**
     * Creates a new highlight control that runs the given activity whenever the
     * neighbor highlight changes.
     *
     * @param activity
     *            the update Activity to run
     */
    public NeighborEdgesHighlightControl(String activity) {
        this.activity = activity;
    }

    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem)
            setNeighborHighlight((NodeItem) item, true);
    }

    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem)
            setNeighborHighlight((NodeItem) item, false);
    }

    /**
     * Set the highlighted state of the neighbors of a node.
     *
     * @param n
     *            the node under consideration
     * @param state
     *            the highlighting state to apply to neighbors
     */
    protected void setNeighborHighlight(NodeItem n, boolean state) {
        Iterator iter = n.edges();
        while (iter.hasNext()) {
            EdgeItem eitem = (EdgeItem) iter.next();
            // NodeItem nitem = eitem.getAdjacentItem(n);
            if (eitem.isVisible()) {
                eitem.setHighlighted(state);
                // nitem.setHighlighted(state);
            }
        }
        if (activity != null)
            n.getVisualization().run(activity);
    }
} // end of class NeighborHighlightControl