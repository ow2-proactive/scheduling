/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.job;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class ParameterSweepingJob.
 * This is the definition of a Parameter Sweeping Job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
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
     * @see org.ow2.proactive.scheduler.job.InternalJob#getType()
     */
    @Override
    public JobType getType() {
        return JobType.PARAMETER_SWEEPING;
    }
}
