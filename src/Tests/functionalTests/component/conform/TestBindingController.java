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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.J;


public class TestBindingController extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected ComponentType u;
    protected Component c;
    protected Component d;
    protected Component e;
    protected boolean isTemplate;

    // -------------------------------------------------------------------------
    // Constructor ans setup
    // -------------------------------------------------------------------------
    public TestBindingController() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", I.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    tf.createFcItfType("servers", I.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION),
                    tf.createFcItfType("client", I.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    tf.createFcItfType("clients", I.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION)
                });
        u = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("serverI", I.class.getName(),
                        TypeFactory.SERVER, TypeFactory.OPTIONAL,
                        TypeFactory.SINGLE),
                    tf.createFcItfType("serverJ", J.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
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

    protected void checkList(BindingController bc, String[] expected) {
        String[] names = bc.listFc();
        HashSet<String> nameSet = new HashSet<String>();
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            if (!nameSet.add(name)) {
                fail("Duplicated interface name: " + name);
            }
        }
        assertEquals(new HashSet<String>(Arrays.asList(expected)), nameSet);
    }

    // -------------------------------------------------------------------------
    // Test errors in lookup, bind, unbind
    // -------------------------------------------------------------------------
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
}
