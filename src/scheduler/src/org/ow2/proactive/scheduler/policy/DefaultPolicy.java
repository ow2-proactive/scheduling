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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * Implementation of the policy according that :
 * <ul>
 * 	<li>Implementation of the policy using FIFO priority ordering.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class DefaultPolicy extends Policy {

	private Set<TaskId> sel = new HashSet<TaskId>();
	private boolean preempt = false;

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.ow2.proactive.scheduler.policy.Policy#getOrderedTasks(java.util.List)
     */
    @Override
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
	Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        //sort jobs by priority
        Collections.sort(jobs);

        /*for (JobDescriptor jd : jobs) {
            toReturn.addAll(jd.getEligibleTasks());
        }*/

        /*enclosing:for (JobDescriptor jd : jobs) {
		for (EligibleTaskDescriptor etd : jd.getEligibleTasks()){
			if (etd.getAttempt() > 40){
				toReturn.clear();
				toReturn.add(etd);
				break enclosing;
			}
			toReturn.add(etd);
		}
        }*/

        if (preempt){



	        enclosing:for (JobDescriptor jd : jobs) {
			for (EligibleTaskDescriptor etd : jd.getEligibleTasks()){
				if (jd.getInternal().getPriority() == JobPriority.HIGHEST || etd.getAttempt() > 10 && etd.getInternal().getNumberOfNodesNeeded() <= RMState.getTotalAliveNodesNumber()){
					toReturn.clear();
					toReturn.add(etd);
					//try to preempt if highest only
					if (jd.getInternal().getPriority() == JobPriority.HIGHEST && etd.getInternal().getNumberOfNodesNeeded() > RMState.getFreeNodesNumber()){
						try {
							//preempt one task
							for (JobDescriptor jdd : jobs){
								for (TaskDescriptor td : jdd.getRunningTasks().values()){
									//we dont preempt highest... obviously... hum
									if (jdd.getInternal().getPriority() != JobPriority.HIGHEST){
										core.preemptTask(jdd.getJobId(),td.getTaskId().getReadableName(),10);
										break enclosing;
									}
								}
							}
						} catch (Exception e){e.printStackTrace();}
					}
					break enclosing;
				}
				toReturn.add(etd);
			}
	        }



        } else {



	        for (JobDescriptor jd : jobs) {
			if (jd.getInternal().getProjectName().equals("switch")){
				preempt = true;
			}
			for (EligibleTaskDescriptor etd : jd.getEligibleTasks()){
				if (!sel.contains(etd.getTaskId()) && etd.getTaskId().value().matches(".*00[12]$")){
					try {
						etd.getInternal().addSelectionScript(
								new SelectionScript("selected=java.net.InetAddress.getLocalHost().getHostName().matches(\".*jily.*\");","js"));
						sel.add(etd.getTaskId());
					} catch(InvalidScriptException e){e.printStackTrace();}
				}
				toReturn.add(etd);
			}
	        }



        }
        return toReturn;
    }

}
