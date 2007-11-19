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
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;


public class TestBindingControllerComposite extends TestBindingController {
    protected Component r;

    public TestBindingControllerComposite() {
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
    public void testCompositeExportBindLookupUnbind() throws Exception {
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
    public void testCompositeImportBindLookupUnbind() throws Exception {
        ContentController cc = Fractal.getContentController(r);
        BindingController bc = Fractal.getBindingController(d);
        bc.bindFc("client", cc.getFcInternalInterface("client"));
        if (isTemplate) {
            checkList(bc, new String[] { "client", "server", "factory" });
        } else {
            checkList(bc, new String[] { "client", "server" });
        }
        assertEquals(cc.getFcInternalInterface("client"), bc.lookupFc("client"));
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
            checkList(bc, new String[] { "client", "server" });
        }
    }

    @Test
    @Ignore
    public void testCompositeSelfBindLookupUnbind() throws Exception {
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
        ContentController cc = Fractal.getContentController(r);
        BindingController bc = Fractal.getBindingController(d);
        cc.removeFcSubComponent(d);
        try {
            bc.bindFc("client", cc.getFcInternalInterface("client"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    @Ignore
    public void testWouldCreateInvalidExportBinding() throws Exception {
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
    public void testWouldCreateInvalidLocalBinding() throws Exception {
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
    public void testWouldCreateInvalidImportBinding() throws Exception {
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
