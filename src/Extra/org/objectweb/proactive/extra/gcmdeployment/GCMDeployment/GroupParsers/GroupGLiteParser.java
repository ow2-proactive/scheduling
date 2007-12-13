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

import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGLite;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGLiteParser extends AbstractGroupParser {
    private static final String NODE_NAME = "gLiteGroup";
    private static final String NODE_NAME_ARGUMENTS = NODE_EXT_NAMESPACE + "arguments";
    private static final String NODE_NAME_OUTPUT_SANDBOX = NODE_EXT_NAMESPACE + "outputSandbox";
    private static final String NODE_NAME_INPUT_SANDBOX = NODE_EXT_NAMESPACE + "inputSandbox";
    private static final String NODE_NAME_CONFIG_FILE = NODE_EXT_NAMESPACE + "configFile";
    private static final String NODE_NAME_JDL_REMOTE_FILE_PATH = NODE_EXT_NAMESPACE + "JDLRemoteFilePath";
    private static final String NODE_NAME_JDL_FILE_PATH = NODE_EXT_NAMESPACE + "JDLFilePath";
    private static final String NODE_NAME_STDERR = NODE_EXT_NAMESPACE + "stderr";
    private static final String NODE_NAME_STDIN = NODE_EXT_NAMESPACE + "stdin";
    private static final String NODE_NAME_STDOUT = NODE_EXT_NAMESPACE + "stdout";
    private static final String NODE_NAME_INPUT_DATA = NODE_EXT_NAMESPACE + "inputData";
    private static final String NODE_NAME_RANK = NODE_EXT_NAMESPACE + "rank";
    private static final String NODE_NAME_REQUIREMENTS = NODE_EXT_NAMESPACE + "requirements";
    private static final String NODE_NAME_ENVIRONMENT = NODE_EXT_NAMESPACE + "environment";
    private static final String ATTR_STORAGE_INDEX = "storageIndex";
    private static final String ATTR_DATA_ACCESS_PROTOCOL = "dataAccessProtocol";
    private static final String ATTR_NODES = "nodes";
    private static final String ATTR_MY_PROXY_SERVER = "myProxyServer";
    private static final String ATTR_RETRY_COUNT = "retryCount";
    private static final String ATTR_VIRTUAL_ORGANISATION = "virtualOrganisation";
    private static final String ATTR_OUTPUT_SE = "outputse";
    private static final String ATTR_EXECUTABLE = "executable";
    private static final String ATTR_HOSTNAME = "hostname";
    private static final String ATTR_JDL_FILE_NAME = "JDLFileName";
    private static final String ATTR_JOB_TYPE = "jobType";
    private static final String ATTR_TYPE = "Type";
    private static final String ATTR_DATA_CATALOG = "dataCatalog";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGLite();
    }

    @Override
    public String getBaseNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupGLite gliteGroup = (GroupGLite) super.parseGroupNode(groupNode, xpath);

        String t = GCMParserHelper.getAttributeValue(groupNode, ATTR_TYPE);
        gliteGroup.setJobType(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_TYPE);
        gliteGroup.setJobJobType(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_JDL_FILE_NAME);
        gliteGroup.setFileName(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOSTNAME);
        gliteGroup.setNetServer(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_EXECUTABLE);
        gliteGroup.setJobExecutable(t);
        gliteGroup.setCommandPath(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_OUTPUT_SE);
        gliteGroup.setJobOutputStorageElement(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_VIRTUAL_ORGANISATION);
        gliteGroup.setJobVO(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_RETRY_COUNT);
        gliteGroup.setJobRetryCount(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_MY_PROXY_SERVER);
        gliteGroup.setJobMyProxyServer(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_NODES);
        gliteGroup.setJobNodeNumber(t);

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);
            if (nodeName.equals(NODE_NAME_ENVIRONMENT)) {
                gliteGroup.setJobEnvironment(nodeValue);
            } else if (nodeName.equals(NODE_NAME_REQUIREMENTS)) {
                gliteGroup.setJobRequirements(nodeValue);
            } else if (nodeName.equals(NODE_NAME_RANK)) {
                gliteGroup.setJobRank(nodeValue);
            } else if (nodeName.equals(NODE_NAME_INPUT_DATA)) {
                t = GCMParserHelper.getAttributeValue(child, ATTR_DATA_ACCESS_PROTOCOL);
                gliteGroup.setJobDataAccessProtocol(t);

                t = GCMParserHelper.getAttributeValue(child, ATTR_STORAGE_INDEX);
                gliteGroup.setJobStorageIndex(t);

                t = GCMParserHelper.getAttributeValue(child, ATTR_DATA_CATALOG);
                gliteGroup.setDataCatalog(t);
            } else if (nodeName.equals(NODE_NAME_JDL_FILE_PATH)) {
                gliteGroup.setFilePath(nodeValue);
            } else if (nodeName.equals(NODE_NAME_JDL_REMOTE_FILE_PATH)) {
                gliteGroup.setRemoteFilePath(nodeValue);
                gliteGroup.setJdlRemote(true);
            } else if (nodeName.equals(NODE_NAME_CONFIG_FILE)) {
                gliteGroup.setConfigFile(nodeValue);
                gliteGroup.setConfigFileOption(true);
            } else if (nodeName.equals(NODE_NAME_INPUT_SANDBOX)) {
                String sandbox = nodeValue;
                StringTokenizer st = new StringTokenizer(sandbox);
                while (st.hasMoreTokens()) {
                    gliteGroup.addInputSBEntry(st.nextToken());
                }
            } else if (nodeName.equals(NODE_NAME_OUTPUT_SANDBOX)) {
                String sandbox = nodeValue;
                StringTokenizer st = new StringTokenizer(sandbox);
                while (st.hasMoreTokens()) {
                    gliteGroup.addOutputSBEntry(st.nextToken());
                }
            } else if (nodeName.equals(NODE_NAME_ARGUMENTS)) {
                gliteGroup.setJobArgument(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                gliteGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                gliteGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDIN)) {
                gliteGroup.setStdin(nodeValue);
            }
        }

        return gliteGroup;
    }
}
