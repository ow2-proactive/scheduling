package org.ow2.proactive_grid_cloud_portal;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;


@Path("/")
public class SchedulerStateRest {
    @GET
    @Path("/jobsids")
    @Produces("application/json")
    public List<String> getJobsIds(@HeaderParam("sessionid") String sessionId) {
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
        System.out.println("sessionid " + sessionId);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        /*
         * try { return s.getState().toString(); } catch (NotConnectedException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } catch (PermissionException e) {
         * e.printStackTrace(); } return "";
         */
    }

    @GET
    @Path("/jobs")
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    @GET
    @Path("/jobs/{jobid}")
    public JobState job(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            JobState js = s.getJobState(jobId);
            System.out.println(js);
            return PAFuture.getFutureValue(js);
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/jobs/{jobid}/result")
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            return PAFuture.getFutureValue(s.getJobResult(jobId));
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @DELETE
    @Path("/jobs/{jobid}")
    public boolean removeJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            s.removeJob(jobId);
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @POST
    @Path("/jobs/{jobid}/kill")
    public void killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) {
        Scheduler s = checkAccess(sessionId);
        try {
            s.killJob(jobId);
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @GET
    @Path("/jobs/{jobid}/tasksids")
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/jobs/{jobid}/tasks/{taskid}")
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskid") String taskid) {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;
        try {
            jobState = s.getJobState(jobId);

            List<String> tasksName = new ArrayList<String>();
            for (TaskState ts : jobState.getTasks()) {
                if (ts.getId().getReadableName().equals(taskid)) {
                    System.out.println(ts);
                    return ts;
                }
            }

        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/jobs/{jobid}/tasks/{taskid}/result")
    public Serializable taskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskid") String taskId) throws Throwable {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;
        try {
            TaskResult tr = s.getTaskResult(jobId, taskId);
            return tr.value();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownJobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownTaskException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    public Scheduler checkAccess(String sessionId) throws WebApplicationException{
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .entity("you are not connected, try to log in first").build());
        }

        return s;
    }

    @POST
    @Path("/jobs/{jobid}/pause")
    public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) {
        final Scheduler s = checkAccess(sessionId);
        try {
            return s.pauseJob(jobId);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        } catch (UnknownJobException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return false;
    }

    @POST
    @Path("/jobs/{jobid}/resume")
    public boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) {
        final Scheduler s = checkAccess(sessionId);
        try {
            return s.resumeJob(jobId);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        } catch (UnknownJobException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
