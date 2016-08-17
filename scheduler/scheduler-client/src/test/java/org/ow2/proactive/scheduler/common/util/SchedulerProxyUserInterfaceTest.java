package org.ow2.proactive.scheduler.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.JobIdImpl;


public class SchedulerProxyUserInterfaceTest {

    private SchedulerProxyUserInterface schedulerProxyUserInterface;

    private Scheduler uischedulerMock;

    @Before
    public void init() throws Exception {
        this.schedulerProxyUserInterface = new SchedulerProxyUserInterface();
        uischedulerMock = Mockito.mock(Scheduler.class);
        this.schedulerProxyUserInterface.uischeduler = uischedulerMock;
    }

    @Test
    public void testChangeStartAt() throws NotConnectedException, UnknownJobException, PermissionException {

        JobId jobId = JobIdImpl.makeJobId("66");
        String startAt = "2017-07-07T00:00:00+01:00";

        when(uischedulerMock.changeStartAt(jobId, startAt)).thenReturn(true);

        assertThat(schedulerProxyUserInterface.changeStartAt(jobId, startAt), is(true));
    }

}
