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
package functionalTests.activeobject.creation.parallel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.creation.A;
import static junit.framework.Assert.assertTrue;

/**
 * Test newActiveInParallel method with the virtual node has NOT been activated
 * @author Alexandre di Costanzo
 *
 * Created on Nov 8, 2005
 */
public class TestVnNotActivated extends FunctionalTest {
    private static final String XML_PATH = TestVnNotActivated.class.getResource(
            "/functionalTests/activeobject/creation/parallel/4_local.xml")
                                                                   .getPath();
    private A[] aos;
    private VirtualNode vn;
    private ProActiveDescriptor padForActiving;

    @Test
    public void action() throws Exception {
        assertTrue(!vn.isActivated());

        this.aos = (A[]) PAActiveObject.newActiveInParallel(A.class.getName(),
                new Object[] { "toto" }, vn);

        assertTrue(aos != null);
        assertTrue(aos.length == 4);

        for (int i = 0; i < this.aos.length; i++) {
            aos[i].getNodeUrl();
        }

        this.vn.killAll(false);
    }

    @Before
    public void initTest() throws Exception {
        padForActiving = PADeployment.getProactiveDescriptor(XML_PATH);
        this.vn = padForActiving.getVirtualNode("Workers02");
    }

    @After
    public void endTest() throws Exception {
        if (padForActiving != null) {
            padForActiving.killall(false);
        }
    }
}
