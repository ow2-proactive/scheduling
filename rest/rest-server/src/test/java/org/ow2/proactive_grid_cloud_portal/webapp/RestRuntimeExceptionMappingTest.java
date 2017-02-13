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
package org.ow2.proactive_grid_cloud_portal.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.NotFoundException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;


public class RestRuntimeExceptionMappingTest extends RestTestServer {

    @BeforeClass
    public static void setUpRest() throws Exception {
        new RestRuntime().addExceptionMappers(ResteasyProviderFactory.getInstance());
    }

    @Test
    public void unknown_path_404_exception() throws Exception {
        GetMethod unknownPath = new GetMethod("http://localhost:" + port + "/" + "rest/a_path_that_does_not_exist");

        new HttpClient().executeMethod(unknownPath);
        ExceptionToJson exception = readResponse(unknownPath);

        assertEquals(404, exception.getHttpErrorCode());
        assertNotNull(exception.getErrorMessage());
        assertNotNull(exception.getException());
        assertEquals(NotFoundException.class.getName(), exception.getExceptionClass());
        assertNotNull(exception.getStackTrace());
    }

    private ExceptionToJson readResponse(GetMethod httpGetMethod) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InputStream is = httpGetMethod.getResponseBodyAsStream();
        return jsonMapper.readValue(is, ExceptionToJson.class);
    }
}
