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
                tf.createFcItfType("server", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("servers", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION),
                tf.createFcItfType("client", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("clients", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION) });
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
        Fractal.getBindingController(c).bindFc("client", d.getFcInterface("server"));
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
     * This is only one of the possible semantics for the lifecycle controller. For instance, we
     * choose instead, in AOKell, to throw a RuntimeException. Hence the thread is no longer alive.
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
            assertEquals("STOPPED", Fractal.getLifeCycleController(c).getFcState());
        }
    }

    // -------------------------------------------------------------------------
    // Test invalid operations in started state
    // -------------------------------------------------------------------------
    @Test
    @Ignore
    public void testUnbindNotStopped() throws Exception {
        Fractal.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        Fractal.getBindingController(c).bindFc("clients0", d.getFcInterface("server"));
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
}
