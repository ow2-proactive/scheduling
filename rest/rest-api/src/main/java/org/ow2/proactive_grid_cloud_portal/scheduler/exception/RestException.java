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
package org.ow2.proactive_grid_cloud_portal.scheduler.exception;

import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.signal.SignalApiException;


public class RestException extends Exception {
    public RestException(String message) {
        super(message);
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RestException wrapExceptionToRest(SchedulerException schedulerException) {
        if (schedulerException instanceof NotConnectedException) {
            return new NotConnectedRestException(schedulerException);
        } else if (schedulerException instanceof PermissionException) {
            return new PermissionRestException(schedulerException);
        } else if (schedulerException instanceof UnknownJobException) {
            return new UnknownJobRestException(schedulerException);
        } else if (schedulerException instanceof JobAlreadyFinishedException) {
            return new JobAlreadyFinishedRestException(schedulerException);
        } else if (schedulerException instanceof SubmissionClosedException) {
            return new SubmissionClosedRestException(schedulerException);
        } else if (schedulerException instanceof JobCreationException) {
            return new JobCreationRestException(schedulerException);
        } else if (schedulerException instanceof UnknownTaskException) {
            return new UnknownTaskRestException(schedulerException);
        } else if (schedulerException instanceof SignalApiException) {
            return new SignalApiRestException(schedulerException);
        }
        return new UnknownJobRestException(schedulerException);
    }

    public static SchedulerException unwrapRestException(RestException restException) {
        if (restException instanceof NotConnectedRestException) {
            return new NotConnectedException(restException);
        } else if (restException instanceof PermissionRestException) {
            return new PermissionException(restException);
        } else if (restException instanceof UnknownJobRestException) {
            return new UnknownJobException(restException);
        } else if (restException instanceof JobAlreadyFinishedRestException) {
            return new JobAlreadyFinishedException(restException);
        } else if (restException instanceof SubmissionClosedRestException) {
            return new SubmissionClosedException(restException);
        } else if (restException instanceof JobCreationRestException) {
            return new JobCreationException(restException);
        } else if (restException instanceof UnknownTaskRestException) {
            return new UnknownTaskException(restException);
        } else if (restException instanceof SignalApiRestException) {
            return new SignalApiException(restException);
        }
        return new UnknownJobException(restException);
    }

}
