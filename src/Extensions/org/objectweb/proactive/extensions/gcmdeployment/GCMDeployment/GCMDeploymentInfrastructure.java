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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;


/**
 *
 * TODO (long term) Transform this class into a real tree.
 *         This implementation duplicates Bridge and Tree like in the previous
 *  ProActive deployment framework.
 *
 * TODO Allow to start a command on intermediate Bridges
 *
 */
public class GCMDeploymentInfrastructure {
    private Map<String, Group> groups;
    private Map<String, Bridge> bridges;
    private Map<String, HostInfo> hosts;

    public GCMDeploymentInfrastructure() {
        groups = new HashMap<String, Group>();
        bridges = new HashMap<String, Bridge>();
        hosts = new HashMap<String, HostInfo>();
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Map<String, Bridge> getBridges() {
        return bridges;
    }

    public Map<String, HostInfo> getHosts() {
        return hosts;
    }

    public void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void addBrige(Bridge bridge) {
        bridges.put(bridge.getId(), bridge);
    }

    public void addHost(HostInfo host) {
        hosts.put(host.getId(), host);
    }
}
