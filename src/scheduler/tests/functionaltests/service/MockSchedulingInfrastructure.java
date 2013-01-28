package functionaltests.service;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.core.SchedulerClassServers;
import org.ow2.proactive.scheduler.core.SchedulingInfrastructure;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.UserRMProxy;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.NodeSet;


public class MockSchedulingInfrastructure implements SchedulingInfrastructure {

    private final SchedulerDBManager dbManager;

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);

    private SchedulerClassServers classServers = Mockito.mock(SchedulerClassServers.class);

    private final DataSpaceServiceStarter dsStarter;

    private AtomicInteger releaseNodeCounter = new AtomicInteger();

    private RMProxiesManager rmProxiesManager;

    public MockSchedulingInfrastructure(SchedulerDBManager dbManager) throws Exception {
        this.dbManager = dbManager;
        this.dsStarter = mock(DataSpaceServiceStarter.class);

        UserRMProxy userProxy = mock(UserRMProxy.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                releaseNodeCounter.incrementAndGet();
                return null;
            }

        }).when(userProxy).releaseNodes((NodeSet) anyObject());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                releaseNodeCounter.incrementAndGet();
                return null;
            }

        }).when(userProxy).releaseNodes((NodeSet) anyObject(), (Script<?>) anyObject());

        rmProxiesManager = mock(RMProxiesManager.class);
        when(rmProxiesManager.getUserRMProxy(anyString(), (Credentials) anyObject())).thenReturn(userProxy);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    @Override
    public SchedulerClassServers getTaskClassServer() {
        return classServers;
    }

    @Override
    public DataSpaceServiceStarter getDataSpaceServiceStarter() {
        return dsStarter;
    }

    @Override
    public SchedulerDBManager getDBManager() {
        return dbManager;
    }

    @Override
    public RMProxiesManager getRMProxiesManager() {
        return rmProxiesManager;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        System.out.println("Requested to submit: " + runnable);
        Future<?> future = executorService.submit(runnable);
        try {
            future.get();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }
        return future;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        System.out.println("Requested to submit: " + task);
        Future<T> future = executorService.submit(task);
        return future;
    }

    @Override
    public void schedule(Runnable runnable, long delay) {
        System.out.println("Requested to schedule " + runnable + ", delay: " + delay);
        ScheduledFuture<?> future = executorService.schedule(runnable, 1, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Override
    public void schedule(Callable<?> task, long delay) {
        System.out.println("Requested to schedule " + task + ", delay: " + delay);
        ScheduledFuture<?> future = executorService.schedule(task, 1, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    void assertRequests(int releaseNodes) {
        Assert.assertEquals("Relase node request", releaseNodes, releaseNodeCounter.intValue());
        releaseNodeCounter.set(0);
    }

}
