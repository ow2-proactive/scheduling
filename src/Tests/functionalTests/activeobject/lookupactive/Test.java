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
package functionalTests.activeobject.lookupactive;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.UrlBuilder;

import functionalTests.Helper;

import testsuite.test.Assertions;
/**
 * Test register and lookup AOs
 */
public class Test {
	private static final long serialVersionUID = -6695388796855172951L;

	@org.junit.Test
	public void action() throws Exception {
        A a = (A) ProActive.newActive(A.class.getName(), new Object[] { "toto" });
        a.register();

        // check lookup works
        String url = UrlBuilder.buildUrlFromProperties("localhost", "A");
        a = (A) ProActive.lookupActive(A.class.getName(), url);
        
        assertTrue(a != null);
        assertEquals(a.getName(), "toto");

        // check listActive contains the previous lookup
        String host = UrlBuilder.buildUrlFromProperties("localhost", "");
        String[] registered = ProActive.listActive(host);
        Assertions.assertNotNull(registered);

        for (int i = 0; i < registered.length; i++) {
            if (registered[i].substring(registered[i].lastIndexOf('/'))
                                 .equals("/A")) {
                return;
            }
        }

        throw new Exception("Could not find registered object in list of objects");

    }
	
	 @After
	  public void after() {
		  Helper.killJVMs();
	  }
}
