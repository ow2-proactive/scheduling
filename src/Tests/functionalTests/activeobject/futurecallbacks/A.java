package functionalTests.activeobject.futurecallbacks;

import java.util.concurrent.Future;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.MutableInteger;


public class A {
    private A brother;
    public static int counter = 0;

    public A() {
    }

    public void myCallback(Future<MutableInteger> f) throws Exception {
        MutableInteger i = f.get();
        i.getValue();
        synchronized (A.class) {
            A.counter++;
            A.class.notifyAll();
        }
    }

    public void start() {
        MutableInteger slow = this.brother.slow();
        ProFuture.addActionOnFuture(slow, "myCallback");
        MutableInteger fast = this.brother.fast();
        ProFuture.waitFor(fast);
        ProFuture.addActionOnFuture(fast, "myCallback");
    }

    public MutableInteger slow() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new MutableInteger();
    }

    public MutableInteger fast() {
        return new MutableInteger();
    }

    public void giveBrother(A a) {
        this.brother = a;
    }
}
