/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * This class represents a job for the policy.
 * The internal scheduler job is not sent to the policy.
 * Only a restricted number of properties on each jobs is sent to the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class JobDescriptorImpl implements JobDescriptor {

    public static final Logger logger = Logger.getLogger(JobDescriptorImpl.class);

    public static final String STARTING_TASK_RECOVERY_FOR_JOB = "Starting task recovery for job ";

    /** Job Id  */
    private JobId jobId;

    /** Job Priority */
    private JobPriority jobPriority;

    private transient InternalJob internalJob;

    /** List that knows which task has children and which have not */
    private Set<TaskId> hasChildren = new HashSet<>();

    /** Job tasks to be able to be schedule */
    @XmlTransient
    private Map<TaskId, EligibleTaskDescriptor> eligibleTasks = new ConcurrentHashMap<>();

    /** Those are not directly eligible, and will be triggered by an IF control flow action */
    @XmlTransient
    private Map<TaskId, EligibleTaskDescriptor> branchTasks = new ConcurrentHashMap<>();

    /** Job running tasks */
    public Map<TaskId, TaskDescriptor> runningTasks = new ConcurrentHashMap<>();

    /** Job paused tasks */
    private Map<TaskId, EligibleTaskDescriptor> pausedTasks = new HashMap<>();

    /** All tasks with their children */
    private final Map<InternalTask, TaskDescriptor> allTasksWithTheirChildren = new HashMap<>();

    private Map<String, String> genericInformation;

    private String owner;

    private Credentials credentials;

    /**
     * Create a new instance of job descriptor using an internal job.
     * Just make a mapping between some fields of the two type of job in order to
     * give it to the policy.
     * It ensures that the policy won't have bad activities on the real internal job.
     *
     * @param job the internal job to be lighted.
     */
    public JobDescriptorImpl(InternalJob job) {
        this.internalJob = job;
        this.jobPriority = getInternal().getPriority();
        this.jobId = getInternal().getId();
        this.genericInformation = job.getRuntimeGenericInformation();
        this.owner = job.getOwner();
        this.credentials = job.getCredentials();
        if (job.getType() == JobType.TASKSFLOW) {
            //build dependence tree
            makeTree(job);
        } else {
            //every tasks are eligible
            for (InternalTask td : job.getITasks()) {
                if (td.getStatus() == TaskStatus.SUBMITTED) {
                    eligibleTasks.put(td.getId(), new EligibleTaskDescriptorImpl(td));
                }
            }
        }
    }

    @Override
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Make a dependences tree of the job's tasks according to the dependence list
     * stored in taskDescriptor.
     * This list represents the ordered TaskDescriptor list of its parent tasks.
     */
    private void makeTree(InternalJob job) {

        // task's name which cannot be entry point
        // (because other tasks reference them in on of the if actions)
        final Set<String> nonEligibleTaskNames = job.getITasks()
                                                    .stream()
                                                    .filter(internalTask -> internalTask.getFlowScript() != null)
                                                    .filter(internalTask -> FlowActionType.parse(internalTask.getFlowScript()
                                                                                                             .getActionType())
                                                                                          .equals(FlowActionType.IF))
                                                    .flatMap(internalTask -> Stream.of(internalTask.getFlowScript()
                                                                                                   .getActionTarget(),
                                                                                       internalTask.getFlowScript()
                                                                                                   .getActionTargetElse(),
                                                                                       internalTask.getFlowScript()
                                                                                                   .getActionContinuation())
                                                                                   .filter(Objects::nonNull)
                                                                                   .filter(action -> !action.equals(internalTask.getName())))
                                                    .collect(Collectors.toSet());

        //create task descriptor list
        for (InternalTask td : job.getITasks()) {
            //if this task is a first task, put it in eligible tasks list
            EligibleTaskDescriptor lt = new EligibleTaskDescriptorImpl(td);

            if (isEntryPoint(td, nonEligibleTaskNames)) {
                eligibleTasks.put(td.getId(), lt);
            }

            if (td.getJoinedBranches() != null || td.getIfBranch() != null) {
                branchTasks.put(td.getId(), lt);
            }

            allTasksWithTheirChildren.put(td, lt);
        }

        //now for each taskDescriptor, set the parents and children list
        for (InternalTask td : job.getITasks()) {
            if (td.getDependences() != null) {
                TaskDescriptor taskDescriptor = allTasksWithTheirChildren.get(td);

                for (InternalTask depends : td.getIDependences()) {
                    ((EligibleTaskDescriptorImpl) taskDescriptor).addParent(allTasksWithTheirChildren.get(depends));
                }

                for (TaskDescriptor lt : taskDescriptor.getParents()) {
                    ((EligibleTaskDescriptorImpl) lt).addChild(taskDescriptor);
                    hasChildren.add(lt.getTaskId());
                }
            }
        }
    }

    /**
     * Tags all startable tasks as entry point
     * a startable task : has no dependency, and is not target of an if control flow action
     *
     * @param internalTask a Task
     * @param nonEligibleTaskNames set of task names which cannot be entry point
     * @return true if t is an entry point among all tasks in otherTasks, or false
     */
    private boolean isEntryPoint(InternalTask internalTask, Set<String> nonEligibleTaskNames) {
        List<TaskState> dependences = internalTask.getDependences();

        return (dependences == null || dependences.isEmpty()) && !nonEligibleTaskNames.contains(internalTask.getName());
    }

    /**
     * Return true if the task represented by the given taskId has children, false if not.
     *
     * @param taskId the id representing the real task.
     * @return true if the task represented by the given taskId has children, false if not.
     */
    public boolean hasChildren(TaskId taskId) {
        return hasChildren.contains(taskId);
    }

    /**
     * Delete this task from eligible task view and add it to running view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    public void start(TaskId taskId) {
        runningTasks.put(taskId, eligibleTasks.remove(taskId));
    }

    /**
     * Delete this task from running task view and add it to eligible view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    public void reStart(TaskId taskId) {
        eligibleTasks.put(taskId, (EligibleTaskDescriptor) runningTasks.remove(taskId));
    }

    /**
     * Complete LOOP action on JobDescriptor side
     *
     * @param initiator Task initiating the LOOP action
     * @param tree InternalTask tree of replicated tasks
     * @param target Target task of the LOOP action
     */
    public void doLoop(TaskId initiator, Map<TaskId, InternalTask> tree, InternalTask target, InternalTask newInit) {
        Map<TaskId, EligibleTaskDescriptorImpl> acc = new HashMap<>();

        // create new EligibleTasks and accumulate it
        for (Entry<TaskId, InternalTask> it : tree.entrySet()) {
            TaskId itId = it.getValue().getId();
            EligibleTaskDescriptorImpl td = new EligibleTaskDescriptorImpl(it.getValue());
            acc.put(itId, td);
        }

        EligibleTaskDescriptorImpl oldEnd = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
        if (oldEnd == null) {
            // can occur if a task is killed
            oldEnd = (EligibleTaskDescriptorImpl) eligibleTasks.get(initiator);
        }
        EligibleTaskDescriptorImpl newStart = acc.get(target.getId());
        EligibleTaskDescriptorImpl newEnd = acc.get(newInit.getId());

        // plug the end of the old tree (initiator) to the beginning of the new (target)
        for (TaskDescriptor ot : oldEnd.getChildren()) {
            newEnd.addChild(ot);
            ot.getParents().remove(oldEnd);
            ot.getParents().add(newEnd);
        }
        oldEnd.clearChildren();

        // recreate the dependencies
        for (Entry<TaskId, InternalTask> it : tree.entrySet()) {
            TaskId itId = it.getValue().getTaskInfo().getTaskId();
            EligibleTaskDescriptorImpl down = acc.get(itId);

            List<InternalTask> ideps = new ArrayList<>();
            int deptype = 0;
            if (it.getValue().hasDependences()) {
                ideps.addAll(it.getValue().getIDependences());
            }
            if (it.getValue().getIfBranch() != null) {
                deptype = 1;
                ideps.add(it.getValue().getIfBranch());
            }
            if (it.getValue().getJoinedBranches() != null) {
                deptype = 2;
                ideps.addAll(it.getValue().getJoinedBranches());
            }

            if (ideps.size() > 0 && !target.equals(itId)) {
                for (InternalTask parent : ideps) {
                    if (parent == null) {
                        continue;
                    }
                    EligibleTaskDescriptorImpl up = acc.get(parent.getTaskInfo().getTaskId());
                    switch (deptype) {
                        case 0:
                            if (parent.getId().equals(initiator)) {
                                up = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
                            }
                            up.addChild(down);
                            down.addParent(up);
                            break;
                        case 1:
                        case 2:
                            // 'weak' dependencies from FlowAction#IF are not
                            // represented in TaskDescriptor
                            branchTasks.put(down.getTaskId(), down);
                            break;
                    }

                }
            }
        }

        //    EligibleTaskDescriptorImpl newTask = (EligibleTaskDescriptorImpl) acc.get(target.getId());

        setNewLoopTaskToPausedIfJobIsPaused(newStart);

        putNewLoopTaskIntoPausedOrEligableList(target.getId(), newStart);

        runningTasks.remove(initiator);
    }

    private void putNewLoopTaskIntoPausedOrEligableList(TaskId taskid, EligibleTaskDescriptor newLoopTask) {
        if (((EligibleTaskDescriptorImpl) newLoopTask).getInternal().getStatus().equals(TaskStatus.PAUSED)) {
            pausedTasks.put(taskid, newLoopTask);
        } else {
            eligibleTasks.put(taskid, newLoopTask);
        }
    }

    private void setNewLoopTaskToPausedIfJobIsPaused(TaskDescriptor newLoopTask) {
        if (internalJob.getStatus() == JobStatus.PAUSED) {
            ((EligibleTaskDescriptorImpl) newLoopTask).getInternal().setStatus(TaskStatus.PAUSED);
        }
    }

    /**
     * Complete IF action on JobDescriptor side
     *
     * @param initiator Task initiating the IF action
     * @param branchStart START task of the IF branch
     * @param branchEnd END task of the IF branch
     * @param ifJoin JOIN task of the IF action, or null
     * @param elseTarget the START task of the ELSE branch that will not be executed
     * @param elseTasks list of tasks contained in the not executed ELSE branch
     */
    public void doIf(TaskId initiator, TaskId branchStart, TaskId branchEnd, TaskId ifJoin, TaskId elseTarget,
            List<InternalTask> elseTasks) {
        EligibleTaskDescriptorImpl init = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
        if (init == null) {
            // can occur if a task is killed
            init = (EligibleTaskDescriptorImpl) eligibleTasks.get(initiator);
        }
        EligibleTaskDescriptorImpl start = (EligibleTaskDescriptorImpl) branchTasks.get(branchStart);
        EligibleTaskDescriptorImpl end = null;
        EligibleTaskDescriptorImpl join = null;
        if (ifJoin != null) {
            join = (EligibleTaskDescriptorImpl) branchTasks.get(ifJoin);
        }

        // plug the initiator with the beginning of the IF block
        init.addChild(start);
        start.addParent(init);

        // the join task is optional
        if (join != null) {
            for (EligibleTaskDescriptor td : branchTasks.values()) {
                LinkedList<EligibleTaskDescriptorImpl> q = new LinkedList<>();
                q.offer((EligibleTaskDescriptorImpl) td);

                // find the matching end block task
                do {
                    EligibleTaskDescriptorImpl ptr = q.poll();

                    // if (ptr.getChildren() == null || ptr.getChildren().size() == 0) {
                    if (ptr.getTaskId().equals(branchEnd)) {
                        end = ptr;
                        break;
                    } else {
                        for (TaskDescriptor desc : ptr.getChildren()) {
                            if (!q.contains(desc)) {
                                q.offer((EligibleTaskDescriptorImpl) desc);
                            }
                        }
                    }
                } while (q.size() > 0);
                if (end != null) {
                    break;
                }
            }
            // plug the join task with the end of the if block
            join.addParent(end);
            end.addChild(join);
        }
        branchTasks.remove(start);
        if (join != null) {
            branchTasks.remove(join);
        }

        for (InternalTask it : elseTasks) {
            EligibleTaskDescriptorImpl td = (EligibleTaskDescriptorImpl) branchTasks.remove(it.getId());
            LinkedList<EligibleTaskDescriptorImpl> q = new LinkedList<>();
            if (td != null) {
                q.clear();
                q.offer(td);
                while (q.size() > 0) {
                    EligibleTaskDescriptorImpl ptr = q.poll();
                    ptr.setChildrenCount(0);
                    ptr.setCount(0);
                    if (ptr.getChildren() != null) {
                        for (TaskDescriptor child : ptr.getChildren()) {
                            q.offer((EligibleTaskDescriptorImpl) child);
                        }
                    }
                }
            }
        }
    }

    /**
     * Complete REPLICATE action on JobDescriptor side
     *
     * @param initiator Task initiating the REPLICATE action
     * @param tree InternalTask tree of replicated tasks
     * @param target Target task of the REPLICATE action: first task of the block
     * @param oldEnd End task of the replicated block ; original version
     * @param newEnd End task of the replicated block ; dup version
     */
    public void doReplicate(TaskId initiator, Map<TaskId, InternalTask> tree, InternalTask target, TaskId oldEnd,
            TaskId newEnd) {
        Map<TaskId, EligibleTaskDescriptorImpl> acc = new HashMap<>();

        // create new EligibleTasks and accumulate it
        for (Entry<TaskId, InternalTask> it : tree.entrySet()) {
            TaskId itId = it.getValue().getTaskInfo().getTaskId();
            EligibleTaskDescriptorImpl td = new EligibleTaskDescriptorImpl(it.getValue());
            acc.put(itId, td);
        }

        // recreate the dependencies
        for (Entry<TaskId, InternalTask> it : tree.entrySet()) {
            TaskId itId = it.getValue().getTaskInfo().getTaskId();
            EligibleTaskDescriptorImpl down = acc.get(itId);

            List<InternalTask> ideps = new ArrayList<>();
            int deptype = 0;
            if (it.getValue().hasDependences()) {
                ideps.addAll(it.getValue().getIDependences());
            } else if (it.getValue().getIfBranch() != null) {
                deptype = 1;
                ideps.add(it.getValue().getIfBranch());
            } else if (it.getValue().getJoinedBranches() != null) {
                deptype = 2;
                ideps.addAll(it.getValue().getJoinedBranches());
            }
            if (ideps != null && !target.equals(itId)) {
                for (InternalTask parent : ideps) {
                    if (parent == null) {
                        continue;
                    }
                    EligibleTaskDescriptorImpl up = acc.get(parent.getId());
                    if (up == null) {
                        continue;
                    }
                    switch (deptype) {
                        case 0:
                            up.addChild(down);
                            down.addParent(up);
                            break;
                        case 1:
                        case 2:
                            branchTasks.put(down.getTaskId(), down);
                            break;
                    }
                }
            }
        }

        EligibleTaskDescriptorImpl oldTask = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
        EligibleTaskDescriptorImpl newTask = acc.get(target.getId());
        if (oldTask == null) {
            oldTask = (EligibleTaskDescriptorImpl) eligibleTasks.get(initiator);
        }
        HashSet<TaskId> excl = new HashSet<>();
        EligibleTaskDescriptorImpl endTask = (EligibleTaskDescriptorImpl) findTask(oldTask, oldEnd, excl);
        if (endTask == null) {
            // findTask cannot walk weak dependencies (IF/ELSE) down, lets walk these branches ourselves
            for (TaskDescriptor branch : branchTasks.values()) {
                endTask = (EligibleTaskDescriptorImpl) findTask(branch, oldEnd, excl);
                if (endTask != null) {
                    break;
                }
            }
        }
        EligibleTaskDescriptorImpl end = acc.get(newEnd);

        for (TaskDescriptor t : endTask.getChildren()) {
            end.addChild(t);
            ((EligibleTaskDescriptorImpl) t).addParent(end);
        }

        newTask.addParent(oldTask);
        oldTask.addChild(newTask);

        eligibleTasks.put(target.getId(), newTask);
    }

    /**
     * Find a task given a parent task
     *
     * @param haystack task from which the child subtree will be walked
     * @param needle task to find in <code>haystack's</code> subtree
     * @return the TaskDescriptor corresponding <code>needle</code> if it is
     *  a child of <code>haystack's</code>, or null
     */
    private TaskDescriptor findTask(TaskDescriptor haystack, TaskId needle, HashSet<TaskId> excl) {
        if (excl.contains(haystack.getTaskId()))
            return null;

        excl.add(haystack.getTaskId());

        if (needle.equals(haystack.getTaskId())) {
            return haystack;
        }
        for (TaskDescriptor td : haystack.getChildren()) {
            if (needle.equals(td.getTaskId())) {
                return td;
            }
            TaskDescriptor ttd = findTask(td, needle, excl);
            if (ttd != null) {
                return ttd;
            }
        }
        return null;
    }

    /**
     * Update the eligible list of task and dependencies if necessary.
     * This function considered that the taskId is in eligible task list.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task to remove from running task.
     */
    public void terminate(TaskId taskId) {
        terminate(taskId, false);
    }

    /**
     * Update the eligible list of task and dependencies if necessary.
     * This function considered that the taskId is in eligible task list.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task to remove from running task.
     */
    public void terminate(TaskId taskId, boolean inErrorTask) {

        Map<TaskId, ? extends TaskDescriptor> currentTasks = inErrorTask ? pausedTasks : runningTasks;

        List<TaskId> taskIdsToSkip = new ArrayList<>();

        if (getInternal().getType() == JobType.TASKSFLOW) {
            TaskDescriptor taskToTerminate = currentTasks.get(taskId);
            if (taskToTerminate == null && !inErrorTask) {
                // occurs when a task is killed in pending state
                taskToTerminate = eligibleTasks.remove(taskId);
                if (taskToTerminate != null) {
                    runningTasks.put(taskId, taskToTerminate);
                }
            }

            if (taskToTerminate != null) {

                // if it is IF condition, and it is skipped, then we just skip all branches
                InternalTask internalTask = ((EligibleTaskDescriptorImpl) taskToTerminate).getInternal();
                if (internalTask.getFlowScript() != null &&
                    FlowActionType.parse(internalTask.getFlowScript().getActionType()).equals(FlowActionType.IF) &&
                    internalTask.getStatus().equals(TaskStatus.SKIPPED)) {
                    Set<EligibleTaskDescriptor> branches = new HashSet<>(3);
                    branches.add(getTaskByName(internalTask.getFlowScript().getActionTarget()));
                    branches.add(getTaskByName(internalTask.getFlowScript().getActionTargetElse()));
                    branches.add(getTaskByName(internalTask.getFlowScript().getActionContinuation()));
                    for (EligibleTaskDescriptor childTask : branches) {
                        runningTasks.put(childTask.getTaskId(), childTask);
                        taskIdsToSkip.add(childTask.getTaskId());
                    }

                } else if (!taskToTerminate.getChildren().isEmpty()) {
                    for (TaskDescriptor childTask : taskToTerminate.getChildren()) {
                        decreaseParentCount(childTask);

                        if (((EligibleTaskDescriptorImpl) childTask).getCount() == 0) {
                            if (internalJob.getStatus() == JobStatus.PAUSED) {
                                pausedTasks.put(childTask.getTaskId(), (EligibleTaskDescriptor) childTask);
                            } else if (internalJob.getStatus() == JobStatus.IN_ERROR &&
                                       ((EligibleTaskDescriptorImpl) childTask).getInternal()
                                                                               .getStatus() == TaskStatus.PAUSED) {
                                pausedTasks.put(childTask.getTaskId(), (EligibleTaskDescriptor) childTask);
                            } else if (((EligibleTaskDescriptorImpl) childTask).getInternal()
                                                                               .getStatus() == TaskStatus.SKIPPED) {
                                runningTasks.put(childTask.getTaskId(), childTask);
                                taskIdsToSkip.add(childTask.getTaskId());
                            } else {
                                eligibleTasks.put(childTask.getTaskId(), (EligibleTaskDescriptor) childTask);
                            }
                        }

                    }
                }

                decreaseChildrenCountForAllParents(taskToTerminate);
            }
        }

        currentTasks.remove(taskId);

        for (TaskId taskIdToSkip : taskIdsToSkip) {
            terminate(taskIdToSkip);
        }

    }

    public EligibleTaskDescriptor getTaskByName(String taskName) {
        return (EligibleTaskDescriptor) allTasksWithTheirChildren.values()
                                                                 .stream()
                                                                 .filter(x -> x.getTaskId()
                                                                               .getReadableName()
                                                                               .equals(taskName))
                                                                 .findFirst()
                                                                 .get();
    }

    public InternalTask getInternalTaskByName(String taskName) {
        return allTasksWithTheirChildren.keySet().stream().filter(x -> x.getName().equals(taskName)).findFirst().get();
    }

    private void decreaseParentCount(TaskDescriptor childTask) {
        ((EligibleTaskDescriptorImpl) childTask).setCount(((EligibleTaskDescriptorImpl) childTask).getCount() - 1);
    }

    private void decreaseChildrenCountForAllParents(TaskDescriptor taskToTerminate) {
        for (TaskDescriptor parentTask : taskToTerminate.getParents()) {
            ((EligibleTaskDescriptorImpl) parentTask).setChildrenCount(parentTask.getChildrenCount() - 1);
        }
    }

    public void recoverTask(TaskId taskId) {
        EligibleTaskDescriptor taskToRun = eligibleTasks.remove(taskId);
        if (taskToRun == null) {
            taskToRun = pausedTasks.remove(taskId);
        }
        if (taskToRun != null) {
            runningTasks.put(taskId, taskToRun);
            terminate(taskId);
        }
    }

    /**
     * Failed this job descriptor by removing every tasks from eligible and running list.
     * This function considered that the taskIds are in eligible tasks list.
     * Visibility is package because user cannot use this method.
     */
    public void failed() {
        eligibleTasks.clear();
        runningTasks.clear();
    }

    public void pause(TaskId taskId) {
        if (getInternal().getType() == JobType.TASKSFLOW) {
            EligibleTaskDescriptor eligibleTaskDescriptor = eligibleTasks.remove(taskId);

            if (eligibleTaskDescriptor != null) {
                pausedTasks.put(taskId, eligibleTaskDescriptor);
            }
        }
    }

    public void pausedTaskOnError(TaskId taskId) {
        if (getInternal().getType() == JobType.TASKSFLOW) {
            TaskDescriptor taskDescriptor = runningTasks.remove(taskId);

            if (taskDescriptor != null) {
                pausedTasks.put(taskId, (EligibleTaskDescriptor) taskDescriptor);
            }
        }
    }

    public void unpause(TaskId taskId) {
        if (getInternal().getType() == JobType.TASKSFLOW) {
            EligibleTaskDescriptor eligibleTaskDescriptor = pausedTasks.remove(taskId);

            if (eligibleTaskDescriptor != null) {
                eligibleTasks.put(taskId, eligibleTaskDescriptor);
            }
        }
    }

    public EligibleTaskDescriptor removePausedTask(TaskId taskId) {
        if (getInternal().getType() == JobType.TASKSFLOW) {
            return pausedTasks.remove(taskId);
        }
        return null;
    }

    public void updateTaskScheduledTime(TaskId taskId, long scheduledTime) {
        if (getInternal().getType() == JobType.TASKSFLOW) {
            EligibleTaskDescriptor eligibleTaskDescriptor = eligibleTasks.get(taskId);

            if (eligibleTaskDescriptor != null) {
                ((EligibleTaskDescriptorImpl) eligibleTaskDescriptor).getInternal().setScheduledTime(scheduledTime);
            }
        }
    }

    public void restoreInErrorTasks() {
        final Iterator<Entry<TaskId, EligibleTaskDescriptor>> iterator = eligibleTasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<TaskId, EligibleTaskDescriptor> entry = iterator.next();
            TaskId taskId = entry.getKey();
            EligibleTaskDescriptor task = entry.getValue();

            if (((EligibleTaskDescriptorImpl) task).getInternal().getStatus() == TaskStatus.IN_ERROR) {
                pausedTasks.put(taskId, task);
                iterator.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void restoreRunningTasks() {
        final String performanceTestOngoing = System.getProperty("performanceTestOngoing");
        if (performanceTestOngoing != null && performanceTestOngoing.equalsIgnoreCase("true")) {
            logger.info(STARTING_TASK_RECOVERY_FOR_JOB + jobId);
        }

        final Iterator<Entry<TaskId, EligibleTaskDescriptor>> iterator = eligibleTasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<TaskId, EligibleTaskDescriptor> entry = iterator.next();
            TaskId taskId = entry.getKey();
            EligibleTaskDescriptor task = entry.getValue();

            if (((EligibleTaskDescriptorImpl) task).getInternal().getStatus() == TaskStatus.RUNNING) {
                logger.debug("Move task " + taskId + " from eligible tasks to running tasks");
                runningTasks.put(taskId, task);
                iterator.remove();
            }
        }
    }

    /**
     * Get a task descriptor that is in the running task.
     *
     * @param id the id of the task descriptor to retrieve.
     * @return the task descriptor associated to this id, or null if not running.
     */
    public TaskDescriptor GetRunningTaskDescriptor(TaskId id) {
        return runningTasks.get(id);
    }

    /**
     * To get the tasks.
     *
     * @return the tasks.
     */
    @XmlTransient
    public Collection<TaskDescriptor> getEligibleTasks() {
        return new Vector<TaskDescriptor>(eligibleTasks.values());
    }

    /**
     * {@inheritDoc}
     */
    public JobId getJobId() {
        return jobId;
    }

    /**
     * {@inheritDoc}
     */
    public JobPriority getJobPriority() {
        return jobPriority;
    }

    /**
     * {@inheritDoc}
     */
    public InternalJob getInternal() {
        return internalJob;
    }

    /**
     * {@inheritDoc}
     */
    public Map<TaskId, TaskDescriptor> getRunningTasks() {
        return runningTasks;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("squid:S1452") // it makes sonar to ignore "Generic wildcard types should not be used in return parameters" rule.
    public Map<TaskId, ? extends TaskDescriptor> getPausedTasks() {
        return pausedTasks;
    }

    /**
     * This method is used to find all task that should be skiped when REPS is 0
     * We do BFS to mark all task in the block which has to be skipped.
     * We return children and branches if it IF confition
     */
    public List<InternalTask> getTaskChildrenWithIfBranches(InternalTask internalTask) {
        List<InternalTask> children = new ArrayList<>();
        if (allTasksWithTheirChildren.containsKey(internalTask)) {
            for (TaskDescriptor taskDescriptor : allTasksWithTheirChildren.get(internalTask).getChildren()) {
                children.add(((EligibleTaskDescriptorImpl) taskDescriptor).getInternal());
            }
        }

        if (internalTask.getFlowScript() != null &&
            FlowActionType.parse(internalTask.getFlowScript().getActionType()).equals(FlowActionType.IF)) {
            children.add(getInternalTaskByName(internalTask.getFlowScript().getActionTarget()));
            children.add(getInternalTaskByName(internalTask.getFlowScript().getActionTargetElse()));
            children.add(getInternalTaskByName(internalTask.getFlowScript().getActionContinuation()));
        }

        return children;
    }

    /**
     * {@inheritDoc}
     *
     * Compare jobs on Priority
     */
    public int compareTo(JobDescriptor job) {
        return job.getJobPriority().compareTo(getJobPriority());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JobDescriptor(" + getJobId() + ")";
    }

}
