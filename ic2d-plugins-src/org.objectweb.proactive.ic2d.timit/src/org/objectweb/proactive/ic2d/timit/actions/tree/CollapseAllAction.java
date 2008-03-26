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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;


/**
 * This action is executed when the user wants to collapse all timer trees in the Timer Tree View.
 * @author The ProActive Team
 *
 */
public class CollapseAllAction extends Action {
    public static final String COLLAPSE_ALL = "Collapse All";
    private final TimerTreeHolder timerTreeHolder;

    public CollapseAllAction(final TimerTreeHolder t) {
        this.timerTreeHolder = t;
        this.setId(COLLAPSE_ALL);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/collapse_all.gif"), null)));
        this.setToolTipText(COLLAPSE_ALL);
        this.setEnabled(true);
    }

    @Override
    public final void run() {
        if ((this.timerTreeHolder == null) || (this.timerTreeHolder.getChildren() == null)) {
            return;
        }
        for (final TimerTreeNodeObject t : timerTreeHolder.getChildren()) {
            t.firePropertyChange(TimerTreeNodeObject.P_EXPAND_STATE, null, false);
        }
    }
}
