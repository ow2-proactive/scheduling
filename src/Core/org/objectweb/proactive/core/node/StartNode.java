/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.node;

import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * This class is a utility class allowing to start a ProActive node with a JVM.
 * It is very useful to start a node on a given host that will receive later
 * active objects created by other distributed applications.
 * </p><p>
 * This class has a main method and can be used directly from the java command.
 * <br>
 * use<br>
 * &nbsp;&nbsp;&nbsp;java org.objectweb.proactive.core.node.StartNode<br>
 * to print the options from command line or see the java doc of the main method.
 * </p><p>
 * A node represents the minimum services ProActive needs to work with a remote JVM.
 * Any JVM that is going to interact with active objects has at least one associated
 * node. The node must have a remote implementation that allow an object to remotely
 * invoke its methods.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class StartNode {
    public static final int DEFAULT_CLASSFILE_SERVER_PORT = 2001;
    static Logger logger;
    protected static final int DEFAULT_PORT = 1099;
    protected static final int MAX_RETRY = 3;
    protected static final String NO_REBIND_OPTION_NAME = "-noRebind";
    protected static final String NO_CLASS_SERVER_OPTION_NAME = "-noClassServer";
    protected static final String NO_REGISTRY_OPTION_NAME = "-noRegistry";
    protected static final String MULTICAST_LOCATOR_NAME = "-multicastLocator";

    static {
        ProActiveConfiguration.load();
        logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading ProActive class");
        }

        try {
            Class.forName("org.objectweb.proactive.ProActive");
        } catch (ClassNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.fatal("Loading of ProActive class FAILED");
            }

            e.printStackTrace();
            System.exit(1);
        }
    }

    protected boolean noClassServer = false;
    protected boolean noRebind = false;
    protected boolean noRegistry = false;
    protected boolean multicastLocator = false;
    protected int registryPortNumber = DEFAULT_PORT;
    protected String classpath;
    protected String nodeURL;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected StartNode() {
    }

    private StartNode(String[] args) {
        if (args.length == 0) {
            nodeURL = null;
            printUsage();
        } else {
            nodeURL = args[0];
            registryPortNumber = URIBuilder.getPortNumber(nodeURL);
            checkOptions(args, 1);
            readClassPath(args, 1);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void main(String[] args) {
        try {
            new StartNode(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(e.toString());
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void checkOptions(String[] args, int start) {
        for (int i = start; i < args.length; i++)
            checkOption(args[i]);
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Reads the classpath from the arguments
     */
    protected void readClassPath(String[] args, int start) {
        if (noClassServer) {
            return;
        }

        // look for classpath
        for (int i = start; i < args.length; i++) {
            String s = args[i];

            if (s.charAt(0) != '-') {
                classpath = s;

                break;
            }
        }
    }

    protected void createNode(String nodeURL, boolean noRebind)
        throws NodeException, AlreadyBoundException {
        int exceptionCount = 0;

        while (true) {
            try {
                Node node = null;

                if (nodeURL == null) {
                    node = NodeFactory.getDefaultNode();
                } else {
                    node = NodeFactory.createNode(nodeURL, !noRebind, null,
                            null, null);
                }

                logger.info("OK. Node " + node.getNodeInformation().getName() +
                    " ( " + node.getNodeInformation().getURL() + " ) " +
                    " is created in VM id=" + UniqueID.getCurrentVMID());

                break;
            } catch (NodeException e) {
                exceptionCount++;

                if (exceptionCount == MAX_RETRY) {
                    throw e;
                } else {
                    logger.error("Error, retrying (" + exceptionCount + ")");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                }

                // end if
            }

            // try
        }

        // end while
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Run the complete creation of the node step by step by invoking the other
     * helper methods
     */
    protected void run()
        throws java.io.IOException, NodeException, AlreadyBoundException {
        // create node
        createNode(nodeURL, noRebind);
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Checks one given option from the arguments
     */
    protected void checkOption(String option) {
        if (NO_REBIND_OPTION_NAME.equals(option)) {
            noRebind = true;
        } else if (NO_CLASS_SERVER_OPTION_NAME.equals(option)) {
            noClassServer = true;
        } else if (NO_REGISTRY_OPTION_NAME.equals(option)) {
            noRegistry = true;
        } else if (MULTICAST_LOCATOR_NAME.equals(option)) {
            multicastLocator = true;
        } else {
            printUsage();
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void printUsage() {
        String localhost = "localhost";

        try {
            localhost = URIBuilder.getHostNameorIP(ProActiveInet.getInstance()
                                                                .getInetAddress());
        } catch (java.lang.SecurityException e) {
            logger.error("InetAddress failed: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("usage: java " + this.getClass().getName() +
            " <node URL> [options]");
        logger.info(" - options");
        logger.info("     " + NO_CLASS_SERVER_OPTION_NAME +
            " : indicates not to create a ClassServer for RMI.");
        logger.info(
            "                      By default a ClassServer is automatically created");
        logger.info("                      to serve class files on demand.");
        logger.info("     " + NO_REBIND_OPTION_NAME +
            "      : indicates not to use rebind when registering the");
        logger.info(
            "                      node to the RMIRegistry. If a node of the same name");
        logger.info(
            "                      already exists, the creation of the new node will fail.");
        logger.info("  for instance: java " + StartNode.class.getName() + " " +
            Constants.RMI_PROTOCOL_IDENTIFIER + "://" + localhost + "/node1");
        logger.info("                java " + StartNode.class.getName() + " " +
            Constants.RMI_PROTOCOL_IDENTIFIER + "://" + localhost + "/node2  " +
            NO_CLASS_SERVER_OPTION_NAME + " " + NO_REBIND_OPTION_NAME);
    }
}
