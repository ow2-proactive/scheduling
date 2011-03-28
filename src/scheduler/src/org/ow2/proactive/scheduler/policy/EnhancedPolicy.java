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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobPriority;
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
 *  <li>Starvation issues remain possible with multi-node tasks but could be avoid using attempt number.
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public class EnhancedPolicy extends Policy {

    private static final long serialVersionUID = 30L;
    private static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.SCHEDULE);

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.ow2.proactive.scheduler.common.policy.Policy#getOrderedTasks(java.util.List)
     */
    @Override
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        //sort jobs by priority and extract eligible tasks
        /*Collections.sort(jobs);
        for (JobDescriptor jd : jobs) {
            toReturn.addAll(jd.getEligibleTasks());
            //TIPS : the 3 following lines are used to fill stillPending list
            for (EligibleTaskDescriptor etd : jd.getEligibleTasks()) {
                stillPending.add(etd.getId());
            }
        }*/
        //TIPS : getAttempt(taskId) returns the number of attempts for the given task
        //enhanced behavior : avoid starvation
        Collections.sort(jobs);
        enclosing: for (JobDescriptor jd : jobs) {
            for (EligibleTaskDescriptor etd : jd.getEligibleTasks()) {
                stillPending.add(etd.getId());//mandatory to maintain an up-to-date list of attempts
                if (jd.getPriority().equals(JobPriority.HIGHEST) || getAttempt(etd.getId()) > 100) {
                    toReturn.clear();
                    toReturn.add(etd);
                    break enclosing;
                }
                toReturn.add(etd);
            }
        }

        //--------------------------------------------
        //call filter method and return filtered tasks
        return filter(toReturn);
    }

    /********************************************************************/
    /***********************  SPLIT BEHAVIOR BELOW **********************/

    /** Configuration file name used to change current configuration */
    private String CONFIG_FILE_NAME = PASchedulerProperties
            .getAbsolutePath("config/scheduler/DefaultPolicy.conf");
    /** Number of loop to wait until the next read of configuration file */
    private int READ_ARGUMENT_LOOP_FREQUENCY = 0;
    /** Maximum number of tasks returned by the policy in each loop */
    private int NB_TASKS_PER_LOOP = Integer.MAX_VALUE;

    private int globalNbCalls = 0;
    private Set<TaskId> ids = new HashSet<TaskId>();
    private long lastModifConfFile = 0;
    private int previousFreeNodeNumber = 0;

    /**
     * filter, mark and split task in order to return each time a limited number of tasks.
     * We try to be as fair as possible while splitting.
     * 
     * @param tasks list of ordered tasks
     * @return a filtered, splitted list of ordered tasks
     */
    private Vector<EligibleTaskDescriptor> filter(Vector<EligibleTaskDescriptor> tasks) {
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

        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();

        //fill list of task to be returned by the policy
        //max number of returned tasks will be the number of tasks per loop
        int i = 0;
        for (EligibleTaskDescriptor etd : tasks) {
            if (!ids.contains(etd.getId())) {
                toReturn.add(etd);
                ids.add(etd.getId());
                addAttempt(etd.getId());
                if (++i == NB_TASKS_PER_LOOP) {
                    break;
                }
            }
        }

        //clear ids list in some conditions
        if (toReturn.size() == 0 || freeNodeNb != previousFreeNodeNumber || freeNodeNb == 0) {
            ids.clear();
        }

        previousFreeNodeNumber = freeNodeNb;
        cleanAttempt();
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

    /**
     * Return the number of time we tried to schedule this task.
     * 
     * @param id the id of an eligible task
     * @return the number of time we attempt to run this task.
     */
    private Map<TaskId, Integer> attempts = new HashMap<TaskId, Integer>();

    protected int getAttempt(TaskId id) {
        Integer attempt = attempts.get(id);
        if (attempt == null) {
            return 0;
        } else {
            return attempt;
        }
    }

    /**
     * Add an attempts to the list of attempts.
     * 
     * @param id the id of the task concerned
     */
    private void addAttempt(TaskId id) {
        Integer attempt = attempts.get(id);
        if (attempt == null) {
            attempts.put(id, 0);
        } else {
            attempts.put(id, attempt + 1);
        }
    }

    /**
     * Clean the list of attempts by deleting no more eligible task.
     * This method also clear list of still pending tasks.
     */
    private Set<TaskId> stillPending = new HashSet<TaskId>();

    private void cleanAttempt() {
        Iterator<TaskId> iter = attempts.keySet().iterator();
        while (iter.hasNext()) {
            if (!stillPending.contains(iter.next())) {
                iter.remove();
            }
        }
        stillPending.clear();
    }

}
