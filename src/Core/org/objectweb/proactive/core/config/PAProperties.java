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
package org.objectweb.proactive.core.config;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * A Java Properties factory for ProActive
 *
 * All Java properties supported by ProActive must be declared and documented in
 * this enumeration. Provided methods provided must be used in place
 * of System.(get|set)property() or the ProActiveConfiguration class.
 *
 * TODO Add integer properties
 */
public enum PAProperties {

    // ----------

    /** this property identifies proactive's configuration file */
    PA_CONFIGURATION_FILE("proactive.configuration", false),

    /** this property indicates to create for each ProActive object a MBeans */
    PA_JMX_MBEAN("proactive.jmx.mbean", true),

    /** this property indicates to send JMX notifications */
    PA_JMX_NOTIFICATION("proactive.jmx.notification", true),

    /** this property identifies the communication protocol */
    PA_COMMUNICATION_PROTOCOL("proactive.communication.protocol", false),

    /** this property allows to override the default java behavior when retrieving the runtime url */
    PA_RUNTIME_IPADDRESS("proactive.runtime.ipaddress", false),

    /** this property allows to set the hostname of a runtime */
    PA_HOSTNAME("proactive.hostname", false),

    /** this property indicates to use the IP address instead of DNS entry for a runtime url */
    PA_USE_IP_ADDRESS("proactive.useIPaddress", true),

    /** TODO cdelbe Describe this property */
    PA_FUTURE_AC("proactive.future.ac", true),

    /** TODO cdelbe Describe this property */
    SCHEMA_VALIDATION("schema.validation", true),

    /** this property indicates if the ProActive class loader must be used */
    PA_CLASSLOADER("proactive.classloader", true),

    /** TODO cmathieu Describe this property */
    PA_PAD("proactive.pad", false),

    /** TODO cdelbe Describe this property */
    PA_EXIT_ON_EMPTY("proactive.exit_on_empty", true),

    /** TODO gchazara Describe this property */
    PA_FUTUREMONITORING_TTM("proactive.futuremonitoring.ttm", false),

    /** TODO cdelbe Describe this property */
    PA_STACKTRACE("proactive.stack_trace", true),
    PA_HOME("proactive.home", false),
    /** this property indicates the location of the log4j configuration file */
    LOG4J("log4j.configuration", false),

    /** TODO cmathieu Describe this property */
    LOG4J_DEFAULT_INIT_OVERRIDE("log4j.defaultInitOverride", true),

    /** this property indicates the location of the java Security policy file */
    SECURITY_POLICY("java.security.policy", false),

    /**  TODO cmathieu Describe this property */
    PA_RUNTIME_NAME("proactive.runtime.name", false),

    /** TODO cmathieu Describe this property */
    PA_SECONDARYNAMES("proactive.secondaryNames", false),

    /** this property should be used when one wants to start only a runtime without an additionnal main class */
    PA_RUNTIME_STAYALIVE("proactive.runtime.stayalive", true),

    // ---------- RMI

    /** this property identifies the default port used by the RMI transport layer */
    PA_RMI_PORT("proactive.rmi.port", false),
    JAVA_RMI_SERVER_CODEBASE("java.rmi.server.codebase", false),

    // ---------- HTTP

    /** this property identifies the default port for the xml-http protocol  */
    PA_XMLHTTP_PORT("proactive.http.port", false),

    /** TODO vlegrand Describe this property */
    PA_HTTP_SERVLET("proactive.http.servlet", true),
    PA_HTTP_PORT("proactive.http.port", false),

    // ---------- COMPONENTS

    /** TODO  cdalmass Describe this property */
    PA_FRACTAL_PROVIDER("fractal.provider", false),

    /** TODO  cdalmass Describe this property */
    PA_COMPONENT_CREATION_TIMEOUT("components.creation.timeout", false),

    /** TODO  cdalmass Describe this property */
    PA_COMPONENT_USE_SHORTCUTS("proactive.components.use_shortcuts", true),

    // ---------- Migration

    /** TODO Describe this property */
    PA_LOCATION_SERVER("proactive.locationserver", false),

    /** TODO cdelbe Describe this property */
    PA_LOCATION_SERVER_RMI("proactive.locationserver.rmi", false),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_TTL("proactive.mixedlocation.ttl", false),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_UPDATINGFORWARDER("proactive.mixedlocation.updatingForwarder",
        true),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_MAXMIGRATIONNB("proactive.mixedlocation.maxMigrationNb",
        false),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_MAXTIMEONSITE("proactive.mixedlocation.maxTimeOnSite",
        false),
    // ---------- SSH

    /** this property identifies the default port used by the RMISSH transport layer */
    PA_SSH_PORT("proactive.ssh.port", false),

    /** this property identifies the location of the known host file for the RMISSH transport layer */
    PA_SSH_KNOWN_HOST("proactive.ssh.known_hosts", false),

    /** this property identifies the location of RMISSH key directory */
    PA_SSH_KEY_DIR("proactive.ssh.key_directory", false),

    /** this property identifies that when using SSH tunneling, a normal connection should be tried before tunneling */
    PA_SSH_TUNNELING_TRY_NORMAL_FIRST("proactive.tunneling.try_normal_first",
        true),
    /** this property identifies if the garbage collector should be turned on when using SSH tunneling */
    PA_SSH_TUNNELING_USE_GC("proactive.tunneling.use_gc", true),

    /** this property identifies the garbage collector period when using SSH tunneling */
    PA_SSH_TUNNELING_GC_PERIOD("proactive.tunneling.gc_period", false),

    /** this property identifies the know hosts file location when using ssh tunneling
     *  if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    PA_SSH_TUNNELING_KNOW_HOSTS("proactive.ssh.known_hosts", false),

    /** TODO cmathieu Describe this property */
    PA_SSH_TUNNELING_CONNECT_TIMEOUT("proactive.tunneling.connect_timeout",
        false),
    /** TODO cmathieu Describe this property */
    PA_SSH_USERNAME("proactive.ssh.username", true),
    // ------------ Fault Tolerance

    /** TODO cdelbe Describe this property */
    PA_FT("proactive.ft", true),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_CHECKPOINT("proactive.ft.server.checkpoint", false),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_LOCATION("proactive.ft.server.location", false),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RECOVERY("proactive.ft.server.recovery", false),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_GLOBAL("proactive.ft.server.global", false),

    /** TODO cdelbe Describe this property */
    PA_FT_TTC("proactive.ft.ttc", false),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RESOURCE("proactive.ft.server.resource", false),

    /** TODO cdelbe Describe this property */
    PA_FT_PROTOCOL("proactive.ft.protocol", false),

    // ---------- Security

    /** this property indicates if a RMISecurityManager has to be instanciated*/
    PA_SECURITYMANAGER("proactive.securitymanager", true),

    /** this property indicates the location of the runtime' security manager configuration file */
    PA_RUNTIME_SECURITY("proactive.runtime.security", false),

    /** this property indicates the url of the security domain the runtime depends on */
    PA_RUNTIME_DOMAIN_URL("proactive.runtime.domain.url", false),

    // ------------ Timit

    /** TODO vbodnart Describe this property */
    PA_TIMIT_ACTIVATION("proactive.timit.activation", false),

    // -------------- Master/Slave

    /** The ping period is the default interval at which slaves receive a ping message (to check if they're alive).*/
    PA_MASTERSLAVE_PINGPERIOD("proactive.masterslave.pingperiod", false),

    // -------------- P2P

    /** Acquisition method. */
    PA_P2P_ACQUISITION("proactive.p2p.acq", false),

    /** Port number. */
    PA_P2P_PORT("proactive.p2p.port", false),

    /** NOA is in number of peers. */
    PA_P2P_NOA("proactive.p2p.noa", false),

    /** TTU is in minutes. */
    PA_P2P_TTU("proactive.p2p.ttu", false),

    /** TTL is in hops. */
    PA_P2P_TTL("proactive.p2p.ttl", false),

    /** List capacity of message sequence number. */
    PA_P2P_MSG_MEMORY("proactive.p2p.msg_capacity", false),

    /** Percentage of agree response. */
    PA_P2P_EXPLORING_MSG("proactive.p2p.expl_msg", false),

    /** Timeout for node acquisition. */
    PA_P2P_NODES_ACQUISITION_T0("proactive.p2p.nodes_acq_to", false),

    /** Lookup frequency for nodes. */
    PA_P2P_LOOKUP_FREQ("proactive.p2p.lookup_freq", false),

    /** If true deploying one shared nodes by CPU, else only one node is shared. */
    PA_P2P_MULTI_PROC_NODES("proactive.p2p.multi_proc_nodes", true),

    /** Path of the xml deployment descriptor, for deploying shared nodes. */
    PA_P2P_XML_PATH("proactive.p2p.xml_path", false),

    /** Boolean value for disable node sharing. */
    PA_P2P_NO_SHARING("proactive.p2p.nosharing", true),

    // -------------- DGC

    /** TODO gchazara Describe this property */
    PA_DGC("proactive.dgc", true),
    /**
     * TimeToAlone
     * After this delay, we suppose we got a message from all our referencers.
     */
    PA_DGC_TTA("proactive.dgc.tta", false),

    /**
     * TimeToBroadcast
     * Time is always in milliseconds. It is fundamental for this value
     * to be the same in all JVM of the distributed system, so think twice
     * before changing it.
     */
    PA_DGC_TTB("proactive.dgc.ttb", false),

    // -------------- Misc

    /** TODO Describe this property */
    PA_BYTECODEMANIPULATOR("byteCodeManipulator", false),

    /** TODO vlegrand Describe this property */
    CATALINA_BASE("catalina.base", false),
    PA_UNICORE_FORKCLIENT("proactive.unicore.forkclient", true);static final Logger logger =
        ProActiveLogger.getLogger(Loggers.CONFIGURATION);
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private String key;
    private boolean isBoolean;

    PAProperties(String str, boolean isBoolean) {
        this.key = str;
        this.isBoolean = isBoolean;
    }

    /**
     * Returns the key associated to this property
     * @return the key associated to this property
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value of this property
     * @return the value of this property
     */
    public String getValue() {
        return ProActiveConfiguration.getInstance().getProperty(key);
    }

    /**
     * Set the value of this property
     * @param value new value of the property
     */
    public void setValue(String value) {
        ProActiveConfiguration.getInstance().setProperty(key, value);
    }

    @Override
    public String toString() {
        return key + "=" + getValue();
    }

    /**
     * Indicates if the property is set.
     * @return true if and only if the property has been set.
     */
    public boolean isSet() {
        return ProActiveConfiguration.getInstance().getProperty(key) != null;
    }

    /**
     * Indicates if this property is true.
     *
     * This method can only be called with boolean property. Otherwise an {@link IllegalArgumentException}
     * is thrown.
     *
     * If the value is illegal for a boolean property, then false is returned and a warning is
     * printed.
     *
     * @return true if the property is set to true.
     */
    public boolean isTrue() {
        if (!isBoolean) {
            RuntimeException e = new IllegalArgumentException(key +
                    " is not a boolean property. isTrue cannot be called on this property");
            logger.error(e);
            throw e;
        }

        if (!isSet()) {
            return false;
        }

        String val = getValue();
        if (TRUE.equals(val)) {
            return true;
        }

        if (FALSE.equals(val)) {
            return false;
        }

        logger.warn(key + " is a boolean property but its value is nor " +
            TRUE + " nor " + FALSE + " " + "(" + val + "). ");
        return false;
    }

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    public String getCmdLine() {
        return "-D" + key + '=';
    }

    public boolean isBoolean() {
        return isBoolean;
    }
}
