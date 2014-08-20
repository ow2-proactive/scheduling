/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_UNAUTHORIZED_ACCESS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.FORBIDDEN;
import static org.ow2.proactive_grid_cloud_portal.cli.utils.ExceptionUtility.debugMode;
import static org.ow2.proactive_grid_cloud_portal.cli.utils.ExceptionUtility.stackTraceAsString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus;
import org.ow2.proactive_grid_cloud_portal.cli.json.ErrorView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.ExceptionUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;


public abstract class AbstractCommand implements Command {

    protected int statusCode(HttpResponseStatus status) {
        return status.statusCode();
    }

    protected int statusCode(HttpResponseWrapper response) {
        return response.getStatusCode();
    }

    protected <T> T readValue(HttpResponseWrapper response, Class<T> valueType,
            ApplicationContext currentContext) {
        try {
            return currentContext.getObjectMapper().readValue(response.getContent(), valueType);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected <T> T readValue(HttpResponseWrapper response, TypeReference<T> valueType,
            ApplicationContext currentContext) {
        try {
            return currentContext.getObjectMapper().readValue(response.getContent(), valueType);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }

    }

    protected void writeLine(ApplicationContext currentContext, String format, Object... args) {
        if (!currentContext.isSilent()) {
            try {
                currentContext.getDevice().writeLine(format, args);
            } catch (IOException ioe) {
                throw new CLIException(REASON_IO_ERROR, ioe);
            }
        }
    }

    protected String readLine(ApplicationContext currentContext, String format, Object... args) {
        try {
            return currentContext.getDevice().readLine(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected char[] readPassword(ApplicationContext currentContext, String format, Object... args) {
        try {
            return currentContext.getDevice().readPassword(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected HttpResponseWrapper execute(HttpUriRequest request, ApplicationContext currentContext) {
        String sessionId = currentContext.getSessionId();
        if (sessionId != null) {
            request.setHeader("sessionid", sessionId);
        }
        HttpClient client = HttpUtility.threadSafeClient();
        try {
            if ("https".equals(request.getURI().getScheme()) && currentContext.canInsecureAccess()) {
                HttpUtility.setInsecureAccess(client);
            }
            HttpResponse response = client.execute(request);
            return new HttpResponseWrapper(response);

        } catch (Exception e) {
            throw new CLIException(CLIException.REASON_OTHER, e.getMessage(), e);
        } finally {
            ((HttpRequestBase) request).releaseConnection();
        }
    }


    @SuppressWarnings("unchecked")
    protected void handleError(String errorMessage, HttpResponseWrapper response,
            ApplicationContext currentContext) {
        String responseContent = StringUtility.responseAsString(response);
        Stack resultStack = resultStack(currentContext);
        ErrorView errorView = errorView(responseContent, currentContext);
        if (errorView != null) {
            resultStack.push(errorView);
        } else {
            resultStack.push(responseContent);
        }
        if (errorView != null) {
            writeError(errorMessage, errorView, currentContext);
        } else {
            writeError(errorMessage, responseContent, currentContext);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleError(String errorMessage, Exception error, ApplicationContext currentContext) {
        Stack resultStack = resultStack(currentContext);
        resultStack.push(error);

        if (error instanceof NotConnectedRestException) {
            throw new CLIException(REASON_UNAUTHORIZED_ACCESS, errorMessage, error);
        }

        writeLine(currentContext, errorMessage);
        Throwable cause = error.getCause();

        writeLine(currentContext, "Error Message: %s", (cause == null) ? error.getMessage() : cause
                .getMessage());

        if (debugMode(currentContext)) {
            writeLine(currentContext, "Stack Track: %s",
                    stackTraceAsString((cause == null) ? error : cause));
        }
    }

    protected CLIException buildCLIException(int reason, HttpResponseWrapper response,
            ApplicationContext currentContext) {
        String responseContent = StringUtility.responseAsString(response);
        ErrorView errorView = errorView(responseContent, currentContext);
        if (errorView != null) {
            throw new CLIException(reason, errorView.getErrorMessage(), errorView.getStackTrace());
        } else {
            HttpErrorView httpErrorView = errorView(responseContent);
            throw new CLIException(reason, httpErrorView.errorMessage, httpErrorView.stackTrace);
        }
    }

    private void writeError(String errorMsg, String responseContent, ApplicationContext currentContext) {
        writeLine(currentContext, errorMsg);

        HttpErrorView errorView = errorView(responseContent);

        if (errorView.errorCode != null) {
            writeLine(currentContext, "HTTP Error Code: %s", errorView.errorCode);
        }

        if (errorView.errorMessage != null) {
            writeLine(currentContext, "Error Message: %s", errorView.errorMessage);
        }

        if (errorView.errorCode == null && errorView.errorMessage == null) {
            writeLine(currentContext, "Error Message:%n%s", responseContent);
        }

        if (debugMode(currentContext)) {
            writeLine(currentContext, "Stack Trace:%s", errorView.stackTrace);
        }
    }

    public Stack resultStack(ApplicationContext currentContext) {
        return currentContext.resultStack();
    }

    protected ErrorView errorView(String responseContent, ApplicationContext currentContext) {
        try {
            return currentContext.getObjectMapper().readValue(responseContent.getBytes(), ErrorView.class);
        } catch (Exception ignore) {
            return null;
        }
    }

    protected HttpErrorView errorView(String responseContent) {
        try {
            HttpErrorView errorView = new HttpErrorView();
            BufferedReader reader = new BufferedReader(new StringReader(responseContent));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("errorMessage:")) {
                    errorView.errorMessage = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("httpErrorCode:")) {
                    errorView.errorCode = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("stackTrace:")) {
                    StringBuilder buffer = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    errorView.stackTrace = buffer.toString();
                    break;
                }
            }

            return errorView;

        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    private void writeError(String errorMessage, ErrorView error, ApplicationContext currentContext) {
        if (statusCode(FORBIDDEN) == error.getHttpErrorCode()) {
            // this exception would be handled at an upper level ..
            throw new CLIException(REASON_UNAUTHORIZED_ACCESS, error.getErrorMessage());
        }
        writeLine(currentContext, errorMessage);
        writeLine(currentContext, "%s %s", "HTTP Error Code:", error.getHttpErrorCode());
        writeLine(currentContext, "%s %s", "Error Message:", error.getErrorMessage());
        if (debugMode(currentContext)) {
            writeLine(currentContext, "%s%n%s", "Stack Trace:", error.getStackTrace());
        }
    }

    private class HttpErrorView {
        private String errorCode;
        private String errorMessage;
        private String stackTrace;
    }
}
