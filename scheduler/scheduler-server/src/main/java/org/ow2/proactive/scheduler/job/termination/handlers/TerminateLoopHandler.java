package org.ow2.proactive.scheduler.job.termination.handlers;

import java.util.Date;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Predictor;

public class TerminateLoopHandler {

	public static final Logger logger = Logger.getLogger(TerminateLoopHandler.class);

	private final InternalJob internalJob;

	public TerminateLoopHandler(InternalJob internalJob) {
		this.internalJob = internalJob;
	}

	public boolean terminateLoopTask(boolean didAction, FlowAction action, InternalTask initiator,
			ChangedTasksInfo changesInfo, SchedulerStateUpdate frontend) {
		// find the target of the loop
		InternalTask target = null;
		if (action.getTarget().equals(initiator.getName())) {
			target = initiator;
		} else {
			target = internalJob.findTaskUp(action.getTarget(), initiator);
		}
		didAction = internalJob.replicateForNextLoopIteration(initiator, target, changesInfo, frontend, action);
		if (didAction && action.getCronExpr() != null) {
			for (TaskId tid : changesInfo.getNewTasks()) {
				InternalTask newTask = internalJob.getIHMTasks().get(tid);
				try {
					Date startAt = (new Predictor(action.getCronExpr())).nextMatchingDate();
					newTask.addGenericInformation(InternalJob.GENERIC_INFO_START_AT_KEY,
							ISO8601DateUtil.parse(startAt));
					newTask.setScheduledTime(startAt.getTime());
				} catch (InvalidPatternException e) {
					// this will not happen as the cron expression is
					// already being validated in FlowScript class.
				}
			}
		}
		return didAction;
	}

}
