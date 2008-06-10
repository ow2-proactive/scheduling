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

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * Test ForgetOnSend strategies on SPMD groups
 */
@GCMDeploymentReady
public class TestGroup extends GCMFunctionalTestDefaultNodes {

    private boolean result;
    private B b, b1, b2, b3, b4;

    public TestGroup() {
        super(4, 1);
    }

    /**
     * Try to send FOS request on SPMD group with mixed strategies and destination (unique/group)
     */
    @Test
    public void stressedMixedSendings() {
        try {
            Object[][] params = { { "B1" }, { "B2" }, { "B3" }, { "B4" } };
            Node[] nodes = { super.getANode(), super.getANode(), super.getANode(), super.getANode() };
            b = (B) PASPMD.newSPMDGroup(B.class.getName(), params, nodes);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        b1 = (B) PAGroup.get(b, 0);
        b2 = (B) PAGroup.get(b, 1);
        b3 = (B) PAGroup.get(b, 2);
        b4 = (B) PAGroup.get(b, 3);

        PAActiveObject.setForgetOnSend(b, "a");
        PAActiveObject.setForgetOnSend(b2, "b");
        PAActiveObject.setForgetOnSend(b, "c");

        result = true;
        SlowlySerializableObject[] resB = new SlowlySerializableObject[500];
        String res;

        for (int i = 0; i < resB.length && result; i++) {
            b.a(); // fos group
            resB[i] = b2.b(i); // fos unique + future
            b.c(); // fos group
            b.d(); // standard group
            b1.e(); // standard unique

            // Check B1
            res = b1.takeFast(); // standard
            if (!res.equals("acde")) {
                System.out.println("Echec sur B1 -" + res);
                result = false;
            }

            // Check B2
            res = b2.takeFast(); // standard
            if (!res.equals("abcd")) {
                System.out.println("Echec sur B2 -" + res);
                result = false;
            }

            // Check B3
            res = b3.takeFast(); // standard
            if (!res.equals("acd")) {
                System.out.println("Echec sur B3 -" + res);
                result = false;
            }

            // Check B4
            res = b4.takeFast(); // standard
            if (!res.equals("acd")) {
                System.out.println("Echec sur B4 -" + res);
                result = false;
            }
        }

        // Check FIFO Point-To-Point Ordering
        assertTrue(result);

        // Check B results
        result = true;
        for (int i = 0; i < resB.length && result; i++) {
            if (!resB[i].getName().equals("res" + i)) {
                result = false;
            }
        }
        assertTrue(result);
    }
}