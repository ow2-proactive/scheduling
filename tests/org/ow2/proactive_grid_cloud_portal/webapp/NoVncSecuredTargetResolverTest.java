/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.webapp;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive_grid_cloud_portal.scheduler.JobOutput;
import org.ow2.proactive_grid_cloud_portal.scheduler.JobOutputAppender;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;

import java.net.InetSocketAddress;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NoVncSecuredTargetResolverTest {

    private final SchedulerProxyUserInterface schedulerMock = mock(SchedulerProxyUserInterface.class);

    @Before
    public void mockSchedulerState() throws NotConnectedException, UnknownJobException, PermissionException {
        JobState jobState = mock(JobState.class);
        when(schedulerMock.getJobState("42")).thenReturn(jobState);
        TaskState taskState = mock(TaskState.class);
        when(taskState.getName()).thenReturn("remoteVisuTask");
        TaskId taskId = mock(TaskId.class);
        when(taskId.value()).thenReturn("1");
        when(taskState.getId()).thenReturn(taskId);
        when(jobState.getHMTasks()).thenReturn(Collections.singletonMap(taskId, taskState));
    }

    @Test
    public void testSessionIdIsChecked() throws Exception {
        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve("non_existing_session", "42", "remoteVisuTask");
        assertNull(targetVncHost);
    }

    @Test
    public void testMagicStringFoundInLogs() throws Exception {
        String sessionId = SchedulerSessionMapper.getInstance().add(schedulerMock, "bob");
        when(schedulerMock.getTaskResult("42", "remoteVisuTask")).thenReturn(
                new TaskResultImpl(
                        TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1, false),
                        new byte[0],
                        new byte[0],
                        new SimpleTaskLogs("PA_REMOTE_CONNECTION;1;vnc;node.grid.com:5900", ""),
                        Collections.<String, String>emptyMap()
                )
        );

        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve(sessionId, "42", "remoteVisuTask");

        assertEquals(5900, targetVncHost.getPort());
        assertEquals("node.grid.com", targetVncHost.getHostName());
    }

    @Test
    public void testMagicStringFoundInLogs_MagicStringOnSeveralLines() throws Exception {
        String sessionId = SchedulerSessionMapper.getInstance().add(schedulerMock, "bob");
        when(schedulerMock.getTaskResult("42", "remoteVisuTask")).thenReturn(
                new TaskResultImpl(
                        TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1, false),
                        new byte[0],
                        new byte[0],
                        new SimpleTaskLogs("PA_REMOTE_CONNECTION\nPA_REMOTE_CONNECTION;1;vnc;node.grid.com:5900", ""),
                        Collections.<String, String>emptyMap()
                )
        );

        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve(sessionId, "42", "remoteVisuTask");

        assertEquals(5900, targetVncHost.getPort());
        assertEquals("node.grid.com", targetVncHost.getHostName());
    }

    @Test
    public void testMagicStringFoundInLiveLogs_TaskNotFinished() throws Exception {
        String sessionId = SchedulerSessionMapper.getInstance().add(schedulerMock, "bob");
        SchedulerSessionMapper.getInstance().getSchedulerSession(sessionId).setJobOutputAppender(createLiveLogs("PA_REMOTE_CONNECTION;1;vnc;node.grid.com:5900"));
        when(schedulerMock.getTaskResult("42", "remoteVisuTask")).thenReturn(null);

        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve(sessionId, "42", "remoteVisuTask");

        assertEquals(5900, targetVncHost.getPort());
        assertEquals("node.grid.com", targetVncHost.getHostName());
    }

    @Test
    public void testMagicStringFoundInLiveLogs() throws Exception {
        String sessionId = SchedulerSessionMapper.getInstance().add(schedulerMock, "bob");
        SchedulerSessionMapper.getInstance().getSchedulerSession(sessionId).setJobOutputAppender(createLiveLogs("[Visualization_task@node2;10:38:06]PA_REMOTE_CONNECTION;1;vnc;node.grid.com:5900"));
        when(schedulerMock.getTaskResult("42", "remoteVisuTask")).thenReturn(
                new TaskResultImpl(
                        TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1, false),
                        new byte[0],
                        new byte[0],
                        new SimpleTaskLogs("", ""),
                        Collections.<String, String>emptyMap()
                )
        );

        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve(sessionId, "42", "remoteVisuTask");

        assertEquals(5900, targetVncHost.getPort());
        assertEquals("node.grid.com", targetVncHost.getHostName());
    }

    @Test
    public void testMagicStringFoundInLiveLogs_MagicStringOnSeveralLines() throws Exception {
        String sessionId = SchedulerSessionMapper.getInstance().add(schedulerMock, "bob");
        SchedulerSessionMapper.getInstance().getSchedulerSession(sessionId).setJobOutputAppender(createLiveLogs("PA_REMOTE_CONNECTION\nPA_REMOTE_CONNECTION;1;vnc;node.grid.com:5900 "));
        when(schedulerMock.getTaskResult("42", "remoteVisuTask")).thenReturn(
                new TaskResultImpl(
                        TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1, false),
                        new byte[0],
                        new byte[0],
                        new SimpleTaskLogs("", ""),
                        Collections.<String, String>emptyMap()
                )
        );

        InetSocketAddress targetVncHost = new NoVncSecuredTargetResolver().doResolve(sessionId, "42", "remoteVisuTask");

        assertEquals(5900, targetVncHost.getPort());
        assertEquals("node.grid.com", targetVncHost.getHostName());
    }

    private JobOutputAppender createLiveLogs(String logs) throws Exception {
        JobOutputAppender jobOutputAppender = mock(JobOutputAppender.class);
        JobOutput jobOutput = new JobOutput();
        jobOutput.log(logs);
        when(jobOutputAppender.getJobOutput()).thenReturn(jobOutput);
        return jobOutputAppender;
    }
}
