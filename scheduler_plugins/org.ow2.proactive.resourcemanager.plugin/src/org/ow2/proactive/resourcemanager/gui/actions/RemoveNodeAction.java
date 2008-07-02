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
package org.ow2.proactive.resourcemanager.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.ow2.proactive.resourcemanager.gui.dialog.RemoveNodeDialog;
import org.ow2.proactive.resourcemanager.gui.tree.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.tree.TreeLeafElement;


/**
 * @author The ProActive Team
 */
public class RemoveNodeAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static RemoveNodeAction instance = null;
    private TreeViewer viewer = null;
    private Composite parent = null;

    private RemoveNodeAction(Composite parent, TreeViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
        this.setText("Remove node");
        this.setToolTipText("To remove a node");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/remove_node.gif"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        String tmp = null;
        ISelection selection = viewer.getSelection();
        if (selection != null) {
            TreeLeafElement leaf = (TreeLeafElement) ((IStructuredSelection) selection).getFirstElement();
            if (leaf != null) {
                if (leaf.getType().equals(TreeElementType.NODE)) {
                    tmp = leaf.getName();
                } else
                    viewer.setSelection(null);
            }
        }
        RemoveNodeDialog.showDialog(parent.getShell(), tmp);
    }

    public static RemoveNodeAction newInstance(Composite parent, TreeViewer viewer) {
        instance = new RemoveNodeAction(parent, viewer);
        return instance;
    }

    public static RemoveNodeAction getInstance() {
        return instance;
    }
}