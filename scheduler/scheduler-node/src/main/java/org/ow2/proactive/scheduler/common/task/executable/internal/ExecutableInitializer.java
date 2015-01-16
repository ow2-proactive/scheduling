/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable.internal;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.utils.NodeSet;


/**
 * ExecutableInitializer is used to initialized the executable.
 * It is sent to the init method of each executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface ExecutableInitializer extends StandaloneExecutableInitializer {

    /**
     * Returns a list of ProActive Nodes used by the task
     * @return list of nodes
     */
    public NodeSet getNodes();

    /**
     * Set the nodes list value to the given nodes value
     *
     * @param nodes the nodes to set
     */
    public void setNodes(NodeSet nodes);

    /**
     * Set the decrypter value to the given decrypter value
     *
     * @param decrypter the decrypter to set
     */
    public void setDecrypter(Decrypter decrypter);

    /**
     * Get the decrypter from this initializer
     *
     * @return the decrypter from this initializer
     */
    public Decrypter getDecrypter();

    /**
     * Returns the DataSpaceFileObject of the local space
     * @return
     */
    public DataSpacesFileObject getLocalSpaceFileObject();

    /**
     * Sets the task local space
     * @param space local space file object
     */
    public void setLocalSpaceFileObject(DataSpacesFileObject space);

    /**
     * Returns the DataSpaceFileObject of the input space
     * @return
     */
    public DataSpacesFileObject getInputSpaceFileObject();

    /**
     * Sets the task input space
     * @param space input space file object
     */
    public void setInputSpaceFileObject(DataSpacesFileObject space);

    /**
     * Returns the DataSpaceFileObject of the output space
     * @return
     */
    public DataSpacesFileObject getOutputSpaceFileObject();

    /**
     * Sets the task output space
     * @param space output space file object
     */
    public void setOutputSpaceFileObject(DataSpacesFileObject space);

    /**
     * Returns the DataSpaceFileObject of the global space
     * @return
     */
    public DataSpacesFileObject getGlobalSpaceFileObject();

    /**
     * Sets the task global space
     * @param space global space file object
     */
    public void setGlobalSpaceFileObject(DataSpacesFileObject space);

    /**
     * Returns the DataSpaceFileObject of the user space
     * @return
     */
    public DataSpacesFileObject getUserSpaceFileObject();

    /**
     * Sets the task user space
     * @param space user space file object
     */
    public void setUserSpaceFileObject(DataSpacesFileObject space);

}
