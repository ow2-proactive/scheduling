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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListJobTasksCommand;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ApplicationContextImpl.class)
@PowerMockIgnore({ "javax.script.*", "com.sun.script.*", "org.fusesource.jansi.internal.Kernel32" })
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
        when(restApi.getJobTasksIds(anyString(), eq(jobId))).thenReturn(this.taskNames);

        executeTest(jobId);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[task1, task2, task3, task4]" + System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdTag() throws Exception {
        when(restApi.getJobTasksIdsByTag(anyString(), eq(jobId), eq(tag))).thenReturn(this.filteredTaskNames);
        executeTest(jobId, tag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[task1, task2]" + System.lineSeparator()));
    }

    @Test
    public void testCommandJobIdUnknownTag() throws Exception {
        when(restApi.getJobTasksIdsByTag(anyString(), eq(jobId), eq(unknownTag)))
                .thenReturn(new ArrayList<String>());

        executeTest(jobId, unknownTag);

        String out = capturedOutput.toString();
        System.out.println(out);

        assertThat(out, equalTo("[]" + System.lineSeparator()));
    }

    @Test
    public void testCommandUnknownJob() throws Exception {
        when(restApi.getJobTasksIds(anyString(), eq(unknownJobId))).thenThrow(exceptionUnknownJob);
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
        when(restApi.getJobTasksIdsByTag(anyString(), eq(unknownJobId), anyString()))
                .thenThrow(exceptionUnknownJob);
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

    @Test
    public void testJobIdOnlyFromInteractive() throws Exception {
        typeLine("listtasks(1)");
        executeTestInteractive();
        verify(restApi).getJobTasksIds(anyString(), eq("1"));
    }

    @Test
    public void testJobIdTagFromInteractive() throws Exception {
        typeLine("listtasks(1, 'LOOP-T2-1')");
        executeTestInteractive();
        verify(restApi).getJobTasksIdsByTag(anyString(), eq("1"), eq("LOOP-T2-1"));
    }

    @Override
    protected void executeCommandWithArgs(Object... args) {
        if (args.length == 1) {
            new ListJobTasksCommand((String) args[0]).execute(this.context);
        } else {
            new ListJobTasksCommand((String) args[0], (String) args[1]).execute(this.context);
        }
    }
}
