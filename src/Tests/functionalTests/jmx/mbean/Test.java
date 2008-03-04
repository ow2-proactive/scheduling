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
package functionalTests.jmx.mbean;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * Test the creation of the JMX MBean. (i.e BodyWrapperMBean and ProActiveRuntimeWrapperMBean)
 * 
 * @author Jean-Michael Legait
 */
@GCMDeploymentReady
public class Test extends FunctionalTestDefaultNodes {
    private A ao;

    public Test() {
        super(DeploymentType._1x1);
        super.vContract
                .setVariableFromProgram(
                        "jvmargDefinedByTest",
                        "-Dcom.sun.management.jmxremote -Dproactive.jmx.mbean=true -Dproactive.jmx.notification=true",
                        VariableContractType.DescriptorDefaultVariable);
    }

    @Before
    public void initTest() throws Exception {
        Node node = super.getANode();
        ao = (A) PAActiveObject.newActive(A.class.getName(), new Object[] {}, node);
    }

    @org.junit.Test
    public void action() throws Exception {
        assertTrue("The MBean associated to the active object doesn't exist!", ao.existBodyWrapperMBean());
        assertTrue("The MBean associated to the ProActive Runtime doesn't exist!", ao
                .existProActiveRuntimeWrapperMBean());
    }
}
