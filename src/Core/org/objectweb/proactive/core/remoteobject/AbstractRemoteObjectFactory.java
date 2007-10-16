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

import java.util.Hashtable;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.ibis.IbisRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmissh.RmiSshRemoteObjectFactory;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * This class provides helper methods for manipulation remote objects.
 *
 */
public abstract class AbstractRemoteObjectFactory {
    protected static ClassServerHelper classServerHelper;
    protected static Hashtable<String, RemoteObjectFactory> activatedRemoteObjectFactories;
    protected static Hashtable<String, Class<?extends RemoteObjectFactory>> remoteObjectFactories;

    static {
        remoteObjectFactories = new Hashtable<String, Class<?extends RemoteObjectFactory>>();
        remoteObjectFactories.put(Constants.RMI_PROTOCOL_IDENTIFIER,
            RmiRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
            HTTPRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.RMISSH_PROTOCOL_IDENTIFIER,
            RmiSshRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.IBIS_PROTOCOL_IDENTIFIER,
            IbisRemoteObjectFactory.class);

        createClassServer();
        activatedRemoteObjectFactories = new Hashtable<String, RemoteObjectFactory>();
    }

    /**
     * insert a new location within the codebase property
     * @param newLocationURL the new location to add
     * @return the new codebase
     */
    protected static synchronized String addCodebase(String newLocationURL) {
        String oldCodebase = System.getProperty("java.rmi.server.codebase");
        String newCodebase = null;
        if (oldCodebase != null) {
            // RMI support multiple class server locations
            newCodebase = oldCodebase + " " + newLocationURL;
        } else {
            newCodebase = newLocationURL;
        }

        System.setProperty("java.rmi.server.codebase", newCodebase);

        return newCodebase;
    }

    /**
     *        create the class server -- mandatory for class file transfer
     */
    protected static synchronized void createClassServer() {
        if (classServerHelper == null) {
            try {
                classServerHelper = new ClassServerHelper();
                String codebase = classServerHelper.initializeClassServer();

                addCodebase(codebase);
            } catch (Exception e) {
                ProActiveLogger.getLogger(Loggers.CLASS_SERVER)
                               .warn("Error with the ClassServer : " +
                    e.getMessage());
            }
        }
    }

    /**
     * @param protocol
     * @return return the remote object factory associated to the given protocol
     * @throws UnknownProtocolException
     */
    public static RemoteObjectFactory getRemoteObjectFactory(String protocol)
        throws UnknownProtocolException {
        try {
            RemoteObjectFactory rof = activatedRemoteObjectFactories.get(protocol);
            if (rof != null) {
                return rof;
            } else {
                Class<?> rofClazz = remoteObjectFactories.get(protocol);

                if (rofClazz != null) {
                    RemoteObjectFactory o = (RemoteObjectFactory) rofClazz.newInstance();

                    activatedRemoteObjectFactories.put(protocol, o);

                    return o;
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        throw new UnknownProtocolException(
            "there is no RemoteObjectFactory defined for the protocol : " +
            protocol);
    }
}
