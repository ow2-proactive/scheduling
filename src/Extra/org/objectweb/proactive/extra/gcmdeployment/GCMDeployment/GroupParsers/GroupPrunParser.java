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
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupPrun;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupPrunParser extends AbstractGroupParser {
    private static final String NODE_NAME_STDOUT = NODE_EXT_NAMESPACE + "stdout";
    private static final String NODE_NAME_WALL_TIME = NODE_EXT_NAMESPACE + "wallTime";
    private static final String NODE_NAME_PROCESSOR_PER_NODE = NODE_EXT_NAMESPACE + "processorPerNode";
    private static final String NODE_NAME_HOSTS_NUMBER = NODE_EXT_NAMESPACE + "hostsNumber";
    private static final String NODE_NAME_HOSTLIST = NODE_EXT_NAMESPACE + "hostList";
    private static final String ATTR_QUEUE = "queue";
    private static final String NODE_NAME = "prunGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupPrun();
    }

    @Override
    public String getBaseNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupPrun prunGroup = (GroupPrun) super.parseGroupNode(groupNode, xpath);

        String queueName = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        prunGroup.setQueueName(queueName);

        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(childNode);
            if (nodeName.equals(NODE_NAME_HOSTLIST)) {
                prunGroup.setHostList(nodeValue);
            } else if (nodeName.equals(NODE_NAME_HOSTS_NUMBER)) {
                prunGroup.setHostsNumber(nodeValue);
            } else if (nodeName.equals(NODE_NAME_PROCESSOR_PER_NODE)) {
                prunGroup.setProcessorPerNodeNumber(nodeValue);
            } else if (nodeName.equals(NODE_NAME_WALL_TIME)) {
                prunGroup.setWallTime(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                prunGroup.setStdout(nodeValue);
            }
        }

        return prunGroup;
    }
}
