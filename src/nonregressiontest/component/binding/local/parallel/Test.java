/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package nonregressiontest.component.binding.local.parallel;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import testsuite.test.FunctionalTest;


/**
 * @author Matthieu Morel
 *
 * Step 3 : bindings, life cycle start, interface method invocation
 *
 *                 ___________________________
 *                 |                                        ______                                        |
 *                 |                                         |                        |                                        |
 *                 |                        /        i1        |(p1)        |i2        \                        |
 *                 |                /                        |_____|                         \                  |                                 ______
 *         i1        |        /                                                                                        \        |                                |                        |
 *                 |        \                                   ______                                /        |        i2        ---i2        |(p3)        |
 *                 |                \                |                        |                        /                |                                |_____|
 *                 |                        \         i1        |(p2)        |i2        /                        |
 *                 |                                        |_____|                                        |
 *                 |__(c2)____________________|
 *
 *
  */
public class Test extends FunctionalTest {
    public static String MESSAGE = "-->Main";
    Component p1;
    Component p2;
    Component p3;
    Component pr1;
    Message message;
    Component[] pr1SubComponents;

    public Test() {
        super("Binding of parallel components on the local default node",
            "Binding of parallel components on the local default node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        throw new testsuite.exception.NotStandAloneException();
    }

    public Component[] action(Component[] components) throws Exception {
        p1 = components[0];
        p2 = components[1];
        p3 = components[2];
        pr1 = components[3];
        System.setProperty("proactive.future.ac", "enable");
        // start a new thread so that automatic continuations are enabled for components
        ACThread acthread = new ACThread();
        acthread.start();
        acthread.join();
        System.setProperty("proactive.future.ac", "disable");
        return (new Component[] { p1, p2, p3, pr1 });
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    private class ACThread extends Thread {
        public void run() {
            try {
                // BINDING
                Fractal.getBindingController(pr1).bindFc("i1",
                    p1.getFcInterface("i1"));
                Fractal.getBindingController(pr1).bindFc("i1",
                    p2.getFcInterface("i1"));
                Fractal.getBindingController(p1).bindFc("i2",
                    pr1.getFcInterface("i2"));
                Fractal.getBindingController(p2).bindFc("i2",
                    pr1.getFcInterface("i2"));
                Fractal.getBindingController(pr1).bindFc("i2",
                    p3.getFcInterface("i2"));

                // START LIFE CYCLE
                Fractal.getLifeCycleController(pr1).startFc();
                Fractal.getLifeCycleController(p3).startFc();

                // INVOKE INTERFACE METHOD
                I1 i1 = (I1) pr1.getFcInterface("i1");

                //I1 i1= (I1)p1.getFcInterface("i1");
                message = null;
                message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        Group message_group = ProActiveGroup.getGroup(message);
        Message result = new Message();

        // result is a group
        for (int i = 0; i < message_group.size(); i++) {
            result.append((Message) message_group.get(i));
        }

        return (result.toString().equals((Test.MESSAGE +
            PrimitiveComponentA.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentA.MESSAGE + Test.MESSAGE) +
            (Test.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE +
            Test.MESSAGE)));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
