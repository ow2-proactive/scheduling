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
package functionalTests.gcmdeployment.virtualnode;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNodeSubscribe {
    static GCMApplication gcma;
    boolean isReady = false;
    long nodes = 0;

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        GCMApplication gcma;

        gcma = PAGCMDeployment.loadApplicationDescriptor(LocalHelpers.getDescriptor(this));
        GCMVirtualNode vnGreedy = gcma.getVirtualNode("greedy");
        GCMVirtualNode vnMaster = gcma.getVirtualNode("master");

        Assert.assertFalse(vnGreedy.subscribeIsReady(this, "isReady"));

        Assert.assertFalse(vnMaster.subscribeNodeAttachment(this, "LOL"));
        Assert.assertFalse(vnMaster.subscribeNodeAttachment(this, "brokenNodeAttached"));
        Assert.assertFalse(vnMaster.subscribeIsReady(this, "brokenIsReady"));

        Assert.assertTrue(vnMaster.subscribeNodeAttachment(this, "nodeAttached"));
        Assert.assertTrue(vnMaster.subscribeIsReady(this, "isReady"));

        // Crash it !
        vnMaster.subscribeIsReady(this, "null");
        vnMaster.subscribeIsReady(null, null);
        vnMaster.unsubscribeIsReady(this, "null");
        vnMaster.unsubscribeIsReady(null, null);

        gcma.startDeployment();
        gcma.waitReady();

        // wait for the notification
        Thread.sleep(1000);
        Assert.assertTrue(isReady);
        Assert.assertEquals(2, nodes);
    }

    public void isReady(GCMVirtualNode vn) {
        isReady = true;
    }

    public void nodeAttached(Node node, GCMVirtualNode vn) {
        nodes++;
        if (nodes == 2) {
            vn.unsubscribeNodeAttachment(this, "nodeAttached");
        }
    }

    public void brokenIsReady(long l) {
    }

    public void brokenNodeAttached(Object o) {
    }
}
