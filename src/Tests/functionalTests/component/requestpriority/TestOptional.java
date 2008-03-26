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
package functionalTests.component.requestpriority;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import functionalTests.ComponentTest;


/**
 * Test that the priority controller is optional.
 *
 * @author The ProActive Team
 */
public class TestOptional extends ComponentTest {
    private static String P1_NAME = "priority_component";
    private static String P2_NAME = "standard_component";

    /**
     *
     */
    @Test
    public void testPriorityController() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        ControllerDescription myController = new ControllerDescription(P1_NAME, Constants.PRIMITIVE,
            "/functionalTests/component/requestpriority/my-component-config.xml", false);
        ComponentType pc_type = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                FItf.ITF_NAME, FItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                TypeFactory.SINGLE), });

        Component p1 = cf.newFcInstance(pc_type, myController, new ContentDescription(
            PriotirizedComponent.class.getName(), new Object[] {}));

        assertEquals(Fractal.getNameController(p1).getFcName(), P1_NAME);

        p1.getFcInterface(Constants.REQUEST_PRIORITY_CONTROLLER);
    }

    @Test(expected = NoSuchInterfaceException.class)
    public void testNoPriorityController() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        ComponentType pc_type = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                FItf.ITF_NAME, FItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                TypeFactory.SINGLE), });
        Component p1 = cf.newFcInstance(pc_type, new ControllerDescription(P2_NAME, Constants.PRIMITIVE),
                new ContentDescription(PriotirizedComponent.class.getName(), new Object[] {}));

        assertEquals(Fractal.getNameController(p1).getFcName(), P2_NAME);

        p1.getFcInterface(Constants.REQUEST_PRIORITY_CONTROLLER);
    }
}
