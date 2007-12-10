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
package org.objectweb.proactive.p2p.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Jan 4, 2005
 */
@PublicAPI
public class StartP2PService implements P2PConstants {
    // TODO Disable node sharing when starting a p2p sevice
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_STARTSERVICE);

    static {
        ProActiveConfiguration.load();
    }

    // -------------------------------------------------------------------------
    // Tools for handling command line arguments
    // -------------------------------------------------------------------------
    private static final String USAGE = "java " +
        StartP2PService.class.getName() + " [-acq acquisitionMethod]" +
        " [-port portNumber]" + " [-s Peer ...] [-f PeerListFile]\n" +
        "More options:\n" + " -noa NOA is in number of peers\n" +
        " -ttu TTU is in minutes\n" + " -ttl TTL is in hop\n" +
        " -capacity List capacity of message sequence number\n" +
        " -exploring Percentage of agree response to determine response to exploring messag\n" +
        " -booking Expiration time for booking without use a shared node\n" +
        " -node_acq Timeout for node acquisition\n" +
        " -lookup Lookup frequency for nodes\n" +
        " -no_multi_proc_nodes to share only a node. Otherwise, 1 node by CPU\n" +
        " -xml_path Deployment descriptor path\n" +
        " -no_sharing Start the service with none shared nodes";

    // -------------------------------------------------------------------------
    // Class Fileds
    //--------------------------------------------------------------------------
    private Vector peers = new Vector();
    private P2PService p2pService = null;

    // -------------------------------------------------------------------------
    // Class Constructors
    // -------------------------------------------------------------------------
    private StartP2PService(Args parsed) {
        // Adding peers from command line
        this.peers.addAll(parsed.peers);

        // Adding peers from file
        if (parsed.peerListFile != null) {
            this.peerFileParser(parsed.peerListFile);
        }
    }

    /**
     * <p>Construct a new <code>StartP2PService</code> .</p>
     * <p>Acquisition method and port number for the ProActive Runtime are specified
     * by ProActive Java system properties:</p>
     * <ul>
     *         <li><code>proactive.p2p.acq</code>: acquisition method.</li>
     *         <li><code>proactive.p2p.port</code>: port number.</li>
     * </ul>
     */
    public StartP2PService() {
        // nothing to do
    }

    /**
     * <p>Construct a new <code>StartP2PService</code> with a list of peers for first
     * contact.</p>
     * <p>Acquisition method and port number for the ProActive Runtime are specified
     * by ProActive Java system properties:</p>
     * <ul>
     *         <li><code>proactive.p2p.acq</code>: acquisition method.</li>
     *         <li><code>proactive.p2p.port</code>: port number.</li>
     * </ul>
     * @param peerListFile a file with a list of peers.
     */
    public StartP2PService(String peerListFile) {
        this.peerFileParser(peerListFile);
    }

    /**
     *<p>Construct a new <code>StartP2PService</code> with a list of peers for first
     * contact.</p>
     * <p>Acquisition method and port number for the ProActive Runtime are specified
     * by ProActive Java system properties:</p>
     * <ul>
     *         <li><code>proactive.p2p.acq</code>: acquisition method.</li>
     *         <li><code>proactive.p2p.port</code>: port number.</li>
     * </ul>
     * @param peers list of peers.
     */
    public StartP2PService(Vector peers) {
        if (peers != null) {
            this.peers.addAll(peers);
        }
    }

    /**
     * Print <code>msg</code> in error output.
     * @param msg the message or <code>null</code>.
     */
    private static void usage(String msg) {
        if (msg != null) {
            System.err.println(msg);
        }

        System.err.println("Usage:\n" + USAGE);
        System.exit(1);
    }

    /**
     * Parsing command line arguments.
     * @param args Arguments from command line.
     * @return an <code>Args</code> object with arguments from command line.
     */
    private static Args parseArgs(String[] args) {
        Args parsed = new Args();
        int index = 0;

        while (index < args.length) {
            String argname = args[index];

            if ("-acq".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.acquisitionMethod = args[index];
                index++;

                continue;
            } else if ("-port".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.portNumber = args[index];
                index++;

                continue;
            } else if ("-s".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.peers.add(args[index]);
                index++;

                while ((index < args.length) && !args[index].startsWith("-")) {
                    parsed.peers.add(args[index]);
                    index++;
                }

                continue;
            } else if ("-f".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.peerListFile = args[index];
                index++;

                continue;
            } else if ("-noa".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.noa = args[index];
                index++;

                continue;
            } else if ("-ttu".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.ttu = args[index];
                index++;

                continue;
            } else if ("-ttl".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.ttl = args[index];
                index++;

                continue;
            } else if ("-capacity".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.msg_capacity = args[index];
                index++;

                continue;
            } else if ("-exploring".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.expl_msg = args[index];
                index++;

                continue;
            } else if ("-node_acq".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.nodes_acq_to = args[index];
                index++;

                continue;
            } else if ("-lookup".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.lookup_freq = args[index];
                index++;

                continue;
            } else if ("-xml_path".equalsIgnoreCase(argname)) {
                index++;

                if (index > args.length) {
                    usage(argname);
                }

                parsed.xml_path = args[index];
                index++;

                continue;
            } else if ("-no_multi_proc_nodes".equalsIgnoreCase(argname)) {
                parsed.multi_proc_nodes = "false";
                index++;

                continue;
            } else if ("-no_sharing".equalsIgnoreCase(argname)) {
                parsed.no_sharing = "true";
                index++;

                continue;
            }

            usage("Unknow argumnent " + argname);
        }

        return parsed;
    }

    /**
     * Init P2P java system properties with defauld value or specified value by
     * command line.
     * @param parsed Arguments from command line.
     */
    private static void initP2PProperties(Args parsed) {
        ProProperties.PA_P2P_ACQUISITION.setValue(parsed.acquisitionMethod);
        ProProperties.PA_P2P_PORT.setValue(parsed.portNumber);
        ProProperties.PA_P2P_NOA.setValue(parsed.noa);
        ProProperties.PA_P2P_TTU.setValue(parsed.ttu);
        ProProperties.PA_P2P_TTL.setValue(parsed.ttl);
        ProProperties.PA_P2P_MSG_MEMORY.setValue(parsed.msg_capacity);
        ProProperties.PA_P2P_EXPLORING_MSG.setValue(parsed.expl_msg);
        ProProperties.PA_P2P_NODES_ACQUISITION_T0.setValue(parsed.nodes_acq_to);
        ProProperties.PA_P2P_LOOKUP_FREQ.setValue(parsed.lookup_freq);
        ProProperties.PA_P2P_MULTI_PROC_NODES.setValue(parsed.multi_proc_nodes);

        if (parsed.xml_path != null) {
            ProProperties.PA_P2P_XML_PATH.setValue(parsed.xml_path);
        }

        if (parsed.no_sharing == null) {
            ProProperties.PA_P2P_NO_SHARING.setValue(ProProperties.FALSE);
        } else {
            ProProperties.PA_P2P_NO_SHARING.setValue(parsed.no_sharing);
        }
    }

    // -------------------------------------------------------------------------
    // Main method
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        // Parsing command line
        Args parsed = parseArgs(args);

        // Init system properties with new value
        initP2PProperties(parsed);

        // For Debbugging
        try {
            logger.info("**** Starting jvm on " +
                URIBuilder.getHostNameorIP(URIBuilder.getLocalAddress()));
        } catch (UnknownHostException e) {
            logger.warn("Couldn't get local host name", e);
        }

        // NO REMOVE the isDebugEnabled test
        if (logger.isDebugEnabled()) {
            logger.debug("**** Starting jvm with classpath " +
                System.getProperty("java.class.path"));
            logger.debug("****              with bootclasspath " +
                System.getProperty("sun.boot.class.path"));
            logger.debug("Acquisition method: " +
                ProProperties.PA_P2P_ACQUISITION.getValue());
            logger.debug("Port number: " +
                ProProperties.PA_P2P_PORT.getValue());

            if (parsed.peerListFile != null) {
                logger.debug("Peer list file: " + parsed.peerListFile);
            }

            if (parsed.peers.size() != 0) {
                String servers = "";

                for (int i = 0; i < parsed.peers.size(); i++) {
                    servers += (" " + parsed.peers.get(i));
                }

                logger.debug("Peers:" + servers);
            }
        }

        StartP2PService service = new StartP2PService(parsed);

        try {
            // Start the ProActive P2P Service
            service.start();
        } catch (ProActiveException e) {
            logger.fatal("Failed to active the P2P service", e);
        }
    }

    // -------------------------------------------------------------------------
    // Class methods
    // -------------------------------------------------------------------------
    private void peerFileParser(String fileURL) {
        try {
            FileReader serverList = new FileReader(fileURL);
            BufferedReader in = new BufferedReader(serverList);
            String line;

            while ((line = in.readLine()) != null) {
                this.peers.add(line);
            }

            in.close();
        } catch (FileNotFoundException e) {
            logger.warn("Couldn't open peer list file", e);
        } catch (IOException e) {
            logger.warn("Problem appear during treating peer list file", e);
        }
    }

    /**
     * Add acquisition method and port number to URL of peers.
     */
    public static Vector checkingPeersUrl(Vector peerList) {
        int nbUrls = peerList.size();
        Vector newPeerList = new Vector(nbUrls);

        for (int i = 0; i < nbUrls; i++) {
            String url = (String) peerList.get(i);

            if (url.indexOf("//") < 0) {
                url = ProProperties.PA_P2P_ACQUISITION.getValue() + "://" +
                    url;
            }

            if (!url.matches(".*:[0-9]+.*")) {
                url += (":" + ProProperties.PA_P2P_PORT.getValue());
            }

            newPeerList.add(url);
        }

        return newPeerList;
    }

    /**
     * <p>Start a new active ProActive P2P service.</p>
     * <p><b>Warning: </b> it's not a thread.</p>
     * @throws ProActiveException
     */
    public void start() throws ProActiveException {
        // Cleanning peers URL
        this.peers = StartP2PService.checkingPeersUrl(this.peers);

        // Starting new Active P2P Service
        String acquisitionMethod = ProProperties.PA_P2P_ACQUISITION.getValue();
        String portNumber = ProProperties.PA_P2P_PORT.getValue();

        // Keep previous port value
        String bckPortValue = null;

        if (!acquisitionMethod.equals(Constants.IBIS_PROTOCOL_IDENTIFIER)) {
            bckPortValue = ProProperties.PA_P2P_PORT.getValue();
            System.setProperty("proactive." + acquisitionMethod + ".port",
                portNumber);
        } else {
            bckPortValue = ProProperties.PA_RMI_PORT.getValue();
            System.setProperty("proactive.rmi.port", portNumber);
        }

        // ProActiveRuntime creation
        ProActiveRuntime paRuntime = RuntimeFactory.getProtocolSpecificRuntime(acquisitionMethod);

        // Set property port with previous value
        if (bckPortValue != null) {
            if (!acquisitionMethod.equals(Constants.IBIS_PROTOCOL_IDENTIFIER)) {
                System.setProperty("proactive." + acquisitionMethod + ".port",
                    bckPortValue);
            } else {
                System.setProperty("proactive.rmi.port", bckPortValue);
            }
        }

        // Node Creation
        String url = null;

        try {
            url = paRuntime.createLocalNode(URIBuilder.buildURI("localhost",
                        P2PConstants.P2P_NODE_NAME, acquisitionMethod,
                        Integer.parseInt(portNumber)).toString(), false, null,
                    paRuntime.getVMInformation().getName(), Job.DEFAULT_JOBID);
        } catch (AlreadyBoundException e) {
            logger.warn("This name " + P2PConstants.P2P_NODE_NAME +
                " is already bound in the registry", e);
        }

        // P2PService Active Object Creation
        this.p2pService = (P2PService) ProActiveObject.newActive(P2PService.class.getName(),
                null, url);

        try {
            ProActiveObject.enableAC(this.p2pService);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Couldn't enable AC for the P2P service", e);
            }
        }

        logger.info(
            "/////////////////  STARTING P2P SERVICE //////////////////");

        // Record the ProActiveRuntime in other from Servers List File
        if (!this.peers.isEmpty()) {
            this.p2pService.firstContact(this.peers);
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public P2PService getP2PService() {
        return this.p2pService;
    }

    private static class Args {
        private String acquisitionMethod = ProProperties.PA_P2P_ACQUISITION.getValue();
        private String portNumber = ProProperties.PA_P2P_PORT.getValue();
        private String noa = ProProperties.PA_P2P_NOA.getValue();
        private String ttu = ProProperties.PA_P2P_TTU.getValue();
        private String ttl = ProProperties.PA_P2P_TTL.getValue();
        private String msg_capacity = ProProperties.PA_P2P_MSG_MEMORY.getValue();
        private String expl_msg = ProProperties.PA_P2P_EXPLORING_MSG.getValue();
        private String nodes_acq_to = ProProperties.PA_P2P_NODES_ACQUISITION_T0.getValue();
        private String lookup_freq = ProProperties.PA_P2P_LOOKUP_FREQ.getValue();
        private String multi_proc_nodes = ProProperties.PA_P2P_MULTI_PROC_NODES.getValue();
        private String xml_path = ProProperties.PA_P2P_XML_PATH.getValue();
        private String peerListFile = null;
        private final Vector peers = new Vector();
        private String no_sharing = ProProperties.PA_P2P_NO_SHARING.getValue();
    }
}
