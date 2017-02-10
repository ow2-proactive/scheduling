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
 * Exception generated when a task could not be started due to
 * dependences failure.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@PublicAPI
public class TaskCouldNotStartException extends SchedulerException {

    /**
     * Create a new instance of TaskCouldNotStartException
     *
     * @param msg the message to attach.
     */
    public TaskCouldNotStartException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of TaskCouldNotStartException
     */
    public TaskCouldNotStartException() {
        super("The task could not start due to dependency failure");
    }

    /**
     * Create a new instance of TaskCouldNotStartException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public TaskCouldNotStartException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of TaskCouldNotStartException
     *
     * @param cause the cause of the exception.
     */
    public TaskCouldNotStartException(Throwable cause) {
        super(cause);
    }

}
