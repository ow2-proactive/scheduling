package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.IOException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.ow2.proactive.utils.Tools;

import static com.google.common.base.Throwables.getStackTraceAsString;


public class LocalInfrastructure extends InfrastructureManager {

    public static final int DEFAULT_NODE_NUMBER = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    public static final int DEFAULT_TIMEOUT = 30000;

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true)
    private Credentials credentials;
    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine")
    private int maxNodes = DEFAULT_NODE_NUMBER;
    // number of nodes which can still be acquired
    private AtomicInteger acquiredNodes;
    private AtomicInteger lostNodes;
    private AtomicInteger handledNodes;
    private AtomicBoolean commandLineStarted;
    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    private int nodeTimeout = DEFAULT_TIMEOUT;
    @Configurable(description = "Additional ProActive properties")
    private String paProperties = "";

    private transient ProcessExecutor processExecutor;

    public LocalInfrastructure() {
    }

    @Override
    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        this.acquireNode();
    }

    @Override
    public void acquireNode() {
        if (this.commandLineStarted.compareAndSet(false, true)) {
            this.nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    LocalInfrastructure.this.startNodeProcess();
                }
            });
        } else {
            logger.debug("Cannot acquire more nodes");
        }
    }

    private void startNodeProcess() {
        acquiredNodes.set(0);
        lostNodes.set(0);

        String baseNodeName = "local-" + this.nodeSource.getName();
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
            paPropList.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + rmHome + "config" +
                os.fs + "security.java.policy-client");
        }
        if (!this.paProperties.contains(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName())) {
            paPropList.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() + rmHome +
                "config" + os.fs + "network" + os.fs + "node.ini");
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
        clb.setNumberOfNodes(handledNodes.get());
        try {
            clb.setCredentialsValueAndNullOthers(new String(this.credentials.getBase64()));
        } catch (KeyException e) {
            createLostNodes(baseNodeName, "Cannot decrypt credentials value", e);
            return;
        }
        List<String> cmd;
        try {
            cmd = clb.buildCommandLineAsList(false);
        } catch (IOException e) {
            createLostNodes(baseNodeName, "Cannot build command line", e);
            return;
        }

        // The printed cmd with obfuscated credentials
        final String obfuscatedCmd = Tools.join(cmd, " ");

        List<String> depNodeURLs = new ArrayList<>(handledNodes.get());
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseNodeName, handledNodes.get());
        try {
            depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames, obfuscatedCmd, "Node launched locally", this.nodeTimeout));

            // Deobfuscate the cred value
            Collections.replaceAll(cmd, CommandLineBuilder.OBFUSC, clb.getCredentialsValue());

            processExecutor = new ProcessExecutor(baseNodeName, cmd, false, true);
            processExecutor.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (processExecutor != null && !processExecutor.isProcessFinished()) {
                        processExecutor.killProcess();
                    }
                }
            }));

            logger.info("Local Nodes command started : " + obfuscatedCmd);

        } catch (IOException e) {
            String lf = System.lineSeparator();
            String mess = "Cannot launch rm node " + baseNodeName + lf + Utils.getStacktrace(e);
            multipleDeclareDeployingNodeLost(depNodeURLs, mess);
            if (processExecutor != null) {
                cleanProcess();
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
                    String out = Tools.join(processExecutor.getOutput(), "\n");
                    String err = Tools.join(processExecutor.getErrorOutput(), "\n");
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

        if (allNodesLost()) {
            // clean up the process
            cleanProcess();
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
     * @param message
     *            a message
     * @param e
     *            the cause
     */
    private void createLostNodes(String baseName, String message, Throwable e) {
        List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseName, handledNodes.get());
        for (int nodeIndex = 0; nodeIndex < handledNodes.get(); nodeIndex++) {
            String name = createdNodeNames.get(nodeIndex);
            String lf = System.lineSeparator();
            String url = super.addDeployingNode(name, "deployed as daemon",
                    "Deploying a local infrastructure node", this.nodeTimeout);
            String st = getStackTraceAsString(e);
            super.declareDeployingNodeLost(url, message + lf + st);
        }
    }

    private boolean allNodesAcquiredOrLost() {
        return (acquiredNodes.get() + lostNodes.get()) == handledNodes.get();
    }

    private boolean allNodesLost() {
        return lostNodes.get() == handledNodes.get();
    }

    private void cleanProcess() {
        if (processExecutor != null) {
            processExecutor.killProcess();
            commandLineStarted.set(false);
            processExecutor = null;
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
        this.commandLineStarted = new AtomicBoolean(false);
        this.handledNodes = new AtomicInteger(maxNodes);

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
        logger.debug("Removing node " + node.getNodeInformation().getURL() + " from " + this.getClass().getSimpleName());

        if (!this.nodeSource.getDownNodes().contains(node)) {
            // the node was manually removed
            handledNodes.decrementAndGet();
        }

        int remainingNodesCount = this.acquiredNodes.decrementAndGet();
        // If there is no remaining node, kill the JVM process
        if (remainingNodesCount == 0 && commandLineStarted.get()) {
            shutDown();
        }
    }

    @Override
    public void shutDown() {
        if (processExecutor != null) {
            processExecutor.killProcess();
        }
        commandLineStarted.set(false);
        // do not set processExecutor to null here or NPE can appear in the startProcess method, running in a different thread.
        logger.info("Process associated with node source " + nodeSource.getName() + " destroyed");
    }

    @Override
    public String toString() {
        return "Local Infrastructure";
    }
}
