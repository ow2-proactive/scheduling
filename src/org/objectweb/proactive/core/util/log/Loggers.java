package org.objectweb.proactive.core.util.log;


/**
 * This interfaces centralizes the names of the loggers.
 *
 * @author Matthieu Morel
 *
 */
public interface Loggers {
    
    static final public String CORE = "proactive";
    static final public String CLASSLOADING = "proactive.classloading";
    static final public String EVENTS = "proactive.events";
    static final public String RUNTIME = "proactive.runtime";
    static final public String BODY = "proactive.body";
    static final public String MOP = "proactive.mop";
    static final public String GROUPS = "proactive.groups";
    public static final String NFE = "proactive.nfe";
    public static final String HTTP_TRANSPORT = "proactive.communication.transport.http";
    public static final String MIGRATION = "proactive.migration";
    public static final String REQUESTS = "proactive.communication.requests";
    public static final String JINI = "proactive.jini";
    static final public String UTIL = "proactive.util";
    static final public String LOG = "proactive.util.log";
    static final public String XML = "proactive.xml";
    static final public String STUB_GENERATION = "proactive.mop.stubgeneration";
    static final public String RMI = "proactive.communication.rmi";
    static final public String SSH = "proactive.communication.ssh";
    
    static final public String COMPONENTS = "proactive.components";
    static final public String COMPONENTS_REQUESTS = COMPONENTS + ".request";
    static final public String COMPONENTS_ACTIVITY = COMPONENTS + ".activity";
    static final public String COMPONENTS_BYTECODE_GENERATION = COMPONENTS +".bytecodegeneration";
    static final public String COMPONENTS_ADL = COMPONENTS +".adl";
    static final public String COMPONENTS_GUI = COMPONENTS +".gui";
    
    static final public String DEPLOYMENT = "proactive.deployment";
    static final public String DEPLOYMENT_PROCESS = DEPLOYMENT + ".process";
    static final public String DEPLOYMENT_LOG = DEPLOYMENT + ".log";
    static final public String FILETRANSFER = "proactive.filetransfer";
    
    public static final String LOAD_BALANCING = "proactive.loadbalancing";
    
    public static final String IC2D = "proactive.ic2d";
    public static final String EXAMPLES = "proactive.examples";

    // P2P loggers
    public static final String P2P = "proactive.p2p";
    public static final String P2P_STARTSERVICE = P2P + ".startservice";
    public static final String P2P_VN = P2P + ".vn"; //descriptor
    public static final String P2P_DESC_SERV = P2P_VN + ".service"; // threads, nodes lookup
    public static final String P2P_SERVICE = P2P + ".service";
    public static final String P2P_NODES = P2P + ".nodes"; // lookup and nodes sharing
    public static final String P2P_ACQUAINTANCES = P2P + ".acquaintances";
    public static final String P2P_FIRST_CONTACT = P2P + ".first_contact";
    public static final String P2P_SKELETONS = P2P + ".skeletons";
    public static final String P2P_SKELETONS_WORKER = P2P_SKELETONS +".worker";
    public static final String P2P_SKELETONS_MANAGER = P2P_SKELETONS +".manager";
    public static final String P2P_DAEMON= P2P+".daemon";

    // Security loggers
    public static final String SECURITY = "proactive.security";
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
    public static final String SECURITY_PSM = SECURITY + ".psm";

    // Fault-tolerance loggers
    public static final String FAULT_TOLERANCE = "proactive.ft";
    public static final String FAULT_TOLERANCE_CIC = FAULT_TOLERANCE + ".cic";
    public static final String FAULT_TOLERANCE_PML = FAULT_TOLERANCE + ".pml";
    
    
}
