/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.NotFoundException;

import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class RestRuntimeExceptionMappingTest extends RestTestServer {

    @BeforeClass
    public static void setUpRest() throws Exception {
        new RestRuntime().addExceptionMappers(ResteasyProviderFactory.getInstance());
    }

    @Test
    public void unknown_path_404_exception() throws Exception {
        GetMethod unknownPath = new GetMethod("http://localhost:" + port + "/" +
            "rest/a_path_that_does_not_exist");

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
