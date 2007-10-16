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
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupUnicore;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Not used
 * @author glaurent
 *
 */
public class GroupUnicoreParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupUnicore();
    }

    public String getNodeName() {
        return "unicoreGroup";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupUnicore unicoreGroup = (GroupUnicore) getGroup();

        String t = GCMParserHelper.getAttributeValue(groupNode, "jobname");
        unicoreGroup.uParam.setUsiteName(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "keypassword");
        unicoreGroup.uParam.setKeyPassword(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "submitjob");
        unicoreGroup.uParam.setSubmitJob(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "savejob");
        unicoreGroup.uParam.setSaveJob(t);

        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            if (nodeName.equals("unicoreDirPath")) {
                PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                unicoreGroup.uParam.setUnicoreDir(path);
            } else if (nodeName.equals("keyFilePath")) {
                PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                unicoreGroup.uParam.setKeyFilePath(path);
            } else if (nodeName.equals("unicoreOption")) {
                NodeList grandChildren = childNode.getChildNodes();
                for (int j = 0; j < grandChildren.getLength(); ++j) {
                    Node grandChildNode = grandChildren.item(j);
                    if (grandChildNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String grandChildNodeName = grandChildNode.getNodeName();

                    if (grandChildNodeName.equals("usite")) {
                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "name");
                        unicoreGroup.uParam.setUsiteName(t);

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "type");
                        unicoreGroup.uParam.setUsiteType(t);

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "url");
                        unicoreGroup.uParam.setUsiteUrl(t);
                    } else if (grandChildNodeName.equals("vsite")) {
                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "name");
                        unicoreGroup.uParam.setVsiteName(t);

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "nodes");
                        unicoreGroup.uParam.setVsiteNodes(Integer.parseInt(t));

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "processors");
                        unicoreGroup.uParam.setVsiteProcessors(Integer.parseInt(
                                t));

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "memory");
                        unicoreGroup.uParam.setVsiteMemory(Integer.parseInt(t));

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "runtime");
                        unicoreGroup.uParam.setVsiteRuntime(Integer.parseInt(t));

                        t = GCMParserHelper.getAttributeValue(grandChildNode,
                                "priority");
                        unicoreGroup.uParam.setVsitePriority(t);
                    }
                }
            }
        }
    }
}
