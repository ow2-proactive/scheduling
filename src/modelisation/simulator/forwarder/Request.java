package modelisation.simulator.forwarder;

import modelisation.statistics.ExponentialLaw;


public class Request {
    public static int BLOCKED = 0;
    public static int RUNNING = 1;
    private int state;
    private int forwarded;
    private double startTime;
    private double remainingTime;
    private int hops;
    private ExponentialLaw expoGamma;

    public Request() {
    }

    public Request(double startTime, int hops, double gamma) {
        this.expoGamma = new ExponentialLaw(gamma);
        this.hops = hops;
        this.startTime = startTime;
        this.remainingTime = this.expoGamma.next() * 1000;
        this.state = RUNNING;
        System.out.println("Request: the communication will last " +
            this.remainingTime);
    }

    public double getRemainingTime() {
        return this.remainingTime;
    }

    public double getStartTime() {
        return this.startTime;
    }

    public void setRemainingTime(double l) {
        this.remainingTime = l;
    }

    public void decreaseRemainingTime(double l) {
        this.remainingTime -= l;
    }

    public int getHops() {
        return this.hops;
    }

    public void setHops(int i) {
        this.hops = i;
    }

    public void doNextHop() {
        this.remainingTime = this.expoGamma.next() * 1000;
        this.state = RUNNING;
        this.forwarded++;
    }

    public int getState() {
        return this.state;
    }

    public void block(double time) {
        this.state = BLOCKED;
        this.remainingTime = time;
    }

    public void unblock() {
        this.state = RUNNING;
        this.hops++;
        this.remainingTime = this.expoGamma.next() * 1000;
    }

    public void forwarded() {
        this.forwarded++;
    }

    public int getForwarded() {
        return this.forwarded;
    }
}
