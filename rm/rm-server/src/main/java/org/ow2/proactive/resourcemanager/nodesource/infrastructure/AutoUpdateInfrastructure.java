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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import static com.google.common.base.Throwables.getStackTraceAsString;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyException;
import java.util.List;
import java.util.Properties;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.process_tree_killer.ProcessTree;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;


/**
 *
 * This infrastructure launching a node by download node.jar on a node machine.
 * Command to start a node can be tuned by client.
 *
 */
public class AutoUpdateInfrastructure extends HostsFileBasedInfrastructureManager {

    public static final String CLI_FILE_PROPERTY = "cli.file.property";

    public static final String NODE_NAME = "node.name";

    public static final String HOST_NAME = "host.name";

    public static final String NODESOURCE_NAME = "nodesource.name";

    public static final String NODESOURCE_CREDENTIALS = "nodesource.credentials";

    public static final String NB_NODES = "nb.nodes";

    @Configurable(description = "Command that will be launched for every host")
    protected String command = "scp -o StrictHostKeyChecking=no ${pa.rm.home}/dist/war/rest/node.jar ${host.name}:/tmp/${node.name}.jar && " +
                               "ssh -o StrictHostKeyChecking=no ${host.name} " +
                               "\"${java.home}/bin/java -jar /tmp/${node.name}.jar -w ${nb.nodes} -v ${nodesource.credentials} -n ${node.name} -s ${nodesource.name} -p 30000 -r ${rm.url} " +
                               "1>>/tmp/${node.name}.log 2>&1\"";

    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        if (parameters != null && parameters.length >= 4) {
            this.command = parameters[4].toString();
        }
    }

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using a custom script, register it
     * manually in the nodesource.
     *
     * @param host The host on which one the node will be started
     * @param nbNodes number of nodes to deploy
     * @param depNodeURLs list of deploying or lost nodes urls created      
     * @throws org.ow2.proactive.resourcemanager.exception.RMException
     *             acquisition failed
     */
    protected void startNodeImpl(InetAddress host, int nbNodes, final List<String> depNodeURLs) throws RMException {

        final String nodeName = this.nodeSource.getName() + "-" + ProActiveCounter.getUniqID();

        String credentials = "";
        try {
            credentials = new String(nodeSource.getAdministrator().getCredentials().getBase64());
        } catch (KeyException e) {
            logger.error("Invalid credentials");
            return;
        }

        Properties localProperties = new Properties();
        localProperties.put(NODE_NAME, nodeName);
        localProperties.put(HOST_NAME, host.getHostName());
        localProperties.put(NODESOURCE_CREDENTIALS, credentials);
        localProperties.put(NODESOURCE_NAME, nodeSource.getName());
        localProperties.put(NB_NODES, nbNodes);

        String filledCommand = replaceProperties(command, localProperties);
        filledCommand = replaceProperties(filledCommand, System.getProperties());

        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(nodeName, nbNodes);
        depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames,
                                                     filledCommand,
                                                     "Deploying node on host " + host,
                                                     this.nodeTimeOut));
        addTimeouts(depNodeURLs);

        Process p;
        try {
            logger.debug("Deploying node: " + nodeName);
            logger.debug("Launching the command: " + filledCommand);
            p = Runtime.getRuntime().exec(new String[] { "bash", "-c", filledCommand });
        } catch (IOException e1) {
            multipleDeclareDeployingNodeLost(depNodeURLs,
                                             "Cannot run command: " + filledCommand +
                                                          " - \n The following exception occurred: " +
                                                          getStackTraceAsString(e1));
            throw new RMException("Cannot run command: " + filledCommand, e1);
        }

        String lf = System.lineSeparator();

        int circuitBreakerThreshold = 5;
        while (!anyTimedOut(depNodeURLs) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    logger.error("Child process at " + host.getHostName() + " exited abnormally (" + exitCode + ").");
                } else {
                    logger.error("Launching node script has exited normally whereas it shouldn't.");
                }
                String pOutPut = Utils.extractProcessOutput(p);
                String pErrPut = Utils.extractProcessErrput(p);
                final String description = "Script failed to launch a node on host " + host.getHostName() + lf +
                                           "   >Error code: " + exitCode + lf + "   >Errput: " + pErrPut +
                                           "   >Output: " + pOutPut;
                logger.error(description);
                if (super.checkNodeIsAcquiredAndDo(nodeName, null, new Runnable() {
                    public void run() {
                        multipleDeclareDeployingNodeLost(depNodeURLs, description);
                    }
                })) {
                    return;
                } else {
                    // there isn't any race regarding node registration
                    throw new RMException("A node " + nodeName + " is not expected anymore because of an error.");
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                // registration is ok, we destroy the process
                logger.debug("Destroying the process: " + p);
                try {
                    ProcessTree.get().get(p).kill();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                circuitBreakerThreshold--;
                logger.trace("An exception occurred while monitoring a child process", e);
            }
        }

        // if we exit because of a timeout
        if (anyTimedOut(depNodeURLs)) {
            // we remove it
            removeTimeouts(depNodeURLs);
            // we destroy the process
            p.destroy();
            throw new RMException("Deploying Node " + nodeName + " not expected any more");
        }
        if (circuitBreakerThreshold <= 0) {
            logger.error("Circuit breaker threshold reached while monitoring a child process.");
            throw new RMException("Several exceptions occurred while monitoring a child process.");
        }
    }

    private String replaceProperties(String commandLine, Properties properties) {

        for (Object prop : properties.keySet()) {
            String propName = "${" + prop + "}";
            String propValue = properties.getProperty(prop.toString());
            if (propValue != null) {
                commandLine = commandLine.replace(propName, propValue);
            }
        }
        return commandLine;
    }

    @Override
    protected void killNodeImpl(Node node, InetAddress host) throws RMException {
    }

    /**
     * @return short description of the IM
     */
    @Override
    public String getDescription() {
        return "In order to deploy a node this infrastructure ssh to a computer and uses wget to download proactive node distribution. It keep nodes always up-to-dated and does not require pre-installed proactive on a node machine.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AutoUpdate Infrastructure";
    }
}
