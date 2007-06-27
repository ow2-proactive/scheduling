package functionalTests.activeobject.garbagecollection;

import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.core.util.MutableInteger;


public class A implements InitActive {
    public static final MutableInteger deadCounter = new MutableInteger();
    private Collection<A> references;
    private boolean isReal = false; // not a stub

    public A() {
    }

    public void initActivity(Body body) {
        this.isReal = true;
        references = new Vector<A>();
    }

    public void addRef(A ref) {
        this.references.add(ref);
    }

    public void finalize() throws Throwable {
        try {
            if (this.isReal) {
                synchronized (deadCounter) {
                    deadCounter.add(1);
                    deadCounter.notify();
                }
            }
        } finally {
            super.finalize();
        }
    }
}
