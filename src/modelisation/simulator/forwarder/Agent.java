package modelisation.simulator.forwarder;

import modelisation.statistics.ExponentialLaw;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Agent {
    private RandomNumberGenerator expoDelta;
    private RandomNumberGenerator expoNu;
    private double delta;
    private double nu;
    private double remainingTime;
    private double currentMigrationLength;
    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int WAITING_FOR_TENSIONING = 2;
    private int state;

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
        //	this.expoDelta = new ExponentialLaw(delta);
        //	this.expoNu = new ExponentialLaw(nu);
    }

    public void waitBeforeMigration() {
        if (this.expoNu == null) {
            this.expoNu = RandomNumberFactory.getGenerator("nu");
            this.expoNu.initialize(nu, 39566417);
        }
        double time = expoNu.next() * 1000;
        this.remainingTime = time;
        this.state = WAITING;
        System.out.println("Agent: waiting " + time + " before migration");
    }

    public void startMigration() {
        if (this.expoDelta == null) {
            this.expoDelta = RandomNumberFactory.getGenerator("delta");
            this.expoDelta.initialize(delta, 58373435);
        }

        double time = expoDelta.next() * 1000;
        this.remainingTime = time;
        this.currentMigrationLength = time;
        this.state = MIGRATING;
        System.out.println("Agent: migration started, will last " +
            this.currentMigrationLength);
    }

    public void endMigration() {
        this.state = WAITING;
        System.out.println("Agent: length of the migration " +
            currentMigrationLength);
    }

    public void waitEndOfTensioning(double length) {
        this.state = WAITING_FOR_TENSIONING;
        this.remainingTime = length;
    }

    public void decreaseRemainingTime(double l) {
        this.remainingTime -= l;
    }

    public double getRemainingTime() {
        return this.remainingTime;
    }

    public int getState() {
        return this.state;
    }

    public void migrationOver() {
        System.out.println("TimedMigrationManager: length of the migration " +
            this.currentMigrationLength);
    }

    public String toString() {
        switch (state) {
        case WAITING:
            return "waiting";
        case MIGRATING:
            return "migrating";
        case WAITING_FOR_TENSIONING:
            return "waiting end of tensioning";
        }
        return null;
    }
}
