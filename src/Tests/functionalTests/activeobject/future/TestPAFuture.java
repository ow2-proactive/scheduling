package functionalTests.activeobject.future;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;

import functionalTests.FunctionalTest;


public class TestPAFuture extends FunctionalTest {

    @Test
    public void isAwaitedNoFuture() {
        Object o = new Object();
        boolean resp = PAFuture.isAwaited(o);
        Assert.assertFalse("O is not a future, should not been awaited", resp);
    }

    @Test(timeout = 500)
    public void waitForNoFuture() {
        Object o = new Object();
        PAFuture.waitFor(o);
    }

    @Test(timeout = 500)
    public void waitForWithTimeoutNoFuture() throws ProActiveException {
        Object o = new Object();
        PAFuture.waitFor(o, 1000);
    }

    @Test
    public void waitForAny() {

    }

    @Test
    public void waitForAnyNoFture() {
        Vector<Object> v = new Vector<Object>();
        v.add(new Object());
        v.add(new Object());

        int index;
        index = PAFuture.waitForAny(v);
        v.remove(index);
        index = PAFuture.waitForAny(v);
        v.remove(index);
        Assert.assertTrue(v.isEmpty());
    }

    @Test(timeout = 500)
    public void waitForAllNoFture() {
        Vector<Object> v = new Vector<Object>();
        v.add(new Object());
        v.add(new Object());

        PAFuture.waitForAll(v);
    }

    @Test(timeout = 500)
    public void waitForAllWithTimeoutNoFture() throws ProActiveException {
        Vector<Object> v = new Vector<Object>();
        v.add(new Object());
        v.add(new Object());

        PAFuture.waitForAll(v, 1000);
    }
}
