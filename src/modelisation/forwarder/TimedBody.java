package modelisation.forwarder;

import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.ConstructorCall;
import modelisation.TimedMigrationManager;
import modelisation.timedrequest.TimedFactory;

import java.lang.reflect.InvocationTargetException;

public class TimedBody extends BodyImpl {

    public TimedBody(ConstructorCall c, String nodeURL)
            throws InvocationTargetException, org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
        super(c, nodeURL);
        this.barrier = new TimedBarrier();
    }


///**
//     * Blocking method that makes the caller thread wait until incoming communications
//     * can be accepted.
//     */
//    protected synchronized void waitTillAccept() {
//	long startTime = System.currentTimeMillis();
//	super.waitTillAccept();
//	System.out.println("TimedBody: waitTillAccept() waited " + (System.currentTimeMillis() - startTime));
//    }

    /**
     * Creates the factory in charge of constructing the requests.
     * @return the factory in charge of constructing the requests.
     */
    protected RequestFactory createRequestFactory() {
        return new TimedFactory();
    }

    protected MigrationManager createMigrationManager() {
        return new TimedMigrationManager();
    }

    protected class TimedBarrier extends Barrier implements java.io.Serializable {

        protected int counter;
        protected boolean open;

        public synchronized void enter() {
            long startTime = System.currentTimeMillis();
            super.enter();
            System.out.println("TimedBody: waitTillAccept() waited " + (System.currentTimeMillis() - startTime));
        }

        public synchronized void close() {
            long startTime = System.currentTimeMillis();
            super.close();
            System.out.println("Barrier: close() lasted " + (System.currentTimeMillis() - startTime));
        }
    }
}
