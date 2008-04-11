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
package functionalTests.component.binding.local.collective;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.type.Composite;

import functionalTests.ComponentTest;
import functionalTests.component.I1Multicast;
import functionalTests.component.I2;
import functionalTests.component.Message;
import functionalTests.component.PrimitiveComponentB;
import functionalTests.component.PrimitiveComponentD;
import functionalTests.component.PrimitiveComponentE;


/**
 * @author The ProActive Team
 *
 * a test for bindings on client collective interfaces between remote components
 */
public class Test extends ComponentTest {

    /**
     *
     */
    public static String MESSAGE = "-->Main";
    Component pD1;
    Component pB1;
    Component pB2;
    Message message;

    public Test() {
        super("Communication between local primitive components through client collection interface",
                "Communication between local primitive components through client collection interface ");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);
        ComponentType D_Type = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1Multicast.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION) });
        ComponentType B_Type = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "i2", I2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });

        ComponentType eType = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1Multicast.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION) });

        ComponentType fType = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i2-server", I2.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION),
                type_factory.createFcItfType("i2-client", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION) });

        // instantiate the components
        pD1 = cf.newFcInstance(D_Type, new ControllerDescription("pD1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentD.class.getName(), new Object[] {}));
        pB1 = cf.newFcInstance(B_Type, new ControllerDescription("pB1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        pB2 = cf.newFcInstance(B_Type, new ControllerDescription("pB2", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));

        // check that listFc() does not return the name of the collective interface :
        // it should return no client interface
        Assert.assertTrue(Fractal.getBindingController(pD1).listFc().length == 0);

        // bind the components
        System.err.println("Fractal.getBindingController(pD1).bindFc(i2_01, pB1.getFcInterface(i2));");
        Fractal.getBindingController(pD1).bindFc("i2_01", pB1.getFcInterface("i2"));

        System.err.println("Fractal.getBindingController(pD1).bindFc(i2_02, pB2.getFcInterface(i2));");
        Fractal.getBindingController(pD1).bindFc("i2_02", pB2.getFcInterface("i2"));

        // check that listFc() does not return the name of the collective interface
        String[] listFc = Fractal.getBindingController(pD1).listFc();
        String listItf = "[";
        for (String string : listFc) {
            listItf += (string + ", ");
        }
        listItf += "]";
        Arrays.sort(listFc);
        Assert.assertTrue("Wrong interface list: " + listItf, Arrays.equals(listFc, new String[] { "i2_01",
                "i2_02" }));

        // start them
        Fractal.getLifeCycleController(pD1).startFc();
        Fractal.getLifeCycleController(pB1).startFc();
        Fractal.getLifeCycleController(pB2).startFc();

        message = null;
        I1Multicast i1 = (I1Multicast) pD1.getFcInterface("i1");
        List<Message> msg1 = i1.processInputMessage(new Message(MESSAGE));
        for (Message message : msg1) {
            message.append(MESSAGE);
        }

        //      test collection itf with composite component
        Component c1 = cf.newFcInstance(fType, new ControllerDescription("composite1", Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}));
        Component pB3 = cf.newFcInstance(B_Type, new ControllerDescription("pB3", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        Component pB4 = cf.newFcInstance(B_Type, new ControllerDescription("pB4", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        Component pB5 = cf.newFcInstance(B_Type, new ControllerDescription("pB5", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        Component pB6 = cf.newFcInstance(B_Type, new ControllerDescription("pB6", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        Component pE = cf.newFcInstance(eType, new ControllerDescription("pE", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentE.class.getName(), new Object[] {}));
        Fractal.getContentController(c1).addFcSubComponent(pB3);
        Fractal.getContentController(c1).addFcSubComponent(pB4);
        Fractal.getContentController(c1).addFcSubComponent(pE);
        Fractal.getBindingController(c1).bindFc("i2-server-01", pB3.getFcInterface("i2"));
        Fractal.getBindingController(c1).bindFc("i2-server-02", pB4.getFcInterface("i2"));
        Fractal.getBindingController(pE).bindFc("i2-01", c1.getFcInterface("i2-client-01"));
        Fractal.getBindingController(pE).bindFc("i2-02", c1.getFcInterface("i2-client-02"));
        Fractal.getBindingController(c1).bindFc("i2-client-01", pB5.getFcInterface("i2"));
        Fractal.getBindingController(c1).bindFc("i2-client-02", pB6.getFcInterface("i2"));

        Fractal.getLifeCycleController(c1).startFc();
        Fractal.getLifeCycleController(pB5).startFc();
        Fractal.getLifeCycleController(pB6).startFc();
        ((I1Multicast) pE.getFcInterface("i1")).processInputMessage(new Message(""));
        Message expected = new Message("composite-" + PrimitiveComponentB.MESSAGE);
        Message m1 = ((I2) c1.getFcInterface("i2-server-01")).processOutputMessage(new Message("composite-"));
        Message m2 = ((I2) c1.getFcInterface("i2-server-02")).processOutputMessage(new Message("composite-"));
        Assert.assertEquals(new Message("composite-" + PrimitiveComponentB.MESSAGE).toString(), PAFuture
                .getFutureValue(m1).toString());
        Assert.assertEquals(new Message("composite-" + PrimitiveComponentB.MESSAGE).toString(), PAFuture
                .getFutureValue(m2).toString());

        StringBuffer resulting_msg = new StringBuffer();
        for (Message message : msg1) {
            resulting_msg.append(message.toString());
        }

        // this --> primitiveA --> primitiveB --> primitiveA --> this  (message goes through composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentD.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentD.MESSAGE + Test.MESSAGE;
        Assert.assertEquals(resulting_msg.toString(), single_message + single_message);
    }
}
