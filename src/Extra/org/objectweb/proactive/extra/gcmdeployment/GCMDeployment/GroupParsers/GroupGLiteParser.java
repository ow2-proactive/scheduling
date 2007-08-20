package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGLite;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGLiteParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupGLite();
    }

    public String getNodeName() {
        return "gliteGroup";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGLite gliteGroup = (GroupGLite) getGroup();

        String t = GCMParserHelper.getAttributeValue(groupNode, "Type");
        gliteGroup.setJobType(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "jobType");
        gliteGroup.setJobJobType(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "JDLFileName");
        gliteGroup.setFileName(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "hostname");
        gliteGroup.setNetServer(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "executable");
        gliteGroup.setJobExecutable(t);
        gliteGroup.setCommandPath(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "stdOutput");
        gliteGroup.setJobStdOutput(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "stdInput");
        gliteGroup.setJobStdInput(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "stdError");
        gliteGroup.setJobStdError(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "outputse");
        gliteGroup.setJobOutputStorageElement(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "virtualOrganisation");
        gliteGroup.setJobVO(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "retryCount");
        gliteGroup.setJobRetryCount(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "myProxyServer");
        gliteGroup.setJobMyProxyServer(t);

        t = GCMParserHelper.getAttributeValue(groupNode, "nodeNumber");
        gliteGroup.setJobNodeNumber(Integer.parseInt(t));

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);
            if (nodeName.equals("environment")) {
                gliteGroup.setJobEnvironment(nodeValue);
            } else if (nodeName.equals("requirements")) {
                gliteGroup.setJobRequirements(nodeValue);
            } else if (nodeName.equals("rank")) {
                gliteGroup.setJobRank(nodeValue);
            } else if (nodeName.equals("inputData")) {
                t = GCMParserHelper.getAttributeValue(child,
                        "dataAccessProtocol");
                gliteGroup.setJobDataAccessProtocol(t);

                t = GCMParserHelper.getAttributeValue(child, "storageIndex");
                gliteGroup.setJobStorageIndex(t);
            } else if (nodeName.equals("gLiteOptions")) {
                NodeList optionChildNodes = child.getChildNodes();
                for (int i = 0; i < optionChildNodes.getLength(); ++i) {
                    Node optionChild = optionChildNodes.item(i);
                    if (optionChild.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    nodeName = optionChild.getNodeName();
                    if (nodeName.equals("JDLFilePath")) {
                        PathElement path = GCMParserHelper.parsePathElementNode(optionChild);
                        gliteGroup.setFilePath(path);
                    } else if (nodeName.equals("JDLRemoteFilePath")) {
                        PathElement path = GCMParserHelper.parsePathElementNode(optionChild);
                        gliteGroup.setRemoteFilePath(path);
                        gliteGroup.setJdlRemote(true);
                    } else if (nodeName.equals("configFile")) {
                        PathElement path = GCMParserHelper.parsePathElementNode(optionChild);
                        gliteGroup.setConfigFile(path);
                        gliteGroup.setConfigFileOption(true);
                    } else {
                        nodeValue = GCMParserHelper.getElementValue(optionChild);
                        if (nodeName.equals("inputSandbox")) {
                            String sandbox = nodeValue;
                            StringTokenizer st = new StringTokenizer(sandbox);
                            while (st.hasMoreTokens()) {
                                gliteGroup.addInputSBEntry(st.nextToken());
                            }
                        } else if (nodeName.equals("outputSandbox")) {
                            String sandbox = nodeValue;
                            StringTokenizer st = new StringTokenizer(sandbox);
                            while (st.hasMoreTokens()) {
                                gliteGroup.addOutputSBEntry(st.nextToken());
                            }
                        } else if (nodeName.equals("arguments")) {
                            gliteGroup.setJobArgument(nodeValue);
                        }
                    }
                }
            }
        }
    }
}
