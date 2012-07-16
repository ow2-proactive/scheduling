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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.FORBIDDEN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus;
import org.ow2.proactive_grid_cloud_portal.cli.RestCliException;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.json.ErrorView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.ArrayFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.utils.ObjectUtils;
import org.ow2.proactive_grid_cloud_portal.cli.utils.Tools;

public abstract class AbstractCommand implements Command {

    public AbstractCommand() {
    }

    protected static String string(HttpResponse response) throws Exception {
        return EntityUtils.toString(response.getEntity());
    }

    protected static String string(ArrayFormatter oaf) {
        return Tools.getStringAsArray(oaf);
    }

    protected static String formattedDate(long time) {
        return Tools.getFormattedDate(time);
    }

    protected static String formattedElapsedTime(long time) {
        return Tools.getElapsedTime(time);
    }

    protected static String formattedDuration(long start, long end) {
        return Tools.getFormattedDuration(start, end);
    }

    protected static Object object(byte[] bytes) throws Exception {
        if (bytes == null) {
            return "[NULL]";
        }
        return ObjectUtils.object(bytes);
    }

    protected static int statusCode(ResponseStatus status) {
        return status.statusCode();
    }

    protected static int statusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    protected static String md5Checksum(File file) throws Exception {
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    protected static void write(File file, String content) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, content);
        file.setReadable(true, true);
        file.setWritable(true, true);
    }

    protected static String read(File file) throws IOException {
        return FileUtils.readFileToString(file);
    }

    protected static byte[] byteArray(File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    protected ApplicationContext applicationContext() {
        return ApplicationContext.instance();
    }

    protected <T> T readValue(HttpResponse response, Class<T> valueType)
            throws Exception {
        return applicationContext().getObjectMapper().readValue(
                response.getEntity().getContent(), valueType);
    }

    protected <T> T readValue(HttpResponse response, TypeReference<T> valueType)
            throws Exception {
        return applicationContext().getObjectMapper().readValue(
                response.getEntity().getContent(), valueType);
    }

    protected String resourceUrl(String resource) {
        return applicationContext().getSchedulerUrl() + "/scheduler/"
                + resource;
    }

    protected void writeLine(String format, Object... args) throws Exception {
        applicationContext().getDevice().writeLine(format, args);
    }

    protected String readLine(String format, Object... args) throws IOException {
        return applicationContext().getDevice().readLine(format, args);
    }

    protected Writer writer() {
        return applicationContext().getDevice().getWriter();
    }

    protected char[] readPassword(String format, Object... args)
            throws IOException {
        return applicationContext().getDevice().readPassword(format, args);
    }

    protected HttpResponse execute(HttpUriRequest request) throws Exception {
        if (request.getURI().getScheme().equals("https")) {

        }
        return applicationContext().executeClient(request);
    }

    protected void handleError(String errorMessage, HttpResponse response)
            throws Exception {
        String responseContent = string(response);
        ErrorView errorView = null;
        try {
            errorView = applicationContext().getObjectMapper().readValue(
                    responseContent.getBytes(), ErrorView.class);
        } catch (Throwable error) {
            // ignore
        }
        if (errorView != null) {
            writeError(errorMessage, errorView);
        } else {
            writeError(errorMessage, responseContent);
        }
    }

    private void writeError(String errorMsg, String responseContent)
            throws Exception {
        writeLine(errorMsg);

        String errorMessage = null, errorCode = null;
        BufferedReader reader = new BufferedReader(new StringReader(
                responseContent));

        String line = reader.readLine();
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

        if (errorCode != null) {
            writeLine("%s %s", "HTTP Error Code:", errorCode);
        }

        if (errorMessage != null) {
            writeLine("%s %s", "Error Message:", errorMessage);
        }

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("stackTrace:")) {
                while ((line = reader.readLine()) != null) {
                    writeLine(line);
                }
                break;
            }
        }

        if (errorCode == null && errorMessage == null) {
            writeLine("%s%n%s", "Error Message:", responseContent);
        }
    }

    private void writeError(String errorMessage, ErrorView error)
            throws Exception {
        if (statusCode(FORBIDDEN) == error.getHttpErrorCode()) {
            // this exception would be handled at an upper level ..
            throw new RestCliException(error.getHttpErrorCode(),
                    error.getErrorMessage());
        }
        writeLine(errorMessage);
        writeLine("%s %s", "HTTP Error Code:", error.getHttpErrorCode());
        writeLine("%s %s", "Error Message:", error.getErrorMessage());
        writeLine("%s%n%s", "Stack Trace:", error.getStackTrace());
    }
}
