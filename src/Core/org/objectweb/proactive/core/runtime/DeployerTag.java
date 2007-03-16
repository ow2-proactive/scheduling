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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.runtime;


/**
 * DeployerTag allows applications to utilize topographic informations
 * to speed up calculations.
 *
 * Deployment descriptors describe how to acquire ressources. This, static, information
 * can be used to organize Nodes in groups. Two Nodes in the same group are likely close
 * to each other. An application, by using smartly DeployerTag, can improve its
 * performances by bringing together communicating active objects.
 *
 *  A DeployerTag object contains two informations. The first one is who created this node.
 *  The second one is two which group this node contains. It is ensured that tupples <vmid, group>
 *  are uniques.
 */
public class DeployerTag implements java.io.Serializable {
    // An unique identifier describing the VM from which this node was deployed
    private String vmid;

    // The group to which this node is belonging. Groups are unique _per VM_ 
    private int myGroup;

    public DeployerTag() {
        vmid = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation()
                                   .getVMID().toString();
        myGroup = -1;
    }

    public DeployerTag(String _str) {
        String[] sa = _str.split("~");
        assert (sa.length == 2);

        vmid = sa[0];
        myGroup = Integer.parseInt(sa[1]);
    }

    @Override
    public boolean equals(Object _gi) {
        if (this == _gi) {
            return true;
        }

        if (!(_gi instanceof DeployerTag)) {
            return false;
        }

        DeployerTag gi = (DeployerTag) _gi;
        return gi.vmid.equals(vmid) && (gi.myGroup == myGroup);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return vmid + "~" + myGroup;
    }

    /**
     * Returns the VMID of the runtime which deployed this node
     * @return the VMID of the runtime which deployed this node
     */
    public String getVMID() {
        return vmid;
    }

    /**
     * Returns the group of this node
     * @return the group of this node
     */
    public int getGroup() {
        return myGroup;
    }
}
