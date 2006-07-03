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
package nonregressiontest.component.creation.remote.newactive.composite;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.type.Composite;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1;
import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;
import nonregressiontest.descriptor.defaultnodes.TestNodes;


/**
 * @author Matthieu Morel
 *
 * Step 1. Creation of the components
 *
 * Creates the following components :
 *
 *                 __________________
 *                 |                                                                        |                                        ________
 *                 |                                                                        |                                        |                                |
 *         i1        |                                                                        |i2                         i2        |        (p2)                |
 *                 |                                                                        |                                        |_______|
 *                 |                                                                        |
 *                 |_(c1)_____________|
 *
 *                 __________________
 *                 |                                                                        |                                        ________
 *                 |                                                                        |                                        |                                |
 *         i1        |                                                                        |i2                         i1        |        (p1)                |i2
 *                 |                                                                        |                                        |_______|
 *                 |                                                                        |
 *                 |_(c2)_____________|
 *
 *         where :
 *                 (c1) and (c2) are composites, (p1) and (p2) are primitive components
 *                 i1 represents an interface of type I1
 *                 i2 represents an interface of type I2
 *                 c1 and p2 are on a remote JVM
 *
 */
public class Test extends ComponentTest {
    private static final String P1_NAME = "primitive-component-1";
    private static final String P2_NAME = "primitive-component-2";
    private static final String C1_NAME = "composite-component1";
    private static final String C2_NAME = "composite-component2";
    public static final String MESSAGE = "-->Main";
    Component primitiveComponentA;
    String name;
    String nodeUrl;
    Message message;
    Component p1;
    Component p2;
    Component c1;
    Component c2;

    public Test() {
        super("Creation of a composite system on remote nodes",
            "Test creation of a composite system on remote nodes");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        throw new testsuite.exception.NotStandAloneException();
    }

    /**
     * first of interlinked tests
     * @param obj
     */
    public Component[] action(Object obj) throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory)Fractal.getGenericFactory(boot);
        ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                    type_factory.createFcItfType("i1", I1.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    type_factory.createFcItfType("i2", I2.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                });

        p1 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(),
                    new Object[] {  }));
        p2 = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("i2", I2.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE)
                    }),
                new ControllerDescription(P2_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(),
                    new Object[] {  }), TestNodes.getRemoteACVMNode());
        c1 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(C1_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }), TestNodes.getRemoteACVMNode());
        c2 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(C2_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return (new Component[] { p1, p2, c1, c2 });
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        String p1_name = Fractal.getNameController(p1).getFcName();
        String p2_name = Fractal.getNameController(p2).getFcName();
        String c1_name = Fractal.getNameController(c1).getFcName();
        String c2_name = Fractal.getNameController(c2).getFcName();
        return (p1_name.equals(P1_NAME) && p2_name.equals(P2_NAME) &&
        c1_name.equals(C1_NAME) && c2_name.equals(C2_NAME));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            test.postConditions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
