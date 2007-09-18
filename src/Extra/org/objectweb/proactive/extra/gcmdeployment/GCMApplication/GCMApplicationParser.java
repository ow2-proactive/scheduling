package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.xml.sax.SAXException;


/**
 * A parser for the GCM Application descriptor schema.
 *
 * @author cmathieu
 *
 */
public interface GCMApplicationParser extends GCMParserConstants {

    /**
     * Returns all the Resources Providers
     * Descriptor
     *
     * @return all the declared Resources Providers as ResourceProviderParams
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, GCMDeploymentDescriptor> getResourceProviders()
        throws SAXException, IOException;

    /**
     * Returns all the Virtual Node
     *
     * @return all the declared Virtual Nodes
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, VirtualNodeInternal> getVirtualNodes()
        throws SAXException, IOException;

    /**
     * Returns the Command Builder
     *
     * @return the Command Builder associated to this application
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public CommandBuilder getCommandBuilder()
        throws XPathExpressionException, SAXException, IOException;
}
