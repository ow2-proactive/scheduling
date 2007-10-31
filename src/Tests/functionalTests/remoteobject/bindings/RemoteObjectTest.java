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
package functionalTests.remoteobject.bindings;

import java.net.URI;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test for the remote objects, registration of a remote object with several protocols and lookup
 */
public class RemoteObjectTest extends FunctionalTest {
    private static final long serialVersionUID = -6695388796855172951L;

    @org.junit.Test
    public void register() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object
        RemoteObjectExposer roe = new RemoteObjectExposer(ProActiveRuntime.class.getName(),
                p);

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntime");
        InternalRemoteRemoteObject irro = roe.activateProtocol(uri);

        // looking for the remote object
        RemoteObject ro = RemoteObjectHelper.lookup(uri);

        ProActiveRuntime p1 = (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(ro);

        assertTrue(p.getURL().equals(p1.getURL()));
    }

    @org.junit.Test
    public void multibind() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object
        RemoteObjectExposer roe = new RemoteObjectExposer(ProActiveRuntime.class.getName(),
                p);

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntime");

        InternalRemoteRemoteObject irro = roe.activateProtocol(uri);

        // looking for the remote object
        RemoteObject ro = RemoteObjectHelper.lookup(uri);

        ProActiveRuntime p1 = (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(ro);

        // second binding
        URI uri2 = RemoteObjectHelper.generateUrl(Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
                "myruntime");

        InternalRemoteRemoteObject irro2 = roe.activateProtocol(uri2);

        // looking for the remote object
        RemoteObject ro2 = RemoteObjectHelper.lookup(uri2);

        ProActiveRuntime p2 = (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(ro2);

        assertTrue(p2.getURL().equals(p1.getURL()));
    }
}
