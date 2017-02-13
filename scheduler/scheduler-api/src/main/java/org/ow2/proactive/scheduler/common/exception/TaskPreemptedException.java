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


/**
 * Exception generated when admin preempt a task.<br>
 * This exception is thrown in the task result of the preempted task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class TaskPreemptedException extends SchedulerException {

    /**
     * Create a new instance of TaskPreemptedException
     *
     * @param msg the message to attach.
     */
    public TaskPreemptedException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of TaskPreemptedException
     */
    public TaskPreemptedException() {
    }

    /**
     * Create a new instance of TaskPreemptedException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public TaskPreemptedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of TaskPreemptedException
     *
     * @param cause the cause of the exception.
     */
    public TaskPreemptedException(Throwable cause) {
        super(cause);
    }

}
