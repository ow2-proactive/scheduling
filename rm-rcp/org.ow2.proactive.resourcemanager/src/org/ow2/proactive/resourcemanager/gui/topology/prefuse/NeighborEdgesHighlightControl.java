/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
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