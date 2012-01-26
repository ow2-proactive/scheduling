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
package org.ow2.proactive.tests.performance.jmeter.rm;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.tests.performance.jmeter.TestHosts;
import org.ow2.proactive.tests.performance.rm.NodeSourceEventsMonitor;
import org.ow2.proactive.tests.performance.rm.RMTestListener;


public abstract class BaseNodeSourceCreateClient extends BaseJMeterRMClient {

    public static final String PARAM_HOSTS_LIST = "createNodeSourceHosts";

    public static final String PARAM_NODES_PER_HOST = "createNodeSourceNodesPerHost";

    public static final String PARAM_JAVA_PATH = "createNodeSourceJavaPath";

    public static final String PARAM_SCHEDULING_PATH = "createNodeSourceSchedulingPath";

    public static final String PARAM_NODE_JAVA_OPTIONS = "nodeJavaOptions";

    public static final int NODE_DEPLOY_TIMEOUT = 60000;

    public static final int NODE_SOURCE_REMOVE_TIMEOUT = 60000;

    private static final TestHosts testHosts = new TestHosts();

    private NodeSourceEventsMonitor nodesMonitor;

    private RMTestListener listener;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_HOSTS_LIST, "${createNodeSourceHosts}");
        args.addArgument(PARAM_NODES_PER_HOST, "${createNodeSourceNodesPerHost}");
        args.addArgument(PARAM_JAVA_PATH, "${createNodeSourceJavaPath}");
        args.addArgument(PARAM_SCHEDULING_PATH, "${createNodeSourceSchedulingPath}");
        args.addArgument(PARAM_NODE_JAVA_OPTIONS, "${nodeJavaOptions}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);
        testHosts.initializeHostsIfChanged(context.getParameter(PARAM_HOSTS_LIST));

        nodesMonitor = new NodeSourceEventsMonitor();
        listener = RMTestListener.createRMTestListener(nodesMonitor);
        getResourceManager().syncAddEventListener(listener);
    }

    @Override
    protected void doTeardownTest(JavaSamplerContext context) {
        if (listener != null) {
            try {
                getResourceManager().removeEventListener();
                System.out.println("Removed listener");
            } catch (RMException e) {
                throw new RuntimeException("Failed to remove listener: " + e, e);
            }
        }
        super.doTeardownTest(context);
    }

    protected String getNodeJavaOptions(JavaSamplerContext context) {
        String nodeJavaOptions = context.getParameter(PARAM_NODE_JAVA_OPTIONS);
        if (nodeJavaOptions == null) {
            nodeJavaOptions = "";
        }
        return nodeJavaOptions;
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        TestHosts.Host host = testHosts.getHost();
        try {
            int nodesNumber = context.getIntParameter(PARAM_NODES_PER_HOST);
            String javaPath = context.getParameter(PARAM_JAVA_PATH);
            String schedulingPath = context.getParameter(PARAM_SCHEDULING_PATH);

            String nodeSourceName = "TestNodeSource-" + new UniqueID().getCanonString();
            nodesMonitor.clearState(nodeSourceName);

            SampleResult result = new SampleResult();
            result.sampleStart();
            if (!createNodeSource(nodeSourceName, host.getHostName(), nodesNumber, javaPath, schedulingPath,
                    context)) {
                result.setResponseMessage("Failed to create node source");
                result.setSuccessful(false);
                return result;
            }
            try {
                boolean nodesDeployed = nodesMonitor.waitForNodesInitialization(nodesNumber,
                        NODE_DEPLOY_TIMEOUT);
                result.sampleEnd();
                if (!nodesDeployed) {
                    result.setSuccessful(false);
                    result.setResponseMessage("Failed to deploy nodes");
                } else {
                    result.setSuccessful(true);
                }
            } finally {
                BooleanWrapper removedRequest = getResourceManager().removeNodeSource(nodeSourceName, true);
                if (!removedRequest.getBooleanValue()) {
                    result.setSuccessful(false);
                    result.setResponseMessage("Failed to removed node source");
                } else {
                    boolean removedEvent = nodesMonitor.waitForNodeSourceRemoval(NODE_SOURCE_REMOVE_TIMEOUT);
                    if (!removedEvent) {
                        result.setSuccessful(false);
                        result.setResponseMessage("Failed to get NODESOURCE_REMOVED event");
                    }
                }
            }

            return result;
        } finally {
            testHosts.releaseHost(host);
        }
    }

    protected abstract boolean createNodeSource(String nodeSourceName, String hostName, int nodesNumber,
            String javaPath, String schedulingPath, JavaSamplerContext context) throws Exception;

}
