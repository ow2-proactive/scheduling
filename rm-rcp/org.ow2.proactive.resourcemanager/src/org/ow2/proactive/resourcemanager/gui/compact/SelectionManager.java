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
package org.ow2.proactive.resourcemanager.gui.compact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.resourcemanager.gui.compact.view.NodeView;
import org.ow2.proactive.resourcemanager.gui.compact.view.View;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Selectable;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.handlers.DescribeCommandHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.LockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.UnlockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;
import org.ow2.proactive.resourcemanager.gui.views.NodeInfoView;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTopologyView;


/**
 *
 * Class handles selection of elements in compact view.
 * Supports selection with shift and ctrl (segment and single selection).
 *
 */
public class SelectionManager {

    // selected elements
    private ArrayList<View> selected = new ArrayList<View>();
    // main viewer
    private CompactViewer matrix;

    /**
     * Constructs SelectionManager.
     */
    public SelectionManager(CompactViewer matrix) {
        this.matrix = matrix;
    }

    /**
     * Selects of deselect view based on its previous state.
     */
    public void invertSelection(View view) {
        if (!view.isSelected()) {
            select(view);
        } else {
            deselect(view);
        }
    }

    /**
     * selects a view given the actual node it contains
     */
    public void select(List<Selectable> tl) {
        select(tl, true);
    }

    public void select(List<Selectable> tl, boolean propagate) {
        this.deselectAll();
        for (Selectable sel : tl) {
            TreeLeafElement elt = null;
            if (TreeLeafElement.class.isAssignableFrom(sel.getClass())) {
                elt = (TreeLeafElement) sel;
            } else {
                continue;
            }
            for (View nv : ResourcesCompactView.getCompactViewer().getRootView().getAllViews()) {
                if (elt.equals(nv.getElement())) {
                    select(nv, false, propagate);
                    break;
                }
            }
        }
    }

    /**
     * Selects view and its children.
     */
    public void select(View view) {
        select(view, false, true);
    }

    private void select(View view, boolean rec, boolean propagate) {
        if (!selected.contains(view)) {
            view.setSelected(true);
            selected.add(view);
            for (View v : view.getChilds()) {
                select(v, true, propagate);
            }

            if (view.getElement() != null) {
                if (view.getElement() instanceof Node) {
                    Node n = (Node) view.getElement();
                    NodeInfoView.setNode(n);
                    if (propagate && ResourcesTabView.getTabViewer() != null) {
                        ResourcesTabView.getTabViewer().select(n);
                    }
                    if (propagate && !rec && ResourcesTopologyView.getTopologyViewer() != null) {
                        ResourcesTopologyView.getTopologyViewer().setSelection(Collections.singletonList(n));
                    }
                } else if (propagate && !rec && ResourcesTopologyView.getTopologyViewer() != null) {
                    List<NodeView> all = view.getAllNodeViews();
                    if (all.size() > 0) {
                        Node n = (Node) all.get(0).getElement();
                        ResourcesTopologyView.getTopologyViewer().setSelection(Collections.singletonList(n));
                    }
                }

                if (propagate && ResourceExplorerView.getTreeViewer() != null) {
                    if (!rec) {
                        ResourceExplorerView.getTreeViewer().select(view.getElement());
                    }
                }
            }
        }
    }

    /**
     * Deselects view and its children.
     */
    public void deselect(View view) {
        if (selected.contains(view)) {
            view.setSelected(false);
            selected.remove(view);

            for (View v : view.getChilds()) {
                deselect(v);
            }
        }
    }

    /**
     * Returns selected views.
     */
    public ArrayList<View> getSelected() {
        return selected;
    }

    /**
     * Deselects all views.
     */
    public void deselectAll() {
        for (View view : selected) {
            // clear selection
            view.setSelected(false);
        }

        selected.clear();
    }

    /**
     * Selects from first selected view to specified one.
     */
    public void selectTo(View view) {
        if (selected.size() == 0) {
            invertSelection(view);
        } else {
            View firstSelected = selected.get(0);
            List<NodeView> labelsInRange = matrix.subset(firstSelected.getPosition(), view.getPosition());
            for (NodeView l : labelsInRange) {
                select(l);
            }
        }
    }

    /**
     * Updates selection handler to activate context popup menu.
     */
    public void updateSelectionHandler() {
        if (RMStore.isConnected()) {
            ArrayList<Selectable> selectedNodes = new ArrayList<Selectable>();

            for (View view : selected) {
                // Find the source of the selected item for the removing source and add node combo in compact view
                RMStore.getInstance().getModel().findSelectedSource(view.getElement());

                if ((view instanceof NodeView)) {
                    selectedNodes.add((Selectable) view.getElement());
                }
            }
            RemoveNodesHandler.getInstance().setSelectedNodes(selectedNodes);
            LockNodesHandler.getInstance().setSelectedNodes(selectedNodes);
            UnlockNodesHandler.getInstance().setSelectedNodes(selectedNodes);
            DescribeCommandHandler.getInstance().setSelectedNodes(selectedNodes);
        }
    }

}
