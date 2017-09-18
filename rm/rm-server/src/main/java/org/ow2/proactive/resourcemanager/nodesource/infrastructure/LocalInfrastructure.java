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

import java.io.IOException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;


public class LocalInfrastructure extends InfrastructureManager {

    public static final int DEFAULT_NODE_NUMBER = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

    public static final long DEFAULT_TIMEOUT = 30000;

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true)
    private Credentials credentials;

    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine")
    private int maxNodes = DEFAULT_NODE_NUMBER;

    // number of nodes which can still be acquired
    private AtomicInteger acquiredNodes;

    private AtomicInteger lostNodes;

    private AtomicInteger handledNodes;

    /**
     * Index of deployment, if startNode is called multiple times, each time a new process will be created.
     * The index is used to prevent conflicts in nodes urls
     */
    private AtomicInteger index;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    private long nodeTimeout = DEFAULT_TIMEOUT;

    @Configurable(description = "Additional ProActive properties")
    private String paProperties = "";

    /**
     * A map containing process executors, associated with their corresponding deployment node urls.
     */
    private transient Map<ProcessExecutor, List<String>> processExecutors = new ConcurrentHashMap<>();

    public LocalInfrastructure() {
    }

    @Override
    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        startNode(maxNodes);
    }

    @Override
    public void acquireNode() {
        startNode(1);
    }

    @Override
    public void acquireNodes(int n, Map<String, ?> nodeConfiguration) {
        startNode(n);
    }

    private void startNode(final int n) {
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                LocalInfrastructure.this.startNodeProcess(n);
            }
        });
    }

    private void startNodeProcess(int numberOfNodes) {

        this.handledNodes.addAndGet(numberOfNodes);
        int currentIndex = index.getAndIncrement();
        String baseNodeName = "local-" + this.nodeSource.getName() + "-" + currentIndex;
        OperatingSystem os = OperatingSystem.UNIX;
        // assuming no cygwin, windows or the "others"...
        if (System.getProperty("os.name").contains("Windows")) {
            os = OperatingSystem.WINDOWS;
        }
        String rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
        if (!rmHome.endsWith(os.fs)) {
            rmHome += os.fs;
        }
        CommandLineBuilder clb = this.getDefaultCommandLineBuilder(os);
        // RM_Home set in bin/unix/env script
        clb.setRmHome(rmHome);
        ArrayList<String> paPropList = new ArrayList<>();
        if (!this.paProperties.contains(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getName())) {
            paPropList.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + rmHome + "config" + os.fs +
                           "security.java.policy-client");
        }
        if (!this.paProperties.contains(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName())) {
            paPropList.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() + rmHome + "config" + os.fs +
                           "network" + os.fs + "node.ini");
        }
        if (!this.paProperties.contains(PAResourceManagerProperties.RM_HOME.getKey())) {
            paPropList.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + rmHome);
        }
        if (!this.paProperties.contains("java.library.path")) {
            paPropList.add("-Djava.library.path=" + System.getProperty("java.library.path"));
        }
        if (!paProperties.isEmpty()) {
            Collections.addAll(paPropList, this.paProperties.split(" "));
        }
        clb.setPaProperties(paPropList);
        clb.setNodeName(baseNodeName);
        clb.setNumberOfNodes(numberOfNodes);
        try {
            clb.setCredentialsValueAndNullOthers(new String(this.credentials.getBase64()));
        } catch (KeyException e) {
            createLostNodes(baseNodeName, numberOfNodes, "Cannot decrypt credentials value", e);
            return;
        }
        List<String> cmd;
        try {
            cmd = clb.buildCommandLineAsList(false);
        } catch (IOException e) {
            createLostNodes(baseNodeName, numberOfNodes, "Cannot build command line", e);
            return;
        }

        // The printed cmd with obfuscated credentials
        final String obfuscatedCmd = Joiner.on(' ').join(cmd);

        List<String> depNodeURLs = new ArrayList<>(numberOfNodes);
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseNodeName, numberOfNodes);
        ProcessExecutor processExecutor = null;
        try {
            depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames,
                                                         obfuscatedCmd,
                                                         "Node launched locally",
                                                         this.nodeTimeout));

            // Deobfuscate the cred value
            Collections.replaceAll(cmd, CommandLineBuilder.OBFUSC, clb.getCredentialsValue());

            processExecutor = new ProcessExecutor(baseNodeName, cmd, false, true);
            processExecutor.start();

            processExecutors.put(processExecutor, depNodeURLs);

            final ProcessExecutor tmpProcessExecutor = processExecutor;

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (tmpProcessExecutor != null && !tmpProcessExecutor.isProcessFinished()) {
                        tmpProcessExecutor.killProcess();
                    }
                }
            }));

            logger.info("Local Nodes command started : " + obfuscatedCmd);

        } catch (IOException e) {
            String lf = System.lineSeparator();
            String mess = "Cannot launch rm node " + baseNodeName + lf + Throwables.getStackTraceAsString(e);
            multipleDeclareDeployingNodeLost(depNodeURLs, mess);
            if (processExecutor != null) {
                cleanProcess(processExecutor);
            }
            return;
        }

        // watching process
        int threshold = 10;
        while (!allNodesAcquiredOrLost()) {
            if (processExecutor.isProcessFinished()) {
                int exit = processExecutor.getExitCode();
                if (exit != 0) {
                    String lf = System.lineSeparator();
                    String message = "RMNode exit code == " + exit + lf;
                    message += "Command: " + obfuscatedCmd + lf;
                    String out = Joiner.on('\n').join(processExecutor.getOutput());
                    String err = Joiner.on('\n').join(processExecutor.getErrorOutput());
                    message += "stdout: " + out + lf + "stderr: " + err;
                    multipleDeclareDeployingNodeLost(depNodeURLs, message);
                }
            } else {
                logger.debug("Waiting for nodes " + baseNodeName + " acquisition");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for local process status", e);
                threshold--;
                if (threshold <= 0) {
                    break;
                }
            }
        }

        logger.debug("Local Infrastructure manager exits watching loop for nodes " + baseNodeName);
        logNodeOutput(baseNodeName + " stdout: ", processExecutor.getOutput());
        logNodeOutput(baseNodeName + " stderr: ", processExecutor.getErrorOutput());

        if (allNodesLost(numberOfNodes)) {
            // clean up the process
            cleanProcess(processExecutor);
        }
    }

    private void logNodeOutput(final String prefix, List<String> nodeOutputLines) {
        if (nodeOutputLines != null) {
            for (String processOutputLine : nodeOutputLines) {
                logger.debug(prefix + processOutputLine);
            }
        }
    }

    /**
     * Creates a lost node. The deployment has failed while building the command
     * line
     *
     * @param numberOfNodes
     * @param message
     *            a message
     * @param e
     */
    private void createLostNodes(String baseName, int numberOfNodes, String message, Throwable e) {
        List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseName, numberOfNodes);
        for (int nodeIndex = 0; nodeIndex < numberOfNodes; nodeIndex++) {
            String name = createdNodeNames.get(nodeIndex);
            String lf = System.lineSeparator();
            String url = super.addDeployingNode(name,
                                                "deployed as daemon",
                                                "Deploying a local infrastructure node",
                                                this.nodeTimeout);
            String st = Throwables.getStackTraceAsString(e);
            super.declareDeployingNodeLost(url, message + lf + st);
        }
    }

    private boolean allNodesAcquiredOrLost() {
        return (acquiredNodes.get() + lostNodes.get()) == handledNodes.get();
    }

    private boolean allNodesLost(int numberOfNodes) {
        return lostNodes.get() == numberOfNodes;
    }

    private void cleanProcess(ProcessExecutor processExecutor) {
        if (processExecutor != null) {
            processExecutor.killProcess();
            processExecutors.remove(processExecutor);
        }
    }

    /**
     * args[0] = credentials args[1] = max nodes args[2] = timeout args[3] = pa
     * props
     */
    @Override
    protected void configure(Object... args) {
        int index = 0;
        try {
            this.credentials = Credentials.getCredentialsBase64((byte[]) args[index++]);
        } catch (KeyException e1) {
            throw new IllegalArgumentException("Cannot decrypt credentials", e1);
        }

        try {
            this.maxNodes = Integer.parseInt(args[index++].toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot determine max node");
        }

        this.acquiredNodes = new AtomicInteger(0);
        this.lostNodes = new AtomicInteger(0);
        this.handledNodes = new AtomicInteger(0);
        this.index = new AtomicInteger(0);

        try {
            this.nodeTimeout = Integer.parseInt(args[index++].toString());
        } catch (Exception e) {
            logger.warn("Cannot determine node timeout, using default:" + this.nodeTimeout, e);
        }

        this.paProperties = args[index++].toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        this.lostNodes.incrementAndGet();
    }

    @Override
    protected void notifyAcquiredNode(Node arg0) throws RMException {
        this.acquiredNodes.incrementAndGet();
    }

    @Override
    public void removeNode(Node node) throws RMException {
        logger.debug("Removing node " + node.getNodeInformation().getURL() + " from " +
                     this.getClass().getSimpleName());

        if (!this.nodeSource.getDownNodes().contains(node)) {
            // the node was manually removed
            handledNodes.decrementAndGet();
        }

        int remainingNodesCount = this.acquiredNodes.decrementAndGet();
        // If there is no remaining node, kill the JVM process
        if (remainingNodesCount == 0) {
            shutDown();
        }
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        acquiredNodes.incrementAndGet();
    }

    @Override
    public void shutDown() {
        for (ProcessExecutor processExecutor : processExecutors.keySet()) {
            processExecutor.killProcess();
        }
        processExecutors.clear();
        // do not set processExecutor to null here or NPE can appear in the startProcess method, running in a different thread.
        logger.info("Process associated with node source " + nodeSource.getName() + " destroyed");
    }

    @Override
    public String toString() {
        return "Local Infrastructure";
    }

}
