package modelisation.simulator.common;

public class SimulatorElement {
    protected int state;
    protected double remainingTime;

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
}
