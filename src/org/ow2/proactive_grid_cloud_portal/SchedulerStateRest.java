package org.ow2.proactive_grid_cloud_portal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.collection.PersistentMap;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.ext.filessplitmerge.schedulertools.SchedulerProxyUserInterface;


@XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
@Path("/scheduler/")
public class SchedulerStateRest {


    @GET
    @Path("jobsids")
    @Produces("application/json")
    public List<String> getJobsIds(@HeaderParam("sessionid") String sessionId) throws NotConnectedException,
            PermissionException {
        Scheduler s = null;

        s = checkAccess(sessionId);

        List<JobState> jobs = new ArrayList<JobState>();

        jobs.addAll(s.getState().getPendingJobs());
        jobs.addAll(s.getState().getRunningJobs());
        jobs.addAll(s.getState().getFinishedJobs());

        List<String> names = new ArrayList<String>();
        for (JobState j : jobs) {
            names.add(j.getId().toString());
        }

        return names;

    }

    @GET
    @Path("jobs")
    @Produces({ "application/json", "application/xml" })
    public List<UserJobInfo> jobs(@HeaderParam("sessionid") String sessionId) throws PermissionException,
            NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        List<JobState> jobs = new ArrayList<JobState>();

        jobs.addAll(s.getState().getPendingJobs());
        jobs.addAll(s.getState().getRunningJobs());
        jobs.addAll(s.getState().getFinishedJobs());

        List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
        for (JobState j : jobs) {
            jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j.getJobInfo()));
        }
        return jobInfoList;

    }

    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    @XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
    public MyJobState job(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        ResteasyProviderFactory dispatcher = ResteasyProviderFactory.getInstance();
        dispatcher.addStringConverter(RestartModeConverter.class);
        dispatcher.addStringConverter(IntWrapperConverter.class);
        dispatcher.registerProvider(PersistentMapConverter.class);

        JobState js;
        js = s.getJobState(jobId);
        js = PAFuture.getFutureValue(js);
      
        return new MyJobState(js);
      

    }

    @GET
    @Path("jobs/{jobid}/result")
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        Scheduler s = checkAccess(sessionId);

        return PAFuture.getFutureValue(s.getJobResult(jobId));

    }

    @DELETE
    @Path("jobs/{jobid}")
    public boolean removeJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.removeJob(jobId);
    }

    @POST
    @Path("jobs/{jobid}/kill")
    public boolean killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.killJob(jobId);

    }

    @GET
    @Path("jobs/{jobid}/tasksids")
    public List<String> getJobTasksIds(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);
        List<String> tasksName = new ArrayList<String>();
        for (TaskState ts : jobState.getTasks()) {
            tasksName.add(ts.getId().getReadableName());
        }

        return tasksName;

    }

    @GET
    @Path("jobs/{jobid}/tasks")
    public List<TaskStateWrapper> getJobTasks(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);
        List<TaskStateWrapper> taskW = new ArrayList<TaskStateWrapper>();
        for (TaskState ts : jobState.getTasks()) {
            taskW.add(new TaskStateWrapper(ts));
        }

        return taskW;

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}")
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskid) throws NotConnectedException, UnknownJobException,
            PermissionException, UnknownTaskException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);

        for (TaskState ts : jobState.getTasks()) {
            if (ts.getId().getReadableName().equals(taskid)) {
                return ts;
            }
        }

        throw new UnknownTaskException("task " + taskid + "not found");
    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}/result")
    @Produces("*/*")
    public Serializable taskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskid") String taskId) throws Throwable {
        Scheduler s = checkAccess(sessionId);

        TaskResult tr = s.getTaskResult(jobId, taskId);
        return tr.value();
    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}/log/all")
    @Produces("*/*")
    public String tasklog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskId) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.getTaskResult(jobId, taskId).getOutput().getAllLogs(true);

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}/log/err")
    @Produces("*/*")
    public String tasklogErr(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskId) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);

        return s.getTaskResult(jobId, taskId).getOutput().getStderrLogs(true);

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}/log/out")
    @Produces("*/*")
    public String tasklogout(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskId) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.getTaskResult(jobId, taskId).getOutput().getStdoutLogs(true);

    }

    /**
     * the method check is the session id is valid i.e. a scheduler client
     * is associated to the session id in the session map. If not, a NotConnectedException  
     * is thrown specifying the invalid access      *  
     * @param sessionId
     * @return the scheduler linked to the session id, an NotConnectedException, if no 
     * such mapping exists.
     * @throws NotConnectedException 
     */
    public Scheduler checkAccess(String sessionId) throws NotConnectedException {
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }

        return s;
    }

    //    /**
    //     * @param sessionId
    //     * @param message
    //     * @throws WebApplicationException http status code 403 Forbidden
    //     */
    //    public void handlePermissionException(String sessionId, String message) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_FORBIDDEN)
    //                .entity("you are not authorized to perform the action:" + message).build());
    //    }

    //    /**
    //     * @param sessionId
    //     * @param jobId
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleUnknowJobException(String sessionId, String jobId) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity("job " + jobId + "not found").build());
    //    }

    //    /**
    //     * @param sessionId
    //     * @param jobId
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleSubmissionClosedJobException(String sessionId) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity("the scheduler is stopped, you cannot submit a job").build());
    //    }
    //    /**
    //     * @param sessionId
    //     * @param taskId
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleUnknowTaskException(String sessionId, String taskId) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity("task " + taskId + "not found").build());
    //    }

    //    /**
    //     * @param sessionId
    //     * @param taskId
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleJobAlreadyFinishedException(String sessionId, String msg) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity(msg).build());
    //    }

    @POST
    @Path("jobs/{jobid}/pause")
    public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);

        return s.pauseJob(jobId);

    }

    @POST
    @Path("jobs/{jobid}/resume")
    public boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.resumeJob(jobId);

    }

    @POST
    @Path("submit")
    public JobId submit(@HeaderParam("sessionid") String sessionId, MultipartInput multipart)
            throws IOException, JobCreationException, NotConnectedException, PermissionException,
            SubmissionClosedException {
        Scheduler s = checkAccess(sessionId);
        File tmp;

        tmp = File.createTempFile("prefix", "suffix");
        for (InputPart part : multipart.getParts()) {

            BufferedWriter outf = new BufferedWriter(new FileWriter(tmp));
            outf.write(part.getBodyAsString());
            outf.close();

        }

        Job j = JobFactory.getFactory().createJob(tmp.getAbsolutePath());
        return s.submit(j);

    }

    @PUT
    @Path("disconnect")
    public void disconnet(@HeaderParam("sessionid") final String sessionId) throws NotConnectedException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);

        s.disconnect();
        PAActiveObject.terminateActiveObject(s, true);
        SchedulerSessionMapper.getInstance().getSessionsMap().remove(sessionId);

    }

    @PUT
    @Path("pause")
    public boolean pauseScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.pause();

    }

    @PUT
    @Path("stop")
    public boolean stopScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.stop();
    }

    @PUT
    @Path("resume")
    public boolean resumeScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.resume();

    }

    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public void schedulerChangeJobPriorityByName(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("name") String priorityName)
            throws NotConnectedException, UnknownJobException, PermissionException,
            JobAlreadyFinishedException {
        final Scheduler s = checkAccess(sessionId);
        s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));

    }

    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("value") String priorityValue)
            throws NumberFormatException, NotConnectedException, UnknownJobException, PermissionException,
            JobAlreadyFinishedException {
        final Scheduler s = checkAccess(sessionId);
        s.changeJobPriority(jobId, JobPriority.findPriority(Integer.parseInt(priorityValue)));

    }

    @PUT
    @Path("freeze")
    public boolean freezeScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.freeze();

    }

    @GET
    @Path("status")
    public SchedulerStatus getSchedulerStatus(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getStatus());

    }

    @PUT
    @Path("start")
    public boolean startScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.start();

    }

    @PUT
    @Path("kill")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.kill();

    }

    @POST
    @Path("linkrm")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId,
            @FormParam("rmurl") String rmURL) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.linkResourceManager(rmURL);

    }

}
