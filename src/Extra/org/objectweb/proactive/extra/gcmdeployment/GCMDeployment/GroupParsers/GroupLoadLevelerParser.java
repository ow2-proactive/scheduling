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

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupLoadLeveler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupLoadLevelerParser extends AbstractGroupParser {
    private static final String NODE_NAME_TASKS_PER_HOST = NODE_EXT_NAMESPACE + "tasksPerHost";
    private static final String NODE_NAME_CPUS_PER_TASK = NODE_EXT_NAMESPACE + "cpusPerTask";
    private static final String NODE_NAME_NB_TASKS = NODE_EXT_NAMESPACE + "nbTasks";
    private static final String NODE_NAME_ARGUMENTS = NODE_EXT_NAMESPACE + "arguments";
    private static final String NODE_NAME_MAX_TIME = NODE_EXT_NAMESPACE + "maxTime";
    private static final String NODE_NAME_RESOURCES = NODE_EXT_NAMESPACE + "resources";
    private static final String NODE_NAME_DIRECTORY = NODE_EXT_NAMESPACE + "directory";
    private static final String NODE_NAME_STDERR = NODE_EXT_NAMESPACE + "stderr";
    private static final String NODE_NAME_STDOUT = NODE_EXT_NAMESPACE + "stdout";
    private static final String ATTR_JOB_NAME = "jobName";
    private static final String NODE_NAME = "loadLevelerGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupLoadLeveler();
    }

    @Override
    public String getBaseNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupLoadLeveler loadLevelerGroup = (GroupLoadLeveler) super.parseGroupNode(groupNode, xpath);

        String jobname = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_NAME);

        loadLevelerGroup.setJobName(jobname);

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
            } else if (nodeName.equals(NODE_NAME_NB_TASKS)) {
                loadLevelerGroup.setNbTasks(nodeValue);
            } else if (nodeName.equals(NODE_NAME_CPUS_PER_TASK)) {
                loadLevelerGroup.setCpusPerTask(nodeValue);
            } else if (nodeName.equals(NODE_NAME_TASKS_PER_HOST)) {
                loadLevelerGroup.setTasksPerHost(nodeValue);
            }
        }

        return loadLevelerGroup;
    }
}
