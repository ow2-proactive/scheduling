package modelisation.simulator.mixed.mixedwithcalendar;

import org.apache.log4j.Logger;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;


public class Agent extends SimulatorElement {
    static Logger logger = Logger.getLogger(Agent.class.getName());
    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int MIGRATED = 2;
    public final static int CALLING_SERVER = 3;
    public final static int REFUSED = 6;
    public final static int BLOCKED = 7;
    public static final boolean NO = false;
    public static final boolean YES = true;

    static {
        Agent.callServer = !"NO".equals(System.getProperties().getProperty("agent.performcallserver"));
        if (logger.isInfoEnabled()) {
            logger.info("--- Agent will perform call to server " +
                Agent.callServer);
        }
    }

    protected static boolean callServer;
    protected Event currentEvent;
    protected TtlGenerator ttlGenerator;

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
    protected double initialTtl;
    protected double currentTtl;

    //    private RandomNumberGenerator expoDelta;
    //   private RandomNumberGenerator expoNu;
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
        if (logger.isDebugEnabled()) {
            logger.debug(" AgentWithExponentialMigrationAndServer: waited " +
                this.remainingTime + " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
        this.remainingTime = this.waitTime();
        this.notifyEvent("Wait");
    }

    public Agent(Simulator simulator, double nu, double delta, double ttl,
        int maxMigration, int i) {
        this(simulator, nu, delta, maxMigration, i);
        //System.out.println("XXXX");
        this.initialTtl = ttl;
        this.currentTtl = ttl;
        this.ttlGenerator = new TtlGenerator(this.initialTtl);
    }

    public void notifyEvent(String description) {
        if (this.currentEvent != null) {
            this.simulator.removeEvent(this.currentEvent);
        }
        this.timeNextEvent = this.remainingTime +
            this.simulator.getCurrentTime();
        this.currentEvent = new Event(this.timeNextEvent, this, description);
        this.simulator.addEvent(currentEvent);
    }

    public void setForwarderChain(ForwarderChain fc) {
        this.forwarderChain = fc;
    }

    public void setServer(Server s) {
        this.server = s;
    }

    public int receiveMessage() {
        if (logger.isDebugEnabled()) {
            logger.debug("Agent.receiveMessage " + this);
        }
        if (this.state == WAITING) {
            return Agent.WAITING;
        } else {
            return Agent.BLOCKED;
        }
    }

    public void update(double time) {
        this.currentEvent = null;
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

        //  }
    }

    public double generateMigrationTime() {
        return this.simulator.generateMigrationTime();
    }

    public double generateAgentWaitingTime() {
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
        this.notifyEvent("Migration");
        if (logger.isDebugEnabled()) {
            Agent.logger.debug("Agent: Migration started ");
        }
    }

    public void endMigration(double endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Agent: Migration ended ");
            logger.debug("Agent: length of the migration " +
                (endTime - startTime));
            logger.debug("Agent: will call server " +
                (((this.migrationCounter + 1) % maxMigrations) == 0));
        }
        this.averagatorDelta.add(endTime - startTime);
        if (((this.migrationCounter + 1) % maxMigrations) == 0) {
            this.callServer(endTime);
        } else {
            this._endOfMigration(endTime);
        }
    }

    protected void _endOfMigration(double endTime) {
        this.migrationCounter++;
        this.state = WAITING;
        //     this.forwarderChain.add(new Forwarder(this.migrationCounter - 1,
        //           this.server, this.simulator, this.id), this.simulator.generateForwarderLifeTime());
        this.forwarderChain.add(new Forwarder(this.migrationCounter - 1,
                this.server, this.simulator, this.id), this.currentTtl);
        this.remainingTime = this.waitTime();
        this.notifyEvent("Wait");
        if (logger.isDebugEnabled()) {
            logger.debug("Agent._endOfMigration migrationCounter " +
                this.migrationCounter);
            logger.debug("Agent._endOfMigration total time " +
                (endTime - startTime));
            logger.debug("Agent: waited " + this.remainingTime +
                " before migration");
        }
        this.averagatorNu.add(this.remainingTime);
    }

    public void callServer(double time) {
        if (Agent.callServer) {
            if (logger.isDebugEnabled()) {
                logger.debug("Agent.callServer");
            }
            this.state = CALLING_SERVER;

            double tmp = this.generateCommunicationTimeServer();
            this.averagatorGamma2.add(tmp);
            this.remainingTime = tmp;
            this.notifyEvent("Call server");
        } else {
            endOfCallServer(time);
        }
    }

    public void endOfCallServer(double time) {
        if (logger.isDebugEnabled()) {
            logger.debug("Agent.endOfCallServer");
        }
        this.server.receiveRequestFromAgent(migrationCounter + 1, id);
        this._endOfMigration(time);
    }

    /**
     * Called by the ForwarderChain when a tensioning is initiated
     */
    public void startTensioning(double length) {
        //the agent can only be waiting when a tensioning is initiated
        if (logger.isDebugEnabled()) {
            logger.debug("Tensioning requested length " + length +
                " next event " + this.currentEvent.getTime());
        }
        if (this.currentEvent.getTime() < (length +
                this.simulator.getCurrentTime())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Simulator: agent wait end of tensioning " +
                    (length - this.remainingTime));
            }

            //this is because of a nasty assumption in the model
            //it should be      this.remainingTime = time;
            this.remainingTime = length + this.waitTime();
            this.notifyEvent("Waiting because of tensioning");
        }
    }

    public int getNumber() {
        return this.migrationCounter;
    }

    public Averagator getAveragatorDelta() {
        return this.averagatorDelta;
    }

    public void end() {
        if (logger.isInfoEnabled()) {
            logger.info("########## Agent ##################");
            logger.info("* nu = " + (1000 / this.averagatorNu.average()));
            logger.info("* delta  = " +
                (1000 / this.averagatorDelta.average()) + " " +
                this.averagatorDelta.getCount());
            logger.info("gamma2 server = " +
                (1000 / this.averagatorGamma2.average()));
            logger.info(" gamma2 count " + this.averagatorGamma2.getCount());
            logger.info("* Real delta = " +
                (1000 / ((this.averagatorDelta.getTotal() +
                this.averagatorGamma2.getTotal()) / this.averagatorDelta.getCount())));
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

    public String getStateAsString() {
        if (this.getState() == Agent.WAITING) {
            return "0";
        } else {
            return "1";
        }
    }

    public String getName() {
        return "Agent" + this.id;
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer("Agent: ");
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

    public double getRemainingTime() {
        if (this.currentEvent != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("current event is " +
                    this.currentEvent.toString());
            }
            return this.currentEvent.getTime() -
            this.simulator.getCurrentTime();
        } else {
            return Double.MAX_VALUE;
        }
    }

    public void previousCommunicationTime(double d, int tries) {
        //    	if (this.ttlGenerator.newCommunicationTime(d)) { 	
        //    		this.currentTtl = this.ttlGenerator.getNewTtl();
        //			System.out.println("Getting new value for ttl = " + this.currentTtl);
        //		
        //    	}
        //System.out.println("Agent: previous communication time " + d);
    }
}
