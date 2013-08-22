/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * SchedulingJobComparator is used to compare jobs for scheduling.
 * The comparison is made on some particular fields described in this class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class SchedulingTaskComparator {

    private InternalTask task;
    private int ssHashCode;
    private String owner;

    /**
     * Create a new instance of SchedulingTaskComparator
     *
     * @param task
     */
    public SchedulingTaskComparator(InternalTask task, String owner) {
        this.task = task;
        this.ssHashCode = SelectionScript.hashCodeFromList(task.getSelectionScripts());
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SchedulingTaskComparator)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        //ELSE : get given task comparator
        SchedulingTaskComparator tcomp = (SchedulingTaskComparator) obj;
        if (task == tcomp.task) {
            return true;
        }
        //test whether the selections script are the same
        boolean sameSsHash = (ssHashCode == tcomp.ssHashCode);
        //test whether nodes exclusion are the same (both is null...
        boolean sameNodeEx = (task.getNodeExclusion() == null) && (tcomp.task.getNodeExclusion() == null);
        //...or they are equals
        sameNodeEx = sameNodeEx ||
            (task.getNodeExclusion() != null && task.getNodeExclusion().equals(tcomp.task.getNodeExclusion()));
        //test whether owner is the same
        boolean sameOwner = this.owner.equals(tcomp.owner);
        //if the parallel environment is specified for any of tasks => not equal
        boolean isParallel = task.isParallel() || tcomp.task.isParallel();

        boolean requireNodeWithTokern = task.getGenericInformations().containsKey(
                SchedulerConstants.NODE_ACCESS_TOKEN) ||
            tcomp.task.getGenericInformations().containsKey(SchedulerConstants.NODE_ACCESS_TOKEN);

        // if topology is specified for any of task => not equal
        // for now topology is allowed only for parallel tasks which is
        // checked before

        // boolean topologySpecified = task.isTopologySpecified() || tcomp.task.isTopologySpecified();
        //add the 5 tests to the returned value
        return sameSsHash && sameNodeEx && sameOwner && !isParallel && !requireNodeWithTokern;
    }

    /**
     * Get the ssHashCode
     *
     * @return the ssHashCode
     */
    public int getSsHashCode() {
        return ssHashCode;
    }

}
