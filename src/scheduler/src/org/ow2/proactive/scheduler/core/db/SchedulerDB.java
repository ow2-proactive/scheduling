/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.DatabaseManager;


/**
 * SchedulerDB...
 * 
 * @author The ProActive Team
 */
public class SchedulerDB extends AbstractSchedulerDB {
    private Connection connection = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private String configFile = null;

    /**
     * Default constructor.
     *
     * @param configFile the file that contains the description of the database.
     * @throws SQLException
     */
    public SchedulerDB(String configFile) throws SQLException {
        this.configFile = configFile;
        connection = DatabaseManager.getInstance(configFile).connect(false);
        connection.setAutoCommit(false);
        statement = connection.createStatement();
        System.out.println("[SCHEDULER-DATABASE] instance ok !");
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- private ------------------------------- //
    // -------------------------------------------------------------------- //
    private Blob serialize(Object o) {
        try {
            return new SerialBlob(ObjectToByteConverter.ObjectStream.convert(o));
        } catch (SerialException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object deserialize(Blob blob) {
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

    private boolean commit() {
        try {
            connection.commit();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            rollback();
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
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#disconnect()
     */
    @Override
    public void disconnect() {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // Nothing to do
        }

        DatabaseManager.getInstance(configFile).disconnect();
        System.out.println("[SCHEDULER-DATABASE] disconnect");
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#delete()
     */
    @Override
    public void delete() {
        try {
            statement.execute("DROP TABLE " + AbstractSchedulerDB.TASK_TABLE_NAME);
            statement.execute("DROP TABLE " + AbstractSchedulerDB.JOB_TABLE_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getURL()
     */
    @Override
    public String getURL() {
        try {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            return "unknown";
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#addJob(org.ow2.proactive.scheduler.job.InternalJob)
     */
    @Override
    public boolean addJob(InternalJob job) {
        System.out.println("[SCHEDULER-DATABASE] addjob");

        try {
            int jobid_hashcode = job.getId().hashCode();

            //Add job in table
            preparedStatement = connection
                    .prepareStatement("INSERT INTO JOB_AND_JOB_EVENTS(jobid_hashcode, job) VALUES (?,?)");
            preparedStatement.setInt(1, jobid_hashcode);
            preparedStatement.setBlob(2, serialize(job));

            int nb = preparedStatement.executeUpdate();
            int count = 1;

            // Add all future taskResult with the precious property
            preparedStatement = connection
                    .prepareStatement("INSERT INTO TASK_EVENTS_AND_TASK_RESULTS(jobid_hashcode,taskid_hashcode,precious) VALUES(?,?,?)");

            Map<TaskId, InternalTask> map = job.getHMTasks();
            Iterator<TaskId> iter = map.keySet().iterator();
            TaskId taskId = null;
            InternalTask task = null;

            while (iter.hasNext()) {
                taskId = iter.next();
                task = map.get(taskId);

                preparedStatement.setInt(1, jobid_hashcode);
                preparedStatement.setInt(2, task.getId().hashCode());
                preparedStatement.setBoolean(3, task.isPreciousResult());
                count++;
                nb += preparedStatement.executeUpdate();
            }

            if (count == nb)
                return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#removeJob(org.ow2.proactive.scheduler.common.job.JobId)
     */
    @Override
    public boolean removeJob(JobId jobId) {
        System.out.println("[SCHEDULER-DATABASE] remove");

        try {
            statement.execute("DELETE FROM TASK_EVENTS_AND_TASK_RESULTS WHERE jobid_hashcode=" +
                jobId.hashCode());
            statement.execute("DELETE FROM JOB_AND_JOB_EVENTS WHERE jobid_hashcode=" + jobId.hashCode());

            return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#addTaskResult(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public boolean addTaskResult(TaskResult taskResult) {
        System.out.println("[SCHEDULER-DATABASE] addTaskResult");

        try {
            preparedStatement = connection
                    .prepareStatement("UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskresult=? WHERE jobid_hashcode=? AND taskid_hashcode=?");

            preparedStatement.setBlob(1, serialize(taskResult));
            preparedStatement.setInt(2, taskResult.getTaskId().getJobId().hashCode());
            preparedStatement.setInt(3, taskResult.getTaskId().hashCode());

            if (preparedStatement.executeUpdate() == 1)
                return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    @Override
    public JobResult getJobResult(JobId jobId) {
        System.out.println("[SCHEDULER-DATABASE] getJobResult");
        JobResultImpl result = new JobResultImpl(jobId);

        ResultSet rs = null;
        Blob blob = null;

        try {
            rs = statement
                    .executeQuery("SELECT taskresult, precious FROM TASK_EVENTS_AND_TASK_RESULTS WHERE jobid_hashcode=" +
                        jobId.hashCode());

            TaskResult taskResult = null;
            while (rs.next()) {
                blob = rs.getBlob(1);
                if (blob != null) {
                    taskResult = (TaskResult) deserialize(blob);
                    result.addTaskResult(taskResult.getTaskId().getReadableName(), taskResult, rs
                            .getBoolean(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Nothing to do
                }
            }

            /* Blob.free() is not available in Java 5 */

            //            if (blob != null) {
            //                try {
            //                    blob.free();
            //                } catch (SQLException e) {
            //                    // Nothing to do
            //                }
            //            }
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        System.out.println("[SCHEDULER-DATABASE] getRecoverableState");

        ResultSet rs = null;
        Blob blob = null;

        try {
            List<InternalJob> internalJobList = new ArrayList<InternalJob>();
            List<JobResult> jobResultList = new ArrayList<JobResult>();
            Map<JobId, InternalJob> internalHMJobList = new HashMap<JobId, InternalJob>();
            Map<JobId, JobResult> jobResultMap = new HashMap<JobId, JobResult>();
            Map<JobId, JobEvent> jobEventMap = new HashMap<JobId, JobEvent>();
            Map<TaskId, TaskEvent> taskEventMap = new HashMap<TaskId, TaskEvent>();

            rs = statement.executeQuery("SELECT job, jobevent FROM JOB_AND_JOB_EVENTS");

            while (rs.next()) {
                // Get Job
                InternalJob internalJob = (InternalJob) deserialize(rs.getBlob(1));
                internalHMJobList.put(internalJob.getId(), internalJob);
                internalJobList.add(internalJob);
                //
                jobResultMap.put(internalJob.getId(), new JobResultImpl(internalJob.getId()));
                //
                blob = rs.getBlob(2);

                if (blob != null) {
                    JobEvent jobEvent = (JobEvent) deserialize(blob);
                    jobEventMap.put(jobEvent.getJobId(), jobEvent);
                }
            }

            rs = statement.executeQuery("SELECT taskevent,taskresult FROM TASK_EVENTS_AND_TASK_RESULTS");

            while (rs.next()) {
                blob = rs.getBlob(1);

                if (blob != null) {
                    TaskEvent taskEvent = (TaskEvent) deserialize(blob);
                    taskEventMap.put(taskEvent.getTaskId(), taskEvent);

                    blob = rs.getBlob(2);

                    if (blob != null) {
                        TaskResultImpl taskResult = ((TaskResultImpl) deserialize(blob));
                        taskResult.clean();
                        jobResultMap.get(taskResult.getTaskId().getJobId()).addTaskResult(
                                taskResult.getTaskId().getReadableName(),
                                taskResult,
                                internalHMJobList.get(taskResult.getTaskId().getJobId()).getHMTasks().get(
                                        taskResult.getTaskId()).isPreciousResult());
                    }
                }
            }

            if ((internalJobList.size() == 0) && (jobResultMap.size() == 0) && (jobEventMap.size() == 0) &&
                (taskEventMap.size() == 0)) {
                return null;
            }

            Collection<JobResult> col = jobResultMap.values();

            for (JobResult jr : col)
                jobResultList.add(jr);

            return new RecoverableState(internalJobList, jobResultList, jobEventMap, taskEventMap);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Nothing to do
                }
            }

            /* Blob.free() is not available in Java 5 */

            //            if (blob != null) {
            //                try {
            //                    blob.free();
            //                } catch (SQLException e) {
            //                    // Nothing to do
            //                }
            //            }
        }

        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getTaskResult(org.ow2.proactive.scheduler.common.task.TaskId)
     */
    @Override
    public TaskResult getTaskResult(TaskId taskId) {
        System.out.println("[SCHEDULER-DATABASE] getTaskResult");
        ResultSet rs = null;
        Blob blob = null;

        try {
            rs = statement
                    .executeQuery("SELECT taskresult FROM TASK_EVENTS_AND_TASK_RESULTS WHERE taskid_hashcode=" +
                        taskId.hashCode());
            if (rs.next()) {
                blob = rs.getBlob(1);
                if (blob != null)
                    return (TaskResult) deserialize(blob);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Nothing to do
                }
            }

            /* Blob.free() is not available in Java 5 */

            //            if (blob != null) {
            //                try {
            //                    blob.free();
            //                } catch (SQLException e) {
            //                    // Nothing to do
            //                }
            //            }
        }

        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setJobEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    @Override
    public boolean setJobEvent(JobEvent jobEvent) {
        System.out.println("[SCHEDULER-DATABASE] setJobEvent");

        try {
            preparedStatement = connection
                    .prepareStatement("UPDATE JOB_AND_JOB_EVENTS SET jobevent=? WHERE jobid_hashcode=?");
            preparedStatement.setBlob(1, serialize(jobEvent));
            preparedStatement.setInt(2, jobEvent.getJobId().hashCode());

            if (preparedStatement.executeUpdate() == 1)
                return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setTaskEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    @Override
    public boolean setTaskEvent(TaskEvent taskEvent) {
        System.out.println("[SCHEDULER-DATABASE] setTaskEvent");

        try {
            preparedStatement = connection
                    .prepareStatement("UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskevent=? WHERE jobid_hashcode=? AND taskid_hashcode=?");

            preparedStatement.setBlob(1, serialize(taskEvent));
            preparedStatement.setInt(2, taskEvent.getJobId().hashCode());
            preparedStatement.setInt(3, taskEvent.getTaskId().hashCode());

            if (preparedStatement.executeUpdate() == 1)
                return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setJobAndTasksEvents(org.ow2.proactive.scheduler.common.job.JobEvent,
     *      java.util.List)
     */
    @Override
    public boolean setJobAndTasksEvents(JobEvent jobEvent, List<TaskEvent> tasksEvents) {
        System.out.println("[SCHEDULER-DATABASE] setJobAndTaskEvents");

        try {
            preparedStatement = connection
                    .prepareStatement("UPDATE JOB_AND_JOB_EVENTS SET jobevent=? WHERE jobid_hashcode=?");
            preparedStatement.setBlob(1, serialize(jobEvent));
            preparedStatement.setInt(2, jobEvent.getJobId().hashCode());

            int nb = preparedStatement.executeUpdate();
            int count = 1;

            preparedStatement = connection
                    .prepareStatement("UPDATE TASK_EVENTS_AND_TASK_RESULTS SET taskevent=? WHERE jobid_hashcode=? AND taskid_hashcode=?");

            for (TaskEvent taskEvent : tasksEvents) {
                preparedStatement.setBlob(1, serialize(taskEvent));
                preparedStatement.setInt(2, taskEvent.getJobId().hashCode());
                preparedStatement.setInt(3, taskEvent.getTaskId().hashCode());

                nb += preparedStatement.executeUpdate();
                count++;
            }

            if (count == nb)
                return commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rollback();
        return false;
    }

}