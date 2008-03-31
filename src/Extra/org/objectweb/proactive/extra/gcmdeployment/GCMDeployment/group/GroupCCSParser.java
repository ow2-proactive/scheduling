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


public class GroupCCSParser extends AbstractGroupSchedulerParser {

    private static final String NODE_NAME = "ccsGroup";
    private static final String NODE_NAME_RESOURCES = "resources";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_STDERR = "stderr";

    private static final String ATTR_RESOURCES_CPUS = "cpus";
    private static final String ATTR_RESOURCES_WALLTIME = "walltime";

    @Override
    public AbstractGroup createGroup() {
        return new GroupCCS();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupCCS ccsGroup = (GroupCCS) super.parseGroupNode(groupNode, xpath);

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
                    ccsGroup.setResources(nodeValue);
                    if (childNode.hasAttributes()) {
                        GCMD_LOGGER
                                .warn(NODE_NAME_RESOURCES +
                                    "tag has both attributes and value. It's probably a mistake. Attributes are IGNORED");
                    }
                } else {

                    String cpus = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_CPUS);
                    if (cpus != null) {
                        ccsGroup.setCpus(Integer.parseInt(cpus));
                    }

                    String walltime = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_WALLTIME);
                    if (walltime != null) {
                        ccsGroup.setWallTime(walltime);
                    }

                }
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                ccsGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                ccsGroup.setStderr(nodeValue);
            }
        }
        return ccsGroup;
    }
}
