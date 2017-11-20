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


/**
 * @author ActiveEon Team
 * @since 23/10/2017
 */
public class InstallPackageCommandTest {

    @Mock
    private ApplicationContext currentContextMock;

    @Mock
    private SchedulerRestClient schedulerRestClientMock;

    @Mock
    private AbstractDevice deviceMock;

    @Mock
    private SchedulerRestInterface schedulerRestInterfaceMock;

    private Stack<Exception> stack;

    private String packagePath;

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

    @Test(expected = CLIException.class)
    public void testEmptyPackagePathProvided() throws Exception {

        packagePath = "";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' does not exist."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidPackagePathProvided() throws Exception {

        packagePath = "/path/to/non/existing/package";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' does not exist."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidPackageTypeProvided() throws Exception {

        packagePath = System.getProperty("user.dir") + "/src/test/java/config/empty.xml";
        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' must be a directory or a zip file."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidPackageUrlProvided() throws Exception {

        packagePath = "invalid://github.com/ow2-proactive/proactive-examples/";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' does not exist."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidPackageGithubUrlProvided() throws Exception {

        packagePath = "http://github.com/ow2-proactive/proactive-examples/non/existing/url";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' is not a reachable package URL."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testNotGithubPackageUrlProvided() throws Exception {

        packagePath = "https://github.com/ow2-proactive/scheduling/issues";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' is not a valid github URL."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testUnreachableURLProvided() throws Exception {

        packagePath = "http://githunreachable.com/ow2-proactive/proactive-examples/";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' is not a reachable package URL."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testUnreachableShortURLProvided() throws Exception {

        packagePath = "http://bit.ly/2zK7FMk";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' is not a reachable package URL."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidPackageWebbUrlProvided() throws Exception {

        packagePath = "http://www.lamsade.dauphine.fr/~cornaz/Enseignement/ALGO-JAVA/";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(),
                   is("'" + packagePath +
                      "' is not a valid URL of a proactive package as it does not contain the METADATA.json and the resources folder required for installation."));

        throw stack.get(0);

    }

    @Test(expected = CLIException.class)
    public void testInvalidGithubUrlProvided() throws Exception {

        packagePath = "https://github.com/Aminelouati/wrong_github_url/archive/master.zip";

        InstallPackageCommand installPackageCommand = new InstallPackageCommand(packagePath);
        installPackageCommand.execute(currentContextMock);
        assertThat(stack.get(0).getMessage(), is("'" + packagePath + "' is not a reachable package URL."));

        throw stack.get(0);

    }
}
