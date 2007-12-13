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
package org.objectweb.proactive.core.util.log;

/**
 * This interfaces centralizes the names of the loggers.
 *
 * @author Matthieu Morel
 *
 */
public interface Loggers {
    static final public String CORE = "proactive";
    static final public String CONFIGURATION = CORE + ".configuration";
    static final public String CLASSLOADING = CORE + ".classloading";
    static final public String EVENTS = CORE + ".events";
    static final public String RUNTIME = CORE + ".runtime";
    static final public String NODE = RUNTIME + ".node";
    static final public String BODY = CORE + ".body";
    static final public String MOP = CORE + ".mop";
    public static final String SYNC_CALL = CORE + ".sync_call";
    static final public String GROUPS = CORE + ".groups";
    public static final String GC = CORE + ".gc";
    public static final String HTTP_TRANSPORT = CORE + ".communication.transport.http";
    public static final String MIGRATION = CORE + ".migration";
    public static final String REQUESTS = CORE + ".communication.requests";
    static final public String UTIL = CORE + ".util";
    static final public String LOG = CORE + ".util.log";
    static final public String XML = CORE + ".xml";
    static final public String STUB_GENERATION = CORE + ".mop.stubgeneration";
    static final public String RMI = CORE + ".communication.rmi";
    static final public String SSH = CORE + ".communication.ssh";
    static final public String COMPONENTS = CORE + ".components";
    static final public String COMPONENTS_CONTROLLERS = COMPONENTS + ".controllers";
    static final public String COMPONENTS_REQUESTS = COMPONENTS + ".requests";
    static final public String COMPONENTS_ACTIVITY = COMPONENTS + ".activity";
    static final public String COMPONENTS_GEN_ITFS = COMPONENTS + ".gen.interface";
    static final public String COMPONENTS_GEN_ANNOTATION = COMPONENTS + ".gen.annotation";
    static final public String COMPONENTS_ADL = COMPONENTS + ".adl";
    static final public String COMPONENTS_GUI = COMPONENTS + ".gui";
    static final public String COMPONENTS_MULTICAST = COMPONENTS + ".multicast";
    static final public String COMPONENTS_GATHERCAST = COMPONENTS + ".gathercast";
    static final public String DEPLOYMENT = CORE + ".deployment";
    static final public String DEPLOYMENT_PROCESS = DEPLOYMENT + ".process";
    static final public String DEPLOYMENT_LOG = DEPLOYMENT + ".log";
    static final public String DEPLOYMENT_FILETRANSFER = CORE + ".deployment.filetransfer";
    static final public String FILETRANSFER = CORE + ".filetransfer";
    public static final String LOAD_BALANCING = CORE + ".loadbalancing";
    public static final String IC2D = CORE + ".ic2d";
    public static final String EXAMPLES = CORE + ".examples";

    // P2P loggers
    public static final String P2P = CORE + ".p2p";
    public static final String P2P_STARTSERVICE = P2P + ".startservice";
    public static final String P2P_VN = P2P + ".vn"; //descriptor
    public static final String P2P_DESC_SERV = P2P_VN + ".service"; // threads, nodes lookup
    public static final String P2P_SERVICE = P2P + ".service";
    public static final String P2P_MESSAGE = P2P + ".message";
    public static final String P2P_NODES = P2P + ".nodes"; // lookup and nodes sharing
    public static final String P2P_ACQUAINTANCES = P2P + ".acquaintances";
    public static final String P2P_FIRST_CONTACT = P2P + ".first_contact";
    public static final String P2P_SKELETONS = P2P + ".skeletons";
    public static final String P2P_SKELETONS_WORKER = P2P_SKELETONS + ".worker";
    public static final String P2P_SKELETONS_MANAGER = P2P_SKELETONS + ".manager";
    public static final String P2P_DAEMON = P2P + ".daemon";

    // Security loggers
    public static final String SECURITY = CORE + ".security";
    public static final String SECURITY_NODE = SECURITY + ".node";
    public static final String SECURITY_SESSION = SECURITY + ".session";
    public static final String SECURITY_BODY = SECURITY + ".body";
    public static final String SECURITY_MANAGER = SECURITY + ".manager";
    public static final String SECURITY_REQUEST = SECURITY + ".request";
    public static final String SECURITY_RUNTIME = SECURITY + ".runtime";
    public static final String SECURITY_DOMAIN = SECURITY + ".domain";
    public static final String SECURITY_POLICY = SECURITY + ".policy";
    public static final String SECURITY_POLICYSERVER = SECURITY + ".policyserver";
    public static final String SECURITY_CRYPTO = SECURITY + ".crypto";
    public static final String SECURITY_DESCRIPTOR = SECURITY + ".descriptor";

    // Fault-tolerance loggers
    public static final String FAULT_TOLERANCE = CORE + ".ft";
    public static final String FAULT_TOLERANCE_CIC = FAULT_TOLERANCE + ".cic";
    public static final String FAULT_TOLERANCE_PML = FAULT_TOLERANCE + ".pml";

    // MPI loggers
    static final public String MPI = CORE + ".mpi";
    static final public String MPI_CONTROL = MPI + ".control";
    static final public String MPI_CONTROL_MANAGER = MPI_CONTROL + ".control";
    static final public String MPI_CONTROL_COUPLING = MPI_CONTROL + ".coupling";

    // Scilab loggers
    public static final String SCILAB = CORE + ".scilab";
    public static final String SCILAB_DEPLOY = SCILAB + ".deploy";
    public static final String SCILAB_SERVICE = SCILAB + ".service";
    public static final String SCILAB_WORKER = SCILAB + ".worker";
    public static final String SCILAB_TASK = SCILAB + ".task";

    // Skeleton loggers
    static final public String SKELETONS = CORE + ".skeletons";
    static final public String SKELETONS_STRUCTURE = SKELETONS + ".structure";
    static final public String SKELETONS_ENVIRONMENT = SKELETONS + ".environment";
    static final public String SKELETONS_KERNEL = SKELETONS + ".taskpool";
    static final public String SKELETONS_APPLICATION = SKELETONS + ".application";
    static final public String SKELETONS_DIAGNOSIS = SKELETONS + ".diagnosis";
    static final public String SKELETONS_SYSTEM = SKELETONS + ".system";
    static final public String SCHEDULER = CORE + ".scheduler";
    static final public String RESOURCE_MANAGER = CORE + ".resourceManager";

    // Infrastructure Manager loggers
    static final public String RM = CORE + ".rm";
    static final public String RM_FACTORY = RM + ".factory";
    static final public String RM_CORE = RM + ".core";
    static final public String RM_ADMIN = RM + ".admin";
    static final public String RM_MONITORING = RM + ".monitoring";
    static final public String RM_USER = RM + ".user";
    static final public String RM_DEPLOYMENT_FACTORY = RM + ".deployfactory";
    static final public String RM_DEPLOY = RM_DEPLOYMENT_FACTORY + ".deploy";
    static final public String RM_DATARESOURCE = RM + ".dataresource";
    static final public String RM_TEST = RM + ".test";
    static final public String RM_ACTIVITY_NODES = RM + ".activitynodes";

    //  Master Worker loggers
    static final public String MASTERWORKER = CORE + ".masterworker";
    static final public String MASTERWORKER_WORKERMANAGER = MASTERWORKER + ".workermanager";
    static final public String MASTERWORKER_PINGER = MASTERWORKER + ".pinger";
    static final public String MASTERWORKER_REPOSITORY = MASTERWORKER + ".repository";
    static final public String MASTERWORKER_WORKERS = MASTERWORKER + ".workers";

    // remote Objects
    static final public String REMOTEOBJECT = CORE + ".remoteobject";
    public static final String CLASS_SERVER = CORE + ".class_server";

    // JMX
    static final public String JMX = CORE + ".jmx";
    static final public String JMX_MBEAN = JMX + ".mbean";
    static final public String JMX_NOTIFICATION = JMX + ".notification";

    // Exceptions
    public static final String EXCEPTIONS = CORE + ".exceptions";
    public static final String EXCEPTIONS_ONE_WAY = EXCEPTIONS + ".one_way";
    public static final String EXCEPTIONS_SEND_REPLY = EXCEPTIONS + ".send_reply";
}
