package org.ow2.proactive.scheduler.common.jmx.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.ow2.proactive.scheduler.common.jmx.graphics.DisplayResults;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerUsers;
import org.ow2.proactive.scheduler.common.scheduler.util.Tools;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

/**
 * Implementation of a SchedulerWrapperMBean.
 *
 * @author The ProActive Team
 */
@SuppressWarnings({ "serial", "unchecked" })
public class SchedulerWrapper extends NotificationBroadcasterSupport implements SchedulerWrapperMBean, SchedulerEventListener {

	/** Sequence number for Notifications */
	private long sequenceNumber = 1;

	/** Reference to SchedulerUsers */
	private SchedulerUsers users = new SchedulerUsers();

	/** list of all jobs managed by the scheduler */
    private Map<JobId, InternalJob> jobs = new HashMap<JobId, InternalJob>();

    /** Scheduler current state */
    private SchedulerState stateValue = SchedulerState.STOPPED;

	/**
     * Empty constructor required by JMX
     */
    public SchedulerWrapper() {
        /* Empty Constructor required by JMX */
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################ MBEAN EVENTS AND STATE ################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * The following methods represent the Events managed by the Scheduler MBean
     *
     * Methods to implement the interface SchedulerEventListener
     *
     * Implementing this interface we can send a JMX Notification for each type of event occurred and we can
     * make the related update to the state based on the event occurred
     */
	@Override
	public synchronized void jobChangePriorityEvent(JobEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		/*
		 *	Construct a Notification that describes the event.The
	     * 	source" of a notification is the ObjectName of the MBean
	     * 	that emitted it.
		 */
		Notification n = new Notification("jobChangePriorityEvent", this, sequenceNumber++);
		/*
		 *	Now send the notification using the sendNotification method
		 *	inherited from the parent class NotificationBroadcasterSupport.
		 */
		sendNotification(n);
	}

	@Override
	public synchronized void jobPausedEvent(JobEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("jobPausedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void jobPendingToRunningEvent(JobEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("jobPendingToRunningEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void jobRemoveFinishedEvent(JobEvent event) {
		// Status update
		JobId jobId = event.getJobId();
		InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		// Send the related Notification
		Notification n = new Notification("jobRemoveFinishedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void jobResumedEvent(JobEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("jobResumedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void jobRunningToFinishedEvent(JobEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("jobRunningToFinishedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void jobSubmittedEvent(Job job) {
		// Status update
		this.jobs.put(job.getId(), (InternalJob)job);
		// Send the related Notification
		Notification n = new Notification("jobSubmittedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerFrozenEvent() {
		// Status update
		this.stateValue = SchedulerState.FROZEN;
		// Send the related Notification
		Notification n = new Notification("schedulerFrozenEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerKilledEvent() {
		// Status update
		this.stateValue = SchedulerState.KILLED;
		// Send the related Notification
		Notification n = new Notification("schedulerKilledEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerPausedEvent() {
		// Status update
		this.stateValue = SchedulerState.PAUSED;
		// Send the related Notification
		Notification n = new Notification("schedulerPausedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerRMDownEvent() {
		// Send the related Notification
		Notification n = new Notification("schedulerRMDownEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerRMUpEvent() {
		// Send the related Notification
		Notification n = new Notification("schedulerRMUpEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerResumedEvent() {
		// Send the related Notification
		Notification n = new Notification("schedulerResumedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerShutDownEvent() {
		// Send the related Notification
		Notification n = new Notification("schedulerShutDownEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerShuttingDownEvent() {
		// Status update
		this.stateValue = SchedulerState.SHUTTING_DOWN;
		// Send the related Notification
		Notification n = new Notification("schedulerShuttingDownEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerStartedEvent() {
		// Status update
		this.stateValue = SchedulerState.STARTED;
		// Send the related Notification
		Notification n = new Notification("schedulerStartedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void schedulerStoppedEvent() {
		// Status update
		this.stateValue = SchedulerState.STOPPED;
		// Send the related Notification
		Notification n = new Notification("schedulerStoppedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void taskPendingToRunningEvent(TaskEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("taskPendingToRunningEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void taskRunningToFinishedEvent(TaskEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("taskRunningToFinishedEvent", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void taskWaitingForRestart(TaskEvent event) {
		// Status update
		//JobId jobId = event.getJobId();
		//InternalJob job = this.jobs.remove(jobId);
		//job.update(event);
		//this.jobs.put(jobId, job);
		// Send the related Notification
		Notification n = new Notification("taskWaitingForRestart", this, sequenceNumber++);
		sendNotification(n);
	}

	@Override
	public synchronized void usersUpdate(UserIdentification userIdentification) {
		// Status update
		this.users.addUser(userIdentification);
		// Send the related Notification
		Notification n = new Notification("usersUpdate", this, sequenceNumber++);
		sendNotification(n);
	}

	/* ########################################################################################### */
    /*                                                                                             */
    /* ################################## MBEAN ATTRIBUTES ####################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * The following methods represent the Attributes that is possible to monitor from the MBean
     *
     * Methods to get the values of the attributes of the MBean
     */
	public String getStateValue() {
		return this.stateValue.toString();
	}

	public int getNumberOfConnectedUsers() {
		return this.users.getUsers().size();
	}

	public int getTotalNumberOfJobs() {
		return this.jobs.size();
	}

	public int getNumberOfPendingJobs() {
		int numberOfPendingJobs = 0;
		// Get the Iterator of all the Jobs in the HashMap
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Increment only for the Pending Jobs
			if(job.getState().toString().equals("Pending")) {
				numberOfPendingJobs += 1;
			}
		}
		return numberOfPendingJobs;
	}

	public int getNumberOfRunningJobs() {
		int numberOfRunningJobs = 0;
		Iterator jobsIterator = this.jobs.values().iterator();
		while(jobsIterator.hasNext()) {
			InternalJob job = (InternalJob) jobsIterator.next();
			// Increment only for the Running Jobs
			if(job.getState().toString().equals("Running")) {
				numberOfRunningJobs += 1;
			}
		}
		return numberOfRunningJobs;
	}

	public int getNumberOfFinishedJobs() {
		int numberOfFinishedJobs = 0;
		Iterator jobsIterator = this.jobs.values().iterator();
		while(jobsIterator.hasNext()) {
			InternalJob job = (InternalJob) jobsIterator.next();
			// Increment only for the Finished Jobs
			if(job.getState().toString().equals("Finished")) {
				numberOfFinishedJobs += 1;
			}
		}
		return numberOfFinishedJobs;
	}

	public int getTotalNumberOfTasks() {
		int totalTasks = 0;
		Iterator<InternalJob> jobsIterator = this.jobs.values().iterator();
		while(jobsIterator.hasNext()) {
			InternalJob job = (InternalJob) jobsIterator.next();
			totalTasks += job.getTasks().size();
		}
		return totalTasks;
	}

	public int getNumberOfPendingTasks() {
		int pendingTasks = 0;
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Get the list of Tasks of this Job
			ArrayList<?> tasks = job.getTasks();
			// Loop for each Task of the Job
			for(int j=0;j<tasks.size();j++) {
				// Get the task
				InternalTask task = (InternalTask) tasks.get(j);
				// Increment only for the Pending Tasks
				if(task.getStatus().toString().equals("Pending")) {
					pendingTasks += 1;
				}
			}
		}
		return pendingTasks;
	}

	public int getNumberOfRunningTasks() {
		int runningTasks = 0;
		Iterator jobsIterator = this.jobs.values().iterator();
		while(jobsIterator.hasNext()) {
			InternalJob job = (InternalJob) jobsIterator.next();
			ArrayList<?> tasks = job.getTasks();
			for(int j=0;j<tasks.size();j++) {
				InternalTask task = (InternalTask) tasks.get(j);
				// Increment only for the Running Tasks
				if(task.getStatus().toString().equals("Running")) {
					runningTasks += 1;
				}
			}
		}
		return runningTasks;
	}

	public int getNumberOfFinishedTasks() {
		int finishedTasks = 0;
		Iterator jobsIterator = this.jobs.values().iterator();
		while(jobsIterator.hasNext()) {
			InternalJob job = (InternalJob) jobsIterator.next();
			ArrayList<?> tasks = job.getTasks();
			for(int j=0;j<tasks.size();j++) {
				InternalTask task = (InternalTask) tasks.get(j);
				// Increment only for the Finished Tasks
				if(task.getStatus().toString().equals("Finished")) {
					finishedTasks += 1;
				}
			}
		}
		return finishedTasks;
	}

	/* ########################################################################################### */
    /*                                                                                             */
    /* ################################## MBEAN OPERATIONS ####################################### */
    /*                                                                                             */
    /* ########################################################################################### */

	/**
	 * The following methods represent the Operations that is possible to Invoke on the MBean
	 *
	 * This method displays the id and the status of all the Jobs submitted to the Scheduler and not removed yet
	 */
	public void getAllSubmittedJobs() {
		//Set the title of the Display
		String title = "Submitted Jobs";
		ArrayList resultList = new ArrayList();
		//Add to result String array
		resultList.add("ID                            |STATUS                        ");
		resultList.add("-------------------------------------------------------------");
		//Get the Job Iterator
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Add the id and the status to the list for each Job
			resultList.add(job.getId()+"                              "+job.getState());
		}
		//Get the final result String Array
		int size = resultList.size();
		String[] result = new String[size];
		for(int i=0;i<size;i++) {
			result[i] = (String) resultList.get(i);
		}
		// Display the result String
		new DisplayResults(title, result);
	}

	/**
	 * This method displays the id and the status of all the Tasks submitted to the Scheduler and not removed yet
	 */
	public void getAllSubmittedTasks() {
		//Set the title of the Display
		String title = "Submitted Tasks";
		ArrayList resultList = new ArrayList();
		//Add to result String array
		resultList.add("ID                            |STATUS                        ");
		resultList.add("-------------------------------------------------------------");
		//Get the Job Iterator
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Get the list of Tasks of this Job
			ArrayList<?> tasks = job.getTasks();
			// Loop for each Task of the Job
			for(int j=0;j<tasks.size();j++) {
				// Get the task
				InternalTask task = (InternalTask) tasks.get(j);
				// Add the id and the status to the list for each Task
				resultList.add(task.getId()+"                          "+task.getStatus());
			}
		}
		//Get the final result String Array
		int size = resultList.size();
		String[] result = new String[size];
		for(int i=0;i<size;i++) {
			result[i] = (String) resultList.get(i);
		}
		// Display the result String
		new DisplayResults(title, result);
	}

	/**
	 * This method displays the info of a given Job selected by it`s Id
	 */
	public void getJobInfo(long id) {
		String title = "Job Info";
		String jobInfo = null;
		//Add to result String array
		String descript = "NAME\t\t |VALUE\t\t \n";
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Get only the Job selected
			if(Long.parseLong(job.getId().toString()) == id) {
				// Get a series of strings with the informations of the Job
				String limit = "=========================================================\n\n";
				String space = "---------------------------------------------------------------------------------\n";
				String name = "Name:\t\t "+job.getName()+"\n";
				String description = "Description:\t\t "+job.getDescription()+"\n";
				String totalTasks = "Total Number of Tasks:\t\t "+((job.getTotalNumberOfTasks()==-1) ? 0 : job.getTotalNumberOfTasks())+"\n";
				String finishedTasks = "Finished Tasks:\t\t "+((job.getNumberOfFinishedTask()==-1) ? 0 : job.getNumberOfFinishedTask())+"\n";
				String pendingTasks = "Pending Tasks:\t\t "+((job.getNumberOfPendingTask()==-1) ? 0 : job.getNumberOfPendingTask())+"\n";
				String runningTasks = "Running Tasks:\t\t "+((job.getNumberOfRunningTask()==-1) ? 0 : job.getNumberOfRunningTask())+"\n";
				String submittedTime = "Submitted Time:\t\t "+Tools.getFormattedDate(job.getSubmittedTime())+"\n";
				String startTime = "Start Time:\t\t "+(Tools.getFormattedDate(job.getStartTime())+"\n");
				String finishedTime = "Finished Time:\t\t "+Tools.getFormattedDate(job.getFinishedTime())+"\n";
				//Get the final result String
				jobInfo = descript+limit+name+space+description+space+totalTasks+space+finishedTasks+space+pendingTasks+
												space+runningTasks+space+submittedTime+space+startTime+space+finishedTime+space;
			}
		}
		// Display in a text Area
		new DisplayResults(title, jobInfo);
	}

	/**
	 * This method displays the info of a given Task selected by it`s Id
	 */
	public void getTaskInfo(long id) {
		String title = "Task Info";
		String taskInfo = null;
		//Add to result String array
		String descript = "NAME\t\t |VALUE\t\t \n";
		Iterator jobsIterator = this.jobs.values().iterator();
		// Loop for each Job of the HashMap
		while(jobsIterator.hasNext()) {
			// Get the Job
			InternalJob job = (InternalJob) jobsIterator.next();
			// Get the list of Tasks of this Job
			ArrayList<?> tasks = job.getTasks();
			// Loop for each Task of the Job
			for(int j=0;j<tasks.size();j++) {
				// Get the task
				InternalTask task = (InternalTask) tasks.get(j);
				// Get only the Job selected
				if(Long.parseLong(task.getId().toString()) == id) {
					String limit = "=========================================================\n\n";
					String space = "---------------------------------------------------------------------------------\n";
					String jobId = "Job Id:\t\t "+task.getJobId()+"\n";
					String taskId = "Task Id:\t\t "+task.getId()+"\n";
					String taskName = "Task Name:\t\t "+task.getName()+"\n";
					String hostName = "Execution Host Name:\t\t "+task.getExecutionHostName()+"\n";
					String description = "Description:\t\t "+task.getDescription()+"\n";
					String numberOfNodes = "Number of Nodes Needed:\t\t "+task.getNumberOfNodesNeeded()+"\n";
					String maxNumberOfExecution = "Max Number of Execution:\t\t "+task.getMaxNumberOfExecution()+"\n";
					String maxNumberOfExecutionOnFailure = "Max Number of Execution on Failure:\t\t "+task.getMaxNumberOfExecutionOnFailure()+"\n";
					String numberOfExecutionLeft = "Number of Execution Left:\t\t "+task.getNumberOfExecutionLeft()+"\n";
					String numberOfExecutionOnFailureLeft = "Number of Execution on Failure Left:\t\t "+task.getNumberOfExecutionOnFailureLeft()+"\n";
					String startTime = "Start Time:\t\t "+Tools.getFormattedDate(task.getStartTime())+"\n";
					String finishedTime = "Finished Time:\t\t "+Tools.getFormattedDate(task.getFinishedTime())+"\n";
					String wallTime = "Wall Time:\t\t "+Tools.getFormattedDate(task.getWallTime())+"\n";
					String resultPreview = "Result Preview:\t\t "+task.getResultPreview()+"\n";
					taskInfo = descript+limit+jobId+space+taskId+space+taskName+space+hostName+space+description+space+numberOfNodes+
								      space+maxNumberOfExecution+space+maxNumberOfExecutionOnFailure+space+numberOfExecutionLeft+space+
								       numberOfExecutionOnFailureLeft+space+startTime+space+finishedTime+space+wallTime+space+resultPreview+space;
				}
			}
		}
		// Display in a text Area
		new DisplayResults(title, taskInfo);
	}

	/**
	 * This method displays the info of all the Users Connected to the Scheduler
	 */
	public void getAllConnectedUsersInfo() {
		//Set the title of the Display
		String title = "Connected Users";
		ArrayList resultList = new ArrayList();
		//Add to result String array
		resultList.add("HOST NAME       |USERNAME        |SUBMIT NUMBER   |CONNECTION TIME |LAST SUBMIT TIME");
		resultList.add("------------------------------------------------------------------------------------");
		// Get the connected Users
		Iterator<?> connectedUsers = this.users.getUsers().iterator();
		// Loop for each User
		while(connectedUsers.hasNext()) {
			// Get the User Identification
			UserIdentification userIdentification = (UserIdentification) connectedUsers.next();
			// Add the fields to the list for each User
			resultList.add(userIdentification.getHostName()+"\t |"+userIdentification.getUsername()+"\t |"+userIdentification.getSubmitNumber()+
					"\t |"+userIdentification.getConnectionTime()+"\t |"+userIdentification.getLastSubmitTime()+"\t ");
		}
		//Get the final result String Array
		int size = resultList.size();
		String[] result = new String[size];
		for(int i=0;i<size;i++) {
			result[i] = (String) resultList.get(i);
		}
		// Display the result String
		new DisplayResults(title, result);
	}
}