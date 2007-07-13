package functionalTests.activeobject.futuremonitoring;

public class A {
    public A() {
    }

    public A crash() {
        System.exit(0);
        return null;
    }

    public synchronized A sleepForever() {
        for (;;) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public A wrapFuture(A a) {
    	return a;
    }
}
