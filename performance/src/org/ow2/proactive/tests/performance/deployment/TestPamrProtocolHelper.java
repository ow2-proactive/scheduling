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
package org.ow2.proactive.tests.performance.deployment;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;


public class TestPamrProtocolHelper extends TestProtocolHelper {

    public static final String PAMR_EXISTING_ROUTER_HOST = "test.deploy.pamr.existingRouterHost";

    public static final String PAMR_EXISTING_ROUTER_PORT = "test.deploy.pamr.existingRouterPort";

    protected final PamrReservedId serverPamrId;

    private final String pamrHostName;

    private final Integer pamrPort;

    protected TestPamrProtocolHelper(String javaPath, SchedulingFolder schedulingFolder,
            InetAddress serverHost, String serverReservedId) {
        super(javaPath, schedulingFolder, serverHost);
        if (serverReservedId == null) {
            throw new TestExecutionException(
                "serverReservedId required for execution with pamr not specified");
        }
        String splitted[] = serverReservedId.split(":");
        if (splitted.length != 2 || splitted[0].isEmpty() || splitted[1].isEmpty()) {
            throw new TestExecutionException("Invalid format of serverReseredId: " + serverReservedId);
        }
        serverPamrId = new PamrReservedId(splitted[0], splitted[1]);

        pamrHostName = System.getProperty(PAMR_EXISTING_ROUTER_HOST);
        if (pamrHostName == null || pamrHostName.isEmpty()) {
            throw new TestExecutionException("Property '" + PAMR_EXISTING_ROUTER_HOST +
                "' required to run with exiting PAMR router not specified");
        }
        String pamrPortStr = System.getProperty(PAMR_EXISTING_ROUTER_PORT);
        if (pamrPortStr == null || pamrPortStr.isEmpty()) {
            throw new TestExecutionException("Property '" + PAMR_EXISTING_ROUTER_PORT +
                "' required to run with exiting PAMR router not specified");
        }

        pamrPort = Integer.valueOf(pamrPortStr);
        InetAddress pamrHost = DeploymentTestUtils.checkHostIsAvailable(pamrHostName);
        if (pamrHost == null) {
            throw new TestExecutionException("Pamr host " + pamrHostName + " isn't available");
        }
    }

    @Override
    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), "pamr");
        properties.put(PAMRConfig.PA_NET_ROUTER_ADDRESS.getName(), pamrHostName);
        properties.put(PAMRConfig.PA_NET_ROUTER_PORT.getName(), String.valueOf(pamrPort.intValue()));
        return properties;
    }

    @Override
    public List<String> getAdditionalServerJavaOptions() {
        List<String> options = new ArrayList<String>();

        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pamr");
        options.add(PAMRConfig.PA_NET_ROUTER_ADDRESS.getCmdLine() + pamrHostName);
        options.add(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + String.valueOf(pamrPort.intValue()));

        options.add(PAMRConfig.PA_PAMR_AGENT_ID.getCmdLine() + serverPamrId.getId());
        options.add(PAMRConfig.PA_PAMR_AGENT_MAGIC_COOKIE.getCmdLine() + serverPamrId.getCookie());

        return options;
    }

    @Override
    public String prepareForDeployment() throws Exception {
        // TODO: check that url is really available
        return String.format("pamr://%s/", serverPamrId.getId());
    }

}
