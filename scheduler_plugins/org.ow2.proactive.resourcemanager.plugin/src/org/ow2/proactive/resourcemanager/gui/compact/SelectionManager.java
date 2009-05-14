/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.resourcemanager.gui.compact;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.resourcemanager.gui.compact.view.NodeView;
import org.ow2.proactive.resourcemanager.gui.compact.view.View;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;


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
     * Selects view and its children.
     */
    public void select(View view) {
        if (!selected.contains(view)) {
            view.setSelected(true);
            selected.add(view);
            for (View v : view.getChilds()) {
                select(v);
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
            ArrayList<Node> selectedNodes = new ArrayList<Node>();

            for (View view : selected) {
                if (view instanceof NodeView) {
                    selectedNodes.add((Node) view.getElement());
                }
            }
            RemoveNodesHandler.getInstance().setSelectedNodes(selectedNodes);
        }
    }

}
