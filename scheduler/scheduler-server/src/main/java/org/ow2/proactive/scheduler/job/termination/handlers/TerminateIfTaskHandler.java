package org.ow2.proactive.scheduler.job.termination.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

public class TerminateIfTaskHandler {

	public static final Logger logger = Logger.getLogger(TerminateIfTaskHandler.class);

	private final InternalJob internalJob;

	public TerminateIfTaskHandler(InternalJob internalJob) {
		this.internalJob = internalJob;
	}

	public boolean terminateIfTask(FlowAction action, InternalTask initiator, ChangedTasksInfo changesInfo,
			SchedulerStateUpdate frontend, InternalTask descriptor, TaskId taskId) {

		InternalTask[] targets = searchIfElseJoinTasks(action, initiator);

		// the targetIf from action.getTarget() is the selected branch;
		// the IF condition has already been evaluated prior to being put in a
		// FlowAction
		// the targetElse from action.getTargetElse() is the branch that was NOT
		// selected
		InternalTask targetIf = targets[0];
		InternalTask targetElse = targets[1];
		InternalTask targetJoin = targets[2];

		logger.info("Control Flow Action IF: " + targetIf.getId() + " join: "
				+ ((targetJoin == null) ? "null" : targetJoin.getId()));

		// these 2 tasks delimit the Task Block formed by the IF branch
		InternalTask branchStart = targetIf;
		InternalTask branchEnd = null;

		String match = targetIf.getMatchingBlock();
		if (match != null) {
			for (InternalTask t : internalJob.getIHMTasks().values()) {
				if (match.equals(t.getName())
						&& !(t.getStatus().equals(TaskStatus.FINISHED) || t.getStatus().equals(TaskStatus.SKIPPED))) {
					branchEnd = t;
				}
			}
		}
		// no matching block: there is no block, the branch is a single task
		if (branchEnd == null) {
			branchEnd = targetIf;
		}

		// plug the branch
		branchStart.addDependence(initiator);
		changesInfo.taskUpdated(branchStart);
		if (targetJoin != null) {
			targetJoin.addDependence(branchEnd);
			changesInfo.taskUpdated(targetJoin);
		}

		// the other branch will not be executed
		// first, find the concerned tasks
		List<InternalTask> elseTasks = new ArrayList<>();
		// elseTasks.add(targetElse);
		for (InternalTask t : internalJob.getIHMTasks().values()) {
			if (t.dependsOn(targetElse)) {
				elseTasks.add(t);
			}
		}

		// even though the targetElse is not going to be executed, a
		// dependency on initiator still makes sense and would help
		// reconstruct the job graph on the client
		targetElse.addDependence(initiator);
		changesInfo.taskUpdated(targetElse);

		for (InternalTask it : elseTasks) {
			it.setFinishedTime(descriptor.getFinishedTime() + 1);
			it.setStatus(TaskStatus.SKIPPED);
			it.setExecutionDuration(0);

			changesInfo.taskSkipped(it);
			internalJob.setNumberOfPendingTasks(internalJob.getNumberOfPendingTasks() - 1);
			internalJob.setNumberOfFinishedTasks(internalJob.getNumberOfFinishedTasks() + 1);
			logger.info("Task " + it.getId() + " will not be executed");
		}

		// plug the branch in the descriptor
		TaskId joinId = null;
		if (targetJoin != null) {
			joinId = targetJoin.getId();
		}
		internalJob.getJobDescriptor().doIf(initiator.getId(), branchStart.getId(), branchEnd.getId(), joinId,
				targetElse.getId(), elseTasks);

		((JobInfoImpl) internalJob.getJobInfo()).setTasksChanges(changesInfo, internalJob);
		// notify frontend that tasks were modified
		if (frontend != null) {
			frontend.jobStateUpdated(internalJob.getOwner(),
					new NotificationData<>(SchedulerEvent.TASK_SKIPPED, internalJob.getJobInfo()));
		}
		((JobInfoImpl) internalJob.getJobInfo()).clearTasksChanges();

		// no jump is performed ; now that the tasks have been plugged
		// the flow can continue its normal operation
		internalJob.getJobDescriptor().terminate(taskId);

		return true;
	}

	private InternalTask[] searchIfElseJoinTasks(FlowAction action, InternalTask initiator) {
		InternalTask targetIf = null;
		InternalTask targetElse = null;
		InternalTask targetJoin = null;

		// search for the targets as perfect matches of the unique name
		for (InternalTask it : internalJob.getIHMTasks().values()) {

			// target is finished : probably looped
			if (it.getStatus().equals(TaskStatus.FINISHED) || it.getStatus().equals(TaskStatus.SKIPPED)) {
				continue;
			}
			if (action.getTarget().equals(it.getName())) {
				if (it.getIfBranch().equals(initiator)) {
					targetIf = it;
				}
			} else if (action.getTargetElse().equals(it.getName())) {
				if (it.getIfBranch().equals(initiator)) {
					targetElse = it;
				}
			} else if (action.getTargetContinuation().equals(it.getName())) {
				InternalTask up = internalJob.findTaskUp(initiator.getName(), it);
				if (up != null && up.equals(initiator)) {
					targetJoin = it;
				}
			}

		}

		boolean searchIf = (targetIf == null);
		boolean searchElse = (targetElse == null);
		boolean searchJoin = (targetJoin == null);

		// search of a runnable perfect match for the targets failed;
		// the natural target was iterated, need to find the next iteration
		// which is the the one with the same dup index and base name,
		// but the highest iteration index
		for (InternalTask it : internalJob.getIHMTasks().values()) {

			// does not share the same dup index : cannot be the same scope
			if (it.getReplicationIndex() != initiator.getReplicationIndex()) {
				continue;
			}

			if (it.getStatus().equals(TaskStatus.FINISHED) || it.getStatus().equals(TaskStatus.SKIPPED)) {
				continue;
			}

			String name = InternalTask.getInitialName(it.getName());

			if (searchIf && InternalTask.getInitialName(action.getTarget()).equals(name)) {
				if (targetIf == null || targetIf.getIterationIndex() < it.getIterationIndex()) {
					targetIf = it;
				}
			} else if (searchElse && InternalTask.getInitialName(action.getTargetElse()).equals(name)) {
				if (targetElse == null || targetElse.getIterationIndex() < it.getIterationIndex()) {
					targetElse = it;
				}
			} else if (searchJoin && InternalTask.getInitialName(action.getTargetContinuation()).equals(name)) {
				if (targetJoin == null || targetJoin.getIterationIndex() < it.getIterationIndex()) {
					targetJoin = it;
				}
			}
		}

		InternalTask[] result = { targetIf, targetElse, targetJoin };
		return result;
	}

}
