/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownTaskRestException;


public class ExceptionUtility {

    private ExceptionUtility() {
    }

    public static void throwNCE(Exception e) throws NotConnectedException {
        if (e instanceof NotConnectedRestException) {
            throw new NotConnectedException(e);
        } else {
            throw new RuntimeException(e);
        }
    }

    public static RuntimeException throwNCEOrPE(Exception e) throws NotConnectedException,
            PermissionException {
        if (e instanceof PermissionRestException) {
            throw new PermissionException(e);
        } else {
            throwNCE(e);
        }
        return new RuntimeException(e);
    }

    public static void throwUJEOrNCEOrPE(Exception e) throws UnknownJobException, NotConnectedException,
            PermissionException {
        if (e instanceof UnknownJobRestException) {
            throw new UnknownJobException(e);
        } else {
            throwNCEOrPE(e);
        }
    }

    public static void throwUJEOrNCEOrPEOrUTE(Exception e) throws UnknownJobException, NotConnectedException,
            PermissionException, UnknownTaskException {
        if (e instanceof UnknownTaskRestException) {
            throw new UnknownTaskException(e);
        } else {
            throwUJEOrNCEOrPE(e);
        }
    }

    public static void throwNCEOrPEOrSCEOrJCE(Exception e) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        if (e instanceof SubmissionClosedRestException) {
            throw new SubmissionClosedException(e);
        } else if (e instanceof JobCreationRestException) {
            throw new JobCreationException(e);
        } else {
            throwNCEOrPE(e);
        }
    }

    public static void throwJAFEOrUJEOrNCEOrPE(Exception e) throws JobAlreadyFinishedException,
            UnknownJobException, NotConnectedException, PermissionException {
        if (e instanceof JobAlreadyFinishedRestException) {
            throw new JobAlreadyFinishedException(e);
        } else {
            throwUJEOrNCEOrPE(e);
        }
    }

    public static String getStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        error.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static Exception exception(Throwable t) {
        return (t instanceof Exception) ? (Exception) t : new RuntimeException(t);
    }

}
