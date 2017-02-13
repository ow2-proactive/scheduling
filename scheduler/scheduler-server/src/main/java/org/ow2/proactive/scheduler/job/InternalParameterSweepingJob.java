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
package org.ow2.proactive.scheduler.job;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class ParameterSweepingJob. This is the definition of a Parameter Sweeping
 * Job.
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
        super();
    }

    /**
     * Create a new Parameter Sweeping Job with the given parameters. It
     * provides methods to add or remove tasks.
     *
     * @param name
     *            the current job name.
     * @param priority
     *            the priority of this job between 1 and 5.
     * @param onTaskError
     *            Defines the error behavior when a task error occurs.
     * @param description
     *            a short description of the job and what it will do.
     */

    // * @param runtimeLimit the maximum execution time for this job given in
    // millisecond.
    public InternalParameterSweepingJob(String name, JobPriority priority, OnTaskError onTaskError,
            String description) {
        super(name, priority, onTaskError, description);
    }

    /**
     * Append a task to this job, only if no task has been added before.
     *
     * @param task
     *            the task to add.
     * @return true if the task has been correctly added to the job, false if
     *         not.
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
