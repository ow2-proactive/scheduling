/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.service;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.util.P2PConstants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.UnknownHostException;

import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Jan 4, 2005
 */
public class StartP2PService implements P2PConstants {
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
        " -xml_path Deployment descriptor path";

    private static class Args {
        private String acquisitionMethod = System.getProperty(PROPERTY_ACQUISITION);
        private String portNumber = System.getProperty(PROPERTY_PORT);
        private String noa = System.getProperty(PROPERTY_NOA);
        private String ttu = System.getProperty(PROPERTY_TTU);
        private String ttl = System.getProperty(PROPERTY_TTL);
        private String msg_capacity = System.getProperty(PROPERTY_MSG_MEMORY);
        private String expl_msg = System.getProperty(PROPERTY_EXPLORING_MSG);
        private String booking_max = System.getProperty(PROPERTY_BOOKING_MAX);
        private String nodes_acq_to = System.getProperty(PROPERTY_NODES_ACQUISITION_T0);
        private String lookup_freq = System.getProperty(PROPERTY_LOOKUP_FREQ);
        private String multi_proc_nodes = System.getProperty(PROPERTY_MULTI_PROC_NODES);
        private String xml_path = System.getProperty(PROPERPY_XML_PATH);
        private String peerListFile = null;
        private Vector peers = new Vector();
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
            } else if ("-booking".equalsIgnoreCase(argname)) {
                index++;
                if (index > args.length) {
                    usage(argname);
                }
                parsed.booking_max = args[index];
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
        System.setProperty(PROPERTY_ACQUISITION, parsed.acquisitionMethod);
        System.setProperty(PROPERTY_PORT, parsed.portNumber);
        System.setProperty(PROPERTY_NOA, parsed.noa);
        System.setProperty(PROPERTY_TTU, parsed.ttu);
        System.setProperty(PROPERTY_TTL, parsed.ttl);
        System.setProperty(PROPERTY_MSG_MEMORY, parsed.msg_capacity);
        System.setProperty(PROPERTY_EXPLORING_MSG, parsed.expl_msg);
        System.setProperty(PROPERTY_BOOKING_MAX, parsed.booking_max);
        System.setProperty(PROPERTY_NODES_ACQUISITION_T0, parsed.nodes_acq_to);
        System.setProperty(PROPERTY_LOOKUP_FREQ, parsed.lookup_freq);
        System.setProperty(PROPERTY_MULTI_PROC_NODES, parsed.multi_proc_nodes);
        if (parsed.xml_path != null) {
            System.setProperty(PROPERPY_XML_PATH, parsed.xml_path);
        }
    }

    // -------------------------------------------------------------------------
    // Main method
    // -------------------------------------------------------------------------

    /**
     * Usage: java org.objectweb.proactive.p2p.service.StartP2PService
     * [-acq acquisitionMethod] [-port portNumber] [-s Server ...] [-f ServerListFile]
     *
     * @param args
     *            acquisitionMethod portNumber Servers List File
     */
    public static void main(String[] args) {
        // Parsing command line
        Args parsed = parseArgs(args);

        // Init system properties with new value
        initP2PProperties(parsed);

        // For Debbugging
        try {
            logger.info("**** Starting jvm on " +
                UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()));
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
                System.getProperty("proactive.p2p.acq"));
            logger.debug("Port number: " +
                System.getProperty("proactive.p2p.port"));
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
    // Class Fileds
    //--------------------------------------------------------------------------
    private Vector peers = new Vector();
    private P2PService p2pService = null;

    // -------------------------------------------------------------------------
    // Class Constructors
    // -------------------------------------------------------------------------

    /**
     * Construct a new <code>StartP2PService</code> with peers specified from
     * parsed command line.
     * @param parsed parsed command line arguments.
     */
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

    // -------------------------------------------------------------------------
    // Class methods
    // -------------------------------------------------------------------------

    /**
     * Parse a text file with one host by line.
     * @param <code>fileURL</code> URL of the file.
     */
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
    private void checkingPeersUrl() {
        int nbUrls = this.peers.size();
        for (int i = 0; i < nbUrls; i++) {
            String url = (String) this.peers.get(i);
            if (url.indexOf("//") < 0) {
                url = System.getProperty(P2PConstants.PROPERTY_ACQUISITION) +
                    "://" + url;
            }
            if (!url.matches(".*:[0-9]+$")) {
                url += (":" + System.getProperty(P2PConstants.PROPERTY_PORT));
            }
            this.peers.set(i, url);
        }
    }

    /**
     * <p>Start a new active ProActive P2P service.</p>
     * <p><b>Warning: </b> it's not a thread.</p>
     * @throws ProActiveException
     */
    public void start() throws ProActiveException {
        // Cleanning peers URL
        this.checkingPeersUrl();

        // Starting new Active P2P Service
        String acquisitionMethod = System.getProperty(P2PConstants.PROPERTY_ACQUISITION);
        String portNumber = System.getProperty(P2PConstants.PROPERTY_PORT);

        // Keep previous port value
        String bckPortValue = null;
        if (!acquisitionMethod.equals("ibis")) {
            bckPortValue = System.getProperty("proactive." + acquisitionMethod +
                    ".port");
            System.setProperty("proactive." + acquisitionMethod + ".port",
                portNumber);
        } else {
            bckPortValue = System.getProperty("proactive.rmi.port");
            System.setProperty("proactive.rmi.port", portNumber);
        }

        // ProActiveRuntime creation
        ProActiveRuntime paRuntime = RuntimeFactory.getProtocolSpecificRuntime(acquisitionMethod +
                ":");

        // Set property port with previous value
        if (bckPortValue != null) {
            if (!acquisitionMethod.equals("ibis")) {
                System.setProperty("proactive." + acquisitionMethod + ".port",
                    bckPortValue);
            } else {
                System.setProperty("proactive.rmi.port", bckPortValue);
            }
        }

        // Node Creation
        String url = paRuntime.createLocalNode(P2PConstants.P2P_NODE_NAME,
                false, null, paRuntime.getVMInformation().getName(),
                paRuntime.getJobID());

        // P2PService Active Object Creation
        this.p2pService = (P2PService) ProActive.newActive(P2PService.class.getName(),
                null, url);
        try {
            ProActive.enableAC(this.p2pService);
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

    /**
     * @return the active P2P service if started else <code>null</code>.
     */
    public P2PService getP2PService() {
        return this.p2pService;
    }
}
