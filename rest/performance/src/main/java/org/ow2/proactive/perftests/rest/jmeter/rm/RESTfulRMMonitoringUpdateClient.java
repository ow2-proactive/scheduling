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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.ow2.proactive.perftests.rest.json.rm.NodeEventView;
import org.ow2.proactive.perftests.rest.json.rm.RmStateView;


/**
 * Updates the monitoring host machines and nodes lists periodically.
 * 
 */
public class RESTfulRMMonitoringUpdateClient extends BaseRESTfulRMClient {

    private static final String PARAM_USE_DEFAULT_JMX_URL = "useDefaultJmxUrl";
    private boolean useDefaultJmxUrl = false;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();
        defaultParameters.addArgument(PARAM_USE_DEFAULT_JMX_URL, "${useDefaultJmxUrl}");
        return defaultParameters;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);
        useDefaultJmxUrl = Boolean.parseBoolean(context.getParameter(PARAM_USE_DEFAULT_JMX_URL));
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        setTimestamp();
        SampleResult rmStateResult = fetchRmState();
        if (rmStateResult.isSuccessful()) {
            RmStateView rmState = getObjectMapper().readValue(rmStateResult.getResponseData(),
                    RmStateView.class);
            NodeEventView[] nodesEvents = rmState.getNodesEvents();
            if (nodesEvents != null) {
                setNodeInfo(nodesEvents);
            }
        }
        waitForNextCycle(getTimestamp(), context.getIntParameter(PROP_CLIENT_REFRESH_TIME));
        return rmStateResult;
    }

    private SampleResult fetchRmState() {
        String resourceUrl = (new StringBuilder(getConnection().getUrl())).append("/rm/monitoring")
                .toString();
        return getResource(getClientSession(), "rm-state", resourceUrl);
    }

    private void setNodeInfo(NodeEventView[] nodes) throws Exception {
        Map<String, BaseRESTfulRMClient.Host> hostMap = new HashMap<String, BaseRESTfulRMClient.Host>();
        List<Node> nodeList = new ArrayList<BaseRESTfulRMClient.Node>();
        for (NodeEventView nodeView : nodes) {
            Node node = new Node(nodeView.getHostName(), nodeView.getNodeUrl(), nodeJmxUrl(nodeView));
            nodeList.add(node);
            if (hostMap.get(nodeView.getHostName()) == null) {
                hostMap.put(nodeView.getHostName(), new Host(nodeView.getHostName(), node.getJmxUrl()));
            }
        }
        JMeterUtils.setProperty(clientSpecificHostPropKey(), getObjectMapper().writeValueAsString(
                hostMap.values()));
        JMeterUtils.setProperty(clientSpecificNodePropKey(), getObjectMapper().writeValueAsString(nodeList));
    }

    private String nodeJmxUrl(NodeEventView nodeView) {
        return (useDefaultJmxUrl) ? nodeView.getDefaultJMXUrl() : nodeView.getProactiveJMXUrl();
    }

}
