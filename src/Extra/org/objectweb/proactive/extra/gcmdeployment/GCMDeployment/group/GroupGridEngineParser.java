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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group;

import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGridEngineParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME_RESOURCES = "resources";

    private static final String NODE_NAME_STDOUT = "stdout";
    private static final Object NODE_NAME_STDERR = "stderr";
    private static final Object NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME = "gridEngineGroup";

    private static final String ATTR_QUEUE = "queue";
    private static final String ATTR_JOBNAME = "jobName";

    private static final String ATTR_RESOURCES_PARALLEL_ENVIRONMENT = "parallelEnvironment";
    private static final String ATTR_RESOURCES_NODES = "nodes";
    private static final String ATTR_RESOURCES_WALLTIME = "walltime";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGridEngine();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupGridEngine gridGroup = (GroupGridEngine) super.parseGroupNode(groupNode, xpath);

        String queueName = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        gridGroup.setQueue(queueName);

        String jobName = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOBNAME);
        if (jobName != null) {
            gridGroup.setJobName(jobName);
        }

        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(childNode);

            if (nodeName.equals(NODE_NAME_RESOURCES)) {
                if ((nodeValue != null) && (nodeValue.trim().length() != 0)) {
                    gridGroup.setResources(nodeValue);
                    if (childNode.hasAttributes()) {
                        GCMD_LOGGER
                                .warn(NODE_NAME_RESOURCES +
                                    "tag has both attributes and value. It's probably a mistake. Attributes are IGNORED");
                    }
                } else {
                    String nodes = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_NODES);
                    if (nodes != null) {
                        gridGroup.setNodes(nodes);
                    }

                    String pe = GCMParserHelper.getAttributeValue(childNode,
                            ATTR_RESOURCES_PARALLEL_ENVIRONMENT);
                    if (pe != null) {
                        gridGroup.setParallelEnvironment(pe);
                    }

                    String walltime = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_WALLTIME);
                    if (walltime != null) {
                        gridGroup.setWallTime(walltime);
                    }
                }
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                gridGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                gridGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                gridGroup.setStderr(nodeValue);
            }
        }

        return gridGroup;
    }
}
