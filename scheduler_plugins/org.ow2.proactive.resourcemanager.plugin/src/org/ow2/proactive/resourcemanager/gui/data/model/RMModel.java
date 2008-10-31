package org.ow2.proactive.resourcemanager.gui.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodeSourceHandler;
import org.ow2.proactive.resourcemanager.gui.table.TableLabelProvider;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


public class RMModel {

    private Root root = null;

    /**
     * Use to (un)active view updates
     * Just used at Model initialization
     * Fill the model then refresh the views  
     */
    private boolean updateViews = false;

    //nodes states aggregates
    private int freeNodesNumber;
    private int busyNodesNumber;
    private int downNodesNumber;

    public RMModel() {
        this.root = new Root();
        freeNodesNumber = 0;
        busyNodesNumber = 0;
        downNodesNumber = 0;
    }

    public TreeParentElement getRoot() {
        return (TreeParentElement) root;
    }

    /****************************************************/
    /* Model update methods								*/
    /****************************************************/
    public void addNode(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;

        Node newNode;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());

            // the source cannot be null
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            if (host == null) { // if the host is null, then add it
                host = new Host(nodeEvent.getHostName());
                source.addChild(host);
                if (parentToRefresh == null)
                    parentToRefresh = source;
            }
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            if (vm == null) { // if the vm is null, then add it
                vm = new VirtualMachine(nodeEvent.getVMName());
                host.addChild(vm);
                if (parentToRefresh == null)
                    parentToRefresh = host;
            }

            newNode = new Node(nodeEvent.getNodeUrl(), nodeEvent.getState());
            vm.addChild(newNode);

            if (parentToRefresh == null)
                parentToRefresh = vm;
        }

        switch (nodeEvent.getState()) {
            case FREE:
                this.freeNodesNumber++;
                break;
            case DOWN:
                this.downNodesNumber++;
                break;
            case BUSY:
            case TO_BE_RELEASED:
                this.busyNodesNumber++;
        }
        this.actualizeTreeView(parentToRefresh);
        this.addTableItem(nodeEvent.getNodeSource(), nodeEvent.getHostName(), newNode.getState(), newNode
                .getName());
        this.actualizeStatsView();
    }

    public void removeNode(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());

            remove(vm, nodeEvent.getNodeUrl());
            parentToRefresh = vm;

            if (vm.getChildren().length == 0) {
                remove(host, nodeEvent.getVMName());
                parentToRefresh = host;

                if (host.getChildren().length == 0) {
                    remove(source, nodeEvent.getHostName());
                    parentToRefresh = source;
                }
            }
        }
        switch (nodeEvent.getState()) {
            case FREE:
                this.freeNodesNumber--;
                break;
            case DOWN:
                this.downNodesNumber--;
                break;
            case BUSY:
            case TO_BE_RELEASED:
                this.busyNodesNumber--;
        }

        this.actualizeTreeView(parentToRefresh);
        this.removeTableItem(nodeEvent.getNodeUrl());
        this.actualizeStatsView();
    }

    public void changeNodeState(RMNodeEvent nodeEvent) {
        Node node;
        NodeState previousState;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            node = (Node) find(vm, nodeEvent.getNodeUrl());
            previousState = node.getState();
            node.setState(nodeEvent.getState());
        }
        switch (previousState) {
            case FREE:
                this.freeNodesNumber--;
                break;
            case DOWN:
                this.downNodesNumber--;
                break;
            case BUSY:
            case TO_BE_RELEASED:
                this.busyNodesNumber--;
        }
        switch (nodeEvent.getState()) {
            case FREE:
                this.freeNodesNumber++;
                break;
            case DOWN:
                this.downNodesNumber++;
                break;
            case BUSY:
            case TO_BE_RELEASED:
                this.busyNodesNumber++;
        }

        this.actualizeTreeView(node);
        this.actualizeStatsView();
        this.updateTableItem(nodeEvent.getNodeSource(), nodeEvent.getHostName(), node.getState(), node
                .getName());

    }

    public void addNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeSourceEvent.getSourceName());
            if (source == null) {
                source = new Source(nodeSourceEvent.getSourceName(), nodeSourceEvent.getSourceType());
                root.addChild(source);
            }
        }
        actualizeTreeView(root);
        //refresh node source removal command state
        refreshNodeSourceRemovalHandler();

    }

    public void removeNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        synchronized (root) {
            for (TreeLeafElement n : root.getChildren()) {
                if (n.getName().equals(nodeSourceEvent.getSourceName())) {
                    root.removeChild(n);
                    break;
                }
            }
        }
        actualizeTreeView(root);
        //refresh node source removal command state
        refreshNodeSourceRemovalHandler();
    }

    /****************************************************/
    /* private methods									*/
    /****************************************************/

    private TreeLeafElement find(TreeParentElement parent, String name) {
        for (TreeLeafElement child : parent.getChildren())
            if (child.getName().equals(name))
                return child;
        return null;
    }

    private void remove(TreeParentElement parent, String name) {
        for (TreeLeafElement child : parent.getChildren())
            if (child.getName().equals(name)) {
                parent.removeChild(child);
                break;
            }
    }

    private void refreshNodeSourceRemovalHandler() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                RemoveNodeSourceHandler.getInstance().isEnabled();
            }
        });
    }

    /****************************************************/
    /* view update methods								*/
    /****************************************************/
    private void actualizeTreeView(TreeLeafElement element) {
        //actualize tree view if exists
        if (updateViews && ResourceExplorerView.getTreeViewer() != null) {
            ResourceExplorerView.getTreeViewer().actualize(element);
        }
    }

    private void actualizeStatsView() {
        //actualize stats view if exists
        if (updateViews && StatisticsView.getStatsViewer() != null) {
            StatisticsView.getStatsViewer().actualize();
        }
    }

    private void updateTableItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().updateItem(nodeSource, host, state, nodeUrl);
        }
    }

    private void removeTableItem(String nodeUrl) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().removeItem(nodeUrl);
        }
    }

    private void addTableItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().addItem(nodeSource, host, state, nodeUrl);
        }
    }

    /****************************************************/
    /* model queries methods							*/
    /****************************************************/

    public String[] getSourcesNames(boolean dynamicToo, boolean staticToo, boolean defaultToo) {
        TreeLeafElement[] children = root.getChildren();
        List<String> res = new ArrayList<String>();
        for (TreeLeafElement leaf : children) {
            Source src = (Source) leaf;
            if (dynamicToo && src.isDynamic()) {
                res.add(src.getName());
            } else if (staticToo && src.isStatic()) {
                if (src.isTheDefault()) {
                    if (defaultToo) {
                        res.add(src.getName());
                    }
                } else {
                    res.add(src.getName());
                }
            }
        }
        String[] tmp = new String[res.size()];
        res.toArray(tmp);
        Arrays.sort(tmp);
        return tmp;
    }

    public int getFreeNodesNumber() {
        return freeNodesNumber;
    }

    public int getBusyNodesNumber() {
        return busyNodesNumber;
    }

    public int getDownNodesNumber() {
        return downNodesNumber;
    }

    public void setUpdateViews(boolean updateViews) {
        this.updateViews = updateViews;
    }
}
