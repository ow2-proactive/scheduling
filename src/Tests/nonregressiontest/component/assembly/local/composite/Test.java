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
package nonregressiontest.component.assembly.local.composite;

import java.util.Arrays;

import nonregressiontest.component.ComponentTest;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import testsuite.test.Assertions;


/**
 * @author Matthieu Morel
 *
 * Step 2 : assembles the following component system :
 *
 *                 ___________________________
 *                 |                __________________                |
 *                 |                |                        ______                        |                |                                                ________
 *                 |                |                        |                        |                        |                |                                                |                                |
 *         i1 | i1  |    i1        |(p1)        |i2            |i2   | i2           i2        |        (p2)                |
 *                 |                |                        |_____|                        |                |                                                |                                |
 *                 |                |                                                                        |                |                                                |_______|
 *                 |                |_(c1)_____________|                |
 *                 |                                                                                                        |
 *                 |__(c2)____________________|
 *
 *         where :
 *                 (c2) and (c1) are composite components, (p1) and (p2) are primitive components
 *                 i1 represents an interface of type I1
 *                 i2 represents an interface of type I2
 *
 */
public class Test extends ComponentTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2137029402382027456L;
	public static String MESSAGE = "-->Main";
    Component p1;
    Component p2;
    Component c1;
    Component c2;
    String name;
    Component[] c2SubComponents;
    Component[] c1SubComponents;

    public Test() {
        super("Tests the creation of a composite system on the local default node, and tests super controller",
            "Tests the creation of a composite system on the local default node, and tests super controller");
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
        // ASSEMBLY
        Fractal.getContentController(c1).addFcSubComponent(p1);
        Fractal.getContentController(c2).addFcSubComponent(c1);

        c2SubComponents = Fractal.getContentController(c2).getFcSubComponents();
        c1SubComponents = Fractal.getContentController(c1).getFcSubComponents();
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
        Component[] c2_sub_components = { c1 };
        Component[] c1_sub_components = { p1 };

        // a test for super controllers
        Component[] c1_super_components = Fractal.getSuperController(c1)
                                                 .getFcSuperComponents();
        Component[] p1_super_components = Fractal.getSuperController(p1)
                                                 .getFcSuperComponents();

        Assertions.assertTrue(Arrays.equals(c1_super_components,
                new Component[] { c2 }) &&
            Arrays.equals(p1_super_components, new Component[] { c1 }));

        return (Arrays.equals(c2SubComponents, c2_sub_components) &&
        Arrays.equals(c1SubComponents, c1_sub_components));
    }
}
