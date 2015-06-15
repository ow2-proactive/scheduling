/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import org.junit.Assert;
import org.junit.Test;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


public class TestTaskIdImpl {

    /**
     * Test related to issue ow2-proactive/scheduling#1993
     * <p/>
     * Integer overflow can occur if scaleFactor or jobId is not a long.
     */
    @Test
    public void testIntegerOverflowWithTaskId() {
        long jobIdValue = 4;
        int scaleFactorValue = 1073741823;

        PASchedulerProperties.JOB_FACTOR.updateProperty(Integer.toString(scaleFactorValue));

        JobId jobId =
                new JobIdImpl(jobIdValue, "job");

        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task", 1, true);

        long expectedValue = jobIdValue * scaleFactorValue + 1;

        Assert.assertTrue(expectedValue > 0);
        Assert.assertTrue(Long.parseLong(taskId.value()) > 0);
        Assert.assertEquals(Long.toString(expectedValue), taskId.value());
    }

}
