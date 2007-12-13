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
package functionalTests.activeobject.request.immediateservice;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;


/**
 * Test immediateService method on an AO
 */
public class Test extends FunctionalTest {
    A a;
    DummyObject dum;
    String name;

    @Before
    public void action() throws Exception {
        a = (A) PAActiveObject.newActive(A.class.getName(), new Object[] { "toto" });
        // getObject is set as an IS in the runActivity of A
        dum = a.getObject();
        PAActiveObject.terminateActiveObject(a, true);
    }

    @org.junit.Test
    public void postConditions() throws Exception {
        if (!PAFuture.isAwaited(dum)) {
            assertTrue(dum.getName().equals("toto"));
            return;
        }

        throw new Exception();
    }
}
