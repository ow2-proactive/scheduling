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

import org.junit.After;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;
public class Test extends FunctionalTest {
    private ActiveObjectClass a1;
    private ActiveObjectClass a1bis;
    private ActiveObjectClass a2;
    private ProActiveDescriptor descriptorPad;

    //private TimItBasicReductor t;
    public void initTest() throws Exception {
        // Access the nodes of the descriptor file
        descriptorPad = PADeployment.getProactiveDescriptor(this.getClass()
                                                                .getResource("/functionalTests/timit/timers/basic/descriptor.xml")
                                                                .getPath());
        descriptorPad.activateMappings();
        VirtualNode vnode = descriptorPad.getVirtualNodes()[0];
        Node[] nodes = vnode.getNodes();

        this.a1 = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { "a1" }, nodes[0]);
        this.a1bis = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { "a1bis" }, nodes[1]);
        // Provide the remote reference of a1 and a1bis to a2
        this.a2 = (ActiveObjectClass) PAActiveObject.newActive(ActiveObjectClass.class.getName(),
                new Object[] { this.a1, this.a1bis, "a2" }, nodes[1]);
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
        assertTrue("Problem with the creation of active objects for this test !",
            this.preConditions());
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
        this.descriptorPad.killall(true);
        Thread.sleep(300);
        this.a1 = null;
        this.a1bis = null;
        this.a2 = null;
        this.descriptorPad = null;
        //this.t = null;
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
