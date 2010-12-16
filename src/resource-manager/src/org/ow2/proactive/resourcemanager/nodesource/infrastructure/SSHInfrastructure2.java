/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.OperatingSystem;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Acquires nodes over SSH given a list of hosts
 * <p>
 * Assumes all necessary ProActive configuration has already been performed:
 * ssh username, key directory, etc. This class won't handle it,
 * see {@link org.objectweb.proactive.core.ssh.SSHClient}.
 * <p>
 * Also assumes JRE & Scheduling installation paths are identical
 * on all hosts.
 * <p>
 * If you need more control over you deployment, you may consider
 * using {@link GCMInfrastructure} instead, which contains the
 * functionalities of this Infrastructure, but requires more configuration.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 *
 */
public class SSHInfrastructure2 extends InfrastructureManager {

    /**
     * class' logger
     */
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * ShhClient options (@see {@link SSHClient})
     */
    @Configurable(description = "Options for the ssh command\nto log in the remote hosts")
    protected String sshOptions;
    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable(description = "Absolute path of the java\nexecutable on the remote hosts")
    protected String javaPath = System.getProperty("java.home") + "/bin/java";

    /**
     * Use the Java from JAVA_HOME if defined
     */
    {
        String jhome = System.getenv("JAVA_HOME");
        File f = new File(jhome);
        if (f.exists() && f.isDirectory()) {
            javaPath = jhome + ((jhome.endsWith("/")) ? "" : "/") + "bin/java";
        }
    }
    /**
     * Path to the Scheduling installation on the remote hosts
     */
    @Configurable(description = "Absolute path of the Resource Manager (or Scheduler)\nroot directory on the remote hosts")
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * Node acquisition timeout
     */
    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = 60 * 1000;
    @Configurable(description = "The number of failed attempt to deploy\na node before discarding it")
    protected int attempt = 5;
    /**
     * The type of the OS on the remote machine, 'Linux', 'Windows' or 'Cygwin'
     */
    @Configurable(description = "Linux, Cygwin or Windows depending on\nthe operating system of the remote hosts")
    protected String targetOs = "Linux";
    protected OperatingSystem targetOSObj = null;
    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable(description = "Options for the java command\nlaunching the node on the remote hosts")
    protected String javaOptions;
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true, description = "Absolute path of the credential file")
    protected File rmCredentialsPath;
    protected Credentials credentials = null;
    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;
    /**
     * The list of the remote machines to use
     */
    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts")
    protected File hostsList;

    /**
     * list of free hosts; if host AA has a capacity of 2 runtimes, the initial state
     * of this list will contain twice host AA.
     */
    private List<InetAddress> freeHosts = Collections.synchronizedList(new ArrayList<InetAddress>());
    /** Maintains tresholds per hosts to be able to know if the deployment fails and to retry a given number of time */
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
            logger.debug("Acquiring a new SSH Node. #freeHosts:" + freeHosts.size() + " #registered: " +
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
                        hostsThresholds.put(host, attempt);
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
                    String description = "Could not acquire SSH Node on host " + host.toString() +
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
     * Starts a PA runtime on remote host using SSH, register it manually in the
     * nodesource.
     *
     * @param host hostname of the node on which a node should be started
     * @throws RMException acquisition failed
     */
    private void startRemoteNode(InetAddress host) throws RMException {
        String fs = this.targetOSObj.fs;
        CommandLineBuilder clb = super.getDefaultCommandLineBuilder(this.targetOSObj);
        //we take care of spaces in java path
        clb.setJavaPath(this.javaPath);
        //we set the rm.home prop
        clb.setRmHome(schedulingPath);
        //we set the java security policy file
        StringBuilder sb = new StringBuilder();
        sb.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine());
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("config");
        sb.append(fs);
        sb.append("security.java.policy-client ");
        //we set the log4j configuration file
        String log4jcmd = CentralPAPropertyRepository.LOG4J.getCmdLine();
        if (!this.javaOptions.contains(log4jcmd) && targetOSObj.equals(OperatingSystem.CYGWIN)) {
            //especially on cygwin, there is an issue if no log4j configuration is provided
            sb.append(log4jcmd);
            sb.append(schedulingPath);
            sb.append(fs);
            sb.append("config");
            sb.append(fs);
            sb.append("log4j");
            sb.append(fs);
            sb.append("log4j-defaultNode ");
        }
        //we add extra java/PA configuration
        sb.append(this.javaOptions);
        clb.setPaProperties(sb.toString());
        //afterwards, node's name
        // generate the node name
        // current rmcore shortID should be added to ensure uniqueness
        final String nodeName = "SSH-" + this.nodeSource.getName().replace(" ", "_") + "-" +
            ProActiveCounter.getUniqID();
        clb.setNodeName(nodeName);
        //finally, the credential's value
        String credString = null;
        try {
            credString = new String(this.credentials.getBase64());
        } catch (KeyException e1) {
            throw new RMException("Could not get base64 credentials", e1);
        }
        clb.setCredentialsValueAndNullOthers(credString);

        //add an expected node. every unexpected node will be discarded
        String cmdLine;
        try {
            cmdLine = clb.buildCommandLine();
        } catch (IOException e2) {
            throw new RMException("Cannot build the " + RMNodeStarter.class.getSimpleName() +
                "'s command line.", e2);
        }

        //one escape the command to make it runnable through ssh
        if (cmdLine.contains("\"")) {
            cmdLine = cmdLine.replaceAll("\"", "\\\\\"");
        }

        //we create a new deploying node before ssh command ran
        final String pnURL = super.addDeployingNode(nodeName, cmdLine, "Deploying node on host " + host,
                this.nodeTimeOut);
        this.pnTimeout.put(pnURL, new Boolean(false));

        Process p = null;
        try {
            p = Utils.runSSHCommand(host, cmdLine, sshOptions);
        } catch (IOException e1) {
            super.declareDeployingNodeLost(pnURL, "Cannot run command: " + cmdLine + ", with ssh options: " +
                sshOptions + " - " + e1.getMessage());
            throw new RMException("Cannot run command: " + cmdLine + ", with ssh options: " + sshOptions, e1);
        }

        String lf = System.getProperty("line.separator");

        int circuitBreakerThreshold = 5;
        while (!this.pnTimeout.get(pnURL) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    logger.error("SSH subprocess at " + host.getHostName() + " exited abnormally (" +
                        exitCode + ").");
                } else {
                    logger.error("Launching node process has exited normally whereas it shouldn't.");
                }
                String pOutPut = extractProcessOutput(p);
                String pErrPut = extractProcessErrput(p);
                final String description = "SSH command failed to launch node on host " + host.getHostName() +
                    lf + "   >Error code: " + exitCode + lf + "   >Errput: " + pErrPut + "   >Output: " +
                    pOutPut;
                logger.error(description);
                if (super.checkNodeIsAcquiredAndDo(nodeName, null, new Runnable() {
                    public void run() {
                        SSHInfrastructure2.this.declareDeployingNodeLost(pnURL, description);
                    }
                })) {
                    return;
                } else {
                    //there isn't any race regarding node registration
                    throw new RMException("SSH Node " + nodeName +
                        " is not expected anymore because of an error.");
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                //registration is ok, we destroy the process if it is not running localy
                this.destroyProcessIfRemote(p, host);
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                circuitBreakerThreshold--;
                logger.trace("An exception occurred while monitoring ssh subprocess", e);
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
            logger.error("Circuit breaker threshold reached while monitoring ssh subprocess.");
            throw new RMException("Several exceptions occurred while monitoring ssh subprocess.");
        }
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *			  parameters[0]   : ssh Options, see {@link SSHClient}
     *			  parameters[1]   : java path on the remote machines
     *			  parameters[2]   : Scheduling path on remote machines
     *			  parameters[3]   : acq timeout
     *			  parameters[4]	  : number of attempt to deploy a node
     *			  parameters[5]   : target OS' type (Linux, Windows or Cygwin)
     *            parameters[6]   : extra java options
     *            parameters[7]   : rm cred
     *            parameters[8]   : host list file
     * @throws IllegalArgumentException configuration failed
     */
    @Override
    public void configure(Object... parameters) {
        if (parameters != null && parameters.length >= 9) {
            this.sshOptions = parameters[0].toString();
            this.javaPath = parameters[1].toString();
            if (this.javaPath == null || this.javaPath.equals("")) {
                throw new IllegalArgumentException("A valid Java path must be supplied.");
            }
            this.schedulingPath = parameters[2].toString();
            try {
                this.nodeTimeOut = Integer.parseInt(parameters[3].toString());
            } catch (NumberFormatException e) {
                logger
                        .warn("Number format exception occurred at ns configuration, default acq timeout value set: 60000ms");
                this.nodeTimeOut = 60 * 1000;
            }

            try {
                this.attempt = Integer.parseInt(parameters[4].toString());
            } catch (NumberFormatException e) {
                logger
                        .warn("Number format exception occurred at ns configuration, default attemp value set: 5");
                this.attempt = 5;
            }
            //target OS
            if (parameters[5] != null) {
                this.targetOSObj = OperatingSystem.getOperatingSystem(parameters[5].toString());
                if (this.targetOSObj == null) {
                    throw new IllegalArgumentException(
                        "Only 'Linux', 'Windows' and 'Cygwin' are valid values for Target OS Property.");
                }
            } else {
                throw new IllegalArgumentException("Target OS parameter cannot be null");
            }

            this.javaOptions = parameters[6].toString();

            //credentials
            if (parameters[7] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            try {
                this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[7]);
            } catch (KeyException e) {
                throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
            }

            //host list
            if (parameters[8] == null) {
                throw new IllegalArgumentException("Host file must be specified");
            }
            try {
                byte[] hosts = (byte[]) parameters[8];
                File f = File.createTempFile("hosts", "list");
                FileToBytesConverter.convertByteArrayToFile(hosts, f);
                readHosts(f);
                f.delete();
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read hosts file", e);
            }
        } else {
            throw new IllegalArgumentException("Invalid parameters for infrastructure creation");
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
                hostsThresholds.put(addr, attempt);
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

    /**
     * Extracts remote process errput and returns it
     * @param p the remote process frow which one errput will be extracted.
     * @return the remote process' errput
     */
    private String extractProcessErrput(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            String lf = System.getProperty("line.separator");
            while (br.ready()) {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(lf);
                }
            }
        } catch (IOException e) {
            sb.append("Cannot extract process errput");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.debug("Cannot close process error stream", e);
            }
        }
        return sb.toString();
    }

    /**
     * Extracts remote process output and returns it
     * @param p the remote process frow which one output will be extracted.
     * @return the remote process' output
     */
    private String extractProcessOutput(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            String lf = System.getProperty("line.separator");
            while (br.ready()) {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(lf);
                }
            }
        } catch (IOException e) {
            sb.append("Cannot extract process output");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.debug("Cannot close process output stream", e);
            }
        }
        return sb.toString();
    }

    /**
     * Destroys the process only if it runs on a remote host
     * (ie. kills the SSH process)
     * @param p the process to kill if remote
     * @param host the host on which one the process is running.
     */
    private void destroyProcessIfRemote(Process p, InetAddress host) {
        boolean isRemote = true;
        try {
            isRemote = !host.equals(InetAddress.getLocalHost()) &&
                !host.equals(InetAddress.getByName("127.0.0.1"));
        } catch (UnknownHostException e) {
            logger.trace("A problem occurred while determining if the ssh command is running on localhost.",
                    e);
        }
        if (isRemote) {
            p.destroy();
        }
    }

    /**
     * This method is called by Infrastructure Manager in case of a deploying node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void registerRemovedDeployingNode(String pnURL) {
        this.pnTimeout.put(pnURL, new Boolean(true));
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void registerAcquiredNode(Node node) throws RMException {
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
        InetAddress host = null;
        String nodeName = node.getNodeInformation().getName();
        if ((host = registeredNodes.remove(nodeName)) != null) {
            freeHosts.add(host);
            logger.debug("Node " + nodeName + " removed. #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
            final Node n = node;
            this.nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    try {
                        n.getProActiveRuntime().killRT(false);
                    } catch (Exception e) {
                        logger.trace("An exception occurred during node removal", e);
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
        return "Creates remote runtimes using SSH";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SSH Infrastructure 2";
    }

    @Override
    public void shutDown() {
        this.shutdown = true;
    }
}
