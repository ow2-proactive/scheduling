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
package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;


public class TimerTreeHolderEditPart extends AbstractTimerTreeEditPart {
    protected List getModelChildren() {
        return ((TimerTreeHolder) getModel()).getChildren();
    }

    public final void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TimerTreeHolder.P_ADD_SOURCE)) {
            refreshChildren();
        } else if (evt.getPropertyName()
                          .equals(TimerTreeHolder.P_REMOVE_SELECTED)) {
            ((TimerTreeHolder) this.getModel()).removeDummyRoot((TimerTreeNodeObject) evt.getNewValue());
            List<EditPart> l = this.getViewer().getSelectedEditParts();

            // In order to avoid concurrent exception create a temporary list to be filled with parts to delete
            List<EditPart> toDelete = new ArrayList<EditPart>();

            // Deactivate selected parts
            for (final EditPart e : l) {
                e.deactivate();
                toDelete.add(e);
            }

            // Remove them from the current root editpart
            for (final EditPart e : toDelete) {
                this.removeChild(e);
            }
            // Refresh children list
            this.refreshChildren();
        }
    }
}
