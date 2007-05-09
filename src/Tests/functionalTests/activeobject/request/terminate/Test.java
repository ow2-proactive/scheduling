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
package functionalTests.activeobject.request.terminate;

import org.junit.Before;
import org.objectweb.proactive.ProActive;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.request.A;


/**
 * Test sending termination method
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = 9207669520580816164L;
    A a;

    @Before
    public void action() throws Exception {
        a = (A) ProActive.newActive(A.class.getName(), new Object[0]);
        a.method1();
        a.exit();
        //  Thread.sleep(5000);
    }

    @org.junit.Test(expected = Exception.class)
    public void postConditions() throws Exception {
        a.method1();
    }
}
