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
package org.objectweb.proactive.extensions.scheduler.core;

import java.util.HashMap;

import org.objectweb.proactive.extensions.scheduler.common.job.JobType;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.Stats;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.Tools;


/**
 * Stats class will be used to view some tips on the scheduler.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 25, 2007
 * @since ProActive 3.9
 */
public class StatsImpl implements Stats {

    /** Map of properties of the scheduler */
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * ProActive Empty constructor
     */
    public StatsImpl() {
    }

    /**
     * Set the start time of the scheduler.
     */
    public void startTime() {
        String key = "Start Time";

        if (!properties.containsKey(key)) {
            properties.put(key,
                Tools.getFormattedDate(System.currentTimeMillis()));
        }
    }

    /**
     * Set the last stopped time of the scheduler.
     */
    public void stopTime() {
        String key = "Last Stop Time";

        if (!properties.containsKey(key)) {
            properties.put(key,
                Tools.getFormattedDate(System.currentTimeMillis()));
        }
    }

    /**
     * Set the last paused time of the scheduler.
     */
    public void pauseTime() {
        String key = "Last Pause Time";

        if (!properties.containsKey(key)) {
            properties.put(key,
                Tools.getFormattedDate(System.currentTimeMillis()));
        }
    }

    /**
     * Increase the number of submitted jobs.
     *
     * @param type the job type of the submitted job.
     */
    public void increaseSubmittedJobCount(JobType type) {
        increaseProperty("Jobs Submitted", 1);

        switch (type) {
        case PARAMETER_SWEEPING:
            increasePSJobCount();
            break;
        case PROACTIVE:
            increasePAJobCount();
            break;
        case TASKSFLOW:
            increaseTFJobCount();
            break;
        }
    }

    /**
     * Increase the number of finished jobs.
     *
     * @param nbTasks the number of finished tasks for the job.
     */
    public void increaseFinishedJobCount(int nbTasks) {
        increaseProperty("Jobs finished", 1);
        increaseTaskCount(nbTasks);
    }

    /**
     * Increase the number of finished tasks.
     *
     * @param inc the number of finished tasks
     */
    private void increaseTaskCount(int inc) {
        increaseProperty("Tasks finished", inc);
    }

    /**
     * Increase the number of launched ProActive jobs.
     */
    private void increasePAJobCount() {
        increaseProperty("ProActive jobs Submitted", 1);
    }

    /**
     * Increase the number of launched ParameterSwipping jobs.
     */
    private void increasePSJobCount() {
        increaseProperty("ParameterSwipping jobs Submitted", 1);
    }

    /**
     * Increase the number of launched TaskFlow jobs.
     */
    private void increaseTFJobCount() {
        increaseProperty("TaskFlow jobs Submitted", 1);
    }

    /**
     * Increase the count corresponding to the property name.
     */
    private void increaseProperty(String propertyName, int inc) {
        if (!properties.containsKey(propertyName)) {
            properties.put(propertyName, Integer.valueOf(inc));
        } else {
            properties.put(propertyName,
                ((Integer) properties.get(propertyName)) + inc);
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.scheduler.Stats#getProperties()
     */
    public HashMap<String, Object> getProperties() {
        return properties;
    }
}
