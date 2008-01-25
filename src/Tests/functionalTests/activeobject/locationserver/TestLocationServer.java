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

import junit.framework.Assert;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * Test migration with location server
 */
@GCMDeploymentReady
public class TestLocationServer extends FunctionalTestDefaultNodes {
    A a;
    MigratableA migratableA;
    SimpleLocationServer server;
    UniqueID idA;

    public TestLocationServer() {
        super(DeploymentType._1x1);
    }

    @org.junit.Test
    public void action() throws Exception {
        String serverUrl = PAProperties.PA_LOCATION_SERVER_RMI.getValue();

        this.server = (SimpleLocationServer) PAActiveObject.newActive(SimpleLocationServer.class.getName(),
                new Object[] { serverUrl });

        Thread.sleep(3000);

        this.a = (A) PAActiveObject.newActive(A.class.getName(), null, new Object[] { "toto" }, null, null,
                LocationServerMetaObjectFactory.newInstance());

        this.migratableA = (MigratableA) PAActiveObject.newActive(MigratableA.class.getName(), null,
                new Object[] { "toto" }, null, null, LocationServerMetaObjectFactory.newInstance());

        this.idA = ((BodyProxy) ((StubObject) this.a).getProxy()).getBodyID();

        Node node = super.getANode();
        this.migratableA.moveTo(node);

        Thread.sleep(3000);

        Assert.assertNotNull(this.server.searchObject(this.idA));
        Assert.assertEquals("toto", this.a.getName(this.migratableA));
    }
}
