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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListTaskStatesCommand;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;

import objectFaker.DataFaker;
import objectFaker.propertyGenerator.FixedPropertyGenerator;
import objectFaker.propertyGenerator.PrefixPropertyGenerator;


public class ListTaskStatesCommandTest extends AbstractJobTagCommandTest {

    protected DataFaker<TaskStateData> taskStateFaker;

    protected List<TaskStateData> taskData;

    protected List<TaskStateData> taskDataFiltered;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        JobIdData jobId = jobIdFaker.fake();

        taskStateFaker = new DataFaker<TaskStateData>(TaskStateData.class);
        taskStateFaker.setGenerator("taskInfo.jobId", new FixedPropertyGenerator<JobIdData>(jobId));
        taskStateFaker.setGenerator("taskInfo.taskId.readableName", new PrefixPropertyGenerator("task", 1));
        taskStateFaker.setGenerator("name", new PrefixPropertyGenerator("task", 1));
        taskStateFaker.setGenerator("tag", new FixedPropertyGenerator("LOOP-T2-1"));

        this.taskData = this.taskStateFaker.fakeList(6);
        this.taskDataFiltered = new ArrayList<>();
        this.taskDataFiltered.add(this.taskData.get(0));
        this.taskDataFiltered.add(this.taskData.get(1));
        this.taskDataFiltered.add(this.taskData.get(2));
    }

    @Test
    public void testCommandJobIdOnly() throws Exception {
        when(restApi.getJobTaskStates(anyString(), eq(jobId))).thenReturn(new RestPage(taskData, taskData.size()));
        executeTest(jobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, containsString("ID     NAME"));
        assertThat(out, containsString("task1"));
        assertThat(out, containsString("task2"));
        assertThat(out, containsString("task3"));
        assertThat(out, containsString("task4"));
        assertThat(out, containsString("task5"));
        assertThat(out, containsString("task6"));
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.getJobTaskStatesByTag(anyString(),
                                           eq(jobId),
                                           eq(tag))).thenReturn(new RestPage(taskDataFiltered,
                                                                             taskDataFiltered.size()));
        executeTest(jobId, tag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, containsString("ID     NAME"));
        assertThat(out, containsString("task1"));
        assertThat(out, containsString("task2"));
        assertThat(out, containsString("task3"));
        assertThat(out, not(containsString("task4")));
        assertThat(out, not(containsString("task5")));
        assertThat(out, not(containsString("task6")));
    }

    @Test
    public void testCommandJobIdUnknownTag() throws Exception {
        when(restApi.getJobTaskStatesByTag(anyString(), eq("1"), eq("unknownTag")))
                                                                                   .thenReturn(new RestPage(new ArrayList<TaskStateData>(),
                                                                                                            0));

        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, not(containsString("task")));
        assertThat(out, containsString("ID     NAME"));
    }

    @Test
    public void testCommandUnknownJobIdUnknownTag() throws Exception {
        when(restApi.getJobTaskStatesByTag(anyString(),
                                           eq(unknownJobId),
                                           eq(unknownTag))).thenThrow(exceptionUnknownJob);

        executeTest(unknownJobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                   equalTo("An error occurred while retrieving job('2') state:" + System.lineSeparator() +
                           "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                           "You can enable debug mode for getting more information using -X or --debug option." +
                           System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJob() throws Exception {
        when(restApi.getJobTaskStates(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);

        executeTest(unknownJobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                   equalTo("An error occurred while retrieving job('2') state:" + System.lineSeparator() +
                           "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                           "You can enable debug mode for getting more information using -X or --debug option." +
                           System.lineSeparator()));
    }

    @Ignore
    @Test
    public void testJobIdOnlyFromInteractive() throws Exception {
        typeLine("taskstates(1)");
        executeTestInteractive();
        verify(restApi).getJobTaskStates(anyString(), eq("1"));
    }

    @Ignore
    @Test
    public void testJobIdTagFromInteractive() throws Exception {
        typeLine("taskstates(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).getJobTaskStatesByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        if (args.length == 1) {
            new ListTaskStatesCommand((String) args[0]).execute(this.context);
        } else {
            new ListTaskStatesCommand((String) args[0], (String) args[1]).execute(this.context);
        }
    }
}
