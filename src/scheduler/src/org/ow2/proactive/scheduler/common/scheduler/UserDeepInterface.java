/**
 * 
 */
package org.ow2.proactive.scheduler.common.scheduler;

import java.io.Serializable;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * UserDeepInterface represents the method that have deep access in the scheduler.
 * It provides methods for someone connected to the scheduler as user.<br>
 * This interface provides methods to managed the user task and jobs on the scheduler.
 *
 * @author The ProActive Team
 * @date 20 févr. 08
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public interface UserDeepInterface extends Serializable {

    /**
     * Get the result for the given jobId.
     * A user can only get HIS result back except if he is admin.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the job result represented by the given jobId and if the job exists,
     * so you will receive the result. In any other cases a schedulerException will be thrown.
     *
     * @param jobId the job on which the result will be send
     * @return a job Result containing information about the result.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException;

    /**
     * Remove the job from the scheduler.
     *
     * @param jobId the job to be removed.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void remove(JobId jobId) throws SchedulerException;

    /**
     * Get the result for the given task name in the given jobId.
     * A user can only get HIS result back.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the task result that is in the job represented by the given jobId and if the job and task name exist,
     * so you will receive the result. In any other cases a schedulerException will be thrown.<br>
     *
     * @param jobId the job in which the task result is.
     * @param taskName the name of the task in which the result is.
     * @return a job Result containing information about the result.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException;

    /**
     * Listen for the tasks user log.<br>
     * A user can only listen to HIS jobs.
     *
     * @param jobId the id of the job to listen to.
     * @param hostname the host name where to send the log.
     * @param port the port number on which the log will be sent.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void listenLog(JobId jobId, String hostname, int port) throws SchedulerException;

    /**
     * kill the job represented by jobId.<br>
     * This method will kill every running tasks of this job, and remove it from the scheduler.<br>
     * The job won't be terminated, it won't have result.
     *
     * @param jobId the job to kill.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException;

    /**
     * Pause the job represented by jobId.<br>
     * This method will finish every running tasks of this job, and then pause the job.<br>
     * The job will have to be resumed in order to finish.
     *
     * @param jobId the job to pause.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException;

    /**
     * Resume the job represented by jobId.<br>
     * This method will restart every non-finished tasks of this job.
     *
     * @param jobId the job to resume.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException;

    /**
     * Change the priority of the job represented by jobId.
     *
     * @param jobId the job on which to change the priority.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException;
}
