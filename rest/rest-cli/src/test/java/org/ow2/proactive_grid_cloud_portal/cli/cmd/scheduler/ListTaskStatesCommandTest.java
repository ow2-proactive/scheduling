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

import objectFaker.DataFaker;
import objectFaker.propertyGenerator.FixedPropertyGenerator;
import objectFaker.propertyGenerator.PrefixPropertyGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListTaskStatesCommand;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ApplicationContextImpl.class)
@PowerMockIgnore({"javax.script.*", "com.sun.script.*", "org.fusesource.jansi.internal.Kernel32"})
public class ListTaskStatesCommandTest extends AbstractJobTagCommandTest{


    protected DataFaker<TaskStateData> taskStateFaker;

    protected List<TaskStateData> taskData;

    protected List<TaskStateData> taskDataFiltered;

    @Before
    public void setUp() throws Exception{
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
    public void testCommandJobIdOnly() throws Exception{
        when(restApi.getJobTaskStates(anyString(), eq(jobId))).thenReturn(taskData);
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
    public void testCommandJobIdTag() throws Exception{
        when(restApi.getJobTaskStatesByTag(anyString(), eq(jobId), eq(tag))).thenReturn(taskDataFiltered);
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
    public void testCommandJobIdUnknownTag() throws Exception{
        when(restApi.getJobTaskStatesByTag(anyString(), eq("1"), eq("unknownTag")))
                .thenReturn(new ArrayList<TaskStateData>());

        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, not(containsString("task")));
        assertThat(out, containsString("ID     NAME"));
    }


    @Test
    public void testCommandUnknownJobIdUnknownTag() throws Exception{
        when(restApi.getJobTaskStatesByTag(anyString(), eq(unknownJobId), eq(unknownTag)))
                .thenThrow(exceptionUnknownJob);

        executeTest(unknownJobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("An error occurred while retrieving job('2') state:\r\n" + "" +
                "Error Message: Job 2 does not exists\r\n"));
    }


    @Test
    public void testCommandUnknownJob() throws Exception{
        when(restApi.getJobTaskStates(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);

        executeTest(unknownJobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("An error occurred while retrieving job('2') state:\r\n" + "" +
                "Error Message: Job 2 does not exists\r\n"));
    }


    @Test
    public void testJobIdOnlyFromInteractive() throws Exception{
        typeLine("taskstates(1)");
        executeTestInteractive();
        verify(restApi).getJobTaskStates(anyString(), eq("1"));
    }


    @Test
    public void testJobIdTagFromInteractive() throws Exception{
        typeLine("taskstates(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).getJobTaskStatesByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }


    @Override
    protected void executeCommandWithArgs(Object... args) {
        if(args.length == 1){
            new ListTaskStatesCommand((String) args[0]).execute(this.context);
        }
        else{
            new ListTaskStatesCommand((String) args[0], (String) args[1]).execute(this.context);
        }
    }
}
