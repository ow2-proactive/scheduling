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
import org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
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
    @Configurable
    protected String sshOptions;
    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable
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
    @Configurable
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * Node acquisition timeout
     */
    @Configurable(description = "in ms")
    protected int nodeTimeOut = 60 * 1000;
    /**
     * The type of the OS on the remote machine, 'Linux', 'Windows' or 'Cygwin'
     */
    @Configurable
    protected String targetOs = "Linux";
    protected OperatingSystem targetOSObj = null;
    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable
    protected String javaOptions;
    /**
     * The rm's url
     */
    @Configurable
    protected String rmUrl;
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true)
    protected File rmCredentialsPath;
    protected Credentials credentials = null;
    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;
    /**
     * The list of the remote machines to use
     */
    @Configurable(fileBrowser = true)
    protected File hostsList;

    /**
     * list of free hosts; if host AA has a capacity of 2 runtimes, the initial state
     * of this list will contain twice host AA.
     */
    private List<InetAddress> freeHosts = Collections.synchronizedList(new ArrayList<InetAddress>());

    /**
     * The set of nodes for which one the registerAcquiredNode has been run.
     */
    private Hashtable<String, InetAddress> registeredNodes = new Hashtable<String, InetAddress>();

    /**
     * expected nodes. safe check... If a node is not expected, its registration must be discarded
     */
    private Hashtable<String, InetAddress> expectedNodes = new Hashtable<String, InetAddress>();

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
                registeredNodes.size() + " #expected: " + expectedNodes.size() + " (#expected not accurate)");
        }
        final InetAddress host = tmpHost;
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    startRemoteNode(host);
                    logger.debug("Node acquisition ended. #freeHosts:" + freeHosts.size() + " #registered: " +
                        registeredNodes.size() + " #expected: " + expectedNodes.size() +
                        " (#expected not accurate)");
                } catch (Exception e) {
                    freeHosts.add(host);
                    String description = "Could not acquire SSH Node on host " + host.toString() +
                        ". NS's state refreshed regarding last checked excpetion: #freeHosts:" +
                        freeHosts.size() + " #registered: " + registeredNodes.size() + " #expected: " +
                        expectedNodes.size() + " (#expected not accurate)";
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
        StringBuilder sb = new StringBuilder();
        String ps = this.targetOSObj.ps;
        String fs = this.targetOSObj.fs;
        sb.append(this.javaPath);
        sb.append(" ");
        //we set the rm.home prop
        sb.append(PAResourceManagerProperties.RM_HOME.getCmdLine());
        sb.append(schedulingPath);
        sb.append(" ");
        //we set the java security policy file
        sb.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine());
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("config");
        sb.append(fs);
        sb.append("security.java.policy-client ");
        //we set the log4j configuration file
        String log4jcmd = CentralPAPropertyRepository.LOG4J.getCmdLine();
        if (!this.javaOptions.contains(log4jcmd) && targetOSObj.equals(OperatingSystem.CYGWIN)) {
            sb.append(log4jcmd);
            sb.append(schedulingPath);
            sb.append(fs);
            sb.append("config");
            sb.append(fs);
            sb.append("log4j");
            sb.append(fs);
            sb.append("defaultNode-log4j ");
        }
        //we add extra java/PA configuration
        sb.append(this.javaOptions);
        //we build classpath
        sb.append(" -cp ");
        if (targetOSObj.equals(OperatingSystem.CYGWIN)) {
            sb.append("\\\"");//especially on cygwin, we need to quote the cp
        }
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("jython-engine.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("script-js.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("jruby-engine.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("ProActive.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("ProActive_ResourceManager.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("ProActive_Scheduler-worker.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("ProActive_SRM-common.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("commons-logging-1.1.1.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("commons-httpclient-3.1.jar");
        sb.append(ps);
        sb.append(schedulingPath);
        sb.append(fs);
        sb.append("dist");
        sb.append(fs);
        sb.append("lib");
        sb.append(fs);
        sb.append("commons-codec-1.3.jar");
        sb.append(ps);
        sb.append(".");
        if (targetOSObj.equals(OperatingSystem.CYGWIN)) {
            sb.append("\\\"");//especially on cygwin, we need to quote the cp
        }
        sb.append(" ");
        //we set the executable's name
        sb.append(PAAgentServiceRMStarter.class.getName());

        //now we set PAAgentServiceRMStarter parameters
        //first rm's url
        sb.append(" -r ");
        sb.append(this.rmUrl);
        //afterwards, node's name
        // generate the node name
        // current rmcore shortID should be added to ensure uniqueness
        String nodeName = "SSH-" + this.nodeSource.getName().replace(" ", "_") + "-" +
            ProActiveCounter.getUniqID();
        sb.append(" -n ");
        sb.append(nodeName);
        //the nodesource's name
        sb.append(" -s ");
        sb.append(this.nodeSource.getName());
        //finally, the credential's value
        String credString = null;
        try {
            credString = new String(this.credentials.getBase64()) + " ";
        } catch (KeyException e1) {
            throw new RMException("Could not get base64 credentials", e1);
        }
        sb.append(" -v ");
        sb.append(credString);

        //add an expected node. every unexpected node will be discarded
        expectedNodes.put(nodeName, host);

        String cmdLine = sb.toString();
        Process p = null;
        try {
            p = Utils.runSSHCommand(host, cmdLine, sshOptions);
        } catch (IOException e1) {
            throw new RMException("Cannot run command: " + cmdLine + ", with ssh options: " + sshOptions, e1);
        }

        String lf = System.getProperty("line.separator");

        long t1 = System.currentTimeMillis();
        int circuitBreakerThreshold = 5;
        while (true && circuitBreakerThreshold > 0) {
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
                String description = "SSH command failed to launch node on host " + host.getHostName() + lf +
                    "Error code: " + exitCode + lf + "Errput: " + pErrPut + lf + "Output: " + pOutPut;
                logger.error(description);
                synchronized (expectedNodes) {
                    if (expectedNodes.remove(nodeName) != null) {
                        //there isn't any race regarding node registration
                        throw new RMException("SSH Node " + nodeName +
                            " is not expected anymore because of an error.");
                    } else {
                        if (registeredNodes.containsKey(nodeName)) {
                            //ok, we reached a correct state
                            //no way to destroy the process, we get its exit code...
                            return;
                        } else {
                            throw new RMException("Invalid state, node " + nodeName +
                                " seems to be registered but is not found.");
                        }
                    }
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (registeredNodes.containsKey(nodeName)) {
                //registration is ok, we destroy the process if it is not running localy
                this.destroyProcessIfRemote(p, host);
                return;
            }

            long t2 = System.currentTimeMillis();
            if (t2 - t1 > this.nodeTimeOut || shutdown) {
                synchronized (expectedNodes) {
                    if (expectedNodes.remove(nodeName) != null) {
                        //there isn't any race regarding node registration
                        p.destroy();
                        String pErrPut = extractProcessErrput(p);
                        String pOutPut = extractProcessOutput(p);
                        String description = "SSH command timed out for node " + nodeName + " on host " +
                            host.getHostName() + lf + "Output: " + pOutPut + lf + "Errput: " + pErrPut;
                        logger.error(description);
                        throw new RMException("Timeout occured for node " + nodeName + " on host " +
                            host.getHostAddress());
                    } else {
                        if (registeredNodes.containsKey(nodeName)) {
                            //ok, we reached a correct state
                            this.destroyProcessIfRemote(p, host);
                            return;
                        } else {
                            throw new RMException("Invalid state, node " + nodeName +
                                " seems to be registered but is not found.");
                        }
                    }
                }
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                circuitBreakerThreshold--;
                logger.trace("An exception occurred while monitoring ssh subprocess", e);
            }
        }
        if (circuitBreakerThreshold <= 0) {
            logger.error("Circuit breaker threshold reached while monitoring ssh subprocess.");
            throw new RMException("Several xceptions occurred while monitoring ssh subprocess.");
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
     *			  parameters[4]   : target OS' type (Linux, Windows or Cygwin)
     *            parameters[5]   : extra java options
     *            parameters[6]   : rm url
     *            parameters[7]   : rm cred
     *            parameters[8]   : host list file
     * @throws IllegalArgumentException configuration failed
     */
    @Override
    public BooleanWrapper configure(Object... parameters) {
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

            //target OS
            if (parameters[4] != null) {
                this.targetOSObj = OperatingSystem.getOperatingSystem(parameters[4].toString());
                if (this.targetOSObj == null) {
                    throw new IllegalArgumentException(
                        "Only 'Linux', 'Windows' and 'Cygwin' are valid values for Target OS Property.");
                }
            } else {
                throw new IllegalArgumentException("Target OS parameter cannot be null");
            }

            this.javaOptions = parameters[5].toString();
            this.rmUrl = parameters[6].toString();
            if (this.rmUrl.equals("")) {
                throw new IllegalArgumentException("rmUrl must be specified");
            }

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
        return new BooleanWrapper(true);
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
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

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

    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        synchronized (expectedNodes) {
            InetAddress host = expectedNodes.remove(nodeName);
            if (host != null) {
                registeredNodes.put(nodeName, host);
                logger.debug("New expected node registered: #freeHosts:" + freeHosts.size() +
                    " #registered: " + registeredNodes.size() + " #expected: " + expectedNodes.size() +
                    " (#expected not accurate)");
            } else {
                logger.debug("Non expected node not registered: " + nodeName);
                throw new RMException("Node " + nodeName + " not expected. Rejecting it.");
            }
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
                registeredNodes.size() + " #expected: " + expectedNodes.size() + " (#expected not accurate)");
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
        return "SSH Infrastructure";
    }

    @Override
    public void shutDown() {
        this.shutdown = true;
    }

    /*####################################
     * Helpers
     *###################################*/
    /**
     * Private inner enum which represents supported operating systems
     */
    private enum OperatingSystem {
        WINDOWS(";", "\\\\"), LINUX(":", "/"), CYGWIN(";", "/");
        private String ps, fs;

        private OperatingSystem(String ps, String fs) {
            this.fs = fs;
            this.ps = ps;
        }

        /**
         * Returns the operating system corresponding to the provided String parameter: 'LINUX', 'WINDOWS' or 'CYGWIN'
         * @param desc one of 'LINUX', 'WINDOWS' or 'CYGWIN'
         * @return the appropriate Operating System
         */
        private static OperatingSystem getOperatingSystem(String desc) {
            if (desc == null) {
                throw new IllegalArgumentException("String description of operating system cannot be null");
            }
            desc = desc.toUpperCase();
            if ("LINUX".equals(desc)) {
                return OperatingSystem.LINUX;
            }
            if ("WINDOWS".equals(desc)) {
                return OperatingSystem.WINDOWS;
            }
            if ("CYGWIN".equals(desc)) {
                return OperatingSystem.CYGWIN;
            }
            return null;
        }
    }
}
