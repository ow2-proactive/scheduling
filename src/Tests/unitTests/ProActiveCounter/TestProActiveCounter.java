package unitTests.ProActiveCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveCounter;


public class TestProActiveCounter {
    static final long MAX = 1000000;
    static final int NB_THREAD = 10;

    @Test
    public void testProActivecounter() throws InterruptedException {
        Vector<Long> results = new Vector<Long>();
        Thread[] threads = new Thread[NB_THREAD];

        for (int i = 0; i < NB_THREAD; i++) {
            threads[i] = new Consumer(results);
            threads[i].start();
        }

        for (int i = 0; i < NB_THREAD; i++) {
            threads[i].join();
        }

        Collections.sort(results);
        for (int i = 0; i < results.size(); i++) {
            Assert.assertEquals("i=" + i, results.get(i), i);
        }
    }

    class Consumer extends Thread {
        List<Long> l = new ArrayList<Long>();
        List<Long> results;

        public Consumer(List<Long> results) {
            this.results = results;
        }

        public void run() {
            for (int i = 0; i < (MAX / NB_THREAD); i++) {
                l.add(ProActiveCounter.getUniqID());
                Thread.yield();
            }
            results.addAll(l);
        }
    }
}
