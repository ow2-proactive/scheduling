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
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;

import functionalTests.FunctionalTest;


/**
 * Deployment descriptor technical services.
 */
public class TestApplicationLevel extends FunctionalTest {
    private Node node;

    @Before
    public void before() throws ProActiveException {
        File desc = new File(this.getClass().getResource("TestApplicationLevelApplication.xml").getPath());
        GCMApplication app = PAGCMDeployment.loadApplicationDescriptor(desc);
        app.startDeployment();
        GCMVirtualNode vn = app.getVirtualNode("nodes");
        node = vn.getANode();
    }

    @org.junit.Test
    public void action() throws Exception {
        Assert.assertEquals("aaa", node.getProperty("arg1"));
        Assert.assertNull(node.getProperty("arg2"));
    }
}
