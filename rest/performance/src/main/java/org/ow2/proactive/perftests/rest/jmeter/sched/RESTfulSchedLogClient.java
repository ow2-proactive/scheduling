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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;


/**
 * Retrieves the live-log of each job from available job ids.
 */
public class RESTfulSchedLogClient extends BaseRESTfulSchedClient {

    private static final String PARAM_LOG_CLIENT_REFRESH_TIME = "logClientRefreshTime";

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();
        defaultParameters.addArgument(PARAM_LOG_CLIENT_REFRESH_TIME, "${logClientRefreshTime}");
        return defaultParameters;
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        setTimestamp();
        String[] jobIdSet = getAvailableJobIds();
        SampleResult result = new SampleResult();
        result.sampleStart();
        if (!isEmpty(jobIdSet)) {
            for (String jobId : jobIdSet) {
                result.addSubResult(fetchLiveLogs(jobId));
            }
        } else {
            result.sampleEnd();
            logInfo(String.format("%s: Job ids not available.", Thread.currentThread().toString()));
        }
        result.setSuccessful(true);
        waitForNextCycle(getTimestamp(), context.getIntParameter(PARAM_LOG_CLIENT_REFRESH_TIME));
        return result;
    }

    private SampleResult fetchLiveLogs(String jobId) {
        String resourceUrl = (new StringBuilder(getConnection().getUrl())).append("/jobs/").append(jobId)
                .append("/livelog").toString();
        return getResource(getClientSession(), String.format("job('%s')'s livelog", jobId), resourceUrl);
    }

}
