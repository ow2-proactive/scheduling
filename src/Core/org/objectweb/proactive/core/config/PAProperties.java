package org.objectweb.proactive.core.config;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
public enum PAProperties {

    /** this property identifies the communication protocol */
    PA_COMMUNICATION_PROTOCOL("proactive.communication.protocol", false),

    /** this property allows to override the default java behavior when retrieving the runtime url */
    PA_RUNTIME_IPADDRESS("proactive.runtime.ipaddress", false),

    /** this property allows to set the hostname of a runtime */
    PA_HOSTNAME("proactive.hostname", false),

    /** this property indicates to use the IP address instead of DNS entry for a runtime url */
    PA_USE_IP_ADDRESS("proactive.useIPaddress", true),

    /** this property identifies the default port used by the RMI transport layer */
    PA_RMI_PORT("proactive.rmi.port", false),

    /** this property identifies the default port used by the RMISSH transport layer */
    PA_RMISSH_PORT("proactive.ssh.port", false),

    /** this property identifies the location of the known host file for the RMISSH transport layer */
    PA_RMISSH_KNOWN_HOST("proactive.ssh.known_hosts", false),

    /** this property identifies the location of RMISSH key directory */
    PA_RMISSH_KEY_DIR("proactive.ssh.key_directory", false),

    /** this property identifies the default port for the xml-http protocol  */
    PA_XMLHTTP_PORT("proactive.http.port", false),

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

    /** this property identifies proactive's configuration file */
    PA_CONFIGURATION_FILE("proactive.configuration", false),

    /** this property indicates to create for each ProActive object a MBeans */
    PA_JMX_MBEAN("proactive.jmx.mbean", true),

    /** this property indicates to send JMX notifications */
    PA_JMX_NOTIFICATION("proactive.jmx.notification", true),

    /** TODO Describe this property */
    PA_LOCATION_SERVER("proactive.locationserver", false),

    /** TODO cdelbe Describe this property */
    PA_LOCATION_SERVER_RMI("proactive.locationserver.rmi", false),

    /** TODO cdelbe Describe this property */
    PA_FUTURE_AC("proactive.future.ac", true),

    /** TODO cdelbe Describe this property */
    SCHEMA_VALIDATION("schema.validation", true),
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

    /** TODO vlegrand Describe this property */
    PA_HTTP_SERVLET("proactive.http.servlet", true),

    /** this property indicates if the ProActive class loader must be used */
    PA_CLASSLOADER("proactive.classloader", true),

    /** TODO  cdalmass Describe this property */
    PA_FRACTAL_PROVIDER("fractal.provider", false),

    /** TODO  cdalmass Describe this property */
    PA_COMPONENT_CREATION_TIMEOUT("components.creation.timeout", false),

    /** TODO acontes Describe this property */
    PA_RUNTIME_SECURITY("proactive.runtime.security", false),

    /** TODO acontes Describe this property */
    PA_RUNTIME_DOMAIN_URL("proactive.runtime.domain.url", false),

    /** TODO acontes Describe this property */
    PA_RUNTIME_STAYALIVE("proactive.runtime.stayalive", true),

    /** this property indicates the location of the log4j configuration file */
    LOG4J("log4j.configuration", false),

    /** TODO cmathieu Describe this property */
    LOG4J_DEFAULT_INIT_OVERRIDE("log4j.defaultInitOverride", true),

    /** this property indicates the location of the java Security policy file */
    SECURITY_POLICY("java.security.policy", false),

    /** TODO fviale Describe this property */
    PA_MASTERSLAVE_PINGPERIOD("proactive.masterslave.pingperiod", false),

    /** TODO vlegrand Describe this property */
    CATALINA_BASE("catalina.base", false);static final Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private String key;
    private boolean isBoolean;

    PAProperties(String str, boolean isBoolean) {
        this.key = str;
        this.isBoolean = isBoolean;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return ProActiveConfiguration.getInstance().getProperty(key);
    }

    public void setValue(String value) {
        ProActiveConfiguration.getInstance().setProperty(key, value);
    }

    public String toString() {
        return key + "=" + getValue();
    }

    public boolean isSet() {
        return ProActiveConfiguration.getInstance().getProperty(key) != null;
    }

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

        logger.warn(key + "is a boolean property but its value is nor " + TRUE +
            " nor " + FALSE + " " + "(" + val + "). ");
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
}
