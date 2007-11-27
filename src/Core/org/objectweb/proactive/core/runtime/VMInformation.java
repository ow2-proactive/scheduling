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
package org.objectweb.proactive.core.runtime;


/**
 * <p>
 * A class implementing this interface provides information about the VM it is attached to.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface VMInformation extends java.io.Serializable {

    /**
     * Returns the VMID of the VM the node belongs to
     * @return the VMID of the VM the node belongs to
     */
    public java.rmi.dgc.VMID getVMID();

    /**
     * Returns the InetAddress of the host where is the VM located
     * @return the InetAddress of the host where is the VM located
     */
    public java.net.InetAddress getInetAddress();

    /**
     * Returns the given name (identifier) of this VM.
     *
     * @return the given name (identifier) of this VM
     */
    public String getName();

    /**
     * Returns the host where the vm is located
     * @return the host where the vm is located
     */
    public String getHostName();

    /**
     * Gives the name of the java virtual machine, that was used to create this node.
     * The name corresponds to the name specified in the descriptor file:
     *  <pre>
     *  ...
     *  &lt;deployment&gt;
     *  ...
     *          &lt;jvms&gt;
     *                 ...
     *                &lt;jvm name="The Name"&gt;...&lt;/jvm&gt;
     *                ...
     *                &lt;/jvms&gt;
     *  ...
     *  &lt;/deployment&gt;
     *  </pre>
     * @return The java virtual machine name if created with a descriptor file, null otherwise.
     *
     */
    public String getDescriptorVMName();

    /**
     * Returns the capacity of the ProActive Runtime
     *
     * Capacity corresponds to the number of node created by default on the runtime.
     * Usually capacity corresponds to the number of cores on which the JVM runs.
     */
    public long getCapacity();

    public long getDeploymentId();
}
