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
package unitTests.calcium.stateness;

import static org.junit.Assert.*;
import org.junit.Test;
import org.objectweb.proactive.extensions.calcium.stateness.StateFul;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;


public class TestStateNess {
    @Test
    public void stateFulObject() {
        boolean res = Stateness.isStateFul(new A());
        assertTrue(res);
    }

    @Test
    public void stateLessObject() {
        B b = new B();
        boolean res = Stateness.isStateFul(b);

        assertFalse(res);
    }

    @Test
    public void stateFulAnnotatedObject() {
        boolean res = Stateness.isStateFul(new C());
        assertTrue(res);
    }

    @Test
    public void stateLessAnnotatedObject() {
        boolean res = Stateness.isStateFul(new D());
        assertFalse(res);
    }

    @Test
    public void stateFulSuperClass() {
        boolean res = Stateness.isStateFul(new A_sub());
        assertTrue(res);
    }

    //stateful
    static class A {
        Object o;
    }

    //stateless
    static class B {
    }

    //stateful
    @StateFul(value = true)
    static class C {
    }

    //stateless
    @StateFul(value = false)
    static class D {
        Object o;
    }

    //stateful
    static class A_sub extends A {
    }
}
