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
package org.ow2.proactive.resourcemanager.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Selectable;
import org.ow2.proactive.resourcemanager.gui.handlers.LockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.UnlockNodesHandler;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;
import org.ow2.proactive.resourcemanager.gui.views.NodeInfoView;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


public class TableSelectionListener implements ISelectionChangedListener {

    public void selectionChanged(SelectionChangedEvent event) {
        final Object[] arr = ((IStructuredSelection) event.getSelectionProvider().getSelection()).toArray();
        if (arr.length != 0) {
            final List<Selectable> list = new ArrayList<Selectable>(arr.length);
            for (final Object o : arr) {
                list.add((Selectable) o);
            }
            // normally RM is connected if I can select something...
            if (RMStore.isConnected()) {
                //check for null in order to fix SCHEDULING-1383
                //FIXME: Ugly design. getInstance() method should never return null
                if (RemoveNodesHandler.getInstance() != null)
                    RemoveNodesHandler.getInstance().setSelectedNodes(list);
                if (LockNodesHandler.getInstance() != null)
                    LockNodesHandler.getInstance().setSelectedNodes(list);
                if (RemoveNodesHandler.getInstance() != null)
                    RemoveNodesHandler.getInstance().setSelectedNodes(list);
            }

            if (list.size() > 0) {
                Node n = (Node) list.get(0);
                NodeInfoView.setNode(n);

                if (ResourceExplorerView.getTreeViewer() != null) {
                    ResourceExplorerView.getTreeViewer().select(n);
                }
            }
            if (ResourcesCompactView.getCompactViewer() != null) {
                ResourcesCompactView.getCompactViewer().getSelectionManager().select(list);
            }
        }
    }
}
