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
package org.objectweb.proactive.core.remoteobject;

import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.ibis.IbisRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmissh.RmiSshRemoteObjectFactory;


public class RemoteObjectProtocolFactoryRegistry {
    protected static Hashtable<String, Class<?extends RemoteObjectFactory>> remoteObjectFactories;

    static {
        // set the default supported protocols
        remoteObjectFactories = new Hashtable<String, Class<?extends RemoteObjectFactory>>();
        remoteObjectFactories.put(Constants.RMI_PROTOCOL_IDENTIFIER,
            RmiRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
            HTTPRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.RMISSH_PROTOCOL_IDENTIFIER,
            RmiSshRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.IBIS_PROTOCOL_IDENTIFIER,
            IbisRemoteObjectFactory.class);
    }

    public static void put(String protocol,
        Class<?extends RemoteObjectFactory> factory) {
        remoteObjectFactories.put(protocol, factory);
    }

    public static void remove(String protocol) {
        remoteObjectFactories.remove(protocol);
    }

    public static Class<?extends RemoteObjectFactory> get(String protocol) {
        return remoteObjectFactories.get(protocol);
    }

    public static Enumeration<String> keys() {
        return remoteObjectFactories.keys();
    }
}
