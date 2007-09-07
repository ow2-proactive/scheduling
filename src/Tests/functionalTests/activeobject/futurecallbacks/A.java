package functionalTests.activeobject.futurecallbacks;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.util.MutableInteger;


public class A {
    private A brother;
    public static int counter = 0;

    public A() {
    }

    public void myCallback(FutureResult f) {
        MutableInteger i = (MutableInteger) f.getResult();
        i.getValue();
        synchronized (A.class) {
            A.counter++;
            A.class.notifyAll();
        }
    }

    public void start() throws NoSuchMethodException {
        MutableInteger slow = this.brother.slow();
        ProActive.addFutureCallback(slow, "myCallback");
        MutableInteger fast = this.brother.fast();
        ProActive.waitFor(fast);
        ProActive.addFutureCallback(fast, "myCallback");
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
