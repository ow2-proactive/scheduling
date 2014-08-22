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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.perftests.rest.jmeter.sched;

/**
 * Retrieves the job state of each job from known job-ids list. If state of job 
 * is marked as 'finished', it retrieves the result of each task of the job. 
 * Otherwise retrieves the output of each task.
 */
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.perftests.rest.json.sched.JobStateView;
import org.ow2.proactive.perftests.rest.json.sched.TaskStateView;
import org.ow2.proactive.perftests.rest.utils.HttpUtility;


public class RESTfulSchedJobClient extends BaseRESTfulSchedClient {

    private static final String PARAM_JOB_STATUS_REFRESH_TIME = "jobClientRefreshTime";

    public static final String STATUS_FINISHED = "FINISHED";

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();
        defaultParameters.addArgument(PARAM_JOB_STATUS_REFRESH_TIME, "${jobClientRefreshTime}");
        return defaultParameters;
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        setTimestamp();
        SampleResult result = new SampleResult();
        result.sampleStart();
        String[] availableJobIds = getAvailableJobIds();
        if (!isEmpty(availableJobIds)) {
            for (String jobId : availableJobIds) {
                SampleResult jobStateResult = fetchJobState(jobId);
                result.addSubResult(jobStateResult);
                if (!jobStateResult.isSuccessful()) {
                    continue;
                }
                JobStateView jobState = getObjectMapper().readValue(jobStateResult.getResponseData(),
                        JobStateView.class);
                for (TaskStateView taskState : jobState.getTasks().values()) {
                    result.addSubResult(fetchTaskOutput(jobId, taskName(taskState)));
                    if (STATUS_FINISHED.equals(taskStatus(taskState))) {
                        result.addSubResult(fetchTaskResult(jobId, taskName(taskState)));
                    }
                }
            }
        } else {
            logInfo(String.format("%s: Job ids not available", Thread.currentThread().toString()));
            result.sampleEnd();
        }
        result.setSuccessful(true);
        waitForNextCycle(getTimestamp(), context.getIntParameter(PARAM_JOB_STATUS_REFRESH_TIME));
        return result;
    }

    private SampleResult fetchTaskResult(String jobId, String taskName) {
        String resourceUrl = (new StringBuilder(getConnection().getUrl())).append("/jobs/").append(jobId)
                .append("/tasks/").append(encodePathElement(taskName)).append("/result").toString();
        String resource = String.format("TASK-RESULT(%s:%s)", jobId, taskName);
        return getResource(getClientSession(), resource, resourceUrl);
    }

    private SampleResult fetchJobState(String jobId) {
        String resourceUrl = (new StringBuilder(getConnection().getUrl())).append("/jobs/").append(jobId)
                .toString();
        return getResource(getClientSession(), String.format("job-state('%s')", jobId), resourceUrl);

    }

    private SampleResult fetchTaskOutput(String jobId, String taskName) {
        String resourceUrl = (new StringBuilder(getConnection().getUrl())).append("/jobs/").append(jobId)
                .append("/tasks/").append(encodePathElement(taskName)).append("/result/log/all").toString();
        return getResource(getClientSession(), String.format("task-output(%s:%s)", jobId, taskName),
                resourceUrl);
    }

    private String taskName(TaskStateView taskState) {
        return taskState.getTaskInfo().getTaskId().getReadableName();
    }

    private String taskStatus(TaskStateView taskState) {
        return taskState.getTaskInfo().getTaskStatus();
    }

    private String encodePathElement(String unescaped) {
        try {
            return URIUtil.encodePath(unescaped, HttpUtility.DFLT_CHARSET);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }
}
