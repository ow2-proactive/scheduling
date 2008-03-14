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

import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.ItfWithStream;
import functionalTests.component.conform.components.ItfWithStreamError;
import functionalTests.component.conform.components.ItfWithStreamInherited;
import functionalTests.component.conform.components.ItfWithStreamInheritedError;
import functionalTests.component.conform.components.StreamImpl;


public class TestStream extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected InterfaceType it;

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which not extend StreamInterface
    // -------------------------------------------------------------------------
    @Test
    public void testNoStreamItf() throws Exception {
        it = tf.createFcItfType("server", I.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItf() throws Exception {
        it = tf.createFcItfType("server", ItfWithStream.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface by inheritance
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItfInherited() throws Exception {
        it = tf.createFcItfType("server", ItfWithStreamInherited.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface with error
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItfError() throws Exception {
        try {
            it = tf.createFcItfType("server", ItfWithStreamError.class.getName(), false, false, false);
            fail();
        } catch (InstantiationException e) {
        }
    }

    // -----------------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface by inheritance with error
    // -----------------------------------------------------------------------------------
    @Test
    public void testStreamItfInheritedError() throws Exception {
        try {
            it = tf
                    .createFcItfType("server", ItfWithStreamInheritedError.class.getName(), false, false,
                            false);
            fail();
        } catch (InstantiationException e) {
        }
    }

    // -----------------------------------------------------------------------------------
    // Full test
    // -----------------------------------------------------------------------------------
    @Test
    public void testExecStreamItf() throws Exception {
        try {
            ComponentType t = tf.createFcType(new InterfaceType[] { tf.createFcItfType("server",
                    ItfWithStream.class.getName(), false, false, false) });
            Component c = gf.newFcInstance(t, parametricPrimitive, StreamImpl.class.getName());
            Fractal.getLifeCycleController(c).startFc();
            ItfWithStream iws = (ItfWithStream) c.getFcInterface("server");
            iws.hello();
            iws.hello("world");
        } catch (InstantiationException e) {
        }
    }
}
