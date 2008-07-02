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
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.gui.dialog.AddNodeDialog;
import org.ow2.proactive.resourcemanager.gui.tree.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.tree.TreeLeafElement;


/**
 * @author The ProActive Team
 */
public class AddNodeAction extends Action {

    /**
     * determine the enabled/disabled action state at its instantiation
     */
    public static final boolean ENABLED_AT_CONSTRUCTION = false;

    private static AddNodeAction instance = null;
    private TreeViewer viewer = null;
    private Composite parent = null;

    private AddNodeAction(Composite parent, TreeViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
        this.setText("Add node(s)");
        this.setToolTipText("To add node(s)");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/add_node.gif"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        String tmp = null;
        ISelection selection = viewer.getSelection();
        if (selection != null) {
            TreeLeafElement leaf = (TreeLeafElement) ((IStructuredSelection) selection).getFirstElement();
            if (leaf != null) {
                if ((leaf.getType().equals(TreeElementType.SOURCE)) &&
                    (!leaf.getName().equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME))) {
                    tmp = leaf.getName();
                } else
                    viewer.setSelection(null);
            }
        }
        AddNodeDialog.showDialog(parent.getShell(), tmp);
    }

    /**
     * Creation a new AddNodeAction instance
     * @param parent the composite originating the action
     * @param viewer the TreeViewer, used to determine the node source wherein 
     * the node should be added.
     * @return the AddNodeAction object
     */
    public static AddNodeAction newInstance(Composite parent, TreeViewer viewer) {
        instance = new AddNodeAction(parent, viewer);
        return instance;
    }

    /** return an instance of this class
     * @return the AddNodeAction object instance
     */
    public static AddNodeAction getInstance() {
        return instance;
    }
}