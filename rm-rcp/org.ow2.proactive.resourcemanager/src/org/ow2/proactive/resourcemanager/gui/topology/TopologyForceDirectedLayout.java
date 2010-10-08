package org.ow2.proactive.resourcemanager.gui.topology;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;


public class TopologyForceDirectedLayout extends ForceDirectedLayout {

    public final static float COEFF_MIN = 0;
    public final static float COEFF_MAX = 400;
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
        return super.getSpringCoefficient(arg0);

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
