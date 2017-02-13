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
package org.ow2.proactive_grid_cloud_portal.cli.utils;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;


public class HttpResponseWrapper {
    private byte[] buffer = null;

    private int statusCode = -1;

    public HttpResponseWrapper(HttpResponse response) throws CLIException {
        statusCode = response.getStatusLine().getStatusCode();
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();
            if (inputStream != null) {
                buffer = IOUtils.toByteArray(inputStream);
            }
        } catch (IllegalStateException e) {
            throw new CLIException(REASON_OTHER, e);
        } catch (IOException e) {
            throw new CLIException(REASON_IO_ERROR, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getContent() {
        return buffer;
    }
}
