package org.ow2.proactive_grid_cloud_portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
    @Path("/jobs")
    @Produces("application/json")
    public  List<String> jobs(@HeaderParam("sessionid") String sessionId) {
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
        try {
            return s.getState().toString();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return "";
        */
    }
    
    @GET
    @Path("/jobs/{jobid}")
    public JobState job(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
    try {
        JobState js = s.getJobState(jobId);
        System.out.println(js);
        return js;
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
    @Path("/jobs/{jobid}")
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
    try {
        return s.getJobResult(jobId);
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
    public boolean removeJob(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
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
    public void killJob(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
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

    }
    
  
    
    @GET
    @Path("/jobs/{jobid}/tasks")
    public List<String> jobtasks(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
    JobState jobState; 
    try {
        jobState = s.getJobState(jobId);
        System.out.println("jobState " + jobId + " : " + jobState);
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
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskid") String taskid) {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
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
            @PathParam("jobid") String jobId,@PathParam("taskid") String taskId) throws Throwable {
    Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
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
    
    
}
