package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.rm.util.process.ProcessTreeKiller;
import org.ow2.proactive.utils.Formatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyException;
import java.util.Properties;


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

    @Configurable(description = "Command that will be launched for every host")
    protected String command =
            "scp -o StrictHostKeyChecking=no ${pa.rm.home}/dist/war/rest/node.jar ${host.name}:/tmp/${node.name}.jar && " +
            "ssh -o StrictHostKeyChecking=no ${host.name} " +
            "\"${java.home}/bin/java -jar /tmp/${node.name}.jar -v ${nodesource.credentials} -n ${node.name} -s ${nodesource.name} -p 30000 -r ${rm.url} " +
            "1>>/tmp/${node.name}.log 2>&1\"";

    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        if (parameters != null && parameters.length >= 3) {
            this.command = parameters[3].toString();
        }
    }

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using a custom script, register it manually in the
     * nodesource.
     *
     * @param host hostname of the node on which a node should be started
     * @throws org.ow2.proactive.resourcemanager.exception.RMException acquisition failed
     */
    protected void startNodeImpl(InetAddress host) throws RMException {

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

        String filledCommand = replaceProperties(command, localProperties);
        filledCommand = replaceProperties(filledCommand, System.getProperties());

        final String pnURL = super.addDeployingNode(nodeName, filledCommand, "Deploying node on host " + host,
                this.nodeTimeOut);
        this.pnTimeout.put(pnURL, new Boolean(false));

        Process p;
        try {
            logger.debug("Deploying node: " + nodeName);
            logger.debug("Launching the command: " + filledCommand);
            p = Runtime.getRuntime().exec(new String[] {"bash", "-c", filledCommand});
        } catch (IOException e1) {
            super.declareDeployingNodeLost(pnURL, "Cannot run command: " + filledCommand +
                " - \n The following exception occurred: " + Formatter.stackTraceToString(e1));
            throw new RMException("Cannot run command: " + filledCommand, e1);
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
                        AutoUpdateInfrastructure.this.declareDeployingNodeLost(pnURL, description);
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
                ProcessTreeKiller.get().kill(p);
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

    private String replaceProperties(String commandLine, Properties properties) {

        for(Object prop: properties.keySet()) {
            String propName = "${" + prop + "}";
            String propValue = properties.getProperty(prop.toString());
            if (propValue!=null) {
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
