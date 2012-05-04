package org.ow2.proactive.scheduler.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.ext.filessplitmerge.EmbarrasinglyParrallelApplication;


/**
 * Concrete implementation of this class are responsible with the post
 * treatment, the "merging" of the results of the tasks of a job This object is
 * subscribed by the
 * {@link EmbarrasinglyParrallelApplication#initApplication(String, String, String, Class, Class, Class)}
 * method, as listener to the InternalSchedulerListener's local instance
 *
 * @author esalagea
 *
 */
public class DataTransferManager {

    /**
     * A set of jobs that have been launched and wich's results are awaited Each
     * time a new job is sent to the scheduler for computation, its Id will be
     * added to this set. When the merge of a job have been completely
     * performed, it's Id will be removed from this set This set is persisted in
     * the status file
     */
    protected Set<String> awaitedJobsIds = Collections.synchronizedSet(new HashSet<String>());

    public static final Logger logger_util = ProActiveLogger.getLogger(SchedulerLoggers.UTIL);

    protected static String statusFilename = "dataTransfer.status";
    /**
     * this file keeps the ids of the awaited jobs it is used in case of crash
     * of the application
     */
    protected File statusFile;

    /**
     * This interface will be used in order to gather the results of the
     * finished jobs
     */
    protected SchedulerProxyUserInterface uiScheduler;

    protected static String awaitedJobsAttrName = "awaited_jobs";
    protected static String awaitedJobsSeparator = " ";

    public DataTransferManager() {
    }

    /**
     * Initiates the Post treatment manager. It will connect too the Scheduler
     * and check if the results of the jobs that might have been launched in a
     * previous session are available. If true, these results will be merged
     */
    public void init() {
        try {
            loadAwaitedJobs();
        } catch (FileNotFoundException e) {
            logger_util
                    .error(
                            "Could not load the status file. No data will be retrieved for previousley submitted jobs.",
                            e);
        } catch (IOException e) {
            logger_util
                    .error(
                            "Could not load the status file. No data will be retrieved for previousley submitted jobs.",
                            e);
        }

        // now we have to check with the scheduler if we have results for our
        // awaited jobs
        // we will perform post treatment for all the results on the awaited
        // jobs.
        checkResultsForAwaitedJobs();
    }

    /**
     * This method will check, for each awaited job, if the result is available
     * on the Scheduler. If positive, the will call the performPostTreatment
     * method in order to perform the post treatment.
     */
    protected void checkResultsForAwaitedJobs() {
        // we make a copy of the awaitedJobsIds set in order to iterate over it.
        Set<String> awaitedJobsIdsCopy = new HashSet<String>(awaitedJobsIds);

        Iterator<String> it = awaitedJobsIdsCopy.iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (transferData(id))
                this.removeAwaitedJob(id);
        }
    }

    public boolean transferData(String jobId_srt) {

        try {
            return true;
        } catch (Exception e) {
            logger_util.error("Could not pull data for job " + jobId_srt);
            return false;
        }

    }

    /**
     * This reads the jobs Ids in the status file and updates the awaitedJobs
     * list This operation should be performed when the application starts in
     * order to update the awaited jobs conform to the status file
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected synchronized void loadAwaitedJobs() throws FileNotFoundException, IOException {
        statusFile = new File(statusFilename);
        if (!statusFile.isFile()) {
            return;
        }

        String awaitedJobs = "";
        Properties properties = new Properties();
        properties.load(new FileInputStream(statusFile));
        awaitedJobs = properties.getProperty(awaitedJobsAttrName);
        if ((awaitedJobs == null) || (awaitedJobs.length() == 0))
            return;
        awaitedJobs = awaitedJobs.trim();
        String[] awaitedJobsStrArrray = awaitedJobs.split(awaitedJobsSeparator);
        awaitedJobsIds = Collections
                .synchronizedSet(new HashSet<String>(Arrays.asList(awaitedJobsStrArrray)));
    }

    /**
     * Saves the awaitedJobs ids to the status file
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected synchronized void saveAwaitedJobsToFile() throws FileNotFoundException, IOException {
        String awaitedJobsStr = "";
        for (String s : awaitedJobsIds) {
            awaitedJobsStr += s + awaitedJobsSeparator;
        }
        Properties properties = new Properties();
        properties.put(awaitedJobsAttrName, awaitedJobsStr);
        properties.store(new FileOutputStream(statusFile), null);
    }

    public void update(NotificationData<?> notification) {

        // am I interested in this job?
        JobId id = ((NotificationData<JobInfo>) notification).getData().getJobId();
        if (!awaitedJobsIds.contains(id.toString()))
            return;

        JobState jobState = null;

        try {
            jobState = uiScheduler.getJobState(id);
        } catch (NotConnectedException e) {
            logger_util.error("Could not retreive output data for job " + id, e);
        } catch (UnknownJobException e) {
            logger_util.error("Could not retreive output data for job " + id, e);
        } catch (PermissionException e) {
            logger_util.error("Could not retreive output data for job " + id +
                ". Did you connect with a diffrent user ? ", e);
        }

        if (jobState == null) {
            logger_util.warn("The job " + id +
                " is listed as awaited but is unknown bby the scheduler. It will be removed from local list");
            removeAwaitedJob(id.toString());
        }

        JobStatus status = jobState.getStatus();
        switch (status) {
            case KILLED: {
                logger_util.info("The job " + id + "has been killed. No data will be transfered");
                break;
            }
            case FINISHED: {
                logger_util.info("Transfering data for finished job " + id);
                transferData(id.toString());
            }
            case CANCELED: {
                logger_util.info("Transfering data for canceled job " + id);
                transferData(id.toString());
            }
            case FAILED: {
                logger_util.info("Transfering data for failed job " + id);
                transferData(id.toString());
            }
        }
    }

    /**
     *
     * @return a new HashSet with the awaited jobs. Modifying the result of this
     *         method will not affect the source HashSet (the awaited jobs)
     */
    public HashSet<String> getAwaitedJobs() {
        return new HashSet<String>(awaitedJobsIds);
    }

    public boolean isAwaitedJob(String id) {
        if (awaitedJobsIds.contains(id))
            return true;
        else
            return false;
    }

    public synchronized void addAwaitedJob(String id) {
        this.awaitedJobsIds.add(id);
        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger_util.error("Could not save status file after adding job on awaited jobs list " + id, e);
        } catch (IOException e) {
            logger_util.error("Could not save status file after adding job on awaited jobs list " + id, e);
        }

    }

    public synchronized void removeAwaitedJob(String id) {
        this.awaitedJobsIds.remove(id);

        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger_util.error("Could not save status file after removing job " + id, e);
        } catch (IOException e) {
            logger_util.error("Could not save status file after removing job " + id, e);
        }
    }

}
