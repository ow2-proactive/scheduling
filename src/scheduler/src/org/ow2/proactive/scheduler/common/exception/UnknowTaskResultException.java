/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
 * Exception generated when trying to get task result and the task result does not exist.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class UnknowTaskResultException extends SchedulerException {

    /**
     * Create a new instance of UnknowTaskResultException
     * 
     * @param msg the message to attach.
     */
    public UnknowTaskResultException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of UnknowTaskResultException
     */
    public UnknowTaskResultException() {
    }

    /**
     * Create a new instance of UnknowTaskResultException
     * 
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public UnknowTaskResultException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of UnknowTaskResultException
     * 
     * @param cause the cause of the exception.
     */
    public UnknowTaskResultException(Throwable cause) {
        super(cause);
    }

}
