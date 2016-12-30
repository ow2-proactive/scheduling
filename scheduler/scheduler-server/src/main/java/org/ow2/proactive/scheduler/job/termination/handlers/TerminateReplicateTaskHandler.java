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
        for (InternalTask ti : internalJob.getIHMTasks().values()) {
            List<InternalTask> tl = ti.getIDependences();
            if (tl != null) {
                for (InternalTask ts : tl) {
                    if (ts.getId().equals(initiator.getId()) && !toReplicate.contains(ti)) {
                        if (runs < 1) {
                            long finishedTime = initiator.getFinishedTime() + 1;
                            if (ti.getFlowBlock().equals(FlowBlock.START)) {
                                skipAllblock(changesInfo, ti, finishedTime);
                            } else {
                                skipTask(changesInfo, ti, finishedTime);
                            }
                        } else {
                            // ti needs to be replicated
                            toReplicate.add(ti);
                        }
                    }
                }
            }
        }

        // for each initial task to replicate
        for (InternalTask todup : toReplicate) {

            // determine the target of the replication whether it is a block or
            // a single task
            InternalTask target = null;

            // target is a task block start : replication of the block
            if (todup.getFlowBlock().equals(FlowBlock.START)) {
                String tg = todup.getMatchingBlock();
                for (InternalTask t : internalJob.getIHMTasks().values()) {
                    if (tg.equals(t.getName()) && !(t.getStatus().equals(TaskStatus.FINISHED) ||
                        t.getStatus().equals(TaskStatus.SKIPPED)) && t.dependsOn(todup)) {
                        target = t;
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
                target = todup;
            }

            // for each number of parallel run
            for (int i = 1; i < runs; i++) {

                // accumulates the tasks between the initiator and the target
                Map<TaskId, InternalTask> dup = new HashMap<>();
                // replicate the tasks between the initiator and the target
                try {
                    target.replicateTree(dup, todup.getId(), false, initiator.getReplicationIndex() * runs,
                            0);

                } catch (Exception e) {
                    logger.error("REPLICATE: could not replicate tree", e);
                    break;
                }

                ((JobInfoImpl) internalJob.getJobInfo()).setNumberOfPendingTasks(
                        ((JobInfoImpl) internalJob.getJobInfo()).getNumberOfPendingTasks() + dup.size());

                // pointers to the new replicated tasks corresponding the begin
                // and
                // the end of the block ; can be the same
                InternalTask newTarget = null;
                InternalTask newEnd = null;

                // configure the new tasks
                for (Entry<TaskId, InternalTask> it : dup.entrySet()) {
                    InternalTask nt = it.getValue();
                    nt.setJobInfo(((JobInfoImpl) internalJob.getJobInfo()));
                    int dupIndex = getNextReplicationIndex(InternalTask.getInitialName(nt.getName()),
                            nt.getIterationIndex());
                    internalJob.addTask(nt);
                    nt.setReplicationIndex(dupIndex);
                    assignReplicationTag(nt, initiator, false, action);
                }
                changesInfo.newTasksAdded(dup.values());

                // find the beginning and the ending of the replicated block
                for (Entry<TaskId, InternalTask> it : dup.entrySet()) {
                    InternalTask nt = it.getValue();

                    // connect the first task of the replicated block to the
                    // initiator
                    if (todup.getId().equals(it.getKey())) {
                        newTarget = nt;
                        newTarget.addDependence(initiator);
                        // no need to add newTarget to modifiedTasks
                        // because newTarget is among dup.values(), and we
                        // have added them all
                    }
                    // connect the last task of the block with the merge task(s)
                    if (target.getId().equals(it.getKey())) {
                        newEnd = nt;

                        List<InternalTask> toAdd = new ArrayList<>();
                        // find the merge tasks ; can be multiple
                        for (InternalTask t : internalJob.getIHMTasks().values()) {
                            List<InternalTask> pdeps = t.getIDependences();
                            if (pdeps != null) {
                                for (InternalTask parent : pdeps) {
                                    if (parent.getId().equals(target.getId())) {
                                        toAdd.add(t);
                                    }
                                }
                            }
                        }
                        // connect the merge tasks
                        for (InternalTask t : toAdd) {
                            t.addDependence(newEnd);
                            changesInfo.taskUpdated(t);
                        }
                    }
                }

                // propagate the changes on the JobDescriptor
                internalJob.getJobDescriptor().doReplicate(taskId, dup, newTarget, target.getId(),
                        newEnd.getId());

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

    private void skipAllblock(ChangedTasksInfo changesInfo, InternalTask ti, long finishedTime) {
        InternalTask endBlock = null;
        for (InternalTask t : internalJob.getIHMTasks().values()) {
            if (t.getFlowBlock().equals(FlowBlock.END)) {
                endBlock = t;
                skipTask(changesInfo, endBlock, finishedTime);
                break;
            }
        }
        InternalTask previousTaskInTheBlock = endBlock;
        while (previousTaskInTheBlock.getId() != ti.getId()) {
            for (InternalTask previousBlock : previousTaskInTheBlock.getIDependences()) {
                skipTask(changesInfo, previousBlock, finishedTime);
                previousTaskInTheBlock = previousBlock;
            }
        }

    }

    /**
     * @param changesInfo 
     * @param ti
     * @param initiator 
     */
    private void skipTask(ChangedTasksInfo changesInfo, InternalTask ti, long finishedTime) {
        ti.setFinishedTime(finishedTime);
        ti.setStatus(TaskStatus.SKIPPED);
        ti.setExecutionDuration(0);
        changesInfo.taskSkipped(ti);
        internalJob.setNumberOfPendingTasks(internalJob.getNumberOfPendingTasks() - 1);
        internalJob.setNumberOfFinishedTasks(internalJob.getNumberOfFinishedTasks() + 1);
        logger.info("Task " + ti.getId() + " will not be executed");

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
