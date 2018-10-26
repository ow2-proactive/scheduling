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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobContentCommand;


public class GetJobContentCommandTest extends AbstractJobTagCommandTest {

    protected String expectedOutputJobId = "an output for all the job";

    protected String expectedOutputJobIdTag = "an output for a subset of tasks";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.taskLogByTag(anyString(), eq(jobId), eq(tag))).thenReturn(expectedOutputJobIdTag);
        executeTest(jobId);

        String out = capturedOutput.toString();
        System.out.println(out);

    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        Command command = null;
        if (args.length == 1) {
            command = new GetJobContentCommand((String) args[0]);
        } else {
            throw new RuntimeException("Too much arguemnt: " + args.length);
        }
        command.execute(context);
    }

}
