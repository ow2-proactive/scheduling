/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Apr 30, 2002
 * Time: 10:38:32 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package modelisation.simulator.mixed;


public class Forwarder extends Agent {

    public static final int DEAD = 0;
    public static final int ACTIF = 1;
    public static final int UPDATING_SERVER = 2;

    public static int Current_Number = 0;

//    protected double remainingTime;
    protected int number;
    protected Server server;
    protected Simulator simulator;

    public Forwarder() {
        this(5000000, Current_Number++);
    }

    public Forwarder(int number, Server s, Simulator s2) {
        this(5000000, number);
        this.server = s;
        this.simulator = s2;
    }

    public Forwarder(double lifeTime, int n) {
        this.remainingTime = lifeTime;
        this.number = n;
        this.state = ACTIF;
    }

    public void endLife() {
        this.state = DEAD;
    }

    public void setLifeTime(double l) {
        this.remainingTime = l;
    }

//    public void decreaseLifeTime(double l) {
//        this.lifeTime -= l;
//    }

    public int receiveMessage() {
//        System.out.println("Forwarder.receiveMessage state = " + this.state);
//        System.out.println("Forwarder.receiveMessage " + this);
        return this.state;
    }

    public void startCommunicationServer() {
//        System.out.println("Forwarder.startCommunicationServer");
        this.state = UPDATING_SERVER;
        this.remainingTime = simulator.getCommunicationTimeServer();

    }

    public void endCommunicationServer() {
//        System.out.println("Forwarder.endCommunication");
        this.state = DEAD;
        this.remainingTime = 50000000;
        //we send the number of the next forwarder
        this.server.receiveRequestFromAgent(this.number+1);
    }


    public int getNumber() {
        return this.number;
    }

    public boolean equals(Forwarder f) {
        return this.number == f.getNumber();
    }


    public void update(double time) {
        if (this.remainingTime == 0) {
            switch (this.state) {

                case ACTIF:
//                    this.state = DEAD;
//                    this.remainingTime = 50000000;
                    this.startCommunicationServer();
                    break;
                case UPDATING_SERVER:
//                    this.state = DEAD;
                    this.endCommunicationServer();
                    break;
            }
        }
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

}
