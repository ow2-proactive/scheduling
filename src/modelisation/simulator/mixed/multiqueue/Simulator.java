package modelisation.simulator.mixed.multiqueue;

import modelisation.simulator.common.SimulatorElement;

import modelisation.simulator.mixed.Agent;
import modelisation.simulator.mixed.ForwarderChain;
import modelisation.simulator.mixed.Source;


public class Simulator extends modelisation.simulator.mixed.Simulator {

    protected Source[] sourceArray;
    protected Agent[] agentArray;
    protected ForwarderChain[] forwarderChainArray;
    protected int maxCouples;

    /**
     * Constructor for Simulator.
     */
    public Simulator() {
        super();
    }

    /**
     * Constructor for Simulator.
     */
    public Simulator(double lambda, double nu, double delta, double gamma1, 
                     double gamma2, double mu1, double mu2, double ttl, 
                     int maxMigration, int maxCouples, double length) {
        super(lambda, nu, delta, gamma1, gamma2, mu1, mu2, ttl, maxMigration, 
              length);
        this.maxCouples = maxCouples;
    }

    /**
     * @see modelisation.simulator.mixed.Simulator#displayState()
     */
    public void displayState() {
        //        super.displayState();
        this.displayState("Source: ", this.sourceArray);
        this.displayState("ForwarderChain ", this.forwarderChainArray);
        this.displayState("Agent: ", this.agentArray);
        System.out.println("Server: " + this.server);
    }

    public void displayState(String s, SimulatorElement[] tablo) {
        for (int i = 0; i < tablo.length; i++) {
            System.out.println(s + tablo[i].toString());
        }
    }

    /**
     * @see modelisation.simulator.mixed.Simulator#updateTime()
     */
    public double updateTime() {

        double minTime = getMinTime(agentArray);
        double tmp = getMinTime(sourceArray);
        if (minTime == 0) {
            return minTime;
        }
        // minTime = Math.min(minTime, tmp);
        minTime = (tmp <= minTime) ? tmp : minTime;
        if (minTime == 0) {
            return minTime;
        }
        tmp = getMinTime(forwarderChainArray);
        // minTime = Math.min(minTime, tmp);
        minTime = (tmp <= minTime) ? tmp : minTime;
        if (minTime == 0) {
            return minTime;
        }
        tmp = server.getRemainingTime();
        minTime = (tmp <= minTime) ? tmp : minTime; //Math.min(minTime,tmp );
        if (minTime == 0) {
            return minTime;
        }
        this.decreaseTimeElements(minTime);
        this.currentTime += minTime;
        return minTime;
    }

    /**
     * @see modelisation.simulator.mixed.Simulator#decreaseTimeElements(double)
     */
    public void decreaseTimeElements(double time) {
        //        super.decreaseTimeElements(time);
        if (time == 0) {
            return;
        }
        for (int i = 0; i < maxCouples; i++) {
            this.agentArray[i].decreaseRemainingTime(time);
            this.forwarderChainArray[i].decreaseRemainingTime(time);
            this.sourceArray[i].decreaseRemainingTime(time);
        }
        this.server.decreaseRemainingTime(time);
    }

    protected double getMinTime(SimulatorElement[] tablo) {

        double minTime = tablo[0].getRemainingTime();
        double tmp;
        for (int i = 1; i < tablo.length; i++) {
            tmp = tablo[i].getRemainingTime();
            if (tmp == 0) {
                return 0;
            } else {
                minTime = (tmp <= minTime) ? tmp : minTime;
            }
            // minTime = Math.min(minTime, tablo[i].getRemainingTime());
        }
        return minTime;
    }

    /**
     * @see modelisation.simulator.mixed.Simulator#updateElements(double)
     */
    public void updateElements(double currentTime) {
        //        super.updateElements(currentTime);
        this.updateElements(this.agentArray);
        this.updateElements(this.forwarderChainArray);
        this.updateElements(this.sourceArray);
        this.server.update(this.currentTime);
    }

    public void updateElements(SimulatorElement[] tablo) {
        for (int i = 0; i < tablo.length; i++) {
            tablo[i].update(this.currentTime);
        }
    }

    /**
     * @see modelisation.simulator.mixed.Simulator#initialise()
     */
    public void initialise() {
        sourceArray = new Source[maxCouples];
        agentArray = new Agent[maxCouples];
        forwarderChainArray = new ForwarderChain[maxCouples];
        System.out.println("initialise() for " + maxCouples + "  couples");
        this.server = new MultiqueueServer(this, maxCouples);
        for (int i = 0; i < maxCouples; i++) {
            this.sourceArray[i] = new Source(this, lambda, i);
            this.forwarderChainArray[i] = new ForwarderChain(this);
            //    System.out.println("nu is " + nu);
            this.agentArray[i] = new Agent(this, nu, delta, maxMigration, i);
            this.forwarderChainArray[i].setSource(this.sourceArray[i]);
            this.forwarderChainArray[i].setAgent(this.agentArray[i]);
            this.agentArray[i].setForwarderChain(this.forwarderChainArray[i]);
            this.sourceArray[i].setForwarderChain(forwarderChainArray[i]);
            this.agentArray[i].setServer(this.server);
            this.sourceArray[i].setServer(this.server);
        }
        this.server.setLog(false);
        this.sourceArray[0].setLog(false);
        this.agentArray[0].setLog(false);
        this.forwarderChainArray[0].setLog(false);
        ((MultiqueueServer)this.server).setSourceArray(sourceArray);
    }

    public void end() {
        this.sourceArray[0].end();
        this.agentArray[0].end();
        this.forwarderChainArray[0].end();
        this.server.end();
    }

    public static void main(String[] args) {
        if (args.length < 11) {
            System.err.println(
                    "Usage: java " + Simulator.class.getName() + 
                    " <lambda> <nu> <delta> <gamma1> <gamma2> " + 
                    " <mu1> <mu2>  <alpha> <migration> <couples> <length> ");
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
        System.out.println("     ttl = " + args[7]);
        System.out.println("   max migrations = " + args[8]);
        System.out.println("     couples = " + args[9]);
        System.out.println("     length = " + args[10]);

        Simulator simulator = new Simulator(Double.parseDouble(args[0]), 
                                            Double.parseDouble(args[1]), 
                                            Double.parseDouble(args[2]), 
                                            Double.parseDouble(args[3]), 
                                            Double.parseDouble(args[4]), 
                                            Double.parseDouble(args[5]), 
                                            Double.parseDouble(args[6]), 
                                            Double.parseDouble(args[7]), 
                                            Integer.parseInt(args[8]), 
                                            Integer.parseInt(args[9]), 
                                            Double.parseDouble(args[10]));
        simulator.initialise();
        simulator.simulate();
    }
}