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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListJobTasksCommand;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;


public class ListJobTasksCommandTest extends AbstractJobTagCommandTest {

    protected ArrayList<String> taskNames;

    protected ArrayList<String> filteredTaskNames;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.filteredTaskNames = new ArrayList<>(3);
        this.filteredTaskNames.add("task1");
        this.filteredTaskNames.add("task2");

        this.taskNames = new ArrayList<>(this.filteredTaskNames);
        this.taskNames.add("task3");
        this.taskNames.add("task4");
    }

    @After
    public void tearDown() {
        reset(this.restApi);
    }

    @Test
    public void testCommandJobIdOnly() throws Exception {
        when(restApi.getTasksNames(anyString(), eq(jobId))).thenReturn(new RestPage<String>(this.taskNames,
                                                                                            taskNames.size()));

        executeTest(jobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[task1, task2, task3, task4]" + System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.getJobTasksIdsByTag(anyString(),
                                         eq(jobId),
                                         eq(tag))).thenReturn(new RestPage<String>(this.filteredTaskNames,
                                                                                   filteredTaskNames.size()));
        executeTest(jobId, tag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[task1, task2]" + System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdUnknownTag() throws Exception {
        when(restApi.getJobTasksIdsByTag(anyString(), eq(jobId), eq(unknownTag)))
                                                                                 .thenReturn(new RestPage<String>(new ArrayList<String>(),
                                                                                                                  0));

        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[]" + System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJob() throws Exception {
        when(restApi.getTasksNames(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                   equalTo("An error occurred while retrieving job('2') tasks:" + System.lineSeparator() +
                           "Error message: Job 2 does not exists" + System.lineSeparator() + System.lineSeparator() +
                           "You can enable debug mode for getting more information using -X or --debug option." +
                           System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJobUnknownTag() throws Exception {
        when(restApi.getJobTasksIdsByTag(anyString(), eq(unknownJobId), anyString())).thenThrow(exceptionUnknownJob);
        executeTest(unknownJobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out,
                   equalTo("An error occurred while retrieving job('2') tasks filtered by tag unknownTag:" +
                           System.lineSeparator() + "Error message: Job 2 does not exists" + System.lineSeparator() +
                           System.lineSeparator() +
                           "You can enable debug mode for getting more information using -X or --debug option." +
                           System.lineSeparator()));
    }

    @Ignore
    @Test
    public void testJobIdOnlyFromInteractive() throws Exception {
        typeLine("listtasks(1)");
        executeTestInteractive();
        verify(restApi).getTasksNames(anyString(), eq("1"));
    }

    @Ignore
    @Test
    public void testJobIdTagFromInteractive() throws Exception {
        typeLine("listtasks(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).getJobTasksIdsByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        switch (args.length) {
            case 1:
                // args: jobId
                ListJobTasksCommand.LJTCommandBuilder.newInstance()
                                                     .jobId((String) args[0])
                                                     .instance()
                                                     .execute(this.context);
                break;
            case 2:
                // args: jobId and tag
                ListJobTasksCommand.LJTCommandBuilder.newInstance()
                                                     .jobId((String) args[0])
                                                     .tag((String) args[1])
                                                     .instance()
                                                     .execute(this.context);
                break;
            case 3:
                // args: jobId, offset and limit
                ListJobTasksCommand.LJTCommandBuilder.newInstance()
                                                     .jobId((String) args[0])
                                                     .offset((String) args[1])
                                                     .limit((String) args[2])
                                                     .instance()
                                                     .execute(this.context);
                break;
            default:
                // args: jobId, tag, offset and limit
                // We don't consider parameters beyond the fourth
                ListJobTasksCommand.LJTCommandBuilder.newInstance()
                                                     .jobId((String) args[0])
                                                     .tag((String) args[1])
                                                     .offset((String) args[2])
                                                     .limit((String) args[3])
                                                     .instance()
                                                     .execute(this.context);
                break;
        }
    }
}
