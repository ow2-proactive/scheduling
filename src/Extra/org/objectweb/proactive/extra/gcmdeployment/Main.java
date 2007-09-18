package org.objectweb.proactive.extra.gcmdeployment;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorImpl;
import org.xml.sax.SAXException;


public class Main {
    final private static String GAD = "XXX";

    /**
     * A main to test new ProActive Deployment during its development
     * @throws SAXException
     * @throws IllegalArgumentException
     * @throws XPathExpressionException
     */
    public static void main(String[] args)
        throws IOException, IllegalArgumentException, SAXException,
            XPathExpressionException {
        // 1. Read a GCM Application descriptor
        GCMApplicationDescriptor gad = new GCMApplicationDescriptorImpl(GAD);
    }
}
