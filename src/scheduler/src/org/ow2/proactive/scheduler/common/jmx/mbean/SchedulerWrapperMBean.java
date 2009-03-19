package org.ow2.proactive.scheduler.common.jmx.mbean;

/**
 * MBean representing the ProActive Scheduler.
 *
 * @author The ProActive Team
 */
public interface SchedulerWrapperMBean {

	/**
     * Returns the state of the scheduler.
     *
     * @return The state of the scheduler.
     */
	public String getStateValue();

	/**
     * Returns the number of users connected to the scheduler.
     *
     * @return the number of users connected to the scheduler.
     */
	public int getNumberOfConnectedUsers();

	/**
     * Returns the total number of jobs.
     *
     * @return the total number of jobs.
     */
	public int getTotalNumberOfJobs();

	/**
     * Returns the number of pending jobs of the scheduler.
     *
     * @return The number of pending jobs of the scheduler.
     */
	public int getNumberOfPendingJobs();

	/**
     * Returns the number of running jobs of the scheduler.
     *
     * @return The number of running jobs of the scheduler.
     */
	public int getNumberOfRunningJobs();

	/**
     * Returns the number of finished jobs of the scheduler.
     *
     * @return The number of finished jobs of the scheduler.
     */
	public int getNumberOfFinishedJobs();

	/**
     * Returns the total number of Tasks.
     *
     * @return the total number of Tasks.
     */
	public int getTotalNumberOfTasks();

	/**
     * Returns the number of pending Tasks of the scheduler.
     *
     * @return The number of pending Tasks of the scheduler.
     */
	public int getNumberOfPendingTasks();

	/**
     * Returns the number of running Tasks of the scheduler.
     *
     * @return The number of running Tasks of the scheduler.
     */
	public int getNumberOfRunningTasks();

	/**
     * Returns the number of finished Tasks of the scheduler.
     *
     * @return The number of finished Tasks of the scheduler.
     */
	public int getNumberOfFinishedTasks();

	/**
	 * The following methods represent the Operations that is possible to Invoke on the MBean
	 *
	 * This method displays the id and the status of all the Jobs submitted to the Scheduler and not removed yet
	 */
	public void getAllSubmittedJobs();

	/**
	 * This method displays the id and the status of all the Tasks submitted to the Scheduler and not removed yet
	 */
	public void getAllSubmittedTasks();

	/**
	 * This method displays the info of a given Job selected by it`s Id
	 */
	public void getJobInfo(long id);

	/**
	 * This method displays the info of a given Task selected by it`s Id
	 */
	public void getTaskInfo(long id);

	/**
	 * This method displays the info of all the Users Connected to the Scheduler
	 */
	public void getAllConnectedUsersInfo();
}
