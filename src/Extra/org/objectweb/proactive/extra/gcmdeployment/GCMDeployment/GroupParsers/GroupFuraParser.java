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
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupFura;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupFuraParser extends AbstractGroupParser {
    private static final String NODE_NAME_MAXTIME = "maxtime";
    private static final String NODE_NAME_STDIN = "stdin";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_DESCRIPTION = "description";
    private static final String ATTR_JOB_NAME = "jobName";
    private static final String NODE_NAME = "furaGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupFura();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupFura furaGroup = (GroupFura) super.parseGroupNode(groupNode, xpath);

        String jobName = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_NAME);
        furaGroup.setJobName(jobName);

        groupNode.getChildNodes();

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);

            if (nodeName.equals(NODE_NAME_DESCRIPTION)) {
                furaGroup.setDescription(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                furaGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                furaGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDIN)) {
                furaGroup.setStdin(nodeValue);
            } else if (nodeName.equals(NODE_NAME_MAXTIME)) {
                furaGroup.setMaxTime(nodeValue);
            }
        }

        return furaGroup;
    }
}
