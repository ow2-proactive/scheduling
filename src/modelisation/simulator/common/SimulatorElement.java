package modelisation.simulator.common;

public abstract class SimulatorElement {
    protected int state;
    protected double remainingTime;
    protected int id;
    //indicates wether this object should report
    //messages to the simulator.
    protected boolean log = false;

    /**
     * Get the value of state.
     * @return Value of state.
     */
    public int getState() {
        return state;
    }

    public double getRemainingTime() {
        return this.remainingTime;
    }

    public void setRemainingTime(double l) {
        this.remainingTime = l;
    }

    public void decreaseRemainingTime(double l) {
        this.remainingTime -= l;
    }

    //    public abstract void update(double time);

    /**
     * Method update.
     * @param d
     */
    public void update(double d) {
    }

    /**
 * Sets the log.
 * @param log The log to set
 */
    public void setLog(boolean log) {
        this.log = log;
    }

    public void end() {
    }
}