package modelisation.simulator.mixed.mixedwithcalendar;

import org.apache.log4j.Logger;


public class SimulatorWithMaxTime extends Simulator {
    static Logger logger = Logger.getLogger(SimulatorWithMaxTime.class.getName());
    protected int maxTime;

    public SimulatorWithMaxTime(double lambda, double nu, double delta,
        double gamma1, double gamma2, double mu1, double mu2, double ttl,
        int maxMigration, int maxTime, int maxCouples, double length) {
        super(lambda, nu, delta, gamma1, gamma2, mu1, mu2, ttl, maxMigration,
            maxCouples, length);
        this.maxTime = maxTime;
        //this.maxCouples = maxCouples;
        //    this.calendar = new LinkedListCalendar();
    }

    public void initialise() {
        sourceArray = new Source[maxCouples];
        agentArray = new AgentWithMaxTime[maxCouples];
        forwarderChainArray = new ForwarderChain[maxCouples];
        logger.info("initialise() for " + maxCouples + "  couples");
        this.server = new MultiqueueServer(this, maxCouples);
        for (int i = 0; i < maxCouples; i++) {
            this.sourceArray[i] = new Source(this, lambda, i);
            this.forwarderChainArray[i] = new ForwarderChain(this);
            //    System.out.println("nu is " + nu);
            this.agentArray[i] = new AgentWithMaxTime(this, nu, delta,
                    maxMigration, maxTime, i);
            this.forwarderChainArray[i].setSource(this.sourceArray[i]);
            this.forwarderChainArray[i].setAgent(this.agentArray[i]);
            this.agentArray[i].setForwarderChain(this.forwarderChainArray[i]);
            this.sourceArray[i].setForwarderChain(forwarderChainArray[i]);
            this.agentArray[i].setServer(this.server);
            this.sourceArray[i].setServer(this.server);
            //            this.server.setSource(this.sourceArray[i]);
        }
        this.sourceArray[0].setLog(false);
        this.agentArray[0].setLog(false);
        this.server.setLog(false);
        this.forwarderChainArray[0].setLog(false);
        ((MultiqueueServer) this.server).setSourceArray(sourceArray);
    }

    public static void main(String[] args) {
        if (args.length < 11) {
            System.err.println("Usage: java " + Simulator.class.getName() +
                " <lambda> <nu> <delta> <gamma1> <gamma2> " +
                " <mu1> <mu2>  <alpha> <migration> <couples> <length> ");
            System.exit(-1);
        }

        logger.info("Starting Simulator");
        logger.info("     lambda = " + args[0]);
        logger.info("         nu = " + args[1]);
        logger.info("      delta = " + args[2]);
        logger.info("      gamma1 = " + args[3]);
        logger.info("      gamma2 = " + args[4]);
        logger.info("      mu1 = " + args[5]);
        logger.info("      mu2 = " + args[6]);
        logger.info("     ttl = " + args[7]);
        logger.info("   max migrations = " + args[8]);
        logger.info("   max time on site = " + args[9]);
        logger.info("     couples = " + args[10]);
        logger.info("     length = " + args[11]);

        SimulatorWithMaxTime simulator = new SimulatorWithMaxTime(Double.parseDouble(
                    args[0]), Double.parseDouble(args[1]),
                Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                Double.parseDouble(args[4]), Double.parseDouble(args[5]),
                Double.parseDouble(args[6]), Double.parseDouble(args[7]),
                Integer.parseInt(args[8]), Integer.parseInt(args[9]),
                Integer.parseInt(args[10]), Double.parseDouble(args[11]));
        simulator.initialise();
        simulator.simulate();
    }
}
