package org.objectweb.proactive.extra.scheduler.core.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;
import org.objectweb.proactive.extra.scheduler.util.DatabaseManager;


/**
 * @author FRADJ Johann
 */
public class SchedulerDB extends AbstractSchedulerDB {
    private Connection connection = null;

    /**
     * Default constructor.
     *
     * @throws SQLException
     */
    public SchedulerDB() throws SQLException {
        connection = DatabaseManager.getInstance().connect(false);
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
        //FIXME ais-je le droit de laisser assert ???
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

    // -------------------------------------------------------------------- //
    // ------------------- extends AbstractSchedulerDB -------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#disconnect()
     */
    @Override
    public void disconnect() {
        DatabaseManager.getInstance().disconnect();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addJob(org.objectweb.proactive.extra.scheduler.job.InternalJob)
     */
    @Override
    public boolean addJob(InternalJob internalJob) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#removeJob(org.objectweb.proactive.extra.scheduler.common.job.JobId)
     */
    @Override
    public boolean removeJob(JobId jobId) {
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addTaskResult(org.objectweb.proactive.extra.scheduler.common.task.TaskResult)
     */
    @Override
    public boolean addTaskResult(TaskResult taskResult) {
        // TODO Auto-generated method stub
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
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    @Override
    public boolean setTaskEvent(TaskEvent taskEvent) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobAndTasksEvents(org.objectweb.proactive.extra.scheduler.common.job.JobEvent,
     *      java.util.List)
     */
    @Override
    public boolean setJobAndTasksEvents(JobEvent jobEvent,
        List<TaskEvent> tasksEvents) {
        // TODO Auto-generated method stub
        return false;
    }
}
