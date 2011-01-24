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
package org.ow2.proactive.scheduler.policy;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Implementation of the policy according that :
 * <ul>
 * 	<li>We try to keep FIFO priority order while it's not leading to starvation
 * 	(if first tasks to be started are finally not started because of bad selection script, we must go on next tasks but
 *  this situation could lead to run lower priority before normal or high or even mislead FIFO order.)</li>
 * 	<li>A specified number of tasks can be returned by this policy (NB_TASKS_PER_LOOP setting)</li>
 *  <li>This policy returns groups of tasks sequentially taken in the whole queue.
 *  Tasks that have just been returned are not returned by the next call to the policy until a predefined
 *  number of calls is reached. (for example, if their are X tasks to schedule, the policy returns Y tasks, and there is Z nodes available,
 *  the policy will return X/Z groups of Y tasks AND THEN restart to get task from beginning of the queue.
 *  This will avoid starvation.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class DefaultPolicy extends Policy {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;
    private static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.SCHEDULE);
    /** Configuration file name used to change current configuration */
    private String CONFIG_FILE_NAME = PASchedulerProperties
            .getAbsolutePath("config/scheduler/DefaultPolicy.conf");
    /** Number of loop to wait until the next read of configuration file */
    private int READ_ARGUMENT_LOOP_FREQUENCY = 0;
    /** Maximum number of tasks returned by the policy in each loop */
    private int NB_TASKS_PER_LOOP = Integer.MAX_VALUE;

    private int nbLoop = 0;
    private int globalNbCalls = 0;
    private Set<TaskId> ids = new HashSet<TaskId>();
    private int nbCalls = 0;
    private long lastModifConfFile = 0;
    private int previousFreeNodeNumber = 0;

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.ow2.proactive.scheduler.common.policy.Policy#getOrderedTasks(java.util.List)
     */
    @Override
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        //read configuration file
        //if this.lastModifConfFile==0, it is the first read 
        if (this.lastModifConfFile == 0 ||
            (READ_ARGUMENT_LOOP_FREQUENCY != 0 && ++globalNbCalls % READ_ARGUMENT_LOOP_FREQUENCY == 0)) {
            readConfigFile();
        }

        //check number of free nodes
        int freeNodeNb;
        if (RMState == null) {
            freeNodeNb = NB_TASKS_PER_LOOP;
        } else {
            freeNodeNb = RMState.getFreeNodesNumber();
        }

        //set max number of loop to do each time the number of calls to this method equals 0
        if (nbCalls == 0) {
            nbLoop = 0;
            //max nb of loops is number of task / nb of tasks per loop
            for (JobDescriptor jobDescriptor : jobs) {
                nbLoop += jobDescriptor.getEligibleTasks().size();
            }
            nbLoop = nbLoop / NB_TASKS_PER_LOOP;
        }

        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        //sort jobs by priority
        Collections.sort(jobs);

        //fill list of task to be returned by the policy
        //max number of returned tasks will be the number of tasks per loop
        int i = 0;
        forjob: for (int y = 0; y < jobs.size(); y++) {
            for (EligibleTaskDescriptor etd : jobs.get(y).getEligibleTasks()) {
                if (!ids.contains(etd.getId())) {
                    toReturn.add(etd);
                    ids.add(etd.getId());
                    if (++i == NB_TASKS_PER_LOOP) {
                        break forjob;
                    }
                }
            }
        }

        //clear ids list in some conditions
        nbCalls++;
        if (toReturn.size() == 0 || freeNodeNb != previousFreeNodeNumber || freeNodeNb == 0) {
            ids.clear();
            nbCalls = 0;
        }

        previousFreeNodeNumber = freeNodeNb;

        return toReturn;
    }

    /**
     * Read the configuration file only if it has been modified and number of calls to read has been reached
     */
    private void readConfigFile() {
        File confFile = new File(CONFIG_FILE_NAME);
        long lastModified = confFile.lastModified();
        if (lastModified == 0) {
            logger_dev.warn("Error while accessing the lastModified field of " + CONFIG_FILE_NAME);
            return;
        }
        if (!confFile.exists() || this.lastModifConfFile == lastModified) {
            return;
        }
        try {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(confFile);
            fis.getFD().sync();
            prop.load(fis);
            fis.close();
            if (prop.containsKey("CONFIG_FILE_NAME")) {
                CONFIG_FILE_NAME = PASchedulerProperties.getAbsolutePath(prop.get("CONFIG_FILE_NAME")
                        .toString());
            }
            if (prop.containsKey("READ_ARGUMENT_LOOP_FREQUENCY")) {
                READ_ARGUMENT_LOOP_FREQUENCY = Integer.parseInt(prop.get("READ_ARGUMENT_LOOP_FREQUENCY")
                        .toString());
            }
            if (prop.containsKey("NB_TASKS_PER_LOOP")) {
                NB_TASKS_PER_LOOP = Integer.parseInt(prop.get("NB_TASKS_PER_LOOP").toString());
                if (NB_TASKS_PER_LOOP == 0) {
                    NB_TASKS_PER_LOOP = Integer.MAX_VALUE;
                }
            }
            this.lastModifConfFile = lastModified;
        } catch (Exception e) {
            //file not read due to exception while reading conf file
            logger_dev.warn("Exception while reading Policy configuration file", e);
        }
    }
}
