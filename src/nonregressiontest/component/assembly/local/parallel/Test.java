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
package nonregressiontest.component.assembly.local.parallel;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;


import testsuite.test.FunctionalTest;

import java.util.Arrays;


/**
 * @author Matthieu Morel
 *
 * step 2 : assembles the following component system :
 *
 *                 ___________________________
 *                 |                                        ______                                        |
 *                 |                                         |                        |                                        |
 *                 |                                i1        |(p1)        |i2                                |
 *                 |                                        |_____|                                           |                                 ______
 *         i1        |                                                                                                        |i2                        |                        |
 *                 |                                           ______                                        |                        i2        |(p3)        |
 *                 |                                |                        |                                        |                                |_____|
 *                 |                                 i1        |(p2)        |i2                                |
 *                 |                                        |_____|                                        |
 *                 |__(c2)____________________|
 *
  *         where :
 *                 (p1), (p2) and (p3) are primitive components,
 *                  pr1 is a parallel component
 *                 i1 represents an interface of type I1
 *                 i2 represents an interface of type I2
 *                 i3 represents an interface of type I3
 */
public class Test extends FunctionalTest {
    public static String MESSAGE = "-->Main";
    Component p1;
    Component p2;
    Component p3;
    Component pr1;
    String name;
    Component[] pr1SubComponents;

    public Test() {
        super("Assembly of a parallel component on the local default node",
            "Test creation of a parallel system on the local default node");
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
                // ASSEMBLY
                Fractal.getContentController(pr1).addFcSubComponent(p1);
                Fractal.getContentController(pr1).addFcSubComponent(p2);
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
        Component[] pr1_sub_components = { p1, p2 };
        pr1SubComponents = Fractal.getContentController(pr1).getFcSubComponents();
        return (Arrays.equals(pr1SubComponents, pr1_sub_components));
    }

    public static void main(String[] args) {
        //			TestOld test = new TestOld();
        //			try {
        //				test.action();
        //			} catch (Exception e) {
        //				e.printStackTrace();
        //			}
    }
}
