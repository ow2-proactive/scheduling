package org.objectweb.proactive.core.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Names used in the creation of ProActive ObjectNames.
 * @author ProActiveRuntime
 */
public class FactoryName {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX);
    public static final String OS = "java.lang:type=OperatingSystem";
    public static final String NODE_TYPE = "Node";
    public static final String NODE = "org.objectweb.proactive.core.node:type=" +
        NODE_TYPE;
    public static final String HOST_TYPE = "Host";
    public static final String HOST = "org.objectweb.proactive.core.host:type=" +
        HOST_TYPE;
    public static final String RUNTIME_TYPE = "Runtime";
    public static final String RUNTIME = "org.objectweb.proactive.core.runtimes:type=" +
        RUNTIME_TYPE;
    public static final String VIRTUAL_NODE_TYPE = "VirtualNode";
    public static final String VIRTUAL_NODE = "org.objectweb.proactive.core.virtualnode:type=" +
        VIRTUAL_NODE_TYPE;
    public static final String AO_TYPE = "AO";
    public static final String AO = "org.objectweb.proactive.core.body:type=" +
        AO_TYPE;

    /**
     * Creates a ObjectName corresponding to an active object.
     * @param id The unique id of the active object.
     * @return The ObjectName corresponding to the given id.
     */
    public static ObjectName createActiveObjectName(UniqueID id) {
        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.AO + ", id=" +
                    id.toString().replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the active object", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the active object", e);
        }
        return oname;
    }

    /**
     * Creates a ObjectName corresponding to a node.
     * @param runtimeUrl The url of the ProActive Runtime.
     * @param nodeName The name of the node
     * @return The ObjectName corresponding to the given id.
     */
    public static ObjectName createNodeObjectName(String runtimeUrl,
        String nodeName) {
        runtimeUrl = getCompleteUrl(runtimeUrl);

        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.NODE + ",runtimeUrl=" +
                    runtimeUrl.replace(':', '-') + ", nodeName=" +
                    nodeName.replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the node", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the node", e);
        }
        return oname;
    }

    /**
     * Creates a ObjectName corresponding to a ProActiveRuntime.
     * @param url The url of the ProActiveRuntime.
     * @return The ObjectName corresponding to the given url.
     */
    public static ObjectName createRuntimeObjectName(String url) {
        url = getCompleteUrl(url);

        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.RUNTIME + ",url=" +
                    url.replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the runtime", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the runtime", e);
        }
        return oname;
    }

    /**
     * Creates a ObjectName corresponding to a Virutal Node.
     * @param name The name of the Virutal Node.
     * @param jobID The jobID of the Virutal Node.
     * @return The ObjectName corresponding to the Virutal Node.
     */
    public static ObjectName createVirtualNodeObjectName(String name,
        String jobID) {
        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.VIRTUAL_NODE + ",name=" +
                    name.replace(':', '-') + ", jobID=" +
                    jobID.replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the virtual node", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the virtual node", e);
        }
        return oname;
    }

    /**
     * Creates a complete url 'protocol://host:port/path'
     * @param url
     * @return A complete url
     */
    public static String getCompleteUrl(String url) {
        String host = UrlBuilder.getHostNameFromUrl(url);
        String name = UrlBuilder.getNameFromUrl(url);
        String protocol = UrlBuilder.getProtocol(url);
        int port = UrlBuilder.getPortFromUrl(url);

        String newUrl = UrlBuilder.buildUrl(host, name, protocol, port);

        return newUrl;
    }
}
