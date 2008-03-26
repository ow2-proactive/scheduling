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
package org.objectweb.proactive.core.remoteobject.http.util;

import java.util.HashMap;

import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An HTTP Registry that registers Bodies
 * @author The ProActive Team
 *
 */
public class HTTPRegistry {
    private static final String REGISTRY_NAME = "HTTP_REGISTRY";
    private static HTTPRegistry instance;
    private static HashMap<String, InternalRemoteRemoteObject> rRemteObjectMap = new HashMap<String, InternalRemoteRemoteObject>();

    private HTTPRegistry() {
    }

    /**
     * Gets the unique instance of the registry
     * @return the unique instance of the registry
     */
    public static synchronized HTTPRegistry getInstance() {
        if (instance == null) {
            instance = new HTTPRegistry();
        }
        return instance;
    }

    /**
     * Binds a body  with a name
     * @param name  the name of the body
     * @param body the body to be binded
     */
    public void bind(String name, InternalRemoteRemoteObject body) {
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).debug("registering remote object at " + name);
        rRemteObjectMap.put(name, body);
    }

    /**
     * Unbinds a body from a  name
     * @param name the name binded with a body
     */
    public void unbind(String name) {
        rRemteObjectMap.remove(name);
    }

    /**
     * Gives all the names registered in this registry
     * @return the names list
     */
    public String[] list() {
        String[] list = new String[rRemteObjectMap.size()];
        rRemteObjectMap.keySet().toArray(list);
        return list;
    }

    /**
     * Retrieves a body from a name
     * @param name The name of the body to be retrieved
     * @return the binded body
     */
    public InternalRemoteRemoteObject lookup(String name) {
        return rRemteObjectMap.get(name);
    }
}
