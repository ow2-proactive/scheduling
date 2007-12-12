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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.Group;

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
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {

    /**
         *
         */
    public static String MESSAGE = "-->m";
    private List<Message> messages;

    //ComponentsCache componentsCache;
    ProActiveDescriptor deploymentDescriptor;

    public Test() {
        super("Virtual node exportation / composition in the Fractal ADL",
            "Virtual node exportation / composition in the Fractal ADL");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        //        if (!"enable".equals(System.getProperty("proactive.future.ac"))) {
        //            throw new Exception("automatic continuations are not set");
        //        }
        //        org.objectweb.proactive.core.component.adl.Launcher.main(new String[] {
        //                "-fractal",
        //                "functionalTests.component.descriptor.fractaladl.MessagePassingExample",
        //                "",
        //                Test.class.getResource(
        //                    "/functionalTests/component/descriptor/deploymentDescriptor.xml")
        //                          .getPath()
        //            });
        //
        //        Component c = Registry.instance().getComponent("parallel");
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        deploymentDescriptor = PADeployment.getProactiveDescriptor(Test.class.getResource(
                    "/functionalTests/component/descriptor/deploymentDescriptor.xml")
                                                                             .getPath());
        context.put("deployment-descriptor", deploymentDescriptor);
        Component root = (Component) f.newComponent("functionalTests.component.descriptor.fractaladl.MessagePassingExample",
                context);
        Fractal.getLifeCycleController(root).startFc();
        Component[] subComponents = Fractal.getContentController(root)
                                           .getFcSubComponents();
        for (Component component : subComponents) {
            if ("parallel".equals(Fractal.getNameController(component)
                                             .getFcName())) {
                // invoke method on composite
                I1Multicast i1Multicast = (I1Multicast) component.getFcInterface(
                        "i1");
                //I1 i1= (I1)p1.getFcInterface("i1");
                messages = i1Multicast.processInputMessage(new Message(MESSAGE));

                for (Iterator iter = messages.iterator(); iter.hasNext();) {
                    Message element = (Message) iter.next();
                    element.append(MESSAGE);
                }
                break;
            }
        }
        StringBuffer resulting_msg = new StringBuffer();
        Object futureValue = PAFuture.getFutureValue(messages);
        Message m = (Message) ((Group) futureValue).getGroupByType();

        //        Message m = (Message)(ProActiveGroup.getGroup(ProActive.getFutureValue(messages)).getGroupByType());
        int nb_messages = append(resulting_msg, m);

        //        System.out.println("*** received " + nb_messages + "  : " +
        //            resulting_msg.toString());
        //        System.out.println("***" + resulting_msg.toString());
        // this --> primitiveC --> primitiveA --> primitiveB--> primitiveA --> primitiveC --> this  (message goes through parallel and composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE +
            Test.MESSAGE;

        // there should be 4 messages with the current configuration
        Assert.assertEquals(resulting_msg.toString(),
            single_message + single_message + single_message + single_message);
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @After
    public void endTest() throws Exception {
        //        Launcher.killNodes(false);
        Registry.instance().clear();
        deploymentDescriptor.killall(false);
    }

    private int append(StringBuffer buffer, Message message) {
        int nb_messages = 0;
        if (PAGroup.isGroup(message)) {
            for (int i = 0; i < PAGroup.size(message); i++) {
                nb_messages += append(buffer, (Message) PAGroup.get(message, i));
            }
        } else {
            buffer.append(message.getMessage());
            nb_messages++;
        }
        return nb_messages;
    }
}
