/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * This class represents a job for the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobDescriptor extends Serializable, Comparable<JobDescriptor> {

    /**
     * Return true if the task represented by the given taskId has children, false if not.
     *
     * @param taskId the id representing the real task.
     * @return true if the task represented by the given taskId has children, false if not.
     */
    public boolean hasChildren(TaskId taskId);

    /**
     * To get the id
     *
     * @return the id
     */
    public JobId getId();

    /**
     * To get the priority
     *
     * @return the priority
     */
    public JobPriority getPriority();

    /**
     * To get the tasks.
     *
     * @return the tasks.
     */
    public Collection<EligibleTaskDescriptor> getEligibleTasks();

    /**
     * To get the type
     *
     * @return the type
     */
    public JobType getType();

    /**
     * Returns the number Of Tasks.
     *
     * @return the number Of Tasks.
     */
    public int getNumberOfTasks();

    /**
     * Return the generic informations has a Map.
     *
     * @return the generic informations has a Map.
     */
    public Map<String, String> getGenericInformations();

    /**
     * Returns the projectName.
     *
     * @return the projectName.
     */
    public String getProjectName();
}
