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
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobInfo;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.toTaskResult;

import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;


public class JobResultImpl implements JobResult {
    private static final long serialVersionUID = 1L;

    private JobId jobId;

    private JobInfoData jobInfo;

    private Map<String, TaskResult> allResults;

    private Map<String, TaskResult> preciousResults;

    private Map<String, TaskResult> exceptionResults;

    JobResultImpl(JobResultData data) {
        JobIdData id = data.getId();
        jobId = new JobIdImpl(id.getId(), id.getReadableName());
        allResults = createTaskResultMap(data.getAllResults());
        preciousResults = createTaskResultMap(data.getPreciousResults());
        exceptionResults = createTaskResultMap(data.getExceptionResults());
        jobInfo = data.getJobInfo();
    }

    private Map<String, TaskResult> createTaskResultMap(Map<String, TaskResultData> inputDataMap) {
        Map<String, TaskResult> map = new HashMap<>();
        for (String taskName : inputDataMap.keySet()) {
            TaskResultData taskResultData = inputDataMap.get(taskName);
            map.put(taskName, toTaskResult(jobId, taskResultData));
        }
        return map;
    }

    @Override
    public Map<String, TaskResult> getAllResults() {
        return allResults;
    }

    @Override
    public Map<String, TaskResult> getExceptionResults() {
        return exceptionResults;
    }

    @Override
    public JobId getJobId() {
        return jobId;
    }

    @Override
    public JobInfo getJobInfo() {
        return toJobInfo(jobInfo);
    }

    @Override
    public String getName() {
        return jobId.getReadableName();
    }

    @Override
    public Map<String, TaskResult> getPreciousResults() {
        return preciousResults;
    }

    @Override
    public TaskResult getResult(String taskName) throws UnknownTaskException {
        return allResults.get(taskName);
    }

    @Override
    public boolean hadException() {
        return exceptionResults.size() > 0;
    }

    @Override
    public void removeResult(String taskName) throws UnknownTaskException {
        allResults.remove(taskName);

    }

}
