package org.objectweb.proactive.core.util.log;


/**
 * Centralizes the names of the loggers.
 *
 * @author Matthieu Morel
 *
 */
public interface Loggers {
    static final public String COMPONENTS = "proactive.components";
    static final public String CLASSLOADER = "proactive.classloader";
    static final public String RUNTIME = "proactive.runtime";
    static final public String BODY = "proactive.body";
    
    static final public String COMPONENTS_REQUEST = COMPONENTS+".request";
    static final public String COMPONENTS_ACTIVITY = COMPONENTS+".activity";
    static final public String COMPONENTS_BYTECODE_GENERATION = COMPONENTS+".bytecodegeneration";
    
    static final public String DEPLOYMENT = "proactive.deployment";
    static final public String DEPLOYMENT_PROCESS = DEPLOYMENT+".process";
    
    static final public String FILETRANSFER = "proactive.filetransfer";

    public static final String NFE = "proactive.nfe";

    /** Logger for ProActive Examples. **/
    public static final String EXAMPLES = "proactive.examples";

    // -------------------------------------------------------------------------
    // All P2P loggers

    /** Logger for ProActive P2P. **/
    public static final String P2P = "proactive.p2p";

    /** All related to start P2P Service. */
    public static final String P2P_STARTSERVICE = P2P + ".startservice";

    /** P2P descriptor deploying Logging. */
    public static final String P2P_VN = P2P + ".vn";

    /** Looging of deploying thread, i.e. nodes lookup. */
    public static final String P2P_DESC_SERV = P2P_VN + ".service";

    /** The most important, logging the P2P Service. */
    public static final String P2P_SERVICE = P2P + ".service";

    /** logging lookup and sharing nodes. */
    public static final String P2P_NODES = P2P + ".nodes";

    /** Managing acquaintances. */
    public static final String P2P_ACQUAINTANCES = P2P + ".acquaintances";

    /** First contact logging. */
    public static final String P2P_FIRST_CONTACT = P2P + ".first_contact";

    /** Sekeltons logging. */
    public static final String P2P_SKELETONS = P2P + ".skeletons";

    /** P2P framework workers logging. */
    public static final String P2P_SKELETONS_WORKER = P2P_SKELETONS +
        ".worker";

    /**P2P framework managers logging. */
    public static final String P2P_SKELETONS_MANAGER = P2P_SKELETONS +
        ".manager";

    // -------------------------------------------------------------------------
}
