package org.ow2.proactive.gui.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;


public class ActiveObjectPingerThreadTest1 {

    static class TestPingLogic {
        public boolean ping() {
            return true;
        }
    }

    static TestPingLogic testPingLogic;

    private TestProxy proxy;

    @Before
    public void init() throws Exception {
        testPingLogic = new TestPingLogic();

        proxy = new TestProxy();
        proxy.createActiveObject();
    }

    @After
    public void cleanup() {
        if (proxy != null) {
            proxy.terminateActiveObjectHolder();
        }
    }

    public static class TestActiveObject {

        @ImmediateService
        public boolean ping() {
            return testPingLogic.ping();
        }

        public void terminate() throws Exception {
            PAActiveObject.terminateActiveObject(PAActiveObject.getStubOnThis(), true);
        }

    }

    static class TestProxy extends ActiveObjectProxy<TestActiveObject> {

        @Override
        protected boolean doPingActiveObject(TestActiveObject activeObject) {
            return activeObject.ping();
        }

        @Override
        protected TestActiveObject doCreateActiveObject() throws Exception {
            TestActiveObject ao = PAActiveObject.newActive(TestActiveObject.class, new Object[] {});
            return ao;
        }

        public void teminate() {
            asyncCallActiveObject(new ActiveObjectAccess<TestActiveObject>() {
                public void accessActiveObject(TestActiveObject activeObject) {
                    try {
                        activeObject.terminate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    static class TestPingListener implements ActiveObjectPingerThread.PingListener {

        private volatile boolean pingFalse;

        private volatile boolean pingError;

        private volatile boolean pingTimeout;

        @Override
        public void onPingFalse() {
            pingFalse = true;
        }

        @Override
        public void onPingError() {
            pingError = true;
        }

        @Override
        public void onPingTimeout() {
            pingTimeout = true;
        }

        void checkResult(boolean pingFalse, boolean pingError, boolean pingTimeout) {
            assertEquals("Unexpected pingFalse", pingFalse, this.pingFalse);
            assertEquals("Unexpected pingError", pingError, this.pingError);
            assertEquals("Unexpected pingTimeout", pingTimeout, this.pingTimeout);
        }
    }

    @Test
    public void testSuccesfulPing() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 3000, listener);
        thread.start();
        Thread.sleep(5000);
        thread.stopPinger();
        thread.join();
        listener.checkResult(false, false, false);
    }

    @Test
    public void testPingError1() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 3000, listener);
        thread.start();
        Thread.sleep(3000);
        listener.checkResult(false, false, false);
        proxy.terminateActiveObjectHolder();
        thread.join();
        listener.checkResult(false, true, false);
    }

    @Test
    public void testPingError2() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 3000, listener);
        thread.start();
        Thread.sleep(3000);
        listener.checkResult(false, false, false);
        testPingLogic = new TestPingLogic() {
            public boolean ping() {
                throw new RuntimeException();
            }
        };
        thread.join();
        listener.checkResult(false, true, false);
    }

    @Test
    public void testPingTimeout() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 3000, listener);
        thread.start();
        Thread.sleep(3000);
        listener.checkResult(false, false, false);
        System.out.println("Ping hangs");
        testPingLogic = new TestPingLogic() {
            public boolean ping() {
                while (true) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.join();
        listener.checkResult(false, false, true);
    }

    @Test
    public void testPingFalse() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 3000, listener);
        thread.start();
        Thread.sleep(3000);
        listener.checkResult(false, false, false);
        System.out.println("Ping is false");
        testPingLogic = new TestPingLogic() {
            public boolean ping() {
                return false;
            }
        };
        thread.join();
        listener.checkResult(true, false, false);
    }

    @Test
    public void testPingTimeoutAfterStopPing() throws Exception {
        TestPingListener listener = new TestPingListener();
        ActiveObjectPingerThread thread = new ActiveObjectPingerThread(proxy, 100, 5000, listener);
        thread.start();
        testPingLogic = new TestPingLogic() {
            public boolean ping() {
                while (true) {
                    try {
                        // ping will fail with timeout
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        // wait when pinger calls ping
        Thread.sleep(2000);
        listener.checkResult(false, false, false);
        // cancel pinging, ping will fail with timeout, but listener shouldn't be notified
        thread.stopPinger();
        thread.join();
        listener.checkResult(false, false, false);
    }
}