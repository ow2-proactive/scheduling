/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.table;

import org.ow2.proactive.resourcemanager.common.NodeState;


public class NodeTableItem {

    private String nodeSource;
    private String host;
    private NodeState nodeState;
    private String nodeUrl;

    public NodeTableItem(String ns, String host, NodeState st, String url) {
        this.nodeSource = ns;
        this.host = host;
        this.nodeState = st;
        this.nodeUrl = url;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public NodeState getState() {
        return nodeState;
    }

    public void setState(NodeState state) {
        this.nodeState = state;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o instanceof NodeTableItem && this.nodeUrl.equals(((NodeTableItem) o).getNodeUrl())) {
            return true;
        } else
            return false;
    }
}
