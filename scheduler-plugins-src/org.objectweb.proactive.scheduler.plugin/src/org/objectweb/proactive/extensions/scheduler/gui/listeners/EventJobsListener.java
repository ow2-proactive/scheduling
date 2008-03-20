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
package org.objectweb.proactive.extensions.scheduler.gui.listeners;

import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;


/**
 * @author FRADJ Johann
 *
 */
public interface EventJobsListener {

    /**
     * Invoked when a job has been killed on the scheduler.
     *
     * @param jobId the job to killed.
     */
    public void killedEvent(JobId jobId);

    /**
     * Invoked when a job has been paused on the scheduler.
     *
     * @param event the informations on the paused job.
     */
    public void pausedEvent(JobEvent event);

    /**
     * Invoked when a job has been resumed on the scheduler.
     *
     * @param event the informations on the resumed job.
     */
    public void resumedEvent(JobEvent event);

    /**
     * Invoked when a job priority has been changed.
     *
     * @param event the informations on the resumed job.
     */
    public void priorityChangedEvent(JobEvent event);
}
