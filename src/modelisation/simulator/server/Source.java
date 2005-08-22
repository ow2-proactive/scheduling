package modelisation.simulator.server;

import modelisation.simulator.common.SimulatorElement;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Source extends SimulatorElement {
    public static final int WAITING = 0;
    public static final int COMMUNICATION = 1;
    public static final int WAITING_FOR_AGENT = 2;
    public static final int CALLING_SERVER = 3;
    public static final int WAITING_FOR_SERVER = 4;
    public static final int WAITING_ERROR_MESSAGE = 5;
    protected double startTime;
    protected double endTime;
    protected boolean start;
    protected double lambda;
    protected RandomNumberGenerator expo;

    public Source() {
    }

    public Source(double lambda) {
        this.lambda = lambda;
    }

    public void waitBeforeCommunication() {
        this.start = false;
        //        if (this.expo == null)
        //            this.expo = new ExponentialLaw(lambda, System.currentTimeMillis() + 15498387);
        if (this.expo == null) {
            this.expo = RandomNumberFactory.getGenerator("lambda");
            //            this.expo.initialize(lambda, System.currentTimeMillis() + 15498387);
            this.expo.initialize(lambda, 8936917);
        }
        double time = expo.next() * 1000;

        //        time = 1000/lambda;
        System.out.println("Source: calling the agent after  " + time);
        this.state = WAITING;
        this.remainingTime = time;
    }

    public void startCommunication(double startTime, double length) {
        this.start = true;
        this.state = COMMUNICATION;
        this.remainingTime = length;
        this.startTime = startTime;
        System.out.println(">>>>> Source: communication started at time " +
            startTime);
        System.out.println("Source: communication started will last   " +
            remainingTime);
    }

    public void continueCommunication(double length) {
        this.start = false;
        System.out.println("Source: continue for " + length);
        this.remainingTime = length;
        this.state = COMMUNICATION;
    }

    public void waitForAgent(double length) {
        this.start = false;
        System.out.println("Source: waiting for the agent will last " + length);
        this.remainingTime = length;
        this.state = WAITING_FOR_AGENT;
    }

    public void waitForError(double length) {
        this.start = false;
        this.remainingTime = length;
        //        System.out.println("DXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        this.state = WAITING_ERROR_MESSAGE;
    }

    public void endCommunication(double endTime) {
        this.start = false;
        this.state = WAITING;
        this.endTime = endTime;
        System.out.println("<<<<< Source: communication finished at time " +
            endTime);
        System.out.println(
            "TimedProxyWithLocationServer:  .............. done after " +
            (endTime - startTime) + " for method echo");
    }

    public void startCommunicationServer(double length) {
        this.start = false;
        System.out.println(
            "Source: communication with server started will last   " + length);
        this.state = CALLING_SERVER;
        this.remainingTime = length;
    }

    public void waitForServer() {
        this.start = false;
        this.state = WAITING_FOR_SERVER;
        this.setRemainingTime(500000);
    }

    /**
     * Get the value of lambda.
     * @return Value of lambda.
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Set the value of lambda.
     * @param v  Value to assign to lambda.
     */
    public void setLambda(double v) {
        this.lambda = v;
    }

    public void removeStar() {
        this.start = false;
    }

    public String toString() {
        switch (this.state) {
        case WAITING:return "WAITING ";
        case COMMUNICATION:return "COMMUNICATION " + (start ? "star " : "");
        case WAITING_FOR_AGENT:return "WAITING_FOR_AGENT ";
        case CALLING_SERVER:return "CALLING_SERVER ";
        case WAITING_FOR_SERVER:return "WAITING_FOR_SERVER ";
        case WAITING_ERROR_MESSAGE:return "WAITING_ERROR_MESSAGE ";
        }
        return null;
    }
}
