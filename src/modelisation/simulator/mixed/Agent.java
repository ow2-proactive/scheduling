package modelisation.simulator.mixed;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;
import modelisation.statistics.RandomNumberGenerator;
import org.apache.log4j.Logger;


public class Agent extends SimulatorElement {

    protected static Logger logger = Logger.getLogger(Agent.class.getName());
    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int MIGRATED = 2;
    public final static int CALLING_SERVER = 3;
    public final static int REFUSED = 6;
    public final static int BLOCKED = 7;
    public static final boolean NO = false;
    public static final boolean YES = true;
    protected static boolean callServer;

    static {
        Agent.callServer = !"NO".equals(System.getProperties().getProperty(
                                                "agent.performcallserver"));
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "--- Agent will perform call to server " + 
                    Agent.callServer);
        }
    }

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
    protected double delta;
    protected double nu;
    private RandomNumberGenerator expoDelta;
    private RandomNumberGenerator expoNu;

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
        this.maxMigrations = mm;
        this.id = ID;
        this.delta = delta;
        this.nu = nu;
        if (log) {
            this.simulator.log(
                    " AgentWithExponentialMigrationAndServer: waited " + 
                    this.remainingTime + " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
        this.remainingTime = this.waitTime();
    }

    public void setForwarderChain(ForwarderChain fc) {
        this.forwarderChain = fc;
    }

    public void setServer(Server s) {
        this.server = s;
    }

    public int receiveMessage() {
        if (logger.isDebugEnabled()) {
            //        logger.debug("Agent.receiveMessage " + this);
        }
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

    public double generateMigrationTime() {
        //        if (this.expoDelta == null) {
        if (logger.isDebugEnabled()) {
            //            logger.debug(
        }
        //                    "Agent " + this.id + " getting random generator");
        //            this.expoDelta = RandomNumberFactory.getGenerator("delta");
        //            this.expoDelta.initialize(delta,
        //                                      System.currentTimeMillis() +
        //                                      395672917);
        if (logger.isDebugEnabled()) {
            //            logger.debug("Agent " + this.id +
        }
        //                               " got  random generator");
        //            //            this.expoDelta.initialize(delta, 58373435);
        //        }
        //        //        double tmp = expoDelta.next() * 1000;
        if (logger.isDebugEnabled()) {
            //        //        logger.debug("Agent: migration started, will last " + tmp);
        }
        //        //        return tmp;
        //        return this.expoDelta.next() * 1000;
        return this.simulator.generateMigrationTime();
    }

    public double generateAgentWaitingTime() {
        //        if (this.expoNu == null) {
        if (logger.isDebugEnabled()) {
            //                      logger.debug(
        }
        //                    "Agent " + this.id + " getting random generator for waitTime with parameter " + nu);
        //            this.expoNu = RandomNumberFactory.getGenerator("nu");
        //            //            this.expoNu.initialize(nu, System.currentTimeMillis() + 39566417);
        //            this.expoNu.initialize(nu, 39566417);
        //        }
        if (logger.isDebugEnabled()) {
            //    //  logger.debug(expoNu.next() * 1000);
        }
        //        return expoNu.next() * 1000;
        return this.simulator.generateAgentWaitingTime();
    }

    public double migrationTime() {
        return this.generateMigrationTime();
    }

    public double waitTime() {
        return this.generateAgentWaitingTime();
    }

    public double generateCommunicationTimeServer() {
        return this.simulator.generateCommunicationTimeServer();
    }

    public void startMigration(double currentTime) {
        this.startTime = currentTime;
        this.state = MIGRATING;
        this.remainingTime = this.migrationTime();
        if (log) {
            this.simulator.log("Agent: Migration will last " + currentTime);
        }
    }

    public void endMigration(double endTime) {
        if (log) {
            this.simulator.log("Agent: Migration ended");
            this.simulator.log(
                    "Agent: length of the migration " + 
                    (endTime - startTime));
            this.simulator.log(
                    "Agent: will call server " + 
                    (((this.migrationCounter + 1) % maxMigrations) == 0));
        }
        this.averagatorDelta.add(endTime - startTime);
        if (((this.migrationCounter + 1) % maxMigrations) == 0) {
            this.callServer(endTime);
        } else {
            this._endOfMigration(endTime);
        }
        // this.endOfCallServer(endTime);
    }

    protected void _endOfMigration(double endTime) {
        this.migrationCounter++;
        this.state = WAITING;
        this.forwarderChain.add(new Forwarder(this.migrationCounter - 1, 
                                              this.server, this.simulator, 
                                              this.id));
        this.remainingTime = this.waitTime();
        if (log) {
            this.simulator.log(
                    "Agent.endOfCallServer migrationCounter " + 
                    this.migrationCounter);
            this.simulator.log(
                    "Agent.endOfCallServer total time " + 
                    (endTime - startTime));
            this.simulator.log(
                    " AgentWithExponentialMigrationAndServer: waited " + 
                    this.remainingTime + " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
    }

    public void callServer(double time) {
        if (Agent.callServer) {
            if (log) {
                this.simulator.log("Agent.callServer");
            }
            this.state = CALLING_SERVER;

            double tmp = this.generateCommunicationTimeServer();
            this.averagatorGamma2.add(tmp);
            this.remainingTime = tmp;
        } else {
            endOfCallServer(time);
        }
    }

    public void endOfCallServer(double time) {
        this.server.receiveRequestFromAgent(migrationCounter + 1, id);
        this._endOfMigration(time);
    }

    /**
     * Called by the ForwarderChain when a tensioning is initiated
     */
    public void startTensioning(double time) {
        //the agent can only be waiting when a tensioning is initiated
        if (this.remainingTime < time) {
            if (log) {
                this.simulator.log(
                        "Simulator: agent wait end of tensioning " + 
                        (time - this.remainingTime));
            }
            //this is because of a nasty assumption in the model
            //it should be      this.remainingTime = time;
            this.remainingTime = time + this.waitTime();
        }
    }

    public int getNumber() {
        return this.migrationCounter;
    }

    public Averagator getAveragatorDelta() {
        return this.averagatorDelta;
    }

    public void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("* nu = " + 1000 / this.averagatorNu.average());
            logger.debug("* delta  = " + 
                         1000 / this.averagatorDelta.average());
            logger.debug("delta count " + this.averagatorDelta.getCount());
            logger.debug(
                    "gamma2 server = " + 
                    1000 / this.averagatorGamma2.average());
            logger.debug(" gamma2 count " + this.averagatorGamma2.getCount());
            logger.debug(
                    "* Real delta = " + 
                    1000 / ((this.averagatorDelta.getTotal() + this.averagatorGamma2.getTotal()) / this.averagatorDelta.getCount()));
        }
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