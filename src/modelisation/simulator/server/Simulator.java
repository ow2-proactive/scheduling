package modelisation.simulator.server;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;

import java.util.ArrayList;


public class Simulator {

    protected Source source;
    protected Agent agent;
    protected Server server;

    protected double currentTime;
    protected double eventTime;
    protected double eventLength;
    protected double length;

    protected double gamma1;
    protected double gamma2;

    protected boolean agentHasMigrated;

    protected String state;

    protected RandomNumberGenerator expoGamma1;
    protected RandomNumberGenerator expoGamma2;

    protected ArrayList t1List;
    protected double t1;

    public Simulator() {
    };

    public Simulator(double lambda, double nu, double delta, double gamma1,
                     double gamma2, double mu1, double mu2, double length) {
        System.out.println("Creating source");
        this.source = new Source(lambda);
        System.out.println("Creating agent");
        this.agent = new Agent(nu, delta);
        this.length = length;
        this.gamma1 = gamma1;
        this.gamma2 = gamma2;
        this.t1List = new ArrayList();
        this.server = new Server(mu1, mu2);
    }


    public void initialise() {
        System.out.println("Bench, length is " + length);
        this.agent.waitBeforeMigration();
        this.source.waitBeforeCommunication();
    }

    public double getNextGamma1Int() {
        if (this.expoGamma1 == null) {
            this.expoGamma1 = RandomNumberFactory.getGenerator("gamma1");
//            this.expoGamma1.initialize(gamma1, System.currentTimeMillis() + 98672);
            this.expoGamma1.initialize(gamma1, 372917);
        }

        return this.expoGamma1.next() * 1000;
//        return 1000/this.gamma1;
    }

    public double getNextGamma2Int() {
        if (this.expoGamma2 == null) {
            this.expoGamma2 = RandomNumberFactory.getGenerator("gamma2");
//            this.expoGamma2.initialize(gamma2, System.currentTimeMillis() + 276371);
            this.expoGamma2.initialize(gamma2, 276371);
        }

//        return this.expoGamma2.next() * 1000;
            double tmp = this.expoGamma2.next()*1000;
        System.out.println("expoGamma2 = " + tmp);
        return tmp;

//        return 1000/gamma2;
    }


    public void simulate() {
        double lengthOfState = 0;
        while (this.currentTime < length) {
            lengthOfState = this.updateTime();
            System.out.println("length of state = " + lengthOfState);
//            System.out.println(" -------------- Time " + this.currentTime + " ----------------------");
//            System.out.println("*** states before update ***");
//            System.out.println("STATE: " + this.server + "" + this.source
//                               + "" + this.agent + " lasted " + lengthOfState);
//            this.displayState();
            if (server.getRemainingTime() == 0) {
                this.serverBehaviour();
                this.source.removeStar();
                //   this.displayState();
            }

            if (agent.getRemainingTime() == 0) {
                this.agentBehaviour();
                this.source.removeStar();
                //    this.displayState();
            }
            if (source.getRemainingTime() == 0) {
                this.sourceBehaviour();
                //     this.displayState();
            }
//            System.out.println("*** states after update ***");
//            this.displayState();
//            System.out.println("--------------------------------------------------------------------");

        }
                System.out.println("Simulator.simulate currentTime " + currentTime);
        // System.out.println("T1 is " + t1);
    }


    public void serverBehaviour() {
        boolean serviceOk = true;
        if (this.server.getState() == server.IDL_EMPTY) {
            this.server.setRemainingTime(50000000);
            return;
        }

        if (this.server.getState() == server.IDL_REQUEST) {
            this.server.serveNextRequest(this.currentTime);
            return;
        }
        if (this.server.getState() == server.SERVING_AGENT) {
            this.server.endOfService(this.currentTime, getNextGamma2Int());
            //we need to check if a reply is needed
//            if (this.server.getState() == server.REPLY_NEEDED) {
//                this.server.sendReply(getNextGamma1Int());
//            }
            return;
        }

        if (this.server.getState() == server.SERVING_SOURCE) {
            this.server.endOfService(this.currentTime, getNextGamma2Int());
            //   this.server.endOfSendReply(this.currentTime);
//            if (serviceOk) {
//
//                //  this.source.state = source.COMMUNICATION;
//                //  this.source.setRemainingTime(getNextGamma1Int());
//
//            }
            return;
        }

        if (this.server.getState() == server.SENDING_REPLY) {
            if (!this.server.hasRequestFromAgent()) {
                this.agent.foundYou();
            }
            this.server.endOfSendReply(this.currentTime);

            this.source.continueCommunication(getNextGamma1Int());
            return;
        }
    }

    public void agentBehaviour() {
        if ((this.agent.getState() == agent.WAITING) ||
                (this.agent.getState() == agent.MIGRATED)) {
            agent.startMigration(this.currentTime);
            return;
        }
        if (this.agent.getState() == agent.MIGRATING) {
            agent.endMigration(this.currentTime);
            server.receiveRequestFromAgent();
//            agent.callServer(this.getNextGamma2Int());
            return;
            // agent.callServer(this.getNextGamma2Int());
            //          this.agentHasMigrated = true;
            //agent.waitBeforeMigration();
            //   return;
        }
//        if (this.agent.getState() == agent.CALLING_SERVER) {
//            agent.endMigration(this.currentTime);
//            server.receiveRequestFromAgent();
//            return;
//        }
    }

    public void sourceBehaviour() {
        if (this.source.getState() == source.WAITING) {
            source.startCommunication(this.currentTime, getNextGamma1Int());
            return;
        }
        if (this.source.getState() == source.COMMUNICATION) {
            if (agent.migrated) {
//                System.out.println("XXXXX Source found agent migrated");
                source.startCommunicationServer(getNextGamma2Int());
                return;
            }
            if (agent.getState() == agent.WAITING) {
                source.endCommunication(this.currentTime);
                source.waitBeforeCommunication();
                return;
            }
            if (agent.getState() == agent.MIGRATING) {
//                System.out.println("XXXXX Source found agent migrating");
                //System.out.println("agent.getRemainingTime() " + agent.getRemainingTime());
                //System.out.println("source.getRemainingTime() " + source.getRemainingTime());
                source.waitForAgent(Math.max(agent.getRemainingTime(), source.getRemainingTime()));
            }
            return;
        }
        if (this.source.getState() == source.WAITING_FOR_AGENT) {
            //  this.source.continueCommunication(getNextGamma1Int());
            if (agent.migrated) {
                source.startCommunicationServer(getNextGamma2Int());
                //  source.waitForError(getNextGamma1Int());
            } else {
                this.source.setRemainingTime(agent.getRemainingTime());
            }
            return;
        }
        if (this.source.getState() == source.WAITING_ERROR_MESSAGE) {
            source.startCommunicationServer(getNextGamma2Int());
            return;
        }

        if (this.source.getState() == source.CALLING_SERVER) {
            source.waitForServer();
            this.server.receiveRequestFromSource();
            return;
        }
        if (this.source.getState() == source.WAITING_FOR_SERVER) {
            return;
        }

    }


    public void displayState() {
        // int stateNumber = 0;
        // StringBuffer tmp = new StringBuffer();
        //first we check the state of the source
        //  System.out.println("---- State at time " + this.currentTime);
        System.out.println("STATE: " + this.server + "" + this.source + "" + this.agent);
        //    System.out.println(this.source);
        //      System.out.println(this.agent);

        // System.out.println("---------------------");


//        this.state = "P" + stateNumber;
//        System.out.println(" === state was " + state + "  lasted " + (currentTime - eventTime));
//        System.out.println(" === " + tmp);
//        this.eventLength = currentTime - eventTime;
//        this.eventTime = currentTime;
    }

    public void calculateT1(double eventLength) {
        if (this.state.equals("P2")) {
            System.out.println("calculateT1: adding eventLength " + eventLength);
            this.t1List.add(new Double(eventLength));
            return;
        }
        if (this.state.equals("P1")) {
            System.out.println("calculateT1: P1 reached after length " + eventLength);
            //time to update the value of t1
            Object[] timeArray = t1List.toArray();
            double tmp = 0;
            for (int i = 0; i < timeArray.length; i++) {
                tmp += ((Double) timeArray[i]).doubleValue() + eventLength;
            }
            System.out.println("   calculateT1: total of tmp " + tmp + " number of values is " + timeArray.length);
            System.out.println("   calculateT1: t1 was " + t1);
            if (timeArray.length > 0)
                this.t1 = (this.t1 + (tmp / timeArray.length)) / 2;
            this.t1List = new ArrayList();
            return;
        }

        Object[] timeArray = t1List.toArray();
        System.out.println("calculateT1: updating list with value " + eventLength + " for " + timeArray.length + " elements ");
        for (int i = 0; i < timeArray.length; i++) {
            System.out.println("   calculateT1: value was " + timeArray[i]);

            timeArray[i] = new Double(((Double) timeArray[i]).doubleValue() + eventLength);
            System.out.println("   calculateT1: now " + timeArray[i]);
        }

    }


    public double updateTime() {
        double minTime = server.getRemainingTime();
        minTime = Math.min(minTime, agent.getRemainingTime());
        minTime = Math.min(minTime, source.getRemainingTime());
//        System.out.println("   Simulator: remaining time for source " + source.getRemainingTime());
//        System.out.println("   Simulator: remaining time for agent " + agent.getRemainingTime());
//        System.out.println("   Simulator: remaining time for server " + server.getRemainingTime());
//        System.out.println("   Simulator: next event at time " + minTime);
        this.source.decreaseRemainingTime(minTime);
        this.agent.decreaseRemainingTime(minTime);
        this.server.decreaseRemainingTime(minTime);
        this.currentTime += minTime;
        return minTime;
    }


    public static void main(String args[]) {
        if (args.length < 8) {
            System.err.println("Usage: java " + Simulator.class.getName()
                               + " <lambda> <nu> <delta> <gamma1> <gamma2> "
                               + " <mu1> <mu2>  <length>");
            System.exit(-1);
        }
        System.out.println("Starting Simulator");
        System.out.println("     lambda = " + args[0]);
        System.out.println("         nu = " + args[1]);
        System.out.println("      delta = " + args[2]);
        System.out.println("      gamma1 = " + args[3]);
        System.out.println("      gamma2 = " + args[4]);
        System.out.println("      mu1 = " + args[5]);
        System.out.println("      mu2 = " + args[6]);
        System.out.println("     length = " + args[7]);

        Simulator simulator = new Simulator(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                                            Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                                            Double.parseDouble(args[4]), Double.parseDouble(args[5]),
                                            Double.parseDouble(args[6]), Double.parseDouble(args[7]));
        simulator.initialise();
        simulator.simulate();
    }

}
