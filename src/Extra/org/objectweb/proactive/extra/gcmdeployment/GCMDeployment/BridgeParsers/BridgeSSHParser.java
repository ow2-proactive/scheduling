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
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.BridgeSSH;
import org.w3c.dom.Node;


public class BridgeSSHParser extends AbstractBridgeParser {
    private static final String ATTR_USERNAME = "username";
    private static final String ATTR_HOSTNAME = "hostname";
    static final String NODE_NAME = "sshBridge";

    @Override
    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        BridgeSSH bridge = (BridgeSSH) super.parseBridgeNode(bridgeNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(bridgeNode,
                ATTR_HOSTNAME);
        String username = GCMParserHelper.getAttributeValue(bridgeNode,
                ATTR_USERNAME);

        BridgeSSH bridgeSSH = ((BridgeSSH) bridge);
        bridgeSSH.setHostname(hostname);
        bridgeSSH.setUsername(username);

        return bridge;
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeSSH();
    }

    public String getBaseNodeName() {
        return NODE_NAME;
    }
}
