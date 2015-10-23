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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobResultCommand;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;

import objectFaker.DataFaker;
import objectFaker.propertyGenerator.PrefixPropertyGenerator;



public class GetJobResultCommandTest extends AbstractJobTagCommandTest {

    protected JobResultData jobResult;

    protected List<TaskResultData> taskResults;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        DataFaker<JobResultData> jobResultFaker = new DataFaker<JobResultData>(JobResultData.class);
        jobResultFaker.setGenerator("id.readableName", new PrefixPropertyGenerator("job", 1));
        jobResultFaker.setGenerator("allResults.key", new PrefixPropertyGenerator("task", 1));
        jobResultFaker.setGenerator("allResults.value.id.readableName",
                new PrefixPropertyGenerator("task", 1));

        jobResult = jobResultFaker.fake();

        DataFaker<TaskResultData> taskResultsDataFaker = new DataFaker<TaskResultData>(TaskResultData.class);
        taskResultsDataFaker.setGenerator("id.readableName", new PrefixPropertyGenerator("task", 4));

        taskResults = taskResultsDataFaker.fakeList(3);
    }

    @Test
    public void testCommandJobIdOnly() throws Exception {
        when(restApi.jobResult(anyString(), eq(jobId))).thenReturn(this.jobResult);
        executeTest(jobId);
        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, containsString("job('1') result:"));
        assertThat(out, containsString("task1 : serializedValue1"));
        assertThat(out, containsString("task2 : serializedValue2"));
        assertThat(out, containsString("task3 : serializedValue3"));
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.taskResultByTag(anyString(), eq(jobId), eq(tag))).thenReturn(this.taskResults);
        executeTest(jobId, tag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, containsString("task4 result: serializedValue1"));
        assertThat(out, containsString("task5 result: serializedValue2"));
        assertThat(out, containsString("task6 result: serializedValue3"));
    }

    @Test
    public void testCommandUnknownJob() throws Exception {
        when(restApi.jobResult(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                equalTo("An error occurred while retrieving job('2') result:" + System.lineSeparator() +
                    "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                    "You can enable debug mode for getting more information using -X or --debug option." +
                    System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdUnknownTag() throws Exception {
        when(restApi.taskResultByTag(anyString(), eq(jobId), eq(unknownTag)))
                .thenReturn(new ArrayList<TaskResultData>());
        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo(System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJobIdUnknownTag() throws Exception {
        when(restApi.taskResultByTag(anyString(), eq(unknownJobId), anyString()))
                .thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                equalTo("An error occurred while retrieving job('2') result:" + System.lineSeparator() +
                    "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                    "You can enable debug mode for getting more information using -X or --debug option." +
                    System.lineSeparator()));
    }

    @Ignore
    @Test
    public void testJobIdOnlyFromInteractive() throws Exception {
        typeLine("jobresult(1)");
        executeTestInteractive();
        verify(restApi).jobResult(anyString(), eq("1"));
    }

    @Ignore
    @Test
    public void testJobIdTagFromInteractive() throws Exception {
        typeLine("jobresult(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).taskResultByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        if (args.length == 1) {
            new GetJobResultCommand((String) args[0]).execute(this.context);
        } else {
            new GetJobResultCommand((String) args[0], (String) args[1]).execute(this.context);
        }
    }
}
