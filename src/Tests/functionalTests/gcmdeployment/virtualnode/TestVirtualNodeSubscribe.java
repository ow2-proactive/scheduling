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
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.FunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNodeSubscribe extends FunctionalTest {
    static GCMApplication gcma;
    GCMVirtualNode vnGreedy;
    GCMVirtualNode vnMaster;

    Semaphore semIsReady = new Semaphore(0);
    Semaphore semNodeAttached = new Semaphore(-1);

    boolean isReady = false;
    long nodes = 0;

    @Before
    public void before() throws ProActiveException, FileNotFoundException {
        gcma = PAGCMDeployment.loadApplicationDescriptor(LocalHelpers.getDescriptor(this));
        vnGreedy = gcma.getVirtualNode("greedy");
        vnMaster = gcma.getVirtualNode("master");
    }

    @Test(expected = ProActiveException.class)
    public void testIsReadyWithGreedyVN() throws ProActiveException {
        vnGreedy.subscribeIsReady(this, "isReady");
    }

    @Test(expected = ProActiveException.class)
    public void testNoSuchMethod() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "LOL", false);
    }

    @Test(expected = ProActiveException.class)
    public void testInvalidSignature() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "brokenNodeAttached", false);
    }

    @Test(expected = ProActiveException.class)
    public void testInvalidSignatureIsReady() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "brokenIsReady", false);
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt1() throws ProActiveException {
        vnMaster.subscribeIsReady(this, "null");
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt2() throws ProActiveException {
        vnMaster.subscribeIsReady(null, null);
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt3() throws ProActiveException {
        vnMaster.unsubscribeIsReady(this, "null");
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt4() throws ProActiveException {
        vnMaster.unsubscribeIsReady(null, null);
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        // Failure <=> Timeout

        vnMaster.subscribeNodeAttachment(this, "nodeAttached", false);
        vnMaster.subscribeIsReady(this, "isReady");

        gcma.startDeployment();
        gcma.waitReady();

        // wait for the notification

        semIsReady.acquire();
        semNodeAttached.acquire();

        vnMaster.unsubscribeNodeAttachment(this, "nodeAttached");
    }

    public void isReady(String vnName) {
        Assert.assertNotNull(gcma.getVirtualNode(vnName));
        semIsReady.release();
    }

    public void nodeAttached(Node node, String vnName) {
        semNodeAttached.release();
    }

    public void brokenIsReady(long l) {
    }

    public void brokenNodeAttached(Object o) {
    }
}
