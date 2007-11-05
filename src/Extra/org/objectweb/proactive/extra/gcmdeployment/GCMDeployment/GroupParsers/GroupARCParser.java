package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupARC;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGLite;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupARCParser extends AbstractGroupParser {
    private static final String NODE_NAME = "arcGroup";
    private static final String ATTR_JOB_NAME = "jobName";

    @Override
    public AbstractGroup createGroup() {
        return new GroupARC();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);
        GroupARC arcGroup = (GroupARC) getGroup();

        try {
            String t = GCMParserHelper.getAttributeValue(groupNode,
                    ATTR_JOB_NAME);
            arcGroup.setJobName(t);

            NodeList childNodes = groupNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                String nodeValue = GCMParserHelper.getElementValue(child);

                if (nodeName.equals("count")) {
                    arcGroup.setCount(nodeValue);
                } else if (nodeName.equals("arguments")) {
                    List<String> args = GCMParserHelper.parseArgumentListNode(xpath,
                            child);
                    arcGroup.setArguments(args);
                } else if (nodeName.equals("inputFiles")) {
                    List<FileTransfer> inputFilesList = parseFileTransferList(child,
                            xpath);
                    arcGroup.setInputFiles(inputFilesList);
                } else if (nodeName.equals("outputFiles")) {
                    List<FileTransfer> outputFilesList = parseFileTransferList(child,
                            xpath);
                    arcGroup.setOutputFiles(outputFilesList);
                } else if (nodeName.equals("stdout")) {
                    arcGroup.setStdout(nodeValue);
                } else if (nodeName.equals("stderr")) {
                    arcGroup.setStderr(nodeValue);
                } else if (nodeName.equals("stdin")) {
                    arcGroup.setStdin(nodeValue);
                } else if (nodeName.equals("maxTime")) {
                    arcGroup.setMaxTime(nodeValue);
                } else if (nodeName.equals("notify")) {
                    arcGroup.setNotify(nodeValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
        }
    }

    public class FileTransfer {
        public String filename;
        public String location;

        FileTransfer(Node fileTransferNode) {
            filename = GCMParserHelper.getAttributeValue(fileTransferNode,
                    "filename");
            location = GCMParserHelper.getAttributeValue(fileTransferNode,
                    "location");
        }
    }

    protected List<FileTransfer> parseFileTransferList(
        Node fileTransferListNode, XPath xpath) throws XPathExpressionException {
        List<FileTransfer> res = new ArrayList<FileTransfer>();

        NodeList ftNodes = (NodeList) xpath.evaluate("paext:transfer",
                fileTransferListNode, XPathConstants.NODESET);

        for (int i = 0; i < ftNodes.getLength(); ++i) {
            Node ftNode = ftNodes.item(i);
            FileTransfer ft = new FileTransfer(ftNode);
            res.add(ft);
        }
        return res;
    }
}
