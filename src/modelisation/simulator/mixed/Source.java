package modelisation.simulator.mixed;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;


public class Source extends SimulatorElement {
    public static final int WAITING = 0;
    public static final int COMMUNICATION = 1;
    public static final int WAITING_FOR_AGENT = 2;
    public static final int COMMUNICATION_FAILED = 3;
    public static final int CALLING_SERVER = 4;
    public static final int WAITING_SERVER = 5;
    protected double startTime;
    protected double endTime;
    protected boolean start;
    protected double lambda;
    protected ForwarderChain forwarderChain;
    protected Simulator simulator;
    protected Server server;
    protected int currentLocation;
    protected double communicationServerStartTime;
    protected double processingServerStartTime;
    protected double tries;
    protected Averagator averagatorLambda;
    protected Averagator averagatorT;
    protected Averagator averagatorTries;
    protected Averagator averagatorGamma2;
    protected Averagator averagatorMu;

    public Source() {
    }

    public Source(Simulator s, double lambda) {
        this(s, lambda, -1);
    }

    public Source(Simulator s, double lambda, int ID) {
        this.lambda = lambda;
        this.averagatorT = new Averagator();
        this.averagatorTries = new Averagator();
        this.averagatorLambda = new Averagator();
        this.averagatorGamma2 = new Averagator();
        this.averagatorMu = new Averagator();
        this.simulator = s;
        this.waitBeforeCommunication();
        this.id = ID;
    }

//    public void log(String s) {
//        if (log) {
//            this.simulator.log(s);
//        }
//    }

    public void setForwarderChain(ForwarderChain fc) {
        this.forwarderChain = fc;
    }

    public void setServer(Server s) {
        this.server = s;
    }

    public void waitBeforeCommunication() {
        this.remainingTime = simulator.generateSourceWaitingTime();
        //       if (log)  {this.simulator.log(" Source: calling the agent after " + this.remainingTime); }
        this.averagatorLambda.add(this.remainingTime);
    }

    public void communicationFailed() {
        this.remainingTime = 0;
        this.state = COMMUNICATION_FAILED;
    }

    public void agentReached(int number) {
        //this.remainingTime = 0;
        this.currentLocation = number;
        this.endCommunication(simulator.getCurrentTime());
    }

    public void update(double time) {
        if (this.remainingTime == 0) {
            switch (this.state) {
                case WAITING:
                    this.startCommunication(time);
                    break;
                case COMMUNICATION:
                    break;
                case COMMUNICATION_FAILED:
                    if (log) {
                        this.simulator.log(
                                "Communication failed after " + 
                                (time - startTime));
                    }
                    this.communicationServerStartTime = time;
                    this.callServer();
                    break;
                case WAITING_FOR_AGENT:
                    break;
                case CALLING_SERVER:
                    this.state = WAITING_SERVER;
                    this.remainingTime = 5000000;
                    this.server.receiveRequestFromSource(this.id);
                    this.processingServerStartTime = time;
                    //this.forwarderChain.startCommunication(currentLocation);
                    break;
                case WAITING_SERVER:
                    this.state = COMMUNICATION;
                    this.remainingTime = 5000000;
                    if (log) {
                        this.simulator.log(
                                "Source: reply from server total " + 
                                (time - communicationServerStartTime)); 
                    }
                    if (log) {
                        this.simulator.log(
                                "Source: processing for server total " + 
                                (time - processingServerStartTime));
                    }
                    this.averagatorMu.add(time - processingServerStartTime);
                    this.tries++;
                    this.forwarderChain.startCommunication(currentLocation);
                    break;
            }
        }
    }

    public void callServer() {
        this.remainingTime = simulator.generateCommunicationTimeServer();
        //        this.server.receiveRequestFromSource();
        //       if (log) this.simulator.log("Source: communication with server started will last  " +
        //                 this.remainingTime);
        this.averagatorGamma2.add(this.remainingTime);
        this.state = CALLING_SERVER;
    }

    public void receiveReplyFromServer(int location) {
        //       if (log) this.simulator.log("Source.receiveReplyFromServer currentLocation " +
        //                 location);
        this.currentLocation = location;
        this.remainingTime = 0;
    }

    public void startCommunication(double startTime) {
        this.remainingTime = 5000000;
        this.state = COMMUNICATION;
        this.forwarderChain.startCommunication(currentLocation);
        this.startTime = startTime;
        this.tries = 1;
        //       if (log) this.simulator.log(">>>>> Source: communication started at time " + startTime);
    }

    public void endCommunication(double endTime) {
        //        this.start = false;
        this.state = WAITING;
        this.endTime = endTime;
        //        this.remainingTime = simulator.getSourceWaitingTime();
        //       if (log) this.simulator.log("<<<<< Source: communication finished at time " + endTime);
        if (log) {
            this.simulator.log(
                    "TimedProxyWithLocationServer:  .............. done after " + 
                    (endTime - startTime));
        }
        if (log) {
            this.simulator.log("Number of tries= " + tries);
        }
        this.averagatorTries.add(tries);
        this.averagatorT.add(endTime - startTime);
        this.waitBeforeCommunication();
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

    public void end() {
        System.out.println(
                "* lambda = " + 1000 / this.averagatorLambda.average());
        System.out.println("* T Source  = " + this.averagatorT.average());
        System.out.println("* Tries  = " + this.averagatorTries.average());
        System.out.println(
                "* gamma2  = " + 1000 / this.averagatorGamma2.average());
        System.out.println(
                "* mu from source = " + 
                1000 / (this.averagatorMu.average() - this.averagatorGamma2.average()));
    }

    //
    //   public void removeStar() {
    //      this.start = false;
    //   }
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        switch (this.state) {
            case WAITING:
                tmp.append("WAITING");
                break;
            case COMMUNICATION:
                tmp.append("COMMUNICATION");
                break;
            case WAITING_FOR_AGENT:
                tmp.append("WAITING_AGENT");
                break;
            case CALLING_SERVER:
                tmp.append("CALLING_SERVER");
                break;
            case WAITING_SERVER:
                tmp.append("WAITING_SERVER");
                break;
        }
        tmp.append(" remainingTime = ").append(remainingTime);
        return tmp.toString();
    }
}