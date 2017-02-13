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
package org.ow2.proactive.scheduler.rest;

import java.lang.reflect.Constructor;

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

import com.google.common.base.Strings;
import com.google.common.base.Throwables;


public class ExceptionUtility {

    private ExceptionUtility() {
    }

    public static void throwNCE(Exception e) throws NotConnectedException {
        if (e instanceof NotConnectedRestException) {
            throw reconstructError(e, NotConnectedException.class);
        } else {
            throw new RuntimeException(e);
        }
    }

    public static RuntimeException throwNCEOrPE(Exception e) throws NotConnectedException, PermissionException {
        if (e instanceof PermissionRestException) {
            throw reconstructError(e, PermissionException.class);
        } else {
            throwNCE(e);
        }
        return new RuntimeException(e);
    }

    public static void throwUJEOrNCEOrPE(Exception e)
            throws UnknownJobException, NotConnectedException, PermissionException {
        if (e instanceof UnknownJobRestException) {
            throw reconstructError(e, UnknownJobException.class);
        } else {
            throwNCEOrPE(e);
        }
    }

    public static void throwUJEOrNCEOrPEOrUTE(Exception e)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        if (e instanceof UnknownTaskRestException) {
            throw reconstructError(e, UnknownTaskException.class);
        } else {
            throwUJEOrNCEOrPE(e);
        }
    }

    public static void throwNCEOrPEOrSCEOrJCE(Exception e)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        if (e instanceof SubmissionClosedRestException) {
            throw reconstructError(e, SubmissionClosedException.class);
        } else if (e instanceof JobCreationRestException) {
            throw reconstructError(e, JobCreationException.class);
        } else {
            throwNCEOrPE(e);
        }
    }

    public static void throwJAFEOrUJEOrNCEOrPE(Exception e)
            throws JobAlreadyFinishedException, UnknownJobException, NotConnectedException, PermissionException {
        if (e instanceof JobAlreadyFinishedRestException) {
            throw reconstructError(e, JobAlreadyFinishedException.class);
        } else {
            throwUJEOrNCEOrPE(e);
        }
    }

    public static Exception exception(Throwable t) {
        return (t instanceof Exception) ? (Exception) t : new RuntimeException(t);
    }

    private static <T extends Exception> T reconstructError(Exception source, Class<T> target) {
        Throwable cause = source.getCause();
        boolean found = false;
        while (cause != null) {
            if (Strings.nullToEmpty(cause.getMessage()).startsWith(target.getName())) {
                found = true;
                break;
            }
            cause = cause.getCause();
        }
        try {
            Exception reconstructed = null;
            if (found) {
                Constructor<? extends Exception> ctor = target.getConstructor(String.class);
                reconstructed = ctor.newInstance(cause.getMessage());
                reconstructed.setStackTrace(cause.getStackTrace());
            } else {
                Constructor<? extends Exception> ctor = target.getConstructor(Throwable.class);
                reconstructed = ctor.newInstance(source);
            }
            return target.cast(reconstructed);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
