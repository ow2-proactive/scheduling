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