package modelisation.simulator.mixed;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;

import org.objectweb.proactive.core.UniqueID;


public class Agent extends SimulatorElement {
    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int MIGRATED = 2;
    public final static int CALLING_SERVER = 3;
    public final static int REFUSED = 6;
    public final static int BLOCKED = 7;
    //    protected UniqueID id;
    protected double startTime;
    protected ForwarderChain forwarderChain;
    protected Simulator simulator;
    protected Server server;
    protected int migrationCounter;
    protected int maxMigrations;
    protected Averagator averagatorDelta;
    protected Averagator averagatorNu;
    protected Averagator averagatorGamma2;

    //    protected int id;
    public Agent() {
    }

    public Agent(Simulator s, double nu, double delta, int mm) {
        this(s, nu, delta, mm, -1);
    }

    public Agent(Simulator s, double nu, double delta, int mm, int ID) {
        this.averagatorDelta = new Averagator();
        this.averagatorNu = new Averagator();
        this.averagatorGamma2 = new Averagator();
        this.state = WAITING;
        this.simulator = s;
        this.state = WAITING;
        this.remainingTime = this.waitTime();
        this.maxMigrations = mm;
        this.id = ID;
        if (log) { this.simulator.log(" AgentWithExponentialMigrationAndServer: waited " + 
                 this.remainingTime + " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
    }

    public void setForwarderChain(ForwarderChain fc) {
        this.forwarderChain = fc;
    }

    public void setServer(Server s) {
        this.server = s;
    }

    public int receiveMessage() {
        //        System.out.println("Agent.receiveMessage " + this);
        if (this.state == WAITING) {
            return Agent.WAITING;
        } else
            return Agent.BLOCKED;
    }

    public void update(double time) {
        if (this.remainingTime == 0) {
            switch (this.state) {
                case WAITING:
                    this.startMigration(time);
                    break;
                case MIGRATING:
                    this.endMigration(time);
                    break;
                case CALLING_SERVER:
                    this.endOfCallServer(time);
                    break;
            }
        }
    }

    public double migrationTime() {
        return simulator.generateMigrationTime();
    }

    public double waitTime() {
        return simulator.generateAgentWaitingTime();
    }

    public void startMigration(double currentTime) {
        this.startTime = currentTime;
        this.state = MIGRATING;
        this.remainingTime = this.migrationTime();
    }

    public void endMigration(double endTime) {
   
        if (log) { this.simulator.log("Agent: Migration Time " + (endTime - startTime));}
        if (log) { this.simulator.log("Agent: length of the migration " + (endTime - startTime));}
        this.averagatorDelta.add(endTime - startTime);
    
        if ((this.migrationCounter++ % maxMigrations) == 0) {
            this.callServer(endTime);
        } else {
            this._endOfMigration(endTime);
        }
        // this.endOfCallServer(endTime);
    }

    protected void _endOfMigration(double endTime) {
        this.state = WAITING;
        this.forwarderChain.add(new Forwarder(this.migrationCounter - 1, 
                                              this.server, this.simulator, 
                                              this.id));
        this.remainingTime = this.waitTime();
        if (log) { this.simulator.log("Agent.endOfCallServer migrationCounter " + 
                 this.migrationCounter);}
        if (log) { this.simulator.log("Agent.endOfCallServer total time " + 
                 (endTime - startTime));}
        if (log) { this.simulator.log(" AgentWithExponentialMigrationAndServer: waited " + 
                 this.remainingTime + " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
    }

    public void callServer(double time) {
               if (log) { this.simulator.log("Agent.callServer");}
        this.state = CALLING_SERVER;
        double tmp = simulator.generateCommunicationTimeServer();
        this.averagatorGamma2.add(tmp);
        this.remainingTime = tmp;
    }

    public void endOfCallServer(double time) {
        this.server.receiveRequestFromAgent(migrationCounter, id);
        this._endOfMigration(time);
    }

    /**
     * Called by the ForwarderChain when a tensioning is initiated
     */
    public void startTensioning(double time) {
        //the agent can only be waiting when a tensioning is initiated
        if (this.remainingTime < time) {
            if (log) { this.simulator.log("Simulator: agent wait end of tensioning " + 
                     (time - this.remainingTime));
            }
            this.remainingTime = time;
        }
    }

    public int getNumber() {
        return this.migrationCounter;
    }

    public Averagator getAveragatorDelta() {
        return this.averagatorDelta;
    }

    public void end() {
        System.out.println("* nu = " + 1000 / this.averagatorNu.average());
        System.out.println(
                "* delta  = " + 1000 / this.averagatorDelta.average());
                System.out.println(
                "delta count " + this.averagatorDelta.getCount());
        System.out.println(
                "gamma2 server = " + 1000 / this.averagatorGamma2.average());
                System.out.println(" gamma2 count " + this.averagatorGamma2.getCount());
                
        System.out.println(
                "* Real delta = " + 
                1000 / ((this.averagatorDelta.getTotal() + this.averagatorGamma2.getTotal()) / this.averagatorDelta.getCount()));
    }

    public String getStateAsLetter() {
        switch (this.state) {
            case WAITING:
                return "w";
            case MIGRATING:
                return "m";
            case CALLING_SERVER:
                return "u";
        }
        return "";
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        switch (this.state) {
            case WAITING:
                tmp.append("WAITING");
                break;
            case MIGRATING:
                tmp.append("MIGRATING");
                break;
            case MIGRATED:
                tmp.append("MIGRATED");
                break;
            case CALLING_SERVER:
                tmp.append("CALLING_SERVER");
                break;
        }
        tmp.append(" remainingTime = ").append(remainingTime);
        return tmp.toString();
    }
}