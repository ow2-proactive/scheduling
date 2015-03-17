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
package org.ow2.proactive.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exception generated when user or admin restart a task.<br>
 * The current execution terminates immediately throwing this exception. The task can be restarted later.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class TaskRestartedException extends SchedulerException {

    private static final long serialVersionUID = 61L;

    /**
     * Create a new instance of TaskRestartedException
     *
     * @param msg the message to attach.
     */
    public TaskRestartedException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of TaskRestartedException
     */
    public TaskRestartedException() {
    }

    /**
     * Create a new instance of TaskRestartedException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public TaskRestartedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of TaskRestartedException
     *
     * @param cause the cause of the exception.
     */
    public TaskRestartedException(Throwable cause) {
        super(cause);
    }

}
