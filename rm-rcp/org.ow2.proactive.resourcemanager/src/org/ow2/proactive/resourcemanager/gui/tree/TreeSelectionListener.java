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
package org.ow2.proactive.resourcemanager.gui.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Selectable;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;
import org.ow2.proactive.resourcemanager.gui.handlers.DescribeCommandHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.LockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.UnlockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;
import org.ow2.proactive.resourcemanager.gui.views.NodeInfoView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;


public class TreeSelectionListener implements ISelectionChangedListener {

    public void selectionChanged(SelectionChangedEvent event) {
        ArrayList<Selectable> selectionList = new ArrayList<Selectable>();
        List<Selectable> tlf = new ArrayList<Selectable>();

        if (event != null && event.getSelectionProvider() != null) {
            Object selection = event.getSelectionProvider().getSelection();

            if (selection != null) {
                for (Object leaf : ((IStructuredSelection) selection).toList()) {
                    getSubTreeNodesList((TreeLeafElement) leaf, selectionList);
                    tlf.add((Selectable) leaf);
                }

            }
        }
        //normally RM is connected if I can select something...
        if (RMStore.isConnected()) {
            RemoveNodesHandler.getInstance().setSelectedNodes(selectionList);
            LockNodesHandler.getInstance().setSelectedNodes(selectionList);
            UnlockNodesHandler.getInstance().setSelectedNodes(selectionList);
            DescribeCommandHandler.getInstance().setSelectedNodes(selectionList);
        }

        if (selectionList.size() > 0) {
            Node n = (Node) selectionList.get(0);
            NodeInfoView.setNode(n);
            if (ResourcesTabView.getTabViewer() != null)
                ResourcesTabView.getTabViewer().select(n);
        }

        if (ResourcesCompactView.getCompactViewer() != null) {
            ResourcesCompactView.getCompactViewer().getSelectionManager().select(tlf);
        }
    }

    private void getSubTreeNodesList(TreeLeafElement leaf, ArrayList<Selectable> selectList) {
        // Find the source of the selected item for the removing source and add node combo
        RMStore.getInstance().getModel().findSelectedSource(leaf);

        if (leaf.getType().equals(TreeElementType.NODE) ||
            leaf.getType().equals(TreeElementType.PENDING_NODE)) {
            if (!selectList.contains(leaf.getName()))
                selectList.add((Selectable) leaf);
        } else if (leaf instanceof TreeParentElement) {
            if (((TreeParentElement) leaf).hasChildren()) {
                for (TreeLeafElement element : ((TreeParentElement) leaf).getChildren()) {
                    getSubTreeNodesList(element, selectList);
                }
            }
        }
    }
}
