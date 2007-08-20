package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserConstants;


public interface GCMDeploymentParser extends GCMParserConstants {
    public GCMDeploymentEnvironment getEnvironment();

    /**
     * Returns the infrastructure declared by the descriptor
         *
         * @see GCMDeploymentInfrastructure
     * @return
     */
    public GCMDeploymentInfrastructure getInfrastructure();

    /**
     * Returns the resources declared by the descriptor
     *
     * @see GCMDeploymentResources
     * @return
     */
    public GCMDeploymentResources getResources();
}
