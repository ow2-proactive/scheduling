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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import trywithcatch.Catch;

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
    PA_CONFIGURATION_FILE("proactive.configuration", PAPropertiesType.STRING),

    /** this property indicates to create for each ProActive object a MBeans */
    PA_JMX_MBEAN("proactive.jmx.mbean", PAPropertiesType.BOOLEAN),

    /** this property indicates to send JMX notifications */
    PA_JMX_NOTIFICATION("proactive.jmx.notification", PAPropertiesType.BOOLEAN),

    /** this property identifies the communication protocol */
    PA_COMMUNICATION_PROTOCOL("proactive.communication.protocol",
        PAPropertiesType.STRING),

    /** this property allows to override the default java behavior when retrieving the runtime url */
    PA_RUNTIME_IPADDRESS("proactive.runtime.ipaddress", PAPropertiesType.STRING),

    /** this property allows to set the hostname of a runtime */
    PA_HOSTNAME("proactive.hostname", PAPropertiesType.STRING),

    /** this property indicates to use the IP address instead of DNS entry for a runtime url */
    PA_USE_IP_ADDRESS("proactive.useIPaddress", PAPropertiesType.BOOLEAN),
    PA_NOLOOPBACK("proactive.nolocal", PAPropertiesType.BOOLEAN),
    PA_NOPRIVATE("proactive.noprivate", PAPropertiesType.BOOLEAN),

    /** TODO cdelbe Describe this property */
    PA_FUTURE_AC("proactive.future.ac", PAPropertiesType.BOOLEAN),

    /** TODO cdelbe Describe this property */
    SCHEMA_VALIDATION("schema.validation", PAPropertiesType.BOOLEAN),

    /** this property indicates if the ProActive class loader must be used */
    PA_CLASSLOADER("proactive.classloader", PAPropertiesType.BOOLEAN),

    /** TODO cmathieu Describe this property */
    PA_PAD("proactive.pad", PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_EXIT_ON_EMPTY("proactive.exit_on_empty", PAPropertiesType.BOOLEAN),

    /** TODO gchazara Describe this property */
    PA_FUTUREMONITORING_TTM("proactive.futuremonitoring.ttm",
        PAPropertiesType.INTEGER),

    /** TODO cdelbe Describe this property */
    PA_STACKTRACE("proactive.stack_trace", PAPropertiesType.BOOLEAN),
    PA_HOME("proactive.home", PAPropertiesType.STRING),

    /** this property indicates the location of the log4j configuration file */
    LOG4J("log4j.configuration", PAPropertiesType.STRING),

    /** TODO cmathieu Describe this property */
    LOG4J_DEFAULT_INIT_OVERRIDE("log4j.defaultInitOverride",
        PAPropertiesType.BOOLEAN),

    /** this property indicates the location of the java Security policy file */
    SECURITY_POLICY("java.security.policy", PAPropertiesType.STRING),

    /**  TODO cmathieu Describe this property */
    PA_RUNTIME_NAME("proactive.runtime.name", PAPropertiesType.STRING),

    /** TODO cmathieu Describe this property */
    PA_SECONDARYNAMES("proactive.secondaryNames", PAPropertiesType.STRING),

    /** this property should be used when one wants to start only a runtime without an additionnal main class */
    PA_RUNTIME_STAYALIVE("proactive.runtime.stayalive", PAPropertiesType.BOOLEAN),

    // ---------- RMI

    /** this property identifies the default port used by the RMI transport layer */
    PA_RMI_PORT("proactive.rmi.port", PAPropertiesType.INTEGER),
    JAVA_RMI_SERVER_CODEBASE("java.rmi.server.codebase", PAPropertiesType.STRING),

    // ---------- HTTP

    /** this property identifies the default port for the xml-http protocol  */
    PA_XMLHTTP_PORT("proactive.http.port", PAPropertiesType.INTEGER),

    /** TODO vlegrand Describe this property */
    PA_HTTP_SERVLET("proactive.http.servlet", PAPropertiesType.BOOLEAN),
    PA_HTTP_PORT("proactive.http.port", PAPropertiesType.INTEGER),

    // ---------- COMPONENTS

    /** TODO  cdalmass Describe this property */
    PA_FRACTAL_PROVIDER("fractal.provider", PAPropertiesType.STRING),

    /** TODO  cdalmass Describe this property */
    PA_COMPONENT_CREATION_TIMEOUT("components.creation.timeout",
        PAPropertiesType.INTEGER),

    /** TODO  cdalmass Describe this property */
    PA_COMPONENT_USE_SHORTCUTS("proactive.components.use_shortcuts",
        PAPropertiesType.BOOLEAN),

    // ---------- Migration

    /** TODO Describe this property */
    PA_LOCATION_SERVER("proactive.locationserver", PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_LOCATION_SERVER_RMI("proactive.locationserver.rmi",
        PAPropertiesType.STRING),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_TTL("proactive.mixedlocation.ttl", PAPropertiesType.INTEGER),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_UPDATINGFORWARDER("proactive.mixedlocation.updatingForwarder",
        PAPropertiesType.BOOLEAN),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_MAXMIGRATIONNB("proactive.mixedlocation.maxMigrationNb",
        PAPropertiesType.INTEGER),

    /** TODO fhuet Describe this property */
    PA_MIXEDLOCATION_MAXTIMEONSITE("proactive.mixedlocation.maxTimeOnSite",
        PAPropertiesType.INTEGER),

    // ---------- SSH

    /** this property identifies the default port used by the RMISSH transport layer */
    PA_SSH_PORT("proactive.ssh.port", PAPropertiesType.INTEGER),

    /** this property identifies the location of the known host file for the RMISSH transport layer */
    PA_SSH_KNOWN_HOST("proactive.ssh.known_hosts", PAPropertiesType.STRING),

    /** this property identifies the location of RMISSH key directory */
    PA_SSH_KEY_DIR("proactive.ssh.key_directory", PAPropertiesType.STRING),

    /** this property identifies that when using SSH tunneling, a normal connection should be tried before tunneling */
    PA_SSH_TUNNELING_TRY_NORMAL_FIRST("proactive.tunneling.try_normal_first",
        PAPropertiesType.BOOLEAN),

    /** this property identifies if the garbage collector should be turned on when using SSH tunneling */
    PA_SSH_TUNNELING_USE_GC("proactive.tunneling.use_gc",
        PAPropertiesType.BOOLEAN),

    /** this property identifies the garbage collector period when using SSH tunneling */
    PA_SSH_TUNNELING_GC_PERIOD("proactive.tunneling.gc_period",
        PAPropertiesType.INTEGER),

    /** this property identifies the know hosts file location when using ssh tunneling
     *  if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    PA_SSH_TUNNELING_KNOW_HOSTS("proactive.ssh.known_hosts",
        PAPropertiesType.STRING),

    /** TODO cmathieu Describe this property */
    PA_SSH_TUNNELING_CONNECT_TIMEOUT("proactive.tunneling.connect_timeout",
        PAPropertiesType.INTEGER),

    /** TODO cmathieu Describe this property */
    PA_SSH_USERNAME("proactive.ssh.username", PAPropertiesType.STRING),

    // ------------ Fault Tolerance

    /** TODO cdelbe Describe this property */
    PA_FT("proactive.ft", PAPropertiesType.BOOLEAN),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_CHECKPOINT("proactive.ft.server.checkpoint",
        PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_LOCATION("proactive.ft.server.location",
        PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RECOVERY("proactive.ft.server.recovery",
        PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_GLOBAL("proactive.ft.server.global", PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_FT_TTC("proactive.ft.ttc", PAPropertiesType.INTEGER),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RESOURCE("proactive.ft.server.resource",
        PAPropertiesType.STRING),

    /** TODO cdelbe Describe this property */
    PA_FT_PROTOCOL("proactive.ft.protocol", PAPropertiesType.STRING),

    // ---------- Security

    /** this property indicates if a RMISecurityManager has to be instanciated*/
    PA_SECURITYMANAGER("proactive.securitymanager", PAPropertiesType.BOOLEAN),

    /** this property indicates the location of the runtime' security manager configuration file */
    PA_RUNTIME_SECURITY("proactive.runtime.security", PAPropertiesType.STRING),

    /** this property indicates the url of the security domain the runtime depends on */
    PA_RUNTIME_DOMAIN_URL("proactive.runtime.domain.url",
        PAPropertiesType.STRING),

    // ------------ Timit

    /** TODO vbodnart Describe this property */
    PA_TIMIT_ACTIVATION("proactive.timit.activation", PAPropertiesType.STRING),

    // -------------- Master/Worker

    /** The ping period is the default interval at which workers receive a ping message (to check if they're alive).*/
    PA_MASTERWORKER_PINGPERIOD("proactive.masterworker.pingperiod",
        PAPropertiesType.INTEGER),

    // -------------- P2P

    /** Acquisition method. */
    PA_P2P_ACQUISITION("proactive.p2p.acq", PAPropertiesType.STRING),

    /** Port number. */
    PA_P2P_PORT("proactive.p2p.port", PAPropertiesType.INTEGER),

    /** NOA is in number of peers. */
    PA_P2P_NOA("proactive.p2p.noa", PAPropertiesType.INTEGER),

    /** TTU is in minutes. */
    PA_P2P_TTU("proactive.p2p.ttu", PAPropertiesType.INTEGER),

    /** TTL is in hops. */
    PA_P2P_TTL("proactive.p2p.ttl", PAPropertiesType.INTEGER),

    /** List capacity of message sequence number. */
    PA_P2P_MSG_MEMORY("proactive.p2p.msg_capacity", PAPropertiesType.INTEGER),

    /** Percentage of agree response. */
    PA_P2P_EXPLORING_MSG("proactive.p2p.expl_msg", PAPropertiesType.INTEGER),

    /** Timeout for node acquisition. */
    PA_P2P_NODES_ACQUISITION_T0("proactive.p2p.nodes_acq_to",
        PAPropertiesType.INTEGER),

    /** Lookup frequency for nodes. */
    PA_P2P_LOOKUP_FREQ("proactive.p2p.lookup_freq", PAPropertiesType.INTEGER),

    /** If true deploying one shared nodes by CPU, else only one node is shared. */
    PA_P2P_MULTI_PROC_NODES("proactive.p2p.multi_proc_nodes",
        PAPropertiesType.BOOLEAN),

    /** Path of the xml deployment descriptor, for deploying shared nodes. */
    PA_P2P_XML_PATH("proactive.p2p.xml_path", PAPropertiesType.STRING),

    /** Boolean value for disable node sharing. */
    PA_P2P_NO_SHARING("proactive.p2p.nosharing", PAPropertiesType.BOOLEAN),

    // -------------- DGC

    /** TODO gchazara Describe this property */
    PA_DGC("proactive.dgc", PAPropertiesType.BOOLEAN),

    /**
     * TimeToAlone
     * After this delay, we suppose we got a message from all our referencers.
     */
    PA_DGC_TTA("proactive.dgc.tta", PAPropertiesType.INTEGER),

    /**
     * TimeToBroadcast
     * Time is always in milliseconds. It is fundamental for this value
     * to be the same in all JVM of the distributed system, so think twice
     * before changing it.
     */
    PA_DGC_TTB("proactive.dgc.ttb", PAPropertiesType.INTEGER),

    // -------------- Misc

    /**
     * PRIVATE
     * This flag indicate the VM is running unit or functional tests
     */
    PA_TEST("proactive.test", PAPropertiesType.BOOLEAN),

    /** TODO Describe this property */
    PA_BYTECODEMANIPULATOR("byteCodeManipulator", PAPropertiesType.STRING),

    /** TODO vlegrand Describe this property */
    CATALINA_BASE("catalina.base", PAPropertiesType.STRING),
    PA_UNICORE_FORKCLIENT("proactive.unicore.forkclient",
        PAPropertiesType.BOOLEAN);static final Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private String key;
    private PAPropertiesType type;

    PAProperties(String str, PAPropertiesType type) {
        this.key = str;
        this.type = type;
    }

    /**
     * Returns the key associated to this property
     * @return the key associated to this property
     */
    public String getKey() {
        return key;
    }

    public PAPropertiesType getType() {
        return type;
    }

    /**
     * Returns the value of this property
     * @return the value of this property
     */
    public String getValue() {
        return ProActiveConfiguration.getInstance().getProperty(key);
    }

    public int getValueAsInt() {
        if (type != PAPropertiesType.INTEGER) {
            RuntimeException e = new IllegalArgumentException(key +
                    " is not an integer property. getValueAsInt cannot be called on this property");
            logger.error(e);
            throw e;
        }

        return Integer.parseInt(getValue());
    }

    /**
     * Set the value of this property
     * @param value new value of the property
     */
    public void setValue(String value) {
        ProActiveConfiguration.getInstance().setProperty(key, value);
    }

    public void setValue(Integer i) {
        ProActiveConfiguration.getInstance().setProperty(key, i.toString());
    }

    public void setValue(Boolean bool) {
        ProActiveConfiguration.getInstance().setProperty(key, bool.toString());
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
        if (type != PAPropertiesType.BOOLEAN) {
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
        return type == PAPropertiesType.BOOLEAN;
    }

    public boolean isValid(String value) {
        switch (type) {
        case BOOLEAN:
            if (TRUE.equals(value) || FALSE.equals(value)) {
                return true;
            }
            return false;
        case INTEGER:
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
            }
            return false;
        case STRING:
            return true;
        default:
            return false;
        }
    }

    static public PAProperties getProperty(String key) {
        for (PAProperties prop : PAProperties.values()) {
            if (prop.getKey().equals(key)) {
                return prop;
            }
        }
        return null;
    }
    public enum PAPropertiesType {STRING,
        INTEGER,
        BOOLEAN;
    }
}
