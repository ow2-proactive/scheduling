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
package org.objectweb.proactive.ic2d.jmxmonitoring.finder;

import java.net.URI;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;


public class RemoteObjectHostRTFinder implements RuntimeFinder {
    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.ic2d.jmxmonitoring.finder.RuntimeFinder#getRuntimeObjects(HostObject)
     */
    public Collection<RuntimeObject> getRuntimeObjects(HostObject host) {
        String hostUrl = host.getUrl();

        Map<String, RuntimeObject> runtimeObjects = new HashMap<String, RuntimeObject>();

        Console console = Console.getInstance(Activator.CONSOLE_NAME);
        console.log("Exploring " + host + " with RMI on port " +
            host.getPort());

        URI[] uris = null;
        try {
            URI target = URIBuilder.buildURI(host.getHostName(), null,
                    host.getProtocol(), host.getPort());
            try {
                uris = RemoteObjectHelper.getRemoteObjectFactory(host.getProtocol())
                                         .list(target);
            } catch (ProActiveException e) {
                if (e.getCause() instanceof ConnectException) {
                    Console.getInstance(Activator.CONSOLE_NAME)
                           .err("Connection refused to " + host);
                    return runtimeObjects.values();
                } else {
                    throw e;
                }
            }

            if (uris != null) {

                /* Searchs all ProActive Runtimes */
                for (int i = 0; i < uris.length; ++i) {
                    URI url = uris[i];
                    try {

                        /*RemoteObject ro = RemoteObjectFactory.getRemoteObjectFactory(host.getProtocol()).lookup(url);*/
                        RemoteObject ro = RemoteObjectHelper.lookup(url);

                        /*Object stub = ro.getObjectProxy();*/
                        Object stub = RemoteObjectHelper.generatedObjectStub(ro);

                        if (stub instanceof ProActiveRuntime) {
                            ProActiveRuntime proActiveRuntime = (ProActiveRuntime) stub;

                            String mbeanServerName = proActiveRuntime.getMBeanServerName();

                            String runtimeUrl = proActiveRuntime.getURL();
                            runtimeUrl = FactoryName.getCompleteUrl(runtimeUrl);

                            ObjectName oname = FactoryName.createRuntimeObjectName(runtimeUrl);

                            if (runtimeObjects.containsKey(runtimeUrl)) {
                                continue;
                            }

                            RuntimeObject runtime = (RuntimeObject) host.getChild(runtimeUrl);
                            if (runtime == null) {
                                // This runtime is not yet monitored
                                runtime = new RuntimeObject(host, runtimeUrl,
                                        oname, hostUrl, mbeanServerName);
                            }
                            runtimeObjects.put(runtimeUrl, runtime);
                        }
                    } catch (ProActiveException pae) {
                        // the lookup returned an active object, and an active object is
                        // not a remote object (for now...)
                        pae.printStackTrace();
                        console.warn("Found active object in registry at " +
                            url);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof ConnectException ||
                    e instanceof ConnectIOException) {
                console.debug(e);
            } else {
                console.logException(e);
            }
        }
        return runtimeObjects.values();
    }
}
