/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.calcium.task;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobalImpl;


/**
 * This class represents the task pool.
 * It handles the tasks that must be solved, which can be in
 * one of the following states: ready, waiting, processing, results(finished).
 *
 * Data parallelism is represented as child tasks of a parent task. This class
 * handles the proper enqueing of tasks (child or parent).
 *
 * @author The ProActive Team
 *
 */
public class TaskPool implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_KERNEL);

    //State Queues
    private ReadyQueue ready; //Tasks ready for execution
    private Hashtable<TaskId, Task<?>> waiting; //Tasks waiting for subtasks completition
    private Vector<Task<?>> results; //Finished root-tasks
    private Hashtable<TaskId, Task<?>> processing; //Tasks being processed at this moment
    private PanicException panicException; //In case the system colapses
    private StatsGlobalImpl stats; //Statistics

    public TaskPool() {
        this.ready = new ReadyQueue();
        this.waiting = new Hashtable<TaskId, Task<?>>();
        this.results = new Vector<Task<?>>();
        this.processing = new Hashtable<TaskId, Task<?>>();
        this.stats = new StatsGlobalImpl();
        this.panicException = null;
    }

    public synchronized Task<?> getResult() throws PanicException {
        while ((results.size() <= 0) && !isPaniqued()) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Thread waiting for results:" + Thread.currentThread().getId());
                }
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (isPaniqued()) {
            notifyAll();
            throw panicException;
        }

        Task<?> resultTask = results.remove(0);
        resultTask.getStats().exitResultsState();

        stats.increaseSolvedTasks(resultTask);

        return resultTask;
    }

    /**
     * This method is gets a task ready for execution.
     * If there are no ready tasks, then this method can do two things:
     * 1. Block until a ready task is available (only if there are taks being processed).
     * 2. Return null if no tasks are available.
     * @param timeout specifies the number of milliseconds to wait for a task.
     * If the timeout is smaller or equal to cero, then it waits indefenetly.
     * @return A task ready for execution or null.
     */
    public synchronized Task<?> getReadyTask(long timeout) {
        long lastinit = System.currentTimeMillis();
        timeout = (timeout > 0) ? timeout : 0; //if timeout<0 => timeout=0

        while (ready.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for ready task:" + Thread.currentThread().getId());
            }

            try {
                wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long newinit = System.currentTimeMillis();
            timeout -= (newinit - lastinit);
            lastinit = newinit;

            if (timeout <= 0) {
                return null;
            }
        }

        Task<?> task = ready.poll(); //get the highest priority task
        task.getStats().exitReadyState();

        processing.put(task.taskId, task);
        if (logger.isDebugEnabled()) {
            logger.debug("Serving taskId=" + task);
        }

        return task;
    }

    public synchronized Vector<Task> getReadyTasks(long timeout) {
        Task t = getReadyTask(timeout);
        if (t == null) {
            return new Vector<Task>(0);
        }

        Vector<Task> v = ready.getBrotherTasks(t); //get the highest priority tasks

        for (Task task : v) {
            task.getStats().exitReadyState();
            if (logger.isDebugEnabled()) {
                logger.debug("Serving taskId=" + task);
            }
            processing.put(task.taskId, task);
        }

        return v;
    }

    /**
     * Adds new root tasks to the task pool
     * @param task The root task that will be added
     */
    public synchronized void addReadyRootTask(Task<?> task) {
        //TODO uncomment this !
        //if(isPaniqued()) throw panicException;
        if (processing.contains(task) || ready.contains(task)) {
            logger.error("Dropping duplicated taskId=" + task.taskId.value());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Enqueing new root taskId=" + task);
        }

        ready.addNewRoot(task);
        notifyAll();
    }

    public void putProcessedTask(Vector<Task> taskV) {
        for (Task t : taskV) {
            putProcessedTask(t);
        }
    }

    public synchronized void putProcessedTask(Task<?> task) {
        Task<?> processingTask = processing.remove(task.taskId);

        //get a snapshot of the current used resources
        task.getStats().setMaxAvailableResources(stats.getProccessingQueueLength());

        if (processingTask == null) {
            logger.error("Dropping Task, since it was not being processed taskId=" + task);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Updating taskId=" + task);
        }

        //The task can be tainted by exceptions on other family members
        if ((processingTask != null) && processingTask.isTainted()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dropping tainted taskId=" + task);
            }
            return;
        }

        task.getStats().exitProcessingState();

        if (task.hasException()) {
            updateExceptionedTask(task);
        } else if (task.isFinished()) {
            updateFinishedTask(task);
        } else {
            updateTask(task);
        }

        if (!ready.isEmpty()) {
            //if(!ready.isEmpty() || results.size()!=0){
            notifyAll();
        }
    }

    public synchronized boolean isPaniqued() {
        return this.panicException != null;
    }

    private void updateExceptionedTask(Task<?> task) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Exceptioned Task taskId=" + task);
        }

        Exception e = task.getException();

        if (e instanceof PanicException) {
            panic((PanicException) e); //Panic exception
            return;
        }

        if (e instanceof RuntimeException) { //Fatal Exception
            deleteTaskFamilyFromQueues(task);
            return;
        }

        //TODO handle Scheduling Exceptions

        //Else: handle regular exceptions
        if (task.isFinished()) {
            String msg = "Panic Error. Task with exceptions cannot be a finished task!";
            logger.error(msg);
            panic(new PanicException(msg));
        }

        if (task.isRootTask()) { //if its a root task then thats all folks
            deleteTaskFamilyFromQueues(task);
            return;
        }

        //if its a child task, we update it as a finished task
        updateFinishedTask(task);
    }

    private void updateFinishedTask(Task<?> task) {
        if (!task.isFinished()) {
            String msg = "Error, updating unfinished task as finished!";
            logger.debug(msg);
            panic(new PanicException(msg));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Updating Finished Task taskId=" + task);
        }

        task.stats.markFinishTime();

        if (task.isRootTask()) { //Task finished
            if (logger.isDebugEnabled()) {
                logger.debug("Adding to results task=" + task);
            }

            //task.files.cleanAll();
            results.add(task);
            notifyAll();
        } else { //task is a subtask
            stats.increaseSolvedTasks(task);

            TaskId parentId = task.taskId.getParentId();
            if (!this.waiting.containsKey(parentId)) {
                logger.error("Error. Parent task id=" + parentId + " is not waiting for child tasks");
                logger.error("Dropping task id=" + task);
                return;
            }

            Task<?> parent = waiting.get(parentId);
            if (!parent.family.setFinishedChild(task)) {
                logger.error("Parent did not recognize child task. Dropping task id=" + task);
                return;
            }

            //If this was the last subtask, then the parent is ready for execution
            if (parent.isReady()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Parent taskId=" + parent.taskId.value() + " is ready");
                }
                if (waiting.remove(parent.taskId) == null) {
                    logger.error("Error, parent not waiting when it should have been.");
                }
                parent.getStats().exitWaitingState();
                ready.addReady(parent);
            }
        }
    } //if its a child task, we update it as a finished task

    private void updateTask(Task<?> task) {
        //logger.debug("Unfinished taskId="+task);
        if (task.family.hasReadyChildTask()) {
            ready.addReadyChilds(task.family);

            if (logger.isDebugEnabled()) {
                logger.debug("Parent Task taskId=" + task.taskId.value() + " is waiting");
            }

            waiting.put(task.taskId, task); //the parent task will wait for it's subtasks
            return;
        }
    } //method

    public synchronized boolean hasResults() {
        return !results.isEmpty();
    }

    public synchronized boolean hasReadyTask() {
        return !ready.isEmpty();
    }

    public synchronized int getReadyQueueLength() {
        return ready.size();
    }

    public synchronized StatsGlobalImpl getStatsGlobal() {
        stats.setQueueLengths(ready.newRoots.size(), ready.older.size(), processing.size(), waiting.size(),
                results.size());
        return stats;
    }

    private void deleteTaskFamilyFromQueues(Task<?> blackSheepTask) {
        //1. Put the root tasks in the results queue
        Task<?> root;
        try {
            root = getRootTask(blackSheepTask);
            root.setException(blackSheepTask.getException());
            results.add(root);
        } catch (PanicException e) {
            panic(e); //panic if can not get root task
            return;
        }

        //2. Delete ready family tasks
        ready.deleteFamily(blackSheepTask);

        //3. Delete waiting family tasks
        Enumeration<Task<?>> enumeration = waiting.elements();
        while (enumeration.hasMoreElements()) {
            Task<?> task = enumeration.nextElement();
            if (task.taskId.getFamilyId().equals(blackSheepTask.taskId.getFamilyId())) {
                waiting.remove(task.taskId);
                task.getStats().exitWaitingState();
            }
        }

        //4. Mark family tasks in the processing queue as tainted.
        enumeration = processing.elements();
        while (enumeration.hasMoreElements()) {
            Task<?> task = enumeration.nextElement();
            if (task.taskId.getFamilyId() == blackSheepTask.taskId.getFamilyId()) {
                task.setTainted(true);
            }
        }
    }

    /**
     * Looks for the root task of this task in the different
     *  internal queues. If the root tasks is found, then the
     *  the task is deleted from the queue and returned.
     *
     *  1. It's self. (The parameter might be it's own root task)
     *  2. The waiting queue (Most likely place to find the root task)
     *  3. The processing queue (It is an error to find it here)
     *  4. The ready queue (It is an even bigger error to find it here)
     *  5. The results queue (It is a very big error to find it here).
     *
     * @param task the root tasks associated with this task
     * @return The root task (if found), or null if it was not found.
     * @throws PanicException If the root task is found where it shouldn't be.
     *
     */
    private Task<?> getRootTask(Task<?> task) throws PanicException {
        if (task.isRootTask()) {
            return task;
        }

        if (this.waiting.containsKey(task.taskId.getFamilyId())) {
            Task<?> root = waiting.remove(task.taskId.getFamilyId());
            root.getStats().exitWaitingState();
            return root;
        }

        if (this.processing.containsKey(task.taskId.getFamilyId())) {
            throw new PanicException("Error, root taskId=" + task.taskId.getFamilyId() +
                " found in processing queue");
        }

        if (this.ready.contains(task)) {
            throw new PanicException("Error, root taskId=" + task.taskId.getFamilyId() +
                " found in ready queue");
        }

        return null;
    }

    public synchronized void panic(PanicException e) {
        logger.error("Kernel Panic:" + e.getCause());
        this.panicException = e;

        notifyAll();
    }

    class ReadyQueue implements Serializable {
        //Root tasks that are in this queue for the first time
        PriorityQueue<Task<?>> newRoots;
        //Root tasks that have already been in this queue before
        PriorityQueue<Task<?>> older;

        public ReadyQueue() {
            newRoots = new PriorityQueue<Task<?>>();
            older = new PriorityQueue<Task<?>>();
        }

        public void addReadyChilds(TaskFamily family) {
            while (family.hasReadyChildTask()) {
                Task child = family.getReadyChild();
                if (logger.isDebugEnabled()) {
                    logger.debug("Child taskId=" + child.taskId.value() + " is ready");
                }
                addReady(child); //child will have more priority than uncles
            }
        }

        public boolean remove(Task<?> task) {
            return newRoots.remove(task) || older.remove(task);
        }

        public int size() {
            return newRoots.size() + older.size();
        }

        public void addNewRoot(Task<?> task) {
            newRoots.add(task);
        }

        public void addReady(Task<?> task) {
            older.add(task);
        }

        public boolean contains(Task<?> task) {
            return newRoots.contains(task) || older.contains(task);
        }

        public Task<?> poll() {
            if (!older.isEmpty()) {
                return older.poll();
            }

            return newRoots.poll();
        }

        public Task peek() {
            if (!older.isEmpty()) {
                return older.peek();
            }

            return newRoots.peek();
        }

        /**
         * @return A vector of at least one ready task, all having the same priority.
         */
        public Vector<Task> getBrotherTasks(Task task) {
            Vector<Task> v = new Vector<Task>();
            v.add(task);

            if (older.isEmpty() || task.isRootTask()) {
                return v;
            }

            Iterator<Task<?>> it = older.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                if (t.taskId.isBrotherTask(task.taskId)) {
                    v.add(t);
                    it.remove();
                }
            }

            return v;
        }

        public boolean isEmpty() {
            return newRoots.isEmpty() && older.isEmpty();
        }

        public void deleteFamily(Task<?> blackSheep) {
            for (Task<?> task : older) {
                if (task.taskId.getFamilyId().equals(blackSheep.taskId.getFamilyId())) {
                    older.remove(task);
                    task.getStats().exitReadyState();
                }
            }
        }
    }
} //class
