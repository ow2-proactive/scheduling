/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
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
            RMModel rmmodel = (RMModel) model;
            StatsItem pendingNodesStat = new StatsItem("# deploying nodes", Integer.toString(rmmodel
                    .getPendingNodesNumber()));
            StatsItem lostNodesStat = new StatsItem("# lost nodes", Integer.toString(rmmodel
                    .getLostNodesNumber()));
            StatsItem configuringNodesStat = new StatsItem("# configuring nodes", Integer.toString(rmmodel
                    .getConfiguringNodesNumber()));
            StatsItem freeNodesStat = new StatsItem("# free nodes", Integer.toString(rmmodel
                    .getFreeNodesNumber()));
            StatsItem busyNodesStat = new StatsItem("# busy nodes", Integer.toString(rmmodel
                    .getBusyNodesNumber()));
            StatsItem downNodesStat = new StatsItem("# down nodes", Integer.toString(rmmodel
                    .getDownNodesNumber()));
            StatsItem totalNodesStat = new StatsItem("# total nodes", Integer.toString(rmmodel
                    .getTotalNodesNumber()));
            return new StatsItem[] { pendingNodesStat, lostNodesStat, configuringNodesStat, freeNodesStat,
                    busyNodesStat, downNodesStat, totalNodesStat };
        }
        //should never return this, RMStatsViewer
        return new Object[] {};
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
