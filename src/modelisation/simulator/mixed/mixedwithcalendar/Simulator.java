package modelisation.simulator.mixed.mixedwithcalendar;

//import modelisation.simulator.mixed.Agent;
//import modelisation.simulator.mixed.ForwarderChain;
//import modelisation.simulator.mixed.Source;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Simulator
    extends modelisation.simulator.mixed.multiqueue.Simulator {

    protected Source[] sourceArray;
    protected Agent[] agentArray;
    protected ForwarderChain[] forwarderChainArray;
    protected Calendar calendar;
    protected Server server;

    public Simulator() {
    }

    public Simulator(double lambda, double nu, double delta, double gamma1, 
                     double gamma2, double mu1, double mu2, double ttl, 
                     int maxMigration, int maxCouples, double length) {
        super(lambda, nu, delta, gamma1, gamma2, mu1, mu2, ttl, maxMigration, 
              maxCouples, length);
        //this.maxCouples = maxCouples;
        this.calendar = new LinkedListCalendar();
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
            //            this.server.setSource(this.sourceArray[i]);
        }
        this.sourceArray[0].setLog(false);
        this.agentArray[0].setLog(false);
        this.forwarderChainArray[0].setLog(false);
        ((MultiqueueServer)this.server).setSourceArray(sourceArray);
    }

    public double generateCommunicationTimeServer() {
        if (this.expoGamma2 == null) {
            this.expoGamma2 = RandomNumberFactory.getGenerator("gamma2");
            this.expoGamma2.initialize(gamma2, 
                                       System.currentTimeMillis() + 276371);
//                        this.expoGamma2.initialize(gamma2, 276371);
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
            this.expoDelta.initialize(delta, 
                                      System.currentTimeMillis() + 395672917);
//                        this.expoDelta.initialize(delta, 58373435);
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
//                       this.expoLambda.initialize(lambda, 8936917);
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
        //   System.out.println(randomTTL.next());
        return randomTTL.next();
    }

    public double getCurrentTime() {
        return this.currentTime;
    }

    public void simulate() {

        double startTime = System.currentTimeMillis();
        double lengthOfState = 0;
        this.previousTime =0;
        int nextEcho = 5;
        while (this.currentTime < length) {
//            System.out.println(">>>>>");
//            System.out.println(this.calendar);

            Event[] events = calendar.removeNextEvents();
            if (events == null) {
                System.err.println("No events to performs, stoping");
                System.exit(-1);
            }
            this.currentTime = events[0].getTime();
            //            if (events[0].object.getClass().getName().equals("modelisation.simulator.mixed.mixedwithcalendar.Forwarder")) {
            //               System.out.println(this.calendar);
            //            }
            for (int i = 0; i < events.length; i++) {
                events[i].getObject().update(this.currentTime);
            }
//            System.out.println(this.calendar);
//            System.out.println("<<<<<");
//         
            if (this.currentTime > (length * nextEcho / 100)) {
                System.err.println(
                        nextEcho + "% at time " + 
                        java.util.Calendar.getInstance().getTime());
                nextEcho += 5;
            }         
        }
        this.end();

        double endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time " + (endTime - startTime));
        //        System.out.println("Simulator.simulate currentTime " + currentTime);
        // System.out.println("T1 is " + t1);
    }

    public void end() {
        this.sourceArray[0].end();
        this.agentArray[0].end();
        this.forwarderChainArray[0].end();
        this.server.end();
    }

    public void addEvent(Event e) {
        this.calendar.addEvent(e);
    }

    public boolean removeEvent(Event event) {
        return this.calendar.removeEvent(event);
    }

    public void displayState() {
        System.out.println("Source: " + this.source);
        System.out.println("ForwarderChain " + this.forwarderChain);
        System.out.println("Agent: " + this.agent);
        System.out.println("Server: " + this.server);
    }

    //    public double updateTime() {
    //
    //        double minTime = agent.getRemainingTime();
    //        minTime = Math.min(minTime, source.getRemainingTime());
    //        minTime = Math.min(minTime, forwarderChain.getRemainingTime());
    //        minTime = Math.min(minTime, server.getRemainingTime());
    //        this.decreaseTimeElements(minTime);
    //        this.currentTime += minTime;
    //        return minTime;
    //    }
    //
    //    public void updateElements(double currentTime) {
    //        this.agent.update(this.currentTime);
    //        this.forwarderChain.update(this.currentTime);
    //        this.source.update(this.currentTime);
    //        this.server.update(this.currentTime);
    //    }
    //
    //    public void decreaseTimeElements(double time) {
    //        this.source.decreaseRemainingTime(time);
    //        this.agent.decreaseRemainingTime(time);
    //        this.forwarderChain.decreaseRemainingTime(time);
    //        this.server.decreaseRemainingTime(time);
    //    }
    public void log(String s) {
        System.out.println(s + "     time " + this.currentTime);
        //        System.out.println(s);
    }

    public static void main(String[] args) {
        //               Simulator s = new Simulator(1, 1, 10, 100, 100, 1000, 1000, 1000, 10, 1,
        //                                        100000);
        //              s.initialise();
        //            s.simulate();
        //           System.exit(0);
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