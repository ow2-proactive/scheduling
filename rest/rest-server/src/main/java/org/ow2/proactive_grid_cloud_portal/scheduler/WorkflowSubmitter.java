/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

import java.io.File;
import java.util.Map;

public class WorkflowSubmitter {

    private static final Logger logger = ProActiveLogger.getLogger(WorkflowSubmitter.class);

    private Scheduler scheduler;

    public WorkflowSubmitter(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Submits an XML workflow to the scheduler.
     *
     * @param workflowFile a workflow file (xml or archive)
     * @param variables    variables to be replaced in the job before submission
     * @return
     * @throws JobCreationException
     * @throws NotConnectedException
     * @throws PermissionException
     * @throws SubmissionClosedException
     */
    public JobId submit(
            File workflowFile,
            Map<String, String> variables
    ) throws PermissionRestException, JobCreationRestException, SubmissionClosedRestException, NotConnectedRestException {
        return submit(workflowFile, true, variables);
    }

    /**
     * Submits a workflow to the scheduler (XML or archive).
     *
     * @param workflowFile a workflow file (XML or archive)
     * @param isXml        flag indicating if XML workflow or archive
     * @param variables    variables to be replaced on submission
     * @return
     * @throws JobCreationException
     * @throws NotConnectedException
     * @throws PermissionException
     * @throws SubmissionClosedException
     */
    public JobId submit(
            File workflowFile,
            Boolean isXml,
            Map<String, String> variables
    ) throws NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, JobCreationRestException {

        JobId jobId;
        try {
            jobId = scheduler.submit(createJobObject(workflowFile, isXml, variables));
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (SubmissionClosedException e) {
            throw new SubmissionClosedRestException(e);
        } catch (JobCreationException e) {
            throw new JobCreationRestException(e);
        }

        storeWorkflowFile(workflowFile, !isXml, jobId);

        return jobId;
    }

    private Job createJobObject(
            File jobFile,
            Boolean isApplicationXmlJob,
            Map<String, String> jobVariables
    ) throws JobCreationException {
        Job jobObject;
        if (isApplicationXmlJob) {
            jobObject = JobFactory.getFactory().createJob(jobFile.getAbsolutePath(), jobVariables);
        } else {
            jobObject = JobFactory.getFactory().createJobFromArchive(jobFile.getAbsolutePath(), jobVariables);
        }
        return jobObject;
    }

    private void storeWorkflowFile(File workflowFile, boolean isAnArchive, JobId jobid) {
        File archiveToStore = new File(PortalConfiguration.jobIdToPath(jobid.value()));
        if (isAnArchive) {
            logger.debug("saving archive to " + archiveToStore.getAbsolutePath());
            workflowFile.renameTo(archiveToStore);
        } else {
            // the job is not an archive, however an archive file can
            // exist for this new job (due to an old submission)
            // In that case, we remove the existing file preventing erronous
            // access
            // to this previously submitted archive.

            if (archiveToStore.exists()) {
                archiveToStore.delete();
            }
        }
    }

}
