package modelisation.simulator.mixed;

import java.util.LinkedList;
import java.util.ListIterator;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;


public class ForwarderChain extends SimulatorElement {
    public static final int IDLE = 0;
    public static final int COMMUNICATING = 1;
    public static final int WAITING_AGENT = 2;
    public static final int TENSIONING = 3;
    //    protected LinkedList list;
    protected Forwarder[] list;
    protected int listSize;
    protected Simulator simulator;
    protected int position;
    protected Source source;
    protected Agent agent;
    protected int objectNumber;
    protected boolean hasBeenForwarded;
    protected int forwarderCount;
    protected Averagator averagatorGamma1;
    protected Averagator averagatorForwarderCount;

    /**
     * Create a forwarding chain
     * @e : the agent at the end of the chain
     */
    public ForwarderChain(Simulator s) {
        this.list = new Forwarder[10];
        this.simulator = s;
        this.position = 0;
        this.source = source;
        this.setRemainingTime(500000);
        this.averagatorGamma1 = new Averagator();
        this.averagatorForwarderCount = new Averagator();
    }

    public void setSource(Source s) {
        this.source = s;
    }

    public void setAgent(Agent a) {
        this.agent = a;
    }

    public void add(Forwarder f) {
        f.setLifeTime(this.simulator.generateForwarderLifeTime());
        if (log) {
            this.simulator.log(
                    "ForwarderChain.add with lifetime " + 
                    f.getRemainingTime() + " with number " + 
                    f.migrationCounter);
        }
        //        f.setLifeTime(10);
        //        this.list.add(f);
        if (this.listSize == this.list.length) {
            Forwarder[] tmp = new Forwarder[listSize];
            System.arraycopy(this.list, 0, tmp, 0, this.list.length);
            this.list = new Forwarder[2 * listSize];
            System.arraycopy(tmp, 0, this.list, 0, tmp.length);
        }
        this.list[listSize] = f;
        this.listSize++;
    }

    public int length() {
        return list.length;
    }

    protected int getPositionFromNumber(int forwarderNumber) {
        //       Forwarder f = null;
        for (int i = 0; i < this.listSize; i++) {
            if (this.list[i].getNumber() == forwarderNumber) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Performs the communication from the source to the agent
     */
    public void reachElement() {
        this.position = getPositionFromNumber(objectNumber);
        if (log) {
            this.simulator.log(
                    "reachElement " + objectNumber + " at position " + 
                    this.position);
        }
        if (this.position < 0) {
            //the element we are looking for in not in the
            //forwarder chain, we check to see if it is the agent
            if (this.agent.getNumber() == objectNumber) {
                if (log) {
                    this.simulator.log(
                            "ForwarderChain.reachElement agent reached");
                }
                if (log) {
                    this.simulator.log(
                            "ForwarderChain.reachElement hasBeenForwarded " + 
                            this.hasBeenForwarded);
                }
                this.reachElementAgent(agent);
            } else {
                this.communicationFailed();
            }
        } else {
            this.reachElementForwarder(this.list[this.position]);
        }
    }

    protected void reachElementAgent(Agent a) {
        int returnValue = a.receiveMessage();
        switch (returnValue) {
            case Agent.WAITING:
                if (this.hasBeenForwarded) {
                    this.startTensioning();
                } else {
                    this.endOfCommunication();
                }
                break;
            case Agent.REFUSED:
                this.communicationFailed();
                break;
            case Agent.BLOCKED:
                this.state = WAITING_AGENT;
                this.setRemainingTime(a.getRemainingTime());
                if (log) {
                    this.simulator.log(
                            " Source: waiting for the agent will last " + 
                            this.remainingTime);
                }
                break;
        }
    }

    protected void reachElementForwarder(Forwarder f) {
        double tmp;
        int returnValue = f.receiveMessage();
        switch (returnValue) {
            case Forwarder.ACTIF:
                this.hasBeenForwarded = true;
                this.objectNumber++;
                tmp = this.communicationLength();
                this.setRemainingTime(tmp);
                this.averagatorGamma1.add(tmp);
                this.averagatorForwarderCount.add(1);
                this.forwarderCount++;
                break;
            default:
                this.communicationFailed();
                break;
        }
    }

    /**
     * Called by a source to init a communication
     */
    public void startCommunication(int forwarderNumber) {
        double tmp;
        this.objectNumber = forwarderNumber;
        this.hasBeenForwarded = false;
        this.forwarderCount = 0;
        if (log) {
            this.simulator.log("ForwarderChain.startCommunication");
        }
        tmp = this.communicationLength();
        this.setRemainingTime(tmp);
        this.averagatorGamma1.add(tmp);
        if (log) {
            this.simulator.log(
                    "ForwarderChain.startCommunication will last " + 
                    this.remainingTime);
            //            this.simulator.log(
            //                    "ForwarderChain.startCommunication length of the chain is " +
            //                    this.list.size());
            this.simulator.log(
                    "ForwarderChain.startCommunication looking for object " + 
                    forwarderNumber);
        }
        this.position = this.getPositionFromNumber(forwarderNumber);
        if (log) {
            this.simulator.log(
                    "ForwarderChain.startCommunication position " + 
                    this.position);
        }
        this.state = COMMUNICATING;
    }

    protected double communicationLength() {
        return simulator.generateCommunicationTimeForwarder();
    }

    protected void communicationFailed() {
        this.setRemainingTime(50000);
        this.source.communicationFailed();
        this.state = IDLE;
        if (log) {
            this.simulator.log(
                    "Communication failed after " + this.forwarderCount + 
                    " forwarders");
        }
    }

    protected void endOfCommunication() {
        this.state = IDLE;
        this.setRemainingTime(5000000);
        this.listSize = 0;
        this.list = new Forwarder[10];
        this.source.agentReached(this.agent.getNumber());
        if (log) {
            this.simulator.log(
                    "Communication succeeded after " + this.forwarderCount + 
                    " forwarders");
        }
    }

    /**
     * Called by the ForwarderChain when the source reaches the agent
     * after having been through forwarders
     */
    public void startTensioning() {
        if (log) {
            this.simulator.log("ForwarderChain.startTensioning");
        }
        this.state = TENSIONING;
        this.setRemainingTime(simulator.generateCommunicationTimeForwarder());
        this.agent.startTensioning(this.remainingTime);
    }

    public double getRemainingTime() {
        double minTime = this.remainingTime;
        for (int i = 0; i < this.listSize; i++) {
            minTime = Math.min(this.list[i].getRemainingTime(), minTime);
        }
        return minTime;
    }

    public void update(double time) {
        if (log) {
            this.simulator.log(this.toString());
        }
        this.updateForwarders(time);
        if (this.remainingTime == 0) {
            switch (this.state) {
                case COMMUNICATING:
                    this.reachElement();
                    break;
                case WAITING_AGENT:
                    if (this.agent.getState() == Agent.CALLING_SERVER) {
                        //         oooopsss, we have to be carreful here, the agent is actually calling the server
                        //         so its migration is not over yet
                        this.setRemainingTime(this.agent.getRemainingTime());
                    } else {
                        this.reachElement();
                    }
                    break;
                case TENSIONING:
                    this.endOfCommunication();
                    break;
            }
        }
    }

    /**
     * Decrease the remaining time of the forwarder chain
     * and its associated forwarders
     */
    public void decreaseRemainingTime(double minTime) {
        this.remainingTime -= minTime;
        for (int i = 0; i < this.listSize; i++) {
            this.list[i].decreaseRemainingTime(minTime);
        }
    }

    public void setRemainingTimoe(double time) {
        if (log) {
            this.simulator.log(
                    "setRemainingTime: old = " + this.remainingTime + 
                    " new = " + time);
        }
        this.remainingTime = time;
    }

    public void updateForwarders(double time) {
        //             if (log) { this.simulator.log(
        //              "ForwarderChain.updateForwarders "
        //                    + this.list.size()
        //                    + " elements");
        //             }
        for (int i = 0; i < this.listSize; i++) {
            this.list[i].update(time);
        }
    }

    public void end() {
        System.out.println(
                "* gamma1 = " + 1000 / this.averagatorGamma1.average());
        System.out.println(
                "* ForwarderCount = " + 
                this.averagatorForwarderCount.average() + " " + 
                this.averagatorForwarderCount.getCount());
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        switch (this.state) {
            case IDLE:
                tmp.append("IDLE");
                break;
            case COMMUNICATING:
                tmp.append("COMMUNICATING");
                break;
            case WAITING_AGENT:
                tmp.append("WAITING_AGENT");
                break;
        }
        //        tmp.append(" size = " + list.size());
        tmp.append(" size = " + this.listSize);
        tmp.append(" position = ").append(position);
        tmp.append(" objectNumber =").append(objectNumber);
        tmp.append(" remainingTime = ").append(remainingTime);
        tmp.append("\n");
        //        ListIterator li = list.listIterator(0);
        Forwarder ftmp = null;
        tmp.append("Source->");
        //        while (li.hasNext()) {
        //            ftmp = (Forwarder)li.next();
        //            tmp.append(ftmp.getNumber());
        //            tmp.append(ftmp.getStateAsLetter());
        //            tmp.append("->");
        //        }
        tmp.append("Agent");
        tmp.append(this.agent.getNumber());
        tmp.append(this.agent.getStateAsLetter());
        return tmp.toString();
    }
}