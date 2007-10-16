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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGridEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGridEngineParser extends AbstractGroupParser {
    private static final String NODE_NAME_SCRIPT_PATH = "scriptPath";
    private static final String NODE_NAME_BOOKING_DURATION = "bookingDuration";
    private static final String NODE_NAME_PARALLEL_ENVIRONMENT = "parallelEnvironment";
    private static final String NODE_NAME_HOSTS_NUMBER = "hostsNumber";
    private static final String XPATH_GRID_ENGINE_OPTION = "gridEngineOption";
    private static final String ATTR_QUEUE = "queue";
    private static final String NODE_NAME = "gridEngineGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGridEngine();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGridEngine gridGroup = (GroupGridEngine) getGroup();

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_QUEUE);
        gridGroup.setQueueName(queueName);

        try {
            Node optionNode = (Node) xpath.evaluate(XPATH_GRID_ENGINE_OPTION,
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals(NODE_NAME_HOSTS_NUMBER)) {
                    gridGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_PARALLEL_ENVIRONMENT)) {
                    gridGroup.setParallelEnvironment(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_BOOKING_DURATION)) {
                    gridGroup.setBookingDuration(nodeExpandedValue);
                    //                    } else if (nodeName.equals(OUTPUT_FILE)) {
                    //                        gridGroup.setOutputFile(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_SCRIPT_PATH)) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    gridGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
