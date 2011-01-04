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
package org.ow2.proactive.resourcemanager.gui.topology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.Cluster;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.AggregateDragControl;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.AggregateLayout;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.GraphLatencyFilter;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.HighLightPredicate;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.HostRenderer;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.LatencyForce;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.NeighborEdgesHighlightControl;
import org.ow2.proactive.resourcemanager.gui.topology.prefuse.TopologyForceDirectedLayout;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;


/**
 * Represents all resource manager infrastructure in compact matrix view. It
 * holds a tree of views which correspond to the resource manager data model and
 * displays them in matrix way.
 */
public class TopologyViewer {

    /** parent composite */
    private Composite parent;

    /** Graph representing the latencies between nodes */
    private Graph graph;
    /** Topology displayed **/
    private Topology topology;

    private final Lock lock = new ReentrantLock();
    /** Lock used to manager 'selectedItems' list */
    private final Lock selectionLock = new ReentrantLock();

    private Set<VisualItem> selectedItems = new HashSet<VisualItem>();
    /** Biggest latency of the graph */
    private long maxLatency;
    private int nbClusters = 0;

    /*
     *  GUI Objects
     */
    /** Main frame */
    private java.awt.Frame frame;
    private prefuse.Display display;
    private Visualization visualization;
    /** Slider for selecting the latency threshold of nodes to display */
    private JValueSlider latencySlider;
    private ColorAction edgeColor;
    private TopologyForceDirectedLayout topologyForceDirectedLayout;
    private GraphLatencyFilter latencyFilter;
    /** palette of color used to draw edges in function of their latency */
    int[] defaultPalette = new int[226];
    //private ButtonGroup clusteringMode = new ButtonGroup();

    /** Name of the graph */
    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String CLUSTERS = "clusters";
    public static final String EDGE_DECORATORS = "edgeDeco";

    private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
    static {
        DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(60));
        DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 11));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates new TopologyViewer.
     */
    public TopologyViewer(final Composite parent) {
        this.parent = parent;
    }

    /**
     * Initialization of TopologyViewer.
     */
    public void init() {
        initComposite();
        initLayout();
        if (ResourceExplorerView.getTreeViewer() != null) {
            ResourceExplorerView.getTreeViewer().addSelectionChangedListener(new TopologySelectionListener());
        }
    }

    /**
     * Initialization of composite.
     */
    private void initComposite() {
        parent.setLayout(new FillLayout());
        parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    }

    /**
     * Layout initialization. SWT RowLayout is used to automatically control
     * resizing of the view.
     */
    private void initLayout() {
        loadMatrix();
    }

    public void loadMatrix() {
        if (RMStore.isConnected()) {
            topology = RMStore.getInstance().getResourceManager().getTopology();
            loadMatrix(topology);
        }
    }

    /**
     * Add a new Proactive Node to the graph
     *
     * @param nodeURL
     *            URL of the node
     */
    public void addNode(final Node node) {
        lock.lock();
        boolean init = false;
        try {
            if (visualization == null) {
                init = true;
                loadMatrix();
            }
        } finally {
            lock.unlock();
        }
        if (!init && RMStore.isConnected() && node.getState() != NodeState.DOWN) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    lock.lock();
                    synchronized (visualization) {
                        try {
                            topology = RMStore.getInstance().getResourceManager().getTopology();
                            String host = node.getParent().getParent().getName();
                            // Topology topology =
                            // RMStore.getInstance().getResourceManager().getTopology();

                            prefuse.data.Node hostNode = null;
                            Iterator nodes = graph.nodes();
                            while (nodes.hasNext()) {
                                prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();
                                if (anotherNode.get("name").equals(host)) {
                                    hostNode = anotherNode;
                                    break;
                                }
                            }

                            if (hostNode != null) {
                                Set<Node> hostNodes = (Set) hostNode.get("nodes");
                                hostNodes.add(node);
                            } else {
                                prefuse.data.Node nodeToAdd = graph.addNode();
                                nodeToAdd.set("name", host);
                                Set<Node> hostNodes = new HashSet<Node>();
                                hostNodes.add(node);
                                nodeToAdd.set("nodes", hostNodes);
                                nodes = graph.nodes();
                                while (nodes.hasNext()) {
                                    prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();

                                    if (nodeToAdd == anotherNode) {
                                        continue;
                                    }

                                    String anotherHost = (String) anotherNode.get("name");
                                    Long latency = topology.getDistance(host, anotherHost);

                                    if (latency != null && latency < Long.MAX_VALUE) {
                                        //										if (graph.getEdge(nodeToAdd,anotherNode) == null
                                        //												&& graph.getEdge(anotherNode,nodeToAdd) == null) {
                                        if (latency > maxLatency) {
                                            maxLatency = latency;
                                            Number oldMaxValue = latencySlider.getMaxValue();
                                            latencySlider.setMaxValue(maxLatency);
                                            if (oldMaxValue.longValue() == 1 ||
                                                oldMaxValue.equals(latencySlider.getValue())) {
                                                latencySlider.setValue(maxLatency);
                                                latencySlider.fireChangeEvent();
                                            }
                                        }

                                        Edge edge = graph.addEdge(nodeToAdd, anotherNode);
                                        edge.setInt("weight", latency.intValue());
                                    }
                                    //									}
                                }
                                visualization.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE);

                                if (graph.getNodeCount() == 2) {
                                    ActionList dynamicColor = (ActionList) visualization
                                            .removeAction("dynamicColor");
                                    dynamicColor.add(0, edgeColor);
                                    visualization.putAction("dynamicColor", dynamicColor);
                                }

                                visualization.run("unique");
                                visualization.run("dynamicColor");
                                visualization.run("layout");
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }
    }

    /**
     * Remove a node from the graph
     *
     * @param nodeURL
     *            URL of the node to remove
     */
    public void removeNode(final Node node, final String host) {
        if (visualization != null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    lock.lock();
                    synchronized (visualization) {
                        try {
                            String nodeURL = node.getName();
                            // String host =
                            // node.getParent().getParent().getName();

                            prefuse.data.Node hostNode = null;
                            Iterator nodes = graph.nodes();
                            while (nodes.hasNext()) {
                                prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();
                                if (anotherNode.get("name").equals(host)) {
                                    hostNode = anotherNode;
                                    break;
                                }
                            }
                            if (hostNode == null) {
                                return;
                            }

                            Set<Node> hostNodes = (Set<Node>) hostNode.get("nodes");
                            Iterator<Node> it = hostNodes.iterator();
                            while (it.hasNext()) {
                                Node node = it.next();
                                if (node.getName().equals(nodeURL)) {
                                    it.remove();
                                    break;
                                }
                            }
                            if (hostNodes.size() == 0) {
                                // No more Proactive Nodes on this host
                                // Remove Prefuse node and all incident edges
                                graph.removeNode(hostNode);
                                if (graph.getNodeCount() == 1) {
                                    ((ActionList) visualization.getAction("dynamicColor")).remove(edgeColor);
                                }
                            }

                            visualization.run("latencyFilter");
                            visualization.run("unique");
                            visualization.run("dynamicColor");
                            visualization.run("layout");
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }
    }

    /**
     * Loads data from resource manager model.
     *
     * @param topology
     */
    public void loadMatrix(final Topology topology) {
        if (RMStore.isConnected()) {
            lock.lock();
            try {
                if (visualization == null) {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            graph = createGraph(topology);
                            if (graph == null) {
                                return;
                            }

                            visualization = new Visualization();
                            // Add the graph representing the topology
                            visualization.add(GRAPH, graph);

                            // Add an aggregate table which represent clusters
                            AggregateTable clusters = visualization.addAggregates(CLUSTERS);
                            clusters.addColumn(VisualItem.POLYGON, float[].class);
                            clusters.addColumn("id", int.class);

                            // draw the "name" label for Nodes
                            HostRenderer nodeRenderer = new HostRenderer("name");
                            nodeRenderer.setRoundedCorner(8, 8); // round the
                            // corners

                            DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);

                            // draw clusters as polygons with curved edges
                            Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
                            ((PolygonRenderer) polyR).setCurveSlack(0.15f);
                            rf.add(new InGroupPredicate(CLUSTERS), polyR);

                            // decorator which draw latencies of edges
                            rf.add(new InGroupPredicate(EDGE_DECORATORS), new LabelRenderer("weight"));

                            // create a new default renderer factory
                            visualization.setRendererFactory(rf);

                            // adding edge decorators,
                            visualization.addDecorators(EDGE_DECORATORS, EDGES, new HighLightPredicate(),
                                    DECORATOR_SCHEMA);

                            // Fill color of nodes
                            final ColorAction colorFillNodes = new ColorAction(NODES, VisualItem.FILLCOLOR,
                                ColorLib.rgb(255, 255, 255));
                            colorFillNodes.add(VisualItem.HOVER, ColorLib.rgb(255, 100, 100));
                            colorFillNodes.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 100, 100));

                            // palette of color used to draw edges in function
                            // of their latency
                            defaultPalette = new int[226];
                            final int[] fixedPalette = new int[226];
                            final int[] highlightPalette = new int[226];
                            for (int i = 0; i < 226; i++) {
                                fixedPalette[i] = ColorLib.rgba(17, 48, 250, i + 30);
                                highlightPalette[i] = ColorLib.rgba(255, 119, 22, i + 30);
                                defaultPalette[i] = ColorLib.rgba(50, 50, 50, i + 15);
                            }
                            // Color of edges
                            edgeColor = new DataColorAction(EDGES, "weight", Constants.NUMERICAL,
                                VisualItem.STROKECOLOR, defaultPalette);
                            edgeColor.add(VisualItem.HIGHLIGHT, new DataColorAction(EDGES, "weight",
                                Constants.NUMERICAL, VisualItem.STROKECOLOR, highlightPalette));
                            edgeColor.add(VisualItem.FIXED, new DataColorAction(EDGES, "weight",
                                Constants.NUMERICAL, VisualItem.STROKECOLOR, fixedPalette));

                            // Text color of edges
                            final ColorAction colorTextEdges = new ColorAction(EDGES, VisualItem.TEXTCOLOR,
                                ColorLib.alpha(0));
                            colorTextEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 119, 22));
                            colorTextEdges.add(VisualItem.FIXED, ColorLib.rgba(17, 48, 250, 240));

                            // Cluster stroke color
                            final ColorAction clusterStroke = new ColorAction(CLUSTERS,
                                VisualItem.STROKECOLOR);
                            clusterStroke.setDefaultColor(ColorLib.gray(50));
                            clusterStroke.add(VisualItem.HOVER, ColorLib.rgb(255, 100, 100));

                            // Cluster fill color
                            int[] palette = new int[] { ColorLib.rgba(255, 200, 200, 150),
                                    ColorLib.rgba(200, 255, 200, 150), ColorLib.rgba(200, 200, 255, 100),
                                    ColorLib.rgba(255, 100, 0, 100), ColorLib.rgba(204, 51, 204, 100),
                                    ColorLib.rgba(255, 204, 0, 100), ColorLib.rgba(0, 204, 204, 100) };
                            final ColorAction clusterFill = new DataColorAction(CLUSTERS, "id",
                                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

                            // Latency filter
                            latencyFilter = new GraphLatencyFilter(GRAPH, maxLatency, false);

                            // action list containing all static colors
                            final ActionList staticActions = new ActionList();
                            staticActions.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.gray(0)));
                            staticActions.add(new ColorAction(NODES, VisualItem.STROKECOLOR, ColorLib.rgb(0,
                                    0, 200)));
                            staticActions.add(clusterFill);

                            // action list containing all dynamic colors
                            final ActionList dynamicColor = new ActionList();
                            dynamicColor.add(colorFillNodes);
                            if (graph.getNodeCount() > 1) {
                                dynamicColor.add(edgeColor);
                            }
                            dynamicColor.add(new LabelLayout(EDGE_DECORATORS));
                            dynamicColor.add(new RepaintAction());

                            // Force simulator used by the animated layout
                            ForceSimulator m_fsim = new ForceSimulator();
                            m_fsim.addForce(new NBodyForce());
                            m_fsim.addForce(new LatencyForce());
                            m_fsim.addForce(new DragForce());

                            // create an action list with an animated layout
                            // the INFINITY parameter tells the action list to
                            // run indefinitely
                            final ActionList layout = new ActionList(5000);
                            topologyForceDirectedLayout = new TopologyForceDirectedLayout(GRAPH, m_fsim,
                                false);
                            layout.add(topologyForceDirectedLayout);
                            layout.add(new LabelLayout(EDGE_DECORATORS));
                            layout.add(new RepaintAction());

                            final ActionList clustersAL = new ActionList(Activity.INFINITY);
                            clustersAL.add(clusterStroke);
                            clustersAL.add(new AggregateLayout(CLUSTERS));
                            clustersAL.add(new RepaintAction());

                            // Put actions
                            visualization.putAction("unique", staticActions);
                            visualization.putAction("latencyFilter", latencyFilter);
                            visualization.putAction("layout", layout);
                            visualization.putAction("dynamicColor", dynamicColor);
                            //visualization.putAction("cluster", clustersAL);

                            // create a new Display that pull from our
                            // Visualization
                            display = new prefuse.Display(visualization);
                            display.setPreferredSize(new Dimension(parent.getSize().x - 310,
                                parent.getSize().y));
                            display.setHighQuality(true);
                            display.addControlListener(new SelectNodeControl(1)); // select nodes
                            display.addControlListener(new AggregateDragControl()); // drag nodes and clusters
                            display.addControlListener(new PanControl()); // pan with background left-drag
                            display.addControlListener(new ZoomControl()); // zoom with vertical right-drag
                            display.addControlListener(new WheelZoomControl()); // mouse zoom
                            display.addControlListener(new NeighborEdgesHighlightControl("dynamicColor")); // sets the highlighted status for edges of the node under the mouse pointer

                            buildGUI();

                            visualization.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE); // Cannot select edges
                            visualization.run("unique");
                            visualization.run("dynamicColor");
                            visualization.run("layout"); // start up the animated layout
                            display.pan(display.getWidth() / 2.0, display.getHeight() / 2.0);
                        }
                    });
                } else {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            graph = createGraph(topology);
                            if (graph == null) {
                                return;
                            }
                            visualization.add(GRAPH, graph);
                        }
                    });
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Construct all GUI related components
     */
    private void buildGUI() {
        /*
         * Construction of the Option Panel
         */
        final JPanel fpanel = new JPanel();
        fpanel.setBackground(Color.WHITE);
        fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.Y_AXIS));
        fpanel.setPreferredSize(new Dimension(300, parent.getSize().y));
        fpanel.setMaximumSize(new Dimension(300, parent.getSize().y));

        final JValueSlider springForceSlider = new JValueSlider("Density",
            TopologyForceDirectedLayout.COEFF_MIN, TopologyForceDirectedLayout.COEFF_MAX,
            topologyForceDirectedLayout.getSpringLenghtCoeff());
        springForceSlider.setBackground(Color.WHITE);
        springForceSlider.setPreferredSize(new Dimension(300, 40));
        springForceSlider.setMaximumSize(new Dimension(300, 40));
        springForceSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                topologyForceDirectedLayout.setSpringLengthCoeff(springForceSlider.getValue().floatValue());
                visualization.run("layout");
            }
        });

        //	TODO: add clustering information to the topology view
        //        // Slider to select number of clusters
        //        final JValueSlider clustersSlider = new JValueSlider("Clusters", 0, 6, 0);
        //        clustersSlider.setBackground(Color.WHITE);
        //        clustersSlider.setPreferredSize(new Dimension(300, 22));
        //        clustersSlider.setMaximumSize(new Dimension(300, 22));
        //        clustersSlider.addChangeListener(new ChangeListener() {
        //            public void stateChanged(ChangeEvent e) {
        //                int nb = clustersSlider.getValue().intValue();
        //                if (nb != nbClusters) {
        //                    nbClusters = nb;
        //                    clusterize();
        //                }
        //            }
        //        });
        //
        //        JRadioButton max = new JRadioButton("MAX");
        //        max.setSelected(true);
        //        JRadioButton avg = new JRadioButton("AVG");
        //        JRadioButton min = new JRadioButton("MIN");
        //
        //        clusteringMode.add(max);
        //        clusteringMode.add(avg);
        //        clusteringMode.add(min);
        //
        //        max.addActionListener(new ActionListener() {
        //			public void actionPerformed(ActionEvent e) {
        //				String clusteringModeStr = clusteringMode.getSelection().toString();
        //				if (nbClusters <= 0 ) {
        //					return;
        //				}
        //		        List<Cluster<String>> clusters = topology.clusterize(nbClusters, BestProximityDescriptor.MAX);
        //		        clusterize(clusters);
        //			}
        //		});
        //        avg.addActionListener(new ActionListener() {
        //			public void actionPerformed(ActionEvent e) {
        //				if (nbClusters <= 0 ) {
        //					return;
        //				}
        //		        List<Cluster<String>> clusters = topology.clusterize(nbClusters, BestProximityDescriptor.AVG);
        //		        clusterize(clusters);
        //			}
        //		});
        //        min.addActionListener(new ActionListener() {
        //			public void actionPerformed(ActionEvent e) {
        //				if (nbClusters <= 0 ) {
        //					return;
        //				}
        //		        List<Cluster<String>> clusters = topology.clusterize(nbClusters, BestProximityDescriptor.MIN);
        //		        clusterize(clusters);
        //			}
        //		});
        //
        //        // box of 'clusterAlgo' combobox + label
        //        Box clusterAlgoBox = new Box(BoxLayout.X_AXIS);
        //        clusterAlgoBox.setPreferredSize(new Dimension(300, 20));
        //        clusterAlgoBox.setMaximumSize(new Dimension(300, 20));
        //        clusterAlgoBox.add(Box.createVerticalGlue());
        //        JLabel lab = new JLabel("Mode");
        //        lab.setToolTipText("Set clustering selection mode");
        //        clusterAlgoBox.add(lab);
        //        clusterAlgoBox.add(Box.createRigidArea(new Dimension(70, 20)));
        //        clusterAlgoBox.add(max);
        //        clusterAlgoBox.add(avg);
        //        clusterAlgoBox.add(min);
        //
        //        // Box containing ClusersSlider and
        //        Box ClusterBox = new Box(BoxLayout.Y_AXIS);
        //        ClusterBox.setPreferredSize(new Dimension(300, 44));
        //        ClusterBox.setMaximumSize(new Dimension(300, 44));
        //        ClusterBox.add(clustersSlider);
        //        ClusterBox.add(clusterAlgoBox);

        // max latency slider, set the latency threshold
        latencySlider = new JValueSlider("Threshold", 0, maxLatency > 0 ? maxLatency : 1, maxLatency);
        latencySlider.setToolTipText("Set the latency threshold of neighbours of selected nodes to display");
        latencySlider.setBackground(Color.WHITE);
        latencySlider.setPreferredSize(new Dimension(300, 40));
        latencySlider.setMaximumSize(new Dimension(300, 40));
        latencySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                latencyFilter.setLatencyThreshold(latencySlider.getValue().longValue());
                latencyFilter.run();
                visualization.run("dynamicColor");
            }
        });

        // checkbox to show/hide edges of unselected nodes
        final JCheckBox displayEdges = new JCheckBox("");
        displayEdges.setBackground(Color.WHITE);
        displayEdges.setToolTipText("Show/hide edges of unselected nodes");
        displayEdges.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (displayEdges.isSelected()) {
                    for (int i = 0; i < 226; i++) {
                        defaultPalette[i] = ColorLib.rgba(50, 50, 50, i + 15);
                    }
                    visualization.run("dynamicColor");
                } else {
                    for (int i = 0; i < 226; i++) {
                        defaultPalette[i] = ColorLib.rgba(0, 0, 0, 0);
                    }
                    visualization.run("dynamicColor");
                }
            }
        });
        displayEdges.setSelected(true);
        displayEdges.doClick();

        // Box of 'displayEdges' checkbox
        Box displayEdgesBox = new Box(BoxLayout.X_AXIS);
        displayEdgesBox.setPreferredSize(new Dimension(300, 40));
        displayEdgesBox.setMaximumSize(new Dimension(300, 40));
        displayEdgesBox.add(Box.createVerticalGlue());
        JLabel lab2 = new JLabel("Show edges");
        lab2.setToolTipText("Show/hide edges of unselected nodes");
        displayEdgesBox.add(lab2);
        displayEdgesBox.add(Box.createRigidArea(new Dimension(30, 40)));
        displayEdgesBox.add(displayEdges);

        // box of visibility filter
        Box visibilityFilterBox = new Box(BoxLayout.Y_AXIS);
        visibilityFilterBox.add(springForceSlider);
        //visibilityFilterBox.add(ClusterBox);
        visibilityFilterBox.add(latencySlider);
        visibilityFilterBox.add(displayEdgesBox);
        visibilityFilterBox.setBorder(BorderFactory.createTitledBorder("Visibility filter"));
        fpanel.add(visibilityFilterBox);

        final JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        mainPanel.add(display);
        mainPanel.add(fpanel);

        /*
         * Frame Listeners
         */
        if (frame == null) {
            Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
            frame = SWT_AWT.new_Frame(swtAwtComponent);
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                if (visualization != null) {
                    visualization.run("layout");
                }
            }

            public void windowDeactivated(WindowEvent e) {
                if (visualization != null) {
                    visualization.cancel("layout");
                }
            }
        });

        // Resize listener
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (visualization != null) {
                    Dimension d = new Dimension(e.getComponent().getSize().width - 315, e.getComponent()
                            .getSize().height);
                    display.setPreferredSize(d);
                    fpanel.setPreferredSize(new Dimension(300, e.getComponent().getSize().height));
                    mainPanel.doLayout();
                }
            }
        });

        MouseListener focus = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!frame.hasFocus()) {
                    frame.requestFocus();
                }
            }
        };
        frame.addMouseListener(focus);
        display.addMouseListener(focus);

        frame.add(mainPanel);
        frame.pack();
        parent.layout();
    }

    /**
     * Creates a graph from RM's topology
     *
     * @param topology
     * @return
     */
    private Graph createGraph(Topology topology) {
        if (topology != null) {
            Graph graph = new Graph();
            graph.getNodeTable().addColumns(
                    new Schema(new String[] { "name", "nodes" }, new Class[] { String.class, Set.class }));
            graph.getEdgeTable().addColumns(new Schema(new String[] { "weight" }, new Class[] { int.class }));
            List<TreeLeafElement> leafsToProcess = new ArrayList<TreeLeafElement>();
            Map<String, prefuse.data.Node> hosts = new HashMap<String, prefuse.data.Node>();
            leafsToProcess.add(RMStore.getInstance().getModel().getRoot());

            while (leafsToProcess.size() > 0) {
                TreeLeafElement element = leafsToProcess.remove(0);
                if (element instanceof TreeParentElement) {
                    for (TreeLeafElement elem : ((TreeParentElement) element).getChildren()) {
                        leafsToProcess.add(elem);
                    }
                } else {
                    // element is a Proactive Node
                    String host = element.getParent().getParent().getName();
                    NodeState state = ((Node) element).getState();
                    if (state == NodeState.BUSY || state == NodeState.FREE) {
                        if (hosts.containsKey(host)) {
                            // Add a node to an already existing host
                            prefuse.data.Node node = hosts.get(host);
                            Set<Node> hostNodes = (Set<Node>) node.get("nodes");
                            hostNodes.add((Node) element);
                        } else {
                            // New host to add to the graph
                            prefuse.data.Node node = graph.addNode();
                            node.set("name", host);
                            // node.set("address", host);
                            Set<Node> hostNodes = new HashSet<Node>();
                            hostNodes.add((Node) element);
                            node.set("nodes", hostNodes);
                            hosts.put(host, node);
                        }
                    }
                }
            }

            maxLatency = 0; // Biggest latency of the graph
            for (prefuse.data.Node node1 : hosts.values()) {
                for (prefuse.data.Node node2 : hosts.values()) {
                    if (node1 == node2)
                        continue;

                    String hostAdress = (String) node1.get("name");
                    String anotherHostAdress = (String) node2.get("name");
                    Long latency = topology.getDistance(hostAdress, anotherHostAdress);

                    if (latency != null && latency < Long.MAX_VALUE) {
                        if (graph.getEdge(node1, node2) == null && graph.getEdge(node1, node2) == null) {
                            if (latency > maxLatency) {
                                maxLatency = latency;
                            }
                            Edge edge = graph.addEdge(node1, node2);
                            edge.setInt("weight", latency.intValue());
                        }
                    }
                }
            }
            return graph;
        }
        return null;
    }

    /**
     * Clears view. Removes all elements and resets internal states.
     */
    public void clear() {
        if (visualization != null) {
            for (ComponentListener cl : frame.getComponentListeners()) {
                frame.removeComponentListener(cl);
            }
            for (WindowListener wl : frame.getWindowListeners()) {
                frame.removeWindowListener(wl);
            }
            frame.removeAll();
            frame.setBackground(Color.WHITE);
            frame.repaint();
            visualization.removeAction("unique");
            visualization.removeAction("latencyFilter");
            visualization.removeAction("layout");
            visualization.removeAction("dynamicColor");
            visualization.reset();
            visualization = null;
        }
    }

    public void setFocus() {
        if (frame != null) {
            frame.requestFocus();
        }
    }

    public void setSelection(List<Node> elements) {
        if (visualization != null) {
            Set<String> hosts = new HashSet<String>();
            Set<VisualItem> selectedItems = new HashSet<VisualItem>();
            for (Node node : elements) {
                String host = node.getParent().getParent().getName();
                hosts.add(host);
            }
            TupleSet focusGroup = visualization.getFocusGroup(Visualization.FOCUS_ITEMS);
            focusGroup.clear();
            TupleSet src = visualization.getGroup(NODES);
            Iterator nodeIterator = src.tuples();
            while (nodeIterator.hasNext()) {
                NodeItem node = (NodeItem) nodeIterator.next();
                String host = node.getString("name");
                if (hosts.contains(host)) {
                    focusGroup.addTuple(node);
                    selectedItems.add(node);
                }
            }
            this.setSelectecItems(selectedItems);
        }
    }

    public void setSelectecItems(Set<VisualItem> items) {
        selectionLock.lock();
        try {
            for (VisualItem item : selectedItems) {
                item.setHighlighted(false);
            }
            this.selectedItems = items;
            for (VisualItem item : selectedItems) {
                item.setHighlighted(false);
                item.setHighlighted(true);
            }
            visualization.run("latencyFilter");
            visualization.run("dynamicColor");
        } finally {
            selectionLock.unlock();
        }
    }

    public void addToSelectedItems(VisualItem item) {
        selectionLock.lock();
        try {
            selectedItems.add(item);
            item.setHighlighted(false);
            item.setHighlighted(true);
            visualization.run("latencyFilter");
            visualization.run("dynamicColor");
        } finally {
            selectionLock.unlock();
        }
    }

    public void removeFromSelectedItems(VisualItem item) {
        selectionLock.lock();
        try {
            selectedItems.remove(item);
            item.setHighlighted(false);
            visualization.run("latencyFilter");
            visualization.run("dynamicColor");
        } finally {
            selectionLock.unlock();
        }
    }

    public boolean IsInSelectedItems(VisualItem item) {
        selectionLock.lock();
        try {
            return selectedItems.contains(item);
        } finally {
            selectionLock.unlock();
        }
    }

    public void clusterize(List<Cluster<String>> clusters) {
        AggregateTable at = (AggregateTable) visualization.getGroup(CLUSTERS);
        Table nodes = graph.getNodeTable();
        SearchQueryBinding searchQ;
        at.clear();
        if (clusters == null || clusters.size() == 0) {
            // don't display clusters
            return;
        }
        int i = 0;
        for (Cluster<String> clusterHosts : clusters) {
            AggregateItem cluster = (AggregateItem) at.addItem();
            cluster.setInt("id", i++);
            for (String host : clusterHosts.getElements()) {
                searchQ = new SearchQueryBinding(nodes, "name");
                SearchTupleSet sts = searchQ.getSearchSet();
                sts.search(host);
                if (sts.getTupleCount() == 1) {
                    TableTuple t = (TableTuple) sts.tuples().next();
                    cluster.addItem(visualization.getVisualItem(NODES, t));
                } else
                    System.out.println("ERREUR : TopologyViewer.clusterize() taille incorecte res = " + sts);
            }
        }
        visualization.run("unique");
        if (nbClusters > 0) {
            visualization.run("cluster");
        }
    }

    class SelectNodeControl extends ControlAdapter {
        protected VisualItem curFocus;
        protected int ccount = 1;
        protected int button = Control.LEFT_MOUSE_BUTTON;

        public SelectNodeControl(int clicks) {
            ccount = clicks;
        }

        /**
         * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem,
         *      java.awt.event.MouseEvent)
         */
        public void itemClicked(VisualItem item, MouseEvent e) {
            if (UILib.isButtonPressed(e, button) && e.getClickCount() == ccount) {
                if (item instanceof NodeItem) {
                    TupleSet focusGroup = visualization.getFocusGroup(Visualization.FOCUS_ITEMS);
                    if (item != curFocus) {
                        // TupleSet ts = vis.getFocusGroup(group);

                        boolean ctrl = e.isControlDown();
                        if (!ctrl) {
                            curFocus = item;
                            // ts.setTuple(item);
                            Set<VisualItem> selectedItems = new HashSet<VisualItem>();
                            selectedItems.add(item);
                            focusGroup.clear();
                            focusGroup.setTuple(item);
                            setSelectecItems(selectedItems);
                        } else if (IsInSelectedItems(item)) {
                            focusGroup.removeTuple(item);
                            removeFromSelectedItems(item);
                        } else {
                            focusGroup.addTuple(item);
                            addToSelectedItems(item);
                        }

                    } else if (e.isControlDown()) {
                        focusGroup.removeTuple(item);
                        removeFromSelectedItems(item);
                        curFocus = null;
                    }
                }
            }
        }
    } // end of class FocusControl
}

/**
 * Set label positions. Labels are assumed to be DecoratorItem instances,
 * decorating their respective nodes. The layout simply gets the bounds of the
 * decorated node and assigns the label coordinates to the center of those
 * bounds.
 */
class LabelLayout extends Layout {
    public LabelLayout(String group) {
        super(group);
    }

    public void run(double frac) {
        Iterator iter = m_vis.items(m_group);
        while (iter.hasNext()) {
            DecoratorItem decorator = (DecoratorItem) iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();

            double x = bounds.getCenterX();
            double y = bounds.getCenterY();

            // modification to move edge labels more to the arrow head
            double x2 = 0, y2 = 0;
            if (decoratedItem instanceof EdgeItem) {
                VisualItem dest = ((EdgeItem) decoratedItem).getTargetItem();
                if (dest.isHover()) {
                    dest = ((EdgeItem) decoratedItem).getSourceItem();
                }
                x2 = dest.getBounds().getCenterX();
                y2 = dest.getBounds().getCenterY();
                x = (x + 1.5 * x2) / 2.5;
                y = (y + 1.5 * y2) / 2.5;
            }

            setX(decorator, null, x);
            setY(decorator, null, y);
        }
    }
} // end of inner class LabelLayout

