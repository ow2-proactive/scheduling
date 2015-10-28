/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobOutputCommand;



public class GetJobOutputCommandTest extends AbstractJobTagCommandTest {

    protected String expectedOutputJobId = "an output for all the job";

    protected String expectedOutputJobIdTag = "an output for a subset of tasks";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCommandJobIdOnly() throws Exception {
        when(restApi.jobLogs(anyString(), eq(jobId))).thenReturn(expectedOutputJobId);
        executeTest(jobId);
        String out = capturedOutput.toString();
        assertThat(out, equalTo(expectedOutputJobId + System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.taskLogByTag(anyString(), eq(jobId), eq(tag))).thenReturn(expectedOutputJobIdTag);
        executeTest(jobId, tag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo(expectedOutputJobIdTag + System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJob() throws Exception {
        when(restApi.jobLogs(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                equalTo("An error occurred while retrieving job('2') output:" + System.lineSeparator() +
                    "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                    "You can enable debug mode for getting more information using -X or --debug option." +
                    System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdUnknownTag() throws Exception {
        when(restApi.taskLogByTag(anyString(), eq(jobId), eq(unknownTag))).thenReturn("");
        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo(System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJobIdUnknownTag() throws Exception {
        when(restApi.taskLogByTag(anyString(), eq(unknownJobId), anyString())).thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                equalTo("An error occurred while retrieving job('2') output:" + System.lineSeparator() +
                    "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                    "You can enable debug mode for getting more information using -X or --debug option." +
                    System.lineSeparator()));
    }

    @Ignore
    @Test
    public void testJobIdOnlyFromInteractive() throws Exception {
        typeLine("joboutput(1)");
        executeTestInteractive();
        verify(restApi).jobLogs(anyString(), eq("1"));
    }

    @Ignore
    @Test
    public void testJobIdTagFromInteractive() throws Exception {
        typeLine("joboutput(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).taskLogByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        Command command = null;
        if (args.length == 1) {
            command = new GetJobOutputCommand((String) args[0]);
        } else {
            command = new GetJobOutputCommand((String) args[0], (String) args[1]);
        }
        command.execute(context);
    }

}
