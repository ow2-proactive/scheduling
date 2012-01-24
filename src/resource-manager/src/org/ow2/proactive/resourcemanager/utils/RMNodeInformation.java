/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Class represent node a collection of node properties.
 * Namely it collects jvm properties, proactive properties and host properties (from script execution).
 * 
 * All the properties are in json format.
 *
 */
public class RMNodeInformation {

    private final String[] IGNORE_JVM_PROPERTIES = { "line.separator", "java.class.path", "java.library.path" };

    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);

    private ScriptExecutor scriptExecutor;

    private Node node;
    private String jvmInfo;
    private String proactiveInfo;

    public RMNodeInformation(Node node) {
        this.node = node;
        scriptExecutor = new ScriptExecutor();
    }

    /**
     * Collects the JVM information from JVM properties
     */
    private String getJVMInfo() {
        boolean first = true;
        List<String> ignor = Arrays.asList(IGNORE_JVM_PROPERTIES);
        String jInfo = "";
        for (String propertyName : System.getProperties().stringPropertyNames()) {

            if (ignor.contains(propertyName)) {
                continue;
            }

            if (!first) {
                jInfo += ",\n";
            } else {
                first = false;
            }
            String value = System.getProperty(propertyName);
            jInfo += "'" + propertyName + "': '" + value + "'";
        }

        return jInfo;
    }

    /**
     * Collects the Proactive information from about the node
     */
    private String getProactiveInfo() {
        ProActiveRuntime runtime = node.getProActiveRuntime();

        String runtimeInfo = "'runtime': {";
        runtimeInfo += "'url': '" + runtime.getURL() + "',";
        runtimeInfo += "'name': '" + runtime.getVMInformation().getName() + "',";
        runtimeInfo += "'hostname': '" + runtime.getVMInformation().getHostName() + "',";
        runtimeInfo += "'inetaddress': '" + runtime.getVMInformation().getInetAddress() + "'";
        runtimeInfo += "}";

        String nodeInfo = "'node': {";
        nodeInfo += "'url': '" + node.getNodeInformation().getURL() + "',";
        nodeInfo += "'name': '" + node.getNodeInformation().getName() + "',";
        try {
            nodeInfo += "'activeobjects': '" + node.getNumberOfActiveObjects() + "'";
        } catch (NodeException e) {
        }
        nodeInfo += "}";

        return nodeInfo + ", " + runtimeInfo;
    }

    /**
     * @return a node info in JSON format
     */
    public String getNodeInfo() {

        if (jvmInfo == null) {
            jvmInfo = getJVMInfo();
        }

        if (proactiveInfo == null) {
            proactiveInfo = getProactiveInfo();
        }

        String info = "{ 'date': '" + new Date() + "', 'proactive': {" + proactiveInfo + "}, 'jvm': {" +
            jvmInfo + "}, 'host': {" + scriptExecutor.getScriptsResults() + "} }";

        logger.debug("Node Info " + info);
        return info;
    }

    /**
     * @return true if new information about the node is available, false otherwise
     */
    public boolean isUpdated() {
        // true when new info from scripts is available or proactive info changed
        String paInfo = getProactiveInfo();

        if (proactiveInfo == null || !paInfo.equals(proactiveInfo)) {
            proactiveInfo = paInfo;
            return true;
        }

        return scriptExecutor.scriptInfoUpdated();
    }
}
