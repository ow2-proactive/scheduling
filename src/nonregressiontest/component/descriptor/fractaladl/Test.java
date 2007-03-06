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
package nonregressiontest.component.descriptor.fractaladl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1Multicast;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;


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
    public void action() throws Exception {
//        if (!"enable".equals(System.getProperty("proactive.future.ac"))) {
//            throw new Exception("automatic continuations are not set");
//        }
//        org.objectweb.proactive.core.component.adl.Launcher.main(new String[] {
//                "-fractal",
//                "nonregressiontest.component.descriptor.fractaladl.MessagePassingExample",
//                "",
//                Test.class.getResource(
//                    "/nonregressiontest/component/descriptor/deploymentDescriptor.xml")
//                          .getPath()
//            });
//
//        Component c = Registry.instance().getComponent("parallel");
        
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        deploymentDescriptor = ProActive.getProactiveDescriptor(Test.class.getResource(
                "/nonregressiontest/component/descriptor/deploymentDescriptor.xml").getPath());
        context.put("deployment-descriptor",deploymentDescriptor);
        Component root = (Component) f.newComponent("nonregressiontest.component.descriptor.fractaladl.MessagePassingExample",context);
        Fractal.getLifeCycleController(root).startFc();
        Component[] subComponents = Fractal.getContentController(root).getFcSubComponents();
        for (Component component : subComponents) {
            if ("parallel".equals(Fractal.getNameController(component).getFcName())) {
                // invoke method on composite
                I1Multicast i1Multicast  = (I1Multicast) component.getFcInterface("i1");
                //I1 i1= (I1)p1.getFcInterface("i1");
                messages =  i1Multicast.processInputMessage(new Message(MESSAGE));

                for (Iterator iter = messages.iterator(); iter.hasNext();) {
					Message element = (Message) iter.next();
					element.append(MESSAGE);
				}
                break;
            }
            
        }


    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
//        Launcher.killNodes(false);
        Registry.instance().clear();
        deploymentDescriptor.killall(false);
    }

    public boolean postConditions() throws Exception {
        //        		System.out.println("\nMESSAGE IS : ");
        //        		System.out.println("-------------------------------------------------");
        //        		message.printToStream(System.out);
        //        		System.out.println("-------------------------------------------------");
        StringBuffer resulting_msg = new StringBuffer();
        Object futureValue = ProActive.getFutureValue(messages);
        Message m = (Message)((Group)futureValue).getGroupByType();
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
        return resulting_msg.toString().equals(single_message + single_message + single_message + single_message);
    }

    private int append(StringBuffer buffer, Message message) {
        int nb_messages = 0;
        if (ProActiveGroup.isGroup(message)) {
            for (int i = 0; i < ProActiveGroup.size(message); i++) {
                nb_messages += append(buffer,
                    (Message) ProActiveGroup.get(message, i));
            }
        } else {
            buffer.append(message.getMessage());
            nb_messages++;
        }
        return nb_messages;
    }

    public static void main(String[] args) {
        
//        System.setProperty("fractal.provider", "org.objectweb.proactive.core.component.Fractive");
//        System.setProperty("java.security.policy", System.getProperty("user.dir")+"/proactive.java.policy");
//        System.setProperty("log4j.configuration", System.getProperty("user.dir")+"/proactive-log4j");
//        System.setProperty("log4j.configuration", "file:" + System.getProperty("user.dir")+"/proactive-log4j");
//        System.setProperty("nonregressiontest.descriptor.defaultnodes.file", "/nonregressiontest/descriptor/defaultnodes/NodesLocal.xml");
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions() ) {
            	System.out.println("SUCCESS!");
            } else {
            	System.out.println("FAILED!");
            }
        } catch (Exception e) {
            System.out.println("FAILED!");
            e.printStackTrace();
        } finally {
            try {
                test.endTest();
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
