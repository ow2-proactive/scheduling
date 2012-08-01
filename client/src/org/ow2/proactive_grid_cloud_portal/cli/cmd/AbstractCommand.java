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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus;
import org.ow2.proactive_grid_cloud_portal.cli.json.ErrorView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public abstract class AbstractCommand implements Command {

    public AbstractCommand() {
    }

    protected int statusCode(HttpResponseStatus status) {
        return status.statusCode();
    }

    protected int statusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    protected ApplicationContext context() {
        return ApplicationContext.instance();
    }

    protected <T> T readValue(HttpResponse response, Class<T> valueType) {
        try {
            return context().getObjectMapper().readValue(
                    response.getEntity().getContent(), valueType);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected <T> T readValue(HttpResponse response, TypeReference<T> valueType) {
        try {
            return context().getObjectMapper().readValue(
                    response.getEntity().getContent(), valueType);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected String resourceUrl(String resource) {
        return context().getRestServerUrl() + "/" + context().getResourceType()
                + "/" + resource;
    }

    protected void writeLine(String format, Object... args) {
        try {
            context().getDevice().writeLine(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected String readLine(String format, Object... args) {
        try {
            return context().getDevice().readLine(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected Writer writer() {
        return context().getDevice().getWriter();
    }

    protected char[] readPassword(String format, Object... args) {
        try {
            return context().getDevice().readPassword(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected HttpResponse execute(HttpUriRequest request) {
        return context().executeClient(request);
    }

    protected void handleError(String errorMessage, HttpResponse response) {
        String responseContent = StringUtility.string(response);
        ErrorView errorView = null;
        try {
            errorView = context().getObjectMapper().readValue(
                    responseContent.getBytes(), ErrorView.class);
        } catch (Throwable error) {
            // if an ErrorView object can't be built from the response. Hence
            // process the response as a string
        }
        if (errorView != null) {
            writeError(errorMessage, errorView);
        } else {
            writeError(errorMessage, responseContent);
        }
    }

    private void writeError(String errorMsg, String responseContent) {
        writeLine(errorMsg);

        String errorMessage = null, errorCode = null;
        BufferedReader reader = new BufferedReader(new StringReader(
                responseContent));

        String line = null;
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("errorMessage:")) {
                    errorMessage = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("httpErrorCode:")) {
                    errorCode = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }

        if (errorCode != null) {
            writeLine("%s %s", "HTTP Error Code:", errorCode);
        }

        if (errorMessage != null) {
            writeLine("%s %s", "Error Message:", errorMessage);
        }

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("stackTrace:")) {
                    while ((line = reader.readLine()) != null) {
                        writeLine(line);
                    }
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }

        if (errorCode == null && errorMessage == null) {
            writeLine("%s%n%s", "Error Message:", responseContent);
        }
    }

    private void writeError(String errorMessage, ErrorView error) {
        if (statusCode(FORBIDDEN) == error.getHttpErrorCode()) {
            // this exception would be handled at an upper level ..
            throw new CLIException(REASON_UNAUTHORIZED_ACCESS,
                    error.getErrorMessage());
        }
        writeLine(errorMessage);
        writeLine("%s %s", "HTTP Error Code:", error.getHttpErrorCode());
        writeLine("%s %s", "Error Message:", error.getErrorMessage());
        writeLine("%s%n%s", "Stack Trace:", error.getStackTrace());
    }
}
