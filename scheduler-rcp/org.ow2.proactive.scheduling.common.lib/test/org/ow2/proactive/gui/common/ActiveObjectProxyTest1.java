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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.gui.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;


public class ActiveObjectProxyTest1 {

    public static class TestActiveObject {

        public void sleep(long timeout) throws Exception {
            System.out.println("Starting sleep " + Thread.currentThread());
            Thread.sleep(timeout);
            System.out.println("Ending sleep");
        }

        public Integer getData(int value) {
            return value * 2;
        }

        @ImmediateService
        public boolean ping() {
            return true;
        }

    }

    static class TestProxy extends ActiveObjectProxy<TestActiveObject> {

        public TestProxy() throws ProActiveException {
            super();
        }

        @Override
        protected boolean doPingActiveObject(TestActiveObject activeObject) {
            return activeObject.ping();
        }

        @Override
        protected TestActiveObject doCreateActiveObject() throws Exception {
            TestActiveObject ao = PAActiveObject.newActive(TestActiveObject.class, new Object[] {});
            return ao;
        }

        public void getData(final ActiveObjectCallResultHandler<Integer> resultHandler, final int data) {
            asyncCallActiveObject(new ActiveObjectAccess<TestActiveObject>() {
                public void accessActiveObject(TestActiveObject activeObject) {
                    Integer result = activeObject.getData(data);
                    resultHandler.handleResult(result);
                }
            });
        }

        public void asyncSleep(final long timeout) {
            asyncCallActiveObject(new ActiveObjectAccess<ActiveObjectProxyTest1.TestActiveObject>() {
                public void accessActiveObject(TestActiveObject activeObject) {
                    try {
                        activeObject.sleep(timeout);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private TestProxy testProxy;

    @Before
    public void createProxy() throws Exception {
        testProxy = new TestProxy();
        testProxy.createActiveObject();
    }

    @After
    public void closeProxy() {
        if (testProxy != null) {
            testProxy.terminateActiveObjectHolder();
        }
    }

    @Test
    public void testActiveObjectCallIsAsync() throws Exception {
        System.out.println("Starting async sleeping");
        long start = System.currentTimeMillis();
        testProxy.asyncSleep(20000);
        long time = System.currentTimeMillis() - start;
        System.out.println("Finishing async sleeping (time " + time + ")");
        assertTrue("Call isn't async, it took: " + time + "ms", time < 10000);
    }

    @Test
    public void testPingIsImmediate() throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testProxy.asyncSleep(Long.MAX_VALUE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        Thread.sleep(1000);
        System.out.println("Starting ping " + Thread.currentThread());
        assertTrue("Ping should return true", testProxy.syncPingActiveObject());
    }

    static class TestHandler implements ActiveObjectCallResultHandler<Integer> {

        private static boolean ready;

        private static Integer result;

        @Override
        public void handleResult(Integer result) {
            TestHandler.result = result;
            resultIsReady();
        }

        synchronized static void resultIsReady() {
            ready = true;
            TestHandler.class.notifyAll();
        }

        synchronized static void waitForResult() throws Exception {
            while (!ready) {
                TestHandler.class.wait();
            }
        }

    }

    @Test
    public void testResultHandler() throws Exception {
        TestHandler handler = new TestHandler();
        testProxy.getData(handler, 10);
        TestHandler.waitForResult();
        assertTrue("Unexpected result: " + TestHandler.result, 20 == TestHandler.result.intValue());
    }

}
