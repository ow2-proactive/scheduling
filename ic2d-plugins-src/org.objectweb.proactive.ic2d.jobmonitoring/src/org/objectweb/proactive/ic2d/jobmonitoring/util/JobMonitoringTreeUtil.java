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
package org.objectweb.proactive.ic2d.jobmonitoring.util;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VirtualNodeObject;


/**
 * Provides utility methods that can be used to build a job monitoring tree.
 * That is to say a tree like this :
 * <pre>
 * VN (job)
 *  |_ Host
 *       |_ JVM (job)
 *           |_ Node (job)
 *                |_ AO (job)
 * </pre>
 *
 */
public class JobMonitoringTreeUtil {

    /**
     * Returns the children (hosts) of the specified virtual node (which is a child
     * of the specified job).
     * A host will be in the list returned if at least one of its children
     * (which is also a child of the specified virtual node) as the same jobID
     * as the job specified.
     * @param vn the virtual node which one wants the children
     * @return Children of the virtual node.
     */
    public static List<HostObject> getVNChildren(VirtualNodeObject vn) {
        List<HostObject> result = new ArrayList<HostObject>();
        List<AbstractData> worldChildren = vn.getWorldObject()
                                             .getMonitoredChildrenAsList();
        for (AbstractData obj : worldChildren) {
            HostObject host = (HostObject) obj;
            if (!getHostChildren(host, vn).isEmpty()) {
                result.add(host);
            }
        }

        /*System.out.println("JobMonitoringTreeUtil.getVNChildren()");
        for(HostObject host : result)
                System.out.print(host.getFullName()+", ");
        System.out.println();*/
        return result;
    }

    /**
     * Returns the children (JVM) of the specified host.
     * A JVM will be in the list returned if :
     * <ul>
     *   <li>the specified host is its parent
     *   <li>the JVM has in his hierarchy at least one child (node) whose parent is the specified virtual node
     * </ul>
     * @param host the host which one wants the children
     * @param vn the virtual node parent of the host
     * @return Children of the host.
     */
    public static List<RuntimeObject> getHostChildren(HostObject host,
        VirtualNodeObject vn) {
        List<RuntimeObject> result = new ArrayList<RuntimeObject>();
        List<AbstractData> hostChildren = host.getMonitoredChildrenAsList();
        for (AbstractData obj : hostChildren) {
            RuntimeObject jvm = (RuntimeObject) obj;
            if ((!getJVMChildren(jvm, vn).isEmpty())) {
                result.add(jvm);
            }
        }

        /*System.out.println("JobMonitoringTreeUtil.getHostChildren()");
        for(VMObject jvm : result)
                System.out.print(jvm.getFullName()+", ");
        System.out.println();*/
        return result;
    }

    /**
     * Returns the children (nodes) of the specified JVM.
     * A node will be in the list returned if :
     * <ul>
     *   <li>the specified JVM is its parent
     *   <li>the specified virtual node is its parent
     * </ul>
     * @param jvm the JVM which one wants the children
     * @param vn the virtual node parent of the JVM
     * @return Children of the JVM
     */
    public static List<NodeObject> getJVMChildren(RuntimeObject jvm,
        VirtualNodeObject vn) {
        List<NodeObject> result = new ArrayList<NodeObject>();
        List<AbstractData> jvmChildren = jvm.getMonitoredChildrenAsList();
        for (AbstractData obj : jvmChildren) {
            NodeObject node = (NodeObject) obj;
            if (node.getVirtualNode().equals(vn)) {
                result.add(node);
            }
        }
        return result;
    }
}
