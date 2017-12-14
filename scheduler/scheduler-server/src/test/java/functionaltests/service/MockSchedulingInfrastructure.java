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
package functionaltests.service;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.core.SchedulerSpacesSupport;
import org.ow2.proactive.scheduler.core.SchedulingInfrastructure;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.NodeSet;


public class MockSchedulingInfrastructure implements SchedulingInfrastructure {

    private final SchedulerDBManager dbManager;

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(5);

    private final ExecutorService executorService;

    private final DataSpaceServiceStarter dsStarter;

    private AtomicInteger releaseNodeCounter = new AtomicInteger();

    private RMProxiesManager rmProxiesManager;

    public MockSchedulingInfrastructure(SchedulerDBManager dbManager) throws Exception {
        this(dbManager, null);
    }

    public MockSchedulingInfrastructure(SchedulerDBManager dbManager, ExecutorService executorService)
            throws Exception {
        this.dbManager = dbManager;
        this.dsStarter = mock(DataSpaceServiceStarter.class);

        if (executorService == null) {
            this.executorService = new AbstractExecutorService() {

                @Override
                public void execute(Runnable command) {
                    command.run();
                }

                @Override
                public List<Runnable> shutdownNow() {
                    return null;
                }

                @Override
                public void shutdown() {

                }

                @Override
                public boolean isTerminated() {
                    return false;
                }

                @Override
                public boolean isShutdown() {
                    return false;
                }

                @Override
                public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                    return false;
                }
            };
        } else {
            this.executorService = executorService;
        }

        RMProxy userProxy = mock(RMProxy.class);
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

        }).when(userProxy).releaseNodes((NodeSet) anyObject(),
                                        (Script<?>) anyObject(),
                                        (VariablesMap) anyObject(),
                                        (Map<String, String>) anyObject(),
                                        (TaskId) anyObject(),
                                        (Credentials) anyObject());

        rmProxiesManager = mock(RMProxiesManager.class);
        when(rmProxiesManager.getUserRMProxy(anyString(), (Credentials) anyObject())).thenReturn(userProxy);
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
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
    public ExecutorService getClientOperationsThreadPool() {
        return executorService;
    }

    @Override
    public ExecutorService getInternalOperationsThreadPool() {
        return executorService;
    }

    @Override
    public ExecutorService getTaskPingerThreadPool() {
        return executorService;
    }

    @Override
    public SchedulerSpacesSupport getSpacesSupport() {
        return null;
    }

    @Override
    public void schedule(Runnable runnable, long delay) {
        System.out.println("Requested to schedule " + runnable + ", delay: " + delay);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(runnable, 1, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Override
    public void schedule(Callable<?> task, long delay) {
        System.out.println("Requested to schedule " + task + ", delay: " + delay);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, 1, TimeUnit.MILLISECONDS);
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
