/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.smartproxy;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.RemoteSource;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedJob;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedTask;
import org.ow2.proactive.scheduler.smartproxy.common.JobTracker;
import org.ow2.proactive_grid_cloud_portal.common.FileType;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;


/**
 * JobTracker that uses the REST API for performing actions on dataspaces.
 *
 * @author The ProActive Team
 */
public class RestJobTrackerImpl extends JobTracker {

    private IDataSpaceClient restDataSpaceClient;

    public void setRestDataSpaceClient(IDataSpaceClient restDataSpaceClient) {
        this.restDataSpaceClient = restDataSpaceClient;
    }

    @Override
    public void removeAwaitedJob(String id) {
        AwaitedJob aj = jobDatabase.getAwaitedJob(id);
        if (aj == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }

        logger.debug("Removing knowledge of job " + id);

        String pullUrl = aj.getPullURL();
        String pushUrl = aj.getPushURL();

        Path remotePullFolder = null;
        Path remotePushFolder = null;

        try {
            remotePullFolder = Paths.get(new URI(pullUrl));
            remotePushFolder = Paths.get(new URI(pushUrl));
        } catch (Exception e) {
            logger.error("Could not remove data for job " + id, e);
            return;
        }

        if (aj.isIsolateTaskOutputs()) {
            Path tmp = remotePullFolder.getParent();
            if (tmp != null) {
                remotePullFolder = tmp;
            }
        }

        Set<Path> foldersToDelete = new HashSet<>();
        foldersToDelete.add(remotePullFolder.getParent());
        if (!remotePullFolder.getParent().equals(remotePushFolder.getParent())) {
            foldersToDelete.add(remotePushFolder.getParent());
        }

        RemoteSource remoteSource = new RemoteSource(IDataSpaceClient.Dataspace.USER);
        remoteSource.setType(FileType.FOLDER);

        for (Path path : foldersToDelete) {
            String location = path.toUri().toString();

            try {
                if (!logger.isTraceEnabled()) {
                    logger.debug("Deleting directory " + location);
                    remoteSource.setPath(location);
                    restDataSpaceClient.delete(remoteSource);
                }
            } catch (NotConnectedException | PermissionException e) {
                logger.warn("Could not delete temporary files at location " + location + " .", e);
            }
        }

        jobDatabase.removeAwaitedJob(id);

        try {
           jobDatabase.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after removing job " + id, e);
        }
    }

    @Override
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

            try {
                RemoteSource remoteSource = new RemoteSource(IDataSpaceClient.Dataspace.USER, pullUrl + "/");
                remoteSource.setType(FileType.FOLDER);
                restDataSpaceClient.delete(remoteSource);
            } catch (Throwable t) {
                logger.warn("Could not remove data for task " + taskName + " of job " + id, t);
            }
        }

        awaitedJob.removeAwaitedTask(taskName);

        if (awaitedJob.getAwaitedTasks().isEmpty()) {
            removeAwaitedJob(id);
            return;
        } else {
            jobDatabase.putAwaitedJob(id, awaitedJob); // this is done to ensure persistence of the operation
        }

        try {
            jobDatabase.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after removing task Task " + taskName + " from Job" + id, e);
        }
    }

}
