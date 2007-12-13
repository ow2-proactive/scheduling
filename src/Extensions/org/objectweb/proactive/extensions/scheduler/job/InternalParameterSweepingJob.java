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
package org.objectweb.proactive.extensions.scheduler.job;

import org.objectweb.proactive.extensions.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extensions.scheduler.common.job.JobType;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask;


/**
 * Class ParameterSweepingJob.
 * This is the definition of a Parameter Swipping Job.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jun 7, 2007
 * @since ProActive 3.9
 */
public class InternalParameterSweepingJob extends InternalJob {

    /**
     * ProActive empty constructor.
     */
    public InternalParameterSweepingJob() {
    }

    /**
     * Create a new Parameter Sweeping Job with the given parameters. It provides methods to add or
     * remove tasks.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param cancelOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */

    //   * @param runtimeLimit the maximum execution time for this job given in millisecond.
    public InternalParameterSweepingJob(String name, JobPriority priority, boolean cancelOnError,
            String description) {
        super(name, priority, cancelOnError, description);
    }

    /**
     * Append a task to this job, only if no task has been added before.
     *
     * @param task the task to add.
     * @return true if the task has been correctly added to the job, false if not.
     */
    @Override
    public boolean addTask(InternalTask task) {
        if (getTasks().size() > 0) {
            return false;
        }

        return super.addTask(task);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.job.JobU#getType()
     */
    @Override
    public JobType getType() {
        return JobType.PARAMETER_SWEEPING;
    }
}
