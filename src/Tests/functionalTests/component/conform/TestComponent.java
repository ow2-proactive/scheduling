/***
 * Julia: France Telecom's implementation of the Fractal API
 * Copyright (C) 2001-2002 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.CAttributes;
import functionalTests.component.conform.components.I;


public class TestComponent extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected final static String AC = "attribute-controller/" + PKG + ".CAttributes/false,false,false";
    protected final static String sI = "server/" + PKG + ".I/false,false,false";
    protected final static String cI = "client/" + PKG + ".I/true,false,false";

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

    // -------------------------------------------------------------------------
    // Test functional and attribute controller interfaces
    // -------------------------------------------------------------------------
    @Test
    public void testParametricPrimitive() throws Exception {
        Component c = gf.newFcInstance(t, parametricPrimitive, C.class.getName());
        Fractal.getLifeCycleController(c).startFc();
        I i = (I) c.getFcInterface("server");
        checkInterface(i);

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
        ca.setWriteOnlyX11(true);
        assertEquals(true, i.n(false, null));
    }

    @Test
    @Ignore
    public void testParametricPrimitiveTemplate() throws Exception {
        Component c = gf.newFcInstance(t, parametricPrimitiveTemplate, C.class.getName());

        CAttributes ca = (CAttributes) c.getFcInterface("attribute-controller");
        ca.setX1(true);
        ca.setX2((byte) 1);
        ca.setX3((char) 1);
        ca.setX4((short) 1);
        ca.setX5(1);
        ca.setX6(1);
        ca.setX7(1);
        ca.setX8(1);
        ca.setX9("1");
        ca.setWriteOnlyX11(true);

        c = Fractal.getFactory(c).newFcInstance();
        Fractal.getLifeCycleController(c).startFc();
        ca = (CAttributes) c.getFcInterface("attribute-controller");

        assertEquals(true, ca.getX1());
        assertEquals((byte) 1, ca.getX2());
        assertEquals((char) 1, ca.getX3());
        assertEquals((short) 1, ca.getX4());
        assertEquals(1, ca.getX5());
        assertEquals((long) 1, ca.getX6());
        assertEquals(1, ca.getX7(), 0);
        assertEquals(1, ca.getX8(), 0);
        assertEquals("1", ca.getX9());
        assertEquals(true, ((I) c.getFcInterface("server")).n(false, null));
    }
}
