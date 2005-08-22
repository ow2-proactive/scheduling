package modelisation.simulator.forwarder;

import java.util.ArrayList;
import java.util.Iterator;


public class SimulatorWithoutRDV {
    private Source source;
    private Agent agent;
    private ArrayList requests;
    private double currentTime;
    private double length;
    private int numberOfHops;
    private int forwarded;
    private double gamma;

    public SimulatorWithoutRDV() {
    }
    ;
    public SimulatorWithoutRDV(double lambda, double nu, double delta,
        double gamma, double length) {
        System.out.println("Creating source");
        this.source = new Source(lambda);
        System.out.println("Creating agent");
        this.agent = new Agent(nu, delta);
        this.length = length;
        this.gamma = gamma;
        this.requests = new ArrayList();
    }

    public void initialise() {
        System.out.println("Bench, length is " + length);
        this.agent.waitBeforeMigration();
        this.source.waitBeforeCommunication();
        this.numberOfHops = 1;
    }

    public void simulate() {
        while (this.currentTime < length) {
            this.updateTime();
            if (this.agent.getRemainingTime() == 0) {
                //System.out.println("SimulatorWithoutRDV: Time to update the agent");
                if (this.agent.getState() == Agent.WAITING) {
                    this.agent.startMigration();
                } else {
                    this.agent.endMigration();
                    this.agent.waitBeforeMigration();
                    this.numberOfHops++;
                    for (Iterator e = requests.iterator(); e.hasNext();) {
                        Request r = (Request) e.next();
                        r.setHops(r.getHops() + 1);
                    }
                }
            }
            if (this.source.getRemainingTime() == 0) {
                System.out.println("Source: sending a request");

                requests.add(new Request(currentTime, numberOfHops, gamma));
                this.source.waitBeforeCommunication();
            }

            //int requestSize = requests.getSize();
            for (int i = 0; i < requests.size(); i++) {
                Request r = (Request) requests.get(i);
                if (r.getRemainingTime() == 0) {
                    System.out.println("Request " + i + " need to be updated");
                    if (r.getState() == Request.RUNNING) {
                        r.setHops(r.getHops() - 1);
                        //this is a request we have to update
                        if (r.getHops() == 0) {
                            if (this.agent.getState() == Agent.WAITING) {
                                //the request can reach the agent
                                System.out.println("Source: .....  done after " +
                                    (currentTime - r.getStartTime()));
                                System.out.println(
                                    "Simulator: the request has been forwarded " +
                                    r.getForwarded() + " times");
                                this.numberOfHops = 1;
                                requests.remove(i);
                                i--;
                            }
                            if (this.agent.getState() == Agent.MIGRATING) {
                                r.block(Math.max(r.getRemainingTime(),
                                        this.agent.getRemainingTime()));
                                System.out.println(
                                    "Simulator: the agent was migrating when the source tried to contact it");
                            }
                        } else {
                            System.out.println("Source: the request " + i +
                                " needs to go through " + r.getHops());

                            r.doNextHop();
                        }
                    } else if (r.getState() == Request.BLOCKED) {
                        r.unblock();
                    }
                }
            }
        }
    }

    public void updateTime() {
        double minTime = this.timeNextEvent();
        System.out.println("SimulatorWithoutRDV: next event at time " +
            minTime);
        this.source.decreaseRemainingTime(minTime);
        this.agent.decreaseRemainingTime(minTime);

        for (Iterator e = requests.iterator(); e.hasNext();) {
            Request r = (Request) e.next();
            r.decreaseRemainingTime(minTime);
        }
        this.currentTime += minTime;
    }

    protected double timeNextEvent() {
        double minTime = Math.min(agent.getRemainingTime(),
                source.getRemainingTime());
        for (Iterator e = requests.iterator(); e.hasNext();) {
            Request r = (Request) e.next();
            minTime = Math.min(minTime, r.getRemainingTime());
        }
        return minTime;
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println(
                "Usage: java modelisation.simulator.forwarder.SimulatorWithoutRDV <lambda> <nu> <delta> <gamma> <length>");
            System.exit(-1);
        }
        System.out.println("Starting SimulatorWithoutRDV");
        System.out.println("     lambda = " + args[0]);
        System.out.println("         nu = " + args[1]);
        System.out.println("      delta = " + args[2]);
        System.out.println("      gamma = " + args[3]);
        System.out.println("     length = " + args[4]);

        SimulatorWithoutRDV simulator = new SimulatorWithoutRDV(Double.parseDouble(
                    args[0]), Double.parseDouble(args[1]),
                Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                Double.parseDouble(args[4]));
        simulator.initialise();
        simulator.simulate();
    }
}
