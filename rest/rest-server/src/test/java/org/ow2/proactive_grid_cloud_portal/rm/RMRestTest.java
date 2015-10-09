/*
 * ################################################################
 *
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.rm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.ProxyFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RMRestTest extends RestTestServer {

    private static final double EXPECTED_RRD_VALUE = 1.042;

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new RMRest());
    }

    // PORTAL-286
    @Test
    public void testStatsHistory_Locale_Fr() throws Exception {
        Locale.setDefault(Locale.FRANCE);

        JSONObject jsonObject = callGetStatHistory();

        assertEquals(EXPECTED_RRD_VALUE, (Double) ((JSONArray) jsonObject.get("AverageActivity")).get(0),
                0.001);

    }

    private JSONObject callGetStatHistory() throws Exception {
        RMProxyUserInterface rmMock = mock(RMProxyUserInterface.class);
        String sessionId = SharedSessionStoreTestUtils.createValidSession(rmMock);

        AttributeList value = new AttributeList(Collections.singletonList(new Attribute("test", createRrdDb()
                .getBytes())));
        when(rmMock.getMBeanAttributes(Matchers.<ObjectName> any(), Matchers.<String[]> any())).thenReturn(
                value);
        RMRestInterface client = ProxyFactory.create(RMRestInterface.class, "http://localhost:" + port + "/");

        String statHistory = client.getStatHistory(sessionId, "hhhhh");
        return (JSONObject) new JSONParser().parse(statHistory);
    }

    private RrdDb createRrdDb() throws IOException {
        final long start = (System.currentTimeMillis() - 10000) / 1000;
        final long end = System.currentTimeMillis() / 1000;

        RrdDef rrdDef = new RrdDef("testDB", start - 1, 300);
        for (String dataSource : RMRest.dataSources) {
            rrdDef.addDatasource(dataSource, DsType.GAUGE, 600, 0, Double.NaN);
        }
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 150);
        RrdDb rrdDb = new RrdDb(rrdDef, org.rrd4j.core.RrdBackendFactory.getFactory("MEMORY"));
        Sample sample = rrdDb.createSample();

        long time = start;
        while (time <= end + 172800L) {
            sample.setTime(time);
            for (String dataSource : RMRest.dataSources) {
                sample.setValue(dataSource, 1.042);
            }
            sample.update();
            time += new Random().nextDouble() * 300 + 1;
        }
        return rrdDb;
    }

    @Test
    public void testShutdown_NoPreemptParameter() throws Exception {
        RMProxyUserInterface rm = mock(RMProxyUserInterface.class);
        when(rm.shutdown(false)).thenReturn(new BooleanWrapper(true));

        String sessionId = SharedSessionStoreTestUtils.createValidSession(rm);
        HttpResponse response = callHttpGetMethod("shutdown", sessionId);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        verify(rm).shutdown(false);
    }

    @Test
    public void testShutdown_PreemptParameter() throws Exception {
        RMProxyUserInterface rm = mock(RMProxyUserInterface.class);
        when(rm.shutdown(true)).thenReturn(new BooleanWrapper(true));

        String sessionId = SharedSessionStoreTestUtils.createValidSession(rm);

        HttpResponse response = callHttpGetMethod("shutdown?preempt=true", sessionId);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        verify(rm).shutdown(true);
    }

    // PORTAL-326
    @Test
    public void testAddNodeOverloading() throws Exception {
        RMProxyUserInterface rm = mock(RMProxyUserInterface.class);
        String sessionId = SharedSessionStoreTestUtils.createValidSession(rm);
        when(rm.addNode(anyString())).thenReturn(new BooleanWrapper(true));

        List<NameValuePair> firstCall = Collections.<NameValuePair> singletonList(new BasicNameValuePair(
            "nodeurl", "url"));
        callHttpPostMethod("node", sessionId, firstCall);

        verify(rm).addNode("url");

        reset(rm);
        when(rm.addNode(anyString(), anyString())).thenReturn(new BooleanWrapper(true));

        List<NameValuePair> secondCall = new ArrayList<>();
        secondCall.add(new BasicNameValuePair("nodeurl", "urlwithnsname"));
        secondCall.add(new BasicNameValuePair("nodesource", "ns"));
        callHttpPostMethod("node", sessionId, secondCall);

        verify(rm).addNode("urlwithnsname", "ns");
    }

    private HttpResponse callHttpGetMethod(String httpMethod, String sessionId) throws IOException {
        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/rm/" + httpMethod);
        httpGet.setHeader("sessionid", sessionId);
        return new DefaultHttpClient().execute(httpGet);
    }

    private HttpResponse callHttpPostMethod(String httpMethod, String sessionId,
            List<NameValuePair> postParameters) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" + port + "/rm/" + httpMethod);
        httpPost.setHeader("sessionid", sessionId);
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        return new DefaultHttpClient().execute(httpPost);
    }

}
