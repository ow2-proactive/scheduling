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
package functionalTests.remoteobject.registry;

import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteObjectProtocolFactoryRegistry;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


/**
 * Test for the remote object protocol registry
 * registration and deletion of a 'dummy' protocol
 * @author acontes
 *
 */
public class RemoteObjectProtocolRegistryTest {
    @org.junit.Test
    public void addRemoveAProtocol() throws Exception {
        RemoteObjectProtocolFactoryRegistry.put("dummy", DummyProtocol.class);

        Class<? extends RemoteObjectFactory> dummyFactoryClass = RemoteObjectProtocolFactoryRegistry
                .get("dummy");

        assertNotNull(dummyFactoryClass);

        RemoteObjectFactory dummyFactory = RemoteObjectHelper.getRemoteObjectFactory("dummy");

        assertTrue(dummyFactory instanceof DummyProtocol);

        RemoteObjectProtocolFactoryRegistry.remove("dummy");

        dummyFactoryClass = RemoteObjectProtocolFactoryRegistry.get("dummy");

        assertNull(dummyFactoryClass);
    }
}
