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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class TaskFlowJob. This is the definition of a tasks flow job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InternalTaskFlowJob extends InternalJob {

    /**
     * ProActive empty constructor.
     */
    public InternalTaskFlowJob() {
        super();
    }

    /**
     * Create a new Tasks Flow Job with the given parameters. It provides
     * methods to add or remove tasks.
     *
     * @param name
     *            the current job name.
     * @param priority
     *            the priority of this job between 1 and 5.
     * @param onTaskError
     *            Defines the error behavior if a task error occurs.
     * @param description
     *            a short description of the job and what it will do.
     */

    // * @param runtimeLimit the maximum execution time for this job given in
    // millisecond.
    public InternalTaskFlowJob(String name, JobPriority priority, OnTaskError onTaskError, String description) {
        super(name, priority, onTaskError, description);
    }

    /**
     * Append a list of tasks to this job.
     *
     * @param tasks
     *            the list of tasks to add.
     * @return true if the list of tasks have been correctly added to the job,
     *         false if not.
     */
    public boolean addTasks(List<InternalTask> tasks) {
        for (InternalTask td : tasks) {
            if (!addTask(td)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.job.InternalJob#getType()
     */
    @Override
    public JobType getType() {
        return JobType.TASKSFLOW;
    }
}
