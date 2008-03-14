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
package functionalTests.activeobject.request.forgetonsend;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTest;
import functionalTests.GCMDeploymentReady;
import static junit.framework.Assert.assertTrue;


@GCMDeploymentReady
public class TestGarbageCollection extends FunctionalTest {

    /*
     * \ v \ 1--->---2 \ /| ^ v | \ / | 3 v / \ | v ^ | / \| 4 5
     */
    private static void buildGraph() throws Exception {
        GCObject a1 = (GCObject) PAActiveObject.newActive(GCObject.class.getName(), null);
        GCObject a2 = (GCObject) PAActiveObject.newActive(GCObject.class.getName(), null);
        GCObject a3 = (GCObject) PAActiveObject.newActive(GCObject.class.getName(), null);
        GCObject a4 = (GCObject) PAActiveObject.newActive(GCObject.class.getName(), null);
        GCObject a5 = (GCObject) PAActiveObject.newActive(GCObject.class.getName(), null);

        PAActiveObject.setForgetOnSend(a1, "addRef");
        PAActiveObject.setForgetOnSend(a2, "addRef");
        PAActiveObject.setForgetOnSend(a3, "addRef");
        PAActiveObject.setForgetOnSend(a4, "addRef");
        PAActiveObject.setForgetOnSend(a5, "addRef");

        a1.addRef(a2);
        a2.addRef(a3);
        a2.addRef(a5);
        a3.addRef(a1);
        a3.addRef(a4);
        a5.addRef(a3);
    }

    /**
     * Test DGC after sending FOS requests
     */
    @org.junit.Test
    public void action() throws Exception {
        /*
         * if ("BEGCObject Systems, Inc.".equals(System.getProperty("java.vendor")) &&
         * "1.6.0_01".equals(System.getProperty("java.version"))) { // With the local GC in this
         * JVM, weak references // to stubs are seemingly never cleared. return; }
         */
        assertTrue(GCObject.countCollected() == 0);
        buildGraph();
        int i = 100; // The Test fails after ~ 50 seconds (standard is < 15 seconds)

        while (GCObject.countCollected() != 5 && i > 0) {
            System.gc();
            Thread.sleep(500);
            i--;
        }
        assertTrue(GCObject.countCollected() == 5);
    }

    @Before
    public void initTest() throws Exception {
        /* This must be done before initializing ProGCObjectctive, and the DGC */
        System.setProperty("proactive.dgc", "true");
        System.setProperty("proactive.dgc.ttb", "500");
    }
}
