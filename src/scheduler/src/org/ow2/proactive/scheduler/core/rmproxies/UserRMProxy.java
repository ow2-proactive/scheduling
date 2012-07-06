/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;


public class UserRMProxy {

    private static final Logger logger_dev = ProActiveLogger.getLogger(UserRMProxy.class);

    private final Credentials credentials;

    private final RMProxiesManager proxiesManager;

    private final Map<URI, RMProxyActiveObject> proxyActiveObjects = new LinkedHashMap<URI, RMProxyActiveObject>(
        2);

    public UserRMProxy(RMProxiesManager proxiesManager, Credentials credentials) {
        this.proxiesManager = proxiesManager;
        this.credentials = credentials;
    }

    public NodeSet getNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion, boolean bestEffort)
            throws RMProxyCreationException {

        RMProxiesManager.Connection rmConnection = proxiesManager.getCurrentRMConnection();

        RMProxyActiveObject proxyActiveObject;
        synchronized (proxyActiveObjects) {
            proxyActiveObject = proxyActiveObjects.get(rmConnection.getRmURI());
            if (proxyActiveObject == null) {
                proxyActiveObject = RMProxyActiveObject.createAOProxy(rmConnection.getRmAuthentication(),
                        credentials);
                proxyActiveObjects.put(rmConnection.getRmURI(), proxyActiveObject);
            }
        }

        NodeSet nodeSet = proxyActiveObject.getNodes(number, descriptor, selectionScriptsList, exclusion,
                bestEffort);
        return nodeSet;
    }

    public void releaseNodes(NodeSet nodeSet) {
        releaseNodes(nodeSet, null);
    }

    public void releaseNodes(NodeSet nodeSet, Script<?> cleaningScript) {
        if (nodeSet.size() == 0) {
            if (nodeSet.getExtraNodes() == null || nodeSet.getExtraNodes().size() == 0) {
                throw new IllegalArgumentException("Trying to release empty NodeSet");
            }
        }

        RMProxyActiveObject tragetProxyActiveObject = null;

        synchronized (proxyActiveObjects) {
            if (proxyActiveObjects.size() > 1) {
                for (Iterator<RMProxyActiveObject> i = proxyActiveObjects.values().iterator(); i.hasNext();) {
                    RMProxyActiveObject proxyActiveObject = i.next();
                    boolean useThisRM;
                    try {
                        useThisRM = proxyActiveObject.isNodeSetForThisRM(nodeSet);
                    } catch (Exception e) {
                        logger_dev.warn("RM call failed with exception", e);
                        try {
                            proxyActiveObject.isActive().getBooleanValue();
                        } catch (Exception activeCheckError) {
                            logger_dev.warn("RM isn't active, remove it from the list", e);
                            i.remove();
                        }
                        continue;
                    }
                    if (useThisRM) {
                        tragetProxyActiveObject = proxyActiveObject;
                        break;
                    }
                }
            } else {
                if (!proxyActiveObjects.isEmpty()) {
                    tragetProxyActiveObject = proxyActiveObjects.values().iterator().next();
                }
            }
        }

        if (tragetProxyActiveObject != null) {
            tragetProxyActiveObject.releaseNodes(nodeSet, cleaningScript);
        } else {
            logger_dev.warn("Didn't find RM to release NodeSet (RM is down or all NodeSet's Nodes are down)");
        }
    }

    void terminate() {
        synchronized (proxyActiveObjects) {
            for (RMProxyActiveObject proxyActiveObject : proxyActiveObjects.values()) {
                proxyActiveObject.terminateProxy();
            }
            proxyActiveObjects.clear();
        }
    }

}
