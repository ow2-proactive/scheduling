package org.ow2.proactive.scheduler.core.db.schedulerdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.ClasspathUtils;
import org.ow2.tests.ProActiveTest;

@Ignore
public class BaseSchedulerDBTest extends ProActiveTest {

	protected SchedulerDBManager dbManager;

	private static Credentials defaultCredentials;

	protected static final String DEFAULT_USER_NAME = "admin";

	protected boolean inMemory = false;

	public BaseSchedulerDBTest() {
		this(false);
	}

	public BaseSchedulerDBTest(boolean inMemory) {
		this.inMemory = inMemory;
	}

	public static class TestResult implements Serializable {

		private int a;

		private String b;

		TestResult(int a, String b) {
			this.a = a;
			this.b = b;
		}

		public int getA() {
			return a;
		}

		public String getB() {
			return b;
		}
	}

	public static class TestException extends Exception {

		private String data;

		public TestException(String message, String data) {
			super(message);
			this.data = data;
		}

		public String getData() {
			return data;
		}
	}

	static class StateMatcher extends TypeSafeMatcher<SchedulerState> {

		private final List<JobStateMatcher> pending = new ArrayList<>();
		private final List<JobStateMatcher> running = new ArrayList<>();
		private final List<JobStateMatcher> finished = new ArrayList<>();

		@Override
		public void describeTo(Description description) {
			description.appendText("State with jobs");
			for (JobStateMatcher job : pending) {
				description.appendDescriptionOf(job);
			}
			for (JobStateMatcher job : running) {
				description.appendDescriptionOf(job);
			}
			for (JobStateMatcher job : finished) {
				description.appendDescriptionOf(job);
			}
		}

		StateMatcher withPending(JobStateMatcher job) {
			pending.add(job);
			return this;
		}

		StateMatcher withRunning(JobStateMatcher job) {
			running.add(job);
			return this;
		}

		StateMatcher withFinished(JobStateMatcher job) {
			finished.add(job);
			return this;
		}

		@Override
		protected boolean matchesSafely(SchedulerState item) {
			assertThat(item.getPendingJobs(), containsInAnyOrder(pending.toArray(new JobStateMatcher[] {})));
			assertThat(item.getRunningJobs(), containsInAnyOrder(running.toArray(new JobStateMatcher[] {})));
			assertThat(item.getFinishedJobs(), containsInAnyOrder(finished.toArray(new JobStateMatcher[] {})));

			return true;
		}

	}

	public SchedulerDBManager getDbManager() {
		return dbManager;
	}

	public static Credentials getDefaultCredentials() throws Exception {
		if (defaultCredentials == null) {
			defaultCredentials = Credentials.createCredentials(DEFAULT_USER_NAME, "admin",
					new File(BaseSchedulerDBTest.class.getResource("/functionaltests/config/pub.key").toURI())
							.getAbsolutePath());
		}
		return defaultCredentials;
	}

	protected SchedulerStateRecoverHelper.RecoveredSchedulerState checkRecoveredState(
			SchedulerStateRecoverHelper.RecoveredSchedulerState state, StateMatcher stateMatcher) {
		assertThat(state.getSchedulerState(), stateMatcher);

		assertThat(state.getPendingJobs(), containsInAnyOrder(stateMatcher.pending.toArray(new JobStateMatcher[] {})));
		assertThat(state.getRunningJobs(), containsInAnyOrder(stateMatcher.running.toArray(new JobStateMatcher[] {})));
		assertThat(state.getFinishedJobs(),
				containsInAnyOrder(stateMatcher.finished.toArray(new JobStateMatcher[] {})));

		return state;
	}

	static Set<JobStatus> finishedJobStatus = new HashSet<>();

	static {
		finishedJobStatus.add(JobStatus.CANCELED);
		finishedJobStatus.add(JobStatus.FAILED);
		finishedJobStatus.add(JobStatus.KILLED);
		finishedJobStatus.add(JobStatus.FINISHED);
	}

	static class JobStateMatcher extends TypeSafeMatcher<JobState> {

		private final JobId id;

		private final List<TaskStateMatcher> pending = new ArrayList<>();

		private final List<TaskStateMatcher> running = new ArrayList<>();

		private final List<TaskStateMatcher> finished = new ArrayList<>();

		private final Set<String> eligibleTasks = new HashSet<>();

		private final JobStatus status;

		private int finishedNumber;

		private int pendingNumber;

		private boolean checkFinished;

		static JobStateMatcher job(JobId id, JobStatus status) {
			return new JobStateMatcher(id, status);
		}

		public JobStateMatcher(JobId id, JobStatus status) {
			this.id = id;
			this.status = status;
		}

		JobStateMatcher withEligible(String... tasks) {
			eligibleTasks.addAll(Arrays.asList(tasks));
			return this;
		}

		JobStateMatcher withPending(TaskStateMatcher task, boolean addToCount) {
			pending.add(task);
			if (addToCount) {
				pendingNumber++;
			}
			return this;
		}

		JobStateMatcher withRunning(TaskStateMatcher task) {
			running.add(task);
			return this;
		}

		JobStateMatcher withFinished(TaskStateMatcher task) {
			return withFinished(task, true);
		}

		JobStateMatcher withFinished(TaskStateMatcher task, boolean addToCount) {
			finished.add(task);
			if (addToCount) {
				finishedNumber++;
			}
			return this;
		}

		JobStateMatcher checkFinished() {
			checkFinished = true;
			return this;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("Job " + id);
		}

		@Override
		protected boolean matchesSafely(JobState item) {
			if (!id.equals(item.getId())) {
				return false;
			}

			Assert.assertEquals(status, item.getStatus());

			Assert.assertEquals("Pending tasks for " + id, pendingNumber, item.getNumberOfPendingTasks());
			Assert.assertEquals("Running tasks for " + id, running.size(), item.getNumberOfRunningTasks());
			Assert.assertEquals("Finished tasks for " + id, finishedNumber, item.getNumberOfFinishedTasks());

			Collection<TaskStateMatcher> all = new ArrayList<>();
			all.addAll(pending);
			all.addAll(running);
			all.addAll(finished);
			assertThat("Tasks for " + id, item.getTasks(), containsInAnyOrder(all.toArray(new TaskStateMatcher[] {})));

			assertThat("Submitted time for " + id, item.getSubmittedTime(), greaterThan(0L));

			if (checkFinished) {
				assertThat("Started time for " + id, item.getStartTime(), greaterThan(0L));
				assertThat("Finished time for " + id, item.getFinishedTime(), greaterThan(0L));
			}

			if (item instanceof InternalJob) {
				InternalJob internalJob = (InternalJob) item;

				if (!finishedJobStatus.contains(status)) {
					Set<String> actualEligible = new HashSet<>();
					for (EligibleTaskDescriptor desc : internalJob.getJobDescriptor().getEligibleTasks()) {
						actualEligible.add(desc.getTaskId().getReadableName());
					}
					Assert.assertEquals("Eligible tasks for " + id, eligibleTasks, actualEligible);
				}
			}

			return true;
		}

	}

	static class TaskStateMatcher extends TypeSafeMatcher<TaskState> {

		private final String name;

		private final TaskStatus status;

		private boolean checkFinished;

		public TaskStateMatcher(String name, TaskStatus status) {
			this.name = name;
			this.status = status;
		}

		public TaskStateMatcher checkFinished() {
			checkFinished = true;
			return this;
		}

		@Override
		public void describeTo(Description desc) {
			desc.appendText("Task " + name + ", " + status);
		}

		@Override
		public boolean matchesSafely(TaskState item) {
			if (!item.getName().equals(name)) {
				return false;
			}

			Assert.assertEquals("Status for " + item.getName(), status, item.getStatus());

			if (checkFinished) {
				assertThat("Finished time for " + item.getName(), item.getFinishedTime(), greaterThan(0L));
			} else {
				assertThat("Finished time for " + item.getName(), item.getFinishedTime(), is(-1L));
			}

			return true;
		}

		boolean checkIfTheSame(TaskState task) {
			if (!name.equals(task.getName())) {
				return false;
			}

			Assert.assertEquals(status, task.getStatus());

			return true;
		}
	}

	static StateMatcher state() {
		return new StateMatcher();
	}

	static TaskStateMatcher task(String name, TaskStatus status) {
		return new TaskStateMatcher(name, status);
	}

	static JobStateMatcher job(JobId id, JobStatus status) {
		return new JobStateMatcher(id, status);
	}

	@Before
	public void initTest() throws Exception {
		PASchedulerProperties.SCHEDULER_HOME.updateProperty(ClasspathUtils.findSchedulerHome());
		PASchedulerProperties.TASK_FORK.updateProperty("true");
		CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);

		if (inMemory) {
			dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
		} else {
			Configuration config = new Configuration()
					.configure(new File(this.getClass().getResource("/functionaltests/config/hibernate.cfg.xml").toURI()));
			dbManager = new SchedulerDBManager(config, true);
		}
	}

	@After
	public void cleanup() {
		if (dbManager != null) {
			dbManager.close();
		}
	}

	public static class TestDummyExecutable extends JavaExecutable {
		@Override
		public Serializable execute(TaskResult... results) throws Throwable {
			return null;
		}
	}

	public InternalJob defaultSubmitJob(TaskFlowJob job) throws Exception {
		return defaultSubmitJob(job, DEFAULT_USER_NAME, -1);
	}

	public InternalJob defaultSubmitJob(TaskFlowJob job, String userName) throws Exception {
		return defaultSubmitJob(job, userName, -1);
	}

	public InternalJob defaultSubmitJob(TaskFlowJob job, String userName, long submittedTime) throws Exception {
		if (job.getTasks().isEmpty()) {
			job.addTask(createDefaultTask("default test task"));
		}
		InternalJob internalJob = InternalJobFactory.createJob(job, getDefaultCredentials());
		internalJob.setOwner(userName);
		internalJob.submitAction();
		if (submittedTime > 0) {
			internalJob.setSubmittedTime(submittedTime);
		}
		dbManager.newJobSubmitted(internalJob);

		return internalJob;
	}

	public InternalJob loadInternalJob(boolean fullState, JobId jobId) {
		List<InternalJob> jobs = dbManager.loadJobs(fullState, jobId);
		Assert.assertEquals(1, jobs.size());
		return jobs.get(0);
	}

	public InternalJob defaultSubmitJobAndLoadInternal(boolean fullState, TaskFlowJob jobDef) throws Exception {
		return defaultSubmitJobAndLoadInternal(fullState, jobDef, DEFAULT_USER_NAME);
	}

	public InternalJob defaultSubmitJobAndLoadInternal(boolean fullState, TaskFlowJob jobDef, String userName)
			throws Exception {
		InternalJob job = defaultSubmitJob(jobDef, userName);
		return loadInternalJob(fullState, job.getId());
	}

	protected static JavaTask createDefaultTask(String taskName) {
		JavaTask task = new JavaTask();
		task.setName(taskName);
		task.setExecutableClassName(TestDummyExecutable.class.getName());
		return task;
	}

	public InternalJob saveSingleTask(Task task) throws Exception {
		TaskFlowJob job = new TaskFlowJob();
		job.addTask(task);
		InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
		Assert.assertEquals(1, jobData.getTasks().size());
		return jobData;
	}

	protected TaskState findTask(JobState jobState, String taskName) {
		for (TaskState taskState : jobState.getTasks()) {
			if (taskState.getName().equals(taskName)) {
				return taskState;
			}
		}
		Assert.fail("Didn't find task with name " + taskName);
		return null;
	}

	protected InternalTask startTask(InternalJob internalJob, InternalTask internalTask) throws Exception {
		internalTask.setExecuterInformation(new ExecuterInformation(null, NodeFactory.getDefaultNode()));
		internalJob.startTask(internalTask);
		return internalTask;
	}

	protected String createString(int length) {
		StringBuilder string = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			string.append("a");
		}
		return string.toString();
	}
}
