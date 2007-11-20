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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;


public class TestContentController extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected Component c;
    protected Component d;
    protected Component e;

    // -------------------------------------------------------------------------
    // Constructor ans setup
    // -------------------------------------------------------------------------

    //  public TestContentController (final String name) {
    //    super(name);
    //  }
    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", I.class.getName(), false,
                        false, false),
                    tf.createFcItfType("client", I.class.getName(), true, true,
                        false)
                });
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, "composite", null);
        d = gf.newFcInstance(t, "composite", null);
        e = gf.newFcInstance(t, "primitive", C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test add and remove
    // -------------------------------------------------------------------------
    @Test
    public void testAddAndRemove() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        assertTrue(Arrays.asList(cc.getFcSubComponents()).contains(e));
        cc.removeFcSubComponent(e);
        assertTrue(!Arrays.asList(cc.getFcSubComponents()).contains(e));
    }

    // -------------------------------------------------------------------------
    // Test add errors
    // -------------------------------------------------------------------------
    @Test
    @Ignore
    public void testAlreadySubComponent() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        try {
            cc.addFcSubComponent(e);
            fail();
        } catch (IllegalContentException e) {
        }
    }

    @Test
    @Ignore
    public void testWouldCreateCycle1() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        try {
            cc.addFcSubComponent(c);
            fail();
        } catch (IllegalContentException e) {
        }
    }

    @Test
    @Ignore
    public void testWouldCreateCycle2() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        ContentController cd = Fractal.getContentController(d);
        cc.addFcSubComponent(d);
        try {
            cd.addFcSubComponent(c);
            fail();
        } catch (IllegalContentException e) {
        }
    }

    // -------------------------------------------------------------------------
    // Test remove errors
    // -------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testNotASubComponent() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        // must throw an IllegalContentException
        cc.removeFcSubComponent(d);
    }

    @Test(expected = IllegalContentException.class)
    public void testWouldCreateNonLocalExportBinding()
        throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(c)
               .bindFc("client", e.getFcInterface("client"));
        // must throw an IllegalContentException
        cc.removeFcSubComponent(e);
    }

    @Test
    @Ignore
    public void testWouldCreateNonLocalImportBinding()
        throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(e)
               .bindFc("client", cc.getFcInternalInterface("client"));
        try {
            cc.removeFcSubComponent(e);
            fail();
        } catch (IllegalContentException e) {
        }
    }

    @Test
    @Ignore
    public void testWouldCreateNonLocalNormalBinding()
        throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(d);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(d)
               .bindFc("client", e.getFcInterface("server"));
        try {
            cc.removeFcSubComponent(e);
            fail();
        } catch (IllegalContentException e) {
        }
    }

    // ---
    public static class Template extends TestContentController {
        @Override
        protected void setUpComponents() throws Exception {
            c = gf.newFcInstance(t, compositeTemplate, null);
            d = gf.newFcInstance(t, compositeTemplate, null);
            e = gf.newFcInstance(t, primitiveTemplate, C.class.getName());
        }

        @Test
        @Ignore
        public void testInstanceContent() throws Exception {
            Component r = gf.newFcInstance(t, "compositeTemplate",
                    new Object[] { "composite", null });
            Fractal.getContentController(r).addFcSubComponent(c);
            Fractal.getContentController(r).addFcSubComponent(d);
            Fractal.getContentController(c).addFcSubComponent(e);
            Fractal.getContentController(d).addFcSubComponent(e);

            Component root = Fractal.getFactory(r).newFcInstance();
            Component[] comps = Fractal.getContentController(root)
                                       .getFcSubComponents();
            assertEquals(2, comps.length);
            Component[] cComps = Fractal.getContentController(comps[0])
                                        .getFcSubComponents();
            Component[] dComps = Fractal.getContentController(comps[1])
                                        .getFcSubComponents();
            assertEquals(1, cComps.length);
            assertEquals(1, dComps.length);
            assertEquals(cComps[0], dComps[0]);
        }
    }
}
