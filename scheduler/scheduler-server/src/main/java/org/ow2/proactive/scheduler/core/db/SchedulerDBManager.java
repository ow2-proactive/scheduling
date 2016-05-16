package org.ow2.proactive.scheduler.core.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.db.TransactionHelper;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
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
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalForkedScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.utils.FileToBytesConverter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.transform.DistinctRootEntityResultTransformer;


public class SchedulerDBManager {

    private static final String JAVA_PROPERTYNAME_NODB = "scheduler.database.nodb";

    private static final int RECOVERY_LOAD_JOBS_BATCH_SIZE = 100;

    private static final Logger logger = Logger.getLogger(SchedulerDBManager.class);

    protected static final Set<JobStatus> FINISHED_JOB_STATUSES = ImmutableSet.of(JobStatus.CANCELED,
            JobStatus.FAILED, JobStatus.KILLED, JobStatus.FINISHED);

    protected static final Set<JobStatus> PENDING_JOB_STATUSES = ImmutableSet.of(JobStatus.PENDING);

    protected static final Set<JobStatus> RUNNING_JOB_STATUSES = ImmutableSet.of(JobStatus.PAUSED,
            JobStatus.IN_ERROR, JobStatus.STALLED, JobStatus.RUNNING);

    protected static final Set<JobStatus> NOT_FINISHED_JOB_STATUSES = ImmutableSet
            .copyOf(Iterables.concat(RUNNING_JOB_STATUSES, PENDING_JOB_STATUSES));

    protected static final Set<TaskStatus> PENDING_TASKS = ImmutableSet.of(TaskStatus.SUBMITTED,
            TaskStatus.PENDING, TaskStatus.NOT_STARTED);

    protected static final Set<TaskStatus> RUNNING_TASKS = ImmutableSet.of(TaskStatus.PAUSED,
            TaskStatus.IN_ERROR, TaskStatus.RUNNING, TaskStatus.WAITING_ON_ERROR,
            TaskStatus.WAITING_ON_FAILURE);

    protected static final Set<TaskStatus> FINISHED_TASKS = ImmutableSet.of(TaskStatus.FAILED,
            TaskStatus.NOT_RESTARTED, TaskStatus.ABORTED, TaskStatus.FAULTY, TaskStatus.FINISHED,
            TaskStatus.SKIPPED);

    private final SessionFactory sessionFactory;

    private final TransactionHelper transactionHelper;

    public static SchedulerDBManager createUsingProperties() {
        if (System.getProperty(JAVA_PROPERTYNAME_NODB) != null) {
            return createInMemorySchedulerDBManager();
        } else {
            File configFile = new File(PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getValueAsString()));

            Map<String, String> propertiesToReplace = new HashMap<>(2, 1f);
            propertiesToReplace.put("${proactive.home}",
                    CentralPAPropertyRepository.PA_HOME.getValue());
            propertiesToReplace.put("${pa.scheduler.home}",
                    PASchedulerProperties.SCHEDULER_HOME.getValueAsString());

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
        config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver");
        config.setProperty("hibernate.connection.url",
                "jdbc:hsqldb:mem:" + System.currentTimeMillis() + ";hsqldb.tx=mvcc");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        return new SchedulerDBManager(config, true);
    }

    public SchedulerDBManager(Configuration configuration, boolean drop) {
        try {
            configuration.addAnnotatedClass(JobData.class);
            configuration.addAnnotatedClass(TaskData.class);
            configuration.addAnnotatedClass(TaskResultData.class);
            configuration.addAnnotatedClass(ScriptData.class);
            configuration.addAnnotatedClass(SelectionScriptData.class);
            configuration.addAnnotatedClass(EnvironmentModifierData.class);
            configuration.addAnnotatedClass(SelectorData.class);
            configuration.addAnnotatedClass(ThirdPartyCredentialData.class);
            if (drop) {
                configuration.setProperty("hibernate.hbm2ddl.auto", "create");
            }

            configuration.setProperty("hibernate.id.new_generator_mappings", "true");
            configuration.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
            configuration.setProperty("hibernate.connection.isolation", "2");

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            transactionHelper = new TransactionHelper(sessionFactory);
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed", ex);
        }
    }

    public Page<JobInfo> getJobs(final int offset, final int limit, final String user, final boolean pending,
            final boolean running, final boolean finished,
            final List<SortParameter<JobSortParameter>> sortParameters) {

        if (!pending && !running && !finished) {
            return new Page<>(new ArrayList<JobInfo>(0), 0);
        }

        DBJobDataParameters params = new DBJobDataParameters(offset, limit, user, pending, running, finished,
                sortParameters);
        int totalNbJobs = getTotalNumberOfJobs(params);
        final Set<JobStatus> jobStatuses = params.getStatuses();
        List<JobInfo> lJobs = executeReadOnlyTransaction(new SessionWork<List<JobInfo>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<JobInfo> doInTransaction(Session session) {
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
                boolean allJobs = pending && running && finished;
                if (!allJobs) {
                    criteria.add(Restrictions.in("status", jobStatuses));
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
                                throw new IllegalArgumentException(
                                        "Unsupported sort paramter: " + param.getParameter());
                        }
                        criteria.addOrder(sortOrder);
                    }
                }

                List<JobData> jobsList = criteria.list();
                List<JobInfo> result = new ArrayList<>(jobsList.size());
                for (JobData jobData : jobsList) {
                    JobInfo jobInfo = jobData.toJobInfo();
                    result.add(jobInfo);
                }

                return result;
            }

        });

        return new Page(lJobs, totalNbJobs);
    }

    public Page<TaskState> getTaskStates(final long from, final long to, final String tag, final int offset,
            final int limit, final String user, final boolean pending, final boolean running,
            final boolean finished, SortSpecifierContainer sortParams) {

        DBTaskDataParameters parameters = new DBTaskDataParameters(tag, from, to, offset, limit, user,
                pending, running, finished, sortParams);
        int totalNbTasks = getTotalNumberOfTasks(parameters);
        List<TaskState> lTasks = executeReadOnlyTransaction(TaskDBUtils.taskStateSessionWork(parameters));

        return new Page<>(lTasks, totalNbTasks);
    }

    public Page<TaskInfo> getTasks(final long from, final long to, final String tag, final int offset,
            final int limit, final String user, final boolean pending, final boolean running,
            final boolean finished) {

        DBTaskDataParameters parameters = new DBTaskDataParameters(tag, from, to, offset, limit, user,
                pending, running, finished, SortSpecifierContainer.EMPTY_CONTAINER);
        int totalNbTasks = getTotalNumberOfTasks(parameters);
        List<TaskInfo> lTaskInfo = executeReadOnlyTransaction(TaskDBUtils.taskInfoSessionWork(parameters));

        return new Page<>(lTaskInfo, totalNbTasks);
    }

    private int getTotalNumberOfTasks(final DBTaskDataParameters params) {

        return executeReadOnlyTransaction(TaskDBUtils.getTotalNumberOfTasks(params));

    }

    private int getTotalNumberOfJobs(final DBJobDataParameters params) {

        return executeReadOnlyTransaction(new SessionWork<Integer>() {

            @Override
            public Integer doInTransaction(Session session) {

                Set<JobStatus> statuses = params.getStatuses();

                if (statuses.isEmpty()) {
                    return 0;
                } else {

                    boolean hasUser = params.getUser() != null && "".compareTo(params.getUser()) != 0;

                    StringBuilder queryString = new StringBuilder(
                            "select count(*) from JobData where removedTime = -1 ");

                    if (hasUser) {
                        queryString.append("and owner = :user ");
                    }

                    queryString.append("and status in (:taskStatus) ");

                    Query query = session.createQuery(queryString.toString());
                    query.setParameterList("taskStatus", statuses);
                    if (hasUser) {
                        query.setParameter("user", params.getUser());
                    }

                    Long count = (Long) query.uniqueResult();

                    return count.intValue();
                }
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
        return executeReadOnlyTransaction(new SessionWork<List<JobUsage>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<JobUsage> doInTransaction(Session session) {
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

                List<JobUsage> result = new ArrayList<>(jobsList.size());
                for (JobData jobData : jobsList) {
                    JobUsage jobUsage = jobData.toJobUsage();
                    result.add(jobUsage);
                }
                return result;
            }
        });
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void close() {
        try {
            if (sessionFactory != null) {
                logger.info("Closing session factory");
                sessionFactory.close();
            }
        } catch (Exception e) {
            logger.error("Error while closing database", e);
        }
    }

    public long getFinishedJobsCount() {
        return getJobsNumberWithStatus(FINISHED_JOB_STATUSES);
    }

    public long getPendingJobsCount() {
        return getJobsNumberWithStatus(Arrays.asList(JobStatus.PAUSED, JobStatus.PENDING));
    }

    public long getRunningJobsCount() {
        return getJobsNumberWithStatus(Arrays.asList(JobStatus.RUNNING, JobStatus.STALLED));
    }

    public long getTotalJobsCount() {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Query query = session.getNamedQuery("getTotalJobsCount");
                return (Long) query.uniqueResult();
            }

        });
    }

    private long getJobsNumberWithStatus(final Collection<JobStatus> status) {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Query query = session.getNamedQuery("getJobsNumberWithStatus")
                        .setParameterList("status", status);

                return (Long) query.uniqueResult();
            }

        });
    }

    public long getFinishedTasksCount() {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Query query = session
                        .getNamedQuery("getFinishedTasksCount")
                        .setParameterList("taskStatus",
                                Arrays.asList(TaskStatus.FINISHED, TaskStatus.FAULTY));

                return (Long) query.uniqueResult();
            }

        });
    }

    public long getPendingTasksCount() {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Collection<TaskStatus> taskStatus = Arrays.asList(TaskStatus.SUBMITTED, TaskStatus.PAUSED,
                        TaskStatus.PENDING, TaskStatus.WAITING_ON_ERROR, TaskStatus.WAITING_ON_FAILURE);
                Query query = session
                        .getNamedQuery("getPendingTasksCount")
                        .setParameterList("jobStatus", NOT_FINISHED_JOB_STATUSES)
                        .setParameterList("taskStatus", taskStatus);

                return (Long) query.uniqueResult();
            }

        });
    }

    public long getRunningTasksCount() {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Query query = session
                        .getNamedQuery("getRunningTasksCount")
                        .setParameterList("jobStatus", NOT_FINISHED_JOB_STATUSES)
                        .setParameterList("taskStatus", Arrays.asList(TaskStatus.RUNNING));

                return (Long) query.uniqueResult();
            }

        });
    }

    public long getTotalTasksCount() {
        return executeReadOnlyTransaction(new SessionWork<Long>() {

            @Override
            public Long doInTransaction(Session session) {
                Query query = session.getNamedQuery("getTotalTasksCount");
                Long count = (Long) query.uniqueResult();
                return count;
            }

        });
    }

    public double getMeanJobPendingTime() {
        return executeReadOnlyTransaction(new SessionWork<Double>() {
            @Override
            public Double doInTransaction(Session session) {
                Query query = session.getNamedQuery("getMeanJobPendingTime");
                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });
    }

    public double getMeanJobExecutionTime() {
        return executeReadOnlyTransaction(new SessionWork<Double>() {
            @Override
            public Double doInTransaction(Session session) {
                Query query = session.getNamedQuery("getMeanJobExecutionTime");
                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }
        });
    }

    public double getMeanJobSubmittingPeriod() {
        return executeReadOnlyTransaction(new SessionWork<Double>() {
            @Override
            public Double doInTransaction(Session session) {
                Query query = session
                        .getNamedQuery("getMeanJobSubmittingPeriod");
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

        Long result = executeReadOnlyTransaction(new SessionWork<Long>() {
            @Override
            public Long doInTransaction(Session session) {
                JobData jobData = session.get(JobData.class, id);
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

        return checkResult(id, result);
    }

    public long getJobPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Long result = executeReadOnlyTransaction(new SessionWork<Long>() {
            @Override
            public Long doInTransaction(Session session) {
                JobData jobData = session.get(JobData.class, id);
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

        return checkResult(id, result);
    }

    public double getMeanTaskPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Double result = executeReadOnlyTransaction(new SessionWork<Double>() {
            @Override
            public Double doInTransaction(Session session) {
                Query jobSubmittedTimeQuery = session
                        .getNamedQuery("getJobSubmittedTime")
                        .setParameter("id", id);
                Long jobSubmittedTime = (Long) jobSubmittedTimeQuery.uniqueResult();
                if (jobSubmittedTime == null) {
                    return null;
                }
                Query query = session
                        .getNamedQuery("getMeanTaskPendingTime")
                        .setParameter("id", id).setParameter("jobSubmittedTime", jobSubmittedTime);

                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });

        return checkResult(id, result);
    }

    private <T> T checkResult(long id, T result) {
        if (result == null) {
            throw new IllegalArgumentException("Job " + id + " doesn't exist");
        } else {
            return result;
        }
    }

    public double getMeanTaskRunningTime(String jobId) {
        final long id = Long.parseLong(jobId);
        Double result = executeReadOnlyTransaction(new SessionWork<Double>() {
            @Override
            public Double doInTransaction(Session session) {
                Query jobQuery = session.getNamedQuery("checkJobExistence")
                        .setParameter("id", id);
                if (jobQuery.uniqueResult() == null) {
                    return null;
                }

                Query query = session.getNamedQuery("getMeanTaskRunningTime")
                        .setParameter("id", id);

                Double result = (Double) query.uniqueResult();
                return result == null ? 0 : result;
            }

        });

        return checkResult(id, result);
    }

    public int getTotalNumberOfHostsUsed(String jobId) {
        final long id = Long.parseLong(jobId);
        int result = executeReadOnlyTransaction(new SessionWork<Integer>() {
            @Override
            public Integer doInTransaction(Session session) {
                Query jobQuery = session.getNamedQuery("checkJobExistence")
                        .setParameter("id", id);
                if (jobQuery.uniqueResult() == null) {
                    return null;
                }

                Query query = session
                        .getNamedQuery("getTotalNumberOfHostsUsed")
                        .setParameter("id", id);

                return ((Long) query.uniqueResult()).intValue();
            }

        });

        return checkResult(id, result);
    }

    public SchedulerAccount readAccount(final String username) {
        return executeReadOnlyTransaction(new SessionWork<SchedulerAccount>() {

            @Override
            public SchedulerAccount doInTransaction(Session session) {
                Query tasksQuery = session
                        .getNamedQuery("readAccountTasks")
                        .setParameter("username", username);

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

                Query jobQuery = session
                        .getNamedQuery("readAccountJobs")
                        .setParameter("username", username);

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
        session.getNamedQuery("updateTaskDataJobScripts")
                .setParameter("jobId", jobId).executeUpdate();
        session.getNamedQuery("deleteScriptData")
                .setParameter("jobId", jobId).executeUpdate();
        session.getNamedQuery("deleteSelectionScriptData")
                .setParameter("jobId", jobId).executeUpdate();
    }

    private void removeJobRuntimeData(Session session, long jobId) {
        removeJobScripts(session, jobId);

        session.getNamedQuery("deleteEnvironmentModifierData")
                .setParameter("jobId", jobId).executeUpdate();

        session.getNamedQuery("deleteSelectorData")
                .setParameter("jobId", jobId).executeUpdate();
    }

    public void removeJob(final JobId jobId, final long removedTime, final boolean removeData) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long id = jobId(jobId);

                if (removeData) {
                    session.createSQLQuery("delete from TASK_DATA_DEPENDENCIES where JOB_ID = :jobId")
                            .setParameter("jobId", id).executeUpdate();
                    session.createSQLQuery("delete from TASK_DATA_JOINED_BRANCHES where JOB_ID = :jobId")
                            .setParameter("jobId", id).executeUpdate();

                    removeJobScripts(session, id);

                    session.getNamedQuery("deleteJobData")
                            .setParameter("jobId", id)
                            .executeUpdate();
                } else {
                    session.getNamedQuery("updateJobDataRemovedTime")
                            .setParameter("removedTime", removedTime)
                            .setParameter("jobId", id)
                            .executeUpdate();
                }

                return null;
            }

        });
    }

    public List<InternalJob> loadNotFinishedJobs(boolean fullState) {
        return loadJobs(fullState, NOT_FINISHED_JOB_STATUSES, -1);
    }

    public List<InternalJob> loadFinishedJobs(boolean fullState, long period) {
        return loadJobs(fullState, FINISHED_JOB_STATUSES, period);
    }

    private List<InternalJob> loadJobs(final boolean fullState, final Collection<JobStatus> status,
            final long period) {
        return executeReadOnlyTransaction(new SessionWork<List<InternalJob>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<InternalJob> doInTransaction(Session session) {
                logger.info("Loading Jobs from database");

                Query query;
                if (period > 0) {
                    query = session
                            .getNamedQuery("loadJobsWithPeriod")
                            .setParameter("minSubmittedTime", System.currentTimeMillis() - period)
                            .setParameterList("status", status)
                            .setReadOnly(true);
                } else {
                    query = session
                            .getNamedQuery("loadJobs")
                            .setParameterList("status", status)
                    .setReadOnly(true);
                }

                List<Long> ids = query.list();

                logger.info(ids.size() + " Jobs to fetch from database");

                return loadInternalJobs(fullState, session, ids);
            }

        });
    }

    public InternalJob loadJobWithTasksIfNotRemoved(final JobId id) {
        return executeReadOnlyTransaction(new SessionWork<InternalJob>() {
            @Override
            public InternalJob doInTransaction(Session session) {
                Query jobQuery = session.getNamedQuery("loadJobDataIfNotRemoved").setReadOnly(true);

                List<InternalJob> result = new ArrayList<>();
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
        return executeReadOnlyTransaction(new SessionWork<List<InternalJob>>() {
            @Override
            public List<InternalJob> doInTransaction(Session session) {
                List<Long> ids = new ArrayList<>(jobIds.length);
                for (JobId jobId : jobIds) {
                    ids.add(jobId(jobId));
                }
                return loadInternalJobs(fullState, session, ids);
            }

        });
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<TaskData>> loadJobsTasks(Session session, List<Long> jobIds) {
        Query tasksQuery = session.getNamedQuery("loadJobsTasks")
                .setParameterList("ids", jobIds)
                .setReadOnly(true)
                .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        Map<Long, List<TaskData>> tasksMap = new HashMap<>(jobIds.size(), 1f);
        for (Long id : jobIds) {
            tasksMap.put(id, new ArrayList<TaskData>());
        }

        List<TaskData> tasks = tasksQuery.list();
        for (TaskData task : tasks) {
            tasksMap.get(task.getJobData().getId()).add(task);
        }

        return tasksMap;
    }

    // Executed in a transaction from the caller
    private List<InternalJob> loadInternalJobs(boolean fullState, Session session, List<Long> ids) {
        Query jobQuery = session.getNamedQuery("loadInternalJobs");

        List<InternalJob> result = new ArrayList<>(ids.size());

        List<Long> batchLoadIds = new ArrayList<>(RECOVERY_LOAD_JOBS_BATCH_SIZE);

        int batchIndex = 1;
        for (Long id : ids) {
            batchLoadIds.add(id);
            if (batchLoadIds.size() == RECOVERY_LOAD_JOBS_BATCH_SIZE) {
                logger.info("Loading internal Jobs, batch number " + batchIndex);
                batchLoadJobs(session, fullState, jobQuery, batchLoadIds, result);
                batchLoadIds.clear();
                session.clear();
                logger.info("Fetched " + (batchIndex * RECOVERY_LOAD_JOBS_BATCH_SIZE) + " internal Jobs");
                batchIndex++;
            }
        }

        if (!batchLoadIds.isEmpty()) {
            batchLoadJobs(session, fullState, jobQuery, batchLoadIds, result);
        }

        logger.info("All required Jobs have been fetched");

        return result;
    }

    // Executed in a transaction from the caller
    private void batchLoadJobs(Session session, boolean fullState, Query jobQuery, List<Long> ids,
            Collection<InternalJob> jobs) {
        Map<Long, List<TaskData>> tasksMap = loadJobsTasks(session, ids);

        jobQuery.setParameterList("ids", ids);
        List<JobData> jobsList = (List<JobData>) jobQuery.list();

        for (JobData jobData : jobsList) {
            InternalJob internalJob = jobData.toInternalJob();
            internalJob.setTasks(toInternalTasks(fullState, internalJob, tasksMap.get(jobData.getId())));

            jobs.add(internalJob);
        }
    }

    private Collection<InternalTask> toInternalTasks(boolean loadFullState, InternalJob internalJob,
            List<TaskData> taskRuntimeDataList) {
        Map<DBTaskId, InternalTask> tasks = new HashMap<>(taskRuntimeDataList.size());

        try {
            for (TaskData taskData : taskRuntimeDataList) {
                InternalTask internalTask = taskData.toInternalTask(internalJob);
                if (loadFullState) {
                    internalTask.setParallelEnvironment(taskData.getParallelEnvironment());
                    internalTask.setGenericInformations(taskData.getGenericInformation());
                    for (SelectionScriptData scriptData : taskData.getSelectionScripts()) {
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
                    List<InternalTask> branches = new ArrayList<>(taskData.getJoinedBranches().size());
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
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long id = jobId(jobId);
                session.getNamedQuery("updateJobDataPriority")
                        .setParameter("priority", priority)
                        .setParameter("jobId", id)
                        .executeUpdate();
                return null;
            }
        });
    }

    public void jobTaskStarted(final InternalJob job, final InternalTask task,
            final boolean taskStatusToPending) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long jobId = jobId(job);

                JobInfo jobInfo = job.getJobInfo();

                session.getNamedQuery("updateJobDataTaskStarted")
                        .setParameter("status", jobInfo.getStatus())
                        .setParameter("startTime", jobInfo.getStartTime())
                        .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                        .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                        .setParameter("jobId", jobId).executeUpdate();

                if (taskStatusToPending) {
                    JobData job = session.load(JobData.class, jobId);
                    session.getNamedQuery("updateTaskDataStatusToPending")
                            .setParameter("taskStatus", TaskStatus.PENDING)
                            .setParameter("job", job).executeUpdate();
                }

                TaskData.DBTaskId taskId = taskId(task);

                TaskInfo taskInfo = task.getTaskInfo();

                session.getNamedQuery("updateTaskDataTaskStarted").setParameter("taskStatus", taskInfo.getStatus())
                        .setParameter("startTime", taskInfo.getStartTime())
                        .setParameter("finishedTime", taskInfo.getFinishedTime())
                        .setParameter("executionHostName", taskInfo.getExecutionHostName())
                        .setParameter("taskId", taskId).executeUpdate();

                return null;
            }

        });
    }

    public void taskRestarted(final InternalJob job, final InternalTask task, final TaskResultImpl result) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long jobId = jobId(job);

                JobInfo jobInfo = job.getJobInfo();

                session.getNamedQuery("updateJobDataTaskRestarted").setParameter("status", jobInfo.getStatus())
                        .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                        .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                        .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                        .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                        .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                        .setParameter("jobId", jobId).executeUpdate();

                TaskData.DBTaskId taskId = taskId(task);

                TaskInfo taskInfo = task.getTaskInfo();

                session.getNamedQuery("updateTaskDataTaskRestarted").setParameter("taskStatus", taskInfo.getStatus())
                        .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                        .setParameter("numberOfExecutionOnFailureLeft",
                                taskInfo.getNumberOfExecutionOnFailureLeft())
                        .setParameter("taskId", taskId).executeUpdate();

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
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long jobId = jobId(job);

                JobInfo jobInfo = job.getJobInfo();

                session.getNamedQuery("updateJobDataAfterWorkflowTaskFinished").setParameter("status", jobInfo.getStatus())
                        .setParameter("finishedTime", jobInfo.getFinishedTime())
                        .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                        .setParameter("numberOfFinishedTasks", jobInfo.getNumberOfFinishedTasks())
                        .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                        .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                        .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                        .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                        .setParameter("totalNumberOfTasks", jobInfo.getTotalNumberOfTasks())
                        .setParameter("jobId", jobId).executeUpdate();

                JobData jobRuntimeData = session.load(JobData.class, jobId);

                List<DBTaskId> taskIds = new ArrayList<>(
                        changesInfo.getSkippedTasks().size() + changesInfo.getUpdatedTasks().size());
                for (TaskId id : changesInfo.getSkippedTasks()) {
                    taskIds.add(taskId(id));
                }
                for (TaskId id : changesInfo.getUpdatedTasks()) {
                    taskIds.add(taskId(id));
                }

                Query tasksQuery = session.getNamedQuery("findTaskData").setParameterList("ids", taskIds);
                List<TaskData> tasksToUpdate = tasksQuery.list();
                Set<TaskId> newTasks = changesInfo.getNewTasks();

                int newListSize = tasksToUpdate.size() + newTasks.size();
                List<TaskData> taskRuntimeDataList = new ArrayList<>(newListSize);
                List<InternalTask> tasks = new ArrayList<>(newListSize);

                for (TaskData taskData : tasksToUpdate) {
                    InternalTask task = job.getIHMTasks().get(taskData.createTaskId(job));
                    taskData.updateMutableAttributes(task);
                    session.update(taskData);
                    taskRuntimeDataList.add(taskData);
                    tasks.add(task);
                }

                int counter = 0;
                for (TaskId newTaskId : newTasks) {
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

                if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
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
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {

                for (TaskState task : job.getTasks()) {
                    updateTaskData(task, session);
                }

                JobInfo jobInfo = job.getJobInfo();

                session.getNamedQuery("updateJobAndTasksState").setParameter("status", jobInfo.getStatus())
                        .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                        .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                        .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                        .setParameter("inErrorTime", jobInfo.getInErrorTime())
                        .setParameter("jobId", jobId(job)).executeUpdate();

                return null;
            }

        });
    }

    public void updateTaskState(final TaskState task) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                updateTaskData(task, session);

                return null;
            }

        });
    }

    private int updateTaskData(final TaskState task, Session session) {

        Query taskUpdateQuery = session.getNamedQuery("updateTaskData");

        TaskInfo taskInfo = task.getTaskInfo();

        return taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus())
                .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                .setParameter("numberOfExecutionOnFailureLeft", taskInfo.getNumberOfExecutionOnFailureLeft())
                .setParameter("inErrorTime", taskInfo.getInErrorTime())
                .setParameter("taskId", taskId(task.getId())).executeUpdate();
    }

    public void updateStartTime(long jobId, long taskId, long newStartTime) {
        updateStartOrEndOrScheduledTime(jobId, taskId, "startTime", newStartTime);
    }

    public void updateFinishedTime(long jobId, long taskId, long newFinishedTime) {
        updateStartOrEndOrScheduledTime(jobId, taskId, "finishedTime", newFinishedTime);
    }

    public void updateScheduledTime(long jobId, long taskId, long newScheduledTime) {
        updateStartOrEndOrScheduledTime(jobId, taskId, "scheduledTime", newScheduledTime);
    }

    private void updateStartOrEndOrScheduledTime(final long jobId, final long taskId, final String fieldName,
            final long time) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {

                Query query = session
                        .createQuery("update TaskData task set task." + fieldName + " = :newTime " + //NOSONAR
                                "where task.id.jobId = :jobId and task.id.taskId= :taskId")
                        .setParameter("newTime", time).setParameter("jobId", jobId)
                        .setParameter("taskId", taskId);

                query.executeUpdate();

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
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long jobId = jobId(job);

                JobInfo jobInfo = job.getJobInfo();

                session.getNamedQuery("updateJobDataAfterTaskFinished")
                        .setParameter("status", jobInfo.getStatus())
                        .setParameter("finishedTime", jobInfo.getFinishedTime())
                        .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                        .setParameter("numberOfFinishedTasks", jobInfo.getNumberOfFinishedTasks())
                        .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                        .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                        .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                        .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                        .setParameter("jobId", jobId)
                        .executeUpdate();

                Query taskUpdateQuery = session.getNamedQuery("updateTaskDataAfterJobFinished");

                if (finishedTask != null) {
                    tasksToUpdate.add(finishedTask.getId());
                }

                for (TaskId id : tasksToUpdate) {
                    InternalTask task = job.getIHMTasks().get(id);
                    TaskData.DBTaskId taskId = taskId(task.getId());

                    TaskInfo taskInfo = task.getTaskInfo();

                    taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus())
                            .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                            .setParameter("numberOfExecutionOnFailureLeft",
                                    taskInfo.getNumberOfExecutionOnFailureLeft())
                            .setParameter("finishedTime", taskInfo.getFinishedTime())
                            .setParameter("executionDuration", taskInfo.getExecutionDuration())
                            .setParameter("taskId", taskId).executeUpdate();
                }

                if (result != null) {
                    TaskData.DBTaskId taskId = taskId(finishedTask.getId());
                    saveTaskResult(taskId, result, session);
                }

                if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
                    session.flush();
                    session.clear();

                    removeJobRuntimeData(session, jobId);
                }

                return null;
            }

        });
    }

    private TaskResultData saveTaskResult(TaskData.DBTaskId taskId, TaskResultImpl result, Session session) {
        TaskData taskRuntimeData = session.load(TaskData.class, taskId);

        TaskResultData resultData = TaskResultData.createTaskResultData(taskRuntimeData, result);
        session.save(resultData);

        return resultData;
    }

    public void jobSetToBeRemoved(final JobId jobId) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long id = jobId(jobId);

                session.getNamedQuery("updateJobDataSetJobToBeRemoved")
                        .setParameter("toBeRemoved", true)
                        .setParameter("jobId", id)
                        .executeUpdate();

                return null;
            }
        });
    }

    public Map<TaskId, TaskResult> loadTasksResults(final JobId jobId, final List<TaskId> taskIds) {
        if (taskIds.isEmpty()) {
            throw new IllegalArgumentException("TaskIds list is empty");
        }

        return executeReadOnlyTransaction(new SessionWork<Map<TaskId, TaskResult>>() {

            @Override
            public Map<TaskId, TaskResult> doInTransaction(Session session) {
                JobData job = session.get(JobData.class, jobId(jobId));

                if (job == null) {
                    throw new DatabaseManagerException("Invalid job id: " + jobId);
                }

                List<TaskData.DBTaskId> dbTaskIds = new ArrayList<>(taskIds.size());
                for (TaskId taskId : taskIds) {
                    dbTaskIds.add(taskId(taskId));
                }

                Query query = session.getNamedQuery("loadTasksResults")
                        .setParameterList("tasksIds", dbTaskIds);

                JobResultImpl jobResult = loadJobResult(session, query, job, jobId);
                if (jobResult == null) {
                    throw new DatabaseManagerException(
                            "Failed to load result for tasks " + taskIds + " (job: " + jobId + ")");
                }

                Map<TaskId, TaskResult> resultsMap = new HashMap<>(taskIds.size());
                for (TaskId taskId : taskIds) {
                    TaskResult taskResult = null;
                    for (TaskResult result : jobResult.getAllResults().values()) {
                        if (result.getTaskId().equals(taskId)) {
                            taskResult = result;
                            break;
                        }
                    }
                    if (taskResult == null) {
                        throw new DatabaseManagerException(
                                "Failed to load result for task " + taskId + " (job: " + jobId + ")");
                    } else {
                        resultsMap.put(taskId, taskResult);
                    }
                }

                if (jobResult.getAllResults().size() != taskIds.size()) {
                    throw new DatabaseManagerException(
                            "Results: " + jobResult.getAllResults().size() + " " + taskIds.size());
                }

                return resultsMap;
            }

        });

    }

    public JobResult loadJobResult(final JobId jobId) {
        return executeReadOnlyTransaction(new SessionWork<JobResult>() {

            @Override
            public JobResult doInTransaction(Session session) {
                long id = jobId(jobId);

                JobData job = session.get(JobData.class, id);

                if (job == null) {
                    return null;
                }

                Query query = session.getNamedQuery("loadJobResult")
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

        int counter = 0;

        for (Object[] result : resultList) {
            TaskResultData resultData = (TaskResultData) result[0];
            DBTaskId dbTaskId = (DBTaskId) result[1];
            String taskName = (String) result[2];
            Boolean preciousResult = (Boolean) result[3];

            boolean nextTask = !dbTaskId.equals(currentTaskId);
            if (nextTask) {
                TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName, dbTaskId.getTaskId());
                jobResult.addTaskResult(taskName, resultData.toTaskResult(taskId), preciousResult);
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
        return executeReadOnlyTransaction(new SessionWork<TaskResult>() {

            @Override
            public TaskResult doInTransaction(Session session) {
                long id = jobId(jobId);

                Object[] taskSearchResult = (Object[]) session
                        .getNamedQuery("loadTasksResultByJobAndTaskName")
                        .setParameter("taskName", taskName)
                        .setParameter("job", session.load(JobData.class, id)).uniqueResult();

                if (taskSearchResult == null) {
                    throw new DatabaseManagerException(
                            "Failed to load result for task '" + taskName + ", job: " + jobId);
                }

                DBTaskId dbTaskId = (DBTaskId) taskSearchResult[0];
                String taskName = (String) taskSearchResult[1];
                TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName, dbTaskId.getTaskId());

                return loadTaskResult(session, taskId, index);
            }

        });
    }

    public TaskResult loadTaskResult(final TaskId taskId, final int index) {
        return executeReadOnlyTransaction(new SessionWork<TaskResult>() {
            @Override
            public TaskResult doInTransaction(Session session) {
                return loadTaskResult(session, taskId, index);
            }

        });
    }

    @SuppressWarnings("unchecked")
    private TaskResult loadTaskResult(Session session, TaskId taskId, int resultIndex) {
        DBTaskId dbTaskId = taskId(taskId);

        TaskData task = session.load(TaskData.class, dbTaskId);
        Query query = session.getNamedQuery("loadTasksResultByTask")
                .setParameter("task", task);

        query.setMaxResults(1);
        query.setFirstResult(resultIndex);
        List<TaskResultData> results = (List<TaskResultData>) query.list();
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0).toTaskResult(taskId);
        }
    }

    public void newJobSubmitted(final InternalJob job) {
        executeReadWriteTransaction(new SessionWork<JobData>() {

            @Override
            public JobData doInTransaction(Session session) {
                JobData jobRuntimeData = JobData.createJobData(job);
                session.save(jobRuntimeData);

                job.setId(new JobIdImpl(jobRuntimeData.getId(), job.getName()));

                ArrayList<InternalTask> iTasks = job.getITasks();
                List<InternalTask> tasksWithNewIds = new ArrayList<>(iTasks.size());

                for (int i = 0; i < iTasks.size(); i++) {
                    InternalTask task = iTasks.get(i);
                    task.setId(TaskIdImpl.createTaskId(job.getId(),
                            task.getTaskInfo().getTaskId().getReadableName(), i));

                    tasksWithNewIds.add(task);
                }

                job.getIHMTasks().clear();

                for (InternalTask task : tasksWithNewIds) {
                    job.getIHMTasks().put(task.getId(), task);
                }

                List<InternalTask> tasks = job.getITasks();
                List<TaskData> taskRuntimeDataList = new ArrayList<>(tasks.size());
                for (InternalTask task : tasks) {
                    taskRuntimeDataList.add(saveNewTask(session, jobRuntimeData, task));
                }
                saveTaskDependencies(session, tasks, taskRuntimeDataList);

                return jobRuntimeData;
            }

        });
    }

    private TaskData getTaskReference(Session session, InternalTask task) {
        return session.get(TaskData.class, taskId(task));
    }

    private void saveTaskDependencies(Session session, List<InternalTask> tasks,
            List<TaskData> taskRuntimeDataList) {
        for (int i = 0; i < tasks.size(); i++) {
            InternalTask task = tasks.get(i);
            TaskData taskRuntimeData = taskRuntimeDataList.get(i);
            if (task.hasDependences()) {
                List<DBTaskId> dependencies = new ArrayList<>(task.getDependences().size());
                for (Task dependency : task.getDependences()) {
                    dependencies.add(taskId((InternalTask) dependency));
                }
                taskRuntimeData.setDependentTasks(dependencies);
            } else {
                taskRuntimeData.setDependentTasks(Collections.<DBTaskId>emptyList());
            }
            if (task.getIfBranch() != null) {
                InternalTask ifBranch = task.getIfBranch();
                taskRuntimeData.setIfBranch(getTaskReference(session, ifBranch));
            } else {
                taskRuntimeData.setIfBranch(null);
            }
            if (task.getJoinedBranches() != null && !task.getJoinedBranches().isEmpty()) {
                List<DBTaskId> joinedBranches = new ArrayList<>(task.getJoinedBranches().size());
                for (InternalTask joinedBranch : task.getJoinedBranches()) {
                    joinedBranches.add(taskId(joinedBranch));
                }
                taskRuntimeData.setJoinedBranches(joinedBranches);
            } else {
                taskRuntimeData.setJoinedBranches(Collections.<DBTaskId>emptyList());
            }
        }
    }

    private TaskData saveNewTask(Session session, JobData jobRuntimeData, InternalTask task) {
        // TODO: use double dispatch to prevent branching
        if (isScriptTask(task)) {
            TaskData taskRuntimeData = TaskData.createTaskData(jobRuntimeData, (InternalScriptTask) task);
            session.save(taskRuntimeData);
            return taskRuntimeData;
        } else {
            throw new IllegalArgumentException("Unexpected task class: " + task.getClass());
        }
    }

    private ExecutableContainer loadExecutableContainer(Session session, InternalTask task) {
        try {
            ExecutableContainer container = null;

            if (isScriptTask(task)) {
                TaskData taskData = queryScriptTaskData(session, task);

                if (taskData != null) {
                    container = taskData.createExecutableContainer();
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

    private boolean isScriptTask(InternalTask task) {
        return task.getClass().equals(InternalForkedScriptTask.class) ||
                task.getClass().equals(InternalScriptTask.class);
    }

    private TaskData queryScriptTaskData(Session session, InternalTask task) {
        return (TaskData) session.getNamedQuery("findTaskDataById")
                .setParameter("taskId", taskId(task)).uniqueResult();
    }

    public ExecutableContainer loadExecutableContainer(final InternalTask task) {
        return executeReadOnlyTransaction(new SessionWork<ExecutableContainer>() {
            @Override
            public ExecutableContainer doInTransaction(Session session) {
                return loadExecutableContainer(session, task);
            }
        });
    }

    public List<SchedulerUserInfo> loadUsersWithJobs() {
        return executeReadOnlyTransaction(new SessionWork<List<SchedulerUserInfo>>() {
            @Override
            public List<SchedulerUserInfo> doInTransaction(Session session) {
                Query query = session.getNamedQuery("findUsersWithJobs");

                List list = query.list();
                List<SchedulerUserInfo> users = new ArrayList<>(list.size());
                for (Object obj : list) {
                    Object[] nameAndCount = (Object[]) obj;
                    users.add(new SchedulerUserInfo(null, nameAndCount[0].toString(), 0,
                            Long.parseLong(nameAndCount[2].toString()),
                            Integer.parseInt(nameAndCount[1].toString())));
                }
                return users;
            }
        });
    }

    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.executeReadWriteTransaction(sessionWork);
    }

    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork, boolean readOnlyEntities) {
        return transactionHelper.executeReadWriteTransaction(sessionWork, readOnlyEntities);
    }

    public <T> T executeReadOnlyTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.executeReadOnlyTransaction(sessionWork);
    }

    private static TaskData.DBTaskId taskId(InternalTask task) {
        return taskId(task.getId());
    }

    private static TaskData.DBTaskId taskId(TaskId taskId) {
        TaskData.DBTaskId id = new TaskData.DBTaskId();
        id.setJobId(jobId(taskId.getJobId()));
        id.setTaskId(taskId.longValue());
        return id;
    }

    private static long jobId(InternalJob job) {
        return jobId(job.getId());
    }

    private static long jobId(JobId jobId) {
        return jobId.longValue();
    }

    private static Configuration createConfiguration(File configFile,
            Map<String, String> propertiesToReplace) {
        try {
            String configContent = new String(FileToBytesConverter.convertFileToByteArray(configFile));

            for (Map.Entry<String, String> property : propertiesToReplace.entrySet()) {
                configContent = configContent.replace(property.getKey(), property.getValue());
            }

            Configuration configuration = new Configuration();

            File modifiedFile = File.createTempFile("dbconfig", "tmp");
            try {
                FileToBytesConverter.convertByteArrayToFile(configContent.getBytes(), modifiedFile);

                if (configFile.getName().endsWith(".xml")) {
                    configuration.configure(configFile);
                } else {
                    try {
                        Properties properties = new Properties();
                        properties
                                .load(Files.newBufferedReader(configFile.toPath(), Charset.defaultCharset()));
                        configuration.addProperties(properties);
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
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
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.saveOrUpdate(new ThirdPartyCredentialData(username, key,
                        encryptedCredential.getEncryptedSymmetricKey(),
                        encryptedCredential.getEncryptedData()));
                return null;
            }
        });
    }

    public Set<String> thirdPartyCredentialsKeySet(final String username) {
        return executeReadOnlyTransaction(new SessionWork<Set<String>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<String> doInTransaction(Session session) {
                Query query = session
                        .getNamedQuery("findThirdPartyCredentialsKeySetByUsername")
                        .setParameter("username", username);
                List<String> keys = query.list();
                return new HashSet<>(keys);

            }
        });
    }

    public void removeThirdPartyCredential(final String username, final String key) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                Query query = session.getNamedQuery("deleteThirdPartyCredentialsKeySetByUsernameAndKey")
                        .setParameter("username", username).setParameter("key", key);
                query.executeUpdate();
                return null;
            }
        });
    }

    public Map<String, HybridEncryptedData> thirdPartyCredentialsMap(final String username) {
        return executeReadOnlyTransaction(new SessionWork<Map<String, HybridEncryptedData>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Map<String, HybridEncryptedData> doInTransaction(Session session) {
                Query query = session.getNamedQuery("findThirdPartyCredentialsMapByUsername")
                        .setParameter("username", username);
                List<Object[]> rows = query.list();
                Map<String, HybridEncryptedData> thirdPartyCredentialsMap = new HashMap<>(rows.size());
                for (Object[] row : rows) {
                    String key = (String) row[0];
                    byte[] encryptedSymmetricKey = (byte[]) row[1];
                    byte[] encryptedValue = (byte[]) row[2];
                    thirdPartyCredentialsMap.put(key,
                            new HybridEncryptedData(encryptedSymmetricKey, encryptedValue));
                }
                return thirdPartyCredentialsMap;

            }
        });
    }

    public boolean hasThirdPartyCredentials(final String jobOwner) {
        return executeReadOnlyTransaction(new SessionWork<Boolean>() {
            @Override
            public Boolean doInTransaction(Session session) {
                Long count = (Long) session
                        .getNamedQuery("hasThirdPartyCredentials")
                        .setParameter("username", jobOwner).uniqueResult();

                return count > 0;
            }
        });
    }

}
