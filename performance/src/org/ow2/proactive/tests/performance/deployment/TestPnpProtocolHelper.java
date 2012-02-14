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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;


public class TestPnpProtocolHelper extends TestProtocolHelper {

    private Integer pnpPort;

    public TestPnpProtocolHelper(HostTestEnv serverHostEnv) {
        super(serverHostEnv);
    }

    @Override
    public String prepareForDeployment() throws Exception {
        pnpPort = DeploymentTestUtils.findFreePort(serverHostEnv);
        String url = String.format("pnp://%s:%d/", serverHostEnv.getHost().getHostName(), pnpPort.intValue());
        return url;
    }

    @Override
    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), "pnp");
        return properties;
    }

    @Override
    public List<String> getAdditionalServerJavaOptions() {
        if (pnpPort == null) {
            throw new IllegalStateException("TestPnpRMDeployHelper didn't prepare deployment");
        }
        List<String> options = new ArrayList<String>();
        options.add(PNPConfig.PA_PNP_PORT.getCmdLine() + String.valueOf(pnpPort.intValue()));
        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");
        return options;
    }

}
