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

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupCGSP;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupCGSPParser extends AbstractGroupParser {
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME_COUNT = "count";
    private static final String ATTR_QUEUE = "queue";
    private static final String ATTR_HOSTNAME = "hostname";
    private static final String NODE_NAME = "cgspGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupCGSP();
    }

    @Override
    public String getBaseNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupCGSP cgspGroup = (GroupCGSP) super.parseGroupNode(groupNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOSTNAME);
        cgspGroup.setHostName(hostname);

        String queue = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        cgspGroup.setQueue(queue);

        groupNode.getChildNodes();

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);

            if (nodeName.equals(NODE_NAME_COUNT)) {
                cgspGroup.setCount(nodeValue);
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                cgspGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                cgspGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                cgspGroup.setStderr(nodeValue);
            }
        }

        return cgspGroup;
    }
}
