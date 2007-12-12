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
package functionalTests.descriptor.services.technicalservice;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Deployment descriptor technical services.
 */
public class Test extends FunctionalTest {
    private static String XML_LOCATION = Test.class.getResource(
            "/functionalTests/descriptor/services/technicalservice/TechnicalService.xml")
                                                   .getPath();
    private ProActiveDescriptor pad;
    private VirtualNode vn1;
    private VirtualNode vn2;

    @org.junit.Test
    public void action() throws Exception {
        this.vn1 = this.pad.getVirtualNode("VN1");
        this.vn2 = this.pad.getVirtualNode("VN2");

        String vn1Arg1 = this.vn1.getNode().getProperty("arg1");
        String vn1Arg2 = this.vn1.getNode().getProperty("arg2");
        String vn2Arg1 = this.vn2.getNode().getProperty("arg1");
        String vn2Arg2 = this.vn2.getNode().getProperty("arg2");

        assertTrue(vn1Arg1.equals("aaa"));
        assertTrue(vn1Arg2 == null);
        assertTrue(vn2Arg1.equals("bbb"));
        assertTrue(vn2Arg2.equals("ccc"));
    }

    @Before
    public void initTest() throws Exception {
        this.pad = PADeployment.getProactiveDescriptor(XML_LOCATION);
        this.pad.activateMappings();
    }

    @After
    public void endTest() throws Exception {
        this.pad.killall(false);
    }
}
