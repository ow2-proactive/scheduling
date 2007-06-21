package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import org.objectweb.proactive.extra.masterslave.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Interface for a repository which contains task objects, each task has an internal ID.
 * @author fviale
 *
 */
public interface TaskRepository {

    /**
     * Adds a new task to the repository
     * @param task the task to add
     * @param hashCode the hashCode of the task in the user address space
     * @return the id of this task
     * @throws TaskAlreadySubmittedException if the task has already been submitted
     */
    long addTask(Task task, int hashCode) throws TaskAlreadySubmittedException;

    /**
     * returns the task associated with this id
     * @param id id of the task
     * @return the task which has this id
     */
    TaskIntern getTask(long id);

    /**
     * Removes the task from the database, i.e. the task won't be used anymore by the system <br/>
     * (i.e. when the result of the task has been computed, the task is not needed anymore) <br/>
     * @param id if of the task to remove
     */
    void removeTask(long id);

    /**
     * Remove the given id from the system, the given id will be forgotten <br/>
     * (i.e. when the results have been given back to the user, the id must be deleted, along with the hashCode stored) <br/>
     * @param id the id to remove
     */
    void removeId(long id);

    /**
     * Asks the repository to save the task in a compressed format<br/>
     * This method is called when the task has been launched and is currently running on a slave. <br/>
     * There is still a chance that the slave will fail and the task will need to be rescheduled, but still most of the times, nothing will happen.<br/>
     * @param id id of the task to save
     */
    void saveTask(long id);

    /**
     * Terminates the repository activity
     * @return
     */
    boolean terminate();
}
