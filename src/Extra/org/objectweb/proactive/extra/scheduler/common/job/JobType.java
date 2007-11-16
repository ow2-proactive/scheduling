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
package org.objectweb.proactive.extra.scheduler.common.job;


/**
 * Class representing the type of the job.
 * Type are best describe below.
 *
 * @author ProActive Team
 * @version 1.0, Jun 11, 2007
 * @since ProActive 3.2
 */
public enum JobType implements java.io.Serializable {

    /**
     * Every tasks can communicate with each others.
     * So they all have to be executed in same time.
     */
    APPLI("Job Application"),
    /**
     * Tasks can be executed one by one or all in same time but
     * every task represents the same native or java task.
     * Only the parameters given to the task will change.
     */
    PARAMETER_SWEEPING("Parameter Sweeping"),
    /**
     * Tasks flow with dependences.
     * Only the task that have their dependences finished
     * can be executed.
     */
    TASKSFLOW("Tasks Flow");private String name;

    JobType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    static JobType getJobType(String typeName) {
        if (typeName.equals("taskFlow")) {
            return TASKSFLOW;
        } else {
            return APPLI;
        }
    }
}
