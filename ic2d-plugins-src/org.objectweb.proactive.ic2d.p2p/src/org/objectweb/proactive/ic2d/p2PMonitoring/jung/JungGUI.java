package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.p2p.v2.monitoring.Dumper;
import org.objectweb.proactive.p2p.v2.monitoring.Link;
import org.objectweb.proactive.p2p.v2.monitoring.P2PNode;
import org.objectweb.proactive.p2p.v2.monitoring.event.P2PNetworkListener;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStringer;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.decorators.ToolTipFunction;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.DefaultSettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.LayoutMutable;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;


public class JungGUI implements ToolTipFunction, P2PNetworkListener {
    //the time in ms we wait after a vertex has been added
    private final int UPDATE_PAUSE = 100;
    protected Graph graph;
    protected VisualizationViewer vv;
    protected PluggableRenderer pr;
    protected StringLabeller sl;
    protected ConstantEdgeStringer edgesLabeller;
    protected DefaultSettableVertexLocationFunction vertexLocations;

    //protected Integer key = new Integer(1);
    protected Layout layout;
    protected boolean mutable;
    protected GraphZoomScrollPane panel;

    //the eclipse display 
    protected Display display;

    public JungGUI(Display d) {
        this.display = d;

        graph = new UndirectedSparseGraph(); //this.createGraph();
        vertexLocations = new DefaultSettableVertexLocationFunction();
        //layout = useNonMutableLayout(graph);
        //layout = useMutableLayout(graph);
        layout = useLayout(graph, 3);

        //Layout layout = new ISOMLayout(graph);
        sl = StringLabeller.getLabeller(graph);
        edgesLabeller = new ConstantEdgeStringer(null);

        pr = new PluggableRenderer();
        //             pr = new CirclePluggableRenderer((CircleLayout) layout);
        pr.setVertexStringer(sl);
        pr.setVertexPaintFunction(new NOAVertexPaintFunction());

        // Creer un GraphLabelRenderer qui retourne notre RotateLabel

        //pr.setGraphLabelRenderer()
        // pr.setVertexLabelCentering(true);
        vv = new VisualizationViewer(layout, pr, new Dimension(1024, 768));
        vv.setPickSupport(new ShapePickSupport());
        pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
        vv.setBackground(Color.white);
        vv.setToolTipListener(this);
        vv.setPickSupport(new ShapePickSupport());
        EditingModalGraphMouse gm = new EditingModalGraphMouse();
        gm.setVertexLocations(vertexLocations);
        gm.setMode(Mode.PICKING);
        gm.add(new PeerPopupMenuPlugin(display, vertexLocations));
        vv.setGraphMouse(gm);
        panel = new GraphZoomScrollPane(vv);
    }

    public void changeLayout(int i) {
        Layout l = useLayout(graph, i);
        vv.stop();
        vv.setGraphLayout(l);
        vv.restart();
    }

    /**
     * Indicates which layout to use
     * 0 : circle layout
     * 1 : KK Layout
     * 2 : FR Layout
     * 3 : Spring Layout
     * @param g
     * @param i the number of the layout to use
     * @return
     */
    public Layout useLayout(Graph g, int i) {
        switch (i) {
        case 0: {
            this.mutable = false;
            return this.useCircleLayout(g);
        }
        case 1: {
            this.mutable = false;
            return this.useKKLayout(g);
        }
        case 2: {
            this.mutable = true;
            return this.useFRLayout(g);
        }
        case 3: {
            this.mutable = true;
            return this.useSpringLayout(g);
        }

        //return this.useFadingVertexLayout(g);
        //         return this.useTreeLayout(g);
        default:
            return null;
        }
    }

    //    public Layout useMutableLayout(Graph g) {
    //    	this.mutable = true;
    //    	return this.useSpringLayout(g);
    //    //	return  this.useFRLayout(g);
    //    }
    //    
    public LayoutMutable useFRLayout(Graph g) {
        return new FRLayout(g);
    }

    /**
     * Mutable layout
     * @param g
     * @return
     */
    protected LayoutMutable useSpringLayout(Graph g) {
        SpringLayout l = new SpringLayout(graph);
        l.setRepulsionRange(500);
        l.setStretch(0.5);
        return l;
    }

    /**
     * Non mutable layout
     * @param g
     * @return
     */
    protected Layout useKKLayout(Graph g) {
        KKLayout kk = new KKLayout(g);
        kk.setLengthFactor(1.3);
        return kk;
    }

    protected Layout useCircleLayout(Graph g) {
        return new CircleLayout(g);
    }

    protected void generateGraphNodes(Dumper dump) {
        Set<Map.Entry<String, P2PNode>> map = (Set<Map.Entry<String, P2PNode>>) dump.getP2PNetwork()
                                                                                    .getSenders()
                                                                                    .entrySet();
        Iterator it = map.iterator();
        while (it.hasNext()) {
            Map.Entry<String, P2PNode> entry = (Map.Entry<String, P2PNode>) it.next();

            // the node might have a -1 index because has never sent anything
            P2PNode node = ((P2PNode) entry.getValue());
            this.addVertex(node);
        }
        layout.restart();
    }

    protected void generateGraphLinks(Dumper dump) {
        //now dump the links
        //   int i = 0;
        Set<Map.Entry<String, Link>> map2 = (Set<Map.Entry<String, Link>>) dump.getP2PNetwork()
                                                                               .getLinks()
                                                                               .entrySet();

        Iterator it = map2.iterator();
        while (it.hasNext()) {
            Link entry = ((Map.Entry<String, Link>) it.next()).getValue();

            //  System.out.println("---- looking for sender " + entry.getSource());
            String source = entry.getSource();
            String dest = entry.getDestination();
            //this.addVertex(source);
            //this.addVertex(dest);
            this.addEdge(source, dest);
            //            vv.repaint();
            //System.out.println("JungGUI.generateGraph()");
            this.updateView();
        }
    }

    protected void updateView() {
        //vv.suspend();
        if (mutable) {
            ((LayoutMutable) layout).update();
            if (!vv.isVisRunnerRunning()) {
                vv.init();
            }
        } else {
            //vv.setGraphLayout(this.useNonMutableLayout(graph));
            // this.layout.restart();
        }
        try {
            Thread.sleep(UPDATE_PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //    	  // make your changes to the graph here
        //    	 	graph.addVertex(new SparseVertex());
        //    	 
        //vv.unsuspend();
        vv.repaint();
    }

    protected void addVertex(P2PNode p) {
        String s = p.getName();
        P2PUndirectedSparseVertex v = (P2PUndirectedSparseVertex) sl.getVertex(s);
        if (v == null) {
            // we haven't seen this peer
            //    System.out.println(" *** Adding peer  --" + s + "--" + p.getNoa() + " " + p.getMaxNOA());
            v = (P2PUndirectedSparseVertex) graph.addVertex(new P2PUndirectedSparseVertex());
            try {
                sl.setLabel(v, s);
            } catch (UniqueLabelException e) {
                e.printStackTrace();
            }
            v.setName(s);
        }
        // in all cases we set its values
        // because they might not be correct
        v.setMaxNoa(p.getMaxNOA());
        v.setNoa(p.getNoa());
    }

    protected void addEdge(String source, String dest) {
        Vertex current = sl.getVertex(source);
        Vertex v = sl.getVertex(dest);
        System.out.println("JungGUI.addEdge() from " + current + " to " + v);
        graph.addEdge(new UndirectedSparseEdge(current, v));
    }

    /**
     * Remove the current P2P Network
     *
     */
    public void clear() {
        vv.stop();
        graph.removeAllEdges();
        graph.removeAllVertices();
        sl.clear();
        vv.restart();
    }

    public JPanel getPanel() {
        return this.panel;
    }

    public void setRepulsionRange(int i) {
        System.out.println("JungGUI.setRepulsionRange() " + i);
        ((SpringLayout) this.layout).setRepulsionRange(i);
    }

    public String getToolTipText(Vertex v) {
        //System.out.println("JungGUI.getToolTipText() " + v);
        return "<html> " + sl.getLabel(v) + " <br> noa = " +
        ((P2PUndirectedSparseVertex) v).getNoa() + "</html>";
        //return null;
    }

    public String getToolTipText(Edge e) {
        // TODO Raccord de m�thode auto-g�n�r�
        return null;
    }

    public String getToolTipText(MouseEvent event) {
        // TODO Raccord de m�thode auto-g�n�r�
        return null;
    }

    public void newPeer(P2PNode node) {
        this.addVertex(node);
        //  layout.restart();
        this.updateView();
    }

    public void newLink(Link link) {
        String source = link.getSource();
        String dest = link.getDestination();
        //this.addVertex(source);
        //this.addVertex(dest);
        this.addEdge(source, dest);
        //            vv.repaint();
        //System.out.println("JungGUI.generateGraph()");
        this.updateView();
    }

    //    public static void main(String[] args) {
    //        JFrame f = new JFrame();
    //        JungGUI gui = new JungGUI();
    //
    //        f.add(gui.getPanel());
    //        f.pack();
    //        f.setVisible(true);
    //        Dumper dump = new Dumper();
    //        dump.getP2PNetwork().addListener(gui);
    //        //dump.createGraphFromFile2(args[0]);
    //        try {
    //            Dumper aDump = (Dumper) ProActive.turnActive(dump);
    //            Dumper.requestAcquaintances(args[0], aDump);
    //        } catch (ActiveObjectCreationException e) {
    //            e.printStackTrace();
    //        } catch (NodeException e) {
    //            e.printStackTrace();
    //        }
    //    }
}
