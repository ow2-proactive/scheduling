package modelisation.simulator.forwarder;

import java.util.ArrayList;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Simulator {
    protected Source source;
    protected Agent agent;
    protected double currentTime;
    protected double eventTime;
    protected double eventLength;
    protected double length;
    protected double gamma;
    protected int numberOfHops;
    protected boolean forwarded;
    protected String state;
    protected RandomNumberGenerator rvGamma;
    protected ArrayList t1List;
    protected double t1;

    public Simulator() {
    }
    ;
    public Simulator(double lambda, double nu, double delta, double gamma,
        double length) {
        System.out.println("Creating source");
        this.source = new Source(lambda);
        System.out.println("Creating agent");
        this.agent = new Agent(nu, delta);
        this.length = length;
        this.gamma = gamma;
        this.t1List = new ArrayList();
    }

    public void initialise() {
        System.out.println("Bench, length is " + length);
        this.agent.waitBeforeMigration();
        this.source.waitBeforeCommunication();
        this.numberOfHops = 1;
    }

    public double getNextGammaInt() {
        //        return this.rvGamma.rand() * 1000;
        if (this.rvGamma == null) {
            this.rvGamma = RandomNumberFactory.getGenerator("gamma");
            this.rvGamma.initialize(gamma, System.currentTimeMillis());
        }
        double tmp = this.rvGamma.next() * 1000;
        System.out.println("Gamma1 = " + tmp);
        return tmp;
        //        return 1000/gamma;
    }

    public void simulate() {
        while (this.currentTime < length) {
            this.updateTime();
            //            System.out.println(" -------------- Time " + this.currentTime + " ----------------------");
            //            this.displayState();
            // this.calculateT1(eventLength);
            if (agent.getRemainingTime() == 0) {
                this.agentBehaviour();
            }
            if (source.getRemainingTime() == 0) {
                this.sourceBehaviour();
            }
        }

        //     System.out.println("T1 is " + t1);
    }

    public void agentBehaviour() {
        if (this.agent.getState() == Agent.WAITING) {
            if (this.source.getState() == Source.TENSIONING) {
                System.out.println("Simulator: agent wait end of tensioning " +
                    source.getRemainingTime());
                this.agent.waitEndOfTensioning(source.getRemainingTime());
            } else {
                agent.startMigration();
            }
            return;
        }
        if (this.agent.getState() == Agent.MIGRATING) {
            agent.endMigration();
            this.numberOfHops++;
            this.forwarded = true;
            agent.waitBeforeMigration();
            return;
        }
        if (this.agent.getState() == Agent.WAITING_FOR_TENSIONING) {
            //          agent.startMigration();
            agent.waitBeforeMigration();
            return;
        }
    }

    public void sourceBehaviour() {
        if (this.source.getState() == Source.WAITING) {
            source.startCommunication(this.currentTime, getNextGammaInt());
            return;
        }
        if (this.source.getState() == Source.COMMUNICATION) {
            this.numberOfHops--;
            if (this.numberOfHops == 0) {
                if (agent.getState() == Agent.WAITING) {
                    if (this.forwarded) {
                        source.tensioning(getNextGammaInt());
                    } else {
                        source.endCommunication(this.currentTime);
                        this.numberOfHops = 1;
                        this.forwarded = false;
                        source.waitBeforeCommunication();
                        return;
                    }
                } else {
                    // source.waitForAgent(Math.max(agent.getRemainingTime(), source.getRemainingTime()));
                    source.waitForAgent(agent.getRemainingTime());
                }
            } else {
                //                this.forwarded = true;
                source.continueCommunication(getNextGammaInt());
            }
            return;
        }
        if (this.source.getState() == Source.TENSIONING) {
            source.endCommunication(this.currentTime);
            this.forwarded = false;
            this.numberOfHops = 1;
            source.waitBeforeCommunication();
            return;
        }

        if (this.source.getState() == Source.WAITING_FOR_AGENT) {
            this.source.continueCommunication(getNextGammaInt());
            return;
        }
    }

    public void displayState() {
        int stateNumber = 0;
        StringBuffer tmp = new StringBuffer();
        tmp.append(this.source + "," + this.agent);
        switch (this.source.getState()) {
        case Source.WAITING: {
            if (this.agent.getState() == Agent.WAITING) {
                //we have to deal with the P2 state by hand
                if (this.numberOfHops == 1) {
                    stateNumber = 2;
                } else {
                    stateNumber = (4 * numberOfHops) - 1;
                }
            } else {
                stateNumber = (4 * numberOfHops) + 1;
            }
            break;
        }
        case Source.WAITING_FOR_AGENT: {
            stateNumber = 1;
            break;
        }
        case Source.COMMUNICATION: {
            if (this.agent.getState() == Agent.WAITING) {
                //if no tensioning is needed...
                if ((this.numberOfHops == 1) && (!this.forwarded)) {
                    stateNumber = 3;
                } else {
                    stateNumber = (4 * numberOfHops);
                }
                break;
            }
            if (this.agent.getState() == Agent.MIGRATING) {
                stateNumber = (4 * numberOfHops) + 2;
            }
            break;
        }
        case Source.TENSIONING: {
            //                    if (agent.getState() == agent.WAITING_FOR_TENSIONING) {
            //                        stateNumber = -1;
            //                    } else {
            stateNumber = 0;
            //                    }
            break;
        }
        }

        tmp.append(", " + numberOfHops + " hops");
        this.state = "P" + stateNumber;
        //        System.out.println(" === state was " + state + "  lasted " + (currentTime - eventTime));
        //        System.out.println(" === " + tmp);
        this.eventLength = currentTime - eventTime;
        this.eventTime = currentTime;
    }

    public void updateTime() {
        double minTime = Math.min(agent.getRemainingTime(),
                source.getRemainingTime());

        //        System.out.println("   Simulator: remaining time for source " + source.getRemainingTime());
        //        System.out.println("   Simulator: remaining time for agent " + agent.getRemainingTime());
        //	System.out.println("   Simulator: next event at time " + minTime);
        this.source.decreaseRemainingTime(minTime);
        this.agent.decreaseRemainingTime(minTime);
        this.currentTime += minTime;
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println(
                "Usage: java modelisation.simulator.forwarder.Simulator <lambda> <nu> <delta> <gamma> <length>");
            System.exit(-1);
        }
        System.out.println("Starting Simulator");
        System.out.println("     lambda = " + args[0]);
        System.out.println("         nu = " + args[1]);
        System.out.println("      delta = " + args[2]);
        System.out.println("      gamma = " + args[3]);
        System.out.println("     length = " + args[4]);

        Simulator simulator = new Simulator(Double.parseDouble(args[0]),
                Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        simulator.initialise();
        simulator.simulate();
    }
}
