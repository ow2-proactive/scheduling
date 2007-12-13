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
package functionalTests.component.binding.remote.collective;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;

import functionalTests.ComponentTest;
import functionalTests.component.I1;
import functionalTests.component.I2;
import functionalTests.component.Message;
import functionalTests.component.PrimitiveComponentB;
import functionalTests.component.PrimitiveComponentD;
import functionalTests.descriptor.defaultnodes.TestNodes;


/**
 * @author Matthieu Morel
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
        super("Communication between remote primitive components through client collective interface",
                "Communication between remote primitive components through client collective interface ");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);

        ComponentType D_Type = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION) });
        ComponentType B_Type = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "i2", I2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });

        // instantiate the components
        pD1 = cf.newFcInstance(D_Type, new ControllerDescription("pD1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentD.class.getName(), new Object[] {}), TestNodes
                        .getRemoteACVMNode());
        pB1 = cf.newFcInstance(B_Type, new ControllerDescription("pB1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        pB2 = cf.newFcInstance(B_Type, new ControllerDescription("pB2", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}), TestNodes
                        .getRemoteACVMNode());

        // bind the components
        Fractal.getBindingController(pD1).bindFc("i2", pB1.getFcInterface("i2"));
        Fractal.getBindingController(pD1).bindFc("i2", pB2.getFcInterface("i2"));

        // start them
        Fractal.getLifeCycleController(pD1).startFc();
        Fractal.getLifeCycleController(pB1).startFc();
        Fractal.getLifeCycleController(pB2).startFc();

        message = null;
        I1 i1 = (I1) pD1.getFcInterface("i1");
        Message msg1 = i1.processInputMessage(new Message(MESSAGE));
        message = msg1.append(MESSAGE);

        StringBuffer resulting_msg = new StringBuffer();
        int message_size = PAGroup.size(message);
        for (int i = 0; i < message_size; i++) {
            resulting_msg.append(((Message) PAGroup.get(message, i)).toString());
        }

        // this --> primitiveA --> primitiveB --> primitiveA --> this  (message goes through composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentD.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentD.MESSAGE + Test.MESSAGE;

        Assert.assertEquals(resulting_msg.toString(), single_message + single_message);
    }
}
