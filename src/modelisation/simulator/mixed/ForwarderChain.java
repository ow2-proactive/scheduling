package modelisation.simulator.mixed;

import modelisation.simulator.common.SimulatorElement;
import java.util.LinkedList;
import java.util.ListIterator;

public class ForwarderChain extends SimulatorElement {

    public static final int IDLE = 0;
    public static final int COMMUNICATING = 1;
    public static final int WAITING_AGENT = 2;
    public static final int TENSIONING = 3;


    protected LinkedList list;
    protected Simulator simulator;
    protected int position;
    protected Source source;
    protected Agent agent;

    protected int objectNumber;
    protected boolean hasBeenForwarded;

    protected int forwarderCount;

    /**
     * Create a forwarding chain
     * @e : the agent at the end of the chain
     */
    public ForwarderChain(Simulator s) {
        this.list = new LinkedList();
        this.simulator = s;
        this.position = 0;
        this.source = source;
        this.remainingTime = 50000000;
    }

    public void setSource(Source s) {
        this.source = s;
    }

    public void setAgent(Agent a) {
        this.agent = a;
    }

    public void add(Forwarder f) {
        f.setLifeTime(this.simulator.getForwarderLifeTime());
        System.out.println("ForwarderChain.add with lifetime "
                           + f.getRemainingTime());
//        f.setLifeTime(10);
        this.list.add(f);
    }

//    public Forwarder getNext(Forwarder f) {
//        ListIterator li = list.listIterator(0);
//        while (li.hasNext() && (!li.next().equals(f))) ;
//        return (Forwarder) li.next();
//    }

    public boolean removeForwarder(Forwarder f) {
        return list.remove(f);
    }

    public int length() {
        return list.size();
    }

//    public void startCommunication(int forwarderNumber) {
//        System.out.println("ForwarderChain.startCommunication");
//        this.remainingTime = this.communicationLength();
//        System.out.println("ForwarderChain.startCommunication will last " +
//                           this.remainingTime);
//        this.state = COMMUNICATING;
//    }

    protected int getPositionFromNumber(int forwarderNumber) {
        ListIterator li = list.listIterator(0);
        Forwarder f = null;
        while (li.hasNext()) {
            f = (Forwarder) li.next();
            if (f.getNumber() == forwarderNumber) {
                return list.indexOf(f);
            }
        }
        return -1; //this.list.size();
    }


//    public void reachElementTest() {
//        if (this.list.size() > 0) {
//            //the agent has migrated since the last call
//            this.list.clear();
//            this.source.communicationFailed();
//            this.remainingTime = 5000000;
//            this.state = IDLE;
//        } else {
//            this.reachElementAgent(this.agent);
//        }
//    }

    /**
     * Performs the communication from the source to the agent
     */
    public void reachElement() {
        this.position = getPositionFromNumber(objectNumber);
        if (this.position < 0) {
            //the element we are looking for in not in the
            //forwarder chain, we check to see if it is the agent
            if (this.agent.getNumber() == objectNumber) {
//                System.out.println("ForwarderChain.reachElement agent reached");
//                System.out.println("ForwarderChain.reachElement hasBeenForwarded "
//                                   + this.hasBeenForwarded);
                this.reachElementAgent(agent);
            } else {
                //  this.source.communicationFailed();
                this.communicationFailed();
            }
        } else {
            this.reachElementForwarder((Forwarder) this.list.get(this.position));
        }

    }
    /*
        if (this.position >= this.list.size()) {
            //the position is not in the list
            //we check to see if it is the agent
//            if (this.objectNumber == agent.getNumber()) {
            System.out.println("ForwarderChain.reachElement agent reached");
            this.reachElementAgent(agent);
//            } else {
//                System.out.println("ForwarderChain.reachElement outside forwarder chain");
//               this.state = Agent.REFUSED;
//            }
        } else {
            this.reachElementForwarder((Forwarder) this.list.get(this.position));
        }
    }       */

    protected void reachElementAgent(Agent a) {
        int returnValue = a.receiveMessage();
        switch (returnValue) {
            case Agent.WAITING:
//                this.state = IDLE;
//                this.remainingTime = 5000000;
                //we should empty the forwarder chain here
                //from 0 to position-1
//                this.list.clear();
//                this.source.agentReached(a.getNumber());
                if (this.hasBeenForwarded) {
                    this.startTensioning();
                } else {
                    this.endOfCommunication();
                }
                break;
            case Agent.REFUSED:
                //we should maybe flush the forwarder chain here
//                this.source.communicationFailed();
                this.communicationFailed();
                break;
            case Agent.BLOCKED:
                //this.position++;
                this.state = WAITING_AGENT;
                this.remainingTime = a.getRemainingTime();
                System.out.println(" Source: waiting for the agent will last " +
                                   this.remainingTime);
                break;
        }
    }

    protected void reachElementForwarder(Forwarder f) {
//        System.out.println("ForwarderChain.reachElementForwarder");
        int returnValue = f.receiveMessage();
        switch (returnValue) {
            case Forwarder.ACTIF:
                this.hasBeenForwarded = true;
                this.objectNumber++;
                this.remainingTime = this.communicationLength();
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
        this.objectNumber = forwarderNumber;
        this.hasBeenForwarded = false;
        this.forwarderCount=0;
//        System.out.println("ForwarderChain.startCommunication");
        this.remainingTime = this.communicationLength();
//        System.out.println("ForwarderChain.startCommunication will last " +
//                           this.remainingTime);
//        System.out.println("ForwarderChain.startCommunication length of the chain is " +
//                           this.list.size());
//        System.out.println("ForwarderChain.startCommunication looking for object " +
//                           forwarderNumber);
        this.position = this.getPositionFromNumber(forwarderNumber);
//        System.out.println("ForwarderChain.startCommunication position "
//                           + this.position);
        this.state = COMMUNICATING;
    }

    protected double communicationLength() {
        return simulator.getCommunicationTimeForwarder();
    }

    protected void communicationFailed() {
        this.remainingTime = 50000;
        this.source.communicationFailed();
        this.state = IDLE;
        System.out.println("Communication failed after " + this.forwarderCount
        + " forwarders");
    }

    protected void endOfCommunication() {
        this.state = IDLE;
        this.remainingTime = 5000000;
        //we should empty the forwarder chain here
        //from 0 to position-1
        this.list.clear();
        this.source.agentReached(this.agent.getNumber());
      System.out.println("Communication succeeded after " + this.forwarderCount
        + " forwarders");
    }


    /**
     * Called by the ForwarderChain when the source reaches the agent
     * after having been through forwarders
     */
    public void startTensioning() {
//        System.out.println("ForwarderChain.startTensioning");
        this.state = TENSIONING;
        this.remainingTime = simulator.getCommunicationTimeForwarder();
        this.agent.startTensioning(this.remainingTime);

    }


    public double getRemainingTime() {
        double minTime = this.remainingTime;
        ListIterator li = this.list.listIterator(0);
        while (li.hasNext()) {
            minTime = Math.min(((Forwarder) li.next()).getRemainingTime(), minTime);
        }
        return minTime;
    }

    public void update(double time) {
        this.updateForwarders(time);
        if (this.remainingTime == 0) {
            switch (this.state) {
//                case IDLE:
//                    this.state = COMMUNICATING;
//                    this.position = 0;
//                    this.remainingTime = this.communicationLength();
//                    break;
                case COMMUNICATING:
                    this.reachElement();
                    break;
                case WAITING_AGENT:
                    this.reachElement();
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
        ListIterator li = this.list.listIterator(0);
        while (li.hasNext()) {
            ((Forwarder) li.next()).decreaseRemainingTime(minTime);
        }
    }

    public void updateForwarders(double time) {
       System.out.println("ForwarderChain.updateForwarders " + this.list.size() );
        ListIterator li = this.list.listIterator(0);
        while (li.hasNext()) {
            ((Forwarder) li.next()).update(time);
        }
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
        tmp.append(" size = " + list.size());
        tmp.append(" position = ").append(position);
        tmp.append(" objectNumber =").append(objectNumber);
        tmp.append(" remainingTime = ").append(remainingTime);
        tmp.append("\n");
        ListIterator li = list.listIterator(0);
        while (li.hasNext()) {
            tmp.append("->");
            tmp.append(((Forwarder) li.next()).getNumber());
        }
        return tmp.toString();
    }
}
