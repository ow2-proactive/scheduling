package modelisation.simulator.forwarder;

import modelisation.statistics.ExponentialLaw;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;

public class Source {


    public static final int WAITING = 0;
    public static final int COMMUNICATION = 1;
    public static final int WAITING_FOR_AGENT = 2;
    public static final int TENSIONING = 3;

    private int state;

    private double startTime;
    private double endTime;

    private double remainingTime;

    private double lambda;
    protected RandomNumberGenerator expoLambda;

    public Source() {
    }

    public Source(double lambda) {
        this.lambda = lambda;
        //	this.expo = new ExponentialLaw(lambda);
    }

    public void waitBeforeCommunication() {
      if (this.expoLambda == null) {
                this.expoLambda = RandomNumberFactory.getGenerator("lambda");
                this.expoLambda.initialize(lambda,  8936917);
            }
       double time = expoLambda.next() * 1000;
//        double time = 1/lambda*1000;
        System.out.println("Source: calling the agent in  " + time);
        this.state = WAITING;
        this.remainingTime = time;
    }


    public void startCommunication(double startTime, double length) {
        this.state = COMMUNICATION;
        this.remainingTime = length;
        this.startTime = startTime;
        System.out.println("Source: communication started will last   " + remainingTime);
    }

    public void continueCommunication(double length) {
        System.out.println("Source: continue for " + length);
        this.remainingTime = length;
        this.state = COMMUNICATION;
    }

    public void waitForAgent(double length) {
        System.out.println("Source: waiting for the agent will last " + length);
        this.remainingTime = length;
        this.state = WAITING_FOR_AGENT;
    }

    public void tensioning(double length) {
        System.out.println("Tensioning");
        this.remainingTime = length;
        this.state = TENSIONING;
    }


    public void endCommunication(double endTime) {
        this.state = WAITING;
        this.endTime = endTime;
        System.out.println("TimedProxyWithLocationServer:  .............. done after " + (endTime - startTime));
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

    public String toString() {
        switch (state) {
            case WAITING:
                {
                    return "waiting";
                }
            case COMMUNICATION:
                {
                    return "calling agent";
                }
           case WAITING_FOR_AGENT:
                {
                    return "waiting for agent";
                }
            case TENSIONING:
                {
                    return "tensioning";
                }
        }
        return null;
    }

}
