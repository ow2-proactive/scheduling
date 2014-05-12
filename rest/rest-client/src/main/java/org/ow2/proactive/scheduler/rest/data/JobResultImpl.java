/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.rest.data.DataUtility.toTaskResult;

import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;

public class JobResultImpl implements JobResult {
	private static final long serialVersionUID = 1L;
	
	private JobId jobId;
	private Map<String, TaskResult> allResults;

	JobResultImpl(JobResultData data) {
		JobIdData id = data.getId();
		jobId = new JobIdImpl(id.getId(), id.getReadableName());
		allResults = new HashMap<String, TaskResult>();
		Map<String, TaskResultData> allResultsData = data.getAllResults();
		for (String taskName : allResultsData.keySet()) {
			TaskResultData taskResultData = allResultsData.get(taskName);
			allResults.put(taskName, toTaskResult(jobId, taskResultData));
		}
	}

	@Override
	public Map<String, TaskResult> getAllResults() {
		return allResults;
	}

	@Override
	public Map<String, TaskResult> getExceptionResults() {
		throw new UnsupportedOperationException();
	}

	@Override
	public JobId getJobId() {
		return jobId;
	}

	@Override
	public JobInfo getJobInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, TaskResult> getPreciousResults() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskResult getResult(String arg0) throws UnknownTaskException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hadException() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeResult(String arg0) throws UnknownTaskException {
		throw new UnsupportedOperationException();

	}

}
