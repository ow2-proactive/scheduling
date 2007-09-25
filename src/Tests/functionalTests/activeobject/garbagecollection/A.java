package functionalTests.activeobject.garbagecollection;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;


public class A implements InitActive {
    private Collection<A> references;
    public static final Collection<WeakReference<A>> weak = new LinkedList<WeakReference<A>>();

    public A() {
    }

    public void initActivity(Body body) {
        // this is not a stub
        A.weak.add(new WeakReference<A>(this));
        references = new Vector<A>();
    }

    public void addRef(A ref) {
        this.references.add(ref);
    }

    public static int countCollected() {
        int count = 0;
        for (WeakReference<A> wr : A.weak) {
            if (wr.get() == null) {
                count++;
            }
        }
        return count;
    }
}
