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
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.ow2.proactive.gui.common.ActiveObjectProxy.ActiveObjectActivityListener;


public class ActiveObjectProxyTest2 {

    public static class TestActiveObject {
    }

    private static volatile int startCounter;

    private static volatile int finishCounter;

    static class TestProxy extends ActiveObjectProxy<TestActiveObject> {

        public TestProxy() throws ProActiveException {
            super();
        }

        @Override
        protected boolean doPingActiveObject(TestActiveObject activeObject) {
            return true;
        }

        @Override
        protected TestActiveObject doCreateActiveObject() throws Exception {
            TestActiveObject ao = PAActiveObject.newActive(TestActiveObject.class, new Object[] {});
            return ao;
        }

        public Integer getData(final int data) {
            try {
                return syncCallActiveObject(new ActiveObjectSyncAccess<TestActiveObject>() {
                    public Integer accessActiveObject(TestActiveObject activeObject) {
                        return data;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void asyncSleep(final long timeout) {
            asyncCallActiveObject(new ActiveObjectAccess<ActiveObjectProxyTest2.TestActiveObject>() {
                public void accessActiveObject(TestActiveObject activeObject) {
                    try {
                        Thread.sleep(timeout);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        protected ActiveObjectActivityListener doCreateActivityListener() {
            listener = new ActiveObjectActivityListener() {

                @Override
                public void onActivityStarted() {
                    startCounter++;
                    System.out.println("Started");
                }

                @Override
                public void onActivityFinished() {
                    finishCounter++;
                    System.out.println("Finished");
                }

            };
            return listener;
        }

    }

    private static ActiveObjectActivityListener listener;

    private TestProxy testProxy;

    @Before
    public void createProxy() throws Exception {
        startCounter = 0;
        finishCounter = 0;

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
    public void testActivityListener1() throws Exception {
        checkCounters(0, 0);
        Assert.assertNotNull("getActivityListener() returns null", testProxy.getActivityListener());
        Assert.assertSame("getActivityListener() returns unexpected instance", listener, testProxy
                .getActivityListener());

        testProxy.getData(0);
        checkCounters(1, 1);
        for (int i = 0; i < 50; i++) {
            checkCounters(i + 1, i + 1);
            testProxy.getData(i);
            checkCounters(i + 2, i + 2);
        }
    }

    @Test
    public void testActivityListener2() throws Exception {
        checkCounters(0, 0);
        testProxy.asyncSleep(3000);
        Thread.sleep(1000);
        checkCounters(1, 0);
        Thread.sleep(3000);
        checkCounters(1, 1);
    }

    @Test
    public void testActivityListener3() throws Exception {
        testProxy.asyncSleep(2000);
        testProxy.asyncSleep(2000);
        testProxy.asyncSleep(2000);
        Thread.sleep(1000);
        checkCounters(1, 0);
        Thread.sleep(2000);
        checkCounters(2, 1);
        Thread.sleep(2000);
        checkCounters(3, 2);
        Thread.sleep(2000);
        checkCounters(3, 3);
    }

    @Test
    public void testActivityListener4() throws Exception {
        testProxy.asyncSleep(3000);
        Thread.sleep(1000);
        checkCounters(1, 0);
        testProxy.getData(10);
        checkCounters(1, 0);
        Thread.sleep(3000);
        checkCounters(1, 1);
    }

    @Test
    public void testActivityListener5() throws Exception {
        testProxy.asyncSleep(4000);
        Thread.sleep(1000);
        checkCounters(1, 0);
        testProxy.terminateActiveObjectHolder();
        checkCounters(1, 1);
        Thread.sleep(4000);
        checkCounters(1, 1);
    }

    private void checkCounters(int expectedStart, int expectedFinish) {
        Assert.assertEquals("Unexpected start counter", expectedStart, startCounter);
        Assert.assertEquals("Unexpected finish counter", expectedFinish, finishCounter);
    }

}
