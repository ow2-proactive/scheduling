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
package org.ow2.proactive.scheduler.smartproxy;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedJob;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedTask;
import org.ow2.proactive.scheduler.smartproxy.common.JobTracker;

import com.google.common.base.Throwables;


/**
 * JobTracker implementation that use active objects and VFS for
 * performing actions on dataspaces.
 *
 * @author The ProActive Team
 */
public class JobTrackerImpl extends JobTracker {

    // TODO: is the FileSystemManager threadSafe ? Do we need to create one
    // instance per thread ?
    // See https://issues.apache.org/jira/browse/VFS-98
    /**
     * The VFS {@link org.apache.commons.vfs2.FileSystemManager} used for file transfer
     */
    protected transient FileSystemManager fsManager;

    public JobTrackerImpl() {
        try {
            fsManager = VFSFactory.createDefaultFileSystemManager();
        } catch (FileSystemException e) {
            logger.error("Could not create Default FileSystem Manager", e);
            Throwables.propagate(e);
        }
    }

    /**
     * Removes from the proxy knowledge all info related with the given job.
     * This will also delete every folder created by the job in the shared input and output spaces
     *
     * @param id jobID
     */
    public void removeAwaitedJob(String id) {
        AwaitedJob aj = jobDatabase.getAwaitedJob(id);

        if (aj == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }
        logger.debug("Removing knowledge of job " + id);

        String pullUrl = aj.getPullURL();
        String pushUrl = aj.getPushURL();

        FileObject remotePullFolder = null;
        FileObject remotePushFolder = null;

        try {
            remotePullFolder = resolveFile(pullUrl);
            remotePushFolder = resolveFile(pushUrl);
        } catch (Exception e) {
            logger.error("Could not remove data for job " + id, e);
            return;
        }
        if (aj.isIsolateTaskOutputs()) {
            try {
                remotePullFolder = remotePullFolder.getParent();
            } catch (FileSystemException e) {
                logger.error("Could not get the parent of folder " + remotePullFolder, e);
            }
        }

        Set<FileObject> foldersToDelete = new HashSet<>();
        try {
            foldersToDelete.add(remotePullFolder.getParent());
            if (!remotePullFolder.getParent().equals(remotePushFolder.getParent()))
                foldersToDelete.add(remotePushFolder.getParent());
        } catch (FileSystemException e) {
            logger.warn("Data in folders " + pullUrl + " and " + pushUrl +
                        " cannot be deleted due to an unexpected error ", e);
        }

        String url = "NOT YET DEFINED";
        for (FileObject fo : foldersToDelete) {
            try {
                url = fo.getURL().toString();

                if (!logger.isTraceEnabled()) {
                    logger.debug("Deleting directory " + url);
                    fo.delete(Selectors.SELECT_ALL);
                    fo.delete();
                }
            } catch (FileSystemException e) {
                logger.warn("Could not delete temporary files at location " + url + " .", e);
            }
        }

        jobDatabase.removeAwaitedJob(id);

        try {
            jobDatabase.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after removing job " + id, e);
        }
    }

    /**
     * Removes from the proxy knowledge all info related with the given task.
     * If all tasks of a job have been removed this way, the job itself will be removed.
     *
     * @param id    jobID
     * @param taskName task name
     */
    public void removeAwaitedTask(String id, String taskName) {
        AwaitedJob awaitedJob = jobDatabase.getAwaitedJob(id);
        if (awaitedJob == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }

        AwaitedTask awaitedTask = awaitedJob.getAwaitedTask(taskName);
        if (awaitedTask == null) {
            logger.warn("Task " + taskName + " from Job " + id + " not in the awaited list");
            return;
        }

        logger.debug("Removing knowledge of task " + taskName + " from job " + id);
        if (awaitedJob.isIsolateTaskOutputs() && awaitedTask.getTaskId() != null) {
            // If the output data as been isolated in a dedicated folder we can delete it.

            String pullUrl = awaitedJob.getPullURL();
            pullUrl = pullUrl.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                                      SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + awaitedTask.getTaskId());

            FileObject remotePullFolder = null;

            try {
                remotePullFolder = resolveFile(pullUrl);
                logger.debug("Deleting directory " + remotePullFolder);
                remotePullFolder.delete(Selectors.SELECT_ALL);
                remotePullFolder.delete();
            } catch (Exception e) {
                logger.warn("Could not remove data for task " + taskName + " of job " + id, e);
            }

        }

        awaitedJob.removeAwaitedTask(taskName);

        if (awaitedJob.getAwaitedTasks().isEmpty()) {
            removeAwaitedJob(id);
            return;
        } else {
            // this is done to ensure persistence of the operation
            jobDatabase.putAwaitedJob(id, awaitedJob);
        }

        try {
            jobDatabase.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after removing task Task " + taskName + " from Job" + id, e);
        }
    }

    public FileObject resolveFile(String url) throws FileSystemException {
        return fsManager.resolveFile(url);
    }

}
