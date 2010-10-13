/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
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
import org.ow2.proactive.scheduler.common.job.JobId;


/**
 * Exception generated when trying to get information about a job that does not exist.<br>
 * This exception is thrown each time the scheduler cannot perform a user request due to an unknown job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class UnknownJobException extends SchedulerException {

    private JobId jobId;

    /**
     * Create a new instance of UnknownJobException
     *
     * @param msg the message to attach.
     */
    public UnknownJobException(String msg) {
        super(msg);
    }

    public UnknownJobException(JobId jobId) {
        super("The job " + jobId + " does not exist !");
        this.jobId = jobId;
    }

    /**
     * Create a new instance of UnknownJobException
     */
    public UnknownJobException() {
    }

    /**
     * Create a new instance of UnknownJobException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public UnknownJobException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of UnknownJobException
     *
     * @param cause the cause of the exception.
     */
    public UnknownJobException(Throwable cause) {
        super(cause);
    }

    public JobId getJobId() {
        return jobId;
    }

}
