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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.views.TimerTreeView;


public class TreeEditPartFactory implements EditPartFactory {
    private TimerTreeView timerTreeView;

    public TreeEditPartFactory(final TimerTreeView timerTreeView) {
        this.timerTreeView = timerTreeView;
    }

    public final EditPart createEditPart(final EditPart context,
        final Object model) {
        EditPart part = null;
        if (model instanceof TimerTreeHolder) {
            part = new TimerTreeHolderEditPart();
        } else if (model instanceof TimerObject) {
            part = new TimerEditPart(timerTreeView);
        }
        if (part != null) {
            part.setModel(model);
        }
        return part;
    }
}
