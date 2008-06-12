package functionalTests.activeobject.proactive359;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.FunctionalTest;


public class TestProActive359 extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), null);

        boolean exception;

        try {
            exception = false;

            StringWrapper foo = ao.foo();
            PAFuture.waitFor(foo, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertFalse(exception);

        StringWrapper bar = ao.bar();
        try {
            exception = false;

            PAFuture.waitFor(bar, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        ao.resume();
        try {
            exception = false;

            PAFuture.waitFor(bar, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
    }

    static public class AO implements InitActive, RunActive, Serializable {
        private BlockingRequestQueue rqueue;

        public AO() {

        }

        public void initActivity(Body body) {
            PAActiveObject.setImmediateService("resume");
            rqueue = body.getRequestQueue();
        }

        public void runActivity(Body body) {
            Service service = new Service(body);
            service.blockingServeOldest("foo");

            rqueue.suspend();
            service.blockingServeOldest("bar");

        }

        public void resume() {
            rqueue.resume();
        }

        public StringWrapper foo() {
            System.err.println("foo");
            return new StringWrapper("foo");
        }

        public StringWrapper bar() {
            System.err.println("bar");
            return new StringWrapper("bar");
        }
    }

}
