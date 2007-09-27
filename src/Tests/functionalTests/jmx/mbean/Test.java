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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.jmx.mbean;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test the creation of the JMX MBean.
 * (i.e BodyWrapperMBean and ProActiveRuntimeWrapperMBean)
 *
 * @author Jean-Michael Legait
 */
public class Test extends FunctionalTest {
    private URL descriptor = Test.class.getResource(
            "/functionalTests/jmx/mbean/MBeanDescriptor.xml");
    private ProActiveDescriptorInternal pad;
    private VirtualNode vn;
    private A ao;

    @Before
    public void initTest() throws Exception {
        this.pad = ProDeployment.getProactiveDescriptor(descriptor.getPath());
        this.vn = this.pad.getVirtualNode("MBeanTEST");
        this.vn.activate();

        Node node = vn.getNode();
        ao = (A) ProActiveObject.newActive(A.class.getName(),
                new Object[] {  }, node);
    }

    @org.junit.Test
    public void action() throws Exception {
        assertTrue("The MBean associated to the active object doesn't exist!",
            ao.existBodyWrapperMBean());
        assertTrue("The MBean associated to the ProActive Runtime doesn't exist!",
            ao.existProActiveRuntimeWrapperMBean());
    }

    @After
    public void endTest() throws Exception {
        this.vn.killAll(true);
        descriptor = null;
        pad = null;
        vn = null;
        ao = null;
    }
}
