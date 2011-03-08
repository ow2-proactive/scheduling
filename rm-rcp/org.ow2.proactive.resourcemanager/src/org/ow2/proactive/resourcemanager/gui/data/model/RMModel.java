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
    private int deployingNodesNumber;
    private int lostNodesNumber;
    private int configuringNodesNumber;
    private int freeNodesNumber;
    private int busyNodesNumber;
    private int downNodesNumber;
    private int lockedNodesNumber;

    // For the removing source and add node combo
    private String sourceToRemoveName = "";

    public RMModel() {
        this.root = new Root();
        deployingNodesNumber = 0;
        lostNodesNumber = 0;
        configuringNodesNumber = 0;
        freeNodesNumber = 0;
        lockedNodesNumber = 0;
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
        synchronized (root) {
            switch (nodeEvent.getNodeState()) {
                //those node states are related to RMNodes
                case FREE:
                case LOCKED:
                case DOWN:
                case BUSY:
                case TO_BE_REMOVED:
                case CONFIGURING:
                    this.addNodeToModel(nodeEvent);
                    break;
                //those node states are related to RMDeployingNodes
                case LOST:
                case DEPLOYING:
                    this.addDeployingNodeToModel(nodeEvent);
                    break;
            }
        }
    }

    /**
     * Helper method that adds a new DeployingNode to the model
     * @param event the event that triggered the addition
     */
    private void addDeployingNodeToModel(RMNodeEvent event) {
        TreeParentElement source = null;
        TreeLeafElement node = null;
        source = (TreeParentElement) find(root, event.getNodeSource());
        if (source != null) {
            node = new DeployingNode(event);
            //a deploying node cannot be more accurately placed, its parent is the nodesource
            source.addFirstChild(node);
        }
        switch (event.getNodeState()) {
            case LOST:
                lostNodesNumber++;
                break;
            case DEPLOYING:
                deployingNodesNumber++;
                break;
        }

        this.actualizeStatsView();
        this.actualizeTreeView(source);
        this.addToCompactView(node);
    }

    /**
     * Helper method that adds a new RMNode to the model
     * @param nodeEvent the event that triggered the addition
     */
    private void addNodeToModel(RMNodeEvent nodeEvent) {
        Node newNode;
        TreeParentElement parentToRefresh = null;
        TreeLeafElement elementToAdd = null;

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
        // TODO check that here node can be only in configuring state
        switch (nodeEvent.getNodeState()) {
            case FREE:
                this.freeNodesNumber++;
                break;
            case DOWN:
                this.downNodesNumber++;
                break;
            case BUSY:
            case TO_BE_REMOVED:
                this.busyNodesNumber++;
                break;
            case LOCKED:
                this.lockedNodesNumber++;
                break;
            case CONFIGURING:
                this.configuringNodesNumber++;
        }
        //finally, we refresh the views
        this.actualizeTreeView(parentToRefresh);
        this.addToCompactView(elementToAdd);
        this.addTableItem(newNode);
        this.actualizeStatsView();
    }

    public void removeNode(RMNodeEvent nodeEvent) {
        synchronized (root) {
            switch (nodeEvent.getNodeState()) {
                //those node states are related to RMNodes
                case FREE:
                case DOWN:
                case BUSY:
                case TO_BE_REMOVED:
                case LOCKED:
                case CONFIGURING:
                    this.removeNodeFromModel(nodeEvent);
                    break;
                //those node states are related to RMPendingNodes
                case LOST:
                case DEPLOYING:
                    this.removeDeployingNodeFromModel(nodeEvent);
                    break;
            }
        }
    }

    /**
     * Removes the pending node associated to the event from the model
     * @param event the event that triggered the removal
     */
    private void removeDeployingNodeFromModel(RMNodeEvent event) {
        TreeParentElement source = null;
        TreeLeafElement toRemove = null;
        source = (TreeParentElement) find(root, event.getNodeSource());
        toRemove = (DeployingNode) find(source, event.getNodeUrl());
        if (toRemove != null) {
            remove(source, toRemove.getName());
        }
        switch (event.getNodeState()) {
            case LOST:
                lostNodesNumber--;
                break;
            case DEPLOYING:
                deployingNodesNumber--;
                break;
        }

        this.actualizeTreeView(source);
        this.actualizeStatsView();
        if (toRemove != null) {
            this.removeFromCompactView(toRemove);
        }

    }

    /**
     * Removes the node associated to the event from the model
     * @param nodeEvent the node that triggered the removal
     */
    private void removeNodeFromModel(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;
        TreeLeafElement elementToRemove = null;
        Node node;
        String hostname;

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

        switch (nodeEvent.getNodeState()) {
            case FREE:
                this.freeNodesNumber--;
                break;
            case DOWN:
                this.downNodesNumber--;
                break;
            case LOCKED:
                this.lockedNodesNumber--;
                break;
            case BUSY:
            case TO_BE_REMOVED:
                this.busyNodesNumber--;
                break;
            case CONFIGURING:
                this.configuringNodesNumber--;
                break;
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
            node.setDescription(nodeEvent.getNodeInfo());
            state = nodeEvent.getNodeState();
        }
        switch (previousState) {
            case CONFIGURING:
                this.configuringNodesNumber--;
                break;
            case FREE:
                this.freeNodesNumber--;
                break;
            case LOCKED:
                this.lockedNodesNumber--;
                break;
            case DOWN:
                this.downNodesNumber--;
                break;
            case BUSY:
            case TO_BE_REMOVED:
                this.busyNodesNumber--;
                break;
        }
        switch (state) {
            case CONFIGURING:
                this.configuringNodesNumber++;
                break;
            case FREE:
                this.freeNodesNumber++;
                break;
            case LOCKED:
                this.lockedNodesNumber++;
                break;
            case DOWN:
                this.downNodesNumber++;
                this.removeNodeFromTopologyView(node, hostname);
                break;
            case BUSY:
            case TO_BE_REMOVED:
                this.busyNodesNumber++;
                break;
        }

        this.actualyseTopologyView(node, previousState);
        this.actualizeTreeView(node);
        this.updateCompactView(node);
        this.actualizeStatsView();
        this.updateTableItem(node);
    }

    /**
     * A pending node update has been issued
     * @param event The associated event
     */
    public void updateDeployingNode(RMNodeEvent event) {
        DeployingNode node = null;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, event.getNodeSource());
            node = (DeployingNode) find(source, event.getNodeUrl());
        }
        //we update the state
        if (event.getNodeState() == NodeState.LOST && event.getPreviousNodeState() == NodeState.DEPLOYING) {
            node.setState(event);
            deployingNodesNumber--;
            lostNodesNumber++;
        }
        //we update the desc
        node.setDescription(event.getNodeInfo());

        this.actualizeTreeView(node);
        this.updateCompactView(node);
        this.actualizeStatsView();
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
            if (previousState == NodeState.CONFIGURING && state == NodeState.FREE) {
                // add new free node to the topology view
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
                case PENDING_NODE:
                    selectedSource = leaf.getParent().getName();
                    break;
            }
        } catch (Exception e) {
            //if exception : default empty string argument
        } finally {
            setSourceToRemoveSelected(selectedSource);
        }
    }

    // End of removing/add combo methods
    public int getPendingNodesNumber() {
        return deployingNodesNumber;
    }

    public int getConfiguringNodesNumber() {
        return configuringNodesNumber;
    }

    public int getLostNodesNumber() {
        return lostNodesNumber;
    }

    public int getFreeNodesNumber() {
        return freeNodesNumber;
    }

    public int getLockedNodesNumber() {
        return lockedNodesNumber;
    }

    public int getBusyNodesNumber() {
        return busyNodesNumber;
    }

    public int getDownNodesNumber() {
        return downNodesNumber;
    }

    public int getTotalNodesNumber() {
        return freeNodesNumber + busyNodesNumber + downNodesNumber + lockedNodesNumber;
    }

    public void setUpdateViews(boolean updateViews) {
        this.updateViews = updateViews;
    }

}
