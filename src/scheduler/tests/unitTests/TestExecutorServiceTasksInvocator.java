/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package unitTests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.threading.CallableWithTimeoutAction;
import org.ow2.proactive.threading.ExecutorServiceTasksInvocator;


/**
 * Test the callableWithTimeoutActionInvocator method
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestExecutorServiceTasksInvocator {

    private static int ID = 0;

    @Test
    public void run() throws Throwable {
        ExecutorService pool = Executors.newFixedThreadPool(4);

        log("Test CallableWithTimeoutAction");
        List<Task> list = new ArrayList<Task>();
        list.add(new Task(300));
        list.add(new Task(400));
        list.add(new Task(500));
        list.add(new Task(600));
        long start = System.currentTimeMillis();
        List<Future<Integer>> results = ExecutorServiceTasksInvocator.invokeAllWithTimeoutAction(pool, list,
                700);
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 600);
        Assert.assertTrue(end - start < 650);
        Integer i = 0;
        for (Future<Integer> future : results) {
            Assert.assertTrue(future.isDone());
            Assert.assertFalse(future.isCancelled());
            Assert.assertEquals(i++, (Integer) future.get());
        }

        list.clear();
        list.add(new Task(300));
        list.add(new Task(400));
        list.add(new Task(500));
        list.add(new Task(700));
        start = System.currentTimeMillis();
        results = ExecutorServiceTasksInvocator.invokeAllWithTimeoutAction(pool, list, 600);
        end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 600);
        Assert.assertTrue(end - start < 650);
        i = 0;
        int nbCanceled = 0;
        for (Future<Integer> future : results) {
            Assert.assertTrue(future.isDone());
            if (future.isCancelled()) {
                nbCanceled++;
                Assert.assertTrue(list.get(i).hasTimeout());
                try {
                    future.get();
                    throw new RuntimeException("Should be in the following catch clause !");
                } catch (CancellationException ce) {
                }
            } else {
                Assert.assertFalse(list.get(i).hasTimeout());
                Assert.assertEquals((int) (i + 4), (int) future.get());
            }
            i++;
        }
        Assert.assertEquals(1, nbCanceled);
        Assert.assertEquals(3, results.size() - nbCanceled);

        list.clear();
        list.add(new Task(1000));
        list.add(new Task(400));
        list.add(new Task(500));
        list.add(new Task(600));
        list.add(new Task(600));
        list.add(new Task(500));
        list.add(new Task(400));
        list.add(new Task(300));
        list.add(new Task(500));
        start = System.currentTimeMillis();
        results = ExecutorServiceTasksInvocator.invokeAllWithTimeoutAction(pool, list, 1200);
        end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 1200);
        Assert.assertTrue(end - start < 1250);
        i = 0;
        nbCanceled = 0;
        for (Future<Integer> future : results) {
            Assert.assertTrue(future.isDone());
            if (future.isCancelled()) {
                nbCanceled++;
                Assert.assertTrue(list.get(i).hasTimeout());
                try {
                    future.get();
                    throw new RuntimeException("Should be in the following catch clause !");
                } catch (CancellationException ce) {
                }
            } else {
                Assert.assertFalse(list.get(i).hasTimeout());
                Assert.assertEquals((int) i + 8, (int) future.get());
            }
            i++;
        }
        Assert.assertEquals(2, nbCanceled);
        Assert.assertEquals(7, results.size() - nbCanceled);
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    class Task implements CallableWithTimeoutAction<Integer> {

        private int id = ID++;
        private volatile boolean timedout = false;
        private long sleep;

        public Task(long sleep) {
            this.sleep = sleep;
        }

        public Integer call() throws Exception {
            log("Starting : " + this + ", wait time : " + sleep);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
            if (!timedout) {
                log("Ending   : " + this);
            }
            return id;
        }

        public void timeoutAction() {
            timedout = true;
            log("Timeout  : " + this);
        }

        public boolean hasTimeout() {
            return timedout;
        }

        @Override
        public String toString() {
            return "T" + id;
        }

    }

}
