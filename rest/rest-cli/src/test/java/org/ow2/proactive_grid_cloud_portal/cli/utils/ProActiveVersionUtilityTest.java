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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.IOException;
import java.nio.charset.Charset;

import com.eclipsesource.json.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProActiveVersionUtilityTest {

    private static final String VALID_RESPONSE_PAYLOAD =
            "{\n\"rm\": \"X.Y.Z\",\n\"rest\": \"X.Y.Z\"\n}";

    @Test
    public void testProActiveClientVersionNotSet() {
        // proactive client version is defined in the manifest of the generated
        // JAR files and is thus available when these JARs are in the classpath
        // and used
        assertEquals(
                ProActiveVersionUtility.VERSION_UNDEFINED,
                ProActiveVersionUtility.getProActiveClientVersion());
    }

    @Test
    public void testGetProActiveServerVersionStatusCode199() throws IOException {
        testGetProActiveServerVersionValidPayload(199, true);
    }

    @Test
    public void testGetProActiveServerVersionStatusCode200() throws IOException {
        testGetProActiveServerVersionValidPayload(200, false);
    }

    @Test
    public void testGetProActiveServerVersionStatusCode299() throws IOException {
        testGetProActiveServerVersionValidPayload(299, false);
    }

    @Test
    public void testGetProActiveServerVersionStatusCode300() throws IOException {
        testGetProActiveServerVersionValidPayload(300, true);
    }

    @Test(expected = ParseException.class)
    public void testGetProActiveServerVersionStatusCode200InvalidPayload() throws IOException {
        testGetProActiveServerVersion(200, "Abracadabra", false);
    }

    private void testGetProActiveServerVersionValidPayload(int statusCode,
            boolean isNullExpected) throws IOException {
        testGetProActiveServerVersion(statusCode, VALID_RESPONSE_PAYLOAD, isNullExpected);
    }

    private void testGetProActiveServerVersion(int statusCode, String payload,
            boolean isNullExpected) throws IOException {
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(httpResponse.getEntity()).thenReturn(new StringEntity(payload, Charset.defaultCharset()));

        String serverVersion = ProActiveVersionUtility.handleResponse(httpResponse);

        verify(httpResponse).getStatusLine();

        if (isNullExpected) {
            assertNull(serverVersion);
        } else {
            assertEquals("X.Y.Z", serverVersion);
            verify(httpResponse).getEntity();
        }
    }

}
