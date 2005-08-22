package modelisation.simulator.server;

import modelisation.simulator.common.SimulatorElement;
import modelisation.statistics.ExponentialLaw;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Agent extends SimulatorElement {
    protected RandomNumberGenerator expoDelta;
    protected RandomNumberGenerator expoNu;
    protected double delta;
    protected double nu;
    protected double gamma2;

    //  protected double remainingTime;
    protected double currentMigrationLength;
    protected double startTime;
    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int MIGRATED = 2;
    public final static int CALLING_SERVER = 3;
    public boolean migrated;

    //  protected int state;

    /**
     * Get the value of delta.
     * @return Value of delta.
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Set the value of delta.
     * @param v  Value to assign to delta.
     */
    public void setDelta(double v) {
        this.delta = v;
    }

    /**
     * Get the value of nu.
     * @return Value of nu.
     */
    public double getNu() {
        return nu;
    }

    /**
     * Set the value of nu.
     * @param v  Value to assign to nu.
     */
    public void setNu(double v) {
        this.nu = v;
    }

    protected ExponentialLaw expo;

    public Agent() {
    }

    public Agent(double nu, double delta) {
        this.delta = delta;
        this.nu = nu;
        this.state = WAITING;
    }

    public void waitBeforeMigration() {
        //       time=1000/nu;
        if (this.expoNu == null) {
            this.expoNu = RandomNumberFactory.getGenerator("nu");
            //            this.expoNu.initialize(nu, System.currentTimeMillis() + 42673931);
            this.expoNu.initialize(nu, 39566417);
        }
        double time = expoNu.next() * 1000;
        this.remainingTime = time;
        System.out.println("AgentWithExponentialMigrationAndServer: waited " +
            time + " before migration");
    }

    public void startMigration(double startTime) {
        this.startTime = startTime;
        if (this.expoDelta == null) {
            this.expoDelta = RandomNumberFactory.getGenerator("delta");
            //            this.expoDelta.initialize(delta, System.currentTimeMillis() + 395672917);
            this.expoDelta.initialize(delta, 58373435);
        }

        double time = expoDelta.next() * 1000;
        this.remainingTime = time;
        this.state = MIGRATING;
        System.out.println("Agent: migration started, will last " +
            this.remainingTime);
    }

    //    public void callServer(double time) {
    //        System.out.println("Agent.callServer: Calling server will last " +
    //                           time);
    //        this.state = CALLING_SERVER;
    //        this.remainingTime = time;
    //    }

    /**
     * Called by the simulator when the server has sent a
     * good reply to the source
     */
    public void foundYou() {
        // if (this.state == MIGRATED) {
        //      this.state = WAITING;
        //  }
        this.migrated = false;
    }

    public void endMigration(double endTime) {
        //    this.state = MIGRATED;
        this.state = WAITING;
        this.migrated = true;
        System.out.println("Agent: Migration Time " + (endTime - startTime));
        this.waitBeforeMigration();
    }

    public String toString() {
        switch (this.state) {
        case WAITING:return "WAITING " + (migrated ? "migrated" : "reachable");
        case MIGRATING:return "MIGRATING " +
            (migrated ? "migrated" : "reachable");
        case MIGRATED:return "MIGRATED ";
        case CALLING_SERVER:return "CALLING_SERVER ";
        }
        return null;
    }

    //    public void migrationOver() {
    //        System.out.println("TimedMigrationManager: length of the migration " + this.currentMigrationLength);
    //    }
}
