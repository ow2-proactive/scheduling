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
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.request.A;
import static junit.framework.Assert.assertTrue;

/**
 * Test sending termination method
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = 9207669520580816164L;
    A a1;
    A a2;
    StringWrapper returnedValue;

    @Before
    public void action() throws Exception {
        a1 = (A) ProActiveObject.newActive(A.class.getName(), new Object[0]);
        a1.method1();
        a1.exit();

        // test with remaining ACs
        a2 = (A) ProActiveObject.newActive(A.class.getName(), new Object[0]);
        a2.initDeleguate();
        returnedValue = a2.getDelegateValue();
        a2.exit();
    }

    @org.junit.Test
    public void postConditions() {
        assertTrue(returnedValue.stringValue().equals("Returned value"));
        int exceptionCounter = 0;
        try {
            a1.method1();
        } catch (RuntimeException e) {
            exceptionCounter++;
        }
        try {
            a2.method1();
        } catch (RuntimeException e) {
            exceptionCounter++;
        }
        assertTrue(exceptionCounter == 2);
    }
}
