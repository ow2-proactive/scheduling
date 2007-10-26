package org.objectweb.proactive.extra.scheduler.core.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;
import org.objectweb.proactive.extra.scheduler.job.JobResultImpl;
import org.objectweb.proactive.extra.scheduler.util.DatabaseManager;


/**
 * @author FRADJ Johann
 */
public class SchedulerDB extends AbstractSchedulerDB {
    private Connection connection = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;

    /**
     * Default constructor.
     *
     * @throws SQLException
     */
    public SchedulerDB() throws SQLException {
        connection = DatabaseManager.getInstance().connect(false);
        connection.setAutoCommit(false);
        statement = connection.createStatement();
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- private ------------------------------- //
    // -------------------------------------------------------------------- //
    private InputStream serialize(Object o) {
        PipedInputStream pis;
        try {
            pis = new PipedInputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new PipedOutputStream(
                        pis));
            oos.writeObject(o);
            oos.flush();
            oos.close();
            return pis;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object deserialize(Blob blob) {
        // FIXME ais-je le droit de laisser assert ???
        assert blob != null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(blob.getBinaryStream());
            Object o = ois.readObject();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // Nothing to do
                }
            }
        }
    }

    private boolean alreadyExistInTaskTable(int jobid_hashcode,
        int taskid_hashcode) {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery(
                    "SELECT 1 FROM TASK_EVENTS_AND_TASK_RESULTS WHERE jobid_hashcode=" +
                    jobid_hashcode + " AND taskid_hashcode=" + taskid_hashcode);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            rollback();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                // Nothing to do
            }
        }
        return false;
    }

    private boolean commit() {
        try {
            connection.commit();
            return true;
        } catch (SQLException e) {
            // TODO si il y a une exception la je suis mort donc que faire ?
            e.printStackTrace();
        }
        return false;
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            // TODO si il y a une exception la je suis mort donc que faire ?
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- extends AbstractSchedulerDB -------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#disconnect()
     */
    @Override
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            // Nothing to do
        }
        DatabaseManager.getInstance().disconnect();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addJob(org.objectweb.proactive.extra.scheduler.job.InternalJob)
     */
    @Override
    public boolean addJob(InternalJob internalJob) {
        try {
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO JOB_AND_JOB_EVENTS(jobid_hashcode, job) VALUES (?,?)");
            preparedStatement.setInt(1, internalJob.getId().hashCode());
            preparedStatement.setBlob(2, serialize(internalJob));
            if (preparedStatement.executeUpdate() == 1) {
                return commit();
            }
        } catch (SQLException e) {
            rollback();
        }
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#removeJob(org.objectweb.proactive.extra.scheduler.common.job.JobId)
     */
    @Override
    public boolean removeJob(JobId jobId) {
        try {
            statement.execute(
                "DELETE FROM JOB_AND_JOB_EVENTS WHERE jobid_hashcode=" +
                jobId.hashCode());
            statement.execute(
                "DELETE FROM TASK_EVENTS_AND_TASK_RESULTS WHERE jobid_hashcode=" +
                jobId.hashCode());
            return commit();
        } catch (SQLException e) {
            rollback();
        }
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addTaskResult(org.objectweb.proactive.extra.scheduler.common.task.TaskResult)
     */
    @Override
    public boolean addTaskResult(TaskResult taskResult) {
        try {
            int jobid_hashcode = taskResult.getTaskId().getJobId().hashCode();
            int taskid_hashcode = taskResult.getTaskId().hashCode();
            if (alreadyExistInTaskTable(jobid_hashcode, taskid_hashcode)) {
                preparedStatement = connection.prepareStatement(
                        "UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskresult=? WHERE jobid_hashcode=? AND taskid_hashcode=?");
            } else {
                preparedStatement = connection.prepareStatement(
                        "INSERT INTO TASK_EVENTS_AND_TASK_RESULTS(taskresult,jobid_hashcode,taskid_hashcode) VALUES(?,?,?)");
            }

            preparedStatement.setBlob(1, serialize(taskResult));
            preparedStatement.setInt(2, jobid_hashcode);
            preparedStatement.setInt(3, taskid_hashcode);
            if (preparedStatement.executeUpdate() == 1) {
                return commit();
            }
        } catch (SQLException e) {
            rollback();
        }
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getJobResult()
     */
    @Override
    public JobResult getJobResult() {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @throws SQLException
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        ResultSet rs = null;
        try {
            List<InternalJob> internalJobList = new ArrayList<InternalJob>();
            List<JobResult> jobResultList = new ArrayList<JobResult>();
            Map<JobId, JobResult> jobResultMap = new HashMap<JobId, JobResult>();
            Map<JobId, JobEvent> jobEventMap = new HashMap<JobId, JobEvent>();
            Map<TaskId, TaskEvent> taskEventMap = new HashMap<TaskId, TaskEvent>();

            rs = statement.executeQuery(
                    "SELECT job, jobevent FROM JOB_AND_JOB_EVENTS");
            while (rs.next()) {
                //
                InternalJob internalJob = (InternalJob) deserialize(rs.getBlob(
                            1));
                internalJobList.add(internalJob);
                //
                jobResultMap.put(internalJob.getId(),
                    new JobResultImpl(internalJob.getId(), internalJob.getName()));
                //
                JobEvent jobEvent = (JobEvent) deserialize(rs.getBlob(2));
                jobEventMap.put(jobEvent.getJobId(), jobEvent);
            }

            rs = statement.executeQuery(
                    "SELECT taskevent,taskresult FROM TASK_EVENTS_AND_TASK_RESULTS");
            while (rs.next()) {
                TaskEvent taskEvent = (TaskEvent) deserialize(rs.getBlob(1));
                taskEventMap.put(taskEvent.getTaskId(), taskEvent);
                jobResultMap.get(taskEvent.getTaskId().getJobId())
                            .addTaskResult(taskEvent.getTaskId()
                                                    .getReadableName(),
                    (TaskResult) deserialize(rs.getBlob(2)));
            }

            if ((internalJobList.size() == 0) && (jobResultMap.size() == 0) &&
                    (jobEventMap.size() == 0) && (taskEventMap.size() == 0) &&
                    commit()) {
                return null;
            }

            Collection<JobResult> col = jobResultMap.values();
            for (JobResult jr : col)
                jobResultList.add(jr);

            if (commit()) {
                return new RecoverableState(internalJobList, jobResultList,
                    jobEventMap, taskEventMap);
            }
        } catch (SQLException e) {
            rollback();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Nothing to do
                }
            }
        }
        return null;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getTaskResult()
     */
    @Override
    public TaskResult getTaskResult() {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public boolean setJobEvent(JobEvent jobEvent) {
        try {
            preparedStatement = connection.prepareStatement(
                    "UPDATE JOB_AND_JOB_EVENTS SET jobevent=? WHERE jobid_hashcode=?");
            preparedStatement.setBlob(1, serialize(jobEvent));
            preparedStatement.setInt(2, jobEvent.getJobId().hashCode());
            if (preparedStatement.executeUpdate() == 1) {
                return commit();
            }
        } catch (SQLException e) {
            rollback();
        }
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    @Override
    public boolean setTaskEvent(TaskEvent taskEvent) {
        try {
            int jobid_hashcode = taskEvent.getJobId().hashCode();
            int taskid_hashcode = taskEvent.getTaskId().hashCode();

            if (alreadyExistInTaskTable(jobid_hashcode, taskid_hashcode)) {
                preparedStatement = connection.prepareStatement(
                        "UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskevent=? WHERE jobid_hashcode=? AND taskid_hashcode=?");
            } else {
                preparedStatement = connection.prepareStatement(
                        "INSERT INTO TASK_EVENTS_AND_TASK_RESULTS(taskevent,jobid_hashcode,taskid_hashcode) VALUES(?,?,?)");
            }
            preparedStatement.setBlob(1, serialize(taskEvent));
            preparedStatement.setInt(2, jobid_hashcode);
            preparedStatement.setInt(3, taskid_hashcode);

            if (preparedStatement.executeUpdate() == 1) {
                return commit();
            }
        } catch (SQLException e) {
            rollback();
        }
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobAndTasksEvents(org.objectweb.proactive.extra.scheduler.common.job.JobEvent,
     *      java.util.List)
     */
    @Override
    public boolean setJobAndTasksEvents(JobEvent jobEvent,
        List<TaskEvent> tasksEvents) {
        // TODO Factoriser le code....
        PreparedStatement updatePreparedStatement = null;
        PreparedStatement insertPreparedStatement = null;
        PreparedStatement tmpPreparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(
                    "UPDATE JOB_AND_JOB_EVENTS SET jobevent=? WHERE jobid_hashcode=?");
            preparedStatement.setBlob(1, serialize(jobEvent));
            preparedStatement.setInt(2, jobEvent.getJobId().hashCode());
            int nb = preparedStatement.executeUpdate();
            int count = 1;

            updatePreparedStatement = connection.prepareStatement(
                    "UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskevent=? WHERE jobid_hashcode=? AND taskid_hashcode=?");

            insertPreparedStatement = connection.prepareStatement(
                    "INSERT INTO TASK_EVENTS_AND_TASK_RESULTS(taskevent,jobid_hashcode,taskid_hashcode) VALUES(?,?,?)");

            int jobid_hashcode;
            int taskid_hashcode;

            for (TaskEvent taskEvent : tasksEvents) {
                jobid_hashcode = taskEvent.getJobId().hashCode();
                taskid_hashcode = taskEvent.getTaskId().hashCode();

                if (alreadyExistInTaskTable(jobid_hashcode, taskid_hashcode)) {
                    tmpPreparedStatement = preparedStatement;
                } else {
                    tmpPreparedStatement = insertPreparedStatement;
                }

                tmpPreparedStatement.setBlob(1, serialize(taskEvent));
                tmpPreparedStatement.setInt(2, jobid_hashcode);
                tmpPreparedStatement.setInt(3, taskid_hashcode);

                nb += tmpPreparedStatement.executeUpdate();
                count++;
            }

            if (count == nb) {
                return commit();
            }
        } catch (SQLException e) {
            rollback();
        } finally {
            try {
                if (insertPreparedStatement != null) {
                    insertPreparedStatement.close();
                }
                if (updatePreparedStatement != null) {
                    updatePreparedStatement.close();
                }
                if (tmpPreparedStatement != null) {
                    tmpPreparedStatement.close();
                }
            } catch (SQLException e) {
                // Nothing to do
            }
        }
        return false;
    }
}
