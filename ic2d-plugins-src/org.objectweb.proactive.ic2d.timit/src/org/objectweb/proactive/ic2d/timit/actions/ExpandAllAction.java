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
package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class ExpandAllAction extends Action {
    public static final String EXPAND_ALL = "Expand All";
    private TimerTreeHolder timerTreeHolder;

    public ExpandAllAction(TimerTreeHolder t) {
        this.timerTreeHolder = t;
        this.setId(EXPAND_ALL);
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "expandall.gif"));
        this.setToolTipText(EXPAND_ALL);
        this.setEnabled(true);
    }

    @Override
    public void run() {
        if ((this.timerTreeHolder == null) ||
                (this.timerTreeHolder.getChildren() == null)) {
            return;
        }
        for (TimerObject t : timerTreeHolder.getChildren()) {
            t.firePropertyChange(TimerObject.P_EXPAND_STATE, null, true);
        }
    }
}
