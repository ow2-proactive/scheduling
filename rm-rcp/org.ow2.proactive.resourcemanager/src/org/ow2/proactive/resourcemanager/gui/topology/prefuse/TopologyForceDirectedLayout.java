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

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;


public class TopologyForceDirectedLayout extends ForceDirectedLayout {

    public final static float COEFF_MIN = 0;
    public final static float COEFF_MAX = 2000;
    private float coeff = 80;

    /**
     * Create a new ForceDirectedLayout. By default, this layout will not
     * restrict the layout to the layout bounds and will assume it is being
     * run in animated (rather than run-once) fashion.
     * @param graph the data group to layout. Must resolve to a Graph instance.
     */
    public TopologyForceDirectedLayout(String graph) {
        this(graph, false, false);
    }

    /**
     * Create a new ForceDirectedLayout. The layout will assume it is being
     * run in animated (rather than run-once) fashion.
     * @param group the data group to layout. Must resolve to a Graph instance.
     * @param enforceBounds indicates whether or not the layout should require
     * that all node placements stay within the layout bounds.
     */
    public TopologyForceDirectedLayout(String group, boolean enforceBounds) {
        this(group, enforceBounds, false);
    }

    /**
     * Create a new ForceDirectedLayout.
     * @param group the data group to layout. Must resolve to a Graph instance.
     * @param enforceBounds indicates whether or not the layout should require
     * that all node placements stay within the layout bounds.
     * @param runonce indicates if the layout will be run in a run-once or
     * animated fashion. In run-once mode, the layout will run for a set number
     * of iterations when invoked. In animation mode, only one iteration of the
     * layout is computed.
     */
    public TopologyForceDirectedLayout(String group, boolean enforceBounds, boolean runonce) {
        super(group, enforceBounds, runonce);
    }

    /**
     * Create a new ForceDirectedLayout. The layout will assume it is being
     * run in animated (rather than run-once) fashion.
     * @param group the data group to layout. Must resolve to a Graph instance.
     * @param fsim the force simulator used to drive the layout computation
     * @param enforceBounds indicates whether or not the layout should require
     * that all node placements stay within the layout bounds.
     */
    public TopologyForceDirectedLayout(String group, ForceSimulator fsim, boolean enforceBounds) {
        this(group, fsim, enforceBounds, false);
    }

    /**
     * Create a new ForceDirectedLayout.
     * @param group the data group to layout. Must resolve to a Graph instance.
     * @param fsim the force simulator used to drive the layout computation
     * @param enforceBounds indicates whether or not the layout should require
     * that all node placements stay within the layout bounds.
     * @param runonce indicates if the layout will be run in a run-once or
     * animated fashion. In run-once mode, the layout will run for a set number
     * of iterations when invoked. In animation mode, only one iteration of the
     * layout is computed.
     */
    public TopologyForceDirectedLayout(String group, ForceSimulator fsim, boolean enforceBounds,
            boolean runonce) {
        super(group, fsim, enforceBounds, runonce);
    }

    @Override
    protected float getMassValue(VisualItem arg0) {
        //		 return 1f;
        return super.getMassValue(arg0);
    }

    @Override
    protected float getSpringCoefficient(EdgeItem arg0) {
        //		return arg0.getFloat("weight")/10;
        //		forcePanel.get
        return SpringForce.DEFAULT_MIN_SPRING_COEFF;

    }

    public void setSpringLengthCoeff(float coeff) {
        if (coeff >= COEFF_MIN && coeff <= COEFF_MAX) {
            this.coeff = coeff;
        }
    }

    public float getSpringLenghtCoeff() {
        return coeff;
    }

    @Override
    protected float getSpringLength(EdgeItem arg0) {
        double val = arg0.getLong("weight") / 150.0;
        return (float) val * (coeff + 40);
    }

}
