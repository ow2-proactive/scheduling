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
package org.ow2.proactive.perftests.rest.jmeter.rm;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive.perftests.rest.utils.HttpUtility.STATUS_OK;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.perftests.rest.utils.HttpResponseWrapper;
import org.ow2.proactive.perftests.rest.utils.HttpUtility;

/**
 * RESTfulRMMonitoringSetupClient is run only once during test execution. It
 * retrieves a session identifier and stores it as JMeter property. A session is
 * shared between threads which has the same thread count (thread number).
 * Therefore threads with same thread count (e.g. thread-1 of thread-group-1,
 * thread-1 of thread-group-2 .. etc) constitute the logical thread group which
 * simulates RM Portal activities.
 * 
 */
public class RESTfulRMMonitoringSetupClient extends BaseRESTfulRMClient {

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context)
            throws Throwable {
        SampleResult result = new SampleResult();
        HttpPost request = new HttpPost(getConnection().getUrl()
                + "/rm/login");
        StringEntity entity = new StringEntity((new StringBuilder())
                .append("username=").append(getConnection().getLogin())
                .append("&password=").append(getConnection().getPassword())
                .toString(), APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponseWrapper response = null;
        boolean successful = false;
        try {
            result.sampleStart();
            response = HttpUtility.execute(null, request);
            result.sampleEnd();
        } catch (IOException ioe) {
            logError(String.format("%s: An error occurred while login:", Thread
                    .currentThread().toString()), ioe);
        }

        if (response != null) {
            if (STATUS_OK == response.getStatusCode()) {
                successful = true;
                setClientSession(new String(response.getContents()));
            } else {
                logError(String.format(
                        "%s: An error occurred while login: %n%s", Thread
                                .currentThread().toString(), new String(
                                response.getContents())));
            }
        }
        result.setSuccessful(successful);
        return result;
    }
}
