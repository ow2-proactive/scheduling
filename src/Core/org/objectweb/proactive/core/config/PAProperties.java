package org.objectweb.proactive.core.config;

public enum PAProperties {

    /** this property identifies the communication protocol */
    PA_COMMUNICATION_PROTOCOL("proactive.communication.protocol"),

    /** this property allows to override the default java behavior when retrieving the runtime url */
    PA_RUNTIME_IPADDRESS("proactive.runtime.ipaddress"),

    /** this property allows to set the hostname of a runtime */
    PA_HOSTNAME("proactive.hostname"),

    /** this property indicates to use the IP address instead of DNS entry for a runtime url */
    PA_USE_IP_ADDRESS("proactive.useIPaddress"),

    /** this property identifies the default port used by the RMI transport layer */
    PA_RMI_PORT("proactive.rmi.port"),
    /** this property identifies the default port used by the RMISSH transport layer */
    PA_RMISSH_PORT("proactive.ssh.port"),

    /** this property identifies the location of the known host file for the RMISSH transport layer */
    PA_RMISSH_KNOWN_HOST("proactive.ssh.known_hosts"),

    /** this property identifies the location of RMISSH key directory */
    PA_RMISSH_KEY_DIR("proactive.ssh.key_directory"),

    /** this property identifies the default port for the xml-http protocol  */
    PA_XMLHTTP_PORT("proactive.http.port"),

    /** this property identifies that when using SSH tunneling, a normal connection should be tried before tunneling */
    PA_SSH_TUNNELING_TRY_NORMAL_FIRST("proactive.tunneling.try_normal_first"),

    /** this property identifies if the garbage collector should be turned on when using SSH tunneling */
    PA_SSH_TUNNELING_USE_GC("proactive.tunneling.use_gc"),

    /** this property identifies the garbage collector period when using SSH tunneling */
    PA_SSH_TUNNELING_GC_PERIOD("proactive.tunneling.gc_period"),

    /** this property identifies the know hosts file location when using ssh tunneling
     *  if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    PA_SSH_TUNNELING_KNOW_HOSTS("proactive.ssh.known_hosts"),

    /** this property identifies proactive's configuration file */
    PA_CONFIGURATION_FILE("proactive.configuration"),

    /** this property indicates to create for each ProActive object a MBeans */
    PA_JMX_MBEAN("proactive.jmx.mbean"),

    /** this property indicates to send JMX notifications */
    PA_JMX_NOTIFICATION("proactive.jmx.notification"),

    /** TODO Describe this property */
    PA_LOCATION_SERVER("proactive.locationserver"),

    /** TODO cdelbe Describe this property */
    PA_LOCATION_SERVER_RMI("proactive.locationserver.rmi"),

    /** TODO cdelbe Describe this property */
    PA_FUTURE_AC("proactive.future.ac"),
    /** TODO cdelbe Describe this property */
    SCHEMA_VALIDATION("schema.validation"),
    /** TODO cdelbe Describe this property */
    PA_FT("proactive.ft"),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_CHECKPOINT("proactive.ft.server.checkpoint"),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_LOCATION("proactive.ft.server.location"),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RECOVERY("proactive.ft.server.recovery"),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_GLOBAL("proactive.ft.server.global"),

    /** TODO cdelbe Describe this property */
    PA_FT_TTC("proactive.ft.ttc"),

    /** TODO cdelbe Describe this property */
    PA_FT_SERVER_RESOURCE("proactive.ft.server.resource"),

    /** TODO cdelbe Describe this property */
    PA_FT_PROTOCOL("proactive.ft.protocol"),
    /** this property indicates the location of the log4j configuration file */
    LOG4J("log4j.configuration"),
    /** this property indicates the location of the java Security policy file */
    SECURITY_POLICY("java.security.policy");private String key;

    PAProperties(String str) {
        this.key = str;
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
        return getValue() + "=" + getValue();
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
