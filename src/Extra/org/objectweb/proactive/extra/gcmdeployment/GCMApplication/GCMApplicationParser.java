package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;


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
     */
    public Map<String, GCMDeploymentDescriptor> getResourceProviders();

    /**
     * Returns all the Virtual Node
     *
     * @return all the declared Virtual Nodes
     */
    public Map<String, VirtualNodeInternal> getVirtualNodes();

    /**
     * Returns the Command Builder
     *
     * @return the Command Builder associated to this application
     */
    public CommandBuilder getCommandBuilder();
}
