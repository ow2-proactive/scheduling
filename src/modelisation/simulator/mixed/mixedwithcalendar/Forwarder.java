/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Apr 30, 2002
 * Time: 10:38:32 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package modelisation.simulator.mixed.mixedwithcalendar;

import org.apache.log4j.Logger;


public class Forwarder extends Agent {

    static Logger logger = Logger.getLogger(Forwarder.class.getName());
    public static final int DEAD = 0;
    public static final int ACTIF = 1;
    public static final int UPDATING_SERVER = 2;
    public static int DefaultState;
    public static int Current_Number = 0;
    //    protected double remainingTime;
    //   protected int number;
    protected Server server;
    protected Simulator simulator;
    protected int agentID;
    protected Event currentEvent;

    static {
        if ("DEAD".equals(System.getProperties().getProperty("forwarder.state"))) {
            Forwarder.DefaultState = DEAD;
        } else {
            Forwarder.DefaultState = ACTIF;
        }
        if (logger.isInfoEnabled()) {
            logger.info("--- Forwarders are " + Forwarder.DefaultState);
        }
    }

    public Forwarder() {
        this(Double.MAX_VALUE, Current_Number++);
    }

    public Forwarder(int number, Server s, Simulator s2, int id) {
        this(Double.MAX_VALUE, number);
        this.server = s;
        this.simulator = s2;
        this.agentID = id;
    }

    public Forwarder(double lifeTime, int n) {
        this.remainingTime = lifeTime;
        //      this.number = n;
        this.migrationCounter = n;
        //      this.state = ACTIF;
        this.state = Forwarder.DefaultState;
    }

    public void setLifeTime(double l) {
        if ("INFINITE".equals(System.getProperty("forwarder.lifetime"))) {
            this.state = ACTIF;
            //   this.remainingTime = new Double(Double.MAX_VALUE).doubleValue();
            //this.notifyEvent();
        } else {
            this.state = DefaultState;
            if (this.state == ACTIF) {
                this.remainingTime = l;
                this.notifyEvent("Alive");
            } else {
                //we do nothing, the forwarder is born dead
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Forwarder: setLifeTime " + l);
        }
    }

    public void endLife() {
        if ("INFINITE".equals(System.getProperty("forwarder.lifetime"))) {
            this.state = ACTIF;
            //   this.remainingTime = new Double(Double.MAX_VALUE).doubleValue();
            //this.notifyEvent();
        } else {
            this.state = DEAD;
        }
    }

    public void notifyEvent(String description) {
        this.timeNextEvent = this.remainingTime + 
                             this.simulator.getCurrentTime();
        if (this.currentEvent != null) {
            this.simulator.removeEvent(this.currentEvent);
        }
        this.currentEvent = new Event(this.timeNextEvent, this, description);
        this.simulator.addEvent(currentEvent);
    }

    public int getAgentID() {
        return this.id;
    }

    public int getMigrationCounter() {
        return this.migrationCounter;
    }

    public static void setDefaultState(int i) {
        Forwarder.DefaultState = i;
    }

    public int receiveMessage() {
        return this.state;
    }

    public void startCommunicationServer() {
        this.state = UPDATING_SERVER;
        this.remainingTime = simulator.generateCommunicationTimeServer();
        this.notifyEvent("Call Server");
    }

    public void endCommunicationServer() {
        //        System.out.println("Forwarder.endCommunication");
        this.state = DEAD;
        //this.remainingTime = 50000000;
        //this.notifyEvent();
        //we send the number of the next forwarder
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Forwarder: calling server, next number is " + 
                    (this.migrationCounter + 1));
        }
        this.server.receiveRequestFromForwarder(this.migrationCounter + 1, 
                                                this.id);
    }

    /**
    *  Remove any pending event in the calendar
    */
    public void clean() {
        if (this.currentEvent != null) {

            boolean b = this.simulator.removeEvent(this.currentEvent);
            //            if (!b) {
            //                System.out.println(" Forwarder clean() " + b);
            //                System.out.println(" Forwarder state " + this.state);
            //            }
        }
    }

    public void update(double time) {
        //    if (this.remainingTime == 0) {
        this.currentEvent = null;
        switch (this.state) {
            case ACTIF:
                //                    this.state = DEAD;
                //                    this.remainingTime = 50000000;
                if (!"NO".equals(System.getProperties().getProperty(
                                         "forwarder.callserver"))) {
                    this.startCommunicationServer();
                } else {
                    this.state = DEAD;
                    this.remainingTime = Double.MAX_VALUE;
                    //  this.notifyEvent();
                }
                break;
            case UPDATING_SERVER:
                //                    this.state = DEAD;
                this.endCommunicationServer();
                break;
            case DEAD:
                this.setRemainingTime(Double.MAX_VALUE);
                // this.notifyEvent();
                break;
        }
        // }
    }

    public String getStateAsLetter() {
        switch (this.state) {
            case DEAD:
                return "d";
            case ACTIF:
                return "a";
            case UPDATING_SERVER:
                return "u";
        }
        return "";
    }

    public double getRemainingTime() {
        if (this.currentEvent != null) {
            return this.currentEvent.getTime() - 
                   this.simulator.getCurrentTime();
        } else {
            return Double.MAX_VALUE;
        }
        //super.getRemainingTime();
    }

    public String getName() {
        return "Forwarder" + this.id;
    }

    public String toString() {

        StringBuffer tmp = new StringBuffer();
        switch (this.state) {
            case DEAD:
                tmp.append("DEAD");
                break;
            case ACTIF:
                tmp.append("ACTIF");
                break;
            case UPDATING_SERVER:
                tmp.append("UPDATNG_SERVER");
                break;
        }
        tmp.append(" remainingTime = ").append(remainingTime);
        return tmp.toString();
    }

    public boolean equals(Forwarder f) {
        return ((this.agentID == f.getAgentID()) && 
               (this.migrationCounter == f.getMigrationCounter()));
    }
}