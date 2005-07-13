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
package nonregressiontest.component.interceptor;

import nonregressiontest.component.ComponentTest;

import nonregressiontest.component.controller.DummyController;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;


/**
 *
 * Checks that interception of functional invocations works, and that the
 * order of the interceptors is the same than this of the controllers config file.
 *
 * @author Matthieu Morel
 *
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;
    String result = null;
    public static final String DUMMY_VALUE = "dummy-value";
    public static final String BEFORE_1 = "before-invocation-1";
    public static final String BEFORE_2 = "before-invocation-2";
    public static final String AFTER_1 = "after-invocation-1";
    public static final String AFTER_2 = "after-invocation-2";
    public static final String SEPARATOR = " ; ";

    public Test() {
        super("Components : interception of functional invocations",
            "Components : interception of functional invocations");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        componentA = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("fooItf",
                            FooItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    }),
                new ControllerDescription("A", Constants.PRIMITIVE,
                    getClass()
                        .getResource("/nonregressiontest/component/interceptor/config.xml")
                        .getPath()),
                new ContentDescription(A.class.getName(), new Object[] {  }));
        //logger.debug("OK, instantiated the component");
        ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).setDummyValue(Test.DUMMY_VALUE);

        Fractal.getLifeCycleController(componentA).startFc();
        // invoke functional methods on A
        // each invocation actually triggers a modification of the dummy value of the dummy controller
        ((FooItf) componentA.getFcInterface("fooItf")).foo();
        //((FooItf) componentA.getFcInterface("fooItf")).foo();
        result = ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).getDummyValue();
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
        String expectedResult = DUMMY_VALUE + SEPARATOR + BEFORE_1 + SEPARATOR +
            BEFORE_2 + SEPARATOR + AFTER_2 + SEPARATOR + AFTER_1;
        return expectedResult.equals(result);
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
