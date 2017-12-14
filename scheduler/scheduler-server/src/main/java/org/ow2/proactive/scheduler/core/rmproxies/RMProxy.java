/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


/**
 *
 * This class represents the proxy to the resource manager limiting the
 * interface of ResourceManager to only few methods. It's also capable to
 * reconnect to the RM if connection is lost.
 *
 */
public class RMProxy {
    private static final Logger logger = Logger.getLogger(RMProxy.class);

    private RMProxiesManager.Connection currentRMConnection;

    private RMProxyActiveObject proxyActiveObject;

    private URI rmURL;

    private Credentials creds;

    RMProxy(URI rmURL, Credentials creds) throws RMException, RMProxyCreationException {
        this.rmURL = rmURL;
        this.creds = creds;
        init();
    }

    public synchronized void init() throws RMException, RMProxyCreationException {
        RMAuthentication auth = RMConnection.join(rmURL.toString());
        proxyActiveObject = RMProxyActiveObject.createAOProxy(auth, creds);
        currentRMConnection = new RMProxiesManager.Connection(rmURL, auth);
    }

    public synchronized void terminate() {
        if (proxyActiveObject != null) {
            try {
                proxyActiveObject.disconnect();
                proxyActiveObject.terminateProxy();
                proxyActiveObject = null;
            } catch (RuntimeException e) {
                logger.warn("Cannot properly shutdown rm proxy ", e);
            }
        }
    }

    public BooleanWrapper isActive() {
        if (proxyActiveObject == null) {
            return new BooleanWrapper(false);
        }
        return proxyActiveObject.isActive();
    }

    public RMState getState() {
        if (proxyActiveObject == null) {
            throw new RuntimeException("Proxy is not initialized");
        }
        return proxyActiveObject.getState();
    }

    public void rebind(URI rmURI) throws RMException, RMProxyCreationException {

        if (rmURI.equals(this.rmURL) && proxyActiveObject != null && proxyActiveObject.isActive().getBooleanValue()) {
            // nothing to do
            logger.info("Do not reconnect to the RM as connection is active for " + rmURL);
            return;
        }

        if (!this.rmURL.equals(rmURI)) {
            logger.info("binding to the new RM " + rmURL);
        }

        this.rmURL = rmURI;
        terminate();
        init();
    }

    public NodeSet getNodes(Criteria criteria) throws RMProxyCreationException {
        NodeSet nodeSet = proxyActiveObject.getNodes(criteria);
        return nodeSet;
    }

    public void releaseNodes(NodeSet nodeSet) {
        releaseNodes(nodeSet, null, null, null, null, null);
    }

    public void releaseNodes(NodeSet nodeSet, Script<?> cleaningScript, Credentials creds) {
        releaseNodes(nodeSet, cleaningScript, null, null, null, creds);
    }

    public void releaseNodes(NodeSet nodeSet, Script<?> cleaningScript, VariablesMap variables,
            Map<String, String> genericInformation, TaskId taskId, Credentials creds) {

        if (nodeSet.size() == 0) {
            if (nodeSet.getExtraNodes() == null || nodeSet.getExtraNodes().size() == 0) {
                throw new IllegalArgumentException("Trying to release empty NodeSet");
            }
        }

        if (proxyActiveObject != null) {
            proxyActiveObject.releaseNodes(nodeSet, cleaningScript, variables, genericInformation, taskId, creds);
        } else {
            logger.warn("Didn't find RM to release NodeSet (RM is down or all NodeSet's Nodes are down)");
        }
    }
}
