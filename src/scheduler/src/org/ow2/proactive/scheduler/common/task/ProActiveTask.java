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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.task.executable.ProActiveExecutable;


/**
 * Use this class to build a ProActive task that will use a {@link ProActiveExecutable} and be integrated in a {@link ProActiveJob}.<br>
 * You have to specify the number of nodes you need during the execution using the {@link #setNumberOfNodesNeeded(int)} method.<br>
 * You can also specify arguments to give to the task using the {@link JavaTask#addArgument(String, String)} as the java task does it.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class ProActiveTask extends JavaTask {

    /**
     * Empty constructor.
     */
    public ProActiveTask() {
    }

    /**
     * <font color="red">Should be never used in this context.</font>
     */
    @Override
    public void addDependence(Task task) {
        throw new RuntimeException("ProActiveTask.addDependence(Task) Should be never used in this context !");
    }

    /**
     * <font color="red">Should be never used in this context.</font>
     */
    @Override
    public void addDependences(List<Task> tasks) {
        throw new RuntimeException(
            "ProActiveTask.addDependences(List<Task>) Should be never used in this context !");
    }

    /**
     * Set the number of nodes needed for this task.<br />
     * This number represents the total number of nodes that you need. You may remember that
     * one node is used to start your task. So if you ask for 11 nodes, 10 would be given to your
     * ProActive executable task.
     * (Default number is 1)
     *
     * @param numberOfNodesNeeded the number Of Nodes Needed to set.
     */
    public void setNumberOfNodesNeeded(int numberOfNodesNeeded) {
        if (this.numberOfNodesNeeded < 1) {
            this.numberOfNodesNeeded = 1;
        }

        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }

}
