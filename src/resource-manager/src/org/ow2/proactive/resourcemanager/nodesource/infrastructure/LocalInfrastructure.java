package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.IOException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.OperatingSystem;
import org.ow2.proactive.utils.Tools;


public class LocalInfrastructure extends InfrastructureManager {

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true)
    private Credentials credentials;
    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine")
    private int maxNodes = 4;
    private AtomicInteger atomicMaxNodes;
    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    private int nodeTimeout = 5000;
    @Configurable(description = "Aditionnal ProActive properties")
    private String paProperties = "";

    /** To link a nodeName with its process */
    private Hashtable<String, Process> nodeNameToProcess;
    /** To link a deploying node url with a boolean, used to notify deploying node removal */
    private Hashtable<String, Boolean> isDeployingNodeLost;
    /** Notifies the acquisition loop that the node has been acquired and that it can exit the loop */
    private Hashtable<String, Boolean> isNodeAcquired;

    /** The shell interpret and its "command" argument */
    private static final String SHELL_INTERPRET = "/bin/sh";
    private static final String SHELL_COMMAND_OPTION = "-c";

    public LocalInfrastructure() {
    }

    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        while (this.atomicMaxNodes.getAndDecrement() >= 1) {
            this.nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    LocalInfrastructure.this.acquireNodeImpl();
                }
            });
        }
        //one decremented once too many
        this.atomicMaxNodes.getAndIncrement();
        logger.info("Cannot acquire more nodes");
    }

    @Override
    public void acquireNode() {
        if (this.atomicMaxNodes.getAndDecrement() >= 1) {
            this.nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    LocalInfrastructure.this.acquireNodeImpl();
                }
            });
        } else {
            //one decremented once too many
            this.atomicMaxNodes.getAndIncrement();
            logger.warn("Cannot acquire a new node");
        }
    }

    private void acquireNodeImpl() {
        String nodeName = "local-" + this.nodeSource.getName() + "-" + ProActiveCounter.getUniqID();
        OperatingSystem os = OperatingSystem.UNIX;
        //assuming no cygwin, windows or the "others"...
        if (System.getProperty("os.name").contains("Windows")) {
            os = OperatingSystem.WINDOWS;
        }
        String rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
        if (!rmHome.endsWith(os.fs)) {
            rmHome += os.fs;
        }
        CommandLineBuilder clb = this.getDefaultCommandLineBuilder(os);
        //RM_Home set in bin/unix/env script
        clb.setRmHome(rmHome);
        boolean containsSpace = rmHome.contains(" ");
        ArrayList<String> paPropList = new ArrayList<String>();
        if (!this.paProperties.contains(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getName())) {
            StringBuilder sb = new StringBuilder(CentralPAPropertyRepository.JAVA_SECURITY_POLICY
                    .getCmdLine());
            sb.append(rmHome).append("config").append(os.fs).append("security.java.policy-client");
            paPropList.add(sb.toString());
        }
        if (!this.paProperties.contains(CentralPAPropertyRepository.LOG4J.getName())) {
            StringBuilder sb = new StringBuilder(CentralPAPropertyRepository.LOG4J.getCmdLine());
            sb.append("file:"); // log4j only understands urls
            sb.append(rmHome).append("config").append(os.fs).append("log4j").append(os.fs).append(
                    "log4j-defaultNode");
            paPropList.add(sb.toString());
        }
        if (!this.paProperties.contains(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName())) {
            StringBuilder sb = new StringBuilder(CentralPAPropertyRepository.PA_CONFIGURATION_FILE
                    .getCmdLine());
            sb.append(rmHome).append("config").append(os.fs).append("proactive").append(os.fs).append(
                    "ProActiveConfiguration.xml");
            paPropList.add(sb.toString());
        }
        if (!this.paProperties.contains(PAResourceManagerProperties.RM_HOME.getKey())) {
            StringBuilder sb = new StringBuilder(PAResourceManagerProperties.RM_HOME.getCmdLine());
            sb.append(rmHome);
            paPropList.add(sb.toString());
        }
        Collections.addAll(paPropList, this.paProperties.split(" "));
        clb.setPaProperties(paPropList);
        clb.setNodeName(nodeName);
        try {
            clb.setCredentialsValueAndNullOthers(new String(this.credentials.getBase64()));
        } catch (KeyException e) {
            createLostNode(nodeName, "Cannot decrypt credentials value", e);
            return;
        }
        List<String> cmd = null;
        try {
            cmd = clb.buildCommandLineAsList(false);
        } catch (IOException e) {
            createLostNode(nodeName, "Cannot build command line", e);
            return;
        }

        // The printed cmd with obfuscated credentials
        final String obfuscatedCmd = Tools.join(cmd, " ");

        String depNodeURL = null;
        Process proc = null;
        try {
            this.isNodeAcquired.put(nodeName, false);
            if (os == OperatingSystem.UNIX && containsSpace) {
                depNodeURL = this.addDeployingNode(nodeName, SHELL_INTERPRET + " " + SHELL_COMMAND_OPTION +
                    " " + obfuscatedCmd, "Node launched locally", this.nodeTimeout);

                logger
                        .debug("LocalIM detected the libRoot variable contains whitespaces. Running the escaped command prepended with \"" +
                            SHELL_INTERPRET + " " + SHELL_COMMAND_OPTION + "\".");

                List<String> newCmd = Arrays.asList(SHELL_INTERPRET, SHELL_COMMAND_OPTION);
                newCmd.addAll(cmd);
                cmd = newCmd;
            } else {
                depNodeURL = this.addDeployingNode(nodeName, obfuscatedCmd, "Node launched locally",
                        this.nodeTimeout);
            }

            // Deobfuscate the cred value
            Collections.replaceAll(cmd, CommandLineBuilder.OBFUSC, clb.getCredentialsValue());

            this.isDeployingNodeLost.put(depNodeURL, false);
            proc = (new ProcessBuilder(cmd)).start();

            this.nodeNameToProcess.put(nodeName, proc);
        } catch (IOException e) {
            String lf = System.getProperty("line.separator");
            String mess = "Cannot launch rm node " + nodeName + lf + Utils.getStacktrace(e);
            this.declareDeployingNodeLost(depNodeURL, mess);
            return;
        }

        // listening output & error of forked process in separated threads
        // otherwise the process may hang

        final Process process = proc;
        Thread outReader = new Thread(new Runnable() {
            public void run() {
                Utils.consumeProcessStream(process.getInputStream());
            }
        });
        outReader.setName("Node " + nodeName + " output listener ");
        outReader.setDaemon(true);
        outReader.start();

        Thread errReader = new Thread(new Runnable() {
            public void run() {
                Utils.consumeProcessStream(process.getErrorStream());
            }
        });
        errReader.setName("Node " + nodeName + " error listener ");
        errReader.setDaemon(true);
        errReader.start();

        //watching process
        int threshold = 5;
        Boolean isLost = false, isAcquired = false;
        while (((isLost = this.isDeployingNodeLost.get(depNodeURL)) != null) && !isLost &&
            ((isAcquired = this.isNodeAcquired.get(nodeName)) != null) && !isAcquired) {
            try {
                int exit = proc.exitValue();
                if (exit != 0) {
                    String lf = System.getProperty("line.separator");
                    String message = "RMNode exit code == " + exit + lf;
                    message += "Command: " + obfuscatedCmd + lf;
                    String out = Utils.extractProcessOutput(proc);
                    String err = Utils.extractProcessErrput(proc);
                    message += "output: " + out + lf + "errput: " + err;
                    this.declareDeployingNodeLost(depNodeURL, message);
                }
                //nodeNameToProcess will be clean up when exiting the loop
            } catch (IllegalThreadStateException e) {
                logger.debug("Waiting for node " + nodeName + " acquisition");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for local process status", e);
                threshold--;
                if (threshold <= 0) {
                    break;
                }
            }
        }
        logger.debug("Local Infrastructure manager exits watching loop for node " + nodeName);
        if (isLost) {
            //clean up the process
            proc.destroy();
            this.nodeNameToProcess.remove(nodeName);
        }
        this.isDeployingNodeLost.remove(depNodeURL);
        this.isNodeAcquired.remove(nodeName);
    }

    /**
     * Creates a lost node. The deployment has failed while building the command line
     * @param string a message
     * @param e the cause
     */
    private void createLostNode(String name, String message, Throwable e) {
        this.isNodeAcquired.remove(name);
        String lf = System.getProperty("line.separator");
        String url = super.addDeployingNode(name, "deployed as daemon",
                "Deploying a new windows azure insance", this.nodeTimeout);
        String st = Utils.getStacktrace(e);
        super.declareDeployingNodeLost(url, message + lf + st);
    }

    /**
     * args[0] = credentials
     * args[1] = max nodes
     * args[2] = timeout
     * args[3] = pa props
     */
    @Override
    protected void configure(Object... args) {
        this.isDeployingNodeLost = new Hashtable<String, Boolean>();
        this.nodeNameToProcess = new Hashtable<String, Process>();
        this.isNodeAcquired = new Hashtable<String, Boolean>();
        int index = 0;
        try {
            this.credentials = Credentials.getCredentialsBase64((byte[]) args[index++]);
        } catch (KeyException e1) {
            throw new IllegalArgumentException("Cannot decrypt credentials", e1);
        }

        try {
            this.maxNodes = Integer.parseInt(args[index++].toString());
            this.atomicMaxNodes = new AtomicInteger(this.maxNodes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot determine max node");
        }

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
        //notifies the watching loop to exit... it will clean
        //isDeployingNodeLost & isNodeAcquired collections up.
        this.isDeployingNodeLost.put(pnURL, true);
    }

    @Override
    protected void notifyAcquiredNode(Node arg0) throws RMException {
        this.isNodeAcquired.put(arg0.getNodeInformation().getName(), true);
    }

    @Override
    public void removeNode(Node arg0) throws RMException {
        String nodeName = arg0.getNodeInformation().getName();
        Process proc = this.nodeNameToProcess.remove(nodeName);
        if (proc != null) {
            try {
                proc.destroy();
                logger.info("Process associated to node " + nodeName + " destroyed");
            } finally {
                this.atomicMaxNodes.incrementAndGet();
            }
        } else {
            logger.warn("No process associated to node " + nodeName);
        }
    }
}
