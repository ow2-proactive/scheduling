package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.IOException;

import org.xml.sax.SAXException;


public class GCMDeploymentDescriptorFactory {
    public static GCMDeploymentDescriptor createDescriptor(
        GCMDeploymentDescriptorParams params) throws SAXException, IOException {
        return new GCMDeploymentDescriptorImpl(params.getGCMDescriptor(),
            params.getFtBlocks());
    }
}
