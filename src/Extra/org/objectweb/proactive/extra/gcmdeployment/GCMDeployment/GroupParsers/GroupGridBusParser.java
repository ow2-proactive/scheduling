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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGridBus;
import org.w3c.dom.Node;


public class GroupGridBusParser extends AbstractGroupParser {
    private static final String XPATH_ARGUMENTS = NODE_EXT_NAMESPACE + "arguments";
    private static final String NODE_NAME = "gridbusGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGridBus();
    }

    @Override
    public String getBaseNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupGridBus gridbusGroup = (GroupGridBus) super.parseGroupNode(groupNode, xpath);

        try {
            Node argumentsNode = (Node) xpath.evaluate(XPATH_ARGUMENTS, groupNode, XPathConstants.NODE);
            List<String> argumentsList = GCMParserHelper.parseArgumentListNode(xpath, argumentsNode);

            gridbusGroup.setArgumentsList(argumentsList);
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return gridbusGroup;
    }
}
