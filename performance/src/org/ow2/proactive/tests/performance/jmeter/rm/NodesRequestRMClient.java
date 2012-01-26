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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;


public class NodesRequestRMClient extends BaseJMeterRMClient {

    public static final String PARAM_REQUESTED_NODES_NUMBER = "requestedNodesNumber";

    public static final String PARAM_USE_SELECTION_SCRIPT = "useSelectionScript";

    public static final String PARAM_SELECTION_SCRIPT_ALWAYS_FALSE = "selectionScriptAlwaysFalse";

    public static final String PARAM_SELECTION_SCRIPT_DYNAMIC_CONTENT = "selectionScriptDynamicContent";

    public static final String PARAM_SELECTION_SCRIPT_TYPE_DYNAMIC = "selectionScriptTypeDynamic";

    public static final String PARAM_NODE_REQUEST_TOPOLOGY = "nodeRequestTopology";

    private Random random = new Random();

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_REQUESTED_NODES_NUMBER, "${requestedNodesNumber}");
        args.addArgument(PARAM_USE_SELECTION_SCRIPT, "${useSelectionScript}");
        args.addArgument(PARAM_SELECTION_SCRIPT_ALWAYS_FALSE, "${selectionScriptAlwaysFalse}");
        args.addArgument(PARAM_SELECTION_SCRIPT_DYNAMIC_CONTENT, "${selectionScriptDynamicContent}");
        args.addArgument(PARAM_SELECTION_SCRIPT_TYPE_DYNAMIC, "${selectionScriptTypeDynamic}");
        args.addArgument(PARAM_NODE_REQUEST_TOPOLOGY, "${nodeRequestTopology}");
        return args;
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        int nodesToRequest = context.getIntParameter(PARAM_REQUESTED_NODES_NUMBER);

        SelectionScript selectionScript = getSelectionScript(context);

        TopologyDescriptor topologyDescriptor = getTopologyDescriptor(context);

        System.out.println("Requesting " + nodesToRequest + " nodes, topology: " +
            topologyDescriptor.getClass().getSimpleName() + ", script: " + (selectionScript != null) + " (" +
            Thread.currentThread() + ")");

        ResourceManager rm = getResourceManager();

        SampleResult result = new SampleResult();
        result.sampleStart();

        List<SelectionScript> scripts = new ArrayList<SelectionScript>(1);
        if (selectionScript != null) {
            scripts.add(selectionScript);
        }

        NodeSet nodes = rm.getAtMostNodes(nodesToRequest, topologyDescriptor, scripts, null);
        PAFuture.waitFor(nodes);
        int nodesReceived = nodes.size();

        BooleanWrapper released = rm.releaseNodes(nodes);
        PAFuture.waitFor(released);

        result.setSuccessful(true);
        result.setResponseMessage(String.format("Requested %d nodes, received %d nodes", nodesToRequest,
                nodesReceived));
        result.sampleEnd();

        return result;
    }

    private SelectionScript getSelectionScript(JavaSamplerContext context) throws Exception {
        boolean useSelectionScript = getBooleanParameter(context, PARAM_USE_SELECTION_SCRIPT);
        if (useSelectionScript) {
            boolean selectionScriptAlwaysFalse = getBooleanParameter(context,
                    PARAM_SELECTION_SCRIPT_ALWAYS_FALSE);
            boolean returnValue = !selectionScriptAlwaysFalse;

            boolean dynamicContent = getBooleanParameter(context, PARAM_SELECTION_SCRIPT_DYNAMIC_CONTENT);
            boolean dynamicScriptType = getBooleanParameter(context, PARAM_SELECTION_SCRIPT_TYPE_DYNAMIC);

            return createScript(returnValue, dynamicContent, dynamicScriptType);
        } else {
            return null;
        }
    }

    private SelectionScript createScript(boolean returnValue, boolean dynamicContent, boolean dynamicScript)
            throws Exception {
        StringBuilder scriptText = new StringBuilder();
        if (dynamicContent) {
            scriptText.append(String.format("// dummy comment %d, %d\n", System.currentTimeMillis(), random
                    .nextLong()));
        }
        scriptText.append(String.format("selected = %s;", String.valueOf(returnValue)));
        return new SelectionScript(scriptText.toString(), "JavaScript", dynamicScript);
    }

    private TopologyDescriptor getTopologyDescriptor(JavaSamplerContext context) {
        if (!context.containsParameter(PARAM_NODE_REQUEST_TOPOLOGY)) {
            return TopologyDescriptor.ARBITRARY;
        }
        String topology = context.getParameter(PARAM_NODE_REQUEST_TOPOLOGY);
        if (topology == null || topology.isEmpty()) {
            return TopologyDescriptor.ARBITRARY;
        }

        if (topology.equals("ARBITRARY")) {
            return TopologyDescriptor.ARBITRARY;
        } else if (topology.equals("BEST_PROXIMITY")) {
            return TopologyDescriptor.BEST_PROXIMITY;
        } else if (topology.equals("DIFFERENT_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE;
        } else if (topology.equals("MULTIPLE_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE;
        } else if (topology.equals("SINGLE_HOST")) {
            return TopologyDescriptor.SINGLE_HOST;
        } else if (topology.equals("SINGLE_HOST_EXCLUSIVE")) {
            return TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        } else {
            throw new IllegalArgumentException("Invalid topology parameter: " + topology);
        }
    }

}
