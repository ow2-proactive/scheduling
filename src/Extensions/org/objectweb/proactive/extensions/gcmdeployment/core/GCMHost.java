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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.VMInformation;


@PublicAPI
public class GCMHost implements Serializable {
    protected String hostname;
    protected Map<VMInformation, GCMRuntime> runtimes;

    public GCMHost(String hostname, Set<Node> nodes) {
        super();
        this.hostname = hostname;
        this.runtimes = new HashMap<VMInformation, GCMRuntime>();

        update(nodes);
    }

    static private Map<VMInformation, Set<Node>> groupByRutnime(Set<Node> nodes) {
        Map<VMInformation, Set<Node>> ret = new HashMap<VMInformation, Set<Node>>();
        for (Node node : nodes) {
            VMInformation vmi = node.getVMInformation();
            if (ret.get(vmi) == null) {
                ret.put(vmi, new HashSet<Node>());
            }
            Set<Node> nodeSet = ret.get(vmi);
            nodeSet.add(node);
        }
        return ret;
    }

    public String getHostname() {
        return hostname;
    }

    public List<GCMRuntime> getRuntimes() {
        return new ArrayList<GCMRuntime>(runtimes.values());
    }

    public void update(Set<Node> nodes) {
        Map<VMInformation, Set<Node>> byRuntime = groupByRutnime(nodes);
        for (VMInformation vmi : byRuntime.keySet()) {
            if (runtimes.containsKey(vmi)) {
                runtimes.get(vmi).update(byRuntime.get(vmi));
            } else {
                GCMRuntime runtime = new GCMRuntime(vmi, byRuntime.get(vmi));
                runtimes.put(vmi, runtime);
            }
        }
    }
}
