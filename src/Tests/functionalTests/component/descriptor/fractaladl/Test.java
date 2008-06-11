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
package functionalTests.component.descriptor.fractaladl;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.ComponentTest;
import functionalTests.component.I1Multicast;
import functionalTests.component.Message;
import functionalTests.component.PrimitiveComponentA;
import functionalTests.component.PrimitiveComponentB;


/**
 * For a graphical representation, open the MessagePassingExample.fractal with the fractal defaultGui
 *
 * This test verifies the parsing and building of a component system using a customized Fractal ADL,
 * and tests new features such as exportation of virtual nodes and cardinality of virtual nodes.
 * It mixes exported and non-exported nodes to make sure these work together.
 *
 * @author The ProActive Team
 */
public class Test extends ComponentTest {

    /**
     *
     */
    public static String MESSAGE = "-->m";
    private List<Message> messages;

    //ComponentsCache componentsCache;
    private GCMApplication newDeploymentDescriptor = null;
    private ProActiveDescriptor oldDeploymentDescriptor = null;

    public Test() {
        super("Virtual node exportation / composition in the Fractal ADL",
                "Virtual node exportation / composition in the Fractal ADL");
    }

    @org.junit.Test
    public void testOldDeployment() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<String, Object> context = new HashMap<String, Object>();

        oldDeploymentDescriptor = PADeployment.getProactiveDescriptor(Test.class.getResource(
                "/functionalTests/component/descriptor/deploymentDescriptor.xml").getPath());

        context.put("deployment-descriptor", oldDeploymentDescriptor);
        Component root = (Component) f.newComponent(
                "functionalTests.component.descriptor.fractaladl.MessagePassingExample", context);
        Fractal.getLifeCycleController(root).startFc();
        Component[] subComponents = Fractal.getContentController(root).getFcSubComponents();
        for (Component component : subComponents) {
            if ("parallel".equals(Fractal.getNameController(component).getFcName())) {
                // invoke method on composite
                I1Multicast i1Multicast = (I1Multicast) component.getFcInterface("i1");
                //I1 i1= (I1)p1.getFcInterface("i1");
                messages = i1Multicast.processInputMessage(new Message(MESSAGE));

                for (Message msg : messages) {
                    System.out.println("Test.testOldDeployment()" + messages);
                    msg.append(MESSAGE);
                }
                break;
            }
        }
        StringBuffer resulting_msg = new StringBuffer();
        int nb_messages = append(resulting_msg, messages);

        //        System.out.println("*** received " + nb_messages + "  : " +
        //            resulting_msg.toString());
        //        System.out.println("***" + resulting_msg.toString());
        // this --> primitiveC --> primitiveA --> primitiveB--> primitiveA --> primitiveC --> this  (message goes through parallel and composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentA.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentA.MESSAGE + Test.MESSAGE;

        // there should be 4 messages with the current configuration
        Assert.assertEquals(2, nb_messages);

        Assert.assertEquals(single_message + single_message, resulting_msg.toString());

    }

    // @org.junit.Test
    public void testNewDeployment() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<String, Object> context = new HashMap<String, Object>();

        String descriptorPath = Test.class.getResource(
                "/functionalTests/component/descriptor/applicationDescriptor.xml").getPath();

        newDeploymentDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(descriptorPath));

        newDeploymentDescriptor.startDeployment();

        context.put("deployment-descriptor", newDeploymentDescriptor);
        Component root = (Component) f.newComponent(
                "functionalTests.component.descriptor.fractaladl.MessagePassingExample", context);
        Fractal.getLifeCycleController(root).startFc();
        Component[] subComponents = Fractal.getContentController(root).getFcSubComponents();
        for (Component component : subComponents) {
            if ("parallel".equals(Fractal.getNameController(component).getFcName())) {
                // invoke method on composite
                I1Multicast i1Multicast = (I1Multicast) component.getFcInterface("i1");
                //I1 i1= (I1)p1.getFcInterface("i1");
                messages = i1Multicast.processInputMessage(new Message(MESSAGE));

                for (Message msg : messages) {
                    msg.append(MESSAGE);
                }
                break;
            }
        }
        StringBuffer resulting_msg = new StringBuffer();
        int nb_messages = append(resulting_msg, messages);

        //        System.out.println("*** received " + nb_messages + "  : " +
        //            resulting_msg.toString());
        //        System.out.println("***" + resulting_msg.toString());
        // this --> primitiveC --> primitiveA --> primitiveB--> primitiveA --> primitiveC --> this  (message goes through parallel and composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentA.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentA.MESSAGE + Test.MESSAGE;

        // there should be 4 messages with the current configuration
        Assert.assertEquals(2, nb_messages);

        Assert.assertEquals(single_message + single_message, resulting_msg.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see testsuite.test.AbstractTest#endTest()
     */
    @After
    public void endTest() throws Exception {
        //        Launcher.killNodes(false);
        Registry.instance().clear();
        if (newDeploymentDescriptor != null)
            newDeploymentDescriptor.kill();

        if (oldDeploymentDescriptor != null)
            oldDeploymentDescriptor.killall(false);
    }

    private int append(StringBuffer buffer, List<Message> messages) {
        int nb_messages = 0;
        for (Message message : messages) {
            nb_messages++;
            buffer.append(message);
        }
        return nb_messages;
    }
}
