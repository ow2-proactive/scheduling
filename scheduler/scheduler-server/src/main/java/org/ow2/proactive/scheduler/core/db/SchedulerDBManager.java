/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core.db;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.hibernate.type.StandardBasicTypes;
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
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
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
import org.ow2.proactive.utils.ObjectByteConverter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;


@SuppressWarnings("JpaQueryApiInspection")
public class SchedulerDBManager {

    public static final String JAVA_PROPERTYNAME_NODB = "scheduler.database.nodb";

    private static final int RECOVERY_LOAD_JOBS_BATCH_SIZE = PASchedulerProperties.SCHEDULER_DB_RECOVERY_LOAD_JOBS_BATCH_SIZE.getValueAsInt();

    private static final Logger logger = Logger.getLogger(SchedulerDBManager.class);

    public static final Set<JobStatus> FINISHED_JOB_STATUSES = ImmutableSet.of(JobStatus.CANCELED,
                                                                               JobStatus.FAILED,
                                                                               JobStatus.KILLED,
                                                                               JobStatus.FINISHED);

    public static final Set<JobStatus> PENDING_JOB_STATUSES = ImmutableSet.of(JobStatus.PENDING);

    public static final Set<JobStatus> RUNNING_JOB_STATUSES = ImmutableSet.of(JobStatus.PAUSED,
                                                                              JobStatus.IN_ERROR,
                                                                              JobStatus.STALLED,
                                                                              JobStatus.RUNNING);

    public static final Set<JobStatus> NOT_FINISHED_JOB_STATUSES = ImmutableSet.copyOf(Iterables.concat(RUNNING_JOB_STATUSES,
                                                                                                        PENDING_JOB_STATUSES));

    public static final String ALL_REQUIRED_JOBS_HAVE_BEEN_FETCHED = "All required Jobs have been fetched"; // important for JobRecoveryTest

    private final SessionFactory sessionFactory;

    private final TransactionHelper transactionHelper;

    public static SchedulerDBManager createUsingProperties() {
        if (System.getProperty(JAVA_PROPERTYNAME_NODB) != null) {
            return createInMemorySchedulerDBManager();
        } else {
            File configFile = new File(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getValueAsString()));

            Map<String, String> propertiesToReplace = new HashMap<>(2, 1f);
            propertiesToReplace.put("${proactive.home}", CentralPAPropertyRepository.PA_HOME.getValue());
            propertiesToReplace.put("${pa.scheduler.home}", PASchedulerProperties.SCHEDULER_HOME.getValueAsString());

            Configuration configuration = createConfiguration(configFile, propertiesToReplace);

            boolean drop = PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();

            if (logger.isInfoEnabled()) {
                logger.info("Starting Scheduler DB Manager " + "with drop DB = " + drop + " and configuration file = " +
                            configFile.getAbsolutePath());
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
            configuration.addAnnotatedClass(JobContent.class);
            configuration.addAnnotatedClass(JobDataVariable.class);
            configuration.addAnnotatedClass(TaskData.class);
            configuration.addAnnotatedClass(TaskDataVariable.class);
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

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties())
                                                                                  .build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            transactionHelper = new TransactionHelper(sessionFactory);

        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed", ex);
        }
    }

    public Page<JobInfo> getJobs(final int offset, final int limit, final String user, final boolean pending,
            final boolean running, final boolean finished, final List<SortParameter<JobSortParameter>> sortParameters) {

        if (!pending && !running && !finished) {
            return new Page<>(new ArrayList<JobInfo>(0), 0);
        }

        DBJobDataParameters params = new DBJobDataParameters(offset,
                                                             limit,
                                                             user,
                                                             pending,
                                                             running,
                                                             finished,
                                                             sortParameters);
        int totalNbJobs = getTotalNumberOfJobs(params);
        final Set<JobStatus> jobStatuses = params.getStatuses();
        List<JobInfo> lJobs = executeReadOnlyTransaction(session -> {
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
                            throw new IllegalArgumentException("Unsupported sort parameter: " + param.getParameter());
                    }
                    criteria.addOrder(sortOrder);
                }
            }

            List<JobData> jobsList = criteria.list();
            return jobsList.stream().map(JobData::toJobInfo).collect(Collectors.toList());
        });

        return new Page<>(lJobs, totalNbJobs);
    }

    public Page<TaskState> getTaskStates(final long from, final long to, final String tag, final int offset,
            final int limit, final String user, final boolean pending, final boolean running, final boolean finished,
            SortSpecifierContainer sortParams) {

        DBTaskDataParameters parameters = new DBTaskDataParameters(tag,
                                                                   from,
                                                                   to,
                                                                   offset,
                                                                   limit,
                                                                   user,
                                                                   pending,
                                                                   running,
                                                                   finished,
                                                                   sortParams);
        int totalNbTasks = getTotalNumberOfTasks(parameters);
        List<TaskState> lTasks = executeReadOnlyTransaction(TaskDBUtils.taskStateSessionWork(parameters));

        return new Page<>(lTasks, totalNbTasks);
    }

    public Page<TaskInfo> getTasks(final long from, final long to, final String tag, final int offset, final int limit,
            final String user, final boolean pending, final boolean running, final boolean finished) {

        DBTaskDataParameters parameters = new DBTaskDataParameters(tag,
                                                                   from,
                                                                   to,
                                                                   offset,
                                                                   limit,
                                                                   user,
                                                                   pending,
                                                                   running,
                                                                   finished,
                                                                   SortSpecifierContainer.EMPTY_CONTAINER);
        int totalNbTasks = getTotalNumberOfTasks(parameters);
        List<TaskInfo> lTaskInfo = executeReadOnlyTransaction(TaskDBUtils.taskInfoSessionWork(parameters));

        return new Page<>(lTaskInfo, totalNbTasks);
    }

    private int getTotalNumberOfTasks(final DBTaskDataParameters params) {

        return executeReadOnlyTransaction(TaskDBUtils.getTotalNumberOfTasks(params));

    }

    private int getTotalNumberOfJobs(final DBJobDataParameters params) {

        return executeReadOnlyTransaction(session -> {

            Set<JobStatus> statuses = params.getStatuses();

            if (statuses.isEmpty()) {
                return 0;
            } else {

                boolean hasUser = params.getUser() != null && "".compareTo(params.getUser()) != 0;

                StringBuilder queryString = new StringBuilder("select count(*) from JobData where removedTime = -1 ");

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
        return executeReadOnlyTransaction(session -> {
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

            return jobsList.stream().map(JobData::toJobUsage).collect(Collectors.toList());
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
        return getJobsNumberWithStatus(Collections.singletonList(JobStatus.FINISHED));
    }

    public long getPendingJobsCount() {
        return getJobsNumberWithStatus(Collections.singletonList(JobStatus.PENDING));
    }

    public long getRunningJobsCount() {
        return getJobsNumberWithStatus(Collections.singletonList(JobStatus.RUNNING));
    }

    public long getTotalJobsCount() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getTotalJobsCount");
            return (Long) query.uniqueResult();
        });
    }

    private long getJobsNumberWithStatus(final Collection<JobStatus> status) {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getJobsNumberWithStatus").setParameterList("status", status);

            return (Long) query.uniqueResult();
        });
    }

    public long getJobsCount(JobStatus status) {
        return getJobsNumberWithStatus(Collections.singletonList(status));
    }

    public long getFinishedTasksCount() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getFinishedTasksCount")
                                 .setParameterList("taskStatus", Arrays.asList(TaskStatus.FINISHED, TaskStatus.FAULTY));

            return (Long) query.uniqueResult();
        });
    }

    public long getPendingTasksCount() {
        return executeReadOnlyTransaction(session -> {
            Collection<TaskStatus> taskStatus = Arrays.asList(TaskStatus.SUBMITTED,
                                                              TaskStatus.PAUSED,
                                                              TaskStatus.PENDING,
                                                              TaskStatus.WAITING_ON_ERROR,
                                                              TaskStatus.WAITING_ON_FAILURE);
            Query query = session.getNamedQuery("getPendingTasksCount")
                                 .setParameterList("jobStatus", NOT_FINISHED_JOB_STATUSES)
                                 .setParameterList("taskStatus", taskStatus);

            return (Long) query.uniqueResult();
        });
    }

    public long getTaskCount(TaskStatus filter) {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getTasksCount").setParameter("taskStatus", filter);

            return (Long) query.uniqueResult();
        });
    }

    public long getRunningTasksCount() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getRunningTasksCount")
                                 .setParameterList("jobStatus", NOT_FINISHED_JOB_STATUSES)
                                 .setParameterList("taskStatus", Collections.singletonList(TaskStatus.RUNNING));

            return (Long) query.uniqueResult();
        });
    }

    public long getTotalTasksCount() {
        return executeReadOnlyTransaction(session -> {
            return (Long) session.getNamedQuery("getTotalTasksCount").uniqueResult();
        });
    }

    public double getMeanJobPendingTime() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getMeanJobPendingTime");
            Double result = (Double) query.uniqueResult();
            return result == null ? 0.0d : result;
        });
    }

    public double getMeanJobExecutionTime() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getMeanJobExecutionTime");
            Double result = (Double) query.uniqueResult();
            return result == null ? 0.0d : result;
        });
    }

    public double getMeanJobSubmittingPeriod() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("getMeanJobSubmittingPeriod");
            Object[] result = (Object[]) query.uniqueResult();
            Long count = (Long) result[0];
            Long minSubmittedTime = (Long) result[1];
            Long maxSubmittedTime = (Long) result[2];
            if (count < 2) {
                return 0.0d;
            } else {
                return (maxSubmittedTime - minSubmittedTime) / (double) (count - 1);
            }
        });
    }

    public long getJobRunningTime(final String jobId) {
        final long id = Long.parseLong(jobId);

        Long result = executeReadOnlyTransaction(session -> {
            JobData jobData = session.get(JobData.class, id);
            if (jobData == null) {
                return null;
            }
            if (jobData.getFinishedTime() > 0L) {
                return jobData.getFinishedTime() - jobData.getStartTime();
            } else {
                return null;
            }
        });

        return checkResult(id, result);
    }

    public long getJobPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Long result = executeReadOnlyTransaction(session -> {
            JobData jobData = session.get(JobData.class, id);
            if (jobData == null) {
                return null;
            }
            if (jobData.getStartTime() > 0L) {
                return jobData.getStartTime() - jobData.getSubmittedTime();
            } else {
                return null;
            }
        });

        return checkResult(id, result);
    }

    public double getMeanTaskPendingTime(final String jobId) {
        final long id = Long.parseLong(jobId);
        Double result = executeReadOnlyTransaction(session -> {
            Query jobSubmittedTimeQuery = session.getNamedQuery("getJobSubmittedTime").setParameter("id", id);
            Long jobSubmittedTime = (Long) jobSubmittedTimeQuery.uniqueResult();
            if (jobSubmittedTime == null) {
                return null;
            }
            Query query = session.getNamedQuery("getMeanTaskPendingTime")
                                 .setParameter("id", id)
                                 .setParameter("jobSubmittedTime", jobSubmittedTime);

            Double result1 = (Double) query.uniqueResult();
            return result1 == null ? 0.0d : result1;
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
        Double result = executeReadOnlyTransaction(session -> {
            Query jobQuery = session.getNamedQuery("checkJobExistence").setParameter("id", id);
            if (jobQuery.uniqueResult() == null) {
                return null;
            }

            Query query = session.getNamedQuery("getMeanTaskRunningTime").setParameter("id", id);

            Double uniqueResult = (Double) query.uniqueResult();
            return uniqueResult == null ? 0.0d : uniqueResult;
        });

        return checkResult(id, result);
    }

    public int getTotalNumberOfHostsUsed(String jobId) {
        final long id = Long.parseLong(jobId);
        int result = executeReadOnlyTransaction(session -> {
            Query jobQuery = session.getNamedQuery("checkJobExistence").setParameter("id", id);
            if (jobQuery.uniqueResult() == null) {
                return null;
            }

            Query query = session.getNamedQuery("getTotalNumberOfHostsUsed").setParameter("id", id);

            return ((Long) query.uniqueResult()).intValue();
        });

        return checkResult(id, result);
    }

    public SchedulerAccount readAccount(final String username) {
        return executeReadOnlyTransaction(session -> {
            Query tasksQuery = session.getNamedQuery("readAccountTasks").setParameter("username", username);

            int taskCount;
            long taskDuration;

            Object[] taskResult = (Object[]) tasksQuery.uniqueResult();
            taskCount = ((Number) taskResult[0]).intValue();
            if (taskResult[1] != null) {
                taskDuration = ((Number) taskResult[1]).longValue();
            } else {
                taskDuration = 0L;
            }

            int jobCount;
            long jobDuration;

            Query jobQuery = session.getNamedQuery("readAccountJobs").setParameter("username", username);

            Object[] jobResult = (Object[]) jobQuery.uniqueResult();
            jobCount = ((Number) jobResult[0]).intValue();
            if (jobResult[1] != null) {
                jobDuration = ((Number) jobResult[1]).longValue();
            } else {
                jobDuration = 0L;
            }

            return new SchedulerAccount(username, taskCount, taskDuration, jobCount, jobDuration);
        });
    }

    private void removeJobScripts(Session session, List<Long> jobIds) {
        // This query competes with "deleteJobDataInBulk" query.
        // So Oracle 12c can stuck in deadlock.
        // It is definitely something to improve.
        // For now, we added "additionalDelayRandomized" in TransactionHelper.
        session.getNamedQuery("updateTaskDataJobScripts").setParameterList("ids", jobIds).executeUpdate();

        session.getNamedQuery("deleteScriptDataInBulk").setParameterList("jobIdList", jobIds).executeUpdate();
        session.getNamedQuery("deleteSelectionScriptDataInBulk").setParameterList("jobIdList", jobIds).executeUpdate();
    }

    private void removeJobScripts(Session session, long jobId) {
        removeJobScripts(session, Collections.singletonList(jobId));
    }

    private void removeJobRuntimeData(Session session, long jobId) {
        removeJobScripts(session, jobId);

        session.getNamedQuery("deleteEnvironmentModifierData").setParameter("jobId", jobId).executeUpdate();

        session.getNamedQuery("deleteTaskDataVariable").setParameter("jobId", jobId).executeUpdate();

        session.getNamedQuery("deleteSelectorData").setParameter("jobId", jobId).executeUpdate();
    }

    public void scheduleJobForRemoval(final JobId jobId, final long timeForRemoval, final boolean shouldRemoveFromDb) {
        // scheduleJobForRemoval
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            session.getNamedQuery("setJobForRemoval")
                   .setParameter("timeForRemoval", timeForRemoval)
                   .setParameter("toBeRemoved", shouldRemoveFromDb)
                   .setParameter("jobId", jobId.longValue())
                   .executeUpdate();
            return null;
        });
    }

    public Map<JobId, String> getJobsToRemove(final long time) {
        return executeReadOnlyTransaction(session -> {
            Query query = session.createSQLQuery("select ID, OWNER from JOB_DATA where " +
                                                 "SCHEDULED_TIME_FOR_REMOVAL <> 0 and " +
                                                 "SCHEDULED_TIME_FOR_REMOVAL < :timeLimit")
                                 .addScalar("ID", StandardBasicTypes.LONG)
                                 .addScalar("OWNER", StandardBasicTypes.STRING)
                                 .setParameter("timeLimit", time);

            return ((List<Object[]>) query.list()).stream()
                                                  .collect(Collectors.toMap(pair -> JobIdImpl.makeJobId(((Long) pair[0]).toString()),
                                                                            pair -> (String) pair[1]));
        });
    }

    public void executeHousekeepingInDB(final List<Long> jobIdList, final boolean shouldRemoveFromDb) {
        executeReadWriteTransaction(new HousekeepingSessionWork(jobIdList, shouldRemoveFromDb));
    }

    public void removeJob(final JobId jobId, final long removedTime, final boolean removeData) {
        removeJob(Collections.singletonList(jobId), removedTime, removeData);
    }

    public void removeJob(final List<JobId> jobIds, final long removedTime, final boolean removeData) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            List<Long> ids = jobIds.stream().map(SchedulerDBManager::jobId).collect(Collectors.toList());

            if (removeData) {
                session.createSQLQuery("delete from TASK_DATA_DEPENDENCIES where JOB_ID in (:ids)")
                       .setParameterList("ids", ids)
                       .executeUpdate();
                session.createSQLQuery("delete from TASK_DATA_JOINED_BRANCHES where JOB_ID in (:ids)")
                       .setParameterList("ids", ids)
                       .executeUpdate();
                session.createSQLQuery("delete from JOB_CONTENT where JOB_ID in (:ids)")
                       .setParameterList("ids", ids)
                       .executeUpdate();

                session.getNamedQuery("deleteJobDataVariable").setParameterList("ids", ids).executeUpdate();

                removeJobScripts(session, ids);

                session.getNamedQuery("deleteJobDataInBulk").setParameterList("jobIdList", ids).executeUpdate();
            } else {
                session.getNamedQuery("updateJobDataRemovedTime")
                       .setParameter("removedTime", removedTime)
                       .setParameter("lastUpdatedTime", new Date().getTime())
                       .setParameterList("ids", ids)
                       .executeUpdate();
            }
            return null;
        });
    }

    public List<InternalJob> loadNotFinishedJobs(boolean fullState) {
        return loadJobs(fullState, NOT_FINISHED_JOB_STATUSES, -1);
    }

    public List<InternalJob> loadFinishedJobs(boolean fullState, long period) {
        return loadJobs(fullState, FINISHED_JOB_STATUSES, period);
    }

    private List<InternalJob> loadJobs(final boolean fullState, final Collection<JobStatus> status, final long period) {
        return executeReadOnlyTransaction(session -> {
            logger.info("Loading Jobs from database");

            Query query;
            if (period >= 0L) {
                query = session.getNamedQuery("loadJobsWithPeriod")
                               .setParameter("minSubmittedTime", System.currentTimeMillis() - period)
                               .setParameterList("status", status)
                               .setReadOnly(true);
            } else {
                query = session.getNamedQuery("loadJobs").setParameterList("status", status).setReadOnly(true);
            }

            List<Long> ids = query.list();

            logger.info(ids.size() + " Jobs to fetch from database");

            return loadInternalJobs(fullState, session, ids);
        });
    }

    public List<InternalJob> loadJobWithTasksIfNotRemoved(final JobId... jobIds) {
        return executeReadOnlyTransaction(session -> {
            Query jobQuery = session.getNamedQuery("loadJobDataIfNotRemoved").setReadOnly(true);

            List<Long> ids = Arrays.stream(jobIds).map(SchedulerDBManager::jobId).collect(Collectors.toList());

            List<InternalJob> result = new ArrayList<>(jobIds.length);
            batchLoadJobs(session, false, jobQuery, ids, result);
            return result;
        });
    }

    public List<InternalJob> loadJobs(final boolean fullState, final JobId... jobIds) {
        return executeReadOnlyTransaction(session -> {
            final List<Long> ids = Stream.of(jobIds).map(SchedulerDBManager::jobId).collect(Collectors.toList());
            return loadInternalJobs(fullState, session, ids);
        });
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<TaskData>> loadJobsTasks(Session session, List<Long> jobIds) {
        Query tasksQuery = session.getNamedQuery("loadJobsTasks")
                                  .setParameterList("ids", jobIds)
                                  .setReadOnly(true)
                                  .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<TaskData> tasks = tasksQuery.list();
        return tasks.stream().collect(Collectors.groupingBy(taskData -> taskData.getJobData().getId()));
    }

    public List<InternalJob> loadInternalJob(Long id) {
        return executeReadOnlyTransaction(session -> loadInternalJobs(false, session, Collections.singletonList(id)));
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

        logger.info(ALL_REQUIRED_JOBS_HAVE_BEEN_FETCHED);

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
                InternalTask internalTask = taskData.toInternalTask(internalJob, loadFullState);
                if (loadFullState) {
                    internalTask.setParallelEnvironment(taskData.getParallelEnvironment());
                    internalTask.setGenericInformation(taskData.getGenericInformation());
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
                    List<InternalTask> branches = taskData.getJoinedBranches()
                                                          .stream()
                                                          .map(tasks::get)
                                                          .collect(Collectors.toList());
                    internalTask.setJoinedBranches(branches);
                }
                internalTask.setName(internalTask.getName());
            }
        }

        return tasks.values();
    }

    public void changeJobPriority(final JobId jobId, final JobPriority priority) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long id = jobId(jobId);
            session.getNamedQuery("updateJobDataPriority")
                   .setParameter("priority", priority)
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("jobId", id)
                   .executeUpdate();
            return null;
        });
    }

    public void jobTaskStarted(final InternalJob job, final InternalTask task, final boolean taskStatusToPending) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long jobId = jobId(job);

            JobInfo jobInfo = job.getJobInfo();

            session.getNamedQuery("updateJobDataTaskStarted")
                   .setParameter("status", jobInfo.getStatus())
                   .setParameter("startTime", jobInfo.getStartTime())
                   .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                   .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("jobId", jobId)
                   .executeUpdate();

            DBTaskId taskId = taskId(task);

            TaskInfo taskInfo = task.getTaskInfo();

            ExecuterInformationData executerInfo = new ExecuterInformationData(taskId.getTaskId(),
                                                                               task.getExecuterInformation());

            session.getNamedQuery("updateTaskDataTaskStarted")
                   .setParameter("taskStatus", taskInfo.getStatus())
                   .setParameter("startTime", taskInfo.getStartTime())
                   .setParameter("finishedTime", taskInfo.getFinishedTime())
                   .setParameter("executionHostName", taskInfo.getExecutionHostName())
                   .setParameter("executerInformationData", executerInfo)
                   .setParameter("taskId", taskId)
                   .executeUpdate();
            return null;
        });
    }

    public void taskRestarted(final InternalJob job, final InternalTask task, final TaskResultImpl result) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long jobId = jobId(job);

            JobInfo jobInfo = job.getJobInfo();

            session.getNamedQuery("updateJobDataTaskRestarted")
                   .setParameter("status", jobInfo.getStatus())
                   .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                   .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                   .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                   .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                   .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("jobId", jobId)
                   .executeUpdate();

            DBTaskId taskId = taskId(task);

            TaskInfo taskInfo = task.getTaskInfo();

            session.getNamedQuery("updateTaskDataTaskRestarted")
                   .setParameter("taskStatus", taskInfo.getStatus())
                   .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                   .setParameter("numberOfExecutionOnFailureLeft", taskInfo.getNumberOfExecutionOnFailureLeft())
                   .setParameter("taskId", taskId)
                   .executeUpdate();

            if (result != null) {
                saveTaskResult(taskId, result, session);
            }

            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public void updateAfterWorkflowTaskFinished(final InternalJob job, final ChangedTasksInfo changesInfo,
            final TaskResultImpl result) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long jobId = jobId(job);

            JobInfo jobInfo = job.getJobInfo();
            session.getNamedQuery("updateJobDataAfterWorkflowTaskFinished")
                   .setParameter("status", jobInfo.getStatus())
                   .setParameter("finishedTime", jobInfo.getFinishedTime())
                   .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                   .setParameter("numberOfFinishedTasks", jobInfo.getNumberOfFinishedTasks())
                   .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                   .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                   .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                   .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                   .setParameter("totalNumberOfTasks", jobInfo.getTotalNumberOfTasks())
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("resultMap", ObjectByteConverter.mapOfSerializableToByteArray(job.getResultMap()))
                   .setParameter("jobId", jobId)
                   .executeUpdate();

            JobData jobRuntimeData = session.load(JobData.class, jobId);

            List<DBTaskId> taskIds = new ArrayList<>(changesInfo.getSkippedTasks().size() +
                                                     changesInfo.getUpdatedTasks().size());
            for (TaskId id : changesInfo.getSkippedTasks()) {
                taskIds.add(taskId(id));
            }
            for (TaskId id : changesInfo.getUpdatedTasks()) {
                taskIds.add(taskId(id));
            }

            Query tasksQuery = session.getNamedQuery("findTaskData").setParameterList("ids", taskIds);
            List<TaskData> tasksToUpdate = tasksQuery.list();
            Set<TaskId> newTasks = changesInfo.getNewTasks();

            for (TaskData taskData : tasksToUpdate) {
                InternalTask task = job.getIHMTasks().get(taskData.createTaskId(job));
                taskData.updateMutableAttributes(task);
                session.update(taskData);
                saveSingleTaskDependencies(session, task, taskData);
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
                saveSingleTaskDependencies(session, task, taskData);
                if (++counter % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            DBTaskId taskId = taskId(result.getTaskId());
            saveTaskResult(taskId, result, session);

            if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
                removeJobRuntimeData(session, jobId);
            }

            return null;
        }, false);
    }

    public void updateAfterJobKilled(InternalJob job, Set<TaskId> tasksToUpdate) {
        updateAfterTaskFinished(job, null, null, tasksToUpdate);
    }

    public void updateAfterJobFailed(InternalJob job, InternalTask finishedTask, TaskResultImpl result,
            Set<TaskId> tasksToUpdate) {
        updateAfterTaskFinished(job, finishedTask, result, tasksToUpdate);
    }

    public void killJob(InternalJob job) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long jobId = jobId(job);

            JobInfo jobInfo = job.getJobInfo();
            int updateJob = 0;
            updateJob = session.getNamedQuery("updateJobDataAfterTaskFinished")
                               .setParameter("status", jobInfo.getStatus())
                               .setParameter("finishedTime", jobInfo.getFinishedTime())
                               .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                               .setParameter("numberOfFinishedTasks", jobInfo.getNumberOfFinishedTasks())
                               .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                               .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                               .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                               .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                               .setParameter("lastUpdatedTime", new Date().getTime())
                               .setParameter("resultMap",
                                             ObjectByteConverter.mapOfSerializableToByteArray(job.getResultMap()))
                               .setParameter("jobId", jobId)
                               .executeUpdate();

            final int notReStarted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.NOT_RESTARTED " +
                                                         " where task.jobData.id = :jobId and task.taskStatus in :taskStatuses ")
                                            .setParameter("jobId", jobId)
                                            .setParameterList("taskStatuses",
                                                              Arrays.asList(TaskStatus.WAITING_ON_ERROR,
                                                                            TaskStatus.WAITING_ON_FAILURE))
                                            .executeUpdate();

            final int notStarted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.NOT_STARTED " +
                                                       " where task.jobData.id = :jobId and task.taskStatus in :taskStatuses ")
                                          .setParameter("jobId", jobId)
                                          .setParameterList("taskStatuses",
                                                            TaskStatus.allExceptThese(TaskStatus.RUNNING,
                                                                                      TaskStatus.WAITING_ON_ERROR,
                                                                                      TaskStatus.WAITING_ON_FAILURE,
                                                                                      TaskStatus.FAILED,
                                                                                      TaskStatus.NOT_STARTED,
                                                                                      TaskStatus.FAULTY,
                                                                                      TaskStatus.FINISHED,
                                                                                      TaskStatus.SKIPPED))
                                          .executeUpdate();

            final int runningToAborted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.ABORTED, " +
                                                             " task.finishedTime = :finishedTime where task.jobData.id = :jobId " +
                                                             " and task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.RUNNING " +
                                                             " and ( task.startTime <= 0 or task.executionDuration >= 0 )")
                                                .setParameter("finishedTime", System.currentTimeMillis())
                                                .setParameter("jobId", jobId)
                                                .executeUpdate();

            final int runningToAbortedWithDuration = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.ABORTED, " +
                                                                         " task.finishedTime = :finishedTime, " +
                                                                         " task.executionDuration = task.finishedTime - task.startTime " +
                                                                         " where task.jobData.id = :jobId " +
                                                                         " and task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.RUNNING " +
                                                                         " and task.startTime > 0 and task.executionDuration < 0 ")
                                                            .setParameter("jobId", jobId)
                                                            .setParameter("finishedTime", System.currentTimeMillis())
                                                            .executeUpdate();

            logger.trace(String.format("Kill job %d and tasks: %d %d %d %d %d",
                                       jobId,
                                       updateJob,
                                       notReStarted,
                                       notStarted,
                                       runningToAborted,
                                       runningToAbortedWithDuration));

            if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
                session.flush();
                session.clear();

                removeJobRuntimeData(session, jobId);
                logger.trace("Flush after kill for job " + jobId);
            }

            return null;
        });
    }

    public void killJobs(List<InternalJob> jobs) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            List<Long> jobIds = jobs.stream().map(SchedulerDBManager::jobId).collect(Collectors.toList());

            for (InternalJob job : jobs) {
                long jobId = jobId(job);
                JobInfo jobInfo = job.getJobInfo();
                int updateJob = 0;
                updateJob = session.getNamedQuery("updateJobDataAfterTaskFinished")
                                   .setParameter("status", jobInfo.getStatus())
                                   .setParameter("finishedTime", jobInfo.getFinishedTime())
                                   .setParameter("numberOfPendingTasks", jobInfo.getNumberOfPendingTasks())
                                   .setParameter("numberOfFinishedTasks", jobInfo.getNumberOfFinishedTasks())
                                   .setParameter("numberOfRunningTasks", jobInfo.getNumberOfRunningTasks())
                                   .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
                                   .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
                                   .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
                                   .setParameter("lastUpdatedTime", new Date().getTime())
                                   .setParameter("resultMap",
                                                 ObjectByteConverter.mapOfSerializableToByteArray(job.getResultMap()))
                                   .setParameter("jobId", jobId)
                                   .executeUpdate();

            }

            final int notReStarted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.NOT_RESTARTED " +
                                                         " where task.jobData.id in :jobIds and task.taskStatus in :taskStatuses ")
                                            .setParameterList("jobIds", jobIds)
                                            .setParameterList("taskStatuses",
                                                              Arrays.asList(TaskStatus.WAITING_ON_ERROR,
                                                                            TaskStatus.WAITING_ON_FAILURE))
                                            .executeUpdate();

            final int notStarted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.NOT_STARTED " +
                                                       " where task.jobData.id in :jobIds and task.taskStatus in :taskStatuses ")
                                          .setParameterList("jobIds", jobIds)
                                          .setParameterList("taskStatuses",
                                                            TaskStatus.allExceptThese(TaskStatus.RUNNING,
                                                                                      TaskStatus.WAITING_ON_ERROR,
                                                                                      TaskStatus.WAITING_ON_FAILURE,
                                                                                      TaskStatus.FAILED,
                                                                                      TaskStatus.NOT_STARTED,
                                                                                      TaskStatus.FAULTY,
                                                                                      TaskStatus.FINISHED,
                                                                                      TaskStatus.SKIPPED))
                                          .executeUpdate();

            final int runningToAborted = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.ABORTED, " +
                                                             " task.finishedTime = :finishedTime where task.jobData.id in :jobIds " +
                                                             " and task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.RUNNING " +
                                                             " and ( task.startTime <= 0 or task.executionDuration >= 0 )")
                                                .setParameter("finishedTime", System.currentTimeMillis())
                                                .setParameter("jobIds", jobIds)
                                                .executeUpdate();

            final int runningToAbortedWithDuration = session.createQuery("update TaskData task set task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.ABORTED, " +
                                                                         " task.finishedTime = :finishedTime, " +
                                                                         " task.executionDuration = task.finishedTime - task.startTime " +
                                                                         " where task.jobData.id in :jobIds " +
                                                                         " and task.taskStatus = org.ow2.proactive.scheduler.common.task.TaskStatus.RUNNING " +
                                                                         " and task.startTime > 0 and task.executionDuration < 0 ")
                                                            .setParameter("jobIds", jobIds)
                                                            .setParameter("finishedTime", System.currentTimeMillis())
                                                            .executeUpdate();

            //            logger.trace(String.format("Kill job %d and tasks: %d %d %d %d %d",
            //                    jobId,
            //                    updateJob,
            //                    notReStarted,
            //                    notStarted,
            //                    runningToAborted,
            //                    runningToAbortedWithDuration));

            session.flush();
            session.clear();
            //            if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
            //                removeJobRuntimeData(session, jobId);
            //                logger.trace("Flush after kill for job " + jobId);
            //            }

            return null;
        });
    }

    public void updateJobAndTasksState(final InternalJob job) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {

            for (TaskState task : job.getTasks()) {
                updateTaskData(task, session);
            }

            updateJobState(job, session);

            return null;
        });
    }

    public void pauseJobAndTasks(final InternalJob job) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            pauseTasks(job, session);

            updateJobState(job, session);

            return null;
        });
    }

    public void updateJobAndRestartAllInErrorTasks(InternalJob job) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            restartAllInErrorTasks(job, session);

            updateJobState(job, session);

            return null;
        });
    }

    private void restartAllInErrorTasks(InternalJob job, Session session) {
        session.getNamedQuery("restartAllInErrorTasks").setParameter("jobId", jobId(job)).executeUpdate();
    }

    private void pauseTasks(InternalJob job, Session session) {
        session.getNamedQuery("pauseTasks").setParameter("jobId", jobId(job)).executeUpdate();
    }

    public void unpauseJobAndTasks(final InternalJob job) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            unpauseTasks(job, session);

            updateJobState(job, session);

            return null;
        });
    }

    private void unpauseTasks(InternalJob job, Session session) {
        if (job.getJobInfo().getStatus() == JobStatus.PENDING) {
            session.getNamedQuery("unpausePendingTasks").setParameter("jobId", jobId(job)).executeUpdate();
        } else if (job.getJobInfo().getStatus() == JobStatus.RUNNING ||
                   job.getJobInfo().getStatus() == JobStatus.STALLED) {
            session.getNamedQuery("unpauseTasks").setParameter("jobId", jobId(job)).executeUpdate();
        }
    }

    private void updateJobState(InternalJob job, Session session) {
        JobInfo jobInfo = job.getJobInfo();

        session.getNamedQuery("updateJobAndTasksState")
               .setParameter("status", jobInfo.getStatus())
               .setParameter("numberOfFailedTasks", jobInfo.getNumberOfFailedTasks())
               .setParameter("numberOfFaultyTasks", jobInfo.getNumberOfFaultyTasks())
               .setParameter("numberOfInErrorTasks", jobInfo.getNumberOfInErrorTasks())
               .setParameter("inErrorTime", jobInfo.getInErrorTime())
               .setParameter("lastUpdatedTime", new Date().getTime())
               .setParameter("jobId", jobId(job))
               .executeUpdate();
    }

    public void updateJobAndTaskState(final InternalJob job, final InternalTask task) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {

            updateTaskData(task, session);

            updateJobState(job, session);

            return null;
        });
    }

    public void updateTaskSchedulingTime(final InternalJob job, final long scheduledTime) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {

            for (TaskState task : job.getTasks()) {
                updateScheduledTime(job.getId().longValue(), task.getId().longValue(), scheduledTime);
            }

            return null;
        });
    }

    public void updateTaskStatus(final EligibleTaskDescriptorImpl task, final TaskStatus newStatus) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {

            final DBTaskId dbTaskId = taskId(task.getInternal());

            Query query = session.createQuery("update TaskData task set task.taskStatus = :newStatus " +
                                              "where task.id = :taskId")
                                 .setParameter("newStatus", newStatus)
                                 .setParameter("taskId", dbTaskId);

            query.executeUpdate();

            return null;
        });
    }

    public void updateTaskState(final TaskState task) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            updateTaskData(task, session);

            return null;
        });
    }

    private int updateTaskData(final TaskState task, Session session) {

        Query taskUpdateQuery = session.getNamedQuery("updateTaskData");

        TaskInfo taskInfo = task.getTaskInfo();

        return taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus())
                              .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                              .setParameter("numberOfExecutionOnFailureLeft",
                                            taskInfo.getNumberOfExecutionOnFailureLeft())
                              .setParameter("inErrorTime", taskInfo.getInErrorTime())
                              .setParameter("taskId", taskId(task.getId()))
                              .executeUpdate();
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
        executeReadWriteTransaction((SessionWork<Void>) session -> {

            Query query = session.createQuery("update TaskData task set task." + fieldName + " = :newTime " + // NOSONAR
                                              "where task.id.jobId = :jobId and task.id.taskId= :taskId")
                                 .setParameter("newTime", time)
                                 .setParameter("jobId", jobId)
                                 .setParameter("taskId", taskId);

            query.executeUpdate();

            return null;
        });
    }

    public void updateAfterTaskFinished(final InternalJob job, final InternalTask finishedTask,
            final TaskResultImpl result) {
        updateAfterTaskFinished(job, finishedTask, result, new HashSet<TaskId>(1));
    }

    private void updateAfterTaskFinished(final InternalJob job, final InternalTask finishedTask,
            final TaskResultImpl result, final Set<TaskId> tasksToUpdate) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
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
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("resultMap", ObjectByteConverter.mapOfSerializableToByteArray(job.getResultMap()))
                   .setParameter("jobId", jobId)
                   .executeUpdate();

            Query taskUpdateQuery = session.getNamedQuery("updateTaskDataAfterJobFinished");

            if (finishedTask != null) {
                tasksToUpdate.add(finishedTask.getId());
            }

            for (TaskId id : tasksToUpdate) {
                InternalTask task = job.getIHMTasks().get(id);
                DBTaskId taskId = taskId(task.getId());

                TaskInfo taskInfo = task.getTaskInfo();

                taskUpdateQuery.setParameter("taskStatus", taskInfo.getStatus())
                               .setParameter("numberOfExecutionLeft", taskInfo.getNumberOfExecutionLeft())
                               .setParameter("numberOfExecutionOnFailureLeft",
                                             taskInfo.getNumberOfExecutionOnFailureLeft())
                               .setParameter("finishedTime", taskInfo.getFinishedTime())
                               .setParameter("executionDuration", taskInfo.getExecutionDuration())
                               .setParameter("taskId", taskId)
                               .executeUpdate();
            }

            if (result != null) {
                DBTaskId taskId = taskId(finishedTask.getId());
                saveTaskResult(taskId, result, session);
            }

            if (FINISHED_JOB_STATUSES.contains(job.getStatus())) {
                session.flush();
                session.clear();

                removeJobRuntimeData(session, jobId);
            }

            return null;
        });
    }

    private TaskResultData saveTaskResult(TaskData.DBTaskId taskId, TaskResultImpl result, Session session) {
        TaskData taskRuntimeData = session.load(TaskData.class, taskId);

        TaskResultData resultData = TaskResultData.createTaskResultData(taskRuntimeData, result);
        session.save(resultData);

        return resultData;
    }

    public void jobSetToBeRemoved(final JobId jobId) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            long id = jobId(jobId);

            session.getNamedQuery("updateJobDataSetJobToBeRemoved")
                   .setParameter("toBeRemoved", true)
                   .setParameter("lastUpdatedTime", new Date().getTime())
                   .setParameter("jobId", id)
                   .executeUpdate();

            return null;
        });
    }

    public Map<TaskId, TaskResult> loadTasksResults(final JobId jobId, final List<TaskId> taskIds) {
        if (taskIds.isEmpty()) {
            throw new IllegalArgumentException("TaskIds list is empty");
        }

        return executeReadOnlyTransaction(session -> {
            JobData job = session.get(JobData.class, jobId(jobId));

            if (job == null) {
                throw new DatabaseManagerException("Invalid job id: " + jobId);
            }

            List<DBTaskId> dbTaskIds = taskIds.stream().map(SchedulerDBManager::taskId).collect(Collectors.toList());

            Query query = session.getNamedQuery("loadTasksResults").setParameterList("tasksIds", dbTaskIds);

            JobResultImpl jobResult = loadJobResult(session, query, job, jobId);
            if (jobResult == null) {
                throw new DatabaseManagerException("Failed to load result for tasks " + taskIds + " (job: " + jobId +
                                                   ")");
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
                    throw new DatabaseManagerException("Failed to load result for task " + taskId + " (job: " + jobId +
                                                       ")");
                } else {
                    resultsMap.put(taskId, taskResult);
                }
            }

            if (jobResult.getAllResults().size() != taskIds.size()) {
                throw new DatabaseManagerException("Results: " + jobResult.getAllResults().size() + " " +
                                                   taskIds.size());
            }

            return resultsMap;
        });

    }

    public JobResult loadJobResult(final JobId jobId) {
        return executeReadOnlyTransaction((SessionWork<JobResult>) session -> {
            long id = jobId(jobId);

            JobData job = session.get(JobData.class, id);

            if (job == null) {
                return null;
            }

            Query query = session.getNamedQuery("loadJobResult").setParameter("job", job);

            return loadJobResult(session, query, job, jobId);
        });
    }

    @SuppressWarnings("unchecked")
    private JobResultImpl loadJobResult(Session session, Query query, JobData job, JobId jobId) {
        JobResultImpl jobResult = new JobResultImpl();
        jobResult.setJobInfo(job.createJobInfo(jobId));

        try {
            jobResult.getResultMap().putAll(ObjectByteConverter.mapOfByteArrayToSerializable(job.getResultMap()));
        } catch (Exception e) {
            logger.error("error ", e);
        }

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
        return executeReadOnlyTransaction(session -> {
            long id = jobId(jobId);

            Object[] taskSearchResult = (Object[]) session.getNamedQuery("loadTasksResultByJobAndTaskName")
                                                          .setParameter("taskName", taskName)
                                                          .setParameter("job", session.load(JobData.class, id))
                                                          .uniqueResult();

            if (taskSearchResult == null) {
                throw new DatabaseManagerException("Failed to load result for task '" + taskName + ", job: " + jobId);
            }

            DBTaskId dbTaskId = (DBTaskId) taskSearchResult[0];
            String taskName1 = (String) taskSearchResult[1];
            TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName1, dbTaskId.getTaskId());

            return loadTaskResult(session, taskId, index);
        });
    }

    /**
     * Load all task results associated with the given job id and task name
     * When a task is executed several times (several attempts), all attempts are stored in the database.
     *
     * @param jobId    job id
     * @param taskName task name
     * @return a list of task results
     */
    public List<TaskResult> loadTaskResultAllAttempts(final JobId jobId, final String taskName) {
        return executeReadOnlyTransaction(session -> {
            long id = jobId(jobId);

            Object[] taskSearchResult = (Object[]) session.getNamedQuery("loadTasksResultByJobAndTaskName")
                                                          .setParameter("taskName", taskName)
                                                          .setParameter("job", session.load(JobData.class, id))
                                                          .uniqueResult();

            if (taskSearchResult == null) {
                throw new DatabaseManagerException("Failed to load result for task '" + taskName + ", job: " + jobId);
            }

            DBTaskId dbTaskId = (DBTaskId) taskSearchResult[0];
            String taskName1 = (String) taskSearchResult[1];
            TaskId taskId = TaskIdImpl.createTaskId(jobId, taskName1, dbTaskId.getTaskId());

            return loadTaskResultAllAttempts(session, taskId);
        });
    }

    public TaskResult loadTaskResult(final TaskId taskId, final int index) {
        return executeReadOnlyTransaction(session -> loadTaskResult(session, taskId, index));
    }

    /**
     * Load all task results associated with the given task id
     * When a task is executed several times (several attempts), all attempts are stored in the database.
     *
     * @param taskId task id
     * @return a list of task results
     */
    public List<TaskResult> loadTaskResultAllAttempts(final TaskId taskId) {
        return executeReadOnlyTransaction(session -> loadTaskResultAllAttempts(session, taskId));
    }

    @SuppressWarnings("unchecked")
    private TaskResult loadTaskResult(Session session, TaskId taskId, int resultIndex) {
        DBTaskId dbTaskId = taskId(taskId);

        TaskData task = session.load(TaskData.class, dbTaskId);
        Query query = session.getNamedQuery("loadTasksResultByTask").setParameter("task", task);

        query.setMaxResults(1);
        query.setFirstResult(resultIndex);
        List<TaskResultData> results = (List<TaskResultData>) query.list();
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0).toTaskResult(taskId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<TaskResult> loadTaskResultAllAttempts(Session session, TaskId taskId) {
        DBTaskId dbTaskId = taskId(taskId);

        TaskData task = session.load(TaskData.class, dbTaskId);
        Query query = session.getNamedQuery("loadTasksResultByTaskAsc").setParameter("task", task);

        return ((List<TaskResultData>) query.list()).stream()
                                                    .map(resultData -> resultData.toTaskResult(taskId))
                                                    .collect(Collectors.toList());
    }

    public void newJobSubmitted(final InternalJob job) {
        executeReadWriteTransaction(session -> {
            JobData jobRuntimeData = JobData.createJobData(job);
            session.save(jobRuntimeData);

            job.setId(new JobIdImpl(jobRuntimeData.getId(), job.getName()));

            ArrayList<InternalTask> iTasks = job.getITasks();
            List<InternalTask> tasksWithNewIds = new ArrayList<>(iTasks.size());

            for (int i = 0; i < iTasks.size(); i++) {
                InternalTask task = iTasks.get(i);
                task.setId(TaskIdImpl.createTaskId(job.getId(), task.getTaskInfo().getTaskId().getReadableName(), i));

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
        });
    }

    private TaskData getTaskReference(Session session, InternalTask task) {
        return session.get(TaskData.class, taskId(task));
    }

    private void saveTaskDependencies(Session session, List<InternalTask> tasks, List<TaskData> taskRuntimeDataList) {
        for (int i = 0; i < tasks.size(); i++) {
            InternalTask task = tasks.get(i);
            TaskData taskRuntimeData = taskRuntimeDataList.get(i);
            saveSingleTaskDependencies(session, task, taskRuntimeData);
        }
    }

    private void saveSingleTaskDependencies(Session session, InternalTask task, TaskData taskRuntimeData) {
        if (task.hasDependences()) {
            List<DBTaskId> dependencies = task.getDependences()
                                              .stream()
                                              .map(taskState -> taskId((InternalTask) taskState))
                                              .collect(Collectors.toList());

            taskRuntimeData.setDependentTasks(dependencies);
        } else {
            taskRuntimeData.setDependentTasks(Collections.emptyList());
        }

        if (task.getIfBranch() != null) {
            taskRuntimeData.setIfBranch(getTaskReference(session, task.getIfBranch()));
        } else {
            taskRuntimeData.setIfBranch(null);
        }

        if (task.getJoinedBranches() != null && !task.getJoinedBranches().isEmpty()) {
            List<DBTaskId> joinedBranches = task.getJoinedBranches()
                                                .stream()
                                                .map(SchedulerDBManager::taskId)
                                                .collect(Collectors.toList());
            taskRuntimeData.setJoinedBranches(joinedBranches);
        } else {
            taskRuntimeData.setJoinedBranches(Collections.emptyList());
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
            throw new DatabaseManagerException("Failed to query script data for task " + task.getId(), e);
        }
    }

    private boolean isScriptTask(InternalTask task) {
        return task.getClass().equals(InternalForkedScriptTask.class) ||
               task.getClass().equals(InternalScriptTask.class);
    }

    private TaskData queryScriptTaskData(Session session, InternalTask task) {
        return (TaskData) session.getNamedQuery("findTaskDataById").setParameter("taskId", taskId(task)).uniqueResult();
    }

    public ExecutableContainer loadExecutableContainer(final InternalTask task) {
        return executeReadOnlyTransaction(session -> loadExecutableContainer(session, task));
    }

    public List<SchedulerUserInfo> loadUsersWithJobs() {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("findUsersWithJobs");

            List<Object[]> list = query.list();

            return list.stream()
                       .map(nameAndCount -> new SchedulerUserInfo(null,
                                                                  nameAndCount[0].toString(),
                                                                  0,
                                                                  Long.parseLong(nameAndCount[2].toString()),
                                                                  Integer.parseInt(nameAndCount[1].toString())))
                       .collect(Collectors.toList());
        });
    }

    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.executeReadWriteTransaction(sessionWork);
    }

    private <T> T executeReadWriteTransaction(SessionWork<T> sessionWork, boolean readOnlyEntities) {
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

    private static Configuration createConfiguration(File configFile, Map<String, String> propertiesToReplace) {
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
                        properties.load(Files.newBufferedReader(configFile.toPath(), Charset.defaultCharset()));
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
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            session.saveOrUpdate(new ThirdPartyCredentialData(username,
                                                              key,
                                                              encryptedCredential.getEncryptedSymmetricKey(),
                                                              encryptedCredential.getEncryptedData()));
            return null;
        });
    }

    public Set<String> thirdPartyCredentialsKeySet(final String username) {
        return executeReadOnlyTransaction((SessionWork<Set<String>>) session -> {
            Query query = session.getNamedQuery("findThirdPartyCredentialsKeySetByUsername").setParameter("username",
                                                                                                          username);
            List<String> keys = query.list();
            return new HashSet<>(keys);

        });
    }

    public void removeThirdPartyCredential(final String username, final String key) {
        executeReadWriteTransaction((SessionWork<Void>) session -> {
            Query query = session.getNamedQuery("deleteThirdPartyCredentialsKeySetByUsernameAndKey")
                                 .setParameter("username", username)
                                 .setParameter("key", key);
            query.executeUpdate();
            return null;
        });
    }

    public Map<String, HybridEncryptedData> thirdPartyCredentialsMap(final String username) {
        return executeReadOnlyTransaction(session -> {
            Query query = session.getNamedQuery("findThirdPartyCredentialsMapByUsername").setParameter("username",
                                                                                                       username);
            List<Object[]> rows = query.list();
            Map<String, HybridEncryptedData> thirdPartyCredentialsMap = new HashMap<>(rows.size());
            for (Object[] row : rows) {
                String key = (String) row[0];
                byte[] encryptedSymmetricKey = (byte[]) row[1];
                byte[] encryptedValue = (byte[]) row[2];
                thirdPartyCredentialsMap.put(key, new HybridEncryptedData(encryptedSymmetricKey, encryptedValue));
            }
            return thirdPartyCredentialsMap;

        });
    }

    public boolean hasThirdPartyCredentials(final String jobOwner) {
        return executeReadOnlyTransaction(session -> {
            Long count = (Long) session.getNamedQuery("hasThirdPartyCredentials")
                                       .setParameter("username", jobOwner)
                                       .uniqueResult();

            return count > 0;
        });
    }

    public String loadInitalJobContent(final JobId jobId) {
        return executeReadOnlyTransaction(session -> {
            long id = jobId(jobId);

            Query query = session.getNamedQuery("loadJobContent").setLong("id", id);
            if (query.uniqueResult() == null) {
                throw new DatabaseManagerException("Invalid job id: " + jobId.value());
            }

            JobContent jobContent = (JobContent) query.uniqueResult();
            final String initJobContent = jobContent.getInitJobContent();
            if (initJobContent == null) {
                throw new DatabaseManagerException("Job content should not be null for job id: " + jobId.value());
            }
            return initJobContent;
        });
    }

    public TransactionHelper getTransactionHelper() {
        return transactionHelper;
    }

    public Map<Long, Map<String, Serializable>> getJobResultMaps(List<String> jobsId) {
        if (jobsId.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return executeReadOnlyTransaction(session -> {

            Query query = session.createQuery("SELECT id, resultMap FROM JobData WHERE id in (:jobIdList)");
            query.setParameterList("jobIdList", jobsId.stream().map(Long::parseLong).collect(Collectors.toList()));

            Map<Long, Map<String, Serializable>> result = new HashMap<>();
            List<Object[]> list = query.list();
            for (Object[] row : list) {
                long id = (long) row[0];
                Map<String, byte[]> resultMapAsBytes = (Map<String, byte[]>) row[1];

                Map<String, Serializable> stringSerializableMap = ObjectByteConverter.mapOfByteArrayToSerializable(resultMapAsBytes);
                result.put(id, stringSerializableMap);
            }

            return result;
        });
    }

    public Map<Long, List<String>> getPreciousTaskNames(List<String> jobsId) {
        if (jobsId.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return executeReadOnlyTransaction(session -> {
            Query query = session.createQuery("SELECT task.id.jobId, task.id.taskId, task.taskName " +
                                              "FROM TaskData as task " + "WHERE task.id.jobId in :jobIdList " +
                                              "and task.preciousResult = true");
            query.setParameterList("jobIdList", jobsId.stream().map(Long::parseLong).collect(Collectors.toList()));
            List<Object[]> list = query.list();
            return list.stream()
                       .collect(Collectors.groupingBy(row -> (Long) row[0])) // group by job id
                       .entrySet()
                       .stream()
                       .collect(Collectors.toMap(Map.Entry::getKey, pair -> pair.getValue()
                                                                                .stream()
                                                                                .sorted(Comparator.comparing(row -> (long) row[1])) // sort by task id
                                                                                .map(row -> (String) row[2])
                                                                                .collect(Collectors.toList())));
        });
    }
}
