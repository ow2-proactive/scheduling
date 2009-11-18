/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.forecaster;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;

/**
 * <i><font size="2" color="#FF0000">** Scheduler core ** </font></i> This is
 * the main active object of the scheduler implementation, it communicates with
 * the entity manager to acquire nodes and with a policy to insert and get jobs
 * from the queue.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerCoreSimulator {

	public static String loggerName = "forecasterLogger";

	/** Scheduler logger */
	public static final Logger logger = ProActiveLogger.getLogger(loggerName);

	/** Simulator of the Resource Manager */
	ResourceManagerSimulator resourceManager;

	/** Scheduler current policy */
	Policy policy;

	/** list of all jobs managed by the scheduler */
	Map<JobId, InternalJob> jobs;

	/** list of pending jobs among the managed jobs */
	Vector<InternalJob> pendingJobs;

	/** list of running jobs among the managed jobs */
	Vector<InternalJob> runningJobs;

	/** list of finished jobs among the managed jobs */
	private Vector<InternalJob> finishedJobs;

	private SchedulingMethod schedulingMethod;

	/**
	 * Create a new scheduler Core with the given arguments.<br>
	 * 
	 * @param rmSimul
	 *            the resource manager on which the scheduler will interact.
	 * @param frontend
	 *            a reference to the frontend.
	 * @param policyFullName
	 *            the fully qualified name of the policy to be used.
	 */
	public SchedulerCoreSimulator(ResourceManagerSimulator rmSimul,
			String policyFullName) {
		try {
			this.jobs = new HashMap<JobId, InternalJob>();
			this.pendingJobs = new Vector<InternalJob>();
			this.runningJobs = new Vector<InternalJob>();
			this.finishedJobs = new Vector<InternalJob>();

			this.resourceManager = rmSimul;

			// starting scheduling policy
			this.policy = (Policy) Class.forName(policyFullName).newInstance();

			logger.info("Instanciated policy : " + policyFullName);
			logger.info("Scheduler Core ready !");
		} catch (InstantiationException e) {
			logger
					.error("The policy class cannot be found : "
							+ e.getMessage());
			logger.error("", e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			logger.error("The method cannot be accessed " + e.getMessage());
			logger.error("", e);
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			logger
					.error("The class definition cannot be found, it might be due to case sentivity : "
							+ e.getMessage());
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	public SimulationResult run() {
		schedulingMethod = new SchedulingMethodSimulator(this);
		boolean simulationFinished = false;

		while (!simulationFinished) {
			schedulingMethod.schedule();
			simulationFinished = ((this.pendingJobs.size() + this.runningJobs
					.size()) == 0);
		}

		return new SimulationResult();

	}

	/**
	 * call this when a task terminates mark task as terminated free node
	 */
	private void terminate(TaskId taskId) {
		JobId jobId = taskId.getJobId();
		logger.info("Received terminate task request for task '" + taskId
				+ "' - job '" + jobId + "'");
		InternalJob job = jobs.get(jobId);

		logger.info("Task '" + taskId + "' on job '" + jobId + "' terminated");

		// to be done before terminating the task, once terminated it is not
		// running anymore..
		// mark the task as terminated within its job
		InternalTask internalTask = job.terminateTask(false, taskId);

		// if this job is finished (every task have finished)
		logger.info("Number of finished tasks : "
				+ job.getNumberOfFinishedTasks() + " - Number of tasks : "
				+ job.getTotalNumberOfTasks());
		if (job.isFinished()) {
			// terminating job
			job.terminate();
			runningJobs.remove(job);
			finishedJobs.add(job);
			logger.info("Job '" + jobId + "' terminated");

		}// if job finished

		resourceManager.freeNodes(internalTask.getExecuterInformations()
				.getNodes());
	}

	// Methods for our simulation ....

	/**
	 * This method is called by the SchedulingMethodSimulator in order to notify
	 * this object that the tasks in argument have been started on their
	 * respective nodes
	 * 
	 * TODO: - save this information in the local state among the running tasks
	 * in the local state choose the one that is supposed to finish first -
	 * finish the chosen task (call the terminate method)
	 * 
	 * 
	 * IMPORTANT: this method may be called with an empty map if there are no
	 * tasks to be scheduled
	 * 
	 */
	public void receiveNotificationTasksStarted(Map<TaskId, NodeSet> tasksList) {

	}

	/**
	 * TODO: implement this method
	 * 
	 * @param jobs
	 */
	public void init(SimulationInitializer initializer) {

	}

}
