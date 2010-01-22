/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.utils.NodeSet;


/**
 * An executable container allows to instantiate the actual executable in a lazy manner, i.e.
 * on the worker node that will execute the actual executable.
 *
 * @author The ProActive Team
 */
@Table(name = "EXECUTABLE_CONTAINER")
@MappedSuperclass
@AccessType("field")
@Proxy(lazy = false)
public abstract class ExecutableContainer implements Serializable {

    /**  */
    private static final long serialVersionUID = 200;
    // node set : not DB managed
    @Transient
    protected NodeSet nodes;

    /**
     * Create and return the contained executable
     * @return the contained executable
     * @throws ExecutableCreationException if the executable cannot be created
     */
    public abstract Executable getExecutable() throws ExecutableCreationException;

    /**
     * Generic init method for executable containers.
     * This method is called by SchedulerCore just before sending the container
     * on the node that will execute the contained executable.
     *
     * @param job the job owning the contained executable
     */
    public abstract void init(ExecutableContainerInitializer initializer);

    /**
     * Create the executable initializer and returns it.
     *
     * @return the created executable initializer
     */
    public abstract ExecutableInitializer createExecutableInitializer();

    /**
     * Set the nodes value to the given nodes value
     *
     * @param nodes the nodes to set
     */
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

}
