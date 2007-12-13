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
package functionalTests.security.ruleCheck;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.security.exceptions.RuntimeSecurityException;


public class Launcher {
    public static void main(String[] args) {
        try {
            ProActiveDescriptor descriptor1 = PADeployment
                    .getProactiveDescriptor("descriptors/security/simple1.xml");
            descriptor1.activateMappings();
            VirtualNode virtualNode1 = descriptor1.getVirtualNode("vn1");
            Node node1 = virtualNode1.getNodes()[0];
            SampleObject a = (SampleObject) PAActiveObject.newActive(SampleObject.class.getName(),
                    new Object[] { "CN=Garden1" }, node1);

            ProActiveDescriptor descriptor2 = PADeployment
                    .getProactiveDescriptor("descriptors/security/simple2.xml");
            descriptor2.activateMappings();
            VirtualNode virtualNode2 = descriptor2.getVirtualNode("vn2");
            Node node2 = virtualNode2.getNodes()[0];
            SampleObject b = (SampleObject) PAActiveObject.newActive(SampleObject.class.getName(),
                    new Object[] { "CN=Garden2" }, node2);

            ProActiveDescriptor descriptor3 = PADeployment
                    .getProactiveDescriptor("descriptors/security/simple3.xml");
            descriptor3.activateMappings();
            VirtualNode virtualNode3 = descriptor3.getVirtualNode("vn3");
            Node node3 = virtualNode3.getNodes()[0];
            SampleObject c = (SampleObject) PAActiveObject.newActive(SampleObject.class.getName(),
                    new Object[] { "CN=Garden3" }, node3);

            //			a.makeTargetDoSomething(a);
            //            System.out.println("==");
            //a.makeTargetDoSomething(b);
            //            System.out.println("==");
            //a.makeTargetDoSomething(c);

            //            System.out.println("//////////////////");

            //            b.makeTargetDoSomething(a);
            //            System.out.println("==");
            //            b.makeTargetDoSomething(b);
            //            System.out.println("==");
            b.makeTargetDoSomething(c);

            //            System.out.println("//////////////////");
            //a.makeTargetDoSomething(b);
            try {
                String s = b.sayhello(c).get();
                System.out.println("s : " + s);
            } catch (RuntimeSecurityException ex) {
                System.out.println("wwwwwwwaaaouuuuu");
            }
            try {
                b.makeTargetDoSomething(c);
            } catch (RuntimeSecurityException ex) {
                System.out.println("void ");
            }

            //            c.makeTargetDoSomething(a);
            //            System.out.println("==");
            //            c.makeTargetDoSomething(b);
            //            System.out.println("==");
            //            c.makeTargetDoSomething(c);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}
