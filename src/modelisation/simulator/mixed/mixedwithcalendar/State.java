package modelisation.simulator.mixed.mixedwithcalendar;

public class State {
    protected double time;
    protected String name;
    protected int[] state;

    //    public State(double time, String name) {
    //        this.time = time;
    //        this.name = name;
    //    }
    public State(int[] state) {
        this(0, state);
    }

    public State(double time, int[] state) {
        this.state = state;
        if ((state[0] == 1) && (state[1] == 1) && (state[2] == 1)) {
            this.state[3] = 0;
        }

        this.time = time;
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.toString();
        }
        return name;
    }

    public double getTime() {
        return time;
    }

    public int getHops() {
        return state[0];
    }

    public int getAgentState() {
        return state[1];
    }

    public int getSourceState() {
        return state[2];
    }

    public int getStar() {
        return state[3];
    }

    public String toString() {
        //        if ((state[0] == 1) && (state[1] == 1) && (state[2] == 1)) {
        //            return "1,1,1";
        //        }
        return state[0] + "," + state[1] + "," + state[2] +
        ((state[3] == 1) ? "*" : "");
    }

    public boolean equals(Object o) {
        State tmpState = (State) o;
        return ((this.getHops() == tmpState.getHops()) &&
        (this.getAgentState() == tmpState.getAgentState()) &&
        (this.getSourceState() == tmpState.getSourceState()) &&
        (this.getStar() == tmpState.getStar()));
    }
}
