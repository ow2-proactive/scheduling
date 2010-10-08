/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 * Represents all resource manager infrastructure in compact matrix view. It
 * holds a tree of views which correspond to the resource manager data model and
 * displays them in matrix way.
 */
public class TopologyViewer {

    // parent composite
    private Composite parent;

    /** Biggest latency of the graph */
    private long maxLatency;
    /** Name of the graph */
    private static String graphName = "graph";
    /** Graph representing the latencies between nodes */
    private Graph graph;
    /** Main frame */
    private java.awt.Frame frame;
    /** Slider for selecting the latency threshold of nodes to display */
    private JValueSlider latencySlider;
    private Visualization visualization;
    private TopologyForceDirectedLayout topologyForceDirectedLayout;

    private final Lock lock = new ReentrantLock();

    /**
     * Creates new CompactViewer.
     */
    public TopologyViewer(final Composite parent) {
        this.parent = parent;
    }

    /**
     * Initialization of CompactViewer.
     */
    public void init() {
        initComposite();
        initLayout();
    }

    /**
     * Initialization of composite.
     */
    private void initComposite() {
        parent.setLayout(new FillLayout());
        parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        //composite = new Composite(parent, SWT.NONE);
        //Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        //        composite.setBackground(white);
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
            Topology topology = RMStore.getInstance().getResourceManager().getTopology();
            loadMatrix(topology);
        }
    }

    /**
     * Add a new Proactive Node to the graph
     * @param nodeURL URL of the node
     */
    public void addNode(final String nodeURL) {
        System.out.println("TopologyViewer.addNode() " + nodeURL);
        lock.lock();
        try {
            if (visualization == null) {
                loadMatrix();
            } else if (RMStore.isConnected()) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        lock.lock();
                        synchronized (visualization) {
                            try {
                                String host = nodeURL.split("//")[1].split(":")[0];
                                Topology topology = RMStore.getInstance().getResourceManager().getTopology();

                                InetAddress address;
                                try {
                                    address = InetAddress.getByName(host);
                                } catch (UnknownHostException e) {
                                    System.out.println(e);
                                    return;
                                }

                                Iterator nodes = graph.nodes();
                                while (nodes.hasNext()) {
                                    prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();
                                    if (anotherNode.get("address").equals(address)) {
                                        // TODO modif
                                        return;
                                    }
                                }

                                prefuse.data.Node nodeToAdd = graph.addNode();
                                nodeToAdd.set("name", host);
                                nodeToAdd.set("address", address);
                                nodes = graph.nodes();
                                while (nodes.hasNext()) {
                                    prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();

                                    if (nodeToAdd == anotherNode) {
                                        continue;
                                    }

                                    InetAddress anotherHostAddress = (InetAddress) anotherNode.get("address");
                                    Long latency = topology.getDistance(address, anotherHostAddress);

                                    if (latency != null && latency < Long.MAX_VALUE) {
                                        if (graph.getEdge(nodeToAdd, anotherNode) == null &&
                                            graph.getEdge(anotherNode, nodeToAdd) == null) {
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
                                            edge.setLong("weight", latency);
                                            //									System.out.println("TopologyViewer.createGraph() distance entre " +
                                            //											paNode.getVMInformation().getHostName() + " et " +
                                            //											anotherPaNode.getVMInformation().getHostName() + " = " + edge.get("weight"));
                                        }
                                    }
                                }
                                visualization.setValue("graph.edges", null, VisualItem.INTERACTIVE,
                                        Boolean.FALSE);
                                visualization.run("unique");
                                visualization.run("dynamicColor");
                                //								visualization.run("layout");
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                });
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove a node from the graph
     * @param nodeURL URL of the node to remove
     */
    public void removeNode(final String nodeURL) {
        //		String nodeName = node.getNodeInformation().getURL();
        //		Iterator nodes = graph.nodes();
        //		while (nodes.hasNext()) {
        //			prefuse.data.Node anotherNode = (prefuse.data.Node) nodes.next();
        //			if (((Node)anotherNode.get("node")).getNodeInformation().getURL().equals(nodeName)) {
        //				graph.removeNode(anotherNode);
        //				visualization.run("latencyFilter");
        //				visualization.run("unique");
        //				visualization.run("dynamicColor");
        //				break;
        //			}
        //		}
    }

    /**
     * Loads data from resource manager model.
     * @param topology
     */
    public void loadMatrix(final Topology topology) {
        if (RMStore.isConnected() && topology.getHosts().size() > 1) {
            lock.lock();
            try {
                if (visualization == null) {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            //												graph = createGraph("/auto/sop-nas2a/u/sop-nas2a/vol/home_oasis/mvaldene/workspace/graph8.txt");
                            graph = createGraph(topology);
                            if (graph == null) {
                                return;
                            }

                            visualization = new Visualization();
                            visualization.add(graphName, graph);

                            // draw the "name" label for NodeItems
                            LabelRenderer nodeRenderer = new LabelRenderer("name");
                            nodeRenderer.setRoundedCorner(8, 8); // round the corners

                            DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
                            //						   rf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
                            //						rf.add("INGROUP('graph.edges')", new LabelRenderer("weight"));

                            // create a new default renderer factory
                            visualization.setRendererFactory(rf);

                            final ColorAction colorFillNodes = new ColorAction("graph.nodes",
                                VisualItem.FILLCOLOR, ColorLib.rgb(255, 255, 255));
                            colorFillNodes.add(VisualItem.HOVER, ColorLib.rgb(255, 100, 100));
                            colorFillNodes.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));

                            final int[] defaultPalette = new int[226];
                            final int[] fixedPalette = new int[226];
                            final int[] highlightPalette = new int[226];
                            for (int i = 0; i < 226; i++) {
                                fixedPalette[i] = ColorLib.rgba(17, 48, 250, i + 30);
                                highlightPalette[i] = ColorLib.rgba(255, 119, 22, i + 30);
                                defaultPalette[i] = ColorLib.rgba(50, 50, 50, i + 15);
                            }
                            final ColorAction aFill = new DataColorAction("graph.edges", "weight",
                                Constants.NUMERICAL, VisualItem.STROKECOLOR, defaultPalette);
                            aFill.add(VisualItem.HIGHLIGHT, new DataColorAction("graph.edges", "weight",
                                Constants.NUMERICAL, VisualItem.STROKECOLOR, highlightPalette));
                            aFill.add(VisualItem.FIXED, new DataColorAction("graph.edges", "weight",
                                Constants.NUMERICAL, VisualItem.STROKECOLOR, fixedPalette));

                            final ColorAction colorEdges = new ColorAction("graph.edges",
                                VisualItem.STROKECOLOR, ColorLib.rgba(100, 100, 100, 120));
                            colorEdges.add(new ColorStrokeEdgePredicate(graphName, visualization), ColorLib
                                    .rgb(255, 50, 50));
                            colorEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 119, 22));
                            colorEdges.add(VisualItem.FIXED, ColorLib.rgba(17, 48, 250, 240));

                            final ColorAction colorTextEdges = new ColorAction("graph.edges",
                                VisualItem.TEXTCOLOR, ColorLib.alpha(0));
                            colorTextEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 119, 22));
                            colorTextEdges.add(VisualItem.FIXED, ColorLib.rgba(17, 48, 250, 240));

                            // Latency filter
                            final GraphLatencyFilter latencyFilter = new GraphLatencyFilter(graphName,
                                maxLatency, false);

                            // create an action list containing all unique assignments
                            final ActionList staticActions = new ActionList();
                            //staticActions.add(new DataSizeAction("graph.edges", "weight"));
                            staticActions.add(new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib
                                    .gray(0)));
                            staticActions.add(new ColorAction("graph.nodes", VisualItem.STROKECOLOR, ColorLib
                                    .rgb(0, 0, 200)));

                            // action list containing all dynamic colors
                            final ActionList dynamicColor = new ActionList();
                            dynamicColor.add(colorFillNodes);
                            //						dynamicColor.add(colorEdges);
                            dynamicColor.add(aFill);
                            //						dynamicColor.add(colorTextEdges);
                            dynamicColor.add(new RepaintAction());

                            // create an action list with an animated layout
                            // the INFINITY parameter tells the action list to run indefinitely
                            ForceSimulator m_fsim = new ForceSimulator();
                            m_fsim.addForce(new NBodyForce());//2.0f, NBodyForce.DEFAULT_DISTANCE, NBodyForce.DEFAULT_THETA));
                            m_fsim.addForce(new LatencyForce());
                            m_fsim.addForce(new DragForce());
                            final ActionList layout = new ActionList(Activity.INFINITY);
                            topologyForceDirectedLayout = new TopologyForceDirectedLayout(graphName, m_fsim,
                                false);
                            layout.add(topologyForceDirectedLayout);
                            layout.add(new RepaintAction());

                            visualization.putAction("unique", staticActions);
                            visualization.putAction("latencyFilter", latencyFilter);
                            visualization.putAction("layout", layout);
                            visualization.putAction("dynamicColor", dynamicColor);

                            // create a new Display that pull from our Visualization
                            final prefuse.Display display = new prefuse.Display(visualization);
                            display.setPreferredSize(new Dimension(parent.getSize().x - 310,
                                parent.getSize().y));
                            display.addControlListener(new FocusControl(1));
                            display.addControlListener(new DragControl()); // drag items around
                            display.addControlListener(new PanControl()); // pan with background left-drag
                            display.addControlListener(new ZoomControl()); // zoom with vertical right-drag
                            display.addControlListener(new WheelZoomControl()); // mouse zoom
                            display.addControlListener(new NeighborHighlightControl("dynamicColor")); // sets the highlighted status for edges of the node under the mouse pointer

                            // Option panel
                            final JForcePanel fpanel = new JForcePanel(m_fsim);
                            //							final JPanel fpanel = new JPanel();
                            //							fpanel.setBackground(Color.WHITE);
                            fpanel.setPreferredSize(new Dimension(300, parent.getSize().y));
                            fpanel.setMaximumSize(new Dimension(300, parent.getSize().y));

                            // max latency slider, set the latency threshold
                            latencySlider = new JValueSlider("Latency threshold", 0,
                                maxLatency > 0 ? maxLatency : 1, maxLatency);
                            latencySlider
                                    .setToolTipText("Set the latency threshold of neighbours of selected nodes to display");
                            latencySlider.setBackground(Color.WHITE);
                            latencySlider.setPreferredSize(new Dimension(300, 20));
                            latencySlider.setMaximumSize(new Dimension(300, 20));
                            latencySlider.addChangeListener(new ChangeListener() {
                                public void stateChanged(ChangeEvent e) {
                                    latencyFilter.setLatencyThreshold(latencySlider.getValue().longValue());
                                    latencyFilter.run();
                                    visualization.run("dynamicColor");
                                }
                            });

                            // checkbox, set the latency filter exclusive mode
                            final JCheckBox exclusive = new JCheckBox("");
                            exclusive
                                    .setToolTipText("If exclusive show only neighbours of selected nodes whose latency with all selected nodes is under the threshold");
                            exclusive.setBackground(Color.WHITE);
                            exclusive.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    latencyFilter.setExclusive(exclusive.isSelected());
                                    latencyFilter.run();
                                    visualization.run("dynamicColor");
                                }
                            });

                            // box of 'exclusive' checkbox + label
                            Box exclusiveBox = new Box(BoxLayout.X_AXIS);
                            exclusiveBox.setPreferredSize(new Dimension(300, 20));
                            exclusiveBox.setMaximumSize(new Dimension(300, 20));
                            exclusiveBox.add(Box.createVerticalGlue());
                            JLabel lab = new JLabel("Exclusive");
                            lab
                                    .setToolTipText("If exclusive show only neighbours of selected nodes whose latency with all selected nodes is under the threshold");
                            exclusiveBox.add(lab);
                            exclusiveBox.add(Box.createRigidArea(new Dimension(46, 20)));
                            exclusiveBox.add(exclusive);
                            //cf2.setBorder(BorderFactory.createLoweredBevelBorder());

                            Box latencyFilterBox = new Box(BoxLayout.Y_AXIS);
                            latencyFilterBox.setPreferredSize(new Dimension(300, 44));
                            latencyFilterBox.setMaximumSize(new Dimension(300, 44));
                            latencyFilterBox.add(latencySlider);
                            latencyFilterBox.add(exclusiveBox);

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
                            displayEdgesBox.setPreferredSize(new Dimension(300, 28));
                            displayEdgesBox.setMaximumSize(new Dimension(300, 28));
                            displayEdgesBox.add(Box.createVerticalGlue());
                            JLabel lab2 = new JLabel("Edges");
                            lab2.setToolTipText("Show/hide edges of unselected nodes");
                            displayEdgesBox.add(lab2);
                            displayEdgesBox.add(Box.createRigidArea(new Dimension(67, 28)));
                            displayEdgesBox.add(displayEdges);

                            // checkBox to run/stop the animation of the force layout
                            final JCheckBox runLayout = new JCheckBox("");
                            runLayout.setToolTipText("Run/Stop the animation");
                            runLayout.setBackground(Color.WHITE);
                            runLayout.setSelected(true);
                            runLayout.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    if (runLayout.isSelected()) {
                                        visualization.run("layout");
                                    } else {
                                        visualization.cancel("layout");
                                    }
                                }
                            });

                            // Box of the 'runLayout' checkBox
                            Box runLayoutBox = new Box(BoxLayout.X_AXIS);
                            runLayoutBox.setPreferredSize(new Dimension(300, 28));
                            runLayoutBox.setMaximumSize(new Dimension(300, 28));
                            runLayoutBox.add(Box.createVerticalGlue());
                            JLabel lab3 = new JLabel("Animation");
                            lab3.setToolTipText("Run/Stop the animation");
                            runLayoutBox.add(lab3);
                            runLayoutBox.add(Box.createRigidArea(new Dimension(39, 28)));
                            runLayoutBox.add(runLayout);

                            // box of visibility filter
                            Box visibilityFilterBox = new Box(BoxLayout.Y_AXIS);
                            visibilityFilterBox.add(latencyFilterBox);
                            visibilityFilterBox.add(displayEdgesBox);
                            visibilityFilterBox.add(runLayoutBox);
                            visibilityFilterBox.setBorder(BorderFactory
                                    .createTitledBorder("Visibility filter"));
                            fpanel.add(visibilityFilterBox);

                            final JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
                            mainPanel.add(display);
                            mainPanel.add(fpanel);

                            // fix selected focus nodes

                            TupleSet focusGroup = visualization.getGroup(Visualization.FOCUS_ITEMS);
                            focusGroup.addTupleSetListener(new TupleSetListener() {
                                public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
                                    for (int i = 0; i < rem.length; ++i) {
                                        if (rem[i] instanceof NodeItem) {
                                            NodeItem node = (NodeItem) (rem[i]);
                                            node.setFixed(false);
                                            Iterator edgeIt = node.edges();
                                            while (edgeIt.hasNext()) {
                                                EdgeItem edge = (EdgeItem) (edgeIt.next());
                                                edge.setFixed(false);
                                                //edge.setHighlighted(false);
                                            }
                                        }
                                    }

                                    for (int i = 0; i < add.length; ++i) {
                                        if (add[i] instanceof NodeItem) {
                                            NodeItem node = (NodeItem) (add[i]);
                                            node.setFixed(false);
                                            node.setFixed(true);
                                            Iterator edgeIt = node.edges();
                                            while (edgeIt.hasNext()) {
                                                EdgeItem edge = (EdgeItem) (edgeIt.next());
                                                edge.setFixed(false);
                                                edge.setFixed(true);
                                            }
                                        }
                                    }

                                    visualization.run("latencyFilter");
                                    visualization.run("dynamicColor");
                                }
                            });

                            if (frame == null) {
                                Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
                                frame = SWT_AWT.new_Frame(swtAwtComponent);
                            }

                            frame.addWindowListener(new WindowAdapter() {
                                public void windowActivated(WindowEvent e) {
                                    if (visualization != null && runLayout.isSelected()) {
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
                            frame.addComponentListener(new ComponentListener() {
                                public void componentShown(ComponentEvent e) {
                                }

                                public void componentResized(ComponentEvent e) {
                                    if (visualization != null) {
                                        Dimension d = new Dimension(e.getComponent().getSize().width - 315, e
                                                .getComponent().getSize().height);
                                        display.setPreferredSize(d);
                                        fpanel.setPreferredSize(new Dimension(300,
                                            e.getComponent().getSize().height));
                                        mainPanel.doLayout();
                                    }
                                }

                                public void componentMoved(ComponentEvent e) {
                                }

                                public void componentHidden(ComponentEvent e) {
                                }
                            });

                            frame.add(mainPanel);
                            frame.pack();

                            parent.layout();

                            visualization
                                    .setValue("graph.edges", null, VisualItem.INTERACTIVE, Boolean.FALSE); // Cannot select edges
                            visualization.run("unique");
                            visualization.run("dynamicColor");
                            visualization.run("layout"); // start up the animated layout
                        }
                    });
                } else {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            graph = createGraph(topology);
                            if (graph == null) {
                                return;
                            }
                            visualization.add(graphName, graph);
                        }
                    });
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Creates a graph from RM's topology
     * @param topology
     * @return
     */
    private Graph createGraph(Topology topology) {
        if (topology != null) {
            Graph graph = new Graph();
            graph.getNodeTable().addColumns(
                    new Schema(new String[] { "name", "address", "nodes" }, new Class[] { String.class,
                            InetAddress.class, List.class }));
            graph.getEdgeTable().addColumns(
                    new Schema(new String[] { "weight" }, new Class[] { double.class }));

            int nbNodes = topology.getHosts().size();
            prefuse.data.Node[] nodes = new prefuse.data.Node[nbNodes];
            int num = 0;
            for (InetAddress adress : topology.getHosts()) {
                System.out.println("TopologyViewer.createGraph() add host = " + adress.getHostName());
                nodes[num] = graph.addNode();
                nodes[num].set("name", adress.getHostName());
                nodes[num++].set("address", adress);
            }

            maxLatency = 0; // Biggest latency of the graph
            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    if (i == j)
                        continue;

                    InetAddress hostAdress = (InetAddress) nodes[i].get("address");
                    InetAddress anotherHostAdress = (InetAddress) nodes[j].get("address");
                    Long latency = topology.getDistance(hostAdress, anotherHostAdress);

                    if (latency != null && latency < Long.MAX_VALUE) {
                        if (graph.getEdge(nodes[i], nodes[j]) == null &&
                            graph.getEdge(nodes[j], nodes[i]) == null) {
                            if (latency > maxLatency) {
                                maxLatency = latency;
                            }

                            Edge edge = graph.addEdge(nodes[i], nodes[j]);
                            edge.setLong("weight", latency);
                            //							System.out.println("TopologyViewer.createGraph() distance entre " +
                            //									paNode.getVMInformation().getHostName() + " et " +
                            //									anotherPaNode.getVMInformation().getHostName() + " = " + edge.get("weight"));
                        }
                    }
                }
            }
            return graph;
        }
        return null;
        //return GraphLib.getGrid(15, 15);
    }

    public void disconnect() {
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

    /**
     * Adds new element to the matrix. If the element is inserted into the
     * middle of the matrix reloads everything (otherwise it's too complex to
     * reloads only affected elements).
     */
    public void addView(final TreeLeafElement element) {
        if (visualization == null) {
            loadMatrix();
        }
    }

    /**
     * Removes element from the matrix. Disposes all required elements and
     * recalculates all positions in the tree.
     */
    public void removeView(final TreeLeafElement element) {
    }

    /**
     * Reloads state of the element. Finds all nodes of this element and updates
     * their states.
     */
    public void updateView(final TreeLeafElement element) {
    }

    /**
     * Clears view. Removes all elements and resets internal states.
     */
    public void clear() {
    }

    /**
     * Dummy implementation of ISelectionProvider
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    /**
     * Return a Graph for test purpose
     * The graph is extracted from a parsed file
     * the file should contain the number of nodes on the first line
     * and then the matrice.
     */
    private Graph createGraph(String filename) {
        Graph graph = new Graph();
        graph.getNodeTable().addColumns(new Schema(new String[] { "name" }, new Class[] { String.class }));
        graph.getEdgeTable().addColumns(new Schema(new String[] { "weight" }, new Class[] { double.class }));

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

            int nbNodes = new Integer(br.readLine());
            System.out.println("nbNodes = " + nbNodes);

            prefuse.data.Node[] nodes = new prefuse.data.Node[nbNodes];
            for (int i = 0; i < nbNodes; i++) {
                nodes[i] = graph.addNode();
                nodes[i].set("name", "node" + i);
            }

            maxLatency = 0;
            for (int i = 0; i < nbNodes; i++) {
                String line = br.readLine();
                String[] values = line.split("\\s+");
                for (int j = 0; j < nbNodes; j++) {
                    if (i != j) {
                        int distance = new Integer(values[j]);
                        System.out.print(distance + " ");
                        if (distance > maxLatency) {
                            maxLatency = distance;
                        }
                        if (graph.getEdge(nodes[i], nodes[j]) == null &&
                            graph.getEdge(nodes[j], nodes[i]) == null) {
                            Edge edge = graph.addEdge(nodes[i], nodes[j]);
                            edge.setDouble("weight", distance);
                        }
                    } else
                        System.out.print("-1 ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }
}
