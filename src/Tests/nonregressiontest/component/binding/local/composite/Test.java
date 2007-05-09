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
package nonregressiontest.component.binding.local.composite;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;


/**
 * @author Matthieu Morel
 *
 * Step 3 : bindings, life cycle start, interface method invocation
 *
 *                 ___________________________
 *                 |                __________________                |
 *                 |                |                        ______                        |                |                                                ________
 *                 |                |                        |                        |                        |                |                                                |                                |
 *         i1-|-i1-|----i1        |(p1)        |i2----        |i2 --|-i2----------i2        |        (p2)                |
 *                 |                |                        |_____|                        |                |                                                |                                |
 *                 |                |                                                                        |                |                                                |_______|
 *                 |                |_(c1)_____________|                |
 *                 |                                                                                                        |
 *                 |__(c2)____________________|
 *
 *
 */
public class Test extends ComponentTest {

    /**
         *
         */
    private static final long serialVersionUID = -348571654012032799L;
    public static String MESSAGE = "-->Main";
    Component p1;
    Component p2;
    Component c1;
    Component c2;
    Message message;
    Component[] c2SubComponents;
    Component[] c1SubComponents;

    public Test() {
        super("Binding of components on the local default node",
            "Binding of components on the local default node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {
        throw new testsuite.exception.NotStandAloneException();
    }

    public Component[] action(Component[] components) throws Exception {
        p1 = components[0];
        p2 = components[1];
        c1 = components[2];
        c2 = components[3];
        // BINDING
        Fractal.getBindingController(c2).bindFc("i1", c1.getFcInterface("i1"));
        Fractal.getBindingController(c1).bindFc("i1", p1.getFcInterface("i1"));
        Fractal.getBindingController(p1).bindFc("i2", c1.getFcInterface("i2"));
        Fractal.getBindingController(c1).bindFc("i2", c2.getFcInterface("i2"));
        Fractal.getBindingController(c2).bindFc("i2", p2.getFcInterface("i2"));

        // START LIFE CYCLE
        Fractal.getLifeCycleController(c2).startFc();
        Fractal.getLifeCycleController(p2).startFc();

        // INVOKE INTERFACE METHOD
        I1 i1 = (I1) c2.getFcInterface("i1");

        //I1 i1= (I1)p1.getFcInterface("i1");
        message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);
        return (new Component[] { p1, p2, c1, c2 });
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
    public void endTest() throws Exception {
    }

    @Override
    public boolean postConditions() throws Exception {
        return (message.toString()
                       .equals(Test.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE +
            Test.MESSAGE));
    }
}
