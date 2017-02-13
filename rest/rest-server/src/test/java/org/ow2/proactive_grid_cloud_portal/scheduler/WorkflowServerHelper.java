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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import functionaltests.AbstractRestFuncTestCase;


@Path("/wsh")
@Produces(APPLICATION_JSON)
public class WorkflowServerHelper {

    private final static URL DEFAULT_JOB_XML = AbstractRestFuncTestCase.class.getResource("config/test-job-with-variables.xml");

    @GET
    @Path("/workflow")
    public String validWorkflow() throws IOException, URISyntaxException {
        return readFile(new File(DEFAULT_JOB_XML.toURI().getPath()));
    }

    @GET
    @Path("/corrupt")
    public String corruptWorkflow() throws IOException {
        return "<<corruptxml";
    }

    private String readFile(File file) throws IOException {
        if (file.exists()) {
            return FileUtils.readFileToString(file);
        }
        throw new IOException(String.format("Could not read file %s", file.getAbsolutePath()));
    }

}
