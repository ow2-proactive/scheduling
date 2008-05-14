package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;


public abstract class AbstractGroupSchedulerParser extends AbstractTupleParser {

    private static final String XPATH_SCRIPTPATH = "dep:scriptPath";

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        AbstractGroup group = super.parseGroupNode(groupNode, xpath);

        String bookedNodesAccess = GCMParserHelper.getAttributeValue(groupNode, "bookedNodesAccess");
        if (bookedNodesAccess != null) {
            group.setBookedNodesAccess(bookedNodesAccess);
        }

        try {

            Node scriptPath = (Node) xpath.evaluate(XPATH_SCRIPTPATH, groupNode, XPathConstants.NODE);

            if (scriptPath != null) {
                group.setScriptPath(GCMParserHelper.parsePathElementNode(scriptPath));
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return group;
    }

}
