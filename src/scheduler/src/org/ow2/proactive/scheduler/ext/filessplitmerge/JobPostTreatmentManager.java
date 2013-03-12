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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.EventType;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalEvent;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalSchedulerEventListener;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.ExceptionToStringHelper;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.util.FilesTools;
import org.ow2.proactive.scheduler.ext.filessplitmerge.util.MySchedulerProxy;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * 		Concrete implementation of this class is responsible with the post
 * treatment (usually the "merging" of the results of the tasks of a job) <p>
 * 		An instance of this class is subscribed by the {@link EmbarrasinglyParrallelApplication#initApplication(String, String, String, Class, Class, Class)}
 * method, as listener to the {@link InternalSchedulerEventListener}'s local instance. <p>
 * 		When a job finishes computation, an event will be received.
 * 		
 * 
 * @author esalagea
 * 
 */
public abstract class JobPostTreatmentManager implements Observer {

    /**
     * 		A set of jobs that have been launched and for which results are awaited<p>
     *  	Each time a new job is sent to the scheduler for computation, its Id will be
     * added to this set. When the merge of a job have been completely
     * performed, it's Id will be removed from this set <p>
     * 		This set is persisted in the status file for fault tolerance and disconnected mode.
     */
    protected Set<String> awaitedJobsIds = Collections.synchronizedSet(new HashSet<String>());

    /**
     * A local view (i.e. not active view) for the {@link InternalSchedulerEventListener} object
     * This object is synchronized with the Scheduler state. It will be used to obtain a Job object from an Id 
     */
    protected InternalSchedulerEventListener goldSchedulerEventListener_localView;

    protected static Executor tpe = Executors.newFixedThreadPool(20, new NamedThreadFactory(
        "JobPostTreatmentManager"));

    /**
     * name of the {@link #statusFile}
     */
    protected static String statusFilename = ".status";

    /**
     * This file will be used in order to persist the ids of the awaited jobs (for fault tollerance and disconnected mode) 
     *
     */
    protected File statusFile;

    /**
     * This interface will be used in order to gather the results of the
     * finished jobs. It is a reference (stub) to the {@link SchedulerProxyUserInterface} active object
     */
    protected Scheduler uiScheduler;

    /**
     * Name of the attribute awaited jobs in the status file 
     */
    protected static String awaitedJobsAttrName = "awaited_jobs";

    public JobPostTreatmentManager() {

    }

    /**
     * Sets the {@link #goldSchedulerEventListener_localView} and {@link #uiScheduler} references
     * 
     */
    protected void connect() {
        try {
            InternalSchedulerEventListener[] localAndActiveViews;
            localAndActiveViews = InternalSchedulerEventListener.getActiveAndLocalReferences();
            goldSchedulerEventListener_localView = localAndActiveViews[0];
            //goldSchedulerEventListener_activeView = localAndActiveViews[1];

        } catch (ActiveObjectCreationException e) {
            LoggerManager.getLogger().info(ExceptionToStringHelper.getStackTrace(e));
        } catch (NodeException e) {
            LoggerManager.getLogger().info(ExceptionToStringHelper.getStackTrace(e));
        }

        try {
            uiScheduler = MySchedulerProxy.getActiveInstance();
        } catch (NodeException ne) {
            LoggerManager.getInstane().error("Could not initiate the post treatment process", ne);
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            LoggerManager.getInstane().error("Could not initiate the post treatment process", e);
        }
    }

    /**
     * Synchronizes this object's status with the status file<p>
     * Populates the list of the awaited jobs with the ones specified in the status file by calling {@link #updateAwaitedJobs()}
     */
    protected void createStatusFromFile() {
        statusFile = new File(statusFilename);
        // System.out.println("Status File = "+statusFile.getAbsolutePath());
        if (!statusFile.exists()) {
            try {
                statusFile.createNewFile();
            } catch (IOException e) {
                LoggerManager.getLogger().info(ExceptionToStringHelper.getStackTrace(e));
            }
        } else {
            // read the awaited jobs IDs from the status file
            updateAwaitedJobs();
            if (awaitedJobsIds.size() > 0) {
                LoggerManager.getInstane()
                        .info(
                                "The results of these jobs have not yet been gatehered: " +
                                    awaitedJobsIds.toString());
            }
        }
    }

    /**
     * Initiates the Post treatment manager. It will connect to the Scheduler (actually it obtains a reference to the local proxy)
     * and check if the results of the jobs that might have been launched in a
     * previous session are available. If true, these results will be merged in a separated thread
     */
    public void init() {
        connect();
        createStatusFromFile();
        if (!uiScheduler.isConnected()) {
            LoggerManager.getInstane().error("Could not initiate the post treatment process. ");
            return;
        }

        // now we have to check with the scheduler if we have results for our
        // awaited jobs
        // we will perform post treatment for all the results on the awaited
        // jobs.
        checkResultsForAwaitedJobs();
    }

    /**
     * This method will check, for each awaited job, if the result is available
     * on the Scheduler. If positive, the will call the {@link #performPostTreatment(JobState)}
     * method in order to perform the post treatment.
     */
    protected void checkResultsForAwaitedJobs() {

        // we make a copy of the awaitedJobsIds set in order to iterate over it.
        Set<String> awaitedJobsIdsCopy = new HashSet<String>(awaitedJobsIds);

        Iterator<String> it = awaitedJobsIdsCopy.iterator();
        while (it.hasNext()) {
            String id = it.next();
            try {
                // /InternalJob job =
                // goldSchedulerEventListener_localView.getJobById(id);
                JobState job = goldSchedulerEventListener_localView.getJobById(id);
                JobResult result = uiScheduler.getJobResult(id);

                if (job == null) {
                    String info = "The job with id=" +
                        id +
                        " seems to be awaited but it is not known by the system (local application). Results for this job will not be merged. \n";
                    LoggerManager.getInstane().info(info);
                    // ProActiveGold.getLogger().info(info);
                }

                if (result != null) {
                    // jobsToRemove.add(id);

                    if (job != null) {
                        // /this.resultsPostTreatment(result, job);
                        // this method creates a thread for the post treatment
                        // so it will end quickly even if the post treatment is
                        // long
                        this.performPostTreatment(job);
                        // PostTreatment pt = new PostTreatment(job);
                        // JobPostTreatmentManager.tpe.execute(pt);
                    }
                }
            } catch (UnknownJobException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                LoggerManager
                        .getInstane()
                        .error(
                                "The job with id=" + id +
                                    " seems to be awaited but it is not known by the Scheduler. Results for this job will not be merged. \n",
                                e);
                // jobsToRemove.add(id);
                this.removeAwaitedJob(id);
                // ProActiveGold.getLogger().info(ExceptionToStringHelper.getStackTrace(e));
            } catch (SchedulerException e) {
                LoggerManager
                        .getInstane()
                        .error(
                                "The job with id=" +
                                    id +
                                    " seems to be awaited but there is an exception when calling getJobResult. Results for this job will not be merged. \n",
                                e);
            } catch (IllegalArgumentException ie) {
                LoggerManager
                        .getInstane()
                        .error(
                                "The job with id=" + id +
                                    " seems to be awaited but it is not known by the System. Results for this job will not be merged. \n",
                                ie);
                this.removeAwaitedJob(id);
                // jobsToRemove.add(id);
            }

        }

        // this.removeAwaitedJobs(jobsToRemove);
    }

    /**
     * 	This reads the jobs Ids in the status file and updates the awaitedJobs
     * list<p>
     *  This operation should be performed when the application starts in
     * order to update the awaited jobs conform to the status file
     */
    protected void updateAwaitedJobs() {
        String awaitedJobs = "";
        FilesTools ft = new FilesTools();
        try {
            awaitedJobs = ft.getValueForAttribute(statusFile, awaitedJobsAttrName, false);
        } catch (IOException e) {
            LoggerManager
                    .getInstane()
                    .warning(
                            "Could not read the awaited jobs from the status file. Results for jobs submited in previous sessions will not be merged. ",
                            e);
        }
        // System.out.println("AwaitedJobs="+awaitedJobs);
        if ((awaitedJobs == null) || (awaitedJobs.length() == 0))
            return;

        awaitedJobs = awaitedJobs.trim();

        while (awaitedJobs.length() > 0) {
            String currentJobId_str = "";
            if (awaitedJobs.indexOf(" ") != -1) {
                currentJobId_str = awaitedJobs.substring(0, awaitedJobs.indexOf(" "));
                awaitedJobs = awaitedJobs.substring(awaitedJobs.indexOf(" ")).trim();
            } else {
                currentJobId_str = awaitedJobs;
                awaitedJobs = "";
            }

            if (currentJobId_str.length() > 0) {
                try {
                    //JobId jobId = JobId.makeJobId(currentJobId_str);
                    awaitedJobsIds.add(currentJobId_str);
                } catch (NumberFormatException e) {
                    LoggerManager
                            .getInstane()
                            .warning(
                                    "The result of the job with id " +
                                        currentJobId_str +
                                        " is awaited by the application but the id is mallformed(should be a number). Please check the syntax of the file " +
                                        statusFile.getAbsolutePath(), e);
                }
            }
        }
        saveAwaitedJobsToFile();
    }

    /**
     * Saves the awaitedJobs ids to the status file
     */
    protected synchronized void saveAwaitedJobsToFile() {
        String awaitedJobsStr = "";
        FilesTools ft = new FilesTools();
        Iterator<String> it = awaitedJobsIds.iterator();
        while (it.hasNext()) {
            String id = it.next();
            awaitedJobsStr += id + " ";
        }

        try {
            ft.saveValueForAttribute(statusFile, JobPostTreatmentManager.awaitedJobsAttrName, awaitedJobsStr);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // ProActiveGold.getLogger().info(ExceptionToStringHelper.getStackTrace(e));
            LoggerManager
                    .getInstane()
                    .warning(
                            "Could not save the awated jobs to file. Please do not stop the application before having all the awaited results as futures sessions of the application will not be able to merge the results of the current jobs.",
                            e);
        }
    }

    /**
     * Events are received from the {@link InternalSchedulerEventListener} When
     * a job is finished we call the
     * {@link #performPostTreatment(InternalJob)} method
     */
    //@Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof InternalEvent)) {
            LoggerManager.getInstane().warning("Unknown notification received: " + arg.toString());
            return;
        }
        InternalEvent ge = (InternalEvent) arg;

        //TODO: fix cast when  upadates are available on the scheduler
        JobState job = ge.getJob();

        final EventType type = ge.getType();

        switch (type) {
            case jobRunningToFinishedEvent:
                // System.out.println("JobPostTreatmentProcess.update() -> perform
                // post treatment here.");
                if (isAwaitedJob(job.getId().value())) {
                    // perform the results post treatment in a new thread
                    performPostTreatment(job);
                    // this.removeAwaitedJob(job.getId());
                }
                break;
            case jobKilledEvent:
                this.removeAwaitedJob(job.getId().value());
                break;
            default:
                break;
        }// switch
    }

    /**
     * Merges the results of the job given as argument. <p>
     *  The {@link #mergeResults(JobConfiguration, int)} method will be called in
     * a new thread
     * 
     * @param job
     */
    private void performPostTreatment(JobState job) {
        JobId jobId = job.getId();
        if (!isAwaitedJob(jobId.value()))
            return;

        JobStatus state = job.getStatus();

        if ((state == JobStatus.CANCELED) || (state == JobStatus.FAILED)) {
            int finished = job.getNumberOfFinishedTasks();
            int total = job.getTotalNumberOfTasks();
            if (finished == 0) {// all the tasks have failed - no post treatment
                LoggerManager
                        .getInstane()
                        .error(
                                "The job has failed: " + job.getName() + "(id=" + job.getId() +
                                    "). All the tasks of this job have failed. Results for this job will not be available.");
                this.removeAwaitedJob(jobId.value());
                return;
            } else {// some of the tasks have failed - we do the post treatment
                LoggerManager.getInstane().error(
                        total - finished + " tasks of the the job " + job.getName() + "(id=" + job.getId() +
                            ") have failed from a total of " + total +
                            " tasks. Merging results of the successfully finished tasks ... ");
            }
        } else {
            LoggerManager.getInstane().info(
                    "Job is finished: " + job.getName() + " (id=" + job.getId() + "). Merging results .... ");
        }

        JobResult result = null;
        try {

            result = uiScheduler.getJobResult(jobId);

        } catch (SchedulerException e) {
            // e.printStackTrace();
            LoggerManager.getInstane().error(
                    "Could not get the result of the job: " + job.getName() + "(" + job.getDescription() +
                        ")\n Results will not be merged.", e);
            return;
        }

        // resultsPostTreatment(result, job);
        PostTreatment pt = new PostTreatment(job, result);
        JobPostTreatmentManager.tpe.execute(pt);

        // LoggerManager.getInstane().info("Computation finished for gold
        // job."+job.getName()+"(id = "+job.getId()+")"+" --
        // "+job.getDescription());
        // this job is no longer awaited
    }

    /**
     * 
     * @return a new HashSet with the awaited jobs. Modifying the result of this
     *         method will not affect the source HashSet (the awaited jobs)
     */
    public HashSet<String> getAwaitedJobs() {
        return new HashSet<String>(awaitedJobsIds);
    }

    /**
     * 
     * @param id
     * @return true, if the job with the given id is awaited by this JobPostTreatmentManager
     */
    public boolean isAwaitedJob(String id) {
        if (awaitedJobsIds.contains(id))
            return true;
        else
            return false;
    }

    /**
     * Ads a job id to the list of awaited jobs id. 
     * Persists the list of awaited jobs to the status file
     * @param id
     */
    public synchronized void addAwaitedJob(String id) {
        this.awaitedJobsIds.add(id);
        this.saveAwaitedJobsToFile();

    }

    /**
     * 
     * @param id id of the job to be removed from the awaited jobs list
     */
    public synchronized void removeAwaitedJob(String id) {
        this.awaitedJobsIds.remove(id);
        this.saveAwaitedJobsToFile();
    }

    /**
     *	Creates the {@link JobConfiguration} object corresponding to the specified job
     *	Calls the {@link #mergeResults(JobConfiguration, int)} method
     *	Removes the job from the awaited jobs list
     * 
     * @param result
     * @param job
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected void resultsPostTreatment(JobResult result, JobState job) {
        JobConfiguration jobConfiguration;
        try {

            Class<? extends JobConfiguration> clazz = EmbarrasinglyParrallelApplication.instance()
                    .getJobConfigurationClass();
            jobConfiguration = createjobConfigurationFromGeneralInfo(job, clazz);

        } catch (InstantiationException e1) {
            LoggerManager.getInstane().error(
                    "Result post treatment has not been performed because an error has occured ", e1);
            return;
        } catch (IllegalAccessException e1) {
            LoggerManager.getInstane().error(
                    "Result post treatment has not been performed because an error has occured ", e1);
            return;
        }

        int numberOfTasks = job.getTotalNumberOfTasks();

        mergeResults(jobConfiguration, numberOfTasks);

        LoggerManager.getInstane().info(
                "Results for job " + job.getName() + " (id=" + job.getId() + ") have been merged.");
        JobPostTreatmentManager.this.removeAwaitedJob(job.getId().value());
    }

    /**
     * This method merges the results of a finished job
     * 
     * @param jc -
     *            the JobConfiguration object that have been provided at job submissin time
     * @param numberOfTasks
     */
    //@snippet-start FileSplitMerge_mergeResults
    protected abstract void mergeResults(JobConfiguration jc, int numberOfTasks);

    //@snippet-end FileSplitMerge_mergeResults

    // ---util methods ---

    protected Method getMethodbyName(Method[] ms, String methodName) {
        for (int i = 0; i < ms.length; i++) {
            Method m = ms[i];
            if (m.getName().equals(methodName))
                return m;
        }

        return null;
    }

    protected JobConfiguration createjobConfigurationFromGeneralInfo(JobState job,
            Class<? extends JobConfiguration> clazz) throws InstantiationException, IllegalAccessException {

        // Class clazz = arg.getClass();

        // if (!JobConfiguration.class.getClass().isAssignableFrom(clazz))
        // {
        // throw new IllegalArgumentException("The expected class in argument
        // should be of type "+JobConfiguration.class.getName());
        // }

        JobConfiguration jobConfiguration = clazz.newInstance();

        Method[] ms = clazz.getDeclaredMethods();

        Map<String, String> generalInfo = job.getGenericInformations();

        for (String key : generalInfo.keySet()) {
            // the key is the property name
            // i.e. for a couple of methods getToto()/setToto(String toto) we
            // have key.equals("Toto")
            String setterName = "set" + key;
            Method setter = getMethodbyName(ms, setterName);
            if (setter != null) {
                String value = generalInfo.get(key);
                Object[] args = new Object[1];
                args[0] = value;

                try {
                    setter.invoke(jobConfiguration, args);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return jobConfiguration;
    }

    class PostTreatment implements Runnable {

        private JobState job;
        private JobResult result;

        public PostTreatment(JobState job, JobResult result) {
            this.job = job;
            this.result = result;
        }

        //@Override
        public void run() {
            JobPostTreatmentManager.this.resultsPostTreatment(result, job);
        }
    }

}
