/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jobmonitoring.actions;

import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


/**
 * @author Jean-Michael Legait and Michèle Reynier
 *
 */
public class CollapseAllAction extends Action {
    private TreeViewer treeViewer;

    /**
     *
     */
    public CollapseAllAction(TreeViewer treeViewer) {
        super("Collapse All");
        this.treeViewer = treeViewer;
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "collapse.gif"));
        this.setToolTipText("Collapse All");
    }

    @Override
    public void run() {
        collapse(((Tree) ((RootTreeEditPart) treeViewer.getRootEditPart()).getWidget()).getItems());
    }

    private void collapse(TreeItem[] items) {
        for (int i = 0; i < items.length; i++) {
            items[i].setExpanded(false);
            collapse(items[i].getItems());
        }
    }
}
