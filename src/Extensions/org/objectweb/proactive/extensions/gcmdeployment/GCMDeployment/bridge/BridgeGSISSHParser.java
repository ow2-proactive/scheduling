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

package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;


public class BridgeGSISSHParser extends BridgeSSHParser {

    static final String NODE_NAME = "gsisshBridge";
    private static final String ATTR_PORT = "port";
    private static final String ATTR_CERTIFICATE = "certificate";

    @Override
    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        BridgeGSISSH bridge = (BridgeGSISSH) super.parseBridgeNode(bridgeNode, xpath);

        String port = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_PORT);

        bridge.setPort(port);

        String certificate = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_CERTIFICATE);

        bridge.setCertificate(certificate);

        return bridge;
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeGSISSH();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

}
