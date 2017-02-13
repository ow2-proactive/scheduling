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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;


public class TestLoadJobPeriod extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        final long currrentTime = System.currentTimeMillis();

        final long hour = 1000 * 60 * 60;
        final long day = 1000 * 60 * 60 * 24;

        // add one not finished job
        defaultSubmitJob(new TaskFlowJob());

        addFinishedJob(currrentTime - hour);
        addFinishedJob(currrentTime - 2 * hour);
        addFinishedJob(currrentTime - 3 * hour);
        addFinishedJob(currrentTime - 2 * day);
        addFinishedJob(currrentTime - 10 * day);

        Assert.assertEquals(5, dbManager.loadFinishedJobs(false, -1).size());
        Assert.assertEquals(3, dbManager.loadFinishedJobs(false, day).size());
        Assert.assertEquals(4, dbManager.loadFinishedJobs(false, 3 * day).size());
        Assert.assertEquals(3, dbManager.loadFinishedJobs(false, 4 * hour).size());
    }

    private void addFinishedJob(long submittedTime) throws Exception {
        InternalJob job = defaultSubmitJob(new TaskFlowJob(), DEFAULT_USER_NAME, submittedTime);
        job.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

    }
}
