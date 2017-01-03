package org.ow2.proactive.scheduler.job.termination.handlers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import it.sauronsoftware.cron4j.Predictor;


public class TerminateReplicateTaskHandler {

    public static final Logger logger = Logger.getLogger(TerminateReplicateTaskHandler.class);

    private final InternalJob internalJob;

    public TerminateReplicateTaskHandler(InternalJob internalJob) {
        this.internalJob = internalJob;
    }

    public boolean terminateReplicateTask(FlowAction action, InternalTask initiator,
            ChangedTasksInfo changesInfo, SchedulerStateUpdate frontend, TaskId taskId) {
        int runs = action.getDupNumber();

        logger.info("Control Flow Action REPLICATE (runs:" + runs + ")");
        List<InternalTask> toReplicate = new ArrayList<>();

        // find the tasks that need to be replicated
        for (InternalTask internalTask : internalJob.getIHMTasks().values()) {
            List<InternalTask> internalTaskDependencies = internalTask.getIDependences() == null
                    ? new ArrayList<InternalTask>() : internalTask.getIDependences();
            for (InternalTask internalTaskDependency : internalTaskDependencies) {
                if (isTheInitiatorTask(initiator, toReplicate, internalTask, internalTaskDependency)) {
                    if (runs < 1) {
                        skipReplication(initiator, changesInfo, internalTask);
                        break;
                    } else {
                        toReplicate.add(internalTask);
                    }
                }
            }
        }

        // for each initial task to replicate
        for (InternalTask internalTaskToReplicate : toReplicate) {

            // determine the target of the replication whether it is a block or
            // a single task
            InternalTask target = null;

            // target is a task block start : replication of the block
            if (internalTaskToReplicate.getFlowBlock().equals(FlowBlock.START)) {
                String tg = internalTaskToReplicate.getMatchingBlock();
                for (InternalTask internalTask : internalJob.getIHMTasks().values()) {
                    if (tg.equals(internalTask.getName()) &&
                        !(internalTask.getStatus().equals(TaskStatus.FINISHED) ||
                            internalTask.getStatus().equals(TaskStatus.SKIPPED)) &&
                        internalTask.dependsOn(internalTaskToReplicate)) {
                        target = internalTask;
                        break;
                    }
                }
                if (target == null) {
                    logger.error("REPLICATE: could not find matching block '" + tg + "'");
                    continue;
                }
            }
            // target is not a block : replication of the task
            else {
                target = internalTaskToReplicate;
            }

            // for each number of parallel run
            for (int i = 1; i < runs; i++) {

                // accumulates the tasks between the initiator and the target
                Map<TaskId, InternalTask> tasksBetweenInitiatorAndTarget = new HashMap<>();
                // replicate the tasks between the initiator and the target
                try {
                    target.replicateTree(tasksBetweenInitiatorAndTarget, internalTaskToReplicate.getId(),
                            false, initiator.getReplicationIndex() * runs, 0);

                } catch (Exception e) {
                    logger.error("REPLICATE: could not replicate tree", e);
                    break;
                }

                ((JobInfoImpl) internalJob.getJobInfo()).setNumberOfPendingTasks(
                        ((JobInfoImpl) internalJob.getJobInfo()).getNumberOfPendingTasks() +
                            tasksBetweenInitiatorAndTarget.size());

                // pointers to the new replicated tasks corresponding the begin
                // and
                // the end of the block ; can be the same
                InternalTask newTarget = null;
                InternalTask newEnd = null;

                // configure the new tasks
                for (InternalTask internalTask : tasksBetweenInitiatorAndTarget.values()) {
                    internalTask.setJobInfo(((JobInfoImpl) internalJob.getJobInfo()));
                    int dupIndex = getNextReplicationIndex(
                            InternalTask.getInitialName(internalTask.getName()),
                            internalTask.getIterationIndex());
                    internalJob.addTask(internalTask);
                    internalTask.setReplicationIndex(dupIndex);
                    assignReplicationTag(internalTask, initiator, false, action);
                }
                changesInfo.newTasksAdded(tasksBetweenInitiatorAndTarget.values());

                // find the beginning and the ending of the replicated block
                for (Entry<TaskId, InternalTask> tasksBetweenInitiatorAndTargetEntry : tasksBetweenInitiatorAndTarget
                        .entrySet()) {
                    InternalTask internalBlockTask = tasksBetweenInitiatorAndTargetEntry.getValue();

                    // connect the first task of the replicated block to the
                    // initiator
                    if (internalTaskToReplicate.getId()
                            .equals(tasksBetweenInitiatorAndTargetEntry.getKey())) {
                        newTarget = internalBlockTask;
                        newTarget.addDependence(initiator);
                        // no need to add newTarget to modifiedTasks
                        // because newTarget is among dup.values(), and we
                        // have added them all
                    }
                    // connect the last task of the block with the merge task(s)
                    if (target.getId().equals(tasksBetweenInitiatorAndTargetEntry.getKey())) {
                        newEnd = internalBlockTask;

                        List<InternalTask> toAdd = new ArrayList<>();
                        // find the merge tasks ; can be multiple
                        for (InternalTask internalTask : internalJob.getIHMTasks().values()) {
                            List<InternalTask> pdeps = internalTask.getIDependences();
                            if (pdeps != null) {
                                for (InternalTask parent : pdeps) {
                                    if (parent.getId().equals(target.getId())) {
                                        toAdd.add(internalTask);
                                    }
                                }
                            }
                        }
                        // connect the merge tasks
                        for (InternalTask internalTask : toAdd) {
                            internalTask.addDependence(newEnd);
                            changesInfo.taskUpdated(internalTask);
                        }
                    }
                }

                // propagate the changes on the JobDescriptor
                internalJob.getJobDescriptor().doReplicate(taskId, tasksBetweenInitiatorAndTarget, newTarget,
                        target.getId(), newEnd.getId());

            }
        }

        // notify frontend that tasks were added to the job
        ((JobInfoImpl) internalJob.getJobInfo()).setTasksChanges(changesInfo, internalJob);
        if (frontend != null) {
            frontend.jobStateUpdated(internalJob.getOwner(),
                    new NotificationData<>(SchedulerEvent.TASK_REPLICATED, internalJob.getJobInfo()));
            frontend.jobStateUpdated(internalJob.getOwner(),
                    new NotificationData<>(SchedulerEvent.TASK_SKIPPED, internalJob.getJobInfo()));
            frontend.jobUpdatedFullData(internalJob);
        }
        ((JobInfoImpl) internalJob.getJobInfo()).clearTasksChanges();

        // no jump is performed ; now that the tasks have been replicated and
        // configured, the flow can continue its normal operation
        internalJob.getJobDescriptor().terminate(taskId);

        return true;
    }

    private void skipReplication(InternalTask initiator, ChangedTasksInfo changesInfo,
            InternalTask internalTask) {
        long finishedTime = initiator.getFinishedTime() + 1;
        if (internalTask.getFlowBlock().equals(FlowBlock.START)) {
            skipTasksUntilEndBlock(changesInfo, internalTask, finishedTime);
        } else {
            skipTask(changesInfo, internalTask, finishedTime);
        }
    }

    private boolean isTheInitiatorTask(InternalTask initiator, List<InternalTask> toReplicate,
            InternalTask internalTask, InternalTask internalTaskDependency) {
        return internalTaskDependency.getId().equals(initiator.getId()) &&
            !toReplicate.contains(internalTask);
    }

    private void skipTasksUntilEndBlock(ChangedTasksInfo changesInfo, InternalTask blockTaskToSkip,
            long finishedTime) {
        skipTask(changesInfo, blockTaskToSkip, finishedTime);
        if (!blockTaskToSkip.getFlowBlock().equals(FlowBlock.END)) {
            for (InternalTask nextBlockTask : getTaskChildren(blockTaskToSkip)) {
                skipTasksUntilEndBlock(changesInfo, nextBlockTask, finishedTime);
            }
        }
    }

    private List<InternalTask> getTaskChildren(InternalTask internalTask) {
        return internalJob.getJobDescriptor().getTaskChildren(internalTask);

    }

    private void skipTask(ChangedTasksInfo changesInfo, InternalTask internalTaskToSkip, long finishedTime) {
        if (internalTaskToSkip.getStatus() != TaskStatus.SKIPPED) {
            internalTaskToSkip.setFinishedTime(finishedTime);
            internalTaskToSkip.setStatus(TaskStatus.SKIPPED);
            internalTaskToSkip.setExecutionDuration(0);
            changesInfo.taskSkipped(internalTaskToSkip);
            internalJob.setNumberOfPendingTasks(internalJob.getNumberOfPendingTasks() - 1);
            internalJob.setNumberOfFinishedTasks(internalJob.getNumberOfFinishedTasks() + 1);
            logger.info("Task " + internalTaskToSkip.getId() + " will not be executed");
        }
    }

    /**
     * Assign a tag to new duplicated task because of a REPLICATE or LOOP.
     * 
     * @param replicatedTask
     *            the new duplicated task.
     * @param initiator
     *            the initiator of the duplication.
     * @param loopAction
     *            true if the duplication if after a loop or, false if it is a
     *            replicate.
     * @param action
     *            the duplication action.
     */
    private void assignReplicationTag(InternalTask replicatedTask, InternalTask initiator, boolean loopAction,
            FlowAction action) {
        StringBuilder buf = new StringBuilder();

        if (loopAction) {
            buf.append("LOOP-");
            buf.append(InternalTask.getInitialName(initiator.getName()));
            if (initiator.getReplicationIndex() > 0) {
                buf.append("*");
                buf.append(initiator.getReplicationIndex());
            }
        } else {
            buf.append("REPLICATE-");
            buf.append(initiator.getName());
        }

        buf.append("-");

        if (loopAction) {
            String cronExpr = action.getCronExpr();
            if ("".equals(cronExpr)) {
                buf.append(replicatedTask.getIterationIndex());
            } else {
                // cron task: the replication index is the next date that
                // matches the cron expression
                Date resolvedCron = (new Predictor(cronExpr)).nextMatchingDate();
                SimpleDateFormat dt = new SimpleDateFormat("dd_MM_YY_HH_mm");
                buf.append(dt.format(resolvedCron));
            }
        } else {
            buf.append(replicatedTask.getReplicationIndex());
        }

        replicatedTask.setTag(buf.toString());
    }

    private int getNextReplicationIndex(String baseName, int iteration) {
        int rep = 0;
        for (InternalTask it : internalJob.getIHMTasks().values()) {
            String name = InternalTask.getInitialName(it.getName());
            if (baseName.equals(name) && iteration == it.getIterationIndex()) {
                rep = Math.max(rep, it.getReplicationIndex() + 1);
            }
        }
        return rep;
    }

}
