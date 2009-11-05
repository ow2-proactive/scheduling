/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.stats;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ow2.proactive.resourcemanager.gui.data.model.RMModel;


public class StatsContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object model) {
        if (model instanceof RMModel) {
            StatsItem freeNodesStat = new StatsItem("# free nodes", Integer.toString(((RMModel) model)
                    .getFreeNodesNumber()));
            StatsItem busyNodesStat = new StatsItem("# busy nodes", Integer.toString(((RMModel) model)
                    .getBusyNodesNumber()));
            StatsItem downNodesStat = new StatsItem("# down nodes", Integer.toString(((RMModel) model)
                    .getDownNodesNumber()));
            return new StatsItem[] { freeNodesStat, busyNodesStat, downNodesStat };
        }
        //should never return this, RMStatsViewer
        return new Object[] {};
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

}
