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
package functionalTests.timit.timers.basic;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;

import functionalTests.FunctionalTest;


public final class Test extends FunctionalTest {
    private ActiveObjectClass a1;
    private ActiveObjectClass a1bis;
    private ActiveObjectClass a2;

    private GCMApplication gcmad;

    //private TimItBasicReductor t;
    public void initTest() throws Exception {
        final File f = new File(this.getClass().getResource(
                "/functionalTests/timit/timers/basic/TimersTestApplication.xml").getPath());
        gcmad = PAGCMDeployment.loadApplicationDescriptor(f);
        gcmad.startDeployment();
        // Access the nodes of the descriptor file
        final GCMVirtualNode vNode = gcmad.getVirtualNode("TestVirtualNode");
        final Node n1 = vNode.getANode();
        final Node n2 = vNode.getANode();
        this.a1 = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { "a1" }, n1);
        this.a1bis = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { "a1bis" }, n2);
        // Provide the remote reference of a1 and a1bis to a2
        this.a2 = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { this.a1, this.a1bis, "a2" }, n2);
        // In order to test the value of the WaitForRequest timer
        // the main will wait a WAITING_TIME, therefore the a2 will be in
        // WaitForRequest for at
        // least 100
        Thread.sleep(100);
    }

    public boolean preConditions() throws Exception {
        return ((this.a1 != null) && (this.a1bis != null)) &&
            ((this.a2 != null) && this.a2.checkRemoteAndLocalReference());
    }

    @org.junit.Test
    public void action() throws Exception {
        // Create active objects
        this.initTest();
        // Check their creation
        assertTrue("Problem with the creation of active objects for this test !", this.preConditions());
        // Check if the Total timer is started
        String reason = this.a2.checkIfTotalIsStarted();
        assertTrue(reason, reason == null);

        // Check if the WaitForRequest timer is stopped during a service of a
        // request
        reason = this.a2.checkIfWfrIsStopped();
        assertTrue(reason, reason == null);

        // Check if the Serve timer is started during a service of a request
        reason = this.a2.checkIfServeIsStarted();
        assertTrue(reason, reason == null);

        // For the next requests a2 is going to use timers
        // SendRequest, BeforeSerialization, Serialization and
        // AfterSerialization timers must be used
        reason = this.a2.performSyncCallOnRemote();
        assertTrue(reason, reason == null);

        // SendRequest and LocalCopy timers must be used
        reason = this.a2.performSyncCallOnLocal();
        assertTrue(reason, reason == null);

        // SendRequest and WaitByNecessity timers must be used
        reason = this.a2.performAsyncCallWithWbnOnLocal();
        assertTrue(reason, reason == null);

        // disable the result output
        // this.t = TimItBasicManager.getInstance().getTimItCommonReductor();
        // t.setGenerateOutputFile(false);
        // t.setPrintOutput(false);
    }

    @After
    public void endTest() throws Exception {
        this.gcmad.kill();
        Thread.sleep(300);
        this.a1 = null;
        this.a1bis = null;
        this.a2 = null;
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
