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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.HostInfo;


public class GCMDeploymentResources {
    private List<Group> groups = Collections.synchronizedList(new ArrayList<Group>());
    private List<Bridge> bridges = Collections.synchronizedList(new ArrayList<Bridge>());
    private HostInfo hostInfo;

    public List<Group> getGroups() {
        return groups;
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void addBridge(Bridge bridge) {
        bridges.add(bridge);
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    protected void setHostInfo(HostInfo hostInfo) {
        assert (this.hostInfo == null);
        this.hostInfo = hostInfo;
    }

    public void check() throws IllegalStateException {
        for (Group group : groups)
            group.check();

        for (Bridge bridge : bridges)
            bridge.check();

        hostInfo.check();
    }
}
