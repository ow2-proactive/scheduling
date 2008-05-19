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
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNode2 extends GCMFunctionalTest {
    public TestVirtualNode2() throws FileNotFoundException {
        super(LocalHelpers.getDescriptor(TestVirtualNode2.class));
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        Thread.sleep(8000);

        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");

        boolean fairness = true;
        int diff = vn1.getCurrentNodes().size() - vn2.getCurrentNodes().size();
        if ((diff < -1) || (diff > 1)) {
            fairness = false;
        }

        Assert.assertTrue("Allocation is not fair between greedy VNs", fairness);
    }
}
