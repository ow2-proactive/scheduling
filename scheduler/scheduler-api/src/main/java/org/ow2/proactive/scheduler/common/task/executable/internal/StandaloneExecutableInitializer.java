/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable.internal;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.LocalSpace;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;


/**
 * StandaloneExecutableInitializer is used to initialize the standalone executable (no dependency to scheduler-node).
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface StandaloneExecutableInitializer extends Serializable {

    /**
     * Returns a list of ProActive Nodes url used by the task
     * @return list of nodes url
     */
    List getNodesURL();

    /**
     * Set the value of nodes url
     *
     * @param nodes the nodes url to set
     */
    public void setNodesURL(List<String> nodes);

    /**
     * Returns an interface to the task local space
     * @return local space interface
     */
    LocalSpace getLocalSpace();

    /**
     * Sets the task local space
     * @param space local space interface
     */
    void setLocalSpace(LocalSpace space);

    /**
     * Returns an interface  to the task remote input space
     * @return remote space interface
     */
    RemoteSpace getInputSpace();

    /**
     * Sets the task input space
     * @param space input space interface
     */
    void setInputSpace(RemoteSpace space);

    /**
     * Returns an interface  to the task remote output space
     * @return remote space interface
     */
    RemoteSpace getOutputSpace();

    /**
     * Sets the task output space
     * @param space output space interface
     */
    void setOutputSpace(RemoteSpace space);

    /**
     * Returns an interface  to the task remote global space
     * @return remote space interface
     */
    RemoteSpace getGlobalSpace();

    /**
     * Sets the task global space
     * @param space global space interface
     */
    void setGlobalSpace(RemoteSpace space);

    /**
     * Returns an interface  to the task remote user space
     * @return remote space interface
     */
    RemoteSpace getUserSpace();

    /**
     * Sets the task user space
     * @param space user space interface
     */
    void setUserSpace(RemoteSpace space);

    void setTaskId(TaskId taskId);

    TaskId getTaskId();

    PrintStream getOutputSink();

    void setOutputSink(PrintStream redirectedStdout);

    void setErrorSink(PrintStream redirectedError);
}
