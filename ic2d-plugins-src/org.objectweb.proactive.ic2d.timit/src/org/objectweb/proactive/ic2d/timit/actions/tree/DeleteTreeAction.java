/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.timit.actions.tree;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;


public class DeleteTreeAction extends Action {
    public static final String DELETE_TREE_ACTION = "Delete Tree";
    private TimerTreeNodeObject target;

    public DeleteTreeAction() {
        super.setId(DELETE_TREE_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "delete_obj.gif"));
        super.setToolTipText(DELETE_TREE_ACTION);
        super.setEnabled(false);
    }

    @Override
    public final void run() {
        TimerTreeHolder t = TimerTreeHolder.getInstance();
        if (t != null) {
            t.firePropertyChange(TimerTreeHolder.P_REMOVE_SELECTED, null, target);
            //t.removeDummyRoot(target);
        }
    }

    public final void setTarget(final TimerTreeNodeObject target) {
        if (target == null) {
            this.setEnabled(false);
            return;
        }
        this.target = target;
        this.setEnabled(true);
    }
}
