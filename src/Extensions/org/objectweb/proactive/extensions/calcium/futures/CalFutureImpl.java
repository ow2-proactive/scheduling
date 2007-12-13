/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.futures;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.statistics.Stats;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskId;


/**
 * This class is an implementation of an simple future object.
 * The class is not Serializable on purpose.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <R> The type of the parameter for which this future stands.
 */
public class CalFutureImpl<R> implements CalFuture<R> {
    Task<R> task;
    TaskId taskId;
    BlockingQueue<CalFuture<R>> callback;
    FileServerClient fserver;
    File outDir;

    public CalFutureImpl(TaskId taskId, FileServerClient fserver, File outRootDir) {
        this.task = null;
        this.taskId = taskId;
        this.fserver = fserver;
        this.outDir = SkeletonSystemImpl.newRandomNamedDirIn(outRootDir);
        this.outDir.mkdirs();
        SkeletonSystemImpl.checkWritableDirectory(outDir);
    }

    @Override
    public int hashCode() {
        return taskId.hashCode();
    }

    public TaskId getTaskId() {
        return this.taskId;
    }

    public boolean isDone() {
        return task != null;
    }

    /**
     * This method returns the result of the computation for
     * every inputed parameter. If no parameter is yet available
     * this method will block.
     *
     * @return The result of the computation on a parameter, or null if there are no more
     * parameters being computed.
     * @throws PanicException Is thrown if a unrecoverable error takes place inside the framework.
     * @throws MuscleException Is thrown if a functional exception happens during the execution
     * of the skeleton's muscle.
     */
    public synchronized R get() throws InterruptedException, MuscleException, TaskException {
        while (!isDone()) {
            wait();
        }

        if (task.hasException()) {
            Exception ex = task.getException();
            if (ex instanceof MuscleException) {
                throw (MuscleException) ex;
            } else if (ex instanceof TaskException) {
                throw (TaskException) ex;
            } else {
                throw new TaskException(ex);
            }
        }

        return task.getObject();
    }

    public Stats getStats() {
        if (!isDone()) {
            return null;
        }

        return task.getStats();
    }

    @SuppressWarnings("unchecked")
    public synchronized void setFinishedTask(Task<?> task) {
        this.task = (Task<R>) task;

        if (!task.hasException()) {
            try {
                task = FileStaging.stageOutput(fserver, task, outDir);
            } catch (Exception e) {
                task.setException(e);
            }
        }

        if (callback != null) {
            callback.add(this);
        }

        notifyAll();
    }

    public synchronized void setCallBackQueue(BlockingQueue<CalFuture<R>> callback) {
        this.callback = callback;
    }

    @Override
    public void finalize() {
        if (outDir.list().length <= 0) {
            outDir.delete();
        }
    }
}
