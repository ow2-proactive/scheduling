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
package org.ow2.proactive.utils;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;


/**
 * @author ActiveEon Team
 * @since 01/08/2020
 */
public class PCAProxyRuleTest {

    @Test
    public void matchAndApplyWithMessyTargetAndReferer() throws IOException {
        String requestPath = "/lab/workspaces/auto-s/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab";
        String referer = "http://myserver.com:8080/lab/workspaces/auto-s/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab?clone";
        String expected = "/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab/workspaces/auto-s/lab";
        runTest(requestPath, HttpMethod.GET, referer, expected, false);
    }

    @Test
    public void matchAndApplyWithCloudAutomationReferer() throws IOException {
        String requestPath = "/lab/workspaces/";
        String referer = "http://myserver.com:8080/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab?clone";
        String expected = "/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab/workspaces/";
        runTest(requestPath, HttpMethod.GET, referer, expected, true);
    }

    @Test
    public void matchAndApplyWithCloudAutomationRefererPostMethod() throws IOException {
        String requestPath = "/lab/workspaces/";
        String referer = "http://myserver.com:8080/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab?clone";
        String expected = "/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab/workspaces/";
        runTest(requestPath, HttpMethod.POST, referer, expected, false);
    }

    @Test
    public void matchAndApplyWithCloudAutomationRefererAndQuery() throws IOException {
        String requestPath = "/lab/workspaces/?param1=value1&param2=value2";
        String referer = "http://myserver.com:8080/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab?clone";
        String expected = "/cloud-automation-service/services/93/endpoints/jupyterlab-endpoint2/lab/workspaces/?param1=value1&param2=value2";
        runTest(requestPath, HttpMethod.GET, referer, expected, true);
    }

    private void runTest(String requestPath, HttpMethod method, String referer, String expectedTarget,
            boolean expectedRedirection) throws IOException {
        PCAProxyRule rule = new PCAProxyRule();
        Request request = Mockito.mock(Request.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String target = requestPath;
        HttpURI requestUri = new HttpURI(target);
        when(request.getMethod()).thenReturn(method.asString());
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(referer);
        when(request.getUri()).thenReturn(requestUri);
        ArgumentCaptor<String> redirectString = ArgumentCaptor.forClass(String.class);
        when(response.encodeRedirectURL(anyString())).then(returnsFirstArg());
        String newTarget = rule.matchAndApply(target, request, response);
        Assert.assertEquals(expectedTarget, newTarget);
        if (HttpMethod.GET.is(method.asString()) && expectedRedirection) {
            verify(response, times(1)).sendRedirect(redirectString.capture());
            Assert.assertEquals(expectedTarget, redirectString.getValue());
        } else {
            verify(response, times(0)).sendRedirect(redirectString.capture());
        }

    }
}
