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
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.threading.ThreadPoolController;
import org.ow2.proactive.threading.ThreadPoolControllerImpl;
import org.ow2.proactive.threading.TimedRunnable;


/**
 * Test the Thread pool controller
 * 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestThreadPoolController {

    private static int ID = 0;

    @Test
    public void run() throws Throwable {
        log("Test Runnable");
        ThreadPoolController controller = new ThreadPoolControllerImpl(4);
        List<Runnable> col = new ArrayList<Runnable>();
        col.add(new Task(400));
        col.add(new Task(500));
        col.add(new Task(600));
        long start = System.currentTimeMillis();
        controller.execute(col.get(0));
        controller.execute(col);
        Assert.assertTrue(System.currentTimeMillis() - start < 400);
        Thread.sleep(1200);
        Assert.assertTrue(((TimedRunnable) col.get(0)).isDone());
        Assert.assertTrue(((TimedRunnable) col.get(1)).isDone());
        Assert.assertTrue(((TimedRunnable) col.get(2)).isDone());

        log("Test TimedRunnable");
        List<Task> list = new ArrayList<Task>();
        list.add(new Task(300));
        list.add(new Task(400));
        list.add(new Task(500));
        list.add(new Task(600));
        start = System.currentTimeMillis();
        Collection<Task> results = controller.execute(list, 700);
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 600);
        Assert.assertTrue(end - start < 610);
        Assert.assertEquals(4, results.size());
        for (Task tr : results) {
            Assert.assertTrue(tr.isDone());
            Assert.assertFalse(tr.hasTimeout());
        }

        list.clear();
        list.add(new Task(300));
        list.add(new Task(400));
        list.add(new Task(500));
        list.add(new Task(700));
        start = System.currentTimeMillis();
        results = controller.execute(list, 600);
        end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 600);
        Assert.assertTrue(end - start < 610);
        Assert.assertEquals(3, results.size());
        for (Task tr : results) {
            Assert.assertTrue(tr.isDone());
            Assert.assertFalse(tr.hasTimeout());
        }
        Assert.assertFalse(list.get(3).isDone());
        Assert.assertTrue(list.get(3).hasTimeout());

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
        results = controller.execute(list, 1200);
        end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 1200);
        Assert.assertTrue(end - start < 1210);
        Assert.assertEquals(7, results.size());
        for (Task tr : results) {
            Assert.assertTrue(tr.isDone());
            Assert.assertFalse(tr.hasTimeout());
        }
        Assert.assertFalse(list.get(7).isDone());
        Assert.assertTrue(list.get(7).hasTimeout());
        Assert.assertFalse(list.get(8).isDone());
        Assert.assertTrue(list.get(8).hasTimeout());
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    class Task implements TimedRunnable {

        private int id = ID++;
        private volatile boolean done = false;
        private volatile boolean timedout = false;
        private long sleep;

        public Task(long sleep) {
            this.sleep = sleep;
        }

        public void run() {
            log("Starting : " + this + ", wait time : " + sleep);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
            if (!timedout) {
                log("Ending   : " + this);
                done = true;
            }
        }

        public boolean isDone() {
            return done;
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
