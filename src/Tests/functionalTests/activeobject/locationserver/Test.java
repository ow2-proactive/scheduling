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
package functionalTests.activeobject.locationserver;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;
import static junit.framework.Assert.assertTrue;

/**
 * Test migration with location server
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = 7390234087440842136L;
    A a;
    MigratableA migratableA;
    SimpleLocationServer server;
    UniqueID idA;

    @Before
    public void Before() throws Exception {
        new TestNodes().action();
    }

    @org.junit.Test
    public void action() throws Exception {
        String serverUrl = PAProperties.PA_LOCATION_SERVER_RMI.getValue();

        this.server = (SimpleLocationServer) PAActiveObject.newActive(SimpleLocationServer.class.getName(),
                new Object[] { serverUrl });

        Thread.sleep(3000);

        this.a = (A) PAActiveObject.newActive(A.class.getName(), null,
                new Object[] { "toto" }, TestNodes.getSameVMNode(), null,
                LocationServerMetaObjectFactory.newInstance());

        this.migratableA = (MigratableA) PAActiveObject.newActive(MigratableA.class.getName(),
                null, new Object[] { "toto" }, TestNodes.getSameVMNode(), null,
                LocationServerMetaObjectFactory.newInstance());

        this.idA = ((BodyProxy) ((StubObject) this.a).getProxy()).getBodyID();

        this.migratableA.moveTo(TestNodes.getLocalVMNode());

        Thread.sleep(3000);

        assertTrue(this.server.searchObject(this.idA) != null);
        assertTrue(this.a.getName(this.migratableA).equals("toto"));
    }
}
