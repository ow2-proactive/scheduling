package unitTests;

import org.ow2.proactive.scheduler.util.Watch;
import org.junit.Assert;
import org.junit.Test;

public class TestWatch {

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore it
        }
    }

    @Test
    public void test_sequence() throws Exception {
        Watch w = Watch.startNewWatch();
        sleep(50);
        Assert.assertTrue(w.elapsedMilliseconds() >= 50);
        Assert.assertTrue(w.elapsedMilliseconds() <= 500);
        sleep(50);
        Assert.assertTrue(w.elapsedMilliseconds() >= 100);
        Assert.assertTrue(w.elapsedMilliseconds() <= 500);
        sleep(400);
        Assert.assertTrue(w.elapsedMilliseconds() >= 500);
    }

}

