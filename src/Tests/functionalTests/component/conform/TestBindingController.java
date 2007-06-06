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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.J;


public class TestBindingController extends BindingConformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected ComponentType u;
    protected Component c;
    protected Component d;
    protected Component e;
    protected boolean isComposite;
    protected boolean isTemplate;

    // -------------------------------------------------------------------------
    // Constructor ans setup
    // -------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", I.class.getName(), false,
                        false, false),
                    tf.createFcItfType("servers", I.class.getName(), false,
                        false, true),
                    tf.createFcItfType("client", I.class.getName(), true,
                        false, false),
                    tf.createFcItfType("clients", I.class.getName(), true,
                        false, true)
                });
        u = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("serverI", I.class.getName(), false,
                        true, false),
                    tf.createFcItfType("serverJ", J.class.getName(), false,
                        false, false),
                });
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        d = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        e = gf.newFcInstance(u, flatPrimitive, C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test list, lookup, bind, unbind
    // -------------------------------------------------------------------------
    @Test
    public void testList() throws Exception {
        BindingController bc = Fractal.getBindingController(c);
        checkList(bc, new String[] { "client" });
    }

    @Test
    @Ignore
    public void testBindLookupUnbind() throws Exception {
        BindingController bc = Fractal.getBindingController(c);
        bc.bindFc("client", d.getFcInterface("server"));
        checkList(bc, new String[] { "client" });
        assertEquals(d.getFcInterface("server"), bc.lookupFc("client"));
        bc.unbindFc("client");
        assertEquals(null, bc.lookupFc("client"));
    }

    @Test
    @Ignore
    public void testCollectionBindLookupUnbind() throws Exception {
        BindingController bc = Fractal.getBindingController(c);
        bc.bindFc("clients0", d.getFcInterface("server"));
        checkList(bc, new String[] { "client", "clients0" });
        assertEquals(d.getFcInterface("server"), bc.lookupFc("clients0"));
        bc.unbindFc("clients0");
        try {
            assertEquals(null, bc.lookupFc("clients0"));
        } catch (NoSuchInterfaceException e) {
            checkList(bc, new String[] { "client" });
        }
    }

    @Test
    @Ignore
    public void testNoSuchInterfaceLookup() throws Exception {
        try {
            Fractal.getBindingController(c).lookupFc("c");
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    @Ignore
    public void testNoSuchInterfaceBind() throws Exception {
        try {
            Fractal.getBindingController(c)
                   .bindFc("c", d.getFcInterface("server"));
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    @Ignore
    public void testNotAServerInterface() throws Exception {
        try {
            Fractal.getBindingController(c)
                   .bindFc("client", c.getFcInterface("client"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    public void testWrongType() throws Exception {
        try {
            Fractal.getBindingController(c)
                   .bindFc("client", e.getFcInterface("serverJ"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    @Ignore
    public void testMandatoryToOptional() throws Exception {
        try {
            Fractal.getBindingController(c)
                   .bindFc("client", e.getFcInterface("serverI"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    @Ignore
    public void testAlreadyBound() throws Exception {
        BindingController bc = Fractal.getBindingController(c);
        bc.bindFc("client", d.getFcInterface("server"));
        try {
            bc.bindFc("client", d.getFcInterface("server"));
            fail();
        } catch (IllegalBindingException e) {
        }
        bc.bindFc("clients0", d.getFcInterface("server"));
        try {
            bc.bindFc("clients0", d.getFcInterface("server"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    @Ignore
    public void testNoSuchInterfaceUnind() throws Exception {
        try {
            Fractal.getBindingController(c).unbindFc("c");
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    @Ignore
    public void testNotBound() throws Exception {
        try {
            Fractal.getBindingController(c).unbindFc("client");
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    // ---
    public static class Composite extends TestBindingController {
        protected Component r;

        public Composite() {
            isComposite = true;
        }

        protected void setUpComponents() throws Exception {
            r = gf.newFcInstance(t, "composite", null);
            c = gf.newFcInstance(t, "primitive", C.class.getName());
            d = gf.newFcInstance(t, "composite", null);
            e = gf.newFcInstance(u, "composite", null);
            Fractal.getContentController(r).addFcSubComponent(c);
            Fractal.getContentController(r).addFcSubComponent(d);
            Fractal.getContentController(r).addFcSubComponent(e);
        }

        @Test
        @Ignore
        public void testCompositeList() throws Exception {
            BindingController bc = Fractal.getBindingController(r);
            if (isTemplate) {
                checkList(bc, new String[] { "client", "server", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server" });
            }
        }

        @Test
        @Ignore
        public void testCompositeExportBindLookupUnbind()
            throws Exception {
            BindingController bc = Fractal.getBindingController(r);
            bc.bindFc("server", c.getFcInterface("server"));
            if (isTemplate) {
                checkList(bc, new String[] { "client", "server", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server" });
            }
            assertEquals(c.getFcInterface("server"), bc.lookupFc("server"));
            bc.unbindFc("server");
            assertEquals(null, bc.lookupFc("server"));
        }

        @Test
        @Ignore
        public void testCompositeCollectionExportBindLookupUnbind()
            throws Exception {
            BindingController bc = Fractal.getBindingController(r);
            bc.bindFc("servers0", c.getFcInterface("server"));
            if (isTemplate) {
                checkList(bc,
                    new String[] { "client", "server", "servers0", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server", "servers0" });
            }
            assertEquals(c.getFcInterface("server"), bc.lookupFc("servers0"));
            bc.unbindFc("servers0");
            try {
                assertEquals(null, bc.lookupFc("servers0"));
            } catch (NoSuchInterfaceException e) {
                checkList(bc, new String[] { "client", "server" });
            }
        }

        @Test
        @Ignore
        public void testCompositeImportBindLookupUnbind()
            throws Exception {
            ContentController cc = Fractal.getContentController(r);
            BindingController bc = Fractal.getBindingController(d);
            bc.bindFc("client", cc.getFcInternalInterface("client"));
            if (isTemplate) {
                checkList(bc, new String[] { "client", "server", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server" });
            }
            assertEquals(cc.getFcInternalInterface("client"),
                bc.lookupFc("client"));
            bc.unbindFc("client");
            assertEquals(null, bc.lookupFc("client"));
        }

        @Test
        @Ignore
        public void testCompositeCollectionImportBindLookupUnbind()
            throws Exception {
            ContentController cc = Fractal.getContentController(r);
            BindingController bc = Fractal.getBindingController(d);
            bc.bindFc("clients0", cc.getFcInternalInterface("client"));
            if (isTemplate) {
                checkList(bc,
                    new String[] { "client", "clients0", "server", "factory" });
            } else {
                checkList(bc, new String[] { "client", "clients0", "server" });
            }
            assertEquals(cc.getFcInternalInterface("client"),
                bc.lookupFc("clients0"));
            bc.unbindFc("clients0");
            try {
                assertEquals(null, bc.lookupFc("clients0"));
            } catch (NoSuchInterfaceException e) {
                if (isTemplate) {
                    checkList(bc, new String[] { "client", "server", "factory" });
                } else {
                    checkList(bc, new String[] { "client", "server" });
                }
            }
        }

        @Test
        @Ignore
        public void testCompositeSelfBindLookupUnbind()
            throws Exception {
            Object itf = Fractal.getContentController(r)
                                .getFcInternalInterface("client");
            BindingController bc = Fractal.getBindingController(r);
            bc.bindFc("server", itf);
            if (isTemplate) {
                checkList(bc, new String[] { "client", "server", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server" });
            }
            assertEquals(itf, bc.lookupFc("server"));
            bc.unbindFc("server");
            assertEquals(null, bc.lookupFc("server"));
        }

        @Test
        @Ignore
        public void testCompositeCollectionSelfBindLookupUnbind()
            throws Exception {
            Object itf = Fractal.getContentController(r)
                                .getFcInternalInterface("clients0");
            BindingController bc = Fractal.getBindingController(r);
            bc.bindFc("servers0", itf);
            if (isTemplate) {
                checkList(bc,
                    new String[] { "client", "server", "servers0", "factory" });
            } else {
                checkList(bc, new String[] { "client", "server", "servers0" });
            }
            assertEquals(itf, bc.lookupFc("servers0"));
            bc.unbindFc("servers0");
            try {
                assertEquals(null, bc.lookupFc("servers0"));
            } catch (NoSuchInterfaceException e) {
                checkList(bc, new String[] { "client", "server" });
            }
        }

        @Test
        @Ignore
        public void testCompositeNoSuchInterface() throws Exception {
            try {
                Fractal.getBindingController(r).lookupFc("c");
                fail();
            } catch (NoSuchInterfaceException e) {
            }
        }

        @Test
        @Ignore
        public void testAlreadyBound() throws Exception {
            super.testAlreadyBound();
            BindingController bc = Fractal.getBindingController(d);
            bc.bindFc("client", c.getFcInterface("server"));
            try {
                bc.bindFc("client", c.getFcInterface("server"));
                fail();
            } catch (IllegalBindingException e) {
            }
            bc.bindFc("clients0", c.getFcInterface("server"));
            try {
                bc.bindFc("clients0", c.getFcInterface("server"));
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testNotBound() throws Exception {
            super.testNotBound();
            try {
                Fractal.getBindingController(d).unbindFc("client");
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testInvalidExportBinding() throws Exception {
            Fractal.getContentController(r).removeFcSubComponent(c);
            BindingController bc = Fractal.getBindingController(r);
            try {
                bc.bindFc("server", c.getFcInterface("server"));
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testInvalidNormalBinding() throws Exception {
            Fractal.getContentController(r).removeFcSubComponent(d);
            BindingController bc = Fractal.getBindingController(c);
            try {
                bc.bindFc("client", d.getFcInterface("server"));
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testInvalidImportBinding() throws Exception {
            BindingController bc = Fractal.getBindingController(d);
            ContentController cc = Fractal.getContentController(r);
            cc.removeFcSubComponent(d);
            try {
                bc.bindFc("client", cc.getFcInternalInterface("client"));
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testInvalidImportBinding2() throws Exception {

            /*
             * Test added by L. Seinturier.
             * Following a bug report in AOKell submitted by P.-C. David.
             */
            BindingController bc = Fractal.getBindingController(r);
            try {
                bc.bindFc("client", d.getFcInterface("server"));
                fail();
            } catch (IllegalBindingException e) {
            }
        }

        @Test
        @Ignore
        public void testWouldCreateInvalidExportBinding()
            throws Exception {
            Fractal.getBindingController(r)
                   .bindFc("server", c.getFcInterface("server"));
            try {
                Fractal.getContentController(r).removeFcSubComponent(c);
                fail();
            } catch (IllegalContentException e) {
            }
        }

        @Test
        @Ignore
        public void testWouldCreateInvalidLocalBinding()
            throws Exception {
            Fractal.getBindingController(c)
                   .bindFc("client", d.getFcInterface("server"));
            try {
                Fractal.getContentController(r).removeFcSubComponent(c);
                fail();
            } catch (IllegalContentException e) {
            }
        }

        @Test
        @Ignore
        public void testWouldCreateInvalidImportBinding()
            throws Exception {
            ContentController cc = Fractal.getContentController(r);
            Fractal.getBindingController(d)
                   .bindFc("client", cc.getFcInternalInterface("client"));
            try {
                cc.removeFcSubComponent(d);
                fail();
            } catch (IllegalContentException e) {
            }
        }
    }

    public static class Template extends TestBindingController {
        public Template() {
            isTemplate = true;
        }

        protected void setUpComponents() throws Exception {
            c = gf.newFcInstance(t, flatPrimitiveTemplate, C.class.getName());
            d = gf.newFcInstance(t, flatPrimitiveTemplate, C.class.getName());
            e = gf.newFcInstance(u, flatPrimitiveTemplate, C.class.getName());
        }
    }

    public static class CompositeTemplate extends Composite {
        public CompositeTemplate() {
            isTemplate = true;
        }

        protected void setUpComponents() throws Exception {
            r = gf.newFcInstance(t, compositeTemplate, null);
            c = gf.newFcInstance(t, primitiveTemplate, C.class.getName());
            d = gf.newFcInstance(t, compositeTemplate, null);
            e = gf.newFcInstance(u, compositeTemplate, null);
            Fractal.getContentController(r).addFcSubComponent(c);
            Fractal.getContentController(r).addFcSubComponent(d);
            Fractal.getContentController(r).addFcSubComponent(e);
        }

        @Test
        @Ignore
        public void testInstanceBinding() throws Exception {
            ContentController cc = Fractal.getContentController(r);
            Fractal.getContentController(e).addFcSubComponent(c);

            Fractal.getBindingController(r)
                   .bindFc("server", c.getFcInterface("server"));
            Fractal.getBindingController(c)
                   .bindFc("client", d.getFcInterface("server"));
            Fractal.getBindingController(d)
                   .bindFc("client", cc.getFcInternalInterface("client"));

            Fractal.getBindingController(r)
                   .bindFc("servers0", c.getFcInterface("servers0"));
            Fractal.getBindingController(c)
                   .bindFc("clients0", d.getFcInterface("servers0"));
            Fractal.getBindingController(d)
                   .bindFc("clients0", cc.getFcInternalInterface("clients0"));

            Component rComp = Fractal.getFactory(r).newFcInstance();

            cc = Fractal.getContentController(rComp);
            Component[] comps = cc.getFcSubComponents();

            Component cComp = comps[0];
            Component dComp = comps[1];

            assertEquals(Fractal.getBindingController(rComp).lookupFc("server"),
                cComp.getFcInterface("server"));
            assertEquals(Fractal.getBindingController(cComp).lookupFc("client"),
                dComp.getFcInterface("server"));
            assertEquals(Fractal.getBindingController(dComp).lookupFc("client"),
                cc.getFcInternalInterface("client"));

            assertEquals(Fractal.getBindingController(rComp).lookupFc("servers0"),
                cComp.getFcInterface("servers0"));
            assertEquals(Fractal.getBindingController(cComp).lookupFc("clients0"),
                dComp.getFcInterface("servers0"));
            assertEquals(Fractal.getBindingController(dComp).lookupFc("clients0"),
                cc.getFcInternalInterface("clients0"));
        }
    }
}
