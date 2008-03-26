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
package org.objectweb.proactive.core.jmx.naming;

import java.net.URI;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Names used in the creation of ProActive ObjectNames.
 * @author The ProActive Team
 */
public class FactoryName {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX);
    public static final String OS = "java.lang:type=OperatingSystem";
    public static final String NODE_TYPE = "Node";
    public static final String NODE = "org.objectweb.proactive.core.node:type=" + NODE_TYPE;
    public static final String HOST_TYPE = "Host";
    public static final String HOST = "org.objectweb.proactive.core.host:type=" + HOST_TYPE;
    public static final String RUNTIME_TYPE = "Runtime";
    public static final String RUNTIME = "org.objectweb.proactive.core.runtimes:type=" + RUNTIME_TYPE;
    public static final String VIRTUAL_NODE_TYPE = "VirtualNode";
    public static final String VIRTUAL_NODE = "org.objectweb.proactive.core.virtualnode:type=" +
        VIRTUAL_NODE_TYPE;
    public static final String AO_TYPE = "AO";
    public static final String AO = "org.objectweb.proactive.core.body:type=" + AO_TYPE;
    public static final String RUNTIME_URL_PROPERTY = "runtimeUrl";
    public static final String VIRTUAL_NODE_JOBID_PROPERTY = "jobID";
    public static final String VIRTUAL_NODE_NAME_PROPERTY = "vnName";
    public static final String NODE_NAME_PROPERTY = "nodeName";
    public static final String AO_ID_PROPERTY = "aoID";

    /**
     * Creates a ObjectName corresponding to an active object.
     * @param id The unique id of the active object.
     * @return The ObjectName corresponding to the given id.
     */
    public static ObjectName createActiveObjectName(UniqueID id) {
        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.AO + "," + AO_ID_PROPERTY + "=" +
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
    public static ObjectName createNodeObjectName(String runtimeUrl, String nodeName) {
        runtimeUrl = getCompleteUrl(runtimeUrl);

        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.NODE + "," + RUNTIME_URL_PROPERTY + "=" +
                runtimeUrl.replace(':', '-') + "," + NODE_NAME_PROPERTY + "=" + nodeName.replace(':', '-'));
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
            oname = new ObjectName(FactoryName.RUNTIME + "," + RUNTIME_URL_PROPERTY + "=" +
                url.replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the runtime", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the runtime", e);
        }
        return oname;
    }

    /**
     * Creates a ObjectName corresponding to a Virtual Node.
     * @param name The name of the Virtual Node.
     * @param jobID The jobID of the Virtual Node.
     * @return The ObjectName corresponding to the Virtual Node.
     */
    public static ObjectName createVirtualNodeObjectName(String name, String jobID) {
        ObjectName oname = null;
        try {
            oname = new ObjectName(FactoryName.VIRTUAL_NODE + "," + VIRTUAL_NODE_NAME_PROPERTY + "=" +
                name.replace(':', '-') + "," + VIRTUAL_NODE_JOBID_PROPERTY + "=" + jobID.replace(':', '-'));
        } catch (MalformedObjectNameException e) {
            logger.error("Can't create the objectName of the virtual node", e);
        } catch (NullPointerException e) {
            logger.error("Can't create the objectName of the virtual node", e);
        }
        return oname;
    }

    /**
     * Return the JMX Server Name used for a given url of a runtime
     * @param runtimeUrl
     * @return The JMX Server Name
     */
    public static String getJMXServerName(String runtimeUrl) {
        return URIBuilder.getNameFromURI(runtimeUrl);
    }

    /**
     * Return the JMX Server Name used for a given uri of a runtime
     * @param runtimeUrl
     * @return The JMX Server Name
     */
    public static String getJMXServerName(URI runtimeURI) {
        return URIBuilder.getNameFromURI(runtimeURI);
    }

    /**
     * Creates a complete url 'protocol://host:port/path'
     * @param url
     * @return A complete url
     */
    public static String getCompleteUrl(String url) {
        URI uri = URI.create(url);
        URI expandedURI = RemoteObjectHelper.expandURI(uri);
        return expandedURI.toString();
    }
}
