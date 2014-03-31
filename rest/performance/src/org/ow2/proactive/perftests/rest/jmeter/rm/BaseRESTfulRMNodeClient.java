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

import java.util.List;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * BaseRESTfulRMNodeClient is an abstraction of all RESTfulRMNodeClients. It
 * uses the implementations of its two abstract methods provided by its child
 * classes to retrieve some specific information from all known nodes.
 */
public abstract class BaseRESTfulRMNodeClient extends BaseRESTfulRMClient {

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context)
            throws Throwable {
        setTimestamp();
        SampleResult result = new SampleResult();
        result.sampleStart();
        List<Node> monitoringNodeSet = getMonitoringNodeSet();
        if (!monitoringNodeSet.isEmpty()) {
            for (Node node : monitoringNodeSet) {
                String resourceUrl = getQueryUrl(
                        getConnection().getUrl(), node.getJmxUrl());
                result.addSubResult(getResource(getClientSession(), getResourceDescription(),
                        resourceUrl));
            }
        } else {
            result.sampleEnd();
        }
        result.setSuccessful(true);
        waitForNextCycle(getTimestamp(),
                context.getIntParameter(PROP_CLIENT_REFRESH_TIME));
        return result;
    }

    abstract protected String getQueryUrl(String serverUrl, String jmxUrl);

    abstract protected String getResourceDescription();

}
