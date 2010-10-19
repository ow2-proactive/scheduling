package org.ow2.proactive_grid_cloud_portal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
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
import org.ow2.proactive.scheduler.task.TaskResultImpl;


@XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
@Path("/scheduler/")
public class SchedulerStateRest {
	/** If the rest api was unable to instantiate the value from byte array representation*/	
	public static final String UNKNOWN_VALUE_TYPE = "Unknown value type";	
	
    @GET
    @Path("jobs")
    @Produces("application/json")
    public List<String> getJobsIds(@HeaderParam("sessionid") String sessionId) throws NotConnectedException,
            PermissionException {
        Scheduler s = null;

        s = checkAccess(sessionId);

        List<JobState> jobs = new ArrayList<JobState>();
        SchedulerState state = s.getState();
        jobs.addAll(state.getPendingJobs());
        jobs.addAll(state.getRunningJobs());
        jobs.addAll(state.getFinishedJobs());

        List<String> names = new ArrayList<String>();
        for (JobState j : jobs) {
            names.add(j.getId().toString());
        }

        return names;

    }

    @GET
    @Path("jobsinfo")
    @Produces({ "application/json", "application/xml" })
    public List<UserJobInfo> jobs(@HeaderParam("sessionid") String sessionId) throws PermissionException,
            NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        List<JobState> jobs = new ArrayList<JobState>();
        SchedulerState state = s.getState();
        jobs.addAll(state.getPendingJobs());
        jobs.addAll(state.getRunningJobs());
        jobs.addAll(state.getFinishedJobs());

        List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
        for (JobState j : jobs) {
            jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j.getJobInfo()));
        }
        return jobInfoList;

    }
    
    @GET
    @Path("state")
    @Produces({ "application/json", "application/xml" })
    public SchedulerState schedulerState(@HeaderParam("sessionid") String sessionId) throws PermissionException,
            NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getState());
    }
    
    @GET
    @Path("state/myjobsonly")
    @Produces({ "application/json", "application/xml" })
    public SchedulerState getSchedulerStateMyJobsOnly(@HeaderParam("sessionid") String sessionId) throws PermissionException,
            NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getState(true));
    }   

    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    @XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
    public JobState job(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);

        JobState js;
        js = s.getJobState(jobId);
        js = PAFuture.getFutureValue(js);
      
        return js;      
    }

    @GET
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
        
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        Scheduler s = checkAccess(sessionId);
        
        return PAFuture.getFutureValue(s.getJobResult(jobId));
    }    
    
    @GET
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")        
    public Map<String, Serializable> jobResultValue(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
    throws NotConnectedException, PermissionException, UnknownJobException {
    	Scheduler s = checkAccess(sessionId);
    	JobResult jobResult = PAFuture.getFutureValue(s.getJobResult(jobId));
    	if (jobResult == null) {
    		return null;
    	}
    	Map<String, TaskResult> allResults = jobResult.getAllResults();
    	Map<String, Serializable> res = new HashMap<String, Serializable>(allResults.size());
    	for(final Entry<String, TaskResult> entry : allResults.entrySet()){        	
    		TaskResult taskResult = entry.getValue();
    		String value = null;
    		// No entry if the task had exception
    		if (taskResult.hadException()) {
    			value = taskResult.getException().getMessage();
    		} else {        		
    			try {
    				Serializable instanciatedValue = taskResult.value();
    				if (instanciatedValue != null) {
    					value = instanciatedValue.toString();
    				}
    			} catch (InternalSchedulerException e) {
    				value = UNKNOWN_VALUE_TYPE;
    			} catch (Throwable t) {
    				value = "Unable to get the value due to " + t.getMessage();
    			}
    		}
    		res.put(entry.getKey(), value);
    	}        
    	return res;  
    }    

    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public boolean removeJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.removeJob(jobId);
    }

    @POST
    @Path("jobs/{jobid}/kill")   
    @Produces("application/json")
    public boolean killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.killJob(jobId);

    }

    @GET
    @Path("jobs/{jobid}/tasks")  
    @Produces("application/json")
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
    @Path("jobs/{jobid}/taskstates")  
    @Produces("application/json")
    public List<TaskState> getJobTaskStates(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);

        List<TaskState> tasks = new ArrayList<TaskState>();
      for (TaskState ts : jobState.getTasks()) {
          tasks.add(ts);
      } 
        
        return tasks;

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")  
    @Produces("application/json")
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedException, UnknownJobException,
            PermissionException, UnknownTaskException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);

        for (TaskState ts : jobState.getTasks()) {
            if (ts.getId().getReadableName().equals(taskname)) {
                return ts;
            }
        }

        throw new UnknownTaskException("task " + taskname + "not found");
    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    public Serializable valueOftaskresult(@HeaderParam("sessionid") String sessionId,
    		@PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws Throwable {
    	Scheduler s = checkAccess(sessionId);
//    	TaskResult taskResult = s.getTaskResult(jobId, taskname);
    	TaskResult taskResult = workaroundforSCHEDULING863(s, jobId, taskname);
    	if (taskResult==null){
    		// task is not finished yet
    		return null;
    	}
    	String value = null;
    	// No entry if the task had exception
    	if (taskResult.hadException()) {
    		value = taskResult.getException().getMessage();
    	} else {        		
    		try {
    			Serializable instanciatedValue = taskResult.value();
    			if (instanciatedValue != null) {
    				value = instanciatedValue.toString();
    			}
    		} catch (InternalSchedulerException e) {
    			value = UNKNOWN_VALUE_TYPE;
    		} catch (Throwable t) {
    			value = "Unable to get the value due to " + t.getMessage();
    		}
    	}               
    	return value;
    }
    
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    public Serializable serializedValueOftaskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws Throwable {
        Scheduler s = checkAccess(sessionId);
//        TaskResult tr = s.getTaskResult(jobId, taskname);
        TaskResult tr = workaroundforSCHEDULING863(s, jobId, taskname);
        tr = PAFuture.getFutureValue(tr);
        return ((TaskResultImpl)tr).getSerializedValue();
    }
    
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    public TaskResult taskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
//        TaskResult tr = s.getTaskResult(jobId, taskname);     
        TaskResult tr = workaroundforSCHEDULING863(s, jobId, taskname);
        return PAFuture.getFutureValue(tr);
    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("*/*")
    public String tasklog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = workaroundforSCHEDULING863(s, jobId, taskname);
        return tr.getOutput().getAllLogs(true);

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("*/*")
    public String tasklogErr(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = workaroundforSCHEDULING863(s, jobId, taskname);
        return tr.getOutput().getStderrLogs(true);

    }

    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("*/*")
    public String tasklogout(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = workaroundforSCHEDULING863(s, jobId, taskname);
        return tr.getOutput().getStdoutLogs(true);

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
    //     * @param taskname
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleUnknowTaskException(String sessionId, String taskname) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity("task " + taskname + "not found").build());
    //    }

    //    /**
    //     * @param sessionId
    //     * @param taskname
    //     * @throws WebApplicationException http status code 404 Not Found
    //     */
    //    public void handleJobAlreadyFinishedException(String sessionId, String msg) throws WebApplicationException {
    //        throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    //                .entity(msg).build());
    //    }

    @POST
    @Path("jobs/{jobid}/pause")   
    @Produces("application/json")
    public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);

        return s.pauseJob(jobId);

    }

    @POST
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.resumeJob(jobId);

    }

    @POST
    @Path("submit")      
    @Produces("application/json")
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
    @Produces("application/json")
    public void disconnect(@HeaderParam("sessionid") final String sessionId) throws NotConnectedException,
            PermissionException {
        final Scheduler s = checkAccess(sessionId);

        s.disconnect();
        PAActiveObject.terminateActiveObject(s, true);
        SchedulerSessionMapper.getInstance().getSessionsMap().remove(sessionId);

    }

    @PUT
    @Path("pause")
    @Produces("application/json")
    public boolean pauseScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.pause();

    }

    @PUT
    @Path("stop") 
    @Produces("application/json")
    public boolean stopScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.stop();
    }

    @PUT
    @Path("resume") 
    @Produces("application/json")
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
    @Produces("application/json")
    public boolean freezeScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.freeze();

    }

    @GET
    @Path("status")   
    @Produces("application/json")
    public SchedulerStatus getSchedulerStatus(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getStatus());

    }

    @PUT
    @Path("start")  
    @Produces("application/json")
    public boolean startScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.start();

    }

    @PUT
    @Path("kill")  
    @Produces("application/json")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.kill();

    }

    @POST
    @Path("linkrm")   
    @Produces("application/json")
    public boolean killScheduler(@HeaderParam("sessionid") final String sessionId,
            @FormParam("rmurl") String rmURL) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.linkResourceManager(rmURL);

    }
    
    @PUT
    @Path("isconnected")
    @Produces("application/json")
    public boolean isConnected(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.isConnected();
    }

    
    private TaskResult workaroundforSCHEDULING863(Scheduler s,String jobId, String taskName) throws UnknownTaskException, NotConnectedException, UnknownJobException, PermissionException {
        try {
            return s.getTaskResult(jobId, taskName);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("has not been found in this job result")) {
                throw new UnknownTaskException("Result of task " + taskName + " does not exist or task is not yet finished");
            } else {
              throw e;
            }
        }
        
    }
    
}
