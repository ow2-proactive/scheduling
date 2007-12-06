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
package functionalTests.descriptor.lookupregister;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test lookup and register in deployment descriptors
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -6581981842836211604L;

    // private static String FS = File.separator;
    private static String AGENT_XML_LOCATION_UNIX;

    static {
        if ("ibis".equals(ProProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
            AGENT_XML_LOCATION_UNIX = Test.class.getResource(
                    "/functionalTests/descriptor/lookupregister/AgentIbis.xml")
                                                .getPath();
        } else {
            AGENT_XML_LOCATION_UNIX = Test.class.getResource(
                    "/functionalTests/descriptor/lookupregister/Agent.xml")
                                                .getPath();
        }
    }

    ProActiveDescriptor proActiveDescriptorAgent;
    A a;

    @org.junit.Test
    public void action() throws Exception {
        proActiveDescriptorAgent = ProDeployment.getProactiveDescriptor("file:" +
                AGENT_XML_LOCATION_UNIX);
        proActiveDescriptorAgent.activateMappings();
        VirtualNode vnAgent = proActiveDescriptorAgent.getVirtualNode("Agent");
        ProActiveObject.newActive(A.class.getName(), new Object[] { "local" },
            vnAgent.getNode());
        VirtualNode vnLookup = ProDeployment.lookupVirtualNode(URIBuilder.buildURIFromProperties(
                    "localhost", "Agent").toString());
        a = (A) vnLookup.getUniqueAO();

        assertTrue((a.getName().equals("local")));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
