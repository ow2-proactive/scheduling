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
package org.objectweb.proactive.extra.infrastructuremanager.common;

import java.io.Serializable;

import org.objectweb.proactive.extra.scheduler.job.IdentifyJob;


public class IMNodeEvent implements Serializable {
    private static final long serialVersionUID = -7781655355601704944L;
    private String nodeUrl = null;
    private String nodeSource = null;
    private String PADName = null;
    private String VnName = null;
    private String hostName = null;
    private String VMName = null;
    private NodeState nodeState;

    public IMNodeEvent() {
    }

    public IMNodeEvent(String url, String nodeSource, String PADName,
        String VnName, String hostname, String vm, NodeState state) {
        this.nodeUrl = url;
        this.nodeSource = nodeSource;
        this.PADName = PADName;
        this.VnName = VnName;
        this.hostName = hostname;
        this.VnName = vm;
        nodeState = state;
    }

    public boolean equals(Object obj) {
        if (obj instanceof IMNodeEvent) {
            return ((IMNodeEvent) obj).nodeUrl.equals(this.nodeUrl);
        }
        return false;
    }

    public String getNodeUrl() {
        return this.nodeUrl;
    }

    public String getNodeSource() {
        return this.nodeSource;
    }

    public String getPADName() {
        return this.PADName;
    }

    public String getVnName() {
        return this.VnName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getVMName() {
        return this.VMName;
    }

    public NodeState getState() {
        return this.nodeState;
    }
}
