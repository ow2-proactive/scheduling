package modelisation.simulator.mixed.mixedwithcalendar;

import modelisation.simulator.common.SimulatorElement;


public class Request extends SimulatorElement {
    public static final int SOURCE = 0;
    public static final int AGENT = 1;
    protected int number;
    protected int senderID;
    protected double creationTime;

    //this is just to test  a new way to get mu using agent requests
    public double timeToSubstract;

    public Request() {
    }

    public Request(int state, int number) {
        this(state, number, -1);
    }

    public Request(int state, int number, int senderID) {
        this(state, number, senderID, 0);
    }

    public Request(int state, int number, int senderID, double creationTime) {
        this.state = state;
        this.number = number;
        this.senderID = senderID;
        this.creationTime = creationTime;
    }

    public boolean isFromAgent() {
        return (this.state == AGENT);
    }

    public int getNumber() {
        return this.number;
    }

    public int getSenderID() {
        return this.senderID;
    }

    public double getCreationTime() {
        return this.creationTime;
    }

    public void setCreationTime(double creationTime) {
        this.creationTime = creationTime;
    }

    public void update(double time) {
    }

    public String toString() {
        String s = new String("request " +
                ((state == SOURCE) ? " from source" : " from agent"));
        s = s + " number " + number;
        return s;
    }
}
