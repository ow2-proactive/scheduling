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
package org.ow2.proactive.scheduler.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * @author ActiveEon Team
 * 
 * @see SchedulerHsqldbStarter
 */
public class SchedulerHsqldbStarterTest {

    private HsqldbServer hsqldbServerMock;

    private SchedulerHsqldbStarter schedulerHsqldbStarter;

    @Before
    public void setUp() throws IOException {
        hsqldbServerMock = Mockito.mock(HsqldbServer.class);
        schedulerHsqldbStarter = new SchedulerHsqldbStarter(hsqldbServerMock);
        schedulerHsqldbStarter = Mockito.spy(schedulerHsqldbStarter);
    }

    @Test
    public void testConfigureCatalogs() throws Exception {
        verify(hsqldbServerMock, times(2)).addCatalog(Mockito.<Path> any(), Mockito.<Path> any());

        schedulerHsqldbStarter.configureCatalogs(hsqldbServerMock, "/scheduler/home/");

        verify(hsqldbServerMock, times(4)).addCatalog(Mockito.<Path> any(), Mockito.<Path> any());
    }

    @Test
    public void testStartNotRequired() throws Exception {
        doReturn(false).when(schedulerHsqldbStarter).isServerModeRequired();

        schedulerHsqldbStarter.startIfNeeded();

        verify(schedulerHsqldbStarter).isServerModeRequired();
        verify(hsqldbServerMock, times(0)).startUp();
    }

    @Test
    public void testStartRequired() throws Exception {
        doReturn(true).when(schedulerHsqldbStarter).isServerModeRequired();
        doNothing().when(schedulerHsqldbStarter).start();

        schedulerHsqldbStarter.startIfNeeded();

        verify(schedulerHsqldbStarter).isServerModeRequired();
        verify(schedulerHsqldbStarter).start();
    }

    @Test
    public void testStopRequired() throws Exception {
        doReturn(true).when(schedulerHsqldbStarter).isRunning();
        doNothing().when(schedulerHsqldbStarter).stopImmediately();

        schedulerHsqldbStarter.stop();

        verify(schedulerHsqldbStarter).isRunning();
        verify(schedulerHsqldbStarter).stopImmediately();
    }

    @Test
    public void testStopNotRequired() throws Exception {
        doReturn(false).when(schedulerHsqldbStarter).isRunning();

        schedulerHsqldbStarter.stop();

        verify(schedulerHsqldbStarter).isRunning();
        verify(schedulerHsqldbStarter, times(0)).stopImmediately();
    }

}
