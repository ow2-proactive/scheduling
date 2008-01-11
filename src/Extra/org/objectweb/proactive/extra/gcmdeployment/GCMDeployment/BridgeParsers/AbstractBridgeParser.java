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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.w3c.dom.Node;


public abstract class AbstractBridgeParser implements BridgeParser {
    static final String ATT_ID = "id";
    static final String ATT_HOSTNAME = "hostname";
    static final String ATT_USERNAME = "username";
    static final String ATT_COMMANDPATH = "commandPath";
    static final String NODE_EXT_NAMESPACE = "";

    public AbstractBridgeParser() {
    }

    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        String value;
        AbstractBridge bridge = createBridge();

        // Mandatory fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_ID);
        bridge.setId(value);
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_HOSTNAME);
        bridge.setHostname(value);

        // Optional fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_USERNAME);
        if (value != null) {
            bridge.setUsername(value);
        }
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_COMMANDPATH);
        if (value != null) {
            bridge.setCommandPath(value);
        }

        return bridge;
    }

    public abstract AbstractBridge createBridge();

    protected abstract String getBaseNodeName();

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
