/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.process;


/**
 * A hierarchical process must implement this interface and override some
 * methods from UniversalProcess
 *
 * <ul>
 *         <li>
 *                 <b>isHierarchical</b>: must return true
 *         </li>
 *         <li>
 *                 <b>getNodeNumber</b>: must return the count of effective nodes
 *                 (forwarders are not)
 *         </li>
 *         <li>
 *                 <b>StartProcess</b>: must ensure that org.objectweb.proactive.core.runtime.StartHierarchical
 *                 is launched on the forwarder
 *         </li>
 * </ul>
 *
 * Take a look at SSHHierarchicalProcess for an example of HierarchicalProcess implementation.
 *
 * @author ProActive Team
 * @see org.objectweb.proactive.core.process.ssh.SSHHierarchicalProcess
 */
public interface HierarchicalProcess {

    /**
     * Set the Process to be deployed from the forwarder
     * @param process
     */
    public void setHierarchicalProcess(ExternalProcess process);

    /**
     * Get the Process to be deployed from the forwarder
     * @return the Process to be deployed from the forwarder
     */
    public ExternalProcess getHierarchicalProcess();
}
