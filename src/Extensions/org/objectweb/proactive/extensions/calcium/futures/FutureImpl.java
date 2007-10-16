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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
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
import org.objectweb.proactive.extensions.calcium.statistics.Stats;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class is an implementation of an simple future object.
 * The class is not serializable on purpose.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <R> The type of the parameter for which this future stands.
 */
public class FutureImpl<R> implements Future<R> {
    Task<R> task;
    int taskId;
    BlockingQueue<Future<R>> callback;
    FileServerClient fserver;
    File outDir;

    public FutureImpl(int taskId, FileServerClient fserver, File outRootDir) {
        this.task = null;
        this.taskId = taskId;
        this.fserver = fserver;
        this.outDir = SkeletonSystemImpl.newRandomNamedDirIn(outRootDir);
        this.outDir.mkdirs();
        SkeletonSystemImpl.checkWritableDirectory(outDir);
    }

    @Override
    public int hashCode() {
        return taskId;
    }

    public int getTaskId() {
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
    public synchronized R get() throws InterruptedException, MuscleException {
        while (!isDone()) {
            wait();
        }

        //TODO fix this exception cast!!!
        if (task.hasException()) {
            throw (MuscleException) task.getException();
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

    public synchronized void setCallBackQueue(BlockingQueue<Future<R>> callback) {
        this.callback = callback;
    }

    @Override
    public void finalize() {
        if (outDir.list().length <= 0) {
            outDir.delete();
        }
    }
}
