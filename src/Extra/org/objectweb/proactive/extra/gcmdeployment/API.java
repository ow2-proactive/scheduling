package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorImpl;
import org.xml.sax.SAXException;


public class API {
    public static GCMApplicationDescriptor getGCMApplicationDescriptor(
        File file)
        throws IllegalArgumentException, SAXException, IOException,
            XPathExpressionException {
        return new GCMApplicationDescriptorImpl(file);
    }
}
