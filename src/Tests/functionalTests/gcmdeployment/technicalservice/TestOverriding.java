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
package functionalTests.gcmdeployment.technicalservice;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.FunctionalTest;


/**
 * Deployment descriptor technical services.
 */
public class TestOverriding extends FunctionalTest {
    private GCMApplication app;

    @Before
    public void before() throws ProActiveException {
        File desc = new File(this.getClass().getResource("TestOverridingApplication.xml").getPath());
        app = PAGCMDeployment.loadApplicationDescriptor(desc);
        app.startDeployment();
        app.waitReady();
    }

    @org.junit.Test
    public void action() throws Exception {
        GCMVirtualNode vn1 = app.getVirtualNode("VN1");
        GCMVirtualNode vn2 = app.getVirtualNode("VN2");
        GCMVirtualNode vn3 = app.getVirtualNode("VN3");
        GCMVirtualNode vn4 = app.getVirtualNode("VN4");

        Node node;

        node = vn1.getANode();
        Assert.assertEquals("application", node.getProperty("arg1"));

        node = vn2.getANode();
        Assert.assertEquals("VN2", node.getProperty("arg1"));

        node = vn3.getANode();
        Assert.assertEquals("NP1", node.getProperty("arg1"));

        node = vn4.getANode();
        Assert.assertEquals("NP1", node.getProperty("arg1"));
    }
}
