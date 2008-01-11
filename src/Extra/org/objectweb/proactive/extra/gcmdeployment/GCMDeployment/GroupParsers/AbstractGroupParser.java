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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class AbstractGroupParser implements GroupParser {
    protected static final String NODE_EXT_NAMESPACE = "";

    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        String id = GCMParserHelper.getAttributeValue(groupNode, "id");

        AbstractGroup group = createGroup();

        group.setId(id);

        String username = GCMParserHelper.getAttributeValue(groupNode, "username");
        if (username != null) {
            group.setUsername(username);
        }

        String commandPath = GCMParserHelper.getAttributeValue(groupNode, "commandPath");
        if (commandPath != null) {
            group.setCommandPath(commandPath);
        }

        String bookedNodesAccess = GCMParserHelper.getAttributeValue(groupNode, "bookedNodesAccess");
        if (bookedNodesAccess != null) {
            group.setBookedNodesAccess(bookedNodesAccess);
        }

        try {
            Node environmentNode = (Node) xpath.evaluate("environment", groupNode, XPathConstants.NODE);

            if (environmentNode != null) {
                Map<String, String> envVars = new HashMap<String, String>();

                NodeList argNodes = (NodeList) xpath.evaluate("variable", environmentNode,
                        XPathConstants.NODESET);

                for (int i = 0; i < argNodes.getLength(); ++i) {
                    Node argNode = argNodes.item(i);
                    String name = GCMParserHelper.getAttributeValue(argNode, "name");
                    String value = GCMParserHelper.getAttributeValue(argNode, "value");
                    envVars.put(name, value);
                }

                group.setEnvironment(envVars);
            }

            Node scriptPath = (Node) xpath.evaluate("scriptPath", groupNode, XPathConstants.NODE);

            if (scriptPath != null) {
                group.setScriptPath(GCMParserHelper.parsePathElementNode(scriptPath));
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return group;
    }

    /**
     * Returns the base nodeName associated to a particular parser
     * (no namespace)
     * @return the nodeName as a String
     */
    protected abstract String getBaseNodeName();

    public abstract AbstractGroup createGroup();

    /**
     * Returns the node's XML namespace associated
     * @return the namespace as a String
     */
    protected String getNodeNameSpace() {
        return NODE_EXT_NAMESPACE;
    }

    public String getNodeName() {
        return getNodeNameSpace() + getBaseNodeName();
    }
}
