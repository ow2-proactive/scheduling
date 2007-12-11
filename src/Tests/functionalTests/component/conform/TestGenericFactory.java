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

import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.CAttributes;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.W;
import functionalTests.component.conform.components.X;
import functionalTests.component.conform.components.Y;
import functionalTests.component.conform.components.Z;


public class TestGenericFactory extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected ComponentType u;
    protected final static String AC = "attribute-controller/" + PKG +
        ".CAttributes/false,false,false";
    protected final static String sI = "server/" + PKG +
        ".I/false,false,false";
    protected final static String cI = "client/" + PKG + ".I/true,false,false";

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", I.class.getName(), false,
                        false, false),
                    tf.createFcItfType("client", I.class.getName(), true,
                        false, false)
                });
        u = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("attribute-controller",
                        CAttributes.class.getName(), false, false, false),
                    tf.createFcItfType("server", I.class.getName(), false,
                        false, false),
                    tf.createFcItfType("client", I.class.getName(), true,
                        false, false)
                });
    }

    // -------------------------------------------------------------------------
    // Test direct component creation
    // -------------------------------------------------------------------------
    @Test
    public void testFPrimitive() throws Exception {
        Component c = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, NC, CP, MCC, GC, MC, PC, sI, cI })));
    }

    @Test
    public void testFParametricPrimitive() throws Exception {
        Component c = gf.newFcInstance(u, flatParametricPrimitive,
                C.class.getName());
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, AC, NC, CP, MCC, GC, MC, PC, sI, cI })));
    }

    @Test
    public void testPrimitive() throws Exception {
        Component c = gf.newFcInstance(t, "primitive", C.class.getName());
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, SC, NC, CP, MCC, GC, MC, PC, sI, cI })));
    }

    @Test
    public void testParametricPrimitive() throws Exception {
        Component c = gf.newFcInstance(u, parametricPrimitive, C.class.getName());
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, MC, LC, MCC, GC, SC, NC, CP, AC, PC, sI, cI
                    })));
        //          new Object[] { COMP, BC, LC, SC, AC, NC, sI, cI })));
    }

    @Test
    public void testComposite() throws Exception {
        Component c = gf.newFcInstance(t, "composite", null);
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, NC, CP, MCC, GC, MC, PC, sI, cI
                    })));
    }

    @Test
    public void testParametricComposite() throws Exception {
        Component c = gf.newFcInstance(u, parametricComposite, C.class.getName());
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, AC, NC, CP, MCC, GC, MC, PC, sI, cI
                    })));
    }

    // -------------------------------------------------------------------------
    // Test component creation via templates
    // -------------------------------------------------------------------------
    @Test
    @Ignore
    public void testFPrimitiveTemplate() throws Exception {
        Component c = gf.newFcInstance(t, "flatPrimitiveTemplate",
                new Object[] { "flatPrimitive", C.class.getName() });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, F, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, NC, PC, sI, cI })));
    }

    @Test
    @Ignore
    public void testFParametricPrimitiveTemplate() throws Exception {
        Component c = gf.newFcInstance(u, "flatParametricPrimitiveTemplate",
                new Object[] { "flatParametricPrimitive", C.class.getName() });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, F, AC, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, AC, NC, PC, sI, cI })));
    }

    @Test
    @Ignore
    public void testPrimitiveTemplate() throws Exception {
        Component c = gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", C.class.getName() });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, F, SC, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, SC, NC, PC, sI, cI })));
    }

    @Test
    @Ignore
    public void testParametricPrimitiveTemplate() throws Exception {
        Component c = gf.newFcInstance(u, "parametricPrimitiveTemplate",
                new Object[] { "parametricPrimitive", C.class.getName() });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, F, SC, AC, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, LC, SC, AC, NC, PC, sI, cI })));
    }

    @Test
    @Ignore
    public void testCompositeTemplate() throws Exception {
        Component c = gf.newFcInstance(t, "compositeTemplate",
                new Object[] { "composite", null });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, CC, F, SC, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, CC, LC, SC, NC, PC, sI, cI })));
    }

    @Test
    @Ignore
    public void testParametricCompositeTemplate() throws Exception {
        Component c = gf.newFcInstance(u, "parametricCompositeTemplate",
                new Object[] { "parametricComposite", C.class.getName() });
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, CC, F, SC, AC, NC, PC, sI, cI })));
        c = Fractal.getFactory(c).newFcInstance();
        checkComponent(c,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, BC, CC, LC, SC, AC, NC, PC, sI, cI })));
    }

    // -------------------------------------------------------------------------
    // Test component creation errors
    // -------------------------------------------------------------------------
    @Test
    public void testUnknownControllerDescriptor() throws Exception {
        try {
            // no such controller descriptor
            gf.newFcInstance(t, "unknownDescriptor", C.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testBadControllerDescriptor1() throws Exception {
        try {
            // error in controller descriptor
            gf.newFcInstance(t, badPrimitive, C.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testBadControllerDescriptor2() throws Exception {
        try {
            // error in controller descriptor
            gf.newFcInstance(u, badParametricPrimitive, C.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testContentClassNotFound() throws Exception {
        try {
            // no such class
            gf.newFcInstance(t, "primitive", "UnknownClass");
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testContentClassAbstract() throws Exception {
        try {
            // X is an abstract class
            gf.newFcInstance(t, "primitive", W.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testContentClassNoDefaultConstructor()
        throws Exception {
        try {
            // X has no public constructor
            gf.newFcInstance(t, "primitive", X.class.getName());
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test(expected = InstantiationException.class)
    @Ignore
    public void testContentClassControlInterfaceMissing()
        throws Exception {
        // Y does not implement BindingController
        gf.newFcInstance(t, "primitive", Y.class.getName());
    }

    @Test(expected = InstantiationException.class)
    @Ignore
    public void testContentClassInterfaceMissing() throws Exception {
        // Z does not implement I
        gf.newFcInstance(t, "primitive", Z.class.getName());
    }

    @Test
    public void testTemplateContentClassNotFound() throws Exception {
        try {
            // no such class
            gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", "UnknownClass" });
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testTemplateContentClassAbstract() throws Exception {
        try {
            // X is an abstract class
            gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", W.class.getName() });
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testTemplateContentClassNoDefaultConstructor()
        throws Exception {
        try {
            // X has no public constructor
            gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", X.class.getName() });
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testTemplateContentClassControlInterfaceMissing()
        throws Exception {
        try {
            // Y does not implement BindingController
            gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", Y.class.getName() });
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testTemplateContentClassInterfaceMissing()
        throws Exception {
        try {
            // Z does not implement I
            gf.newFcInstance(t, "primitiveTemplate",
                new Object[] { "primitive", Z.class.getName() });
            fail();
        } catch (InstantiationException e) {
        }
    }
}
