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
package org.objectweb.proactive.p2p.service.util;


/**
 * Java system properties name and P2P constants.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jan 13, 2005
 */
public interface P2PConstants {

    /**
     * <code>P2P_NODE_NAME</code>: name of the p2pServiceNode where the P2P
     *  Service is running.
     */
    public static final String P2P_NODE_NAME = "P2PNode";

    /**
     * <code>SHARED_NODE_NAME</code>: name of the default shared node.
     */
    public static final String SHARED_NODE_NAME = "SharedNode";

    /**
     * <code>MAX_NODE</code>: special number for asking nodes every times.
     */
    public static final int MAX_NODE = -1;

    /**
     * <code>VN_NAME</code>: name of the virtual node.
     */
    public static final String VN_NAME = "SharedNodes";

    // -------------------------------------------------------------------------
    // Java system properties names
    // -------------------------------------------------------------------------

    /** Acquisition method. */
    public static final String PROPERTY_ACQUISITION = "proactive.p2p.acq";

    /** Port number. */
    public static final String PROPERTY_PORT = "proactive.p2p.port";

    /** NOA is in number of peers. */
    public static final String PROPERTY_NOA = "proactive.p2p.noa";

    /** TTU is in minutes. */
    public static final String PROPERTY_TTU = "proactive.p2p.ttu";

    /** TTL is in hops. */
    public static final String PROPERTY_TTL = "proactive.p2p.ttl";

    /** List capacity of message sequence number. */
    public static final String PROPERTY_MSG_MEMORY = "proactive.p2p.msg_capacity";

    /** Percentage of agree response. */
    public static final String PROPERTY_EXPLORING_MSG = "proactive.p2p.expl_msg";

    /** Expiration time for booking without use a shared node. */
    public static final String PROPERTY_BOOKING_MAX = "proactive.p2p.booking_max";

    /** Timeout for node acquisition. */
    public static final String PROPERTY_NODES_ACQUISITION_T0 = "proactive.p2p.nodes_acq_to";

    /** Lookup frequency for nodes. */
    public static final String PROPERTY_LOOKUP_FREQ = "proactive.p2p.lookup_freq";

    /** If true deploying one shared nodes by CPU, else only one node is shared. */
    public static final String PROPERTY_MULTI_PROC_NODES = "proactive.p2p.multi_proc_nodes";

    /** Path of the xml deployment descriptor, for deploying shared nodes. */
    public static final String PROPERPY_XML_PATH = "proactive.p2p.xml_path";

    /** Timeout for waiting node ack. */
    public static final String PROPERTY_NODE_ACK_TO = "proactive.p2p.ack_to";

    /** Boolean for using or not p2p load balanced. */
    public static final String PROPERTY_LOAD_BAL = "proactive.p2p.loadbalancing";
}
