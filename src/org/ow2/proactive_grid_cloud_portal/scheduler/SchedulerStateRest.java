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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentMap;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
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
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive_grid_cloud_portal.common.LoginForm;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PersistentMapConverter;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

/**
 * this class exposes the Scheduler as a RESTful service.
 */
@XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
@Path("/scheduler/")
public class SchedulerStateRest implements SchedulerRestInterface {
    /**
     * If the rest api was unable to instantiate the value from byte array
     * representation
     */

    public static final String UNKNOWN_VALUE_TYPE = "Unknown value type";

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX
	    + ".rest");

    /**
     * Returns the ids of the current jobs under a list of string.
     * 
     * @param sessionId
     *            a valid session id
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param range
     *            optional, if a sublist has to be returned, the range of the
     *            sublist
     * @return a list of jobs' ids under the form of a list of string
     */
    @GET
    @Path("jobs")
    @Produces("application/json")
    public List<String> jobs(@HeaderParam("sessionid") String sessionId,
	    @QueryParam("index") @DefaultValue("-1") int index,
	    @QueryParam("range") @DefaultValue("-1") int range)
	    throws NotConnectedException, PermissionException {
	Scheduler s = null;

	s = checkAccess(sessionId, "/scheduler/jobs");
	renewLeaseForClient(s);
	List<JobState> jobs = new ArrayList<JobState>();
	SchedulerState state = SchedulerStateCaching.getLocalState();

	jobs.addAll(state.getPendingJobs());
	jobs.addAll(state.getRunningJobs());
	jobs.addAll(state.getFinishedJobs());

	// filter the result if needed
	jobs = subList(jobs, index, range);

	List<String> names = new ArrayList<String>();
	for (JobState j : jobs) {
	    names.add(j.getId().toString());
	}

	return names;

    }

    /**
     * call a method on the scheduler's frontend in order to renew the lease the
     * user has on this frontend. see PORTAL-70
     * 
     * @param sessionId
     * @throws PermissionException
     * @throws NotConnectedException
     */
    private void renewLeaseForClient(Scheduler scheduler)
	    throws NotConnectedException, PermissionException {
    	scheduler.renewSession();
    }

    /**
     * Returns a subset of the scheduler state, including pending, running,
     * finished jobs (in this particular order). each jobs is described using -
     * its id - its owner - the JobInfo class
     * 
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param range
     *            optional, if a sublist has to be returned, the range of the
     *            sublist
     * @param sessionId
     *            a valid session id
     * @return a list of UserJobInfo
     */
    @GET
    @Path("jobsinfo")
    @Produces({ "application/json", "application/xml" })
    public List<UserJobInfo> jobsinfo(
	    @HeaderParam("sessionid") String sessionId,
	    @QueryParam("index") @DefaultValue("-1") int index,
	    @QueryParam("range") @DefaultValue("-1") int range)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "/scheduler/jobsinfo");
	renewLeaseForClient(s);
	List<JobState> jobs = new ArrayList<JobState>();

	SchedulerState state = SchedulerStateCaching.getLocalState();

	jobs.addAll(state.getPendingJobs());
	jobs.addAll(state.getRunningJobs());
	jobs.addAll(state.getFinishedJobs());

	// filter the result if needed
	jobs = subList(jobs, index, range);

	List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
	for (JobState j : jobs) {
	    jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j
		    .getJobInfo()));
	}

	return jobInfoList;

    }

    /**
     * Returns a map containing one entry with the revision id as key and the
     * list of UserJobInfo as value. each jobs is described using - its id - its
     * owner - the JobInfo class
     * 
     * @param sessionId
     *            a valid session id
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param range
     *            optional, if a sublist has to be returned, the range of the
     *            sublist
     * @param myJobs
     *            fetch only the jobs for the user making the request
     * @param pending
     *            fetch pending jobs
     * @param running
     *            fetch running jobs
     * @param finished
     *            fetch finished jobs
     * @return a map containing one entry with the revision id as key and the
     *         list of UserJobInfo as value.
     */
    @GET
    @GZIP
    @Path("revisionjobsinfo")
    @Produces({ "application/json", "application/xml" })
    public Map<AtomicLong, List<UserJobInfo>> revisionAndjobsinfo(
	    @HeaderParam("sessionid") String sessionId,
	    @QueryParam("index") @DefaultValue("-1") int index,
	    @QueryParam("range") @DefaultValue("-1") int range,
	    @QueryParam("myjobs") @DefaultValue("false") boolean myJobs,
	    @QueryParam("pending") @DefaultValue("true") boolean pending,
	    @QueryParam("running") @DefaultValue("true") boolean running,
	    @QueryParam("finished") @DefaultValue("true") boolean finished)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "revisionjobsinfo?index=" + index
		+ "&range=" + range);
	List<UserJobInfo> jobs = new ArrayList<UserJobInfo>();
	renewLeaseForClient(s);

	Map<AtomicLong, LightSchedulerState> stateAndLightRevision = SchedulerStateCaching
		.getRevisionAndLightSchedulerState();

	Entry<AtomicLong, LightSchedulerState> entry = stateAndLightRevision
		.entrySet().iterator().next();

	LightSchedulerState lightState = entry.getValue();

	String user = SchedulerSessionMapper.getInstance()
		.getSchedulerSession(sessionId).getUserName();

	boolean onlyUserJobs = (myJobs && user != null && user.trim().length() > 0);

	for (UserJobInfo j : lightState.getJobs()) {

	    if (onlyUserJobs && !j.getJobOwner().equals(user)) {
		continue;
	    }

	    JobStatus jobStatus = j.getJobinfo().getStatus();

	    switch (jobStatus) {
	    case PENDING:
		if (pending) {
		    jobs.add(j);
		}
		break;
	    case RUNNING:
	    case STALLED:
	    case PAUSED:
		if (running) {
		    jobs.add(j);
		}
		break;
	    case FINISHED:
	    case CANCELED:
	    case FAILED:
	    case KILLED:
		if (finished) {
		    jobs.add(j);
		}
		break;
	    default:
		// unknown status so far
		// but we display it anyway
		jobs.add(j);
		break;
	    }
	}

	if (range != -1) {
	    Collections.sort(jobs, new Comparator<UserJobInfo>() {
			public int compare(UserJobInfo o1, UserJobInfo o2) {
				// create 3 sub groups : pending, running, finished, in
				// order
				// each subgroup is sorted by id
				int o1i = -1;
				switch (o1.getJobinfo().getStatus()) {
				case PENDING:
					o1i = 0;
					break;
				case RUNNING:
				case STALLED:
				case PAUSED:
					o1i = 1;
					break;
				default:
					o1i = 2;
					break;
				}

				int o2i = -1;
				switch (o2.getJobinfo().getStatus()) {
				case PENDING:
					o2i = 0;
					break;
				case RUNNING:
				case STALLED:
				case PAUSED:
					o2i = 1;
					break;
				default:
					o2i = 2;
					break;
				}

				if (o1i < o2i)
					return -1;
				else if (o1i > o2i)
					return 1;

				Integer id1 = Integer.valueOf(o1.getJobid());
				Integer id2 = Integer.valueOf(o2.getJobid());
				return id2.compareTo(id1);
			}
		});
	}

	// filter the result if needed
	jobs = subList(jobs, index, range);

	HashMap<AtomicLong, List<UserJobInfo>> map = new HashMap<AtomicLong, List<UserJobInfo>>();
	map.put(entry.getKey(), jobs);
	return map;

    }

    /**
     * return a sublist of the list <code>list</code> while handling all the
     * possible out of range issue. Note that an index value of -1 and a
     * positive range value return the end of the list from with an index value
     * of the size of the list - range - 1 to the end of the list.
     * 
     * @param list
     *            the list to sublist
     * @param index
     *            the start point of the sublist
     * @param range
     *            the length of the sublist
     * @return return a sublist of the list <code>list</code>
     */
    private <V> List<V> subList(List<V> list, int index, int range) {
	if ((index < 0) && (range < 0)) {
	    // index and range are not set
	    // return the full list
	    return list;
	} else {
	    int listSize = list.size();
	    if (index >= listSize) {
		index = listSize - 1;
	    }

	    if (range < 0) {
		range = listSize - index;
	    }

	    if (range > listSize) {
		range = listSize;
	    }

	    if ((index == -1) && (range >= 0)) {
		index = listSize - range;
	    }

	    if (index + range > listSize) {
		range = listSize - index;
	    }

	    return list.subList(index, index + range);
	}
    }

    /**
     * Returns the state of the scheduler
     * 
     * @param sessionId
     *            a valid session id.
     * @return the scheduler state
     */
    @GET
    @Path("state")
    @Produces({ "application/json", "application/xml" })
    public SchedulerState schedulerState(
	    @HeaderParam("sessionid") String sessionId)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "/scheduler/state");
	renewLeaseForClient(s);
	return SchedulerStateCaching.getLocalState();
    }

    /**
     * Returns the revision number of the scheduler state
     * 
     * @param sessionId
     *            a valid session id.
     * @return the revision of the scheduler state
     */
    @GET
    @Path("state/revision")
    @Produces({ "application/json", "application/xml" })
    public long schedulerStateRevision(
	    @HeaderParam("sessionid") String sessionId)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "/scheduler/revision");
	renewLeaseForClient(s);
	return SchedulerStateCaching.getSchedulerRevision();
    }

    /**
     * Returns a map with only one entry containing as key the revision and as
     * content the scheduler state
     * 
     * @param sessionId
     *            a valid session id.
     * @return a map of one entry containing the revision and the corresponding
     *         scheduler state
     */
    @GET
    @Path("revisionandstate")
    @Produces({ "application/json", "application/xml" })
    public Map<AtomicLong, SchedulerState> getSchedulerStateAndRevision(
	    @HeaderParam("sessionid") String sessionId)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "/scheduler/staterevision");
	renewLeaseForClient(s);
	return SchedulerStateCaching.getRevisionAndSchedulerState();
    }

    /**
     * returns only the jobs of the current user
     * 
     * @param sessionId
     *            a valid session id
     * @return a scheduler state that contains only the jobs of the user that
     *         owns the session <code>sessionid</code>
     */
    @GET
    @Path("state/myjobsonly")
    @Produces({ "application/json", "application/xml" })
    public SchedulerState getSchedulerStateMyJobsOnly(
	    @HeaderParam("sessionid") String sessionId)
	    throws PermissionException, NotConnectedException {
	Scheduler s = checkAccess(sessionId, "/scheduler/myjobsonly");
	return PAFuture.getFutureValue(s.getState(true));
    }

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     * 
     * @param sessionid
     *            a valid session id
     * @param jobid
     *            the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    @XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
    public JobState listJobs(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException {
	Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId);

	JobState js;
	js = s.getJobState(jobId);
	js = PAFuture.getFutureValue(js);

	return js;
    }

    /**
     * Stream the output of job identified by the id <code>jobid</code> only
     * stream currently available logs, call this method several times to get
     * the complete output.
     * 
     * @param sessionid
     *            a valid session id
     * @param jobid
     *            the id of the job to retrieve
     * @throws IOException
     * @throws LogForwardingException
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    public String getLiveLogJob(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException, LogForwardingException,
	    IOException {
	Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId
		+ "/livelog");
	SchedulerSession ss = SchedulerSessionMapper.getInstance()
		.getSchedulerSession(sessionId);

	JobOutput jo = null;
	;

	if (ss.getJobOutputAppender() == null) {
	    jo = JobsOutputController.getInstance().createJobOutput(ss, jobId)
		    .getJobOutput();
	} else {
	    if (!jobId.equalsIgnoreCase(ss.getJobOutputAppender().getJobId())) {
		ss.getJobOutputAppender().terminate();
		ss.setJobOutputAppender(null);
		JobsOutputController.getInstance().createJobOutput(ss, jobId);
	    }
	    jo = ss.getJobOutputAppender().getJobOutput();
	}

	if (jo != null) {
	    CircularArrayList<String> cl = jo.getCl();
	    int size = cl.size();
	    StringBuffer mes = new StringBuffer();
	    for (int i = 0; i < size; i++) {
		mes.append(cl.remove(0));
	    }
	    return mes.toString();

	}

	return "";
    }

    /**
     * number of available bytes in the stream or -1 if the stream does not
     * exist.
     * 
     * @param sessionid
     *            a valid session id
     * @param jobid
     *            the id of the job to retrieve
     * @throws IOException
     * @throws LogForwardingException
     */
    @GET
    @Path("jobs/{jobid}/livelog/available")
    @Produces("application/json")
    public int getLiveLogJobAvailable(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException, LogForwardingException,
	    IOException {
	Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId
		+ "/livelog/available");
	SchedulerSession ss = SchedulerSessionMapper.getInstance()
		.getSchedulerSession(sessionId);

	JobOutputAppender joa = ss.getJobOutputAppender();

	if (joa != null) {
	    return joa.getJobOutput().getCl().size();
	}

	return -1;

    }

    /**
     * remove the live log object.
     * 
     * @param sessionid
     *            a valid session id
     * @param jobid
     *            the id of the job to retrieve
     * @throws IOException
     * @throws LogForwardingException
     */
    @DELETE
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    public boolean deleteLiveLogJob(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException, LogForwardingException,
	    IOException {
	Scheduler s = checkAccess(sessionId, "delete /scheduler/jobs/livelog"
		+ jobId);
	SchedulerSession ss = SchedulerSessionMapper.getInstance()
		.getSchedulerSession(sessionId);

	if (ss.getJobOutputAppender() != null) {
	    ss.getJobOutputAppender().terminate();
	    ss.setJobOutputAppender(null);
	}
	return true;

    }

    /**
     * Returns the job result associated to the job referenced by the id
     * <code>jobid</code>
     * 
     * @param sessionid
     *            a valid session id
     * @result the job result of the corresponding job
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    public JobResult jobResult(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    PermissionException, UnknownJobException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/result");

	return PAFuture.getFutureValue(s.getJobResult(jobId));
    }

    /**
     * Returns all the task results of this job as a map whose the key is the
     * name of the task and its task result.<br>
     * If the result cannot be instantiated, the content is replaced by the
     * string 'Unknown value type'. To get the serialized form of a given
     * result, one has to call the following restful service
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * 
     * @param sessionid
     *            a valid session id
     * @param jobid
     *            a job id
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    public Map<String, Serializable> jobResultValue(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    PermissionException, UnknownJobException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/result/value");
	JobResult jobResult = PAFuture.getFutureValue(s.getJobResult(jobId));
	if (jobResult == null) {
	    return null;
	}
	Map<String, TaskResult> allResults = jobResult.getAllResults();
	Map<String, Serializable> res = new HashMap<String, Serializable>(
		allResults.size());
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
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to delete
     * @return true if success, false if the job not yet finished (not removed,
     *         kill the job then remove it)
     * 
     */
    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public boolean removeJob(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException {
	Scheduler s = checkAccess(sessionId, "DELETE jobs/" + jobId);
	return s.removeJob(jobId);
    }

    /**
     * Kill the job represented by jobId.<br>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job to kill.
     * @return true if success, false if not.
     */
    @PUT
    @Path("jobs/{jobid}/kill")
    @Produces("application/json")
    public boolean killJob(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException {
	Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobId + "/kill");
	return s.killJob(jobId);

    }

    /**
     * Returns a list of the name of the tasks belonging to job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param the
     *            jobid one wants to list the tasks' name
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    public List<String> getJobTasksIds(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks");
	JobState jobState;

	jobState = s.getJobState(jobId);
	List<String> tasksName = new ArrayList<String>();
	for (TaskState ts : jobState.getTasks()) {
	    tasksName.add(ts.getId().getReadableName());
	}

	return tasksName;

    }

    /**
     * Returns a base64 encoded png image corresponding to the jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return Returns a base64 encoded png image corresponding to the jobid
     * @throws IOException
     *             when it is not possible to access to the archive
     */
    @GET
    @Path("jobs/{jobid}/image")
    @Produces("application/json")
    public String getJobImage(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws IOException {

	String image = "";
	InputStream ips = null;
	String enc = null;
	ZipFile jobArchive = new ZipFile(PortalConfiguration.jobIdToPath(jobId));
	ZipEntry imageEntry = jobArchive.getEntry("JOB-INF/image.png");
	if (imageEntry == null) {
	    throw new IOException(
		    "the file JOB-INF/image.png was not found in the job archive");
	}
	ips = new BufferedInputStream(jobArchive.getInputStream(imageEntry));
	int octet;
	while ((octet = ips.read()) != -1) {
	    image += (char) octet;
	}
	ips.close();

	// Encode in Base64
	enc = new String(
		org.apache.commons.codec.binary.Base64.encodeBase64(image
			.getBytes()));

	return enc;
    }

    /**
     * Returns the map of the corresponding job id.
     * 
     * @throws IOException
     *             when the job archive is not found
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return the map corresponding to the <code>jobId</code>
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/map")
    @Produces({ "application/json", "application/xml" })
    public String getJobMap(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws IOException {
	String map = "";
	InputStream ips;

	ZipFile jobArchive = new ZipFile(PortalConfiguration.jobIdToPath(jobId));
	ZipEntry imageEntry = jobArchive.getEntry("JOB-INF/map.xml");
	if (imageEntry == null) {
	    throw new IOException(
		    "the file JOB-INF/map.xml was not found in the job archive");
	}
	ips = new BufferedInputStream(jobArchive.getInputStream(imageEntry));
	InputStreamReader ipsr = new InputStreamReader(ips);
	BufferedReader br = new BufferedReader(ipsr);
	String ligne;
	while ((ligne = br.readLine()) != null) {
	    map += ligne + "\n";
	}
	br.close();

	return map;
    }

    /**
     * Returns a list of taskState
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return a list of task' states of the job <code>jobId</code>
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates")
    @Produces("application/json")
    public List<TaskState> getJobTaskStates(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId) throws NotConnectedException,
	    UnknownJobException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/taskstates");
	JobState jobState;

	jobState = s.getJobState(jobId);

	List<TaskState> tasks = new ArrayList<TaskState>();
	for (TaskState ts : jobState.getTasks()) {
	    tasks.add(ts);
	}

	return tasks;

    }

    /**
     * Return the task state of the task <code>taskname</code> of the job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the task state of the task <code>taskname</code> of the job
     *         <code>jobId</code>
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")
    @Produces("application/json")
    public TaskState jobtasks(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname)
	    throws NotConnectedException, UnknownJobException,
	    PermissionException, UnknownTaskException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname);
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
     * Returns the value of the task result of task <code>taskName</code> of the
     * job <code>jobId</code> <strong>the result is deserialized before sending
     * to the client, if the class is not found the content is replaced by the
     * string 'Unknown value type' </strong>. To get the serialized form of a
     * given result, one has to call the following restful service
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the value of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    public Serializable valueOftaskresult(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname) throws Throwable {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result/value");
	// TaskResult taskResult = s.getTaskResult(jobId, taskname);
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
     * Returns the value of the task result of the task <code>taskName</code> of
     * the job <code>jobId</code> This method returns the result as a byte array
     * whatever the result is.
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the value of the task result as a byte array.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    public byte[] serializedValueOftaskresult(
	    @HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname) throws Throwable {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result/serializedvalue");
	TaskResult tr = s.getTaskResult(jobId, taskname);
	tr = PAFuture.getFutureValue(tr);
	return ((TaskResultImpl) tr).getSerializedValue();
    }

    /**
     * Returns the task result of the task <code>taskName</code> of the job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the task result of the task <code>taskName</code>
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    public TaskResult taskresult(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname)
	    throws NotConnectedException, UnknownJobException,
	    UnknownTaskException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result");
	TaskResult tr = s.getTaskResult(jobId, taskname);
	return PAFuture.getFutureValue(tr);
    }

    /**
     * Returns all the logs generated by the task (either stdout and stderr)
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return all the logs generated by the task (either stdout and stderr) or
     *         an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("application/json")
    public String tasklog(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname)
	    throws NotConnectedException, UnknownJobException,
	    UnknownTaskException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result/log/all");
	TaskResult tr = s.getTaskResult(jobId, taskname);
	if ((tr != null) && (tr.getOutput() != null)) {
	    return tr.getOutput().getAllLogs(true);
	} else {
	    return "";
	}

    }

    /**
     * Returns the standard error output (stderr) generated by the task
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the stderr generated by the task or an empty string if the result
     *         is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("application/json")
    public String tasklogErr(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname)
	    throws NotConnectedException, UnknownJobException,
	    UnknownTaskException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result/log/err");
	TaskResult tr = s.getTaskResult(jobId, taskname);
	if ((tr != null) && (tr.getOutput() != null)) {
	    return tr.getOutput().getStderrLogs(true);
	} else {
	    return "";
	}

    }

    /**
     * Returns the standard output (stderr) generated by the task
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the stdout generated by the task or an empty string if the result
     *         is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("application/json")
    public String tasklogout(@HeaderParam("sessionid") String sessionId,
	    @PathParam("jobid") String jobId,
	    @PathParam("taskname") String taskname)
	    throws NotConnectedException, UnknownJobException,
	    UnknownTaskException, PermissionException {
	Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/"
		+ taskname + "/result/log/out");
	TaskResult tr = s.getTaskResult(jobId, taskname);
	if ((tr != null) && (tr.getOutput() != null)) {
	    return tr.getOutput().getStdoutLogs(true);
	} else {
	    return "";
	}

    }

    /**
     * the method check is the session id is valid i.e. a scheduler client is
     * associated to the session id in the session map. If not, a
     * NotConnectedException is thrown specifying the invalid access *
     * 
     * @param sessionId
     * @return the scheduler linked to the session id, an NotConnectedException,
     *         if no such mapping exists.
     * @throws NotConnectedException
     */
    public SchedulerProxyUserInterface checkAccess(String sessionId, String path)
	    throws NotConnectedException {

	SchedulerSession ss = SchedulerSessionMapper.getInstance()
		.getSchedulerSession(sessionId);
	if (ss == null) {
	    // logger.trace("not found a scheduler frontend for sessionId " +
	    // sessionId);
	    throw new NotConnectedException(
		    "you are not connected to the scheduler, you should log on first");
	}

	SchedulerProxyUserInterface s = ss.getScheduler();

	if (s == null) {
	    // logger.trace("not found a scheduler frontend for sessionId " +
	    // sessionId);
	    throw new NotConnectedException(
		    "you are not connected to the scheduler, you should log on first");
	}
	// logger.trace("found a scheduler frontend for sessionId " + sessionId
	// + ", path =" + path);
	return s;
    }

    private SchedulerProxyUserInterface checkAccess(String sessionId)
	    throws NotConnectedException {
	return checkAccess(sessionId, "");
    }

    // /**
    // * @param sessionId
    // * @param message
    // * @throws WebApplicationException http status code 403 Forbidden
    // */
    // public void handlePermissionException(String sessionId, String message)
    // throws WebApplicationException {
    // throw new
    // WebApplicationException(Response.status(HttpURLConnection.HTTP_FORBIDDEN)
    // .entity("you are not authorized to perform the action:" +
    // message).build());
    // }

    // /**
    // * @param sessionId
    // * @param jobId
    // * @throws WebApplicationException http status code 404 Not Found
    // */
    // public void handleUnknowJobException(String sessionId, String jobId)
    // throws WebApplicationException {
    // throw new
    // WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    // .entity("job " + jobId + "not found").build());
    // }

    // /**
    // * @param sessionId
    // * @param jobId
    // * @throws WebApplicationException http status code 404 Not Found
    // */
    // public void handleSubmissionClosedJobException(String sessionId) throws
    // WebApplicationException {
    // throw new
    // WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    // .entity("the scheduler is stopped, you cannot submit a job").build());
    // }
    // /**
    // * @param sessionId
    // * @param taskname
    // * @throws WebApplicationException http status code 404 Not Found
    // */
    // public void handleUnknowTaskException(String sessionId, String taskname)
    // throws WebApplicationException {
    // throw new
    // WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    // .entity("task " + taskname + "not found").build());
    // }

    // /**
    // * @param sessionId
    // * @param taskname
    // * @throws WebApplicationException http status code 404 Not Found
    // */
    // public void handleJobAlreadyFinishedException(String sessionId, String
    // msg) throws WebApplicationException {
    // throw new
    // WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND)
    // .entity(msg).build());
    // }

    /**
     * Pauses the job represented by jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
	    @PathParam("jobid") final String jobId)
	    throws NotConnectedException, UnknownJobException,
	    PermissionException {
	final Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId
		+ "/pause");

	return s.pauseJob(jobId);

    }

    /**
     * Resumes the job represented by jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
	    @PathParam("jobid") final String jobId)
	    throws NotConnectedException, UnknownJobException,
	    PermissionException {
	final Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId
		+ "/resume");
	return s.resumeJob(jobId);

    }

	/**
	 * Submit job using flat command file
	 * @param sessionId valid session id
	 * @param commandFileContent content of a command file: endline separated native commands
	 * @param jobName name of the job to create
	 * @param selectionScriptContent content of a selection script, or null
	 * @param selectionScriptExtension extension of the selectionscript to determine script engine ("js", "py", "rb")
	 * @return Id of the submitted job
	 * @throws NotConnectedException
	 * @throws IOException
	 * @throws JobCreationException
	 * @throws PermissionException
	 * @throws SubmissionClosedException
	 */
	@POST
	@Path("submitflat")
	@Produces("application/json")
	public JobId submitFlat(@HeaderParam("sessionid") String sessionId,
			@FormParam("commandFileContent") String commandFileContent,
			@FormParam("jobName") String jobName,
			@FormParam("selectionScriptContent") String selectionScriptContent,
			@FormParam("selectionScriptExtension") String selectionScriptExtension)
			throws NotConnectedException, IOException, JobCreationException,
			PermissionException, SubmissionClosedException {
		Scheduler s = checkAccess(sessionId, "submitflat");
		
		try {
			File command = File.createTempFile("flatsubmit_commands_", ".txt");
			command.deleteOnExit();
			
			String selectionPath = null;
			File selection = null;

			if (selectionScriptContent != null
					&& selectionScriptContent.trim().length() > 0) {
				selection = File.createTempFile("flatsubmit_selection_", "."
						+ selectionScriptExtension);
				selection.deleteOnExit();
				PrintWriter pw = new PrintWriter(
						new FileOutputStream(selection));
				pw.print(selectionScriptContent);
				pw.close();
				selectionPath = selection.getAbsolutePath();
			}

			PrintWriter pw = new PrintWriter(new FileOutputStream(command));
			pw.print(commandFileContent);
			pw.close();

			Job j = FlatJobFactory.getFactory()
					.createNativeJobFromCommandsFile(command.getAbsolutePath(),
							jobName, selectionPath, null);
			JobId id = s.submit(j);
			
			command.delete();
			if (selection != null)
				selection.delete();
			return id;
		} catch (IOException e) {
			throw new IOException("I/O Error: " + e.getMessage(), e);
		}
	}
	
    /**
     * Submits a job to the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return the <code>jobid</code> of the newly created job
     * @throws IOException
     *             if the job was not correctly uploaded/stored
     */
    @POST
    @Path("submit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public JobId submit(@HeaderParam("sessionid") String sessionId,
	    MultipartFormDataInput multipart) throws JobCreationException,
	    NotConnectedException, PermissionException,
	    SubmissionClosedException, IOException {
	Scheduler s = checkAccess(sessionId, "submit");

	Map<String, List<InputPart>> formDataMap = multipart.getFormDataMap();

	String name = formDataMap.keySet().iterator().next();
	File tmp = null;
	try {

	    InputPart part1 = multipart.getFormDataMap().get(name).get(0); // "file"
									   // is
									   // the
									   // name
									   // of
									   // the
									   // browser's
									   // input
									   // field
	    InputStream is = part1.getBody(new GenericType<InputStream>() {
	    });
	    tmp = File.createTempFile("job", "d");

	    try {
		OutputStream os = new FileOutputStream(tmp);
		try {
		    byte[] buffer = new byte[4096];
		    for (int n; (n = is.read(buffer)) != -1;)
			os.write(buffer, 0, n);
		} finally {
		    os.close();
		}
	    } finally {
		is.close();
	    }

	    Job j = null;
	    boolean isAnArchive = false;
	    if (part1.getMediaType().toString().toLowerCase()
		    .indexOf(MediaType.APPLICATION_XML.toLowerCase()) > -1) {
		// the job sent is the xml file
		j = JobFactory.getFactory().createJob(tmp.getAbsolutePath());
	    } else {
		// it is a job archive
		j = JobFactory.getFactory().createJobFromArchive(
			tmp.getAbsolutePath());
		isAnArchive = true;
	    }
	    JobId jobid = s.submit(j);

	    File archiveToStore = new File(
		    PortalConfiguration.jobIdToPath(jobid.value()));
	    if (isAnArchive) {
		logger.debug("saving archive to "
			+ archiveToStore.getAbsolutePath());
		tmp.renameTo(archiveToStore);
	    } else {
		// the job is not an archive, however an archive file can
		// exist for this new job (due to an old submission)
		// In that case, we remove the existing file preventing erronous
		// access
		// to this previously submitted archive.

		if (archiveToStore.exists()) {
		    archiveToStore.delete();
		}
	    }

	    return jobid;
	} finally {
	    if (tmp != null) {
		// clean the temporary file
		tmp.delete();
	    }
	}

    }

    /*
     * // Kept for later use if we discover that using MultipartInput does not
     * // longer match our needs. Uploads of byte-encoded job descriptors for
     * instance.
     * 
     * @POST
     * 
     * @Path("submit")
     * 
     * public String submit(@Context HttpServletRequest req) throws Exception {
     * 
     * FileItemFactory factory = new DiskFileItemFactory(); ServletFileUpload
     * upload = new ServletFileUpload(factory);
     * 
     * List items = upload.parseRequest(req); Iterator iter = items.iterator();
     * 
     * while (iter.hasNext()) { FileItem item = (FileItem) iter.next();
     * 
     * if (item.isFormField()) { System.out.println("FORM FIELD");
     * 
     * } else { if (!item.isFormField()) {
     * 
     * String fileName = item.getName(); System.out.println("File Name:" +
     * fileName);
     * 
     * File fullFile = new File(item.getName()); File savedFile = new
     * File("/test", fullFile.getName());
     * 
     * item.write(savedFile); } } }
     * 
     * return "OK"; }
     */

    /**
     * terminates the session id <code>sessionId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @throws NotConnectedException
     *             if the scheduler cannot be contacted
     * @throws PermissionException
     *             if you are not authorized to perform the action
     */
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    public void disconnect(@HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "disconnect");

	try {
	    s.disconnect();
	} finally {
	    SchedulerSessionMapper.getInstance().remove(sessionId);
	    PAActiveObject.terminateActiveObject(s, true);
	    logger.debug("sessionid " + sessionId + " terminated");
	}

    }

    /**
     * pauses the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    public boolean pauseScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "pause");
	return s.pause();

    }

    /**
     * stops the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    public boolean stopScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "stop");
	return s.stop();
    }

    /**
     * resumes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    public boolean resumeScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "resume");
	return s.resume();

    }

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityName
     *            a string representing the name of the priority
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public void schedulerChangeJobPriorityByName(
	    @HeaderParam("sessionid") final String sessionId,
	    @PathParam("jobid") final String jobId,
	    @PathParam("name") String priorityName)
	    throws NotConnectedException, UnknownJobException,
	    PermissionException, JobAlreadyFinishedException {
	final Scheduler s = checkAccess(sessionId, "jobs/" + jobId
		+ "/priority/byname/" + priorityName);
	s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));

    }

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityValue
     *            a string representing the value of the priority
     * @throws NumberFormatException
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public void schedulerChangeJobPriorityByValue(
	    @HeaderParam("sessionid") final String sessionId,
	    @PathParam("jobid") final String jobId,
	    @PathParam("value") String priorityValue)
	    throws NumberFormatException, NotConnectedException,
	    UnknownJobException, PermissionException,
	    JobAlreadyFinishedException {
	final Scheduler s = checkAccess(sessionId, "jobs/" + jobId
		+ "/priority/byvalue" + priorityValue);
	s.changeJobPriority(jobId,
		JobPriority.findPriority(Integer.parseInt(priorityValue)));

    }

    /**
     * freezes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    public boolean freezeScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "freeze");
	return s.freeze();

    }

    /**
     * returns the status of the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return the scheduler status
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("status")
    @Produces("application/json")
    public SchedulerStatus getSchedulerStatus(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "status");
	renewLeaseForClient(s);
	Map<AtomicLong, LightSchedulerState> stateAndLightRevision = SchedulerStateCaching
			.getRevisionAndLightSchedulerState();

	Entry<AtomicLong, LightSchedulerState> entry = stateAndLightRevision
			.entrySet().iterator().next();

	LightSchedulerState lightState = entry.getValue();
	return lightState.getStatus();
    }

    /**
     * starts the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    public boolean startScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "start");
	return s.start();

    }

    /**
     * kills and shutdowns the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false if not
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    public boolean killScheduler(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "kill");
	return s.kill();

    }

    /**
     * Reconnect a new Resource Manager to the scheduler. Can be used if the
     * resource manager has crashed.
     * 
     * @param sessionId
     *            a valid session id
     * @param rmURL
     *            the url of the resource manager
     * @return true if success, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    public boolean killScheduler(
	    @HeaderParam("sessionid") final String sessionId,
	    @FormParam("rmurl") String rmURL) throws NotConnectedException,
	    PermissionException {
	final Scheduler s = checkAccess(sessionId, "linkrm");
	return s.linkResourceManager(rmURL);

    }

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     * 
     * @param sessionId
     *            the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("isconnected")
    @Produces("application/json")
    public boolean isConnected(@HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final Scheduler s = checkAccess(sessionId, "isconnected");
	return s.isConnected();
    }

    /**
     * login to the scheduler using an form containing 2 fields (username &
     * password)
     * 
     * @param username
     *            username
     * @param password
     *            password
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
    @Produces("application/json")
    public String login(@FormParam("username") String username,
	    @FormParam("password") String password) throws LoginException,
	    SchedulerException, KeyException, ActiveObjectCreationException,
	    NodeException {

	// activate the cache mechanism at first login
	/*
	 * synchronized(this) { try { if ((isCacheEnabled) && (cachedState ==
	 * null)){ cachedState = PAActiveObject.newActive(
	 * CachingSchedulerProxyUserInterface.class, new Object[] {});
	 * 
	 * 
	 * cachedState.init(PortalConfiguration.getProperties().getProperty(
	 * PortalConfiguration. scheduler_url),
	 * PortalConfiguration.getProperties
	 * ().getProperty(PortalConfiguration.scheduler_cache_login ),
	 * PortalConfiguration
	 * .getProperties().getProperty(PortalConfiguration.scheduler_cache_password
	 * )); } } catch (Throwable e) { logger.warn(
	 * "unable to log in using the 'caching' account with username '" +
	 * PortalConfiguration
	 * .getProperties().getProperty(PortalConfiguration.scheduler_cache_login
	 * )+ ", cache is disabled"); } }
	 */

	MySchedulerProxyUserInterface scheduler;
	scheduler = PAActiveObject.newActive(
		MySchedulerProxyUserInterface.class, new Object[] {});

	String url = PortalConfiguration.getProperties().getProperty(
		PortalConfiguration.scheduler_url);

	if ((username == null) || (password == null)) {
	    throw new LoginException("empty login/password");
	}

	scheduler.init(url, username, password);

	String sessionId = ""
		+ SchedulerSessionMapper.getInstance().add(scheduler, username);
	logger.info("binding user " + username + " to session " + sessionId);
	return sessionId;
    }

    /**
     * login to the scheduler using a multipart form can be used either by
     * submitting - 2 fields username & password - a credential file with field
     * name 'credential'
     * 
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
    @Produces("application/json")
    public String loginWithCredential(@MultipartForm LoginForm multipart)
	    throws ActiveObjectCreationException, NodeException, KeyException,
	    LoginException, SchedulerException {

	MySchedulerProxyUserInterface scheduler = PAActiveObject.newActive(
		MySchedulerProxyUserInterface.class, new Object[] {});
	String username = null;
	String url = PortalConfiguration.getProperties().getProperty(
		PortalConfiguration.scheduler_url);

	if (multipart.getCredential() != null) {
	    Credentials credentials;
	    try {
		credentials = Credentials.getCredentials(multipart
			.getCredential());
		scheduler.init(url, credentials);
	    } catch (IOException e) {
		throw new LoginException(e.getMessage());
	    }
	} else {
	    if ((multipart.getUsername() == null)
		    || (multipart.getPassword() == null)) {
		throw new LoginException("empty login/password");
	    }

	    username = multipart.getUsername();
	    CredData credData = new CredData(CredData.parseLogin(multipart
		    .getUsername()), CredData.parseDomain(multipart
		    .getUsername()), multipart.getPassword(),
		    multipart.getSshKey());
	    scheduler.init(url, credData);
	}

	String sessionId = ""
		+ SchedulerSessionMapper.getInstance().add(scheduler, username);
	// logger.info("binding user "+ " to session " + sessionId );
	return sessionId;

    }

    /**
     * returns statistics about the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing the statistics
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("stats")
    @Produces("application/json")
    public Map<String, String> getStatistics(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final SchedulerProxyUserInterface s = checkAccess(sessionId, "stats");

	return s.getMappedInfo("ProActiveScheduler:name=RuntimeData");
    }

    /**
     * returns a string containing some data regarding the user's account
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    public Map<String, String> getStatisticsOnMyAccount(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {
	final SchedulerProxyUserInterface s = checkAccess(sessionId,
		"stats/myaccount");

	return s.getMappedInfo("ProActiveScheduler:name=MyAccount");
    }

    /**
     * Users currently connected to the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection\
     * @return list of users
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    public List<UserIdentification> getUsers(
	    @HeaderParam("sessionid") final String sessionId)
	    throws NotConnectedException, PermissionException {

	Scheduler s = checkAccess(sessionId, "users");
	renewLeaseForClient(s);
	Map<AtomicLong, LightSchedulerState> stateAndLightRevision = SchedulerStateCaching
			.getRevisionAndLightSchedulerState();

	Entry<AtomicLong, LightSchedulerState> entry = stateAndLightRevision
			.entrySet().iterator().next();

	LightSchedulerState lightState = entry.getValue();
	return lightState.getUsers();
    }

    /*
     * @GET
     * 
     * needs some tests
     * 
     * @Path("stats/allaccounts") public String
     * getStatisticsOnAllAccounts(@HeaderParam("sessionid") final String
     * sessionId) throws NotConnectedException, PermissionException { final
     * SchedulerProxyUserInterface s = checkAccess(sessionId);
     * 
     * return s.getInfo("ProActiveScheduler:name=AllAccounts"); }
     */

    /**
     * returns the version of the rest api
     * 
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    public String getVersion() {
	return "{ "
		+ "\"scheduler\" : \""
		+ SchedulerStateRest.class.getPackage()
			.getSpecificationVersion()
		+ "\", "
		+ "\"rest\" : \""
		+ SchedulerStateRest.class.getPackage()
			.getImplementationVersion() + "\"" + "}";
    }

    /**
     * generates a credential file from user provided credentials
     * 
     * @return the credential file generated by the scheduler
     * @throws ConnectionException
     * @throws LoginException
     * @throws InternalSchedulerException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("createcredential")
    @Produces("*/*")
    public byte[] getCreateCredential(@MultipartForm LoginForm multipart)
	    throws ConnectionException, LoginException,
	    InternalSchedulerException {

	String url = PortalConfiguration.getProperties().getProperty(
		PortalConfiguration.scheduler_url);

	SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
	PublicKey pubKey = auth.getPublicKey();

	try {
	    Credentials cred = Credentials.createCredentials(
		    new CredData(CredData.parseLogin(multipart.getUsername()),
			    CredData.parseDomain(multipart.getUsername()),
			    multipart.getPassword(), multipart.getSshKey()),
		    pubKey);

	    return cred.getBase64();
	} catch (KeyException e) {
	    throw new InternalSchedulerException(e);
	}
    }
}
