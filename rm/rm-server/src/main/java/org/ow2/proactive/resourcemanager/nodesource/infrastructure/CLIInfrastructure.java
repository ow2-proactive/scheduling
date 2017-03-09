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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * An infrastructure manager that operates custom scripts in order to
 * deploy/remove nodes.
 * <p>
 * Deployment phase:
 * <ul>
 * <li>launch the script by providing host name, node name, node source name, rm
 * url</li>
 * <li>if no node within timeout =&gt; terminates the script.</li>
 * </ul>
 * <p>
 * Removal phase:
 * <ul>
 * <li>remove node from the resource manager</li>
 * <li>launch removal script giving host name and node url.</li>
 * </ul>
 */
public class CLIInfrastructure extends HostsFileBasedInfrastructureManager {

    @Configurable(description = "An interpreter that executes the script")
    protected String interpreter = "bash";

    @Configurable(fileBrowser = true, description = "A script that deploys a node on host (parameters: host, node, ns names and rm url).")
    protected File deploymentScript;

    @Configurable(fileBrowser = true, description = "A script that removes a node (parameters: host name and node url")
    protected File removalScript;

    private final AtomicInteger numberOfRemovalThread = new AtomicInteger(0);

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[4] : An interpreter that launch the script
     *            parameters[5] : A script that deploys nodes on a single host
     *            parameters[6] : A script that removes a node
     * @throws IllegalArgumentException
     *             configuration failed
     */
    @Override
    protected void configure(Object... parameters) {
        super.configure(parameters);
        int index = 4;
        // TODO super admin rights check
        if (parameters != null && parameters.length >= 7) {
            this.interpreter = parameters[index++].toString();

            try {
                byte[] bytes = (byte[]) parameters[index++];
                // putting .cmd as an extension so that it works on Windows
                deploymentScript = File.createTempFile("deployment", ".cmd");
                FileToBytesConverter.convertByteArrayToFile(bytes, deploymentScript);
                // deploymentScript.setExecutable(true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read deployment script", e);
            }

            try {
                byte[] bytes = (byte[]) parameters[index++];
                // putting .cmd as an extension so that it works on Windows
                removalScript = File.createTempFile("removal", ".cmd");
                FileToBytesConverter.convertByteArrayToFile(bytes, removalScript);
                // removalScript.setExecutable(true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read removal script file", e);
            }
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
     * @throws RMException
     *             acquisition failed
     */
    protected void startNodeImpl(InetAddress host, int nbNodes, final List<String> depNodeURLs)
            throws RMException {

        final String nodeName = "SCR-" + this.nodeSource.getName() + "-" + ProActiveCounter.getUniqID();
        final String commandLine = interpreter + " " + deploymentScript.getAbsolutePath() + " " +
            host.getHostName() + " " + nodeName + " " + this.nodeSource.getName() + " " + rmUrl + " " +
            nbNodes;

        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(nodeName, nbNodes);
        depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames, commandLine,
                "Deploying node on host " + host, this.nodeTimeOut));
        addTimeouts(depNodeURLs);

        Process p;
        try {
            logger.debug("Launching the command: " + commandLine);
            p = Runtime.getRuntime().exec(commandLine);
        } catch (IOException e1) {
            multipleDeclareDeployingNodeLost(depNodeURLs, "Cannot run command: " + commandLine +
                " - \n The following exception occured: " + getStackTraceAsString(e1));
            throw new RMException("Cannot run command: " + commandLine, e1);
        }

        String lf = System.lineSeparator();

        int circuitBreakerThreshold = 5;
        while (!anyTimedOut(depNodeURLs) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    logger.error("Child process at " + host.getHostName() + " exited abnormally (" +
                        exitCode + ").");
                } else {
                    logger.error("Launching node script has exited normally whereas it shouldn't.");
                }
                String pOutPut = Utils.extractProcessOutput(p);
                String pErrPut = Utils.extractProcessErrput(p);
                final String description = "Script failed to launch a node on host " + host.getHostName() +
                    lf + "   >Error code: " + exitCode + lf + "   >Errput: " + pErrPut + "   >Output: " +
                    pOutPut;
                logger.error(description);
                if (super.checkNodeIsAcquiredAndDo(nodeName, null, new Runnable() {
                    public void run() {
                        multipleDeclareDeployingNodeLost(depNodeURLs, description);
                    }
                })) {
                    return;
                } else {
                    // there isn't any race regarding node registration
                    throw new RMException(
                        "A node " + nodeName + " is not expected anymore because of an error.");
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                // registration is ok, we destroy the process
                logger.debug("Destroying the process: " + p);
                p.destroy();
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
        if (this.anyTimedOut(depNodeURLs)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void killNodeImpl(Node node, InetAddress h) {
        final Node n = node;
        final InetAddress host = h;
        numberOfRemovalThread.incrementAndGet();
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    final String commandLine = interpreter + " " + removalScript.getAbsolutePath() + " " +
                        host.getHostName() + " " + n.getNodeInformation().getURL();
                    Process p;
                    try {
                        logger.debug("Launching the command: " + commandLine);
                        p = Runtime.getRuntime().exec(commandLine);
                        // TODO add timeout behavior
                        int exitCode = p.waitFor();
                        String pOutPut = Utils.extractProcessOutput(p);
                        String pErrPut = Utils.extractProcessErrput(p);
                        String lf = System.lineSeparator();
                        final String description = "Removal script ouput" + lf + "   >Error code: " +
                            exitCode + lf + "   >Errput: " + pErrPut + "   >Output: " + pOutPut;
                        if (exitCode != 0) {
                            logger.error("Child process at " + host.getHostName() + " exited abnormally (" +
                                exitCode + ").");
                            logger.error(description);
                        } else {
                            logger.info("Removal node process has exited normally for " +
                                n.getNodeInformation().getURL());
                            logger.debug(description);
                        }
                    } catch (IOException e1) {
                        logger.error(e1);
                    }

                } catch (Exception e) {
                    logger.trace("An exception occurred during node removal", e);
                }
                numberOfRemovalThread.decrementAndGet();
            }
        });
    }

    /**
     * @return short description of the IM
     */
    @Override
    public String getDescription() {
        return "Creates remote runtimes using custom scripts";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Script Infrastructure";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutDown() {
        deploymentScript.delete();

        // checking if we need to delete the removal script
        if (this.numberOfRemovalThread.get() <= 0) {
            removalScript.delete();
        }
    }
}
