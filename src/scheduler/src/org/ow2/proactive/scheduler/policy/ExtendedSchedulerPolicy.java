/*
 * ################################################################
 *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.policy;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;

/**
 * ExtendedSchedulerPolicy class provides:
 * 
 * - Support for startAt generic info. The users can annotate their jobs/tasks
 * with 'startAt' generic info. Those jobs/tasks will not be scheduled for
 * execution unless current date-time of the scheduler succeeds date-time
 * specified by startAt generic info.
 * 
 * The value of 'startAt' generic info should be a ISO-8601 complaint string. If
 * the 'startAt' is specified at the task level, it overrides the value
 * specified at job level.
 * 
 */
public class ExtendedSchedulerPolicy extends DefaultPolicy {

	private static final Logger logger = Logger
			.getLogger(ExtendedSchedulerPolicy.class);

	public static final String GENERIC_INFORMATION_KEY_START_AT = "START_AT";

	/*
	 * Utilize 'startAt' generic info and filter any tasks that should not be
	 * scheduled for current execution cycle.
	 */
	@Override
	public Vector<EligibleTaskDescriptor> getOrderedTasks(
			List<JobDescriptor> jobDescList) {
		Date now = new Date();
		Vector<EligibleTaskDescriptor> executionCycleTasks = new Vector<EligibleTaskDescriptor>();
		// sorts the JobDescriptor list by 'priority'.
		Collections.sort(jobDescList);
		for (JobDescriptor jobDesc : jobDescList) {
			Collection<EligibleTaskDescriptor> eligibleTasks = jobDesc
					.getEligibleTasks();
			for (EligibleTaskDescriptor candidate : eligibleTasks) {
				String startAt = getStartAtValue(jobDesc, candidate);
				if (startAt == null) {
					executionCycleTasks.add(candidate);
				} else {
					try {
						if (now.after(ISO8601DateUtil.toDate(startAt))) {
							executionCycleTasks.add(candidate);

						} else {
							if (logger.isTraceEnabled()) {
								logger.trace(String
										.format("Task [jobId:\"%s\", taskId:\"%s\"] is scheduled to be executed at %s."
												+ " It will not be scheduled for this execution cycle at %s.",
												jobDesc.getJobId(),
												candidate.getTaskId(), startAt,
												ISO8601DateUtil.parse(now)));
							}
						}
					} catch (ParseException e) {
						logger.error(
								String.format(
										"An error occurred while processing 'startAt' generic info.%n"
												+ "Task ([job-id:\"%s\", task-id:\"%s\"]) will be scheduled immediately for execution.",
										jobDesc.getJobId().toString(),
										candidate.getTaskId().toString()), e);
						executionCycleTasks.add(candidate);
					}

				}
			}
		}
		return executionCycleTasks;
	}

	/*
	 * START_AT property defined at task level always has the precedence over
	 * the same property defined job level.
	 */
	private String getStartAtValue(JobDescriptor jobDesc,
			EligibleTaskDescriptor taskDesc) {
		String startAt = taskDesc.getInternal().getGenericInformations()
				.get(GENERIC_INFORMATION_KEY_START_AT);
		if (startAt == null) {
			startAt = jobDesc.getInternal().getGenericInformations()
					.get(GENERIC_INFORMATION_KEY_START_AT);
		}
		return startAt;
	}
}
