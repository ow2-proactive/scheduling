package modelisation.simulator.mixed.mixedwithcalendar;

import modelisation.simulator.common.Averagator;


public class AgentWithMaxTime extends Agent {

    protected double maxTime;
    protected double remainingTimeOnSite;
    protected Averagator averagatorMaxTimeReached;
    protected boolean serverCalledOnArrival;

    public AgentWithMaxTime() {
    }

    public AgentWithMaxTime(Simulator s, double nu, double delta, 
                            int maxMigrations, int maxTime, int id) {
        super(s, nu, delta, maxMigrations, id);
        this.maxTime = maxTime;
        this.averagatorMaxTimeReached = new Averagator();
    }

    public void endMigration(double endTime) {
        if (log) {
            this.simulator.log("Agent: Migration ended ");
            this.simulator.log(
                    "Agent: length of the migration " + 
                    (endTime - startTime));
        }
        if (((this.migrationCounter + 1) % maxMigrations) == 0) {
            this.serverCalledOnArrival = true;
            this.callServer(endTime);
        } else {
            this._endOfMigration(endTime);
        }
        //    this.averagatorDelta.add(endTime - startTime);
        //    this._endOfMigration(endTime);
        // this.endOfCallServer(endTime);
    }

    protected void _endOfMigration(double endTime) {
        this.migrationCounter++;
        this.state = WAITING;
        if (log) {
            this.simulator.log(
                    "AgentWithMaxTime_endOfMigration migrationCounter " + 
                    this.migrationCounter);
            this.simulator.log(
                    "AgentWithMaxTime_endOfMigration total time " + 
                    (endTime - startTime));
            //            this.simulator.log(
            //                    " AgentWithMaxTime: waited " +
            //                    this.remainingTime + " before migration");
        }
        this.forwarderChain.add(new Forwarder(this.migrationCounter - 1, 
                                              this.server, this.simulator, 
                                              this.id));
        //here we have the new policy
        //when we have the new waiting time, we compare it to the maxTime
        //if needed, we schedule an update of the server
        this.remainingTime = this.waitTime();
        //          System.out.println("Remaining Time = " + this.remainingTime);
        //          System.out.println("maxTime = " + maxTime);
        if (this.remainingTime > maxTime && !serverCalledOnArrival) {
            this.remainingTimeOnSite = this.remainingTime - maxTime;
            //            System.out.println(
            //                    "RemainingTimeOnSite " + this.remainingTimeOnSite);
            this.remainingTime = maxTime;
            //  this.notifyEvent("Call server");
            this.callServer(endTime);
        } else {
            //       this.state = WAITING;
            this.notifyEvent("Wait");
            this.averagatorNu.add(this.remainingTime);
        }
    }

    public void end() {
        System.out.println("########### AgentWithMaxTime ###### ");
        System.out.println(
                "* Max time reached, remaining = " + 
                this.averagatorMaxTimeReached.getTotal() / this.averagatorMaxTimeReached.getCount() + 
                " " + this.averagatorMaxTimeReached.getCount());
        super.end();
    }

    public void endOfCallServer(double time) {
        if (log) {
            this.simulator.log("AgentWithMaxTime.endOfCallServer");
        }
        if (this.serverCalledOnArrival) {
            this._endOfMigration(time);
            this.server.receiveRequestFromAgent(migrationCounter + 1, id);
        } else {
        	 this.server.receiveRequestFromAgent(migrationCounter, id);
            if (this.remainingTimeOnSite > this.remainingTime) {
                this.averagatorNu.add(this.maxTime + 
                                      this.remainingTimeOnSite);
                this.remainingTime = this.remainingTimeOnSite - 
                                     this.remainingTime;
            } else {
                //System.out.println("XXXXX");
                this.averagatorNu.add(this.maxTime + this.remainingTime);
                this.remainingTime = 0;
            }
            this.averagatorMaxTimeReached.add(this.remainingTime);
            this.state = WAITING;
            this.notifyEvent("Wait");
        }
    }

    public void startMigration(double currentTime) {
        this.serverCalledOnArrival = false;
        super.startMigration(currentTime);
    }
}