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
package nonregressiontest.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.creation.ComponentA;
import nonregressiontest.component.creation.ComponentInfo;


/**
 * @author Matthieu Morel
 *
 * creates a new component
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;
    String result = null;

    public Test() {
        super("Components : Addition of a custom controller",
            "Components : Addition of a custom controller");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        componentA = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("componentInfo",
                            ComponentInfo.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    }),
                new ControllerDescription("componentA", Constants.PRIMITIVE,
                    getClass()
                        .getResource("/nonregressiontest/component/controller/config.xml")
                        .getPath()),
                new ContentDescription(ComponentA.class.getName(),
                    new Object[] { "toto" }));
        //logger.debug("OK, instantiated the component");
        ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).setDummyValue(
            "DUMMY");
        result = ((DummyController) componentA.getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).getDummyValue();
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
        return "DUMMY".equals(result);
    }
}
