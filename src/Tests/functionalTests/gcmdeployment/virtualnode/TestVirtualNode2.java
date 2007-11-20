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
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;
import org.xml.sax.SAXException;


public class TestVirtualNode2 extends Abstract {
    @Test
    public void test()
        throws IllegalArgumentException, XPathExpressionException,
            FileNotFoundException, SAXException, IOException {
        GCMApplicationDescriptor gcma = API.getGCMApplicationDescriptor(getDescriptor());
        waitAllocation();

        VirtualNode vn1 = gcma.getVirtualNode("vn1");
        VirtualNode vn2 = gcma.getVirtualNode("vn2");

        boolean fairness = true;
        int diff = vn1.getNodes().size() - vn2.getNodes().size();
        if ((diff < -1) || (diff > 1)) {
            fairness = false;
        }

        Assert.assertTrue("Allocation is not fair between greedy VNs", fairness);
    }
}
