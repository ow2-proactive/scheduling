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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;


public class TestLifeCycleController extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected Component c;
    protected Component d;

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
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        d = gf.newFcInstance(t, flatPrimitive, C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test started and stopped states
    // -------------------------------------------------------------------------
    @Test
    public void testStarted() throws Exception {
        Fractal.getBindingController(c)
               .bindFc("client", d.getFcInterface("server"));
        assertEquals("STOPPED", Fractal.getLifeCycleController(c).getFcState());
        Fractal.getLifeCycleController(c).startFc();
        assertEquals("STARTED", Fractal.getLifeCycleController(c).getFcState());
        final I i = (I) c.getFcInterface("server");
        Thread t = new Thread(new Runnable() {
                    public void run() {
                        i.m(true);
                    }
                });
        t.start();
        t.join(50);
        assertTrue(!t.isAlive());
    }

    // TODO test issue: this test assumes that a call on a stopped interface hangs
    /*
     * This is only one of the possible semantics for the lifecycle controller.
     * For instance, we choose instead, in AOKell, to throw a RuntimeException.
     * Hence the thread is no longer alive.
     */

    //  public void testStopped () throws Exception {      
    //    final I i = (I)c.getFcInterface("server");
    //    Thread t = new Thread(new Runnable() {
    //      public void run () { i.m(true); }
    //    });
    //    t.start();
    //    t.join(50);
    //    assertTrue(t.isAlive());
    //  }

    // -------------------------------------------------------------------------
    // Test errors in start
    // -------------------------------------------------------------------------
    @Test
    @Ignore
    public void testMandatoryInterfaceNotBound() throws Exception {
        try {
            Fractal.getLifeCycleController(c).startFc();
            fail();
        } catch (IllegalLifeCycleException e) {
            assertEquals("STOPPED",
                Fractal.getLifeCycleController(c).getFcState());
        }
    }

    // -------------------------------------------------------------------------
    // Test invalid operations in started state
    // -------------------------------------------------------------------------
    @Test
    @Ignore
    public void testUnbindNotStopped() throws Exception {
        Fractal.getBindingController(c)
               .bindFc("client", d.getFcInterface("server"));
        Fractal.getBindingController(c)
               .bindFc("clients0", d.getFcInterface("server"));
        Fractal.getLifeCycleController(c).startFc();
        try {
            Fractal.getBindingController(c).unbindFc("client");
            fail();
        } catch (IllegalLifeCycleException e) {
        }
        try {
            Fractal.getBindingController(c).unbindFc("clients0");
            fail();
        } catch (IllegalLifeCycleException e) {
        }
    }

    // ---
    public static class Composite extends TestLifeCycleController {
        protected Component r;

        @Override
        protected void setUpComponents() throws Exception {
            Component o = gf.newFcInstance(t, "composite", null);
            r = gf.newFcInstance(t, "composite", null);
            c = gf.newFcInstance(t, "primitive", C.class.getName());
            d = gf.newFcInstance(t, "primitive", C.class.getName());
            Fractal.getContentController(o).addFcSubComponent(r);
            Fractal.getContentController(r).addFcSubComponent(c);
            Fractal.getContentController(r).addFcSubComponent(d);
        }

        @Test
        public void testRecursiveStartStop() throws Exception {
            ContentController cc = Fractal.getContentController(r);
            Fractal.getBindingController(r)
                   .bindFc("server", c.getFcInterface("server"));
            Fractal.getBindingController(c)
                   .bindFc("client", d.getFcInterface("server"));
            Fractal.getBindingController(d)
                   .bindFc("client", cc.getFcInternalInterface("client"));
            Fractal.getBindingController(r)
                   .bindFc("client", r.getFcInterface("server"));

            Fractal.getLifeCycleController(r).startFc();
            assertEquals("STARTED",
                Fractal.getLifeCycleController(r).getFcState());
            assertEquals("STARTED",
                Fractal.getLifeCycleController(c).getFcState());
            assertEquals("STARTED",
                Fractal.getLifeCycleController(d).getFcState());
            final I i = (I) r.getFcInterface("server");
            Thread t = new Thread(new Runnable() {
                        public void run() {
                            i.m(true);
                        }
                    });
            t.start();
            t.join(50);
            assertTrue(!t.isAlive());

            Fractal.getLifeCycleController(r).stopFc();
            assertEquals("STOPPED",
                Fractal.getLifeCycleController(r).getFcState());
            assertEquals("STOPPED",
                Fractal.getLifeCycleController(c).getFcState());
            assertEquals("STOPPED",
                Fractal.getLifeCycleController(d).getFcState());

            // TODO test issue: this test assumes a call on a stopped interface hangs
            //      t = new Thread(new Runnable() {
            //        public void run () { i.m(true); }
            //      });
            //      t.start();
            //      t.join(50);    
            //      assertTrue(t.isAlive());
        }

        @Override
        @Test
        @Ignore
        public void testMandatoryInterfaceNotBound() throws Exception {
            super.testMandatoryInterfaceNotBound();
            ContentController cc = Fractal.getContentController(r);
            cc.removeFcSubComponent(c);
            cc.removeFcSubComponent(d);
            Fractal.getBindingController(r)
                   .bindFc("client", r.getFcInterface("server"));
            try {
                Fractal.getLifeCycleController(c).startFc();
                fail();
            } catch (IllegalLifeCycleException e) {
            }
        }

        @Test
        @Ignore
        public void testRemoveNotStopped() throws Exception {
            ContentController cc = Fractal.getContentController(r);
            Fractal.getBindingController(r)
                   .bindFc("server", c.getFcInterface("server"));
            Fractal.getBindingController(c)
                   .bindFc("client", cc.getFcInternalInterface("client"));
            Fractal.getBindingController(r)
                   .bindFc("client", r.getFcInterface("server"));
            cc.removeFcSubComponent(d);
            Fractal.getLifeCycleController(r).startFc();

            // TODO test issue: adding a sub-component in a started composite automatically starts the added one?
            cc.addFcSubComponent(d);
            try {
                cc.removeFcSubComponent(d);
                // fail();
            } catch (IllegalLifeCycleException e) {
            }
        }
    }
}
