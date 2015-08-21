package org.ow2.proactive.scheduler.core.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.FilteredExceptionCallback;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.core.account.SchedulerAccount;
import org.ow2.proactive.scheduler.core.db.TaskData.DBTaskId;
import org.ow2.proactive.scheduler.core.db.TransactionHelper.SessionWork;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.forked.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalForkedJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalForkedScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.java.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.nativ.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ScriptExecutableContainer;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import static org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;


public class SchedulerDBManager {

    private static final String JAVA_PROPERTYNAME_NODB = "scheduler.database.nodb";

    private static final Logger logger = Logger.getLogger(SchedulerDBManager.class);

    private static final Logger debugLogger = Logger.getLogger(SchedulerDBManager.class);

    private static final Set<JobStatus> finishedJobStatuses;

    static {
        finishedJobStatuses = new HashSet<JobStatus>();
        finishedJobStatuses.add(JobStatus.CANCELED);
        finishedJobStatuses.add(JobStatus.FAILED);
        finishedJobStatuses.add(JobStatus.KILLED);
        finishedJobStatuses.add(JobStatus.FINISHED);
    }

    private static final Set<JobStatus> pendingJobStatuses;

    static {
        pendingJobStatuses = new HashSet<JobStatus>();
        pendingJobStatuses.add(JobStatus.PENDING);
    }

    private static final Set<JobStatus> runningJobStatuses;

    static {
        runningJobStatuses = new HashSet<JobStatus>();
        runningJobStatuses.add(JobStatus.PAUSED);
        runningJobStatuses.add(JobStatus.STALLED);
        runningJobStatuses.add(JobStatus.RUNNING);
    }

    private static final Set<JobStatus> notFinishedJobStatuses;

    static {
        notFinishedJobStatuses = new HashSet<JobStatus>();
        notFinishedJobStatuses.addAll(runningJobStatuses);
        notFinishedJobStatuses.addAll(pendingJobStatuses);
    }

    private final SessionFactory sessionFactory;

    private final TransactionHelper transactionHelper;

    public static SchedulerDBManager createUsingProperties() {
        if (System.getProperty(JAVA_PROPERTYNAME_NODB) != null) {
            return createInMemorySchedulerDBManager();
        } else {
            File configFile = new File(PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getValueAsString()));

            Map<String, String> propertiesToReplace = new HashMap<String, String>(2);
            propertiesToReplace.put("${proactive.home}", CentralPAPropertyRepository.PA_HOME.getValue());
            propertiesToReplace.put("${pa.scheduler.home}", PASchedulerProperties.SCHEDULER_HOME
                    .getValueAsString());

            Configuration configuration = createConfiguration(configFile, propertiesToReplace);

            boolean drop = PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();

            if (logger.isInfoEnabled()) {
                logger.info("Starting Scheduler DB Manager " + "with drop DB = " + drop +
                    " and configuration file = " + configFile.getAbsolutePath());
            }

            return new SchedulerDBManager(configuration, drop);
        }
    }

    public static SchedulerDBManager createInMemorySchedulerDBManager() {
        Configuration config = new Configuration();
        config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        config.setProperty("hibernate.connection.url", "jdbc:h2:mem:scheduler");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return new SchedulerDBManager(config, true);
    }

    public SchedulerDBManager(Configuration configuration, boolean drop) {
        try {
            configuration.addAnnotatedClass(JobData.class);
            configuration.addAnnotatedClass(TaskData.class);
            configuration.addAnnotatedClass(TaskResultData.class);
            configuration.addAnnotatedClass(JobClasspathContent.class);
            configuration.addAnnotatedClass(JavaTaskData.class);
            configuration.addAnnotatedClass(ForkedJavaTaskData.class);
            configuration.addAnnotatedClass(NativeTaskData.class);
            configuration.addAnnotatedClass(ScriptTaskData.class);
            configuration.addAnnotatedClass(ScriptData.class);
            configuration.addAnnotatedClass(EnvironmentModifierData.class);
            configuration.addAnnotatedClass(SelectorData.class);
            configuration.addAnnotatedClass(ThirdPartyCredentialData.class);
            if (drop) {
                configuration.setProperty("hibernate.hbm2ddl.auto", "create");
            }

            configuration.setProperty("hibernate.id.new_generator_mappings", "true");
            configuration.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
            configuration.setProperty("hibernate.connection.isolation", "2");

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            debugLogger.error("Initial SessionFactory creation failed", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed", ex);
        }
        transactionHelper = new TransactionHelper(sessionFactory);

    }

    public List<JobInfo> getJobs(final int offset, final int limit, final String user, final boolean pending,
            final boolean runnning, final boolean finished,
            final List<SortParameter<JobSortParameter>> sortParameters) {

        if (!pending && !runnning && !finished) {
            return Collections.emptyList();
        }

        return runWithoutTransaction(new SessionWork<List<JobInfo>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<JobInfo> executeWork(Session session) {
                Criteria criteria = session.createCriteria(JobData.class);
                if (limit > 0) {
                    criteria.setMaxResults(limit);
                }
                if (offset >= 0) {
                    criteria.setFirstResult(offset);
                }
                if (user != null) {
                    criteria.add(Restrictions.eq("owner", user));
                }
                boolean allJobs = pending && runnning && finished;
                if (!allJobs) {
                    Set<JobStatus> status = new HashSet<JobStatus>();
                    if (pending) {
                        status.addAll(pendingJobStatuses);
                    }
                    if (runnning) {
                        status.addAll(runningJobStatuses);
                    }
                    if (finished) {
                        status.addAll(finishedJobStatuses);
                    }
                    criteria.add(Restrictions.in("status", status.toArray(new JobStatus[status.size()])));
                }

                criteria.add(Restrictions.eq("removedTime", -1L));

                if (sortParameters != null) {
                    Order sortOrder;
                    for (SortParameter<JobSortParameter> param : sortParameters) {
                        switch (param.getParameter()) {
                            case ID:
                                sortOrder = configureSortOrder(param, Property.forName("id"));
                                break;
                            case NAME:
                                sortOrder = configureSortOrder(param, Property.forName("jobName"));
                                break;
                            case OWNER:
                                sortOrder = configureSortOrder(param, Property.forName("owner"));
                                break;
                            case PRIORITY:
                                sortOrder = configureSortOrder(param, Property.forName("priority"));
                                break;
                            case STATE:
                                sortOrder = new GroupByStatusSortOrder(param.getSortOrder(), "status");
                                break;
                            default:
                                throw new IllegalArgumentException("Unsupported sort paramter: " +
                                    param.getParameter());
                        }
                        criteria.addOrder(sortOrder);
                    }
                }

                List<JobData> jobsList = criteria.list();
                List<JobInfo> result = new ArrayList<JobInfo>(jobsList.size());
                for (JobData jobData : jobsList) {
                    JobInfo jobInfo = jobData.toJobInfo();
                    result.add(jobInfo);
                }

                return result;
            }

        });
    }

    private Order configureSortOrder(SortParameter<JobSortParameter> param, Property property) {
        if (param.getSortOrder().isAscending()) {
            return property.asc();
        } else {
            return property.desc();
        }
    }

    public List<JobUsage> getUsage(final String userName, final Date startDate, final Date endDate) {
        return runWithoutTransaction(new SessionWork<List<JobUsage>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<JobUsage> executeWork(Session session) {
                if (startDate == null || endDate == null) {
                    throw new DatabaseManagerException("Start and end dates can't be null.");
                }

                Criteria criteria = session.createCriteria(JobData.class);
                criteria.setFetchMode("tasks", FetchMode.JOIN);
                criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                criteria.add(Restrictions.eq("owner", userName));
                // exclude killed but not started jobs
                criteria.add(Restrictions.gt("startTime", -1L));
                criteria.add(Restrictions.and(Restrictions.ge("finishedTime", startDate.getTime()),
                        Restrictions.le("finishedTime", endDate.getTime())));

                List<JobData> jobsList = criteria.list();

                List<JobUsage> result = new ArrayList<JobUsage>(jobsList.size());
                for (JobData jobData : jobsList) {
                    JobUsage jobUsage = jobData.toJobUsage();
                    result.add(jobUsage);
                }
                return result;
            }
        });
    }

    public void setCallback(FilteredExceptionCallback callback) {
        this.transactionHelper.setCallback(callback);
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void close() {
        try {
            if (sessionFactory != null) {
                debugLogger.info("Closing session factory");
                sessionFactory.close();
            }
        } catch (Exception e) {
            debugLogger.error("Error while closing database", e);
        }
    }

    public long getFinishedJobsCount() {
        return getJobsNumberWithStatus(finishedJobStatuses);
    }

    public long getPendingJobsCount() {
        return getJobsNumberWithStatus(Arrays.asList(JobStatus.PAUSED, JobStatus.PENDING));
    }

    public long getRunningJobsCount() {
        return getJobsNumberWithStatus(Arrays.asList(JobStatus.RUNNING, JobStatus.STALLED));
    }

    public long getTotalJobsCount() {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Query query = session.createQuery("select count(*) from JobData where removedTime = -1");
                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    private long getJobsNumberWithStatus(final Collection<JobStatus> status) {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Query query = session.createQuery(
                        "select count(*) from JobData where status in (:status) and removedTime = -1")
                        .setParameterList("status", status);

                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public long getFinishedTasksCount() {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Query query = session
                        .createQuery(
                                "select count(*) from TaskData task where taskStatus in (:taskStatus) and task.jobData.removedTime = -1")
                        .setParameterList("taskStatus", Arrays.asList(TaskStatus.FINISHED, TaskStatus.FAULTY));

                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public long getPendingTasksCount() {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Collection<TaskStatus> taskStatus = Arrays.asList(TaskStatus.SUBMITTED, TaskStatus.PAUSED,
                        TaskStatus.PENDING, TaskStatus.WAITING_ON_ERROR, TaskStatus.WAITING_ON_FAILURE);
                Query query = session
                        .createQuery(
                                "select count(*) from TaskData task where taskStatus in (:taskStatus) and task.jobData.status in (:jobStatus) and task.jobData.removedTime = -1")
                        .setParameterList("jobStatus", notFinishedJobStatuses).setParameterList("taskStatus",
                                taskStatus);

                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public long getRunningTasksCount() {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Query query = session.createQuery(
                        "select count(*) from TaskData task where taskStatus in (:taskStatus) "
                            + "and task.jobData.status in (:jobStatus) and task.jobData.removedTime = -1")
                        .setParameterList("jobStatus", notFinishedJobStatuses).setParameterList("taskStatus",
                                Arrays.asList(TaskStatus.RUNNING));

                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public long getTotalTasksCount() {
        return runWithoutTransaction(new SessionWork<Long>() {

            @Override
            public Long executeWork(Session session) {
                Query query = session
                        .createQuery("select count(*) from TaskData task where task.jobData.removedTime = -1");
                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public double getMeanJobPendingTime() {
        return runWithoutTransaction(new SessionWork<Double>() {
            @Override
            public Double executeWork(Session session) {
                Query query = session
                        .createQuery("select avg(startTime - submittedTime) from JobData where startTime > 0 and submittedTime > 0");
                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });
    }

    public double getMeanJobExecutionTime() {
        return runWithoutTransaction(new SessionWork<Double>() {
            @Override
            public Double executeWork(Session session) {
                Query query = session
                        .createQuery("select avg(finishedTime - startTime) from JobData where startTime > 0 and finishedTime > 0");
                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });
    }

    public double getMeanJobSubmittingPeriod() {
        return runWithoutTransaction(new SessionWork<Double>() {
            @Override
            public Double executeWork(Session session) {
                Query query = session
                        .createQuery("select count(*), min(submittedTime), max(submittedTime) from JobData");
                Object[] result = (Object[]) query.uniqueResult();
                Long count = (Long) result[0];
                Long minSubmittedTime = (Long) result[1];
                Long maxSubmittedTime = (Long) result[2];
                if (count < 2) {
                    return 0d;
                } else {
                    return (maxSubmittedTime - minSubmittedTime) / (double) (count - 1);
                }
            }

        });
    }

    public long getJobRunningTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Long result = runWithoutTransaction(new SessionWork<Long>() {
            @Override
            public Long executeWork(Session session) {
                JobData jobData = (JobData) session.get(JobData.class, id);
                if (jobData == null) {
                    return null;
                }
                if (jobData.getFinishedTime() > 0) {
                    return jobData.getFinishedTime() - jobData.getStartTime();
                } else {
                    return null;
                }
            }
        });
        if (result == null) {
            throw new IllegalArgumentException("Job doesn't exists or didn't finish execution");
        } else {
            return result;
        }
    }

    public long getJobPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Long result = runWithoutTransaction(new SessionWork<Long>() {
            @Override
            public Long executeWork(Session session) {
                JobData jobData = (JobData) session.get(JobData.class, id);
                if (jobData == null) {
                    return null;
                }
                if (jobData.getStartTime() > 0) {
                    return jobData.getStartTime() - jobData.getSubmittedTime();
                } else {
                    return null;
                }
            }
        });
        if (result == null) {
            throw new IllegalArgumentException("Job doesn't exists or didn't start execution");
        } else {
            return result;
        }
    }

    public double getMeanTaskPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Double result = runWithoutTransaction(new SessionWork<Double>() {
            @Override
            public Double executeWork(Session session) {
                Query jobSubmittedTimeQuery = session.createQuery(
                        "select submittedTime from JobData where id = :id").setParameter("id", id);
                Long jobSubmittedTime = (Long) jobSubmittedTimeQuery.uniqueResult();
                if (jobSubmittedTime == null) {
                    return null;
                }
                Query query = session
                        .createQuery(
                                "select avg(startTime - :jobSubmittedTime) from TaskData task where task.jobData.id = :id and task.startTime > 0")
                        .setParameter("id", id).setParameter("jobSubmittedTime", jobSubmittedTime);

                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });
        if (result == null) {
            throw new IllegalArgumentException("Job doesn't exists");
        } else {
            return result;
        }
    }

    public double getMeanTaskRunningTime(String jobId) {
        final long id = Long.parseLong(jobId);
        Double result = runWithoutTransaction(new SessionWork<Double>() {
            @Override
            public Double executeWork(Session session) {
                Query jobQuery = session.createQuery("select id from JobData where id = :id").setParameter(
                        "id", id);
                if (jobQuery.uniqueResult() == null) {
                    return null;
                }

                Query query = session
                        .createQuery(
                                "select avg(task.finishedTime - task.startTime) from TaskData task where task.startTime > 0 and task.finishedTime > 0 and task.jobData.id = :id")
                        .setParameter("id", id);

                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });
        if (result == null) {
            throw new IllegalArgumentException("Job doesn't exists");
        } else {
            return result;
        }
    }

    public int getTotalNumberOfHostsUsed(String jobId) {
        final long id = Long.parseLong(jobId);
        Long result = runWithoutTransaction(new SessionWork<Long>() {
            @Override
            public Long executeWork(Session session) {
                Query jobQuery = session.createQuery("select id from JobData where id = :id").setParameter(
                        "id", id);
                if (jobQuery.uniqueResult() == null) {
                    return null;
                }

                Query query = session
                        .createQuery(
                                "select count(distinct executionHostName) from TaskData task where task.jobData.id = :id")
                        .setParameter("id", id);

                Long result = (Long) query.uniqueResult();
                return result;
            }

        });
        if (result == null) {
            throw new IllegalArgumentException("Job doesn't exists");
        } else {
            return result.intValue();
        }
    }

    public SchedulerAccount readAccount(final String username) {
        return runWithoutTransaction(new SessionWork<SchedulerAccount>() {

            @Override
            public SchedulerAccount executeWork(Session session) {
                Query tasksQuery = session.createQuery(
                        "select count(*), sum(task.finishedTime) - sum(task.startTime) from TaskData task "
                            + "where task.finishedTime > 0 and task.jobData.owner = :username").setParameter(
                        "username", username);

                int taskCount;
                long taskDuration;

                Object[] taskResult = (Object[]) tasksQuery.uniqueResult();
                taskCount = ((Number) taskResult[0]).intValue();
                if (taskResult[1] != null) {
                    taskDuration = ((Number) taskResult[1]).longValue();
                } else {
                    taskDuration = 0;
                }

                int jobCount;
                long jobDuration;

                Query jobQuery = session.createQuery(
                        "select count(*), sum(finishedTime) - sum(startTime) from JobData"
                            + " where owner = :username and finishedTime > 0").setParameter("username",
                        username);

                Object[] jobResult = (Object[]) jobQuery.uniqueResult();
                jobCount = ((Number) jobResult[0]).intValue();
                if (jobResult[1] != null) {
                    jobDuration = ((Number) jobResult[1]).longValue();
                } else {
                    jobDuration = 0;
                }

                return new SchedulerAccount(username, taskCount, taskDuration, jobCount, jobDuration);
            }

        });
    }

    private void removeJobScripts(Session session, long jobId) {
        session
                .createQuery(
                        "delete from ScriptData where"
                            + " id in (select td.envScript from ForkedJavaTaskData td where td.taskData.id.jobId = :jobId)"
                            + " or id in (select td.generationScript from NativeTaskData td where td.taskData.id.jobId = :jobId)"
                            + " or id in (select preScript from TaskData where id.jobId = :jobId)"
                            + " or id in (select postScript from TaskData where id.jobId = :jobId)"
                            + " or id in (select cleanScript from TaskData where id.jobId = :jobId)"
                            + " or id in (select flowScript from TaskData where id.jobId = :jobId)"
                            + " or id in (select td.script from ScriptTaskData td where td.taskData.id.jobId = :jobId)"
                            + " or taskData.id.jobId = :jobId").setParameter("jobId", jobId).executeUpdate();
    }

    private void removeJobRuntimeData(Session session, long jobId) {
        removeJobScripts(session, jobId);

        session.createQuery("delete from JavaTaskData where taskData.id.jobId = :jobId").setParameter(
                "jobId", jobId).executeUpdate();
        session.createQuery("delete from ForkedJavaTaskData where taskData.id.jobId = :jobId").setParameter(
                "jobId", jobId).executeUpdate();
        session.createQuery("delete from NativeTaskData where taskData.id.jobId = :jobId").setParameter(
                "jobId", jobId).executeUpdate();
        session.createQuery("delete from ScriptTaskData where taskData.id.jobId = :jobId").setParameter(
                "jobId", jobId).executeUpdate();

        session.createQuery("delete from SelectorData where taskData.id.jobId = :jobId").setParameter(
                "jobId", jobId).executeUpdate();
    }

    public synchronized void removeJob(final JobId jobId, final long removedTime, final boolean removeData) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long id = jobId(jobId);

                if (removeData) {
                    session.createSQLQuery("delete from TASK_DATA_DEPENDENCIES where JOB_ID = :jobId")
                            .setParameter("jobId", id).executeUpdate();
                    session.createSQLQuery("delete from TASK_DATA_JOINED_BRANCHES where JOB_ID = :jobId")
                            .setParameter("jobId", id).executeUpdate();

                    removeJobScripts(session, id);

                    session.createQuery("delete from JobData where id = :jobId").setParameter("jobId", id)
                            .executeUpdate();
                } else {
                    String jobUpdate = "update JobData set removedTime = :removedTime where id = :jobId";
                    session.createQuery(jobUpdate).setParameter("removedTime", removedTime).setParameter(
                            "jobId", id).executeUpdate();
                }

                return null;
            }

        });
    }

    public List<InternalJob> loadNotFinishedJobs(boolean fullState) {
        return loadJobs(fullState, notFinishedJobStatuses, -1);
    }

    public List<InternalJob> loadFinishedJobs(boolean fullState, long period) {
        return loadJobs(fullState, finishedJobStatuses, period);
    }

    private List<InternalJob> loadJobs(final boolean fullState, final Collection<JobStatus> status,
            final long period) {
        return runWithoutTransaction(new SessionWork<List<InternalJob>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<InternalJob> executeWork(Session session) {
                Query query;
                if (period > 0) {
                    long minSubmittedTime = System.currentTimeMillis() - period;
                    query = session
                            .createQuery(
                                    "select id from JobData where status in (:status) and removedTime = -1 and submittedTime >= :minSubmittedTime")
                            .setParameter("minSubmittedTime", minSubmittedTime).setParameterList("status",
                                    status);
                } else {
                    query = session.createQuery(
                            "select id from JobData where status in (:status) and removedTime = -1")
                            .setParameterList("status", status);
                }

                List<Long> ids = query.list();

                return loadInternalJobs(fullState, session, ids);
            }

        });
    }

    public InternalJob loadJobWithTasksIfNotRemoved(final JobId id) {
        return runWithoutTransaction(new SessionWork<InternalJob>() {
            @Override
            public InternalJob executeWork(Session session) {
                Query jobQuery = session
                        .createQuery("from JobData as job where job.id in (:ids) and job.removedTime = -1");
                List<InternalJob> result = new ArrayList<InternalJob>(1);
                batchLoadJobs(session, false, jobQuery, Collections.singletonList(jobId(id)), result);
                if (result.isEmpty()) {
                    return null;
                } else {
                    return result.get(0);
                }
            }

        });
    }

    public List<InternalJob> loadJobs(final boolean fullState, final JobId... jobIds) {
        return runWithoutTransaction(new SessionWork<List<InternalJob>>() {
            @Override
            public List<InternalJob> executeWork(Session session) {
                List<Long> ids = new ArrayList<Long>(jobIds.length);
                for (JobId jobId : jobIds) {
                    ids.add(jobId(jobId));
                }
                return loadInternalJobs(fullState, session, ids);
            }

        });
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<TaskData>> loadJobsTasks(Session session, List<Long> jobIds) {
        Query tasksQuery = session
                .createQuery(
                        "from TaskData as task left outer join fetch task.dependentTasks where task.id.jobId in (:ids)")
                .setParameterList("ids", jobIds).setResultTransformer(
                        DistinctRootEntityResultTransformer.INSTANCE);

        Map<Long, List<TaskData>> tasksMap = new HashMap<Long, List<TaskData>>(jobIds.size());
        for (Long id : jobIds) {
            tasksMap.put(id, new ArrayList<TaskData>());
        }

        List<TaskData> tasks = tasksQuery.list();
        for (TaskData task : tasks) {
            tasksMap.get(task.getJobData().getId()).add(task);
        }

        return tasksMap;
    }

    private List<InternalJob> loadInternalJobs(boolean fullState, Session session, List<Long> ids) {
        Query jobQuery = session.createQuery("from JobData as job where job.id in (:ids)");

        List<InternalJob> result = new ArrayList<InternalJob>(ids.size());

        final int BATCH_SIZE = 100;

        List<Long> batchLoadIds = new ArrayList<Long>(BATCH_SIZE);

        for (Long id : ids) {
            batchLoadIds.add(id);
            if (batchLoadIds.size() == BATCH_SIZE) {
                batchLoadJobs(session, fullState, jobQuery, batchLoadIds, result);
                batchLoadIds.clear();
                session.clear();
            }
        }
        if (!batchLoadIds.isEmpty()) {
            batchLoadJobs(session, fullState, jobQuery, batchLoadIds, result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void batchLoadJobs(Session session, boolean fullState, Query jobQuery, List<Long> ids,
            Collection<InternalJob> jobs) {
        Map<Long, List<TaskData>> tasksMap = loadJobsTasks(session, ids);

        jobQuery.setParameterList("ids", ids);
        List<JobData> jobsList = (List<JobData>) jobQuery.list();

        for (JobData jobData : jobsList) {
            InternalJob internalJob = jobData.toInternalJob();
            if (fullState) {
                String[] classpath = jobData.getClasspath();
                JobEnvironment env = new JobEnvironment(classpath);
                internalJob.setEnvironment(env);
            }
            internalJob.setTasks(toInternalTasks(fullState, internalJob, tasksMap.get(jobData.getId())));

            jobs.add(internalJob);
        }
    }

    private Collection<InternalTask> toInternalTasks(boolean loadFullState, InternalJob internalJob,
            List<TaskData> taskRuntimeDataList) {
        Map<DBTaskId, InternalTask> tasks = new HashMap<DBTaskId, InternalTask>(taskRuntimeDataList.size());

        try {
            for (TaskData taskData : taskRuntimeDataList) {
                InternalTask internalTask = taskData.toInternalTask(internalJob);
                if (loadFullState) {
                    internalTask.setParallelEnvironment(taskData.getParallelEnvironment());
                    internalTask.setGenericInformations(taskData.getGenericInformation());
                    for (ScriptData scriptData : taskData.getSelectionScripts()) {
                        internalTask.addSelectionScript(scriptData.createSelectionScript());
                    }
                    if (taskData.getCleanScript() != null) {
                        internalTask.setCleaningScript(taskData.getCleanScript().createSimpleScript());
                    }
                    if (taskData.getPreScript() != null) {
                        internalTask.setPreScript(taskData.getPreScript().createSimpleScript());
                    }
                    if (taskData.getPostScript() != null) {
                        internalTask.setPostScript(taskData.getPostScript().createSimpleScript());
                    }
                    if (taskData.getFlowScript() != null) {
                        internalTask.setFlowScript(taskData.getFlowScript().createFlowScript());
                    }
                    for (SelectorData selectorData : taskData.getDataspaceSelectors()) {
                        if (selectorData.isInput()) {
                            InputSelector selector = selectorData.createInputSelector();
                            internalTask.addInputFiles(selector.getInputFiles(), selector.getMode());
                        } else {
                            OutputSelector selector = selectorData.createOutputSelector();
                            internalTask.addOutputFiles(selector.getOutputFiles(), selector.getMode());
                        }
                    }
                }
                tasks.put(taskData.getId(), internalTask);
            }
        } catch (InvalidScriptException e) {
            throw new DatabaseManagerException("Failed to initialize loaded script", e);
        }

        for (TaskData taskData : taskRuntimeDataList) {
            InternalTask internalTask = tasks.get(taskData.getId());
            if (!taskData.getDependentTasks().isEmpty()) {
                for (DBTaskId dependent : taskData.getDependentTasks()) {
                    internalTask.addDependence(tasks.get(dependent));
                }
            }
            if (loadFullState) {
                if (taskData.getIfBranch() != null) {
                    internalTask.setIfBranch(tasks.get(taskData.getIfBranch().getId()));
                }
                if (!taskData.getJoinedBranches().isEmpty()) {
                    List<InternalTask> branches = new ArrayList<InternalTask>(taskData.getJoinedBranches()
                            .size());
                    for (DBTaskId joinedBranch : taskData.getJoinedBranches()) {
                        branches.add(tasks.get(joinedBranch));
                    }
                    internalTask.setJoinedBranches(branches);
                }
                internalTask.setName(internalTask.getName());
            }
        }

        return tasks.values();
    }

    public void changeJobPriority(final JobId jobId, final JobPriority priority) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long id = jobId(jobId);
                String jobUpdate = "update JobData set priority = :priority where id = :jobId";
                session.createQuery(jobUpdate).setParameter("priority", priority).setParameter("jobId", id)
                        .executeUpdate();
                return null;
            }
        });
    }

    public void jobTaskStarted(final InternalJob job, final InternalTask task,
            final boolean taskStatusToPending) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long jobId = jobId(job);

                String jobUpdate = "update JobData set status = :status, "
                    + "startTime = :startTime, numberOfPendingTasks = :numberOfPendingTasks, "
                    + "numberOfRunningTasks = :numberOfRunningTasks where id = :jobId";

                JobInfo jobInfo = job.getJobInfo();

                session.createQuery(jobUpdate).setParameter("status", jobInfo.getStatus()).setParameter(
                        "startTime", jobInfo.getStartTime()).setParameter("numberOfPendingTasks",
                        jobInfo.getNumberOfPendingTasks()).setParameter("numberOfRunningTasks",
                        jobInfo.getNumberOfRunningTasks()).setParameter("jobId", jobId).executeUpdate();

                if (taskStatusToPending) {
                    JobData job = (JobData) session.load(JobData.class, jobId);
                    String taskStatusUpdate = "update TaskData task set task.taskStatus = :taskStatus "
                        + "where task.jobData = :job";
                    session.createQuery(taskStatusUpdate).setParameter("taskStatus", TaskStatus.PENDING)
                            .setParameter("job", job).executeUpdate();
                }

                TaskData.DBTaskId taskId = taskId(task);

                String taskUpdate = "update TaskData task set task.taskStatus = :taskStatus, "
                    + "task.startTime = :startTime, task.finishedTime = :finishedTime, "
                    + "task.executionHostName = :executionHostName where task.id = :taskId";

                TaskInfo taskInfo = task.getTaskInfo();

                session.createQuery(taskUpdate).setParameter("taskStatus", taskInfo.getStatus())
                        .setParameter("startTime", taskInfo.getStartTime()).setParameter("finishedTime",
                                taskInfo.getFinishedTime()).setParameter("executionHostName",
                                taskInfo.getExecutionHostName()).setParameter("taskId", taskId)
                        .executeUpdate();

                return null;
            }

        });
    }

    public void taskRestarted(final InternalJob job, final InternalTask task, final TaskResultImpl result) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long jobId = jobId(job);

                String jobUpdate = "update JobData set status = :status, "
                    + "numberOfPendingTasks = :numberOfPendingTasks, "
                    + "numberOfRunningTasks = :numberOfRunningTasks where id = :jobId";

                JobInfo jobInfo = job.getJobInfo();

                session.createQuery(jobUpdate).setParameter("status", jobInfo.getStatus()).setParameter(
                        "numberOfPendingTasks", jobInfo.getNumberOfPendingTasks()).setParameter(
                        "numberOfRunningTasks", jobInfo.getNumberOfRunningTasks()).setParameter("jobId",
                        jobId).executeUpdate();

                TaskData.DBTaskId taskId = taskId(task);

                String taskUpdate = "update TaskData set taskStatus = :taskStatus, "
                    + "numberOfExecutionLeft = :numberOfExecutionLeft,"
                    + "numberOfExecutionOnFailureLeft = :numberOfExecutionOnFailureLeft"
                    + " where id = :taskId";

                TaskInfo taskInfo = task.getTaskInfo();

                session.createQuery(taskUpdate).setParameter("taskStatus", taskInfo.getStatus())
                        .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                        .setParameter("numberOfExecutionOnFailureLeft",
                                taskInfo.getNumberOfExecutionOnFailureLeft()).setParameter("taskId", taskId)
                        .executeUpdate();

                if (result != null) {
                    saveTaskResult(taskId, result, session);
                }

                return null;
            }

        });
    }

    @SuppressWarnings("unchecked")
    public void updateAfterWorkflowTaskFinished(final InternalJob job, final ChangedTasksInfo changesInfo,
            final TaskResultImpl result) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                String jobUpdate = "update JobData set status = :status, "
                    + "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, "
                    + "numberOfFinishedTasks = :numberOfFinishedTasks, "
                    + "numberOfRunningTasks = :numberOfRunningTasks, "
                    + "totalNumberOfTasks =:totalNumberOfTasks where id = :jobId";

                long jobId = jobId(job);

                JobInfo jobInfo = job.getJobInfo();

                session.createQuery(jobUpdate).setParameter("status", jobInfo.getStatus()).setParameter(
                        "finishedTime", jobInfo.getFinishedTime()).setParameter("numberOfPendingTasks",
                        jobInfo.getNumberOfPendingTasks()).setParameter("numberOfFinishedTasks",
                        jobInfo.getNumberOfFinishedTasks()).setParameter("numberOfRunningTasks",
                        jobInfo.getNumberOfRunningTasks()).setParameter("totalNumberOfTasks",
                        jobInfo.getTotalNumberOfTasks()).setParameter("jobId", jobId).executeUpdate();

                JobData jobRuntimeData = (JobData) session.load(JobData.class, jobId);

                List<DBTaskId> taskIds = new ArrayList<TaskData.DBTaskId>(changesInfo.getSkippedTasks()
                        .size() +
                    changesInfo.getUpdatedTasks().size());
                for (TaskId id : changesInfo.getSkippedTasks()) {
                    taskIds.add(taskId(id));
                }
                for (TaskId id : changesInfo.getUpdatedTasks()) {
                    taskIds.add(taskId(id));
                }

                List<TaskData> taskRuntimeDataList = new ArrayList<TaskData>();
                List<InternalTask> tasks = new ArrayList<InternalTask>();

                Query tasksQuery = session.createQuery("from TaskData where id in (:ids)").setParameterList(
                        "ids", taskIds);
                List<TaskData> tasksToUpdate = tasksQuery.list();
                for (TaskData taskData : tasksToUpdate) {
                    InternalTask task = job.getIHMTasks().get(taskData.createTaskId(job));
                    taskData.updateMutableAttributes(task);
                    session.update(taskData);
                    taskRuntimeDataList.add(taskData);
                    tasks.add(task);
                }

                int counter = 0;
                for (TaskId newTaskId : changesInfo.getNewTasks()) {
                    InternalTask task = job.getIHMTasks().get(newTaskId);
                    if (task.getExecutableContainer() == null) {
                        InternalTask from = task.getReplicatedFrom();
                        ExecutableContainer container = from.getExecutableContainer();
                        if (container == null) {
                            container = loadExecutableContainer(session, from);
                        }
                        task.setExecutableContainer(container);
                    }
                    TaskData taskData = saveNewTask(session, jobRuntimeData, task);
                    taskRuntimeDataList.add(taskData);
                    tasks.add(task);
                    if (++counter % 50 == 0) {
                        session.flush();
                        session.clear();
                    }
                }

                saveTaskDependencies(session, tasks, taskRuntimeDataList);

                TaskData.DBTaskId taskId = taskId(result.getTaskId());
                saveTaskResult(taskId, result, session);

                if (finishedJobStatuses.contains(job.getStatus())) {
                    removeJobRuntimeData(session, jobId);
                }

                return null;
            }

        }, false);
    }

    public void updateAfterJobKilled(InternalJob job, Set<TaskId> tasksToUpdate) {
        updateAfterTaskFinished(job, null, null, tasksToUpdate);
    }

    public void updateAfterJobFailed(InternalJob job, InternalTask finishedTask, TaskResultImpl result,
            Set<TaskId> tasksToUpdate) {
        updateAfterTaskFinished(job, finishedTask, result, tasksToUpdate);
    }

    public void updateJobAndTasksState(final InternalJob job) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                String taskUpdate = "update TaskData task set task.taskStatus = :taskStatus where task.id = :taskId";

                Query taskUpdateQuery = session.createQuery(taskUpdate);
                for (TaskState task : job.getTasks()) {
                    TaskInfo taskInfo = task.getTaskInfo();
                    taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus()).setParameter("taskId",
                            taskId(task.getId())).executeUpdate();
                }

                String jobUpdate = "update JobData set status = :status where id = :jobId";

                JobInfo jobInfo = job.getJobInfo();

                session.createQuery(jobUpdate).setParameter("status", jobInfo.getStatus()).setParameter(
                        "jobId", jobId(job)).executeUpdate();

                return null;
            }

        });
    }

    public void updateAfterTaskFinished(final InternalJob job, final InternalTask finishedTask,
            final TaskResultImpl result) {
        updateAfterTaskFinished(job, finishedTask, result, new HashSet<TaskId>(1));
    }

    private void updateAfterTaskFinished(final InternalJob job, final InternalTask finishedTask,
            final TaskResultImpl result, final Set<TaskId> tasksToUpdate) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long jobId = jobId(job);

                String jobUpdate = "update JobData set status = :status, "
                    + "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, "
                    + "numberOfFinishedTasks = :numberOfFinishedTasks, "
                    + "numberOfRunningTasks = :numberOfRunningTasks where id = :jobId";

                JobInfo jobInfo = job.getJobInfo();

                session.createQuery(jobUpdate).setParameter("status", jobInfo.getStatus()).setParameter(
                        "finishedTime", jobInfo.getFinishedTime()).setParameter("numberOfPendingTasks",
                        jobInfo.getNumberOfPendingTasks()).setParameter("numberOfFinishedTasks",
                        jobInfo.getNumberOfFinishedTasks()).setParameter("numberOfRunningTasks",
                        jobInfo.getNumberOfRunningTasks()).setParameter("jobId", jobId).executeUpdate();

                String taskUpdate = "update TaskData task set task.taskStatus = :taskStatus, "
                    + "task.finishedTime = :finishedTime, " + "task.executionDuration = :executionDuration "
                    + "where task.id = :taskId";

                Query taskUpdateQuery = session.createQuery(taskUpdate);

                if (finishedTask != null) {
                    tasksToUpdate.add(finishedTask.getId());
                }

                for (TaskId id : tasksToUpdate) {
                    InternalTask task = job.getIHMTasks().get(id);
                    TaskData.DBTaskId taskId = taskId(task.getId());

                    TaskInfo taskInfo = task.getTaskInfo();

                    taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus()).setParameter(
                            "finishedTime", taskInfo.getFinishedTime()).setParameter("executionDuration",
                            taskInfo.getExecutionDuration()).setParameter("taskId", taskId).executeUpdate();
                }

                if (result != null) {
                    TaskData.DBTaskId taskId = taskId(finishedTask.getId());
                    saveTaskResult(taskId, result, session);
                }

                if (finishedJobStatuses.contains(job.getStatus())) {
                    session.flush();
                    session.clear();

                    removeJobRuntimeData(session, jobId);
                }

                return null;
            }

        });
    }

    private TaskResultData saveTaskResult(TaskData.DBTaskId taskId, TaskResultImpl result, Session session) {
        TaskData taskRuntimeData = (TaskData) session.load(TaskData.class, taskId);

        TaskResultData resultData = TaskResultData.createTaskResultData(taskRuntimeData, result);
        session.save(resultData);

        return resultData;
    }

    public void jobSetToBeRemoved(final JobId jobId) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                long id = jobId(jobId);

                String jobUpdate = "update JobData set toBeRemoved = :toBeRemoved where id = :jobId";

                session.createQuery(jobUpdate).setParameter("toBeRemoved", true).setParameter("jobId", id)
                        .executeUpdate();

                return null;
            }
        });
    }

    public Map<TaskId, TaskResult> loadTasksResults(final JobId jobId, final List<TaskId> taskIds) {
        if (taskIds.isEmpty()) {
            throw new IllegalArgumentException("TaskIds list is empty");
        }

        return runWithoutTransaction(new SessionWork<Map<TaskId, TaskResult>>() {

            @Override
            public Map<TaskId, TaskResult> executeWork(Session session) {
                JobData job = (JobData) session.get(JobData.class, jobId(jobId));

                if (job == null) {
                    throw new DatabaseManagerException("Invalid job id: " + jobId);
                }

                List<TaskData.DBTaskId> dbTaskIds = new ArrayList<TaskData.DBTaskId>(taskIds.size());
                for (TaskId taskId : taskIds) {
                    dbTaskIds.add(taskId(taskId));
                }

                Query query = session
                        .createQuery(
                                "select taskResult, "
                                    + "task.id, "
                                    + "task.taskName, "
                                    + "task.preciousResult from TaskResultData as taskResult join taskResult.taskRuntimeData as task "
                                    + "where task.id in (:tasksIds) order by task.id, taskResult.resultTime desc")
                        .setParameterList("tasksIds", dbTaskIds);

                JobResultImpl jobResult = loadJobResult(session, query, job, jobId);
                if (jobResult == null) {
                    throw new DatabaseManagerException("Failed to load result for tasks " + taskIds +
                        " (job: " + jobId + ")");
                }

                Map<TaskId, TaskResult> resultsMap = new HashMap<TaskId, TaskResult>(taskIds.size());
                for (TaskId taskId : taskIds) {
                    TaskResult taskResult = null;
                    for (TaskResult result : jobResult.getAllResults().values()) {
                        if (result.getTaskId().equals(taskId)) {
                            taskResult = result;
                            break;
                        }
                    }
                    if (taskResult == null) {
                        throw new DatabaseManagerException("Failed to load result for task " + taskId +
                            " (job: " + jobId + ")");
                    } else {
                        resultsMap.put(taskId, taskResult);
                    }
                }

                if (jobResult.getAllResults().size() != taskIds.size()) {
                    throw new DatabaseManagerException("Results: " + jobResult.getAllResults().size() + " " +
                        taskIds.size());
                }

                return resultsMap;
            }

        });

    }

    public JobResult loadJobResult(final JobId jobId) {
        return runWithoutTransaction(new SessionWork<JobResult>() {

            @Override
            public JobResult executeWork(Session session) {
                long id = jobId(jobId);

                JobData job = (JobData) session.get(JobData.class, id);

                if (job == null) {
                    return null;
                }

                Query query = session
                        .createQuery(
                                "select taskResult, "
                                    + "task.id, "
                                    + "task.taskName, "
                                    + "task.preciousResult from TaskResultData as taskResult left outer join taskResult.taskRuntimeData as task "
                                    + "where task.jobData = :job order by task.id, taskResult.resultTime desc")
                        .setParameter("job", job);

                return loadJobResult(session, query, job, jobId);
            }

        });
    }

    @SuppressWarnings("unchecked")
    private JobResultImpl loadJobResult(Session session, Query query, JobData job, JobId jobId) {
        JobResultImpl jobResult = new JobResultImpl();
        jobResult.setJobInfo(job.createJobInfo(jobId));

        DBTaskId currentTaskId = null;

        List<Object[]> resultList = (List<Object[]>) query.list();
        if (resultList.isEmpty()) {
            return jobResult;
        }

        String[] jobClasspath = job.getClasspath();
        int counter = 0;

        for (Object[] result : resultList) {
            TaskResultData resultData = (TaskResultData) result[0];
            DBTaskId dbTaskId = (DBTaskId) result[1];
            String taskName = (String) result[2];
            Boolean preciousResult = (Boolean) result[3];

            boolean nextTask = !dbTaskId.equals(currentTaskId);
            if (nextTask) {
                TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName, dbTaskId.getTaskId(), false);
                jobResult.addTaskResult(taskName, resultData.toTaskResult(taskId, jobClasspath),
                        preciousResult);
                currentTaskId = dbTaskId;
            }

            if (++counter % 100 == 0) {
                session.clear();
            }
        }

        return jobResult;
    }

    public TaskResult loadLastTaskResult(final TaskId taskId) {
        return loadTaskResult(taskId, 0);
    }

    public TaskResult loadTaskResult(final JobId jobId, final String taskName, final int index) {
        return runWithoutTransaction(new SessionWork<TaskResult>() {

            @Override
            public TaskResult executeWork(Session session) {
                long id = jobId(jobId);

                Object[] taskSearchResult = (Object[]) session.createQuery(
                        "select id, taskName from TaskData where "
                            + "taskName = :taskName and jobData = :job").setParameter("taskName", taskName)
                        .setParameter("job", session.load(JobData.class, id)).uniqueResult();

                if (taskSearchResult == null) {
                    throw new DatabaseManagerException("Failed to load result for task '" + taskName +
                        ", job: " + jobId);
                }

                DBTaskId dbTaskId = (DBTaskId) taskSearchResult[0];
                String taskName = (String) taskSearchResult[1];
                TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName, dbTaskId.getTaskId(), false);

                return loadTaskResult(session, taskId, index);
            }

        });
    }

    public TaskResult loadTaskResult(final TaskId taskId, final int index) {
        return runWithoutTransaction(new SessionWork<TaskResult>() {
            @Override
            public TaskResult executeWork(Session session) {
                return loadTaskResult(session, taskId, index);
            }

        });
    }

    @SuppressWarnings("unchecked")
    private TaskResult loadTaskResult(Session session, TaskId taskId, int resultIndex) {
        DBTaskId dbTaskId = taskId(taskId);

        TaskData task = (TaskData) session.load(TaskData.class, dbTaskId);
        Query query = session
                .createQuery(
                        "from TaskResultData result where result.taskRuntimeData = :task order by result.resultTime desc")
                .setParameter("task", task);

        query.setMaxResults(1);
        query.setFirstResult(resultIndex);
        List<TaskResultData> results = (List<TaskResultData>) query.list();
        if (results.isEmpty()) {
            return null;
        } else {
            String[] classpath = (String[]) session.createQuery(
                    "select job.classpath from JobData job where job.id =:jobId").setParameter("jobId",
                    jobId(taskId.getJobId())).uniqueResult();

            return results.get(0).toTaskResult(taskId, classpath);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveClasspathContentIfNeeded(Session session, JobEnvironment jobEnv) {
        if (jobEnv != null && jobEnv.getJobClasspath() != null) {
            List<Long> existing = session.createQuery(
                    "select crc from JobClasspathContent as ce where ce.crc = :crc").setLong("crc",
                    jobEnv.getJobClasspathCRC()).list();
            if (existing.isEmpty()) {
                JobClasspathContent classpathEntry = new JobClasspathContent();
                classpathEntry.setClasspathContent(jobEnv.getJobClasspathContent());
                classpathEntry.setCrc(jobEnv.getJobClasspathCRC());
                classpathEntry.setContainsJarFiles(jobEnv.containsJarFile());
                try {
                    session.save(classpathEntry);
                } catch (ConstraintViolationException e) {
                    debugLogger.warn("Failed to save classpath entry", e);
                }
            }
        }
    }

    public synchronized void newJobSubmitted(final InternalJob job) {
        runWithTransaction(new SessionWork<JobData>() {

            @Override
            public JobData executeWork(Session session) {
                JobEnvironment jobEnv = job.getEnvironment();
                saveClasspathContentIfNeeded(session, jobEnv);

                JobData jobRuntimeData = JobData.createJobData(job);
                session.save(jobRuntimeData);

                job.setId(new JobIdImpl(jobRuntimeData.getId(), job.getName()));

                List<InternalTask> tasksWithNewIds = new ArrayList<InternalTask>();
                for (int i = 0; i < job.getITasks().size(); i++) {
                    InternalTask task = job.getITasks().get(i);
                    task.setId(TaskIdImpl.createTaskId(job.getId(), task.getTaskInfo().getTaskId()
                            .getReadableName(), i, true));
                    tasksWithNewIds.add(task);
                }
                job.getIHMTasks().clear();
                for (InternalTask task : tasksWithNewIds) {
                    job.getIHMTasks().put(task.getId(), task);
                }

                List<InternalTask> tasks = job.getITasks();
                List<TaskData> taskRuntimeDataList = new ArrayList<TaskData>(tasks.size());
                for (InternalTask task : tasks) {
                    taskRuntimeDataList.add(saveNewTask(session, jobRuntimeData, task));
                }
                saveTaskDependencies(session, tasks, taskRuntimeDataList);

                return jobRuntimeData;
            }

        });
    }

    private TaskData getTaskReference(Session session, InternalTask task) {
        return (TaskData) session.get(TaskData.class, taskId(task));
    }

    private void saveTaskDependencies(Session session, List<InternalTask> tasks,
            List<TaskData> taskRuntimeDataList) {
        for (int i = 0; i < tasks.size(); i++) {
            InternalTask task = tasks.get(i);
            TaskData taskRuntimeData = taskRuntimeDataList.get(i);
            if (task.hasDependences()) {
                List<DBTaskId> dependencies = new ArrayList<DBTaskId>(task.getDependences().size());
                for (Task dependency : task.getDependences()) {
                    dependencies.add(taskId((InternalTask) dependency));
                }
                taskRuntimeData.setDependentTasks(dependencies);
            } else {
                taskRuntimeData.setDependentTasks(Collections.<DBTaskId> emptyList());
            }
            if (task.getIfBranch() != null) {
                InternalTask ifBranch = task.getIfBranch();
                taskRuntimeData.setIfBranch(getTaskReference(session, ifBranch));
            } else {
                taskRuntimeData.setIfBranch(null);
            }
            if (task.getJoinedBranches() != null && !task.getJoinedBranches().isEmpty()) {
                List<DBTaskId> joinedBranches = new ArrayList<DBTaskId>(task.getJoinedBranches().size());
                for (InternalTask joinedBranch : task.getJoinedBranches()) {
                    joinedBranches.add(taskId(joinedBranch));
                }
                taskRuntimeData.setJoinedBranches(joinedBranches);
            } else {
                taskRuntimeData.setJoinedBranches(Collections.<DBTaskId> emptyList());
            }
        }
    }

    private TaskData saveNewTask(Session session, JobData jobRuntimeData, InternalTask task) {
        TaskData taskRuntimeData = TaskData.createTaskData(jobRuntimeData, task);
        session.save(taskRuntimeData);

        if (task.getClass().equals(InternalJavaTask.class)) {
            JavaExecutableContainer container = (JavaExecutableContainer) task.getExecutableContainer();
            JavaTaskData javaTaskData = JavaTaskData.createJavaTaskData(taskRuntimeData, container);
            session.save(javaTaskData);
        } else if (task.getClass().equals(InternalForkedJavaTask.class)) {
            ForkedJavaExecutableContainer container = (ForkedJavaExecutableContainer) task
                    .getExecutableContainer();
            ForkedJavaTaskData forkedJavaTaskData = ForkedJavaTaskData.createForkedJavaTaskData(
                    taskRuntimeData, container);
            session.save(forkedJavaTaskData);
        } else if (task.getClass().equals(InternalNativeTask.class)) {
            NativeExecutableContainer container = (NativeExecutableContainer) task.getExecutableContainer();
            NativeTaskData nativeTaskData = NativeTaskData.createNativeTaskData(taskRuntimeData, container);
            session.save(nativeTaskData);
        } else if (task.getClass().equals(InternalForkedScriptTask.class)) {
            ForkedScriptExecutableContainer container = (ForkedScriptExecutableContainer) task
                    .getExecutableContainer();
            ScriptTaskData scriptTaskData = ScriptTaskData.createScriptTaskData(taskRuntimeData, container);
            session.save(scriptTaskData);
        } else if (task.getClass().equals(InternalScriptTask.class)) {
            ScriptExecutableContainer container = (ScriptExecutableContainer) task.getExecutableContainer();
            ScriptTaskData scriptTaskData = ScriptTaskData.createScriptTaskData(taskRuntimeData, container);
            session.save(scriptTaskData);
        } else {
            throw new IllegalArgumentException("Unexpected task class: " + task.getClass());
        }

        return taskRuntimeData;
    }

    public JobClasspathContent loadJobClasspathContent(final long crc) {
        return runWithoutTransaction(new SessionWork<JobClasspathContent>() {
            @Override
            public JobClasspathContent executeWork(Session session) {
                return (JobClasspathContent) session.get(JobClasspathContent.class, crc);
            }

        });
    }

    private ExecutableContainer loadExecutableContainer(Session session, InternalTask task) {
        try {
            ExecutableContainer container = null;

            if (task.getClass().equals(InternalJavaTask.class)) {
                JavaTaskData taskData = (JavaTaskData) session.createQuery(
                        "from JavaTaskData td where td.taskData.id= :taskId").setParameter("taskId",
                        taskId(task)).uniqueResult();

                if (taskData != null) {
                    container = taskData.createExecutableContainer();
                }
            } else if (task.getClass().equals(InternalForkedJavaTask.class)) {
                ForkedJavaTaskData taskData = (ForkedJavaTaskData) session.createQuery(
                        "from ForkedJavaTaskData td where td.taskData.id = :taskId").setParameter("taskId",
                        taskId(task)).uniqueResult();

                if (taskData != null) {
                    container = taskData.createExecutableContainer();
                }
            } else if (task.getClass().equals(InternalNativeTask.class)) {
                NativeTaskData taskData = (NativeTaskData) session.createQuery(
                        "from NativeTaskData td where td.taskData.id = :taskId").setParameter("taskId",
                        taskId(task)).uniqueResult();

                if (taskData != null) {
                    container = taskData.createExecutableContainer();
                }
            } else if (task.getClass().equals(InternalScriptTask.class)) {
                ScriptTaskData taskData = queryScriptTaskData(session, task);

                if (taskData != null) {
                    container = taskData.createExecutableContainer();
                }
            } else if (task.getClass().equals(InternalForkedScriptTask.class)) {
                ScriptTaskData taskData = queryScriptTaskData(session, task);

                if (taskData != null) {
                    container = taskData.createForkedExecutableContainer();
                }
            } else {
                throw new IllegalArgumentException("Unexpected task class: " + task.getClass());
            }

            if (container == null) {
                throw new DatabaseManagerException("Failed to load data for task " + task.getId());
            }

            return container;
        } catch (Exception e) {
            throw new DatabaseManagerException(e);
        }
    }

    private ScriptTaskData queryScriptTaskData(Session session, InternalTask task) {
        return (ScriptTaskData) session.createQuery("from ScriptTaskData td where td.taskData.id = :taskId")
                .setParameter("taskId", taskId(task)).uniqueResult();
    }

    public ExecutableContainer loadExecutableContainer(final InternalTask task) {
        return runWithoutTransaction(new SessionWork<ExecutableContainer>() {
            @Override
            public ExecutableContainer executeWork(Session session) {
                return loadExecutableContainer(session, task);
            }

        });
    }

    public List<SchedulerUserInfo> loadUsersWithJobs() {
        return runWithoutTransaction(new SessionWork<List<SchedulerUserInfo>>() {
            @Override
            public List<SchedulerUserInfo> executeWork(Session session) {
                List<SchedulerUserInfo> users = new ArrayList<SchedulerUserInfo>();
                Query query = session
                        .createQuery("select owner, count(owner), max(submittedTime) from JobData group by owner");

                for (Object obj : query.list()) {
                    Object[] nameAndCount = (Object[]) obj;
                    users.add(new SchedulerUserInfo(null, nameAndCount[0].toString(), 0, Long
                            .parseLong(nameAndCount[2].toString()), Integer.parseInt(nameAndCount[1]
                            .toString())));
                }
                return users;
            }
        });
    }

    private <T> T runWithTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.runWithTransaction(sessionWork);
    }

    private <T> T runWithTransaction(SessionWork<T> sessionWork, boolean readonly) {

        return transactionHelper.runWithTransaction(sessionWork, readonly);
    }

    private <T> T runWithoutTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.runWithoutTransaction(sessionWork);
    }

    private static TaskData.DBTaskId taskId(InternalTask task) {
        return taskId(task.getId());
    }

    private static TaskData.DBTaskId taskId(TaskId taskId) {
        TaskData.DBTaskId id = new TaskData.DBTaskId();
        id.setJobId(jobId(taskId.getJobId()));
        id.setTaskId(Long.valueOf(taskId.value()));
        return id;
    }

    private static long jobId(InternalJob job) {
        return jobId(job.getId());
    }

    private static long jobId(JobId jobId) {
        return Long.valueOf(jobId.value());
    }

    private static Configuration createConfiguration(File configFile, Map<String, String> propertiesToReplace) {
        try {
            String configContent = new String(FileToBytesConverter.convertFileToByteArray(configFile));

            for (Map.Entry<String, String> property : propertiesToReplace.entrySet()) {
                configContent = configContent.replace(property.getKey(), property.getValue());
            }

            Configuration configuration;

            File modifiedFile = File.createTempFile("dbconfig", "tmp");
            try {
                FileToBytesConverter.convertByteArrayToFile(configContent.getBytes(), modifiedFile);
                configuration = new Configuration().configure(modifiedFile);
            } finally {
                modifiedFile.delete();
            }

            return configuration;
        } catch (IOException e) {
            throw new HibernateException("Failed to load Hibernate configuration", e);
        }
    }

    public void putThirdPartyCredential(final String username, final String key,
            final HybridEncryptedData encryptedCredential) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                session.saveOrUpdate(new ThirdPartyCredentialData(username, key, encryptedCredential
                        .getEncryptedSymmetricKey(), encryptedCredential.getEncryptedData()));
                return null;
            }
        });
    }

    public Set<String> thirdPartyCredentialsKeySet(final String username) {
        return runWithoutTransaction(new SessionWork<Set<String>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<String> executeWork(Session session) {
                Query query = session.createQuery(
                        "select key from ThirdPartyCredentialData where username = :username").setParameter(
                        "username", username);
                List<String> keys = query.list();
                return new HashSet<String>(keys);

            }
        });
    }

    public void removeThirdPartyCredential(final String username, final String key) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            public Void executeWork(Session session) {
                Query query = session.createQuery(
                        "delete from ThirdPartyCredentialData where username = :username and key = :key")
                        .setParameter("username", username).setParameter("key", key);
                query.executeUpdate();
                return null;
            }
        });
    }

    public Map<String, HybridEncryptedData> thirdPartyCredentialsMap(final String username) {
        return runWithoutTransaction(new SessionWork<Map<String, HybridEncryptedData>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Map<String, HybridEncryptedData> executeWork(Session session) {
                Query query = session.createQuery(
                        "select key, encryptedSymmetricKey, encryptedValue "
                            + "from ThirdPartyCredentialData " + "where username = :username").setParameter(
                        "username", username);
                List<Object[]> rows = query.list();
                Map<String, HybridEncryptedData> thirdPartyCredentialsMap = new HashMap<String, HybridEncryptedData>(
                    rows.size());
                for (Object[] row : rows) {
                    String key = (String) row[0];
                    byte[] encryptedSymmetricKey = (byte[]) row[1];
                    byte[] encryptedValue = (byte[]) row[2];
                    thirdPartyCredentialsMap.put(key, new HybridEncryptedData(encryptedSymmetricKey,
                        encryptedValue));
                }
                return thirdPartyCredentialsMap;

            }
        });
    }
}
