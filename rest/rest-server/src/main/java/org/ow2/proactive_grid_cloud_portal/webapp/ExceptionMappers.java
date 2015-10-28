/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyException;

import javax.security.auth.login.LoginException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;


public class ExceptionMappers {

    private static class BaseExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
        @Override
        public Response toResponse(T throwable) {
            ExceptionToJson js = new ExceptionToJson();
            js.setErrorMessage(throwable.getMessage());
            js.setHttpErrorCode(getErrorCode());
            js.setStackTrace(ProActiveLogger.getStackTraceAsString(throwable));
            js.setException(throwable);
            js.setExceptionClass(throwable.getClass().getName());
            return Response.status(getErrorCode()).header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON).entity(js).build();
        }

        protected int getErrorCode() {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    public static class IOExceptionMapper extends BaseExceptionMapper<IOException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class NotFoundExceptionMapper extends BaseExceptionMapper<NotFoundException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class JobAlreadyFinishedExceptionExceptionMapper extends
            BaseExceptionMapper<JobAlreadyFinishedException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class JobCreationRestExceptionExceptionMapper extends
            BaseExceptionMapper<JobCreationRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class KeyExceptionExceptionMapper extends BaseExceptionMapper<KeyException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class LoginExceptionExceptionMapper extends BaseExceptionMapper<LoginException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class NotConnectedRestExceptionExceptionMapper extends
            BaseExceptionMapper<NotConnectedRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_UNAUTHORIZED;
        }
    }

    public static class NotConnectedExceptionExceptionMapper extends
            BaseExceptionMapper<NotConnectedException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_UNAUTHORIZED;
        }
    }

    public static class PermissionRestExceptionExceptionMapper extends
            BaseExceptionMapper<PermissionRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
    }

    public static class SchedulerRestExceptionExceptionMapper extends
            BaseExceptionMapper<SchedulerRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class SubmissionClosedRestExceptionExceptionMapper extends
            BaseExceptionMapper<SubmissionClosedRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class UnknownJobRestExceptionExceptionMapper extends
            BaseExceptionMapper<UnknownJobRestException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class UnknownTaskExceptionExceptionMapper extends BaseExceptionMapper<UnknownTaskException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class PermissionExceptionExceptionMapper extends BaseExceptionMapper<PermissionException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
    }

    public static class ProActiveRuntimeExceptionExceptionMapper extends
            BaseExceptionMapper<ProActiveRuntimeException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    public static class RuntimeExceptionExceptionMapper extends BaseExceptionMapper<RuntimeException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    public static class IllegalArgumentExceptionMapper extends BaseExceptionMapper<IllegalArgumentException> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    public static class ThrowableExceptionMapper extends BaseExceptionMapper<Throwable> {
        @Override
        protected int getErrorCode() {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }
}
