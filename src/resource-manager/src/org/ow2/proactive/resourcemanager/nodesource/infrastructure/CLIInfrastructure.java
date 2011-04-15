package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * An infrastructure manager that operates custom scripts in order to deploy/remove nodes.
 * Deployment phase:
 * 	- launch the script by providing host name, node name, node source name, rm url.
 *  - if no node within timeout => terminates the script
 *
 * Removal phase:
 * 	- remove node from the resource manager
 *  - launch removal script giving host name and node url.
 *
 */
public class CLIInfrastructure extends InfrastructureManager {

    @Configurable(description = "An interpreter that executes the script")
    protected String interpreter = "bash";

    @Configurable(fileBrowser = true, description = "A script that deploys a node on host (parameters: host, node, ns names and rm url).")
    protected File deploymentScript;

    @Configurable(fileBrowser = true, description = "A script that removes a node (parameters: host name and node url")
    protected File removalScript;

    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts")
    protected File hostsList;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = 60 * 1000;

    @Configurable(description = "Maximum number of failed attempt to deploy on \na host before discarding it")
    protected int maxDeploymentFailure = 5;

    /**
     * list of free hosts; if host AA has a capacity of 2 runtimes, the initial state
     * of this list will contain twice host AA.
     */
    private List<InetAddress> freeHosts = Collections.synchronizedList(new ArrayList<InetAddress>());

    /** Maintains thresholds per hosts to be able to know if the deployment fails and to retry a given number of time */
    private Hashtable<InetAddress, Integer> hostsThresholds = new Hashtable<InetAddress, Integer>();

    /**
     * The set of nodes for which one the registerAcquiredNode has been run.
     */
    private Hashtable<String, InetAddress> registeredNodes = new Hashtable<String, InetAddress>();

    /**
     * To notify the control loop of the deploying node timeout
     */
    private ConcurrentHashMap<String, Boolean> pnTimeout = new ConcurrentHashMap<String, Boolean>();

    /**
     * shut down indicator
     */
    private boolean shutDown = false;

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *			  parameters[0]   : An interpreter that launch the script
     *			  parameters[1]   : A script that deploys nodes on a single host
     *			  parameters[2]   : A script that removes a node
     *			  parameters[3]   : Absolute path of the file containing the list of remote hosts
     *			  parameters[4]   : acquiring timeout
     *			  parameters[5]	  : number of attempt to deploy a node
     * @throws IllegalArgumentException configuration failed
     */
    @Override
    protected void configure(Object... parameters) {
        // TODO super admin rights check
        if (parameters != null && parameters.length >= 6) {
            this.interpreter = parameters[0].toString();

            try {
                byte[] bytes = (byte[]) parameters[1];
                // putting .cmd as an extension so that it works on Windows
                deploymentScript = File.createTempFile("deployment", ".cmd");
                FileToBytesConverter.convertByteArrayToFile(bytes, deploymentScript);
                deploymentScript.setExecutable(true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read deployment script", e);
            }

            try {
                byte[] bytes = (byte[]) parameters[2];
                // putting .cmd as an extension so that it works on Windows
                removalScript = File.createTempFile("removal", ".cmd");
                FileToBytesConverter.convertByteArrayToFile(bytes, removalScript);
                removalScript.setExecutable(true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read removal script file", e);
            }

            try {
                byte[] bytes = (byte[]) parameters[3];
                hostsList = File.createTempFile("hosts", "list");
                FileToBytesConverter.convertByteArrayToFile(bytes, hostsList);
                readHosts(hostsList);
                hostsList.delete();

            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read hosts file", e);
            }

            try {
                this.nodeTimeOut = Integer.parseInt(parameters[4].toString());
            } catch (NumberFormatException e) {
                logger
                        .warn("Number format exception occurred at ns configuration, default acq timeout value set: 60000ms");
                this.nodeTimeOut = 60 * 1000;
            }

            try {
                this.maxDeploymentFailure = Integer.parseInt(parameters[5].toString());
            } catch (NumberFormatException e) {
                logger
                        .warn("Number format exception occurred at ns configuration, default attemp value set: 5");
                this.maxDeploymentFailure = 5;
            }
        }
    }

    /**
     * Acquire one node per available host
     */
    @Override
    public void acquireAllNodes() {
        synchronized (freeHosts) {
            while (freeHosts.size() > 0) {
                acquireNode();
            }
        }
    }

    /**
     * Acquire one node on an available host
     */
    @Override
    public void acquireNode() {
        InetAddress tmpHost = null;
        synchronized (freeHosts) {
            if (freeHosts.size() == 0) {
                logger.warn("Attempting to acquire nodes while all hosts are already deployed.");
                return;
            }
            tmpHost = freeHosts.remove(0);
            logger.debug("Acquiring a new node. #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
        }
        final InetAddress host = tmpHost;
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    startRemoteNode(host);
                    logger.debug("Node acquisition ended. #freeHosts:" + freeHosts.size() + " #registered: " +
                        registeredNodes.size());
                    //node acquisition went well for host so we update the threshold
                    //node acquisition went well for host so we update the threshold
                    synchronized (freeHosts) {
                        hostsThresholds.put(host, maxDeploymentFailure);
                    }
                } catch (Exception e) {
                    synchronized (freeHosts) {
                        Integer tries = hostsThresholds.get(host);
                        tries--;
                        if (tries > 0) {
                            hostsThresholds.put(host, tries);
                            freeHosts.add(host);
                        } else {
                            logger.debug("Tries threshold reached for host " + host +
                                ". This host is not part of the deployment process anymore.");
                        }
                    }
                    String description = "Could not acquire a node on host " + host.toString() +
                        ". NS's state refreshed regarding last checked excpetion: #freeHosts:" +
                        freeHosts.size() + " #registered: " + registeredNodes.size();
                    logger.error(description, e);
                    return;
                }
            }
        });
    }

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using a custom script, register it manually in the
     * nodesource.
     *
     * @param host hostname of the node on which a node should be started
     * @throws RMException acquisition failed
     */
    private void startRemoteNode(InetAddress host) throws RMException {

        final String nodeName = "SCR-" + this.nodeSource.getName() + "-" + ProActiveCounter.getUniqID();
        final String commandLine = interpreter + " " + deploymentScript.getAbsolutePath() + " " +
            host.getHostName() + " " + nodeName + " " + this.nodeSource.getName() + " " + rmUrl;

        final String pnURL = super.addDeployingNode(nodeName, commandLine, "Deploying node on host " + host,
                this.nodeTimeOut);
        this.pnTimeout.put(pnURL, new Boolean(false));

        Process p;
        try {
            logger.debug("Launching the command: " + commandLine);
            p = Runtime.getRuntime().exec(commandLine);
        } catch (IOException e1) {
            super.declareDeployingNodeLost(pnURL, "Cannot run command: " + commandLine + " - " +
                e1.getMessage());
            throw new RMException("Cannot run command: " + commandLine, e1);
        }

        String lf = System.getProperty("line.separator");

        int circuitBreakerThreshold = 5;
        while (!this.pnTimeout.get(pnURL) && circuitBreakerThreshold > 0) {
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
                        CLIInfrastructure.this.declareDeployingNodeLost(pnURL, description);
                    }
                })) {
                    return;
                } else {
                    //there isn't any race regarding node registration
                    throw new RMException("A node " + nodeName +
                        " is not expected anymore because of an error.");
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                //registration is ok, we destroy the process
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

        //if we exit because of a timeout
        if (this.pnTimeout.get(pnURL)) {
            //we remove it
            this.pnTimeout.remove(pnURL);
            //we destroy the process
            p.destroy();
            throw new RMException("Deploying Node " + nodeName + " not expected any more");
        }
        if (circuitBreakerThreshold <= 0) {
            logger.error("Circuit breaker threshold reached while monitoring a child process.");
            throw new RMException("Several exceptions occurred while monitoring a child process.");
        }
    }

    /**
     * Internal host file parser
     * <p>
     * File format:
     * one host per line, optionally followed by a space and an integer describing the maximum
     * number of runtimes (1 if not specified). Example:
     * <pre>
     * example.com
     * example.org 5
     * example.net 3
     * </pre>
     * @param f the file from which hosts names are to be extracted
     * @throws IOException parsing failed
     */
    private void readHosts(File f) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = "";

        while ((line = in.readLine()) != null) {
            if (line == "" || line.trim().length() == 0)
                continue;

            String[] elts = line.split(" ");
            int num = 1;
            if (elts.length > 1) {
                try {
                    num = Integer.parseInt(elts[1]);
                    if (num < 1) {
                        throw new IllegalArgumentException("Cannot launch less than one runtime per host.");
                    }
                } catch (Exception e) {
                    logger.warn("Error while parsing hosts file: " + e.getMessage());
                    num = 1;
                }
            }
            String host = elts[0];
            try {
                InetAddress addr = InetAddress.getByName(host);
                synchronized (this.freeHosts) {
                    for (int i = 0; i < num; i++) {
                        this.freeHosts.add(addr);
                    }
                }
                hostsThresholds.put(addr, maxDeploymentFailure);
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

    /**
     * This method is called by Infrastructure Manager in case of a deploying node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        this.pnTimeout.put(pnURL, new Boolean(true));
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        registeredNodes.put(nodeName, node.getVMInformation().getInetAddress());
        if (logger.isDebugEnabled()) {
            logger.debug("New expected node registered: #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        final String nodeName = node.getNodeInformation().getName();
        final InetAddress host = registeredNodes.get(nodeName);
        if (host != null) {
            freeHosts.add(host);
            logger.debug("Node " + nodeName + " removed from the infrastructure manager. #freeHosts:" +
                freeHosts.size() + " #registered: " + registeredNodes.size());
            final Node n = node;
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
                            String lf = System.getProperty("line.separator");
                            final String description = "Removal script ouput" + lf + "   >Error code: " +
                                exitCode + lf + "   >Errput: " + pErrPut + "   >Output: " + pOutPut;
                            if (exitCode != 0) {
                                logger.error("Child process at " + host.getHostName() +
                                    " exited abnormally (" + exitCode + ").");
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

                    synchronized (registeredNodes) {
                        registeredNodes.remove(nodeName);
                        if (shutDown && registeredNodes.size() == 0) {
                            logger.debug("Deleting the node removal script.");
                            removalScript.delete();
                        }
                    }
                }
            });
        } else {
            logger.error("Node " + nodeName + " is not known as a Node belonging to this " +
                this.getClass().getSimpleName());
        }
    }

    /**
     * @return short description of the IM
     */
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
        shutDown = true;
        deploymentScript.delete();
        hostsList.delete();

        // checking if we need to delete the removal script
        synchronized (registeredNodes) {
            if (registeredNodes.size() == 0 && removalScript.exists()) {
                removalScript.delete();
            }
        }

    }
}
