/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentMap;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
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
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


/**
 * 
 * this class exposes the Scheduler as a RESTful service.
 *
 */
@XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
@Path("/scheduler/")
public class SchedulerStateRest {
    /** If the rest api was unable to instantiate the value from byte array representation*/
    public static final String UNKNOWN_VALUE_TYPE = "Unknown value type";
    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest");
    private CachingSchedulerProxyUserInterface cachedState;
    private volatile boolean isCacheEnabled = true;

    /**
     * Returns the ids of the current jobs under a list of string.
     * @param sessionId a valid session id
     * @return a list of jobs' ids under the form of a list of string
    */
    @GET
    @Path("jobs")
    @Produces("application/json")
    public List<String> jobs(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException, PermissionException {
        Scheduler s = null;

        s = checkAccess(sessionId);

        List<JobState> jobs = new ArrayList<JobState>();
        SchedulerState state = SchedulerStateCaching.getLocalState();
//        s.getState();
        jobs.addAll(state.getPendingJobs());
        jobs.addAll(state.getRunningJobs());
        jobs.addAll(state.getFinishedJobs());

        List<String> names = new ArrayList<String>();
        for (JobState j : jobs) {
            names.add(j.getId().toString());
        }

        return names;

    }

    /**
     * Returns a subset of the scheduler state, including pending, running, finished
     * jobs (in this particular order).
     * each jobs is described using
     *   - its id
     *   - its owner
     *   - the JobInfo class
     * @param sessionId a valid session id
     * @return a list of UserJobInfo
     */
    @GET
    @Path("jobsinfo")
    @Produces( { "application/json", "application/xml" })
    public List<UserJobInfo> jobsinfo(@HeaderParam("sessionid")
    String sessionId) throws PermissionException, NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        List<JobState> jobs = new ArrayList<JobState>();
        SchedulerState state = SchedulerStateCaching.getLocalState();
//        s.getState();
        jobs.addAll(state.getPendingJobs());
        jobs.addAll(state.getRunningJobs());
        jobs.addAll(state.getFinishedJobs());

        List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
        for (JobState j : jobs) {
            jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j.getJobInfo()));
        }
        return jobInfoList;

    }

    /**
     * Returns the state of the scheduler
     * @param sessionId a valid session id.
     * @return the scheduler state 
     */
    @GET
    @Path("state")
    @Produces( { "application/json", "application/xml" })
    public SchedulerState schedulerState(@HeaderParam("sessionid")
    String sessionId) throws PermissionException, NotConnectedException {
        Scheduler s = checkAccess(sessionId);
//        return PAFuture.getFutureValue(s.getState());
        return SchedulerStateCaching.getLocalState();
    }

    /**
     * Returns the revision number of the scheduler state
     * @param sessionId a valid session id.
     * @return the revision of the scheduler state 
     */
    @GET
    @Path("state/revision")
    @Produces( { "application/json", "application/xml" })
    public long schedulerStateRevision(@HeaderParam("sessionid")
    String sessionId) throws PermissionException, NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        return SchedulerStateCaching.getSchedulerRevision();
    }
    
    /**
     * returns only the jobs of the current user
     * @param sessionId a valid session id
     * @return a scheduler state that contains only the jobs of the user that
     * owns the session <code>sessionid</code>  
    */
    @GET
    @Path("state/myjobsonly")
    @Produces( { "application/json", "application/xml" })
    public SchedulerState getSchedulerStateMyJobsOnly(@HeaderParam("sessionid")
    String sessionId) throws PermissionException, NotConnectedException {
        Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getState(true));
    }

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     * @param sessionid a valid session id
     * @param jobid the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}")
    @Produces( { "application/json", "application/xml" })
    @XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
    public JobState listJobs(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);

        JobState js;
        js = s.getJobState(jobId);
        js = PAFuture.getFutureValue(js);

        return js;
    }

    /**
     * Returns the job result associated to the job referenced by the 
     * id <code>jobid</code>
     * @param sessionid a valid session id
     * @result the job result of the corresponding job  
     */
    @GET
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    public JobResult jobResult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        Scheduler s = checkAccess(sessionId);

        return PAFuture.getFutureValue(s.getJobResult(jobId));
    }

    /**
     * Returns all the task results of this job as a map whose the key is the
     * name of the task and its task result.<br>
     * If the result cannot be instantiated, the content is replaced by the 
     * string 'Unknown value type'. To get the serialized form of a given result,
     * one has to call the following restful service 
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * @param sessionid a valid session id
     * @param jobid a job id
     */
    @GET
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    public Map<String, Serializable> jobResultValue(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        Scheduler s = checkAccess(sessionId);
        JobResult jobResult = PAFuture.getFutureValue(s.getJobResult(jobId));
        if (jobResult == null) {
            return null;
        }
        Map<String, TaskResult> allResults = jobResult.getAllResults();
        Map<String, Serializable> res = new HashMap<String, Serializable>(allResults.size());
        for (final Entry<String, TaskResult> entry : allResults.entrySet()) {
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

    /**
     * Delete a job
     * @param sessionId a valid session id
     * @param jobId the id of the job to delete
     * @return true if success, false if the job not yet finished (not removed,
     * kill the job then remove it) 
     * 
     */
    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public boolean removeJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.removeJob(jobId);
    }

    /**
    * Kill the job represented by jobId.<br>
    *
    * @param sessionId a valid session id
    * @param jobId the job to kill.
    * @return true if success, false if not.
    */
    @PUT
    @Path("jobs/{jobid}/kill")
    @Produces("application/json")
    public boolean killJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        return s.killJob(jobId);

    }

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code>
     * @param sessionId a valid session id
     * @param the jobid one wants to list the tasks' name
     * @return a list of tasks' name 
     */
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    public List<String> getJobTasksIds(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);
        List<String> tasksName = new ArrayList<String>();
        for (TaskState ts : jobState.getTasks()) {
            tasksName.add(ts.getId().getReadableName());
        }

        return tasksName;

    }

    /**
     * Returns a list of taskState 
     * @param sessionId a valid session id
     * @param jobId the job id
     * @return a list of task' states of the job <code>jobId</code>
     */
    @GET
    @Path("jobs/{jobid}/taskstates")
    @Produces("application/json")
    public List<TaskState> getJobTaskStates(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Scheduler s = checkAccess(sessionId);
        JobState jobState;

        jobState = s.getJobState(jobId);

        List<TaskState> tasks = new ArrayList<TaskState>();
        for (TaskState ts : jobState.getTasks()) {
            tasks.add(ts);
        }

        return tasks;

    }

    /**
     * Return the task state of the task <code>taskname</code> of the job <code>jobId</code> 
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the task state of the task  <code>taskname</code> of the job <code>jobId</code> 
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")
    @Produces("application/json")
    public TaskState jobtasks(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedException, UnknownJobException, PermissionException,
            UnknownTaskException {
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

    /**
     * Returns the value of the task result of task <code>taskName</code> of the job <code>jobId</code>
     * <strong>the result is deserialized before sending to the client, if the class is
     * not found the content is replaced by the string 'Unknown value type' </strong>. To get the serialized form of a given result,
     * one has to call the following restful service 
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the value of the task result
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    public Serializable valueOftaskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws Throwable {
        Scheduler s = checkAccess(sessionId);
        //    	TaskResult taskResult = s.getTaskResult(jobId, taskname);
        TaskResult taskResult = s.getTaskResult(jobId, taskname);
        if (taskResult == null) {
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

    /**
     * Returns the value of the task result of the task <code>taskName</code> of the job <code>jobId</code>
     * This method returns the result as a byte array whatever the result is.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the value of the task result as a byte array.
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    public byte[] serializedValueOftaskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws Throwable {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = s.getTaskResult(jobId, taskname);
        tr = PAFuture.getFutureValue(tr);
        return ((TaskResultImpl) tr).getSerializedValue();
    }

    /**
     * Returns the task result of the task <code>taskName</code> 
     * of the job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the task result of the task <code>taskName</code> 
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    public TaskResult taskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedException, UnknownJobException, UnknownTaskException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = s.getTaskResult(jobId, taskname);
        return PAFuture.getFutureValue(tr);
    }

    /**
     *  Returns all the logs generated by the task (either stdout and stderr)
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  all the logs generated by the task (either stdout and stderr)
    */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("*/*")
    public String tasklog(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedException, UnknownJobException, UnknownTaskException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = s.getTaskResult(jobId, taskname);
        return tr.getOutput().getAllLogs(true);

    }

    /**
     *  Returns the standard error output (stderr) generated by the task
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  the stderr generated by the task
    */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("*/*")
    public String tasklogErr(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedException, UnknownJobException, UnknownTaskException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = s.getTaskResult(jobId, taskname);
        return tr.getOutput().getStderrLogs(true);

    }

    /**
     *  Returns the standard output (stderr) generated by the task
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  the stdout generated by the task
    */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("*/*")
    public String tasklogout(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedException, UnknownJobException, UnknownTaskException,
            PermissionException {
        Scheduler s = checkAccess(sessionId);
        TaskResult tr = s.getTaskResult(jobId, taskname);
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
            logger.trace("not found a scheduler frontend for sessionId " + sessionId);
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }
        logger.trace("found a scheduler frontend for sessionId " + sessionId);
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

    /**
     * Pauses the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @POST
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    public boolean pauseJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        final Scheduler s = checkAccess(sessionId);

        return s.pauseJob(jobId);

    }

    /**
     * Resumes the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @POST
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public boolean resumeJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.resumeJob(jobId);

    }

    /**
     * Submits a job to the scheduler 
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return the <code>jobid</code> of the newly created job 
     */
    @POST
    @Path("submit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public JobId submit(@HeaderParam("sessionid")
    String sessionId, MultipartInput multipart) throws IOException, JobCreationException,
            NotConnectedException, PermissionException, SubmissionClosedException {
        Scheduler s = checkAccess(sessionId);
        File tmp;

        tmp = File.createTempFile("prefix", "suffix");
        for (InputPart part : multipart.getParts()) {
            BufferedWriter outf = new BufferedWriter(new FileWriter(tmp));
            outf.write(part.getBodyAsString());
            outf.close();
        }

        Job j = JobFactory.getFactory().createJob(tmp.getAbsolutePath());

        // clean the temporary file
        tmp.delete();

        return s.submit(j);

    }

    /*
      // Kept for later use if we discover that using MultipartInput does not
      // longer match our needs. Uploads of byte-encoded job descriptors for instance.
       
    @POST
    @Path("submit")
    
    public String submit(@Context HttpServletRequest req) throws Exception {

     FileItemFactory factory = new DiskFileItemFactory();
     ServletFileUpload upload = new ServletFileUpload(factory);

     List  items = upload.parseRequest(req);
     Iterator iter = items.iterator();

     while (iter.hasNext()) {
         FileItem item = (FileItem) iter.next();

         if (item.isFormField()) {
             System.out.println("FORM FIELD");

         } else {
          if (!item.isFormField()) {

              String fileName = item.getName();
              System.out.println("File Name:" + fileName);

           File fullFile  = new File(item.getName());  
           File savedFile = new File("/test", fullFile.getName());

           item.write(savedFile);
          }
         }
     }

     return "OK";
    }
    
     */

    /**
     * terminates the session id <code>sessionId</code>
     * @param sessionId a valid session id
     * @throws NotConnectedException if the scheduler cannot be contacted
     * @throws PermissionException if you are not authorized to perform the action
     */
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    public void disconnect(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);

        try {
            s.disconnect();
        } finally {
            SchedulerSessionMapper.getInstance().getSessionsMap().remove(sessionId);
            PAActiveObject.terminateActiveObject(s, true);
            logger.debug("sessionid " + sessionId + "terminated");
        }

    }

    /**
     * pauses the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    public boolean pauseScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.pause();

    }

    /**
     * stops the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    public boolean stopScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.stop();
    }

    /**
     * resumes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    public boolean resumeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.resume();

    }

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityName a string representing the name of the priority
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public void schedulerChangeJobPriorityByName(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("name")
    String priorityName) throws NotConnectedException, UnknownJobException, PermissionException,
            JobAlreadyFinishedException {
        final Scheduler s = checkAccess(sessionId);
        s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));

    }

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityValue a string representing the value of the priority
     * @throws NumberFormatException
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("value")
    String priorityValue) throws NumberFormatException, NotConnectedException, UnknownJobException,
            PermissionException, JobAlreadyFinishedException {
        final Scheduler s = checkAccess(sessionId);
        s.changeJobPriority(jobId, JobPriority.findPriority(Integer.parseInt(priorityValue)));

    }

    /**
     * freezes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    public boolean freezeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.freeze();

    }

    /**
     * returns the status of the scheduler 
     * @param sessionId a valid session id
     * @return the scheduler status
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("status")
    @Produces("application/json")
    public SchedulerStatus getSchedulerStatus(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return PAFuture.getFutureValue(s.getStatus());

    }

    /**
     * starts the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    public boolean startScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.start();

    }

    /**
     * kills and shutdowns the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false if not
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    public boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.kill();

    }

    /**
     * Reconnect a new Resource Manager to the scheduler. 
     * Can be used if the resource manager has crashed.
     * @param sessionId a valid session id
     * @param rmURL the url of the resource manager 
     * @return true if success, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    public boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId, @FormParam("rmurl")
    String rmURL) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.linkResourceManager(rmURL);

    }

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     * @param sessionId the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("isconnected")
    @Produces("application/json")
    public boolean isConnected(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedException, PermissionException {
        final Scheduler s = checkAccess(sessionId);
        return s.isConnected();
    }

    /**
     * login to the scheduler using an form containing 2 fields (username & password)
     *  
     * @param username username
     * @param password password 
     * @return the session id associated to the login
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws LoginException
     * @throws SchedulerException
     * @throws KeyException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("login")
    public String login(@FormParam("username")
    String username, @FormParam("password")
    String password) throws ActiveObjectCreationException, NodeException, LoginException, SchedulerException,
            KeyException {

        // activate the cache mechanism at first login
        /*
        synchronized(this) {
            try {
            if ((isCacheEnabled) && (cachedState == null)){
                cachedState  = PAActiveObject.newActive(
                        CachingSchedulerProxyUserInterface.class, new Object[] {});


                cachedState.init(PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url),
                        PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_login),
                        PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_password));
            }
            } catch (Throwable e) {
                logger.warn("unable to log in using the 'caching' account with username '" +
                        PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_login)+
                        ", cache is disabled");
            }
        }
         */

        SchedulerProxyUserInterface scheduler = PAActiveObject.newActive(
                SchedulerProxyUserInterface.class, new Object[] {});

        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url);

        scheduler.init(url, username, password);

        String sessionId = "" + SchedulerSessionMapper.getInstance().add(scheduler);
        logger.info("binding user " + username + " to session " + sessionId);
        return sessionId;

    }

    /**
     * login to the scheduler using a multipart form
     *  can be used either by submitting 
     *   - 2 fields username & password
     *   - a credential file with field name 'credential'
     * @param multipart
     * @return the session id associated to this new connection
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws KeyException
     * @throws LoginException
     * @throws SchedulerException
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    public String loginWithCredential(@MultipartForm
    LoginForm multipart) throws ActiveObjectCreationException, NodeException, KeyException, LoginException,
            SchedulerException, IOException {

        SchedulerProxyUserInterface scheduler = PAActiveObject.newActive(
                SchedulerProxyUserInterface.class, new Object[] {});

        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url);

        if (multipart.getCredential() != null) {
            Credentials cred = Credentials.getCredentials(multipart.getCredential());
            scheduler.init(url, cred);
        } else {
            scheduler.init(url, multipart.getUsername(), multipart.getPassword());
        }

        String sessionId = "" + SchedulerSessionMapper.getInstance().add(scheduler);
        //      logger.info("binding user "+  " to session " + sessionId );
        return sessionId;

    }
}
