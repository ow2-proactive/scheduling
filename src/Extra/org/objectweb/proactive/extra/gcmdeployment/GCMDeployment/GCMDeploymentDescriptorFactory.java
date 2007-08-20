package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

public class GCMDeploymentDescriptorFactory {
    public static GCMDeploymentDescriptor createDescriptor(
        GCMDeploymentDescriptorParams params) {
        return new GCMDeploymentDescriptorImpl(params.getGCMDescriptor(),
            params.getFtBlocks());
    }
}
