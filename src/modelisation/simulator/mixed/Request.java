/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: May 2, 2002
 * Time: 6:34:59 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package modelisation.simulator.mixed;

import org.apache.log4j.Logger;

import modelisation.simulator.common.SimulatorElement;


public class Request extends SimulatorElement {
    protected static Logger logger = Logger.getLogger(Request.class.getName());
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
