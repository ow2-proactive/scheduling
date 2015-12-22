package org.ow2.proactive.scheduler.core;

import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;

import java.security.KeyException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class SchedulerFrontendTest {

    /**
     * Related to issue #1849.
     * <p>
     * https://github.com/ow2-proactive/scheduling/issues/1849
     */
    @Test
    public void testConnection() throws KeyException, AlreadyConnectedException {
        SchedulerFrontendState frontendState = mock(SchedulerFrontendState.class);
        SchedulerSpacesSupport spacesSupport = mock(SchedulerSpacesSupport.class);

        SchedulerFrontend schedulerFrontend = new SchedulerFrontend(frontendState, spacesSupport);
        schedulerFrontend.connect(new UniqueID(), new UserIdentificationImpl("admin"), null);

        Mockito.verify(spacesSupport, times(1)).registerUserSpace("admin");
    }

}