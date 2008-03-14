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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.CAttributes;
import functionalTests.component.conform.components.CAttributesCompositeImpl;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.J;


public class TestAttributesComposite extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("attribute-controller", CAttributes.class.getName(), false, false, false),
                tf.createFcItfType("server", I.class.getName(), false, false, false),
                tf.createFcItfType("client", I.class.getName(), true, true, false) });
    }

    // -----------------------------------------------------------------------------------
    // Full test
    // -----------------------------------------------------------------------------------
    @Test
    public void testCompositeWithAttributeController() throws Exception {
        try {
            Component c = gf.newFcInstance(t, "composite", CAttributesCompositeImpl.class.getName());
            Fractal.getLifeCycleController(c).startFc();
            CAttributes ca = (CAttributes) c.getFcInterface("attribute-controller");
            ca.setX1(true);
            assertEquals(true, ca.getX1());
            ca.setX2((byte) 1);
            assertEquals((byte) 1, ca.getX2());
            ca.setX3((char) 1);
            assertEquals((char) 1, ca.getX3());
            ca.setX4((short) 1);
            assertEquals((short) 1, ca.getX4());
            ca.setX5(1);
            assertEquals(1, ca.getX5());
            ca.setX6(1);
            assertEquals((long) 1, ca.getX6());
            ca.setX7(1);
            assertEquals(1, ca.getX7(), 0);
            ca.setX8(1);
            assertEquals(1, ca.getX8(), 0);
            ca.setX9("1");
            assertEquals("1", ca.getX9());
        } catch (InstantiationException e) {
        }
    }

    // -----------------------------------------------------------------------------------
    // Test composite with content do not extends AttributeController
    // -----------------------------------------------------------------------------------
    @Test
    public void testCompositeWithContentError() throws Exception {
        try {
            gf.newFcInstance(t, "composite", J.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }
}
