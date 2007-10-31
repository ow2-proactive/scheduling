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

import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;


public class DummyProtocol implements RemoteObjectFactory {
    @Override
    public int getPort() {
        return 452;
    }

    @Override
    public String getProtocolId() {
        return "dummy";
    }

    @Override
    public URI[] list(URI url) throws ProActiveException {
        return new URI[] {  };
    }

    @Override
    public RemoteObject lookup(URI url) throws ProActiveException {
        return null;
    }

    @Override
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target)
        throws ProActiveException {
        return null;
    }

    @Override
    public RemoteRemoteObject register(InternalRemoteRemoteObject target,
        URI url, boolean replacePreviousBinding) throws ProActiveException {
        return null;
    }

    @Override
    public void unregister(URI url) throws ProActiveException {
    }
}
