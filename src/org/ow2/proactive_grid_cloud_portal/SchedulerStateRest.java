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
	@Path("/jobs")
	@Produces("application/json")
	public List<UserJobInfo> jobs(
			@HeaderParam("sessionid") final String sessionId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			final List<JobState> pendingJobs = s.getState().getPendingJobs();
			final List<JobState> runningJobs = s.getState().getRunningJobs();
			final List<JobState> finishedJobs = s.getState().getFinishedJobs();

			final int totalSize = pendingJobs.size() + runningJobs.size()
					+ finishedJobs.size();

			final List<JobState> jobs = new ArrayList<JobState>(totalSize);
			jobs.addAll(pendingJobs);
			jobs.addAll(runningJobs);
			jobs.addAll(finishedJobs);

			final List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>(
					totalSize);
			for (JobState j : jobs) {
				jobInfoList.add(new UserJobInfo(j.getId().value(),
						j.getOwner(), j.getJobInfo()));
			}
			return jobInfoList;
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		return null;

	}

	@GET
	@Path("/jobs/{jobid}")
	public JobState job(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			final JobState js = s.getJobState(jobId);
			return PAFuture.getFutureValue(js);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("/jobs/{jobid}/result")
	public JobResult jobResult(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			return PAFuture.getFutureValue(s.getJobResult(jobId));
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("/jobs/{jobid}/pause")
	public boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
			@PathParam("jobid") final String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
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
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
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

	@POST
	@Path("/jobs/{jobid}/kill")
	public void killJob(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			s.killJob(jobId);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
	}

	@DELETE
	@Path("/jobs/{jobid}")
	public boolean removeJob(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			s.removeJob(jobId);
			return true;
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		return false;
	}

	@GET
	@Path("/jobs/{jobid}/tasks")
	public List<String> jobtasks(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			JobState jobState = s.getJobState(jobId);
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
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		// TODO: Handle null value for s ... invalid session id
		try {
			JobState jobState = s.getJobState(jobId);
			for (TaskState ts : jobState.getTasks()) {
				if (ts.getId().getReadableName().equals(taskid)) {
					System.out.println(ts);
					return ts;
				}
			}
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("/jobs/{jobid}/tasks/{taskid}/result")
	public Serializable taskresult(@HeaderParam("sessionid") String sessionId,
			@PathParam("jobid") String jobId, @PathParam("taskid") String taskId) {
		final Scheduler s = SchedulerSessionMapper.getInstance()
				.getSessionsMap().get(sessionId);
		try {
			TaskResult tr = s.getTaskResult(jobId, taskId);
			// TODO: this call throws a Throwable it should be handled with an
			// appropriate HTTP error
			return tr.value();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (UnknownTaskException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
}
