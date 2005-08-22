package modelisation.simulator.mixed.multiqueue;

import modelisation.simulator.mixed.Agent;
import modelisation.simulator.mixed.ForwarderChain;
import modelisation.simulator.mixed.Source;


public class ThreadedSimulator extends Simulator {
    protected ThreadWrapper wrapper;

    public ThreadedSimulator(double lambda, double nu, double delta,
        double gamma1, double gamma2, double mu1, double mu2, double ttl,
        int maxMigration, int maxCouples, double length) {
        super(lambda, nu, delta, gamma1, gamma2, mu1, mu2, ttl, maxMigration,
            maxCouples, length);
        //  this.maxCouples = maxCouples;
    }

    public void updateElements(double currentTime) {
        wrapper.updateElements(currentTime);
        this.server.update(currentTime);
    }

    public void decreaseTimeElements(double time) {
        wrapper.decreaseTimeElements(time);
        this.server.decreaseRemainingTime(time);
    }

    public void initialise() {
        sourceArray = new Source[maxCouples];
        agentArray = new Agent[maxCouples];
        forwarderChainArray = new ForwarderChain[maxCouples];
        System.out.println("initialise() for " + maxCouples + "  couples");
        this.server = new MultiqueueServer(this, maxCouples);
        this.server.setLog(false);
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
            if (i == 0) {
                this.sourceArray[i].setLog(false);
                this.agentArray[i].setLog(false);
                this.forwarderChainArray[i].setLog(false);
            }

            //            this.server.setSource(this.sourceArray[i]);
        }
        ((MultiqueueServer) this.server).setSourceArray(sourceArray);
        //        System.out.println("XXXXXXXXXXXXXXXXXXXXXX");
        this.wrapper = new ThreadWrapper(sourceArray, agentArray,
                forwarderChainArray, server, 10);

        //                                             System.out.println("XXXXXXXXXXXXXXXXXXXXXX");
    }

    public static void main(String[] args) {
        //Simulator.main(args);
        if (args.length < 11) {
            System.err.println("Usage: java " + Simulator.class.getName() +
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
        Simulator simulator = new ThreadedSimulator(Double.parseDouble(args[0]),
                Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                Double.parseDouble(args[3]), Double.parseDouble(args[4]),
                Double.parseDouble(args[5]), Double.parseDouble(args[6]),
                Double.parseDouble(args[7]), Integer.parseInt(args[8]),
                Integer.parseInt(args[9]), Double.parseDouble(args[10]));
        simulator.initialise();
        simulator.simulate();
        System.exit(0);
    }
}
