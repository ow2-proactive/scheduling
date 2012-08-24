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
package org.ow2.proactive.scheduler.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
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

    private InternalJob internalJob;

    /** List that knows which task has children and which have not */
    private Set<TaskId> hasChildren = new HashSet<TaskId>();

    /** Job tasks to be able to be schedule */
    @XmlTransient
    private Map<TaskId, EligibleTaskDescriptor> eligibleTasks = new ConcurrentHashMap<TaskId, EligibleTaskDescriptor>();

    /** Those are not directly eligible, and will be triggered by an IF control flow action */
    @XmlTransient
    private Map<TaskId, EligibleTaskDescriptor> branchTasks = new ConcurrentHashMap<TaskId, EligibleTaskDescriptor>();

    /** Job running tasks */
    private Map<TaskId, TaskDescriptor> runningTasks = new ConcurrentHashMap<TaskId, TaskDescriptor>();

    /** Job paused tasks */
    private Map<TaskId, TaskDescriptor> pausedTasks = new HashMap<TaskId, TaskDescriptor>();

    /**
     * Create a new instance of job descriptor using an internal job.
     * Just make a mapping between some fields of the two type of job in order to
     * give it to the policy.
     * It ensures that the policy won't have bad activities on the real internal job.
     *
     * @param job the internal job to be lighted.
     */
    public JobDescriptorImpl(InternalJob job) {
        internalJob = job;

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

    /**
     * Make a dependences tree of the job's tasks according to the dependence list
     * stored in taskDescriptor.
     * This list represents the ordered TaskDescriptor list of its parent tasks.
     */
    private void makeTree(InternalJob job) {
        Map<InternalTask, TaskDescriptor> mem = new HashMap<InternalTask, TaskDescriptor>();

        //create task descriptor list
        for (InternalTask td : job.getITasks()) {
            //if this task is a first task, put it in eligible tasks list
            EligibleTaskDescriptor lt = new EligibleTaskDescriptorImpl(td);

            if (isEntryPoint(td, job.getITasks())) {
                eligibleTasks.put(td.getId(), lt);
            }

            if (td.getJoinedBranches() != null || td.getIfBranch() != null) {
                branchTasks.put(td.getId(), lt);
            }

            mem.put(td, lt);
        }

        //now for each taskDescriptor, set the parents and children list
        for (InternalTask td : job.getITasks()) {
            if (td.getDependences() != null) {
                TaskDescriptor taskDescriptor = mem.get(td);

                for (InternalTask depends : td.getIDependences()) {
                    ((EligibleTaskDescriptorImpl) taskDescriptor).addParent(mem.get(depends));
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
     * @param t a Task
     * @param otherTasks the other tasks contained in the job containing task t
     * @return true if t is an entry point among all tasks in otherTasks, or false
     */
    private boolean isEntryPoint(InternalTask t, List<InternalTask> otherTasks) {
        List<TaskState> deps = t.getDependences();
        boolean entryPoint = false;

        // an entry point has no dependency
        if (deps == null || deps.size() == 0) {
            entryPoint = true;
        } else {
            return false;
        }

        // a entry point is not target of an if
        for (Task t2 : otherTasks) {
            if (t.equals(t2)) {
                continue;
            }
            FlowScript sc = t2.getFlowScript();
            if (sc != null) {
                String actionType = sc.getActionType();
                if (FlowActionType.parse(actionType).equals(FlowActionType.IF)) {
                    String tIf = sc.getActionTarget();
                    String tElse = sc.getActionTargetElse();
                    String tJoin = sc.getActionContinuation();
                    if (tIf != null && tIf.equals(t.getName())) {
                        return false;
                    }
                    if (tElse != null && tElse.equals(t.getName())) {
                        return false;
                    }
                    if (tJoin != null && tJoin.equals(t.getName())) {
                        return false;
                    }
                }
            }
        }
        return entryPoint;
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
    public void doLoop(TaskId initiator, Map<TaskId, InternalTask> tree, InternalTask target,
            InternalTask newInit) {
        Map<TaskId, EligibleTaskDescriptorImpl> acc = new HashMap<TaskId, EligibleTaskDescriptorImpl>();

        // create new EligibleTasks and accumulate it
        for (Entry<TaskId, InternalTask> it : tree.entrySet()) {
            TaskId itId = it.getValue().getId();
            EligibleTaskDescriptorImpl td = new EligibleTaskDescriptorImpl(it.getValue());
            acc.put(itId, td);
        }

        EligibleTaskDescriptorImpl oldEnd = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
        EligibleTaskDescriptorImpl newStart = (EligibleTaskDescriptorImpl) acc.get(target.getId());
        EligibleTaskDescriptorImpl newEnd = (EligibleTaskDescriptorImpl) acc.get(newInit.getId());

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

            List<InternalTask> ideps = new ArrayList<InternalTask>();
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

        eligibleTasks.put(target.getId(), newStart);

        runningTasks.remove(initiator);
    }

    /**
     * Complete IF action on JobDescriptor side
     *
     * @param initiator Task initiating the IF action
     * @param branchStart START task of the IF branch
     * @param branchEnd END task of the IF branch
     * @param join JOIN task of the IF action, or null
     * @param elseTarget the START task of the ELSE branch that will not be executed
     * @param elseTasks list of tasks contained in the not executed ELSE branch
     */
    public void doIf(TaskId initiator, TaskId branchStart, TaskId branchEnd, TaskId ifJoin,
            TaskId elseTarget, List<InternalTask> elseTasks) {
        EligibleTaskDescriptorImpl init = (EligibleTaskDescriptorImpl) runningTasks.get(initiator);
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
                LinkedList<EligibleTaskDescriptorImpl> q = new LinkedList<EligibleTaskDescriptorImpl>();
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
            LinkedList<EligibleTaskDescriptorImpl> q = new LinkedList<EligibleTaskDescriptorImpl>();
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
    public void doReplicate(TaskId initiator, Map<TaskId, InternalTask> tree, InternalTask target,
            TaskId oldEnd, TaskId newEnd) {
        Map<TaskId, EligibleTaskDescriptorImpl> acc = new HashMap<TaskId, EligibleTaskDescriptorImpl>();

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

            List<InternalTask> ideps = new ArrayList<InternalTask>();
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
        EligibleTaskDescriptorImpl newTask = (EligibleTaskDescriptorImpl) acc.get(target.getId());
        if (oldTask == null) {
            oldTask = (EligibleTaskDescriptorImpl) eligibleTasks.get(initiator);
        }
        HashSet<TaskId> excl = new HashSet<TaskId>();
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
        EligibleTaskDescriptorImpl end = (EligibleTaskDescriptorImpl) acc.get(newEnd);

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
        if (getInternal().getType() == JobType.TASKSFLOW) {
            TaskDescriptor lt = runningTasks.get(taskId);

            if (lt != null) {
                for (TaskDescriptor task : lt.getChildren()) {
                    ((EligibleTaskDescriptorImpl) task).setCount(((EligibleTaskDescriptorImpl) task)
                            .getCount() - 1);

                    if (((EligibleTaskDescriptorImpl) task).getCount() == 0) {
                        eligibleTasks.put(task.getTaskId(), (EligibleTaskDescriptor) task);
                    }
                }

                for (TaskDescriptor task : lt.getParents()) {
                    ((EligibleTaskDescriptorImpl) task).setChildrenCount(task.getChildrenCount() - 1);
                }
            }
        }
        runningTasks.remove(taskId);
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

    /**
     * Update the list of eligible tasks according to the status of each task.
     * This method is called only if user pause a job.
     *
     * @param taskStatus the taskId with their current status.
     */
    public void update(Map<TaskId, TaskStatus> taskStatus) {
        for (Entry<TaskId, TaskStatus> tid : taskStatus.entrySet()) {
            if (tid.getValue() == TaskStatus.PAUSED) {
                TaskDescriptor lt = eligibleTasks.get(tid.getKey());

                if (lt != null) {
                    pausedTasks.put(tid.getKey(), eligibleTasks.remove(tid.getKey()));
                }
            } else if ((tid.getValue() == TaskStatus.PENDING) || (tid.getValue() == TaskStatus.SUBMITTED)) {
                EligibleTaskDescriptor lt = (EligibleTaskDescriptor) pausedTasks.get(tid.getKey());

                if (lt != null) {
                    eligibleTasks.put(tid.getKey(), lt);
                    pausedTasks.remove(tid.getKey());
                }
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
    public Collection<EligibleTaskDescriptor> getEligibleTasks() {
        return new Vector<EligibleTaskDescriptor>(eligibleTasks.values());
    }

    /**
     * {@inheritDoc}
     */
    public JobId getJobId() {
        return getInternal().getId();
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
    public Map<TaskId, TaskDescriptor> getPausedTasks() {
        return pausedTasks;
    }

    /**
     * {@inheritDoc}
     *
     * Compare jobs on Priority
     */
    public int compareTo(JobDescriptor job) {
        return job.getInternal().getPriority().compareTo(getInternal().getPriority());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JobDescriptor(" + getJobId() + ")";
    }

}
