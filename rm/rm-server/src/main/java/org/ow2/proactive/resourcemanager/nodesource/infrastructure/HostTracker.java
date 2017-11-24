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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * This class tracks the information related to node deployment on a host.
 *
 * This class is not thread safe as its methods are only called within
 * a lock acquired sections.
 */
public class HostTracker implements Serializable {

    protected static final Logger logger = Logger.getLogger(HostTracker.class);

    private InetAddress host;

    private int configuredNodeNumber;

    private boolean needsNodes;

    private Set<String> aliveNodeUrls;

    private Set<String> downNodeUrls;

    private Set<String> removedNodeUrls;

    HostTracker(InetAddress host, int configuredNodeNumber) {
        this.host = host;
        this.configuredNodeNumber = configuredNodeNumber;
        this.needsNodes = true;
        aliveNodeUrls = new HashSet<>();
        downNodeUrls = new HashSet<>();
        removedNodeUrls = new HashSet<>();
    }

    boolean needsNodes() {
        return needsNodes;
    }

    void setNeedsNodes(boolean needsNodes) {
        this.needsNodes = needsNodes;
    }

    int getNeededNodeNumber() {
        return configuredNodeNumber - aliveNodeUrls.size() - removedNodeUrls.size();
    }

    boolean hasAliveNodes() {
        return !aliveNodeUrls.isEmpty();
    }

    boolean managesNodeUrl(String nodeUrl) {
        boolean nodeUrlFound = false;
        if (aliveNodeUrls.contains(nodeUrl)) {
            logger.info("Node " + nodeUrl + " has been found in alive nodes");
            nodeUrlFound = true;
        }
        if (downNodeUrls.contains(nodeUrl)) {
            logger.info("Node " + nodeUrl + " has been found in down nodes");
            nodeUrlFound = true;
        }
        if (removedNodeUrls.contains(nodeUrl)) {
            logger.info("Node " + nodeUrl + " has been found in removed nodes");
            nodeUrlFound = true;
        }
        return nodeUrlFound;
    }

    void putAliveNodeUrl(String aliveNodeUrl) {
        if (!aliveNodeUrls.contains(aliveNodeUrl)) {
            aliveNodeUrls.add(aliveNodeUrl);
            removedNodeUrls.remove(aliveNodeUrl);
            downNodeUrls.remove(aliveNodeUrl);
            logger.info("Alive node: " + this.toString());
        }
    }

    void putRemovedNodeUrl(String removedNodeUrl) {
        if (!removedNodeUrls.contains(removedNodeUrl)) {
            removedNodeUrls.add(removedNodeUrl);
            aliveNodeUrls.remove(removedNodeUrl);
            downNodeUrls.remove(removedNodeUrl);
            logger.info("Removed node: " + this.toString());
        }
    }

    void putDownNodeUrl(String downNodeUrl) {
        if (!downNodeUrls.contains(downNodeUrl)) {
            downNodeUrls.add(downNodeUrl);
            aliveNodeUrls.remove(downNodeUrl);
            removedNodeUrls.remove(downNodeUrl);
            logger.info("Down node: " + this.toString());
        }
    }

    @Override
    public String toString() {
        return "Host " + host.getHostAddress() + ". Configured with " + configuredNodeNumber +
               " nodes and with current state: [alive node number=" + aliveNodeUrls.size() + ", removed node number=" +
               removedNodeUrls.size() + ", down node number=" + downNodeUrls.size() + "]";
    }

}
