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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupLoadLevelerParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME_ARGUMENTS = "arguments";
    private static final String NODE_NAME_MAX_TIME = "maxTime";
    private static final String NODE_NAME_RESOURCES = "resources";
    private static final String NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME = "loadLevelerGroup";
    private static final String TASK_REPARTITION = "taskRepartition";

    public static final String LL_TASK_MODE_BLOCKING = "block";
    public static final String LL_TASK_MODE_TOTAL_TASKS = "totalTasks";
    public static final String LL_TASK_MODE_TASKS_PER_NODE = "tasksPerNode";
    public static final String LL_TASK_MODE_GEOMETRY = "geometry";

    public static final String LL_TASK_REP_BLOCKING = "blocking";
    public static final String LL_TASK_REP_NODE = "node";
    public static final String LL_TASK_REP_TASKS_PER_NODE = "tasks_per_node";
    public static final String LL_TASK_REP_TASK_GEOMETRY = "task_geometry";
    public static final String LL_TASK_REP_TOTAL_TASKS = "total_tasks";

    public static final String LL_TASK_MODE_SIMPLE_NBTASKS = "nbTasks";
    public static final String LL_TASK_MODE_SIMPLE_CPUS_PER_TASKS = "cpusPerTasks";
    public static final String LL_TASK_MODE_SIMPLE_TASKS_PER_HOSTS = "tasksPerHosts";

    @Override
    public AbstractGroup createGroup() {
        return new GroupLoadLeveler();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupLoadLeveler loadLevelerGroup = (GroupLoadLeveler) super.parseGroupNode(groupNode, xpath);

        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(childNode);

            if (nodeName.equals(NODE_NAME_STDOUT)) {
                loadLevelerGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                loadLevelerGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                loadLevelerGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_RESOURCES)) {
                loadLevelerGroup.setResources(nodeValue);
            } else if (nodeName.equals(NODE_NAME_MAX_TIME)) {
                loadLevelerGroup.setMaxTime(nodeValue);
            } else if (nodeName.equals(NODE_NAME_ARGUMENTS)) {
                try {
                    List<String> argList = GCMParserHelper.parseArgumentListNode(xpath, childNode);
                    loadLevelerGroup.setArgumentList(argList);
                } catch (XPathExpressionException e) {
                    GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
                }
            } else if (nodeName.equals(TASK_REPARTITION)) {
                this.parseLoadLevelerTaskRepartition(loadLevelerGroup, childNode);
            }
        }

        return loadLevelerGroup;
    }

    public void parseLoadLevelerTaskRepartition(GroupLoadLeveler loadLevelerGroup, Node taskRepartitionNode) {

        NodeList taskRepartitionChildNodes = taskRepartitionNode.getChildNodes();

        for (int j = 0; j < taskRepartitionChildNodes.getLength(); ++j) {
            Node taskRepartitionChildNode = taskRepartitionChildNodes.item(j);
            if (taskRepartitionChildNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (taskRepartitionChildNode.getNodeName().equals(LL_TASK_MODE_BLOCKING)) {
                final NodeList childNodes = taskRepartitionChildNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = GCMParserHelper.getElementValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_BLOCKING)) {
                        loadLevelerGroup.setBlocking(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TOTAL_TASKS)) {
                        loadLevelerGroup.setTotalTasks(nodeValue);
                    }
                }
            } else if (taskRepartitionChildNode.getNodeName().equals(LL_TASK_MODE_TOTAL_TASKS)) {
                final NodeList childNodes = taskRepartitionChildNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = GCMParserHelper.getElementValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_NODE)) {
                        loadLevelerGroup.setNode(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TOTAL_TASKS)) {
                        loadLevelerGroup.setTotalTasks(nodeValue);
                    }
                }
            } else if (taskRepartitionChildNode.getNodeName().equals(LL_TASK_MODE_TASKS_PER_NODE)) {
                final NodeList childNodes = taskRepartitionChildNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = GCMParserHelper.getElementValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_NODE)) {
                        loadLevelerGroup.setNode(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TASKS_PER_NODE)) {
                        loadLevelerGroup.setTasksPerNode(nodeValue);
                    }
                }
            } else if (taskRepartitionChildNode.getNodeName().equals(LL_TASK_MODE_GEOMETRY)) {
                final NodeList childNodes = taskRepartitionChildNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = GCMParserHelper.getElementValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_TASK_GEOMETRY)) {
                        loadLevelerGroup.setTaskGeometry(nodeValue);
                    }
                }
            }
        }
    }
}
