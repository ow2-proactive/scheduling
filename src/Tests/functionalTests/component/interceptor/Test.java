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
package functionalTests.component.interceptor;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import functionalTests.ComponentTest;
import functionalTests.component.controller.DummyController;


/**
 *
 * Checks that interception of functional invocations works, and that the
 * order of the inputInterceptors is the same than this of the controllers config file.
 *
 * Interceptors are only placed around the "A" component
 *
 *
 * @author Matthieu Morel
 *
 */
public class Test extends ComponentTest {

    /**
     *
     */
    Component componentA;
    Component componentB;
    String name;
    String nodeUrl;
    String result = null;
    public static final String DUMMY_VALUE = "dummy-value";

    public Test() {
        super("Components : interception of functional invocations",
                "Components : interception of functional invocations");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        componentA = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(FooItf.CLIENT_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription("A", Constants.PRIMITIVE, getClass().getResource(
                        "/functionalTests/component/interceptor/config.xml").getPath()),
                new ContentDescription(A.class.getName(), new Object[] {}));

        componentB = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE), }), new ControllerDescription("B",
            Constants.PRIMITIVE), new ContentDescription(B.class.getName(), new Object[] {}));

        Fractal.getBindingController(componentA).bindFc(FooItf.CLIENT_ITF_NAME,
                componentB.getFcInterface(FooItf.SERVER_ITF_NAME));

        //logger.debug("OK, instantiated the component");
        ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME))
                .setDummyValue(Test.DUMMY_VALUE);

        Fractal.getLifeCycleController(componentA).startFc();
        Fractal.getLifeCycleController(componentB).startFc();
        // invoke functional methods on A
        // each invocation actually triggers a modification of the dummy value of the dummy controller
        ((FooItf) componentA.getFcInterface(FooItf.SERVER_ITF_NAME)).foo();
        //((FooItf) componentA.getFcInterface("fooItf")).foo();
        result = ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME))
                .getDummyValue();

        String expectedResult = DUMMY_VALUE + InputInterceptor1.BEFORE_INTERCEPTION +
            InputOutputInterceptor.BEFORE_INPUT_INTERCEPTION +
            // starting invocation, which performs an output invocation, hence the following
            InputOutputInterceptor.BEFORE_OUTPUT_INTERCEPTION + OutputInterceptor1.BEFORE_INTERCEPTION +
            OutputInterceptor1.AFTER_INTERCEPTION + InputOutputInterceptor.AFTER_OUTPUT_INTERCEPTION +
            // invocation now finished
            InputOutputInterceptor.AFTER_INPUT_INTERCEPTION + InputInterceptor1.AFTER_INTERCEPTION;
        ;
        Assert.assertEquals(expectedResult, result);
    }
}
