package modelisation.simulator.mixed;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;
import org.apache.log4j.Logger;

public class Simulator {

protected static Logger logger = Logger.getLogger(Simulator.class.getName());


    protected static final int MAX = 20;
    protected Source source;
    protected Agent agent;
    protected Server server;
    protected ForwarderChain forwarderChain;
    protected double currentTime;
    protected double eventTime;
    protected double eventLength;
    protected double length;
    protected double gamma1;
    protected double gamma2;
    protected double mu1;
    protected double mu2;
    protected double delta;
    protected double nu;
    protected double lambda;
    protected double alpha;
    protected int maxMigration;
    protected boolean agentHasMigrated;
    protected String state;
    protected RandomNumberGenerator expoGamma1;
    protected RandomNumberGenerator expoGamma2;
    protected RandomNumberGenerator expoMu1;
    protected RandomNumberGenerator expoMu2;
    protected RandomNumberGenerator expoDelta;
    protected RandomNumberGenerator expoLambda;
    protected RandomNumberGenerator expoNu;
    protected RandomNumberGenerator randomTTL;
    protected double previousTime;
    protected int sameTimeAsPrevious;

    public Simulator() {
    }

    public Simulator(double lambda, double nu, double delta, double gamma1, 
                     double gamma2, double mu1, double mu2, double alpha, 
                     int maxMigration, double length) {
        this.length = length;
        this.gamma1 = gamma1;
        this.gamma2 = gamma2;
        this.mu1 = mu1;
        this.mu2 = mu2;
        this.delta = delta;
        this.nu = nu;
        this.lambda = lambda;
        this.alpha = alpha;
        this.maxMigration = maxMigration;
    }

    public void initialise() {
if (logger.isDebugEnabled()) {
        logger.debug("Bench, length is " + length);
}
        //        this.agent.waitBeforeMigration();
        //        this.source.waitBeforeCommunication();
if (logger.isDebugEnabled()) {
        logger.debug("Creating source");
}
        this.source = new Source(this, lambda);
if (logger.isDebugEnabled()) {
        logger.debug("Creating Forwarder Chain");
}
        this.forwarderChain = new ForwarderChain(this);
        this.agent = new Agent(this, nu, delta, maxMigration);
        this.server = new Server(this);
        this.forwarderChain.setSource(this.source);
        this.forwarderChain.setAgent(this.agent);
        this.agent.setForwarderChain(this.forwarderChain);
        this.source.setForwarderChain(forwarderChain);
        this.agent.setServer(this.server);
        this.source.setServer(this.server);
        this.server.setSource(this.source);
        this.source.setLog(true);
        this.forwarderChain.setLog(true);
        this.server.setLog(true);
        this.agent.setLog(true);
    }

    public double generateCommunicationTimeServer() {
        if (this.expoGamma2 == null) {
            this.expoGamma2 = RandomNumberFactory.getGenerator("gamma2");
            this.expoGamma2.initialize(gamma2, 
                                       System.currentTimeMillis() + 276371);
//                       this.expoGamma2.initialize(gamma2, 276371);
        }
        return this.expoGamma2.next() * 1000;
    }

    public double generateServiceTimeMu1() {
        if (this.expoMu1 == null) {
            this.expoMu1 = RandomNumberFactory.getGenerator("mu1");
            this.expoMu1.initialize(mu1, System.currentTimeMillis() + 120457);
//                       this.expoMu1.initialize(mu1, 120457);
        }
        return this.expoMu1.next() * 1000;
    }

    public double generateServiceTimeMu2() {
        if (this.expoMu2 == null) {
            this.expoMu2 = RandomNumberFactory.getGenerator("mu2");
            this.expoMu2.initialize(mu2, System.currentTimeMillis() + 67457);
//                        this.expoMu2.initialize(mu2, 67457);
        }
        return this.expoMu2.next() * 1000;
    }

    public double generateCommunicationTimeForwarder() {
        if (this.expoGamma1 == null) {
            this.expoGamma1 = RandomNumberFactory.getGenerator("gamma1");
            this.expoGamma1.initialize(gamma1, 
                                       System.currentTimeMillis() + 372917);
//                        this.expoGamma1.initialize(gamma1, 372917);
        }
        return this.expoGamma1.next() * 1000;
    }

    public double generateMigrationTime() {
        if (this.expoDelta == null) {
            this.expoDelta = RandomNumberFactory.getGenerator("delta");
//            this.expoDelta.initialize(delta, 
//                                      System.currentTimeMillis() + 395672917);
                        this.expoDelta.initialize(delta, System.currentTimeMillis() +  58373435);
        }
        return this.expoDelta.next() * 1000;
    }

    public double generateAgentWaitingTime() {
        if (this.expoNu == null) {
            this.expoNu = RandomNumberFactory.getGenerator("nu");
                        this.expoNu.initialize(nu, System.currentTimeMillis() + 39566417);
//            this.expoNu.initialize(nu, 39566417);
        }
        return expoNu.next() * 1000;
    }

    public double generateSourceWaitingTime() {
        if (this.expoLambda == null) {
            this.expoLambda = RandomNumberFactory.getGenerator("lambda");
            this.expoLambda.initialize(lambda, 
                                       System.currentTimeMillis() + 8936917);
//                        this.expoLambda.initialize(lambda, 8936917);
        }
        return expoLambda.next() * 1000;
    }

    public double generateForwarderLifeTime() {
        if ("INFINITE".equals(System.getProperty("forwarder.lifetime"))) {
            return new Double(Double.MAX_VALUE).doubleValue();
        }
        if (this.randomTTL == null) {
            this.randomTTL = RandomNumberFactory.getGenerator("alpha");
            this.randomTTL.initialize(1 / alpha, 
                                      System.currentTimeMillis() + 5437);
//                        this.randomTTL.initialize(alpha, 4251);
        }
if (logger.isDebugEnabled()) {
        //   logger.debug(randomTTL.next());
}
        return randomTTL.next();
    }

    public double getCurrentTime() {
        return this.currentTime;
    }

    public void simulate() {
double startTime = System.currentTimeMillis();
        double lengthOfState = 0;
        this.previousTime = 0;

        int nextEcho = 5;
        while (this.currentTime < length) {
            lengthOfState = this.updateTime();
            if (this.previousTime == this.currentTime) {
                this.sameTimeAsPrevious++;
            } else {
                this.previousTime = this.currentTime;
                this.sameTimeAsPrevious = 0;
            }
            if (this.sameTimeAsPrevious > Simulator.MAX) {
if (logger.isDebugEnabled()) {
                logger.debug(" Simulation loop detected ");
}
                this.displayState();
                System.exit(-1);
            }
            if (this.currentTime > (length * nextEcho / 100)) {
                System.err.println(
                        nextEcho + "% at time " + 
                        java.util.Calendar.getInstance().getTime());
                nextEcho += 5;
            }
if (logger.isDebugEnabled()) {
            //            logger.debug("length of state = " + lengthOfState);
            //            logger.debug(
}
            //                    "\n -------------- Time " + this.currentTime +
            //                    " ----------------------");
if (logger.isDebugEnabled()) {
            //            logger.debug("*** states before update ***");
            //            logger.debug("STATE: " + this.server + "" + this.source
}
            //                               + "" + this.agent + " lasted " + lengthOfState);
            //            this.displayState();
if (logger.isDebugEnabled()) {
            //            logger.debug("***********************");
}
            this.updateElements(currentTime);
            //                this.displayState();
if (logger.isDebugEnabled()) {
            //            logger.debug("--------------------------------------------------------------------");
}
        }
        this.end();
        double endTime = System.currentTimeMillis();
if (logger.isDebugEnabled()) {
        logger.debug("Total Execution Time " + (endTime - startTime));
        //        logger.debug("Simulator.simulate currentTime " + currentTime);
        // logger.debug("T1 is " + t1);
}
    }

    public void end() {
        this.source.end();
    }

    public void displayState() {
if (logger.isDebugEnabled()) {
        logger.debug("Source: " + this.source);
        logger.debug("ForwarderChain " + this.forwarderChain);
        logger.debug("Agent: " + this.agent);
        logger.debug("Server: " + this.server);
}
    }

    public double updateTime() {

        double minTime = agent.getRemainingTime();
        minTime = Math.min(minTime, source.getRemainingTime());
        minTime = Math.min(minTime, forwarderChain.getRemainingTime());
        minTime = Math.min(minTime, server.getRemainingTime());
        this.decreaseTimeElements(minTime);
        this.currentTime += minTime;
        return minTime;
    }

    public void updateElements(double currentTime) {
        this.agent.update(this.currentTime);
        this.forwarderChain.update(this.currentTime);
        this.source.update(this.currentTime);
        this.server.update(this.currentTime);
    }

    public void decreaseTimeElements(double time) {
        this.source.decreaseRemainingTime(time);
        this.agent.decreaseRemainingTime(time);
        this.forwarderChain.decreaseRemainingTime(time);
        this.server.decreaseRemainingTime(time);
    }

    public void log(String s) {
if (logger.isDebugEnabled()) {
        logger.debug(s + "     time " + this.currentTime);
        //        logger.debug(s);
}
    }

    public static void main(String[] args) {
        if (args.length < 10) {
            System.err.println(
                    "Usage: java " + Simulator.class.getName() + 
                    " <lambda> <nu> <delta> <gamma1> <gamma2> " + 
                    " <mu1> <mu2>  <alpha> <migration> <length>");
            System.exit(-1);
        }
if (logger.isDebugEnabled()) {
        logger.debug("Starting Simulator");
        logger.debug("     lambda = " + args[0]);
        logger.debug("         nu = " + args[1]);
        logger.debug("      delta = " + args[2]);
        logger.debug("      gamma1 = " + args[3]);
        logger.debug("      gamma2 = " + args[4]);
        logger.debug("      mu1 = " + args[5]);
        logger.debug("      mu2 = " + args[6]);
        logger.debug("      alpha = " + args[7]);
        logger.debug("   max migrations = " + args[8]);
        logger.debug("     length = " + args[9]);
}

        Simulator simulator = new Simulator(Double.parseDouble(args[0]), 
                                            Double.parseDouble(args[1]), 
                                            Double.parseDouble(args[2]), 
                                            Double.parseDouble(args[3]), 
                                            Double.parseDouble(args[4]), 
                                            Double.parseDouble(args[5]), 
                                            Double.parseDouble(args[6]), 
                                            Double.parseDouble(args[7]), 
                                            Integer.parseInt(args[8]), 
                                            Double.parseDouble(args[9]));
        simulator.initialise();
        simulator.simulate();
    }
}
