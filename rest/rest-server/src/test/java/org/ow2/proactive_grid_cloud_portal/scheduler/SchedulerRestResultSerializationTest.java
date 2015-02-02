package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rossigneux on 30/01/15.
 */
public class SchedulerRestResultSerializationTest extends RestTestServer {

    private SchedulerRestInterface client;
    private SchedulerProxyUserInterface scheduler;
    private String sessionId;

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new SchedulerStateRest());
    }

    @Before
    public void setUp() throws Exception {
        client = ProxyFactory.create(SchedulerRestInterface.class, "http://localhost:" + port + "/");
        scheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(scheduler);
    }

    @Test
    public void killTask() throws Exception {
        when(scheduler.killTask("123", "Test")).thenReturn(true);
        boolean result = client.killTask(sessionId, "123", "Test");
        assertTrue(result);
    }

    @Test
    public void preemptTask() throws Exception {
        when(scheduler.preemptTask("123", "Test", 5)).thenReturn(true);
        boolean result = client.preemptTask(sessionId, "123", "Test");
        assertTrue(result);
    }

    @Test
    public void restartTask() throws Exception {
        when(scheduler.restartTask("123", "Test", 5)).thenReturn(true);
        boolean result = client.restartTask(sessionId, "123", "Test");
        assertTrue(result);
    }
}