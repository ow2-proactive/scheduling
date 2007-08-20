package org.objectweb.proactive.extra.gcmdeployment;

import java.io.IOException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorImpl;


public class Main {
    final private static String GAD = "XXX";

    /**
     * A main to test new ProActive Deployment during its development
     */
    public static void main(String[] args) throws IOException {
        // 1. Read a GCM Application descriptor
        GCMApplicationDescriptor gad = new GCMApplicationDescriptorImpl(GAD);
    }
}
