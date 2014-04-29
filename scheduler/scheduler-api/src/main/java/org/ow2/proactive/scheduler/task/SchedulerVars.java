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
package org.ow2.proactive.scheduler.task;

/**
 * Scheduler related java properties. Thoses properties are automatically
 * translated into system property when the task is native (see NativeTaskLauncher) :
 * SYSENV_NAME = upcase(JAVAENV_NAME).replace('.','_')
 */
public enum SchedulerVars {
    /**  */
    JAVAENV_JOB_ID_VARNAME("pas.job.id"),
    /**  */
    JAVAENV_JOB_NAME_VARNAME("pas.job.name"),
    /**  */
    JAVAENV_TASK_ID_VARNAME("pas.task.id"),
    /**  */
    JAVAENV_TASK_NAME_VARNAME("pas.task.name"),
    /**  */
    JAVAENV_TASK_ITERATION("pas.task.iteration"),
    /**  */
    JAVAENV_TASK_REPLICATION("pas.task.replication");

    String varName;

    SchedulerVars(String vn) {
        varName = vn;
    }

    /**
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return varName;
    }
}
