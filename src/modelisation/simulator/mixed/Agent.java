package modelisation.simulator.mixed;

import modelisation.simulator.common.SimulatorElement;

public class Agent extends SimulatorElement {

    public final static int WAITING = 0;
    public final static int MIGRATING = 1;
    public final static int MIGRATED = 2;
    public final static int CALLING_SERVER = 3;
    public final static int REFUSED = 6;
    public final static int BLOCKED = 7;


    protected double startTime;
    protected ForwarderChain forwarderChain;
    protected Simulator simulator;
    protected Server server;
    protected int migrationCounter;
    protected int maxMigrations;

    public Agent() {
    }

    public Agent(Simulator s, double nu, double delta, int  mm) {
        this.state = WAITING;
        this.simulator = s;
        this.state = WAITING;
        this.remainingTime = this.waitTime();
        this.maxMigrations = mm;
        System.out.println(" AgentWithExponentialMigrationAndServer: waited " +
                           this.remainingTime + " before migration");
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
        return simulator.getMigrationTime();
    }

    public double waitTime() {
        return simulator.getAgentWaitingTime();
    }

    public void startMigration(double currentTime) {
        this.startTime = currentTime;
        this.state = MIGRATING;
        this.remainingTime = this.migrationTime();
    }

    public void endMigration(double endTime) {
//        this.forwarderChain.add(new Forwarder(this.migrationCounter));
//        this.migrationCounter++;
        System.out.println("Agent: Migration Time " + (endTime - startTime));
//        System.out.println("migrationCounter " + ((this.migrationCounter+1) % maxMigrations));
        if ((this.migrationCounter++ % maxMigrations) == 0) {
          this.callServer(endTime);
        } else {
            this._endOfMigration(endTime);
        }
        // this.endOfCallServer(endTime);
    }

    protected void _endOfMigration(double endTime) {
         this.state = WAITING;
        this.forwarderChain.add(new Forwarder(this.migrationCounter-1,
                                              this.server, this.simulator));
//        this.migrationCounter++;
//        this.migrationCounter++;
        this.remainingTime = this.waitTime();
       System.out.println("Agent.endOfCallServer total time " +
                           (endTime - startTime));
        System.out.println(" AgentWithExponentialMigrationAndServer: waited " +
                           this.remainingTime + " before migration");
   
    }


    public void callServer(double time) {
        System.out.println("Agent.callServer");
        this.state = CALLING_SERVER;
        this.remainingTime = simulator.getCommunicationTimeServer();
    }

    public void endOfCallServer(double time) {
        this.server.receiveRequestFromAgent(migrationCounter);
        this._endOfMigration(time);
    }

    /**
     * Called by the ForwarderChain when a tensioning is initiated
     */
    public void startTensioning(double time) {
    //the agent can only be waiting when a tensioning is initiated
        if (this.remainingTime < time) {
              System.out.println("Simulator: agent wait end of tensioning "
                                   + (time-this.remainingTime));
            this.remainingTime=time;
        }
    }


    public int getNumber() {
        return this.migrationCounter;
    }


	public String getStateAsLetter() {
		switch (this.state) {
			case WAITING :
				return "w";
			case MIGRATING :
				return "m";
			case CALLING_SERVER :
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
