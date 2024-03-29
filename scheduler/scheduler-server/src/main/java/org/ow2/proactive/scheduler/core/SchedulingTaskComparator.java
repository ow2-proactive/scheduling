/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.SelectionScript;

import com.google.common.base.Strings;


/**
 * SchedulingJobComparator is used to compare jobs for scheduling.
 * The comparison is made on some particular fields described in this class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class SchedulingTaskComparator {

    private InternalTask task;

    private Set<String> digests = new HashSet<>();

    private static final Logger logger = Logger.getLogger(SchedulingTaskComparator.class);

    private InternalJob job;

    private String sessionid;

    private String owner;

    /**
     * Create a new instance of SchedulingTaskComparator
     *
     * @param task
     * @param sessionid
     */
    public SchedulingTaskComparator(InternalTask task, InternalJob job, String sessionid) {
        this.task = task;
        this.job = job;
        this.sessionid = sessionid;
        this.owner = job.getOwner();
        if (task.getSelectionScripts() != null) {
            computeHashForSelectionScripts();
        }
    }

    private void computeHashForSelectionScripts() {
        List<SelectionScript> scriptList = SchedulingMethodImpl.resolveScriptVariables(task.getSelectionScripts(),
                                                                                       task.getRuntimeVariables(),
                                                                                       sessionid,
                                                                                       owner);

        for (SelectionScript script : scriptList) {
            SelectionScript modifiedScript = script;
            try {
                Map<String, Serializable> bindings = SchedulingMethodImpl.createBindingsForSelectionScripts(job,
                                                                                                            task,
                                                                                                            null);
                modifiedScript = SchedulingMethodImpl.replaceBindingsInsideScript(script, bindings);

            } catch (Exception e) {
                logger.error("Error while replacing selection script bindings for task " + task.getId(), e);
            }
            try {
                digests.add(new String(modifiedScript.digest()));
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error while computing selection script digest for task " + task.getId(), e);
            }
        }
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
        boolean sameSsHash = digests.equals(tcomp.digests);
        //test whether nodes exclusion are the same (both is null...
        boolean sameNodeEx = (task.getNodeExclusion() == null) && (tcomp.task.getNodeExclusion() == null);
        //...or they are equals
        sameNodeEx = sameNodeEx ||
                     (task.getNodeExclusion() != null && task.getNodeExclusion().equals(tcomp.task.getNodeExclusion()));
        //test whether owner is the same
        boolean sameOwner = this.job.getOwner().equals(tcomp.job.getOwner());
        //if the parallel environment is specified for any of tasks => not equal
        boolean isParallel = task.isParallel() || tcomp.task.isParallel();

        Map<String, String> taskGenericInfoA = task.getRuntimeGenericInformation();
        Map<String, String> taskGenericInfoB = tcomp.task.getRuntimeGenericInformation();

        String tokenA = taskGenericInfoA.get(SchedulerConstants.NODE_ACCESS_TOKEN);
        String tokenB = taskGenericInfoB.get(SchedulerConstants.NODE_ACCESS_TOKEN);

        boolean sameToken = (Strings.isNullOrEmpty(tokenA) && Strings.isNullOrEmpty(tokenB)) ||
                            (!Strings.isNullOrEmpty(tokenA) && !Strings.isNullOrEmpty(tokenB) && tokenA.equals(tokenB));

        String nsA = taskGenericInfoA.get(SchedulerConstants.NODE_SOURCE_GENERIC_INFO);
        String nsB = taskGenericInfoB.get(SchedulerConstants.NODE_SOURCE_GENERIC_INFO);

        boolean sameNodeSourceGI = (Strings.isNullOrEmpty(nsA) && Strings.isNullOrEmpty(nsB)) ||
                                   (!Strings.isNullOrEmpty(nsA) && !Strings.isNullOrEmpty(nsB) &&
                                    nsA.equalsIgnoreCase(nsB));

        // if topology is specified for any of task => not equal
        // for now topology is allowed only for parallel tasks which is
        // checked before

        //add the 6 tests to the returned value
        return sameSsHash && sameNodeEx && sameOwner && !isParallel && sameToken && sameNodeSourceGI;
    }

}
