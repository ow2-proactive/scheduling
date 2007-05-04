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
package functionalTests.node.localnode;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
/**
 * @author Alexandre di Costanzo
 *
 */
public class Test {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1626919410261919710L;
	private A ao;

   
    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Before
	public void initTest() throws Exception {
        this.ao = (A) ProActive.newActive(A.class.getName(),
                new Object[] { "bernard Lavilliers" });
        
       
    }

   @org.junit.Test
	public void action() throws Exception {
        Node aoNode = this.ao.getMyNode();
        aoNode.setProperty("test", "bernard Lavilliers");
        
        assertTrue(aoNode.getProperty("test").compareTo("bernard Lavilliers") == 0);

        ProActive.terminateActiveObject(this.ao, false);
    }
}
