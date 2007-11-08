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
    private static final String NODE_NAME_TASKS_PER_HOST = "tasksPerHost";
    private static final String NODE_NAME_CPUS_PER_TASK = "cpusPerTask";
    private static final String NODE_NAME_NB_TASKS = "nbTasks";
    private static final String NODE_NAME_ARGUMENTS = "arguments";
    private static final String NODE_NAME_MAX_TIME = "maxTime";
    private static final String NODE_NAME_RESOURCES = "resources";
    private static final String NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String ATTR_JOB_NAME = "jobName";
    private static final String NODE_NAME = "loadLevelerGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupLoadLeveler();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupLoadLeveler loadLevelerGroup = (GroupLoadLeveler) getGroup();

        String jobname = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_JOB_NAME);

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
                    List<String> argList = GCMParserHelper.parseArgumentListNode(xpath,
                            childNode);
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
    }
}
