/*
 * RSHJVMProcessImpl.java
 *
 * Copyright 1997 - 2001 INRIA Project Oasis. All Rights Reserved.
 *
 * This software is the proprietary information of INRIA Sophia Antipolis.
 * 2004 route des lucioles, BP 93 , FR-06902 Sophia Antipolis
 * Use is subject to license terms.
 *
 * @author  ProActive Team
 * @version ProActive 0.9 (November 2001)
 *
 * ===================================================================
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL INRIA, THE OASIS PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ===================================================================
 *
 */
package modelisation.server.fakebench;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

import modelisation.statistics.ExponentialLaw;


public class FakeBench2 {
    //we put here the stats for the agent and the source
    protected final static int WAITING = 1;
    protected final static int WAITING_ON_AGENT = 2;
    protected final static int MIGRATING = 3;
    protected final static int CALLING_AGENT = 4;
    protected final static int CALLING_SERVER = 5;
    protected final static int MIGRATED = 6;
    protected static LocationServer locationServer;

    public FakeBench2() {
    }

    public FakeBench2(String[] args) {
        DummyObject dummy = null;
        try {
            dummy = (DummyObject) ProActive.newActive(DummyObject.class.getName(),
                    (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object tmp = dummy.getUniqueID();
        ProActive.waitFor(tmp);
        UniqueID id = (UniqueID) ((FutureProxy) ((StubObject) tmp).getProxy()).getResult();
        tmp = dummy.getRemoteAdapter();
        ProActive.waitFor(tmp);
        UniversalBody tmpBody = (UniversalBody) ((FutureProxy) ((StubObject) tmp).getProxy()).getResult();
        Agent agent = new Agent(id, tmpBody, Double.parseDouble(args[1]),
                Double.parseDouble(args[2]));
        Source source = new Source(id, Double.parseDouble(args[0]),
                Double.parseDouble(args[3]), agent);
        FakeBench2.locationServer = LocationServerFactory.getLocationServer();
        new Thread(source).start();
        new Thread(agent).start();
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: " + FakeBench2.class.getName() +
                " <number> <lambda>" + " <nu> <delta> <gamma1>");
            System.exit(0);
        }
        int number = Integer.parseInt(args[0]);
        String[] tmp = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            tmp[i - 1] = args[i];
        }

        for (int i = 0; i < number; i++) {
            new FakeBench2(tmp);
        }
    }

    static public class DummyObject implements org.objectweb.proactive.RunActive {
        protected UniqueID id;
        protected UniversalBody body;

        public DummyObject() {
        }

        public UniqueID getUniqueID() {
            return this.id;
        }

        public UniversalBody getRemoteAdapter() {
            return this.body;
        }

        public void runActivity(Body b) {
            org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(b);
            this.id = b.getID();
            this.body = b.getRemoteAdapter();
            service.fifoServing();
        }
    }

    public static class BenchElement {
        protected UniqueID id;
        protected int state;
        protected ExponentialLaw expo;
        protected ExponentialLaw expo2;
        protected long stateLength;

        public void wait(double time) {
            System.out.println("BenchElement.wait for " + (long) time);
            long startTime = System.currentTimeMillis();
            System.out.println("BenchElement.wait started" + " at time " +
                startTime);
            try {
                Thread.sleep((long) time);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("BenchElement.wait ended" + " at time " +
                endTime);
            System.out.println("BenchElement.wait ended" + " lasted " +
                (endTime - startTime));
        }

        public long getStateLength() {
            return this.stateLength;
        }
    }

    static public class Source extends BenchElement implements Runnable {
        protected Agent agent;
        protected long startTime = 0;

        public Source(UniqueID id, double lambda, double gamma1, Agent a) {
            this.id = id;
            this.state = FakeBench2.WAITING;
            expo = new ExponentialLaw(lambda);
            expo2 = new ExponentialLaw(gamma1);
            agent = a;
        }

        public void run() {
            while (true) {
                this.changeState();
            }
        }

        protected void changeState() {
            double waitTime = 0;
            if (state == FakeBench2.WAITING) {
                System.out.println("Source.changeState: waiting" + " at time " +
                    System.currentTimeMillis());
                waitTime = expo.next() * 1000;
                wait(waitTime);
                startTime = System.currentTimeMillis();
                this.state = FakeBench2.CALLING_AGENT;
                return;
            }
            if (state == CALLING_AGENT) {
                System.out.println("Source.changeState: calling agent" +
                    " at time " + System.currentTimeMillis());

                waitTime = expo2.next() * 1000;
                wait(waitTime);
                System.out.println(
                    "TimedRequestWithLocationServer:  .............. 1/gamma = " +
                    waitTime + " for method echo" + " at time " +
                    System.currentTimeMillis());
                if (this.agent.getState() == MIGRATED) {
                    this.state = CALLING_SERVER;
                }
                if (this.agent.getState() == MIGRATING) {
                    System.out.println("Source.changeState: agent is migrating" +
                        " at time " + System.currentTimeMillis());
                    long t1 = System.currentTimeMillis();
                    this.agent.waitEndOfMigration();
                    long t2 = System.currentTimeMillis();
                    System.out.println(
                        "Source.changeState: agent is migrating waited " +
                        (t2 - t1) + " at time " + System.currentTimeMillis());
                    this.state = CALLING_SERVER;
                    return;
                }
                if (this.agent.getState() == WAITING) {
                    System.out.println(
                        "Source.changeState: calling agent ... ok" +
                        " at time " + System.currentTimeMillis());
                    this.agent.call();
                    long endTime = System.currentTimeMillis();
                    System.out.println(
                        "TimedRequestWithLocationServer:  .............. done  = " +
                        (endTime - startTime) + " for method echo" +
                        " at time " + System.currentTimeMillis());
                    this.state = FakeBench2.WAITING;
                }
            }
            if (state == CALLING_SERVER) {
                System.out.println("Source.changeState: calling server" +
                    " at time " + System.currentTimeMillis());
                this.callServer();
                this.agent.foundYou();
                this.state = CALLING_AGENT;
                return;
            }
        }

        protected void callServer() {
            long startTimeBackupSolution = System.currentTimeMillis();
            UniversalBody mobile = (UniversalBody) FakeBench2.locationServer.searchObject(id);
            long endTimeBackupSolution = System.currentTimeMillis();
            System.out.println(
                "TimedRequestWithLocationServer:  .............. 1/gamma = " +
                (endTimeBackupSolution - startTimeBackupSolution) +
                " for method searchObject");
            System.out.println(
                "TimedRequestWithLocationServer:  .............. done  = " +
                (endTimeBackupSolution - startTimeBackupSolution) +
                " for method searchObject");
            //   System.out.println("TimedRequestWithLocationServer: backupSolution() server has sent an answer after "
            //                        + (endTimeBackupSolution - startTimeBackupSolution));
            ProActive.waitFor(mobile);
            System.out.println(
                "TimedRequestWithLocationServer: backupSolution() server has sent an answer after " +
                (System.currentTimeMillis() - startTimeBackupSolution) +
                " at time " + System.currentTimeMillis());
        }
    }

    static public class Agent extends BenchElement implements Runnable {
        protected UniversalBody body;

        public Agent(UniqueID id, UniversalBody body, double nu, double delta) {
            this.id = id;
            this.state = FakeBench2.WAITING;
            expo = new ExponentialLaw(nu);
            expo2 = new ExponentialLaw(delta);
            this.body = body;
        }

        public void run() {
            while (true) {
                this.changeState();
            }
        }

        protected void changeState() {
            if ((this.state == FakeBench2.WAITING) ||
                    (this.state == FakeBench2.MIGRATED)) {
                synchronized (this) {
                    this.state = FakeBench2.MIGRATING;
                }

                //System.out.println("Agent migrating");
                this.stateLength = (long) (expo2.next() * 1000);
                this.wait((double) stateLength);
                // System.out.println("Agent calling server");
                long startTime = System.currentTimeMillis();
                this.callServer();
                long endTime = System.currentTimeMillis();
                synchronized (this) {
                    this.state = FakeBench2.MIGRATED;
                    this.notifyAll();
                }
                System.out.println(
                    "TimedMigrationManagerWithServer: Migration Time " +
                    (endTime - startTime + stateLength));
                this.wait(expo.next() * 1000);
            }
        }

        public int getState() {
            return this.state;
        }

        public void call() {
            this.state = WAITING;
        }

        public void callServer() {
            //            System.out.println("Agent.callServer id = " + id);
            locationServer.updateLocation(id, body);
        }

        public void foundYou() {
            if (this.state == FakeBench2.MIGRATED) {
                this.state = FakeBench2.WAITING;
            }
        }

        public synchronized void waitEndOfMigration() {
            while (this.state == FakeBench2.MIGRATING) {
                try {
                    wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
