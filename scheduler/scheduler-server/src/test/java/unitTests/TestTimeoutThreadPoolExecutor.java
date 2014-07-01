/*
 * ################################################################
 *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package unitTests;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.threading.CallableWithTimeoutAction;
import org.ow2.proactive.threading.TimeoutThreadPoolExecutor;


/**
 * Test against class TimeoutThreadPoolExecutor.
 * 
 * @author ProActive team
 *
 */
public class TestTimeoutThreadPoolExecutor {

    private TimeoutThreadPoolExecutor executor;

    @Before
    public void init() {
        executor = TimeoutThreadPoolExecutor.newFixedThreadPool(1, new NamedThreadFactory(
            "TestTimeoutThreadPoolExecutor", false));
    }

    @After
    public void clear() {
        executor.shutdown();
    }

    static private class TestCallable implements CallableWithTimeoutAction<String> {

        private volatile boolean timeoutWasCalled;

        private volatile boolean wasInterrupted;

        private volatile boolean started;

        private final long sleepTimeout;

        TestCallable(long sleepTimeout) {
            this.sleepTimeout = sleepTimeout;
        }

        @Override
        public String call() throws Exception {
            started = true;
            try {
                Thread.sleep(sleepTimeout);
            } catch (InterruptedException e) {
                wasInterrupted = true;
                Thread.currentThread().interrupt();
            }
            return "OK";
        }

        @Override
        public void timeoutAction() {
            timeoutWasCalled = true;
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isTimeoutWasCalled() {
            return timeoutWasCalled;
        }

        public boolean isWasInterrupted() {
            return wasInterrupted;
        }

    }

    @Test
    public void testTimeoutThreadPoolExecutor() throws Exception {
        TestCallable callable;
        Future<String> result;

        callable = new TestCallable(1);
        result = executor.submitWithTimeout(callable, 5000, TimeUnit.MILLISECONDS);
        Assert.assertEquals("OK", result.get(1000, TimeUnit.MILLISECONDS));
        Thread.sleep(6000);
        Assert
                .assertFalse("Timeout shouldn't be called if callable completed", callable
                        .isTimeoutWasCalled());
        Assert.assertFalse("Callable shouldn't be interrupted if callable completed", callable
                .isWasInterrupted());

        callable = new TestCallable(10000);
        result = executor.submitWithTimeout(callable, 1000, TimeUnit.MILLISECONDS);
        try {
            result.get(10000, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            // expected exception
        }
        Thread.sleep(2000);
        Assert.assertTrue("Timeout should be called", callable.isTimeoutWasCalled());
        Assert.assertTrue("Callable should be interrupted in case of timeout", callable.isWasInterrupted());

        callable = new TestCallable(10000);
        result = executor.submitWithTimeout(callable, 3000, TimeUnit.MILLISECONDS);
        while (!callable.isStarted()) {
            Thread.sleep(100);
        }
        result.cancel(true);
        try {
            result.get();
        } catch (CancellationException e) {
            // expected exception
        }
        Thread.sleep(2000);
        Assert.assertFalse("Timeout shouldn't be called if task is cancelled", callable.isTimeoutWasCalled());
        Assert.assertTrue("Callable should be interrupted if task is cancelled", callable.isWasInterrupted());
    }

}
