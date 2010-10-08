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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodeSourceHandler;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTopologyView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


public class RMModel implements Serializable {

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

    // For the removing source and add node combo
    private String sourceToRemoveName = "";

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
        TreeLeafElement elementToAdd = null;

        Node newNode;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());

            // the source cannot be null
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            if (host == null) { // if the host is null, then add it
                host = new Host(nodeEvent.getHostName());
                source.addChild(host);
                if (parentToRefresh == null) {
                    parentToRefresh = source;
                    elementToAdd = host;
                }
            }
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            if (vm == null) { // if the vm is null, then add it
                vm = new VirtualMachine(nodeEvent.getVMName());
                host.addChild(vm);
                if (parentToRefresh == null) {
                    parentToRefresh = host;
                    elementToAdd = vm;
                }
            }

            newNode = new Node(nodeEvent);
            vm.addChild(newNode);

            if (parentToRefresh == null) {
                parentToRefresh = vm;
                elementToAdd = newNode;
            }
        }

        switch (nodeEvent.getNodeState()) {
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
        this.addToCompactView(elementToAdd);
        this.addTableItem(newNode);
        this.actualizeStatsView();
        this.addNodeToTopologyView(newNode);

    }

    public void removeNode(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;
        TreeLeafElement elementToRemove = null;
        Node node;
        String hostname;

        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            node = (Node) find(vm, nodeEvent.getNodeUrl());
            hostname = host.getName();

            elementToRemove = remove(vm, nodeEvent.getNodeUrl());
            parentToRefresh = vm;

            if (vm.getChildren().length == 0) {
                elementToRemove = remove(host, nodeEvent.getVMName());
                parentToRefresh = host;

                if (host.getChildren().length == 0) {
                    elementToRemove = remove(source, nodeEvent.getHostName());
                    parentToRefresh = source;
                }
            }
        }
        switch (nodeEvent.getNodeState()) {
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
        this.removeTableItem(node);
        this.removeFromCompactView(elementToRemove);
        this.actualizeStatsView();
        this.removeNodeFromTopologyView(node, hostname);
    }

    public void changeNodeState(RMNodeEvent nodeEvent) {
        Node node;
        String hostname;
        NodeState previousState, state;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            node = (Node) find(vm, nodeEvent.getNodeUrl());
            hostname = host.getName();
            previousState = node.getState();
            node.setState(nodeEvent);
            state = nodeEvent.getNodeState();
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
        switch (state) {
            case FREE:
                this.freeNodesNumber++;
                break;
            case DOWN:
                this.downNodesNumber++;
                this.removeNodeFromTopologyView(node, hostname);
                break;
            case BUSY:
            case TO_BE_RELEASED:
                this.busyNodesNumber++;
        }

        this.actualyseTopologyView(node, previousState);
        this.actualizeTreeView(node);
        this.updateCompactView(node);
        this.actualizeStatsView();
        this.updateTableItem(node);
    }

    public void addNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        TreeParentElement source = null;
        synchronized (root) {
            source = (TreeParentElement) find(root, nodeSourceEvent.getSourceName());
            if (source == null) {
                source = new Source(nodeSourceEvent);
                root.addChild(source);
            }
        }
        actualizeTreeView(root);
        addToCompactView(source);
        //refresh node source removal command state
        refreshNodeSourceRemovalHandler();

    }

    public void removeNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        TreeLeafElement source = null;
        synchronized (root) {
            for (TreeLeafElement n : root.getChildren()) {
                if (n.getName().equals(nodeSourceEvent.getSourceName())) {
                    source = n;
                    root.removeChild(n);
                    break;
                }
            }
        }
        actualizeTreeView(root);
        removeFromCompactView(source);
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

    private TreeLeafElement remove(TreeParentElement parent, String name) {
        for (TreeLeafElement child : parent.getChildren()) {
            if (child.getName().equals(name)) {
                parent.removeChild(child);
                return child;
            }
        }
        return null;
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
    private void addNodeToTopologyView(Node node) {
        //actualize tree view if exists
        if (updateViews && ResourcesTopologyView.getTopologyViewer() != null) {
            ResourcesTopologyView.getTopologyViewer().addNode(node);
        }
    }

    private void removeNodeFromTopologyView(Node node, String host) {
        //actualize tree view if exists
        if (updateViews && ResourcesTopologyView.getTopologyViewer() != null) {
            ResourcesTopologyView.getTopologyViewer().removeNode(node, host);
        }
    }

    private void actualyseTopologyView(Node node, NodeState previousState) {
        // actualize topology view if exists
        if (updateViews && ResourcesTopologyView.getTopologyViewer() != null) {
            NodeState state = node.getState();
            if ((previousState == NodeState.DOWN || previousState == NodeState.TO_BE_RELEASED) &&
                (state == NodeState.FREE || state == NodeState.BUSY)) {
                ResourcesTopologyView.getTopologyViewer().addNode(node);
            }
        }
    }

    private void actualizeTreeView(TreeLeafElement element) {
        //actualize tree view if exists
        if (updateViews && ResourceExplorerView.getTreeViewer() != null) {
            ResourceExplorerView.getTreeViewer().actualize(element);
        }
    }

    private void addToCompactView(TreeLeafElement element) {
        if (updateViews) {
            if (ResourcesCompactView.getCompactViewer() != null) {
                ResourcesCompactView.getCompactViewer().addView(element);
            }
        }
    }

    private void removeFromCompactView(TreeLeafElement element) {
        if (updateViews) {
            if (ResourcesCompactView.getCompactViewer() != null) {
                ResourcesCompactView.getCompactViewer().removeView(element);
            }
        }
    }

    private void updateCompactView(TreeLeafElement element) {
        if (updateViews) {
            if (ResourcesCompactView.getCompactViewer() != null) {
                ResourcesCompactView.getCompactViewer().updateView(element);
            }
        }
    }

    private void actualizeStatsView() {
        //actualize stats view if exists
        if (updateViews && StatisticsView.getStatsViewer() != null) {
            StatisticsView.getStatsViewer().actualize();
        }
    }

    private void updateTableItem(Node node) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().updateItem(node);
        }
    }

    private void removeTableItem(Node node) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().removeItem(node);
        }
    }

    private void addTableItem(Node node) {
        //actualize table view if exists
        if (updateViews && ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().addItem(node);
        }
    }

    /****************************************************/
    /* model queries methods							*/
    /****************************************************/

    public String[] getSourcesNames(boolean defaultToo) {
        TreeLeafElement[] children = root.getChildren();
        List<String> res = new ArrayList<String>();
        res.add(""); // The first blank line use in the combo if no source is selected before trying remove one

        for (TreeLeafElement leaf : children) {
            Source src = (Source) leaf;
            if (src.isTheDefault()) {
                if (defaultToo) {
                    res.add(src.getName());
                }
            } else {
                res.add(src.getName());
            }
        }
        String[] tmp = new String[res.size()];
        res.toArray(tmp);
        Arrays.sort(tmp);
        return tmp;
    }

    // Methods use for the source removing and the add node combo     
    public void setSourceToRemoveSelected(String sourceName) {
        this.sourceToRemoveName = sourceName;
    }

    public String getSourceToRemoveSelected() {
        return this.sourceToRemoveName;
    }

    public void findSelectedSource(TreeLeafElement leaf) {
        String selectedSource = "";
        try {
            switch (leaf.getType()) {
                case SOURCE:
                    selectedSource = leaf.getName();
                    break;
                case HOST:
                    selectedSource = leaf.getParent().getName();
                    break;
                case VIRTUAL_MACHINE:
                    selectedSource = leaf.getParent().getParent().getName();
                    break;
                case NODE:
                    selectedSource = leaf.getParent().getParent().getParent().getName();
                    break;
            }
        } catch (Exception e) {
            //if exception : default empty string argument
        } finally {
            setSourceToRemoveSelected(selectedSource);
        }
    }

    // End of removing/add combo methods

    public int getFreeNodesNumber() {
        return freeNodesNumber;
    }

    public int getBusyNodesNumber() {
        return busyNodesNumber;
    }

    public int getDownNodesNumber() {
        return downNodesNumber;
    }

    public int getTotalNodesNumber() {
        return freeNodesNumber + busyNodesNumber + downNodesNumber;
    }

    public void setUpdateViews(boolean updateViews) {
        this.updateViews = updateViews;
    }
}
