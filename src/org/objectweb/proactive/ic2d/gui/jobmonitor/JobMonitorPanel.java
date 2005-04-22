package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataModelTraversal;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeModel;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJob;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredObjectSet;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.Switcher;


public class JobMonitorPanel extends JPanel implements JobMonitorConstants {
    private final TreeView[] views;
    private static final int DEFAULT_RMI_PORT = Registry.REGISTRY_PORT;
    private static final String EXTRACT_MENU_LABEL = "Extract view to a new window";
    private JTabbedPane tabs;
    private Vector frames;
    private DataAssociation asso;
    private NodeExploration explorator;
    private DefaultListModel monitoredHosts;
    private DefaultListModel skippedObjects;
    private JPopupMenu popupmenu;
    private Thread refresher;
    private volatile boolean refresh = true;
    private int ttr = 60;

    public JobMonitorPanel(IC2DGUIController _controller) {
        asso = new DataAssociation();

        views = new TreeView[] {
                new TreeView("Job view / Virtual Nodes",
                    new int[] { JOB, VN, HOST, JVM, NODE, AO }, false),
                new TreeView("Job view / Hosts",
                    new int[] { JOB, HOST, JVM, VN, NODE, AO }, false),
                new TreeView("Host view",
                    new int[] { HOST, JOB, JVM, VN, NODE, AO }, false),
                new TreeView("Custom view",
                    new int[] { JOB, VN, HOST, JVM, NODE, AO }, true)
            };

        setLayout(new GridLayout(1, 1));

        createRefresher();

        monitoredHosts = new DefaultListModel();
        skippedObjects = new DefaultListModel();
        skippedObjects.addElement(new MonitoredJob(ProActive.getJobId()));

        tabs = new JTabbedPane();
        frames = new Vector();

        explorator = new NodeExploration(asso, skippedObjects, _controller);

        add(tabs);

        final JPopupMenu extractMenu = new JPopupMenu();
        JMenuItem extract = new JMenuItem(EXTRACT_MENU_LABEL);
        extract.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final Component tab = tabs.getSelectedComponent();
                    final String title = tabs.getTitleAt(tabs.getSelectedIndex());

                    final JFrame f = new JFrame(title);
                    f.setSize(tab.getSize());
                    Container c = f.getContentPane();
                    c.setLayout(new GridLayout(1, 1));
                    c.add(tab);

                    f.addWindowListener(new WindowAdapter() {
                            public void windowClosing(WindowEvent e) {
                                tabs.addTab(title, tab);
                                frames.remove(f);
                            }
                        });

                    frames.add(f);

                    f.setVisible(true);
                }
            });

        extractMenu.add(extract);

        for (int i = 0; i < views.length; i++)
            tabs.addTab(views[i].getLabel(), views[i].getPane());

        tabs.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        Component menu = extractMenu.getComponent(0);
                        if (tabs.getTabCount() > 1) {
                            if (!menu.isEnabled()) {
                                menu.setEnabled(true);
                            }
                        } else {
                            if (menu.isEnabled()) {
                                menu.setEnabled(false);
                            }
                        }

                        extractMenu.show(tabs, e.getX(), e.getY());
                    }
                }
            });
    }

    private void createRefresher() {
        refresh = true;
        refresher = new Thread(new Runnable() {
                    public void run() {
                        //				System.out.println ("Start of refresher thread");
                        while (refresh) {
                            try {
                                //						System.out.println ("Waiting for refresh - ttr = " + ttr + " seconds");
                                Thread.sleep(ttr * 1000);
                                //						System.out.println ("Automatic refresh starting");
                                handleHosts();
                            } catch (InterruptedException e) {
                                //						e.printStackTrace();
                            }
                        }

                        //				System.out.println ("Stop of refresher thread");
                    }
                });
    }

    protected void finalize() throws Throwable {
        stopRefreshing();
        super.finalize();
    }

    private void stopRefreshing() {
        if (refresh) {
            //			System.out.println ("Stoppping refresher thread");
            refresh = false;
            refresher.interrupt();
        }
    }

    public int getTtr() {
        return ttr;
    }

    public void setTtr(int _ttr) {
        ttr = _ttr;
        if (refresh) {
            refresher.interrupt();
        } else {
            createRefresher();
        }
    }

    public void addMonitoredHost(String host, int port) {
        MonitoredHost hostObject = new MonitoredHost(host, port,"rmi:");
        if (!monitoredHosts.contains(hostObject)) {
            monitoredHosts.addElement(hostObject);
            if (monitoredHosts.size() == 1) {
                refresher.start();
            }
        }

        //		System.out.println ("There are now " + monitoredHosts.size() + " monitored hosts");
    }

    public void updateHosts() {
        new Thread(new Runnable() {
                public void run() {
                    handleHosts();
                }
            }).start();
    }

    void hideOwnedFrames() {
        //		System.out.println("Hiding frames: " + frames.size());
        hideOrShow(true);
        stopRefreshing();
    }

    void showOwnedFrames() {
        //		System.out.println("Showing frames: " + frames.size());
        if (!monitoredHosts.isEmpty()) {
            createRefresher();
            refresher.start();
        }
        hideOrShow(false);
    }

    private void rebuildAll() {
        for (int i = 0; i < views.length; i++) {
            views[i].getModel().rebuild();
            views[i].doneRefreshing();
        }
    }

    private void handleHosts() {
        synchronized (monitoredHosts) {
            asso.clear();
            explorator.startExploration();

            for (int i = 0, size = monitoredHosts.size(); i < size; ++i) {
                MonitoredHost hostObject = (MonitoredHost) monitoredHosts.get(i);
                String host = hostObject.getFullName();
                String protocol = hostObject.getMonitorProtocol();

                //				System.out.println ("\nMonitoring host " + (i + 1) + " / " + size + ": " + host);
                handleHost(host, protocol);
            }

            explorator.exploreKnownJVM();
            explorator.endExploration();

            rebuildAll();
        }
    }

    public void updateHost(final BasicMonitoredObject hostObject) {
        new Thread(new Runnable() {
                public void run() {
                    asso.deleteItem(hostObject);
                    explorator.startExploration();
                    handleHost(hostObject.getFullName(),((MonitoredHost)hostObject).getMonitorProtocol());
                    explorator.endExploration();
                }
            }).start();
    }

    private void handleHost(String host, String protocol) {
        String hostname = host;
        int port = DEFAULT_RMI_PORT;
        int pos = host.lastIndexOf(":");
        if (pos != -1) {
            // if the hostname is host:port
            try {
                port = Integer.parseInt(host.substring(1 + pos));
            } catch (NumberFormatException e) {
                port = DEFAULT_RMI_PORT;
            }

            hostname = host.substring(0, pos);
        }

        explorator.exploreHost(hostname, port, protocol);
    }

    private void dump(Object o) {
        System.out.println("<object class='" + o.getClass() + "'>");
        System.out.println(o.toString());
        System.out.println("</object>");
        System.out.println();
    }

    private void hideOrShow(boolean hide) {
        for (int i = 0, size = frames.size(); i < size; ++i) {
            JFrame f = (JFrame) frames.get(i);
            f.setVisible(!hide);
        }
    }

    public NodeExploration getNodeExploration() {
        return explorator;
    }

    public DefaultListModel getMonitoredHosts() {
        return monitoredHosts;
    }

    public DefaultListModel getSkippedObjects() {
        return skippedObjects;
    }

    public void clearDeleted() {
        asso.clearDeleted();
        rebuildAll();
    }

    class TreeView {
        private static final int ROW_HEIGHT = 25;
        private String label;
        private JTree tree;
        private JSplitPane pane;
        private JPopupMenu popupmenu;
        private JobMonitorStatus status;

        public TreeView(String label, int[] keys, boolean allowExchange) {
            this.label = label;
            DataModelTraversal traversal = new DataModelTraversal(keys);
            DataTreeModel model = new DataTreeModel(asso, traversal);

            JPanel left = createContent(model, allowExchange);
            JScrollPane right = new JScrollPane(status);

            pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, right);
            pane.setOneTouchExpandable(true);
        }

        private void constructPopupMenu(TreePath[] selection) {
            popupmenu = new JPopupMenu();

            AbstractAction a = new AbstractAction("Refresh monitoring tree") {
                    public void actionPerformed(ActionEvent e) {
                        updateHosts();
                    }
                };

            JMenuItem menuItem = new JMenuItem(a);
            menuItem.setEnabled(monitoredHosts.size() > 0);
            popupmenu.add(menuItem);

            final MonitoredObjectSet objects = new MonitoredObjectSet();
            boolean hasHosts = false;
            int sameKey = NO_KEY;
            
            if (selection != null) {
                for (int i = 0; i < selection.length; i++) {
                    DataTreeNode node = (DataTreeNode) selection[i].getLastPathComponent();
                    if (!node.isRoot()) {
                        BasicMonitoredObject object = node.getObject();
                        objects.add(object);
                    	int key = object.getKey();

                        if (key == HOST) {
                            hasHosts = true;
                        }

                        if (i == 0)
                        	sameKey = key;
                        else if (key != sameKey)
                        	sameKey = NO_KEY;
                    }
                }
            }

            a = new AbstractAction("Refresh selected hosts") {
                        public void actionPerformed(ActionEvent e) {
                            Iterator iter = objects.iterator();
                            while (iter.hasNext()) {
                                BasicMonitoredObject object = (BasicMonitoredObject) iter.next();
                                if (object.getKey() == HOST) {
                                    updateHost(object);
                                }
                            }
                            rebuildAll();
                            tree.repaint();
                        }
                    };

            menuItem = new JMenuItem(a);
            menuItem.setEnabled(hasHosts);
            popupmenu.add(menuItem);

            a = new AbstractAction("Stop monitoring these objects") {
                        public void actionPerformed(ActionEvent e) {
                            Iterator iter = objects.iterator();
                            while (iter.hasNext()) {
                                BasicMonitoredObject object = (BasicMonitoredObject) iter.next();
                                skippedObjects.addElement(object);
                                asso.removeItem(object);
                            }
                            rebuildAll();
                            tree.repaint();
                        }
                    };

            menuItem = new JMenuItem(a);
            menuItem.setEnabled(!objects.isEmpty());
            popupmenu.add(menuItem);

            a = new AbstractAction("Kill selected objects") {
                        public void actionPerformed(ActionEvent e) {
                            explorator.killObjects(objects);
                        }
                    };

            menuItem = new JMenuItem(a);
            menuItem.setEnabled(sameKey == JOB || sameKey == JVM);
            popupmenu.add(menuItem);
        }

        private void addButtons(JPanel panel, final JTree tree) {
            JPanel buttons = new JPanel();
            buttons.setLayout(new FlowLayout());
            panel.add(buttons, BorderLayout.SOUTH);

            JButton expand = new JButton("Expand all");
            expand.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        for (int row = 0; row < tree.getRowCount(); row++)
                            tree.expandRow(row);
                    }
                });
            buttons.add(expand);

            JButton collapse = new JButton("Collapse all");
            collapse.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        for (int row = tree.getRowCount() - 1; row >= 0;
                                row--)
                            tree.collapseRow(row);
                    }
                });
            buttons.add(collapse);
        }

        private JPanel createContent(DataTreeModel model, boolean allowExpand) {
            JPanel left = new JPanel(new BorderLayout());

            tree = new JTree(model);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            tree.setCellRenderer(new JobMonitorTreeCellRenderer());
            tree.setLargeModel(true);
            tree.setRowHeight(ROW_HEIGHT);

            JScrollPane pane = new JScrollPane(tree);
            left.add(pane, BorderLayout.CENTER);

            tree.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        TreePath[] selection = tree.getSelectionPaths();
                        if (e.isPopupTrigger()) {
                            constructPopupMenu(selection);
                            popupmenu.show(tree, e.getX(), e.getY());
                        }
                    }
                });

            addButtons(left, tree);

            Switcher s = new Switcher(tree, allowExpand);
            JPanel switcher = new JPanel(new GridLayout(1, 1));
            switcher.setBackground(Color.WHITE);
            switcher.add(s);
            switcher.setBorder(BorderFactory.createEtchedBorder());
            left.add(switcher, BorderLayout.NORTH);

            status = new JobMonitorStatus(tree);
            return left;
        }

        public String getLabel() {
            return label;
        }

        public JSplitPane getPane() {
            return pane;
        }

        public DataTreeModel getModel() {
            return (DataTreeModel) tree.getModel();
        }

        public void doneRefreshing() {
            tree.expandRow(0);
        }
    }
}
