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
    public static int defaultState;
    public static int Current_Number = 0;
    //    protected double remainingTime;
    //   protected int number;
    protected Server server;
    protected Simulator simulator;

    static {
        if ("DEAD".equals(System.getProperties().getProperty("forwarder.state"))) {
            Forwarder.defaultState = DEAD;
        } else {
            Forwarder.defaultState = ACTIF;
        }
        System.out.println("--- Forwarders are " + Forwarder.defaultState);
    }

    public Forwarder() {
        this(5000000, Current_Number++);
    }

    public Forwarder(int number, Server s, Simulator s2, int id) {
        this(5000000, number);
        this.server = s;
        this.simulator = s2;
    }

    public Forwarder(double lifeTime, int n) {
        this.remainingTime = lifeTime;
        //      this.number = n;
        this.migrationCounter = n;
        //      this.state = ACTIF;
        this.state = Forwarder.defaultState;
    }

    public void endLife() {
	if ("INFINITE".equals(System.getProperty("forwarder.lifetime"))) {
	    this.state= ACTIF;
	    this.remainingTime= new Double(Double.MAX_VALUE).doubleValue();
	}else {
        this.state = DEAD;
	}
    }

    public void setLifeTime(double l) {
        this.remainingTime = l;
        //      this.simulator.log("Forwarder: setLifeTime " + l);
    }

    public static void setDefaultState(int i) {
        Forwarder.defaultState = i;
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
//              System.out.println("Forwarder.startCommunicationServer");
        this.state = UPDATING_SERVER;
        this.remainingTime = simulator.generateCommunicationTimeServer();
    }

    public void endCommunicationServer() {
        //        System.out.println("Forwarder.endCommunication");
        this.state = DEAD;
        this.remainingTime = 50000000;
        //we send the number of the next forwarder
        //      System.out.println(
        //         "Forwarder: calling server, next number is " + (this.number + 1));
        this.server.receiveRequestFromForwarder(this.migrationCounter + 1, 
                                                this.id);
    }

    //
    //   public int getNumber() {
    //      return this.number;
    //   }
    public boolean equals(Forwarder f) {
        return this.migrationCounter == f.getNumber();
    }

    public void update(double time) {
        if (this.remainingTime == 0) {
            switch (this.state) {
                case ACTIF:
                    //                    this.state = DEAD;
                    //                    this.remainingTime = 50000000;
                    if (!"NO".equals(System.getProperties().getProperty("forwarder.callserver"))) {
                         this.startCommunicationServer();
                    } else {
                    	     this.state = DEAD;
        this.remainingTime = 50000000;
                    }
                    
                    break;
                case UPDATING_SERVER:
                    //                    this.state = DEAD;
                    this.endCommunicationServer();
                    break;
                case DEAD:
                    this.setRemainingTime(50000000);
                    break;
            }
        }
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
