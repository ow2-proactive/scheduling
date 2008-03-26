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
package functionalTests.component;

import java.util.Arrays;

import org.junit.Assert;
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

import functionalTests.ComponentTestDefaultNodes;


public class TestComponentRemote extends ComponentTestDefaultNodes {
    private static final String P1_NAME = "primitive-component-1";
    private static final String P2_NAME = "primitive-component-2";
    private static final String C1_NAME = "composite-component1";
    private static final String C2_NAME = "composite-component2";
    public static String MESSAGE = "-->Main";
    private static Component p1;
    private static Component p2;
    private static Component c1;
    private static Component c2;

    public TestComponentRemote() {
        super(DeploymentType._2x1);
    }

    /**
     * @author The ProActive Team
     *
     * Step 1. Creation of the components
     *
     * Creates the following components :
     *
     *                 __________________
     *                 |                  |                                        ________
     *                 |                  |                                        |       |
     *         i1      |                  |i2                            i2        | (p2)  |
     *                 |                  |                                        |_______|
     *                 |                  |
     *                 |_(c1)_____________|
     *
     *                 __________________
     *                 |                  |                                        ________
     *                 |                  |                                        |       |
     *         i1      |                  |i2                            i1        |  (p1) |i2
     *                 |                  |                                        |_______|
     *                 |                  |
     *                 |_(c2)_____________|
     *
     *         where :
     *                 (c1) and (c2) are composites, (p1) and (p2) are primitive components
     *                 i1 represents an interface of type I1
     *                 i2 represents an interface of type I2
     *                 c1 and p2 are on a remote JVM
     *
     */
    @org.junit.Test
    public void testCreationNewactiveComposite() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);
        ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) });

        p1 = cf.newFcInstance(i1_i2_type, new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(), new Object[] {}));
        p2 = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "i2", I2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription(P2_NAME, Constants.PRIMITIVE), new ContentDescription(
                    PrimitiveComponentB.class.getName(), new Object[] {}), super.getANode());
        c1 = cf.newFcInstance(i1_i2_type, new ControllerDescription(C1_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}), super.getANode());
        c2 = cf.newFcInstance(i1_i2_type, new ControllerDescription(C2_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}));

        Assert.assertEquals(Fractal.getNameController(p1).getFcName(), P1_NAME);
        Assert.assertEquals(Fractal.getNameController(p2).getFcName(), P2_NAME);
        Assert.assertEquals(Fractal.getNameController(c1).getFcName(), C1_NAME);
        Assert.assertEquals(Fractal.getNameController(c2).getFcName(), C2_NAME);
        //    }

        /**
         * @author The ProActive Team
         *
         * Step 2 : assembles the following component system :
         *
         *                 _____________________________
         *                 |      __________________    |
         *                 |      |     ______      |   |         ________
         *                 |      |    |     |      |   |        |       |
         *              i1 | i1   | i1 |(p1) |i2    |i2 | i2   i2| (p2)  |
         *                 |      |    |_____|      |   |        |       |
         *                 |      |                 |   |        |_______|
         *                 |      |_(c1)____________|   |
         *                 |                            |
         *                 |__(c2)______________________|
         *
         *         where :
         *                 (c2) and (c1) are composite components, (p1) and (p2) are primitive components
         *                 i1 represents an interface of type I1
         *                 i2 represents an interface of type I2
         *                 c1 and p2 are on a remote JVM
         */
        //    @org.junit.Test
        //    public void testAssemblyRemoteComposite() throws Exception {
        //        new TestComponentRemote().testCreationNewactiveComposite();
        // ASSEMBLY
        Fractal.getContentController(c1).addFcSubComponent(p1);
        Fractal.getContentController(c2).addFcSubComponent(c1);

        Component[] c2SubComponents = Fractal.getContentController(c2).getFcSubComponents();
        Component[] c1SubComponents = Fractal.getContentController(c1).getFcSubComponents();
        Component[] c2_sub_components = { c1 };
        Component[] c1_sub_components = { p1 };

        Assert.assertTrue(Arrays.equals(c2SubComponents, c2_sub_components));
        Assert.assertTrue(Arrays.equals(c1SubComponents, c1_sub_components));

        /**
         * @author The ProActive Team
         *
         * Step 3 : bindings, life cycle start, interface method invocation
         *
         *        ___________________________________
         *       |        ___________________        |
         *       |       |       ______      |       |                   ________
         *       |       |      |     |      |       |                  |       |
         *    i1-|----i1-|----i1|(p1) |i2----|i2 ----|-i2-------------i2|(p2)   |
         *       |       |      |_____|      |       |                  |       |
         *       |       |                   |       |                  |_______|
         *       |       |_(c1)______________|       |
         *       |                                   |                           
         *       |__(c2)_____________________________|
         *
         *
         */
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
        Message message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);

        Assert.assertEquals(message.toString(), TestComponentRemote.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE + TestComponentRemote.MESSAGE);
    }
}
