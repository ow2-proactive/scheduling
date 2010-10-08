package org.ow2.proactive.resourcemanager.gui.topology.prefuse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.data.util.FilterIterator;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;


/**
 * Filter Action that sets visible all items within a specified graph distance
 * from a set of focus items; all other items will be set to invisible.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphLatencyFilter extends GroupAction {

    protected long m_latency;
    protected String m_sources;
    protected Predicate m_groupP;
    protected String edgeGroupName;
    protected String nodeGroupName;
    protected BreadthFirstIterator m_bfs;
    protected List<NodeItem> hidenNodes;
    protected boolean exclusive;

    /**
     * Create a new GraphDistanceFilter that processes the given data group
     * and uses the given graph distance. By default, the
     * {@link prefuse.Visualization#FOCUS_ITEMS} group will be used as the
     * source nodes from which to measure the distance.
     * @param group the group to process. This group should resolve to a
     * Graph instance, otherwise exceptions will be thrown when this
     * Action is run.
     * @param latency the graph distance within which items will be
     * visible.
     */
    public GraphLatencyFilter(String group, long latency, boolean exclusive) {
        this(group, Visualization.FOCUS_ITEMS, latency, exclusive);
    }

    /**
     * Create a new GraphDistanceFilter that processes the given data group
     * and uses the given graph distance.
     * @param group the group to process. This group should resolve to a
     * Graph instance, otherwise exceptions will be thrown when this
     * Action is run.
     * @param sources the group to use as source nodes for measuring
     * graph distance.
     * @param latency the graph distance within which items will be
     * visible.
     */
    public GraphLatencyFilter(String group, String sources, long latency, boolean exclusive) {
        super(group);
        hidenNodes = new ArrayList<NodeItem>();
        m_sources = sources;
        m_latency = latency;
        m_groupP = new InGroupPredicate(PrefuseLib.getGroupName(group, Graph.NODES));
        edgeGroupName = PrefuseLib.getGroupName(group, Graph.EDGES);
        nodeGroupName = PrefuseLib.getGroupName(group, Graph.NODES);
        m_bfs = new BreadthFirstIterator();
        this.exclusive = exclusive;
    }

    /**
     * Return the graph distance threshold used by this filter.
     * @return the graph distance threshold
     */
    public long getLatencyThreshold() {
        return m_latency;
    }

    /**
     * Set the graph distance threshold used by this filter.
     * @param latency the graph distance threshold to use
     */
    public void setLatencyThreshold(long latency) {
        m_latency = latency;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    /**
     * Get the name of the group to use as source nodes for measuring
     * graph distance. These form the roots from which the graph distance
     * is measured.
     * @return the source data group
     */
    public String getSources() {
        return m_sources;
    }

    /**
     * Set the name of the group to use as source nodes for measuring
     * graph distance. These form the roots from which the graph distance
     * is measured.
     * @param sources the source data group
     */
    public void setSources(String sources) {
        m_sources = sources;
    }

    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {
        // mark the items
        Iterator items = m_vis.visibleItems(m_group);
        while (items.hasNext()) {
            VisualItem item = (VisualItem) items.next();
            item.setDOI(Constants.MINIMUM_DOI);
        }

        while (!hidenNodes.isEmpty()) {
            NodeItem node = hidenNodes.remove(0);
            Iterator it = node.edges();
            while (it.hasNext()) {
                EdgeItem eitem = (EdgeItem) it.next();
                eitem.setVisible(true);
                if (eitem.getTargetItem().isHover() || eitem.getSourceItem().isHover()) {
                    eitem.setHighlighted(true);
                }
            }
            node.setVisible(true);
        }

        // set up the graph traversal
        TupleSet src = m_vis.getGroup(m_sources);
        if (src.getTupleCount() > 0) {
            Iterator srcs = new FilterIterator(src.tuples(), m_groupP); // Fixed nodes
            Set<NodeItem> visiblesNodes = new HashSet<NodeItem>();
            while (srcs.hasNext()) {
                NodeItem fixedNode = (NodeItem) (srcs.next());
                Iterator edgesIt = fixedNode.edges();
                while (edgesIt.hasNext()) {
                    EdgeItem edge = (EdgeItem) (edgesIt.next());
                    NodeItem node = edge.getAdjacentItem(fixedNode);
                    Integer latency = (Integer) (edge.get("weight"));
                    if (!exclusive && (latency <= m_latency || node.isFixed())) {
                        visiblesNodes.add(node);
                    } else if (exclusive && latency > m_latency && !node.isFixed()) {
                        hidenNodes.add(node);
                    }
                }
                visiblesNodes.add(fixedNode);
            }

            TupleSet tuples = m_vis.getGroup(nodeGroupName);
            Iterator nodeIterator = tuples.tuples();
            while (nodeIterator.hasNext()) {
                NodeItem node = (NodeItem) (nodeIterator.next());
                if ((!exclusive && !visiblesNodes.contains(node)) || (exclusive && hidenNodes.contains(node))) {
                    if (!exclusive) {
                        hidenNodes.add(node);
                    }
                    Iterator it = node.edges();
                    while (it.hasNext()) {
                        EdgeItem eitem = (EdgeItem) it.next();
                        eitem.setVisible(false);
                        eitem.setHighlighted(false);
                        //						eitem.setFixed(false);
                    }
                    node.setVisible(false);
                }
            }
        }
    }

}
