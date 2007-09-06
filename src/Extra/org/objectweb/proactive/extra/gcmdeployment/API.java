package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorImpl;


public class API {
    public static GCMApplicationDescriptor getGCMApplicationDescriptor(
        File file) {
        return new GCMApplicationDescriptorImpl(file);
    }
}
