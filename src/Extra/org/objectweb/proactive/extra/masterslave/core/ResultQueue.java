package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.objectweb.proactive.extra.masterslave.interfaces.Master;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


/**
 * This class is responsible for keeping the completed results.<br/>
 * The class will serve the results in a Completion order mode or a Submission order mode<br/>
 * @author fviale
 *
 */
public class ResultQueue implements Serializable {
    //	 current ordering mode
    private Master.OrderingMode mode;

    // submitted tasks ids (ordered)
    private TreeSet<Long> idsubmitted = new TreeSet<Long>();

    // sorted set of results (for submission order)
    private SortedSet<TaskIntern> orderedResults = new TreeSet<TaskIntern>();

    // unordered list of results (for 
    private LinkedList<TaskIntern> unorderedResults = new LinkedList<TaskIntern>();

    /**
     * Creates the result queue with the given ordering mode
     * @param mode
     */
    public ResultQueue(Master.OrderingMode mode) {
        this.mode = mode;
    }

    /**
     * Adds a completed task to the queue
     * @param task completed task
     */
    public void addCompletedTask(TaskIntern task) {
        if (mode == Master.OrderingMode.CompletionOrder) {
            unorderedResults.add(task);
        } else {
            orderedResults.add(task);
        }
    }

    /**
     * Specifies that a new tasks have been submitted
     * @param task pending task
     */
    public void addPendingTask(TaskIntern task) {
        idsubmitted.add(task.getId());
    }

    /**
     * Tells if all results are currently available in the queue
     * @return the answer
     */
    public boolean areAllResultsAvailable() {
        if (mode == Master.OrderingMode.CompletionOrder) {
            return unorderedResults.size() == idsubmitted.size();
        } else {
            return orderedResults.size() == idsubmitted.size();
        }
    }

    /**
     * Clears the queue
     */
    public void clear() {
        if (mode == Master.OrderingMode.CompletionOrder) {
            unorderedResults.clear();
        } else {
            orderedResults.clear();
        }
    }

    /**
     * Count the number of results available for the current reception order
     * @return the number of results available for the current reception order
     */
    public int countAvailableResults() {
        if (mode == Master.OrderingMode.CompletionOrder) {
            return unorderedResults.size();
        } else {
            int resultcount = 0;
            Iterator<TaskIntern> it = orderedResults.iterator();
            Iterator<Long> it2 = idsubmitted.iterator();
            while (it.hasNext() && (it.next().getId() == it2.next())) {
                resultcount++;
            }
            return resultcount;
        }
    }

    /**
     * Counts the number of pending tasks
     * @return the number of pending tasks
     */
    public int countPendingResults() {
        return idsubmitted.size();
    }

    /**
     * Returns all completed tasks (if and only if there are no pending tasks)
     * @return a list containing all completed tasks, if all tasks are completed
     * @throws NoSuchElementException if some tasks are not completed
     */
    public List<TaskIntern> getAll() throws NoSuchElementException {
        if (areAllResultsAvailable()) {
            List<TaskIntern> answer = new ArrayList<TaskIntern>();
            Iterator<TaskIntern> it;
            if (mode == Master.OrderingMode.CompletionOrder) {
                it = unorderedResults.iterator();
            } else {
                it = orderedResults.iterator();
            }
            while (it.hasNext()) {
                answer.add(it.next());
                it.remove();
            }
            return answer;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the next completed task (depending of the current ResultReceptionOrder)
     * @return the next completed task, if the next task in the current ResultReceptionOrder is available
     * @throws NoSuchElementException if no task in the current ResultReceptionOrder are available
     */
    public TaskIntern getNext() throws NoSuchElementException {
        if (isOneResultAvailable()) {
            TaskIntern answer;
            if (mode == Master.OrderingMode.CompletionOrder) {
                answer = unorderedResults.poll();
            } else {
                Iterator<TaskIntern> it = orderedResults.iterator();
                answer = it.next();
                it.remove();
            }
            idsubmitted.remove(answer.getId());
            return answer;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the next completed task (depending of the current ResultReceptionOrder)
     * @param k number of completed tasks to get
     * @return a list containing the k next completed tasks, if the k next tasks in the current ResultReceptionOrder are available
     * @throws NoSuchElementException if not enough tasks in the current ResultReceptionOrder are available
     */
    public List<TaskIntern> getNextK(int k) {
        if (countAvailableResults() >= k) {
            List<TaskIntern> answer = new ArrayList<TaskIntern>();
            Iterator<TaskIntern> it;
            if (mode == Master.OrderingMode.CompletionOrder) {
                it = unorderedResults.iterator();
            } else {
                it = orderedResults.iterator();
            }
            int count = 0;
            while (it.hasNext() && (count < k)) {
                answer.add(it.next());
                it.remove();
                count++;
            }
            return answer;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Tells that the queue has neither results nor pending tasks
     * @return true if the result queue has neither results nor pending tasks, false otherwise
     */
    public boolean isEmpty() {
        if (mode == Master.OrderingMode.CompletionOrder) {
            return idsubmitted.isEmpty() && unorderedResults.isEmpty();
        } else {
            return idsubmitted.isEmpty() && orderedResults.isEmpty();
        }
    }

    /**
     * Tells that there is at least one result available for the current reception order
     * @return true if there is at least one result available for the current reception order, false otherwise
     */
    public boolean isOneResultAvailable() {
        if (mode == Master.OrderingMode.CompletionOrder) {
            return !unorderedResults.isEmpty();
        } else {
            if (!orderedResults.isEmpty()) {
                return (orderedResults.first().getId() == idsubmitted.first());
            }
            return false;
        }
    }

    /**
     * Changes the current result ordering mode
     * @param mode the new result ordering mode
     */
    public void setMode(Master.OrderingMode mode) {
        if ((mode == Master.OrderingMode.CompletionOrder) &&
                (this.mode == Master.OrderingMode.SubmitionOrder)) {
            Iterator<TaskIntern> it = orderedResults.iterator();
            while (it.hasNext()) {
                TaskIntern res = it.next();
                unorderedResults.add(res);
                it.remove();
            }
        } else if ((mode == Master.OrderingMode.SubmitionOrder) &&
                (this.mode == Master.OrderingMode.CompletionOrder)) {
            Iterator<TaskIntern> it = unorderedResults.iterator();
            while (it.hasNext()) {
                TaskIntern res = it.next();
                orderedResults.add(res);
                it.remove();
            }
        }
    }
}
