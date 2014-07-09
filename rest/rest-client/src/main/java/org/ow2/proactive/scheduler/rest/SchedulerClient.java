/*
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.Job2XMLTransformer;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.JarUtils;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.rest.data.DataUtility;
import org.ow2.proactive.scheduler.rest.data.TaskResultImpl;
import org.ow2.proactive.scheduler.rest.readers.OctetStreamReader;
import org.ow2.proactive.scheduler.rest.readers.WildCardTypeReader;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.exception;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwJAFEOrUJEOrNCEOrPE;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwNCEOrPE;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwNCEOrPEOrSCEOrJCE;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwUJEOrNCEOrPE;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwUJEOrNCEOrPEOrUTE;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.jobId;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.taskState;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobInfos;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobResult;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobState;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobUsages;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toSchedulerUserInfos;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toTaskResult;


public class SchedulerClient extends ClientBase implements ISchedulerClient {

    private static final long retry_interval = TimeUnit.SECONDS.toMillis(1);

    private SchedulerRestClient schedulerRestClient;

    private String sid;
    private String url;
    private String login;
    private String password;

    private SchedulerEventReceiver schedulerEventReceiver;

    private SchedulerClient() {
    }

    /**
     * Creates an ISchedulerClient instance.
     *
     * @return an ISchedulerClient instance
     */
    public static ISchedulerClient createInstance() {
        SchedulerClient client = new SchedulerClient();
        return (ISchedulerClient) Proxy.newProxyInstance(ISchedulerClient.class.getClassLoader(),
                new Class[] { ISchedulerClient.class }, new SessionHandler(client));
    }

    /**
     * Initialize this instance. Retrieves a new session from the server.
     */
    public void init(String url, String login, String password) throws Exception {
        init(url, login, password, false);
    }

    /**
     * Initialize this instance. Retrieves a new session from the server.
     * @param insecure true to disable HTTPS certificate checking
     */
    public void initInsecure(String url, String login, String password) throws Exception {
        init(url, login, password, true);
    }

    private void init(String url, String login, String password, boolean insecure) throws Exception {
        HttpClient client = HttpUtility.threadSafeClient();
        if (insecure) {
            HttpUtility.setInsecureAccess(client);
        }
        SchedulerRestClient restApiClient = new SchedulerRestClient(url, new ApacheHttpClient4Engine(client));

        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        factory.register(new WildCardTypeReader());
        factory.register(new OctetStreamReader());

        setApiClient(restApiClient);

        this.url = url;
        this.login = login;
        this.password = password;

        renewSession();
    }

    @Override
    public List<JobUsage> getAccountUsage(String user, Date start, Date end) throws NotConnectedException,
            PermissionException {
        List<JobUsage> jobUsages = null;
        try {
            List<JobUsageData> jobUsageDataList = restApi().getUsageOnAccount(sid, user, start, end);
            jobUsages = toJobUsages(jobUsageDataList);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobUsages;
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException,
            PermissionException {
        List<JobUsage> jobUsages = null;
        try {
            List<JobUsageData> jobUsageDataList = restApi().getUsageOnMyAccount(sid, startDate, endDate);
            jobUsages = toJobUsages(jobUsageDataList);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobUsages;
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        changeJobPriority(jobId.value(), priority);
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        try {
            restApi().schedulerChangeJobPriorityByName(sid, jobId, priority.name());
        } catch (Exception e) {
            throwJAFEOrUJEOrNCEOrPE(e);
        }
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        try {
            restApi().disconnect(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
    }

    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        boolean success = false;
        try {
            success = restApi().freezeScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return success;
    }

    @Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        return getJobResult(jobId.value());
    }

    @Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        JobResult jobResult = null;
        try {
            JobResultData jobResultData = restApi().jobResult(sid, jobId);
            jobResult = toJobResult(jobResultData);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobResult;
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        JobState jobState = null;
        try {
            JobStateData jobStateData = restApi().listJobs(sid, jobId);
            jobState = toJobState(jobStateData);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobState;
    }

    @Override
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return getJobState(jobId.value());
    }

    @Override
    public List<JobInfo> getJobs(int index, int range, JobFilterCriteria criteria,
            List<SortParameter<JobSortParameter>> arg3) throws NotConnectedException, PermissionException {
        List<JobInfo> jobInfos = null;
        try {
            List<UserJobData> userJobDataList = restApi().jobsinfo(sid, index, range);
            jobInfos = toJobInfos(userJobDataList);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobInfos;
    }

    @Override
    public String getJobServerLogs(String jobId) throws UnknownJobException, NotConnectedException,
            PermissionException {
        String jobServerLog = "";
        try {
            jobServerLog = restApi().jobServerLog(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobServerLog;
    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        SchedulerStatus status = null;
        try {
            SchedulerStatusData schedulerStatus = restApi().getSchedulerStatus(sid);
            status = SchedulerStatus.valueOf(schedulerStatus.name());
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return status;
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        TaskResultImpl taskResult = null;
        try {
            TaskResultData taskResultData = restApi().taskresult(sid, jobId, taskName);
            taskResult = (TaskResultImpl) toTaskResult(JobIdImpl.makeJobId(jobId), taskResultData);
            if (taskResult.value() == null) {
                Serializable value = restApi().valueOftaskresult(sid, jobId, taskName);
                if (value != null) {
                    taskResult.setHadException(true);
                    taskResult.setValue(value);
                }
            }

            String all = restApi().tasklog(sid, jobId, taskName);
            String out = restApi().tasklogout(sid, jobId, taskName);
            String err = restApi().tasklogErr(sid, jobId, taskName);

            taskResult.setOutput(DataUtility.toTaskLogs(all, out, err));

        } catch (Throwable t) {
            throwUJEOrNCEOrPEOrUTE(exception(t));
        }
        return taskResult;
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return getTaskResult(jobId.value(), taskName);
    }

    @Override
    public String getTaskServerLogs(String arg0, String arg1) throws UnknownJobException,
            UnknownTaskException, NotConnectedException, PermissionException {
        String taskLogs = "";
        try {
            taskLogs = restApi().tasklog(sid, arg0, arg1);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return taskLogs;
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        List<SchedulerUserInfo> schedulerUserInfos = null;
        try {
            List<SchedulerUserData> users = restApi().getUsers(sid);
            schedulerUserInfos = toSchedulerUserInfos(users);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return schedulerUserInfos;
    }

    @Override
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        List<SchedulerUserInfo> schedulerUserInfos = null;
        try {
            List<SchedulerUserData> usersWithJobs = restApi().getUsersWithJobs(sid);
            schedulerUserInfos = toSchedulerUserInfos(usersWithJobs);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return schedulerUserInfos;
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = false;
        try {
            isConnected = restApi().isConnected(sid);
        } catch (NotConnectedRestException e) {
            // ignore
        }
        return isConnected;
    }

    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        boolean isKilled = false;
        try {
            isKilled = restApi().killScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isKilled;
    }

    @Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return killJob(jobId.value());
    }

    @Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        boolean isJobKilled = false;
        try {
            isJobKilled = restApi().killJob(sid, jobId);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isJobKilled;
    }

    @Override
    public boolean linkResourceManager(String rmUrl) throws NotConnectedException, PermissionException {
        boolean isLinked = false;
        try {
            isLinked = restApi().linkRm(sid, rmUrl);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isLinked;
    }

    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        boolean isSchedulerPaused = false;
        try {
            isSchedulerPaused = restApi().pauseScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isSchedulerPaused;
    }

    @Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return pauseJob(jobId.value());
    }

    @Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        boolean isJobPaused = false;
        try {
            isJobPaused = restApi().pauseJob(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return isJobPaused;
    }

    @Override
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return preemptTask(jobId.value(), taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        boolean isTaskPreempted = false;
        try {
            isTaskPreempted = restApi().preemptTask(sid, jobId, taskName);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return isTaskPreempted;
    }

    @Override
    public boolean removeJob(JobId arg0) throws NotConnectedException, UnknownJobException,
            PermissionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        boolean isJobRemoved = false;
        try {
            isJobRemoved = restApi().removeJob(sid, jobId);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isJobRemoved;
    }

    @Override
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return restartTask(jobId.value(), taskName, restartDelay);
    }

    @Override
    public boolean restartTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        boolean isTaskRestarted = false;
        try {
            isTaskRestarted = restApi().restartTask(sid, jobId, taskName);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return isTaskRestarted;
    }

    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        boolean isResumed = false;
        try {
            isResumed = restApi().resumeScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isResumed;
    }

    @Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return resumeJob(jobId.value());
    }

    @Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        boolean isJobResumed = false;
        try {
            isJobResumed = restApi().resumeJob(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return isJobResumed;
    }

    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        boolean isShutdown = false;
        try {
            isShutdown = restApi().killScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isShutdown;
    }

    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        boolean success = false;
        try {
            success = restApi().startScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return success;
    }

    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        boolean isStopped = false;
        try {
            isStopped = restApi().stopScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isStopped;
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        JobIdData jobIdData = null;
        try {
            String jobXml = (new Job2XMLTransformer()).jobToxml((TaskFlowJob) job);
            jobIdData = restApiClient().submitXml(sid,
                    IOUtils.toInputStream(jobXml, String.valueOf(Charset.defaultCharset())));
        } catch (Exception e) {
            throwNCEOrPEOrSCEOrJCE(e);
        }
        return jobId(jobIdData);
    }

    @Override
    public JobId submitAsJobArchive(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        String archiveName = String.valueOf(job.hashCode());
        File archiveDir = null;
        try {
            try {
                archiveDir = File.createTempFile(archiveName, "");
                FileUtils.forceDelete(archiveDir);

                if (!archiveDir.mkdir()) {
                    throw new IOException("Unable to create the dir " + archiveDir);
                }
            } catch (IOException e) {
                throw new JobCreationException("Unable to create the archive dir for " + archiveName, e);
            }

            // The xml job descriptor
            File jobXmlFile = new File(archiveDir, "job.xml");

            // The lib.jar that will contain all non jar classpath entries
            final String libJarName = "lib.jar";
            File libJar = new File(archiveDir, libJarName);

            // The self-contained job archive
            File jobArchive = new File(archiveDir, archiveName + ".jobarch");

            // Job archive entries
            ArrayList<String> archiveEntries = new ArrayList<String>();
            archiveEntries.add(jobXmlFile.getAbsolutePath());
            archiveEntries.add(libJar.getAbsolutePath());

            // Jar names that will be pathElement tags in the xml job descriptor
            ArrayList<String> jarsNames = new ArrayList<String>();
            jarsNames.add(libJarName);

            // All dirs that will go inside the lib.jar
            ArrayList<String> dirs = new ArrayList<String>();

            // Get job classpath from job environment
            JobEnvironment originalJobEnv = job.getEnvironment();
            for (String pathElement : originalJobEnv.getJobClasspath()) {
                // Skip pathElement relative to $USERSPACE etc ..
                if (pathElement.startsWith("$")) {
                    jarsNames.add(pathElement);
                } else {
                    if (pathElement.endsWith(".jar")) {
                        archiveEntries.add(pathElement);
                        jarsNames.add(new File(pathElement).getName());
                    } else {
                        dirs.add(pathElement);
                    }
                }
            }

            try { // Jar all non jared dirs
                JarUtils.jar(dirs.toArray(new String[dirs.size()]), libJar, null, null, null, null);
            } catch (IOException e) {
                throw new JobCreationException("Unable to jar non-jared directories " + dirs, e);
            }

            // Add all jars as relative path entries to the unified job env and
            // specify them when packing the self contained jar
            JobEnvironment unifiedJobEnv = new JobEnvironment();
            try {
                unifiedJobEnv.setJobClasspath(jarsNames.toArray(new String[jarsNames.size()]));
            } catch (IOException e) {
                throw new JobCreationException("Unable to set the job classpath of the unified job env", e);
            }

            // Set the unified env before dumping the job xml
            job.setEnvironment(unifiedJobEnv);

            try {// Dump the xml job descriptor
                (new Job2XMLTransformer()).job2xmlFile((TaskFlowJob) job, jobXmlFile);
            } catch (Exception e) {
                throw new JobCreationException("Unable to create the xml job descriptor", e);
            }
            // Set back the original env to keep the job unmodified to the caller
            job.setEnvironment(originalJobEnv);

            try { // Pack all archive entries into the self-contained jobarchive
                JarUtils.zip(archiveEntries.toArray(new String[archiveEntries.size()]), jobArchive, null);
            } catch (IOException e) {
                throw new JobCreationException("Unable to create the job archive", e);
            }

            FileInputStream fis = null;
            JobIdData jobIdData = null;
            try {
                fis = new FileInputStream(jobArchive);
                jobIdData = restApiClient().submitJobArchive(sid, fis);
            } catch (Exception e) {
                throwNCEOrPEOrSCEOrJCE(e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
            return jobId(jobIdData);
        } finally {
            FileUtils.deleteQuietly(archiveDir);
        }
    }

    @Override
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return isJobFinished(jobId.toString());
    }

    @Override
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return !getJobState(jobId).getStatus().isJobAlive();
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout) throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException {
        return waitForJob(jobId.value(), timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout) throws NotConnectedException,
            UnknownJobException, PermissionException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            if (isJobFinished(jobId)) {
                return getJobResult(jobId);
            }
            if (currentTimeMillis() + retry_interval < timeout) {
                sleep(retry_interval);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for the job: job-id=%s", jobId));
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException {
        boolean finished = false;
        try {
            TaskStateData taskStateData = restApi().jobtasks(sid, jobId, taskName);
            TaskState taskState = taskState(taskStateData);
            finished = !taskState.getStatus().isTaskAlive();
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return finished;
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            if (isTaskFinished(jobId, taskName)) {
                return getTaskResult(jobId, taskName);
            }
            if (currentTimeMillis() + retry_interval < timeout) {
                sleep(retry_interval);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for the task: job-id=%s, task-id=%s", jobId,
                taskName));
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout) throws NotConnectedException,
            UnknownJobException, PermissionException, TimeoutException {
        long timestamp = 0;
        List<JobResult> results = new ArrayList<JobResult>();
        for (String jobId : jobIds) {
            timestamp = currentTimeMillis();
            results.add(waitForJob(jobId, timeout));
            timeout = timeout - (currentTimeMillis() - timestamp);
        }
        return results;
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            for (String jobId : jobIds) {
                if (isJobFinished(jobId)) {
                    return toEntry(jobId, getJobResult(jobId));
                }
            }
            if (currentTimeMillis() + retry_interval < timeout) {
                sleep(retry_interval);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for any job: jobIds=%s.", String.valueOf(jobIds)));
    }

    @Override
    public Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            for (String taskName : taskNames) {
                if (isTaskFinished(jobId, taskName)) {
                    return toEntry(taskName, getTaskResult(jobId, taskName));
                }
            }
            if (currentTimeMillis() + retry_interval < timeout) {
                sleep(retry_interval);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for any task: job-id=%s, task-ids=%s.", jobId,
                String.valueOf(taskNames)));
    }

    @Override
    public List<Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        long timestamp = 0;
        List<Map.Entry<String, TaskResult>> taskResults = new ArrayList<Map.Entry<String, TaskResult>>();
        for (String taskName : taskNames) {
            timestamp = currentTimeMillis();
            Entry<String, TaskResult> taskResultEntry = toEntry(taskName, waitForTask(jobId, taskName,
                    timeout));
            taskResults.add(taskResultEntry);
            timeout = timeout - (currentTimeMillis() - timestamp);
        }
        return taskResults;
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file)
            throws NotConnectedException, PermissionException {
        boolean uploaded = false;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            uploaded = restApiClient().pushFile(sid, spacename, pathname, filename, inputStream);
        } catch (Exception e) {
            throwNCEOrPE(e);
        } finally {
            closeIfPossible(inputStream);
        }
        return uploaded;
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile) throws NotConnectedException,
            PermissionException {
        try {
            restApiClient().pullFile(sid, space, pathname, outputFile);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
    }

    @Override
    public boolean deleteFile(String space, String pathname) throws NotConnectedException,
            PermissionException {
        boolean deleted = false;
        try {
            deleted = restApi().deleteFile(sid, space, pathname);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return deleted;
    }

    @Override
    public void renewSession() throws NotConnectedException {
        try {
            sid = restApi().login(login, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setSession(String sid) {
        this.sid = sid;
    }

    @Override
    public String getSession() {
        return sid;
    }

    @Override
    public void addEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        try {
            removeEventListener();
            schedulerEventReceiver = (new SchedulerEventReceiver.Builder()).restServerUrl(url).sessionId(sid)
                    .schedulerEventListener(listener).myEventsOnly(myEventsOnly).selectedEvents(events)
                    .build();
            schedulerEventReceiver.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        if (schedulerEventReceiver != null) {
            schedulerEventReceiver.stop();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    private void closeIfPossible(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private SchedulerRestInterface restApi() {
        return schedulerRestClient.getScheduler();
    }

    private void setApiClient(SchedulerRestClient schedulerRestClient) {
        this.schedulerRestClient = schedulerRestClient;
    }

    private SchedulerRestClient restApiClient() {
        return schedulerRestClient;
    }

    private <K, V> Map.Entry<K, V> toEntry(final K k, final V v) {
        return new AbstractMap.SimpleEntry<K, V>(k, v);

    }
}
