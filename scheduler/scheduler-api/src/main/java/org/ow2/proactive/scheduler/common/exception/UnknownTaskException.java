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
package org.ow2.proactive.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * Exception generated when trying to get task result and the task result does not exist.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class UnknownTaskException extends SchedulerException {

    private JobId jobId;

    private String taskName;

    private TaskId taskId;

    /**
     * Create a new instance of UnknownTaskException
     *
     * @param msg the message to attach.
     */
    public UnknownTaskException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of UnknownTaskException
     */
    public UnknownTaskException() {
    }

    public UnknownTaskException(String taskName, JobId jobId) {
        super("The task " + taskName + "of job " + jobId + " is unknown !");
        this.jobId = jobId;
        this.taskName = taskName;
    }

    public UnknownTaskException(TaskId taskId, JobId jobId) {
        super("The task " + taskId + "of job " + jobId + " is unknown !");
        this.jobId = jobId;
        this.taskId = taskId;
    }

    /**
     * Create a new instance of UnknownTaskException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public UnknownTaskException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of UnknownTaskException
     *
     * @param cause the cause of the exception.
     */
    public UnknownTaskException(Throwable cause) {
        super(cause);
    }

}
