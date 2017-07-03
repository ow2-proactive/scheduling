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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;


public class SubmitJobCommandTest {
    @Mock
    private ApplicationContext currentContextMock;

    @Mock
    private SchedulerRestClient schedulerRestClientMock;

    @Mock
    private SchedulerRestInterface schedulerRestInterfaceMock;

    @Mock
    private AbstractDevice deviceMock;

    @Mock
    private File jobFileMock;

    @Mock
    private JobKeyValueTransformer jobKeyValueTransormerMock;

    @Mock
    private JobIdData jobIdDataMock;

    @Mock
    private FileInputStream fileInputStreamMock;

    private Stack<Exception> stack;

    private String[] params;

    @Before
    public void init() {
        stack = new Stack<Exception>();
        MockitoAnnotations.initMocks(this);

        Mockito.when(currentContextMock.getSessionId()).thenReturn("sessionid");
        Mockito.when(currentContextMock.getDevice()).thenReturn(deviceMock);
        Mockito.when(currentContextMock.getRestClient()).thenReturn(schedulerRestClientMock);
        Mockito.when(schedulerRestClientMock.getScheduler()).thenReturn(schedulerRestInterfaceMock);
        Mockito.when(currentContextMock.resultStack()).thenReturn(stack);
    }

    @Test
    public void testFilePathProvided() throws Exception {

        params = new String[2];
        params[0] = new String();
        params[0] = "a";

        String contentType = URLConnection.getFileNameMap().getContentTypeFor(params[0]);

        Mockito.when(schedulerRestClientMock.submitJobArchive("sessionid",
                                                              fileInputStreamMock,
                                                              jobKeyValueTransormerMock.transformVariablesToMap("")))
               .thenReturn(null);

        new SubmitJobCommand(params).execute(currentContextMock);

        Mockito.verify(schedulerRestClientMock).submitJobArchive("sessionid", null, null);
    }

    @Test(expected = CLIException.class)
    public void testInvalidFilePathProvided() throws Exception {

        params = new String[1];
        params[0] = "/test/a.xm";

        new SubmitJobCommand(params).execute(currentContextMock);

        assertThat(stack.get(0).getMessage(), is("'" + params[0] + "' is not a valid file."));

        Mockito.verify(schedulerRestClientMock, times(0)).submitJobArchive(anyString(), anyObject(), anyObject());

        throw stack.get(0);
    }

    @Test(expected = CLIException.class)
    public void testNonExistingFilePathProvided() throws Exception {

        params = new String[1];
        params[0] = "/Users/c.xml";

        new SubmitJobCommand(params).execute(currentContextMock);

        assertThat(stack.get(0).getMessage(), is("'" + params[0] + "' does not exist."));

        Mockito.verify(schedulerRestClientMock, times(0)).submitJobArchive(anyString(), anyObject(), anyObject());

        throw stack.get(0);
    }

    @Test(expected = CLIException.class)
    public void testFileIsEmpty() throws Exception {

        params = new String[1];
        params[0] = "/Users/zeinebzhioua/Desktop/ActiveEon/scheduling/rest/rest-cli/src/test/java/org/ow2/proactive_grid_cloud_portal/cli/cmd/sched/a.xml";

        new SubmitJobCommand(params).execute(currentContextMock);

        assertThat(stack.get(0).getMessage(), is("'" + params[0] + "' is empty."));

        Mockito.verify(schedulerRestClientMock, times(0)).submitJobArchive(anyString(), anyObject(), anyObject());

        throw stack.get(0);
    }
}
