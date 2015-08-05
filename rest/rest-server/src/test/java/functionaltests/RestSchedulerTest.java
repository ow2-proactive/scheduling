/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class RestSchedulerTest extends AbstractRestFuncTestCase {

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void before() throws Exception {
        Scheduler scheduler = getScheduler();
        if (!SchedulerStatus.STARTED.equals(scheduler.getStatus())) {
            scheduler.start();
        }
    }

    @Test
    public void testStartScheduler() throws Exception {
        Scheduler scheduler = getScheduler();
        scheduler.stop();
        String resourceUrl = getResourceUrl("start");
        HttpPut httpPut = new HttpPut(resourceUrl);
        setSessionHeader(httpPut);
        HttpResponse response = executeUriRequest(httpPut);
        assertHttpStatusOK(response);
        assertTrue(Boolean.valueOf(getContent(response)));
        assertTrue(SchedulerStatus.STARTED.equals(scheduler.getStatus()));
    }

    @Test
    public void testGetStatistics() throws Exception {
        String resourceUrl = getResourceUrl("stats");
        HttpGet httpGet = new HttpGet(resourceUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        assertEquals("Started", jsonObject.get("Status").toString());
    }
}
