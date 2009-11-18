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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.core.SchedulingTaskComparator;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;

/**
 * SchedulingMethodImpl is the default implementation for the scheduling process
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
final class SchedulingMethodSimulator implements SchedulingMethod {

	/** Scheduler logger */
	protected static final Logger logger = ProActiveLogger
			.getLogger(SchedulerCoreSimulator.loggerName);

	protected SchedulerCoreSimulator core = null;

	SchedulingMethodSimulator(SchedulerCoreSimulator core) {
		this.core = core;
	}

	/**
	 * Scheduling process. For this implementation, steps are :<br>
	 * <ul>
	 * <li>Select running and pending jobs to be scheduled
	 * <li>Get an ordered list of the selected tasks to be scheduled
	 * <li>While returned tasks list is not empty :
	 * <ul>
	 * <li>Get n first compatible tasks (same selection script, same node
	 * exclusion
	 * <li>Ask nodes to RM according to the previous specification
	 * <li>Try to start each tasks
	 * <li>Job started event if needed
	 * <li>Task started event
	 * </ul>
	 * <li>Manage exception while deploying tasks on nodes
	 * </ul>
	 */
	public void schedule() {
		// create the map with the tasks to be started and their respective
		// nodes
		// Remember one task might need several nodes
		Map<TaskId, NodeSet> startedtasks = new HashMap<TaskId, NodeSet>();

		// get job Descriptor list with eligible jobs (running and pending)
		ArrayList<JobDescriptor> jobDescriptorList = createJobDescriptorList();

		// ask the policy all the tasks to be schedule according to the jobs
		// list.
		LinkedList<EligibleTaskDescriptor> taskRetrivedFromPolicy = new LinkedList<EligibleTaskDescriptor>(
				core.policy.getOrderedTasks(jobDescriptorList));

		// if there is no task to be scheduled, notify the Core Simulator with
		// an empty map
		if (taskRetrivedFromPolicy == null
				|| taskRetrivedFromPolicy.size() == 0) {
			core.receiveNotificationTasksStarted(startedtasks);
			return;
		}

		logger.info("Number of tasks ready to be scheduled : "
				+ taskRetrivedFromPolicy.size());

		while (!taskRetrivedFromPolicy.isEmpty()) {
			// get rmState and update it in scheduling policy
			RMState rmState = core.resourceManager.getRMState();
			core.policy.RMState = rmState;
			int freeResourcesNb = rmState.getNumberOfFreeResources().intValue();
			logger.info("Number of free resources : " + freeResourcesNb);

			// if there is no free resources, stop it right now
			// this should not happen during the simulation
			// when we reach here, there should be at least one resource
			// available
			// TODO: check this and remove lines...
			if (freeResourcesNb == 0) {
				break;
			}

			// get the next compatible tasks from the whole returned policy
			// tasks
			LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<EligibleTaskDescriptor>();
			int neededResourcesNumber = getNextcompatibleTasks(
					taskRetrivedFromPolicy, freeResourcesNb, tasksToSchedule);
			logger.debug("Number of nodes to ask for : "
					+ neededResourcesNumber);

			// ask nodes to the RM, fail tasks and jobs if selection script
			// fails (tasks could never be started)
			NodeSet nodeSet = getRMNodes(neededResourcesNumber, tasksToSchedule);

			// fill in the map of started tasks:

			Node node = null;
			while (nodeSet != null && !nodeSet.isEmpty()) {
				EligibleTaskDescriptor taskDescriptor = tasksToSchedule
						.removeFirst();
				InternalJob currentJob = core.jobs.get(taskDescriptor
						.getJobId());
				InternalTask internalTask = currentJob.getIHMTasks().get(
						taskDescriptor.getId());

				// create launcher and try to start the task
				node = nodeSet.get(0);

				// enough nodes to be launched at same time for a
				// communicating task
				if (nodeSet.size() >= internalTask.getNumberOfNodesNeeded()) {
					nodeSet.remove(0);

					// create launcher
					NodeSet nodes = new NodeSet();
					nodes.add(node);
					for (int i = 0; i < (internalTask.getNumberOfNodesNeeded() - 1); i++) {
						nodes.add(nodeSet.remove(0));
					}// for nodes

					finalizeStarting(currentJob, internalTask);

				}// if nodeSet.size() >=
				// internalTask.getNumberOfNodesNeeded()

				// if every task that should be launched have been removed
				if (tasksToSchedule.isEmpty()) {
					// get back unused nodes to the RManager
					if (!nodeSet.isEmpty()) {
						core.resourceManager.freeNodes(nodeSet);
					}
					// and leave the loop
					break;
				}
			}

		}
	}

	/**
	 * Create the eligible job descriptor list. This list contains every
	 * eligible jobs containing eligible tasks.
	 * 
	 * @return eligible job descriptor list
	 */
	protected ArrayList<JobDescriptor> createJobDescriptorList() {
		ArrayList<JobDescriptor> list = new ArrayList<JobDescriptor>();

		// add running jobs
		for (InternalJob j : core.runningJobs) {
			if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
				list.add(j.getJobDescriptor());
			}
		}

		// if scheduler is not paused, add pending jobs
		for (InternalJob j : core.pendingJobs) {
			if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
				list.add(j.getJobDescriptor());
			}
		}
		return list;
	}

	/**
	 * Extract the n first compatible tasks from the first argument list, and
	 * return them according that the extraction is stopped when the maxResource
	 * number is reached.<br>
	 * Two tasks are compatible if and only if they have the same list of
	 * selection script and the same list of node exclusion. The check of
	 * compliance is currently done by the {@link SchedulingTaskComparator}
	 * class.<br>
	 * This method has two side effects : extracted tasks are removed from the
	 * bagOfTasks and put in the toFill list
	 * 
	 * @param bagOfTasks
	 *            the list of tasks form which to extract tasks
	 * @param maxResource
	 *            the limit number of resources that the extraction should not
	 *            exceed
	 * @param toFill
	 *            the list that will contains the task to schedule at the end.
	 *            This list must not be null but must be empty.<br>
	 *            this list will be filled with the n first compatible tasks
	 *            according that the number of resources needed by these tasks
	 *            does not exceed the given max resource number.
	 * @return the number of nodes needed to start every task present in the
	 *         'toFill' argument at the end of the method.
	 */
	protected int getNextcompatibleTasks(
			LinkedList<EligibleTaskDescriptor> bagOfTasks, int maxResource,
			LinkedList<EligibleTaskDescriptor> toFill) {
		if (toFill == null || bagOfTasks == null) {
			throw new IllegalArgumentException(
					"The two given lists must not be null !");
		}
		int neededResource = 0;
		if (maxResource > 0 && !bagOfTasks.isEmpty()) {
			EligibleTaskDescriptor etd = bagOfTasks.removeFirst();
			InternalJob currentJob = core.jobs.get(etd.getJobId());
			InternalTask internalTask = currentJob.getIHMTasks().get(
					etd.getId());
			int neededNodes = internalTask.getNumberOfNodesNeeded();
			SchedulingTaskComparator referent = new SchedulingTaskComparator(
					internalTask);

			do {
				if (neededNodes > maxResource) {
					// this instruction is important and could be :
					// break : in this case, a multi node task leads the search
					// to be stopped and the
					// the current task would be retried on the next step (avoid
					// starvation better than the next one)
					// continue : in this case, we continue to start the maximum
					// number of task in a single scheduling loop.
					// this case will focus on starting single node task first
					// if lot of resources are busy.
					break;
				} else {
					// check if the task is compatible with the other previous
					// one
					if (referent.equals(new SchedulingTaskComparator(
							internalTask))) {
						neededResource += neededNodes;
						maxResource -= neededNodes;
						toFill.add(etd);
					} else {
						bagOfTasks.addFirst(etd);
						break;
					}
				}
				// if bagOfTasks is not empty
				if (!bagOfTasks.isEmpty()) {
					etd = bagOfTasks.removeFirst();
					currentJob = core.jobs.get(etd.getJobId());
					internalTask = currentJob.getIHMTasks().get(etd.getId());
					neededNodes = internalTask.getNumberOfNodesNeeded();
				}
			} while (maxResource > 0 && !bagOfTasks.isEmpty());
		}
		return neededResource;
	}

	/**
	 * Ask to the RM the given number of node resources.<br>
	 * If there is a problem with these task selection (such as bad
	 * selectionScript) this method will terminate the corresponding tasks and
	 * jobs. As the selection scripts contain errors, the task and its
	 * surrounding jobs must be stopped.
	 * 
	 * @param neededResourcesNumber
	 *            the number of resources to ask for (must be > 0).
	 * @param tasksToSchedule
	 *            the task to be scheduled
	 * @return A nodeSet that contains at most 'neededResourcesNumber' available
	 *         compatible resources. An empty nodeSet if no nodes could be found
	 *         null if the their was an exception when asking for the nodes (ie :
	 *         selection script has failed)
	 */
	protected NodeSet getRMNodes(int neededResourcesNumber,
			LinkedList<EligibleTaskDescriptor> tasksToSchedule) {
		NodeSet nodeSet = new NodeSet();

		if (neededResourcesNumber <= 0) {
			throw new IllegalArgumentException(
					"Args 'neededResourcesNumber' must be > 0");
		}

		EligibleTaskDescriptor etd = tasksToSchedule.getFirst();
		InternalJob currentJob = core.jobs.get(etd.getJobId());
		InternalTask internalTask = currentJob.getIHMTasks().get(etd.getId());

		nodeSet = core.resourceManager.getAtMostNodes(neededResourcesNumber,
				internalTask.getSelectionScripts());
		// the following line is used to unwrap the future, warning when
		// moving or removing
		// it may also throw a ScriptException which is a RuntimeException
		return nodeSet;
	}

	/**
	 * Finalize the start of the task by mark it as started. Also mark the job
	 * if it is not already started.
	 * 
	 * @param job
	 *            the job that owns the task to be started
	 * @param task
	 *            the task to be started
	 */
	protected void finalizeStarting(InternalJob job, InternalTask task) {
		// set the different informations on job
		if (job.getStartTime() < 0) {
			// if it is the first task of this job
			job.start();
			core.pendingJobs.remove(job);
			core.runningJobs.add(job);
			// update tasks events list and send it to front-end
		}

		// set the different informations on task
		job.startTask(task);
	}

}
