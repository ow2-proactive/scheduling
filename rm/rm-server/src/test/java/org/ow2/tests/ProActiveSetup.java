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
package org.ow2.tests;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObjectFactory;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * ProActive configuration for tests
 *
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class ProActiveSetup {
    /** Name of the variable used in gcmd to set the os type. */
    static final private String VAR_OS = "os";

    /** Name of the variable used in gcma to set the java parameters */
    static final private String VAR_JVM_PARAMETERS = "JVM_PARAMETERS";

    /** The parameters to pass to the forked JVMs */
    final private List<String> jvmParameters;

    /** The variable contract to pass to GCMA. */
    final private VariableContractImpl vc;

    final private PAMRSetup pamrSetup;

    public ProActiveSetup() {
        this.pamrSetup = new PAMRSetup();
        this.jvmParameters = buildJvmParameters();
        this.vc = buildVariableContract(this.getJvmParameters());
    }

    /**
     * Start everything required to run the tests
     *
     * @throws Exception
     *   If something goes wrong
     */
    final public void start() throws Exception {
        this.pamrSetup.start();
    }

    final public VariableContractImpl getVariableContract() {
        return this.vc;
    }

    final public String getJvmParameters() {
        StringBuilder sb = new StringBuilder();
        for (String param : jvmParameters) {
            sb.append(param).append(" ");
        }
        return sb.toString().trim();
    }

    final public List<String> getJvmParametersAsList() {
        return this.jvmParameters;
    }

    public static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append(item);
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Stop everything that had been started by {@link ProActiveSetup#start}
     *
     * @throws Exception
     *   If something goes wrong
     */
    final public void shutdown() {
        this.pamrSetup.stop();
    }

    private VariableContractImpl buildVariableContract(String jvmParameters) {
        VariableContractImpl vContract;
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS,
                                         OperatingSystem.getOperatingSystem().name(),
                                         VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(VAR_JVM_PARAMETERS, jvmParameters, VariableContractType.ProgramVariable);
        return vContract;
    }

    private List<String> buildJvmParameters() {
        final ArrayList<String> jvmParameters = new ArrayList<>();
        jvmParameters.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        jvmParameters.add("-Djava.awt.headless=true"); // for Mac builds
        jvmParameters.add(CentralPAPropertyRepository.PA_RUNTIME_PING.getCmdLine() + false);
        jvmParameters.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() +
                          CentralPAPropertyRepository.PA_HOME.getValue());
        jvmParameters.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");

        if (PAMRRemoteObjectFactory.PROTOCOL_ID.equals(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue())) {
            jvmParameters.add(PAMRConfig.PA_NET_ROUTER_ADDRESS.getCmdLine() + this.pamrSetup.address);
            jvmParameters.add(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + this.pamrSetup.port);
        }

        return jvmParameters;
    }

    static private class PAMRSetup {
        /** PAMR router when started or null. */
        volatile private Router router;

        /** Reversed port for PAMR router. */
        final int port;

        /** Reserve a port for the router if needed */
        volatile private ServerSocket reservedPort;

        /** Address of the PAMR router. */
        final String address;

        public PAMRSetup() {
            // Get router address
            if (PAMRConfig.PA_NET_ROUTER_ADDRESS.isSet()) {
                address = PAMRConfig.PA_NET_ROUTER_ADDRESS.getValue();
            } else {
                address = "localhost";
            }

            // Get router port (reserve a dynamic port if needed)
            if (PAMRConfig.PA_NET_ROUTER_PORT.isSet() && PAMRConfig.PA_NET_ROUTER_PORT.getValue() != 0) {
                port = PAMRConfig.PA_NET_ROUTER_PORT.getValue();
            } else {
                ServerSocket ss = null;
                try {
                    ss = new ServerSocket(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reservedPort = ss;
                port = reservedPort.getLocalPort();
            }
            PAMRConfig.PA_NET_ROUTER_PORT.setValue(this.port);

        }

        synchronized public void start() throws Exception {
            if (this.router != null) {
                return;
            }

            if (this.reservedPort != null) {
                this.reservedPort.close();
            }

            if (PAMRRemoteObjectFactory.PROTOCOL_ID.equals(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue())) {
                RouterConfig config = new RouterConfig();
                config.setPort(this.port);
                router = Router.createAndStart(config);
            }
        }

        public void stop() {
            if (this.router != null) {
                this.router.stop();
            }
        }
    }
}
