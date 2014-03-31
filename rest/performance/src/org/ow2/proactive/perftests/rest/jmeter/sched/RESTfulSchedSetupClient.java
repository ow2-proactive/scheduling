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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.perftests.rest.jmeter.sched;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive.perftests.rest.utils.HttpUtility.STATUS_OK;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.perftests.rest.utils.HttpResponseWrapper;
import org.ow2.proactive.perftests.rest.utils.HttpUtility;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;

/**
 * RESTfulSchedSetClient is run only once at the start of test execution. It
 * retrieves a session identifier and stores as a JMeter property. It is stored
 * using a key which contains 'thread count' as the prefix. A session identifier
 * is shared between threads with same thread count (one from each thread
 * group). This group of threads with same thread count number formulates the
 * logical thread group which simulates the use of Scheduler Portal.
 */
public class RESTfulSchedSetupClient extends BaseRESTfulSchedClient {
    @Override
    protected SampleResult doRunTest(JavaSamplerContext context)
            throws Throwable {
        SampleResult result = new SampleResult();
        String resourceUrl = (new StringBuffer(getConnection().getUrl()))
                .append("/login").toString();
        HttpPost request = new HttpPost(resourceUrl);
        StringEntity entity = new StringEntity((new StringBuilder())
                .append("username=").append(getConnection().login())
                .append("&password=").append(getConnection().password())
                .toString(), APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponseWrapper response = null;
        boolean success = false;
        try {
            result.sampleStart();
            response = HttpUtility.execute(null, request);
        } catch (IOException ioe) {
            throw new TestExecutionException(String.format(
                    "%s: An error occurred while retrieving the session id:",
                    Thread.currentThread().toString()), ioe);
        } finally {
            result.sampleEnd();
        }
        if (response != null) {
            byte[] data = response.getContents();
            if (STATUS_OK == response.getStatusCode()) {
                result.setResponseData(data);
                setClientSession(new String(data));
                success = true;
            } else {
                throw new TestExecutionException(
                        String.format(
                                "%s: An error occurred while retrieving session id: %n%s",
                                Thread.currentThread().toString(), new String(
                                        data)));
            }
        }
        result.setSuccessful(success);
        return result;
    }
}
