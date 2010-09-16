package org.ow2.proactive_grid_cloud_portal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
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


@Path("/scheduler/")
public class SchedulerStateRest {
    @GET
    @Path("jobsids")
    @Produces("application/json")
    public List<String> getJobsIds(@HeaderParam("sessionid") String sessionId) {
        Scheduler s = checkAccess(sessionId);
        try {
            List<JobState> jobs = new ArrayList<JobState>();

            jobs.addAll(s.getState().getPendingJobs());
            jobs.addAll(s.getState().getRunningJobs());
            jobs.addAll(s.getState().getFinishedJobs());

            List<String> names = new ArrayList<String>();
            for (JobState j : jobs) {
                names.add(j.getId().toString());
            }

            return names;
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to list the jobs by ids");
        }
        return null;
    }

    @GET
    @Path("jobs")
    @Produces("application/json")
    public List<UserJobInfo> jobs(@HeaderParam("sessionid") String sessionId) {
        Scheduler s = checkAccess(sessionId);
        try {
            List<JobState> jobs = new ArrayList<JobState>();

            jobs.addAll(s.getState().getPendingJobs());
            jobs.addAll(s.getState().getRunningJobs());
            jobs.addAll(s.getState().getFinishedJobs());

            List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
            for (JobState j : jobs) {
                jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j.getJobInfo()));
            }
            return jobInfoList;
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to list the jobs");
        }
        return null;

    }

    @GET
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public JobState job(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            JobState js;
            js = s.getJobState(jobId);
            return PAFuture.getFutureValue(js);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get a description of the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        // somehow impossible to reach
        return null;
    }

    @GET
    @Path("jobs/{jobid}/result")
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            return PAFuture.getFutureValue(s.getJobResult(jobId));
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get result of the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return null;
    }

    @DELETE
    @Path("jobs/{jobid}")
    public boolean removeJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            s.removeJob(jobId);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to remove the job" + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return false;
    }

    @POST
    @Path("jobs/{jobid}/kill")
    public void killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            s.killJob(jobId);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to kill the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }

    }

    @GET
    @Path("jobs/{jobid}/tasksids")
    public List<String> getJobTasksIds(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;
        try {
            jobState = s.getJobState(jobId);
            List<String> tasksName = new ArrayList<String>();
            for (TaskState ts : jobState.getTasks()) {
                tasksName.add(ts.getId().getReadableName());
            }

            return tasksName;
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get the tasks' ids of the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return null;
    }
    
    @GET
    @Path("jobs/{jobid}/tasks")
    public List<TaskStateWrapper> getJobTasks(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;
        try {
            jobState = s.getJobState(jobId);
            List<TaskStateWrapper> taskW= new ArrayList<TaskStateWrapper>();
            for (TaskState ts : jobState.getTasks()) {
                taskW.add(new TaskStateWrapper(ts));
            }
            
            
            return taskW;
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "get the tasks'ids of the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return null;
    }
    

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}")
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskid) {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;
        try {
            jobState = s.getJobState(jobId);

            for (TaskState ts : jobState.getTasks()) {
                if (ts.getId().getReadableName().equals(taskid)) {
                    return ts;
                }
            }

        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get the description of the task " + taskid);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return null;
    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskid}/result")
    public Serializable taskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskid") String taskId) throws Throwable {
        Scheduler s = checkAccess(sessionId);
        try {
            TaskResult tr = s.getTaskResult(jobId, taskId);
            return tr.value();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get the result of the task " + taskId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        } catch (UnknownTaskException e) {
            handleUnknowTaskException(sessionId, taskId);
        }
        return null;
    }

    /**
     * the method check is the session id is valid i.e. a scheduler client
     * is associated to the session id in the session map. If not, a WebApplicationException 
     * is thrown specifying the invalid access      *  
     * @param sessionId
     * @return the scheduler linked to the session id, an WebApplicationException, if no 
     * such mapping exists.
     */
    public Scheduler checkAccess(String sessionId) throws WebApplicationException {
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .entity("you are not connected, try to log in first").build());
        }

        return s;
    }

    /**
     * @param sessionId
     * @throws WebApplicationException http status code 401 Unauthorized
     */
    public void handleNotConnectedException(String sessionId) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
                .entity("you are not connected, try to log in first").build());
    }

    /**
     * @param sessionId
     * @param message
     * @throws WebApplicationException http status code 403 Forbidden
     */
    public void handlePermissionException(String sessionId, String message) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_FORBIDDEN)
                .entity("you are not authorized to perform the action:" + message).build());
    }

    /**
     * @param sessionId
     * @param jobId
     * @throws WebApplicationException http status code 404 Not Found
     */
    public void handleUnknowJobException(String sessionId, String jobId) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
                .entity("job " + jobId + "not found").build());
    }
    
    /**
     * @param sessionId
     * @param jobId
     * @throws WebApplicationException http status code 404 Not Found
     */
    public void handleSubmissionClosedJobException(String sessionId) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
                .entity("the scheduler is stopped, you cannot submit a job").build());
    }
    /**
     * @param sessionId
     * @param taskId
     * @throws WebApplicationException http status code 404 Not Found
     */
    public void handleUnknowTaskException(String sessionId, String taskId) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
                .entity("task " + taskId + "not found").build());
    }

    

    /**
     * @param sessionId
     * @param taskId
     * @throws WebApplicationException http status code 404 Not Found
     */
    public void handleJobAlreadyFinishedException(String sessionId, String msg) throws WebApplicationException {
        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
                .entity(msg).build());
    }
    
    @POST
    @Path("jobs/{jobid}/pause")
    public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) {
        final Scheduler s = checkAccess(sessionId);
        try {
            return s.pauseJob(jobId);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to pause the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return false;
    }

    @POST
    @Path("jobs/{jobid}/resume")
    public boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) {
        final Scheduler s = checkAccess(sessionId);
        try {
            return s.resumeJob(jobId);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to resume the job " + jobId);
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        }
        return false;
    }
    
    
    @POST
    @Path("submit")
    public JobId submit(@HeaderParam("sessionid") String sessionId, MultipartInput multipart) {
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
        System.out.println("sessionid " + sessionId);
        File tmp;
        try {
            tmp = File.createTempFile("prefix", "suffix");
            for (InputPart part : multipart.getParts()) {

                BufferedWriter outf = new BufferedWriter(new FileWriter(tmp));
                outf.write(part.getBodyAsString());
                outf.close();

            }

            Job j = JobFactory.getFactory().createJob(tmp.getAbsolutePath());
            return s.submit(j);
        } catch (JobCreationException e) {
            
        } catch (NotConnectedException e) {
           handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to submit a job");
        } catch (SubmissionClosedException e) {
            handleSubmissionClosedJobException(sessionId);
        } catch (IOException e) {
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_GONE)
                    .entity("The scheduler is not available").build());
        }

        return null;
    }

    @PUT
    @Path("disconnect")
    public void disconnet(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             s.disconnect();
             PAActiveObject.terminateActiveObject(s, true);
             SchedulerSessionMapper.getInstance().getSessionsMap().remove(sessionId);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to disconnect from the scheduler ");
        }
    }
    
    
    @PUT
    @Path("pause")
    public boolean pauseScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.pause();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to pause the scheduler ");
        }
        return false;
    }
    
    @PUT
    @Path("stop")
    public boolean stopScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.stop();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to stop the scheduler ");
        }
        return false;
    }    
    
    @PUT
    @Path("resume")
    public boolean resumeScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.resume();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to resume the scheduler ");
        }
        return false;
    }
    
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public void schedulerChangeJobPriorityByName(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("name") String priorityName) {
        final Scheduler s = checkAccess(sessionId);
        try {
             s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to change the piority of the job " + jobId);
        } catch (UnknownJobException e) {
          handleUnknowJobException(sessionId, jobId);
        } catch (JobAlreadyFinishedException e) {
            handleJobAlreadyFinishedException(sessionId, e.getMessage());
        }
        
    }
    
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("value") String priorityValue) {
        final Scheduler s = checkAccess(sessionId);
        try {
             s.changeJobPriority(jobId, JobPriority.findPriority(Integer.parseInt(priorityValue)));
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to change the piority of the job " + jobId);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            handleUnknowJobException(sessionId, jobId);
        } catch (JobAlreadyFinishedException e) {
            handleJobAlreadyFinishedException(sessionId, e.getMessage());
        }
        
    }
    
    
    @PUT
    @Path("freeze")
    public boolean  freezeScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.freeze();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to freeze the scheduler ");
        }
        return false;
    }
    
    
    @GET
    @Path("status")
    public SchedulerStatus getSchedulerStatus(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.getStatus();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to get the scheduler' status ");
        }
        return null;
    }
    
    @PUT
    @Path("start")
    public boolean startScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.start();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to start the scheduler ");
        }
        return false;
    }
    
    @PUT
    @Path("kill")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.kill();
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to kill the scheduler ");
        }
        return false;
    }
    
    @POST
    @Path("linkrm")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId, 
            @FormParam("rmurl") String rmURL) {
        final Scheduler s = checkAccess(sessionId);
        try {
             return s.linkResourceManager(rmURL);
        } catch (NotConnectedException e) {
            handleNotConnectedException(sessionId);
        } catch (PermissionException e) {
            handlePermissionException(sessionId, "to link a resource manager to the scheduler ");
        }
        return false;
    }
    
    
    
}
