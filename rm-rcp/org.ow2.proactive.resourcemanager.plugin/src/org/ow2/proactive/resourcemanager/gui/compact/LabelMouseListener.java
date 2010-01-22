/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.resourcemanager.gui.compact;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.ow2.proactive.resourcemanager.gui.compact.view.View;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Class handles mouse events over labels in compact view.
 *
 */
public class LabelMouseListener implements MouseListener {

    private View view;
    private SelectionManager selectionManager;

    public LabelMouseListener(View view) {
        this.view = view;
        this.selectionManager = ResourcesCompactView.getCompactViewer().getSelectionManager();
    }

    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
    }

    /**
     * Handled events:
     * - simple click -> selection
     * - double click -> open of tree view and selection of corresponding element
     * - click with ctrl or shift -> standard selection mechanism
     * - right click -> menu
     */
    public void mouseUp(MouseEvent e) {
        if (e.button == 3) {
            // right button click
            // updating selection handler and show menu
            selectionManager.updateSelectionHandler();
            view.getLabel().getParent().getMenu().setVisible(true);
        } else if (e.count > 1) {
            // double click => moving to tree view
            if (ResourcesCompactView.getInstance() != null) {
                ResourcesCompactView.getInstance().putTreeViewOnTop();

                if (ResourceExplorerView.getTreeViewer() != null) {
                    ResourceExplorerView.getTreeViewer().expandAll();
                    ResourceExplorerView.getTreeViewer().select(view.getElement());
                }
            }
        } else if ((e.stateMask & SWT.SHIFT) != 0) {
            // select group of labels
            selectionManager.selectTo(view);
        } else if ((e.stateMask & SWT.CTRL) != 0) {
            // add another selected label
            selectionManager.invertSelection(view);
        } else {
            // select single label
            selectionManager.deselectAll();
            selectionManager.invertSelection(view);
        }
    }

}
