/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.descriptor.basic;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationParserImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.GCMDeploymentParserImpl;
import org.xml.sax.SAXException;


public class TestBasicDescriptorParsing {

    //    @Test
    public void oldDeploymentDescriptorParse() throws Exception {
        String descriptorLocation = getClass().getResource("javaproperty_ERROR.xml").getPath();

        Object proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" + descriptorLocation);

    }

    @Test
    public void deploymentDescriptorParse() throws Exception {
        String descriptorLocation = getClass().getResource("wrong_namespace.xml").getPath();

        boolean gotException = false;

        try {
            GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(new File(descriptorLocation), null);
        } catch (SAXException e) {
            gotException = e.getException().getMessage().contains("old format");
        }

        Assert.assertTrue(gotException);

    }

    //    @Test
    public void applicationDescriptorParse() throws Exception {

        String descriptorLocation = getClass().getResource("application_ProActive_MS_basic.xml").getPath();

        System.out.println("parsing " + descriptorLocation);
        GCMApplicationParserImpl parser = new GCMApplicationParserImpl(new File(descriptorLocation), null);

        parser.getCommandBuilder();
        parser.getVirtualNodes();
        parser.getNodeProviders();

    }

}
