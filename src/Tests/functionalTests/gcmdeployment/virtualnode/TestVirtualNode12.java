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
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNode;

import functionalTests.GCMDeploymentReady;
import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


@GCMDeploymentReady
public class TestVirtualNode12 extends GCMFunctionalTest {
    public TestVirtualNode12() throws FileNotFoundException {
        super(LocalHelpers.getDescriptor(TestVirtualNode12.class));
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException {
        gcmad.waitReady();

        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        Assert.assertEquals(7, vn1.getCurrentNodes().size());

        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        Assert.assertEquals(15, vn2.getCurrentNodes().size());
    }
}
