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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobIdDataAndError;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


public class WorkflowSubmitter {

    private static final Logger logger = ProActiveLogger.getLogger(WorkflowSubmitter.class);

    private static final String FILE_ENCODING = PASchedulerProperties.FILE_ENCODING.getValueAsString();

    private Scheduler scheduler;

    private SchedulerSpaceInterface space;

    private String sessionId;

    public WorkflowSubmitter(Scheduler scheduler, SchedulerSpaceInterface space, String sessionId) {
        this.scheduler = scheduler;
        this.space = space;
        this.sessionId = sessionId;
    }

    /**
     * Submit multiple workflows
     *
     * @param workflowsContents list of workflow contents
     * @param workflowsVariables variables associated with each workflow
     * @param workflowsGenericInfo generic info associated with each workflow
     * @return a list of objects containing, for each workflow, a job id or any error occurring during workflow submission
     * @throws NotConnectedException
     */
    public List<JobIdDataAndError> submitJobs(List<String> workflowsContents,
            List<Map<String, String>> workflowsVariables, List<Map<String, String>> workflowsGenericInfo)
            throws NotConnectedException {
        // answer is merged from valid results or errors occurring at this level or lower levels
        List<JobIdDataAndError> mergedAnswer = new ArrayList<>(workflowsContents.size());

        // list containing errors occurring when parsing workflows
        // if the parsing is successful, a null entry is added
        List<JobIdDataAndError> jobIdDataWorkflowParsing = new ArrayList<>(workflowsContents.size());
        List<Job> jobs = new ArrayList<>(workflowsContents.size());
        for (int i = 0; i < workflowsContents.size(); i++) {
            String workflowContent = workflowsContents.get(i);
            if (workflowContent != null) {
                Map<String, String> variables = workflowsVariables.get(i);
                Map<String, String> genericInfos = workflowsGenericInfo.get(i);
                try (InputStream workflowStream = IOUtils.toInputStream(workflowContent,
                                                                        Charset.forName(FILE_ENCODING))) {

                    Job job = createJobObject(workflowStream, variables, genericInfos);
                    jobs.add(job);
                    jobIdDataWorkflowParsing.add(null);
                } catch (Exception e) {
                    jobs.add(null);
                    jobIdDataWorkflowParsing.add(new JobIdDataAndError(e.getMessage(),
                                                                       StackTraceUtil.getStackTrace(e)));
                }
            } else {
                jobs.add(null);
                jobIdDataWorkflowParsing.add(null);
            }
        }
        List<JobIdDataAndError> jobidsReturnedByScheduler = scheduler.submit(jobs);

        for (int i = 0; i < workflowsContents.size(); i++) {
            if (jobIdDataWorkflowParsing.get(i) != null) {
                mergedAnswer.add(jobIdDataWorkflowParsing.get(i));
            } else {
                mergedAnswer.add(jobidsReturnedByScheduler.get(i));
            }
        }

        for (int i = 0; i < workflowsContents.size(); i++) {
            if (mergedAnswer.get(i) != null) {
                String visualization = jobs.get(i).getVisualization();
                File visualizationFile = new File(PortalConfiguration.jobIdToPath("" + mergedAnswer.get(i).getId()) +
                                                  ".html");
                try {
                    Files.deleteIfExists(visualizationFile.toPath());
                    if (visualization != null && !visualization.isEmpty()) {
                        FileUtils.write(new File(visualizationFile.getAbsolutePath()),
                                        jobs.get(i).getVisualization(),
                                        Charset.forName(FILE_ENCODING));
                    }
                } catch (IOException e) {
                    logger.warn("Error writing visualization file", e);
                }
            }
        }

        return mergedAnswer;
    }

    /**
     * Submits a workflow to the scheduler (XML or archive).
     * It also creates a job visualization HTML file.
     *
     * @param workflowStream a workflow as input stream
     * @param variables variables to be replaced on submission
     * @param genericInfos generic informations to be replaced on submission
     * @return job ID of the job created for the specified workflow and associated variables.
     * @throws JobCreationRestException
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    public JobId submit(InputStream workflowStream, Map<String, String> variables, Map<String, String> genericInfos)
            throws NotConnectedRestException, PermissionRestException, SubmissionClosedRestException,
            JobCreationRestException {
        try {
            long t0 = System.currentTimeMillis();
            Job job = createJobObject(workflowStream, variables, genericInfos);
            long t1 = System.currentTimeMillis();
            JobId jobId = scheduler.submit(job);
            long t2 = System.currentTimeMillis();
            // Create Job's SVG visualization file
            String visualization = job.getVisualization();
            File visualizationFile = new File(PortalConfiguration.jobIdToPath(jobId.value()) + ".html");
            long t3 = System.currentTimeMillis();
            Files.deleteIfExists(visualizationFile.toPath());
            if (visualization != null && !visualization.isEmpty()) {
                FileUtils.write(new File(visualizationFile.getAbsolutePath()),
                                job.getVisualization(),
                                Charset.forName(FILE_ENCODING));
            }
            long d1 = t1 - t0;
            long d2 = t2 - t1;
            long d3 = t3 - t2;
            logger.debug(String.format("timer;%d;%d;%d;%d", jobId.longValue(), d1, d2, d3));
            return jobId;
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (SubmissionClosedException e) {
            logger.warn("Could not submit job", e);
            throw new SubmissionClosedRestException(e);
        } catch (JobCreationException e) {
            logger.warn("Could not submit job", e);
            throw new JobCreationRestException(e);
        } catch (Exception e) {
            logger.warn("Unexpected error when submitting job", e);
            throw new JobCreationRestException(e);
        }
    }

    private Job createJobObject(InputStream workflowStream, Map<String, String> jobVariables,
            Map<String, String> jobGenericInfos) throws JobCreationException {
        return JobFactory.getFactory().createJob(workflowStream,
                                                 jobVariables,
                                                 jobGenericInfos,
                                                 scheduler,
                                                 space,
                                                 sessionId);
    }

}
