package modelisation.simulator.mixed.mixedwithcalendar;

import modelisation.simulator.common.Averagator;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

//import modelisation.simulator.mixed.Agent;
//import modelisation.simulator.mixed.ForwarderChain;
//import modelisation.simulator.mixed.Source;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Locale;
import java.util.Vector;


public class Simulator {
    //  extends modelisation.simulator.mixed.multiqueue.Simulator {
    static Logger logger = Logger.getLogger(Simulator.class.getName());
    static DecimalFormat df;

    static {
        df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        df.applyPattern("##0.00");
    }

    protected State firstState = new State(new int[] { 1, 0, 0, 0 });
    protected Path[] preferedPaths;
    protected Source[] sourceArray;
    protected Agent[] agentArray;
    protected ForwarderChain[] forwarderChainArray;
    protected Calendar calendar;
    protected Server server;
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
    protected double ttl;
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
    protected int maxCouples;
    protected Path visitedStates;
    protected Averagator averagatorPreferedPath;
    protected int totalFirst;

protected int tmpCounter;

    public Simulator() {
    }

    public Simulator(double lambda, double nu, double delta, double gamma1,
        double gamma2, double mu1, double mu2, double ttl, int maxMigration,
        int maxCouples, double length) {
        // super(lambda, nu, delta, gamma1, gamma2, mu1, mu2, ttl, maxMigration,
        //     maxCouples, length);
        this.length = length;
        this.gamma1 = gamma1;
        this.gamma2 = gamma2;
        this.mu1 = mu1;
        this.mu2 = mu2;
        this.delta = delta;
        this.nu = nu;
        this.lambda = lambda;
        this.ttl = ttl;
        this.maxMigration = maxMigration;
        this.maxCouples = maxCouples;
        this.calendar = new LinkedListCalendar();
        this.averagatorPreferedPath = new Averagator();
        this.preferedPaths = generatePreferedPaths();
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
            this.agentArray[i] = new Agent(this, nu, delta, ttl, maxMigration, i);
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
        ((MultiqueueServer) this.server).setSourceArray(sourceArray);
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
//    if (tmpCounter++ > 1000 ) {
//    	System.err.println("Changing Gamma1");
//    	gamma1=gamma1*5;
//		this.expoGamma1.initialize(gamma1,
//					   System.currentTimeMillis());
//					   this.tmpCounter=0;
//    	
//    } else {
    
    	
        if (this.expoGamma1 == null) {
            this.expoGamma1 = RandomNumberFactory.getGenerator("gamma1");
            this.expoGamma1.initialize(gamma1,
                System.currentTimeMillis() + 372917);

            //                        this.expoGamma1.initialize(gamma1, 372917);
        }
//    }
        return this.expoGamma1.next() * 1000;
    
    }

    public double generateMigrationTime() {
//		if (tmpCounter++ > 1000 ) {
//			   System.err.println("Changing delta");
//			   delta=delta/1.5;
//			this.expoDelta.initialize(delta,
//							System.currentTimeMillis() + 395672917);
//    	tmpCounter=0;
//		   } else {
//    
//    	
    	
        if (this.expoDelta == null) {
            this.expoDelta = RandomNumberFactory.getGenerator("delta");
            this.expoDelta.initialize(delta,
                System.currentTimeMillis() + 395672917);

            //                        this.expoDelta.initialize(delta, 58373435);
        }
//		}

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
            this.randomTTL = RandomNumberFactory.getGenerator("ttl");
            this.randomTTL.initialize(1 / ttl, System.currentTimeMillis() +
                5437);

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
        this.previousTime = 0;

        int nextEcho = 5;
        this.visitedStates = new Path();

        while (this.currentTime < length) {
            if (Simulator.logger.isDebugEnabled()) {
                logger.debug("----------- " + df.format(this.currentTime));
                logger.debug(this.calendar);
            }
            this.addState(currentTime, this.getState());

            Event[] events = calendar.removeNextEvents();

            if (events == null) {
                System.err.println("No events to performs, stoping");
                System.exit(-1);
            }
            this.currentTime = events[0].getTime();

            if (Simulator.logger.isDebugEnabled()) {
                logger.debug("     " + this.calendar);

                //  logger.debug(this.getState());
                logger.debug(this.getVisitedStates(false));
            }

            for (int i = 0; i < events.length; i++) {
                events[i].getObject().update(this.currentTime);
            }

            if (this.currentTime > ((length * nextEcho) / 100)) {
                System.err.println(nextEcho + "% at time " +
                    java.util.Calendar.getInstance().getTime());
                nextEcho += 5;
            }
        }
        this.end();

        double endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time " + (endTime - startTime));
    }

    public void addState(double time, int[] state) {
        State newState = new State(time, state);

        //        System.out.println(newState);
        if (this.preferedPaths.length == 0) {
        	return;
        }
        
        if (this.preferedPaths[0].get(0).equals(newState)) {
            totalFirst++;
            this.visitedStates.clear();
        }
        this.visitedStates.add(newState);

        if (endOfPreferedPath(newState)) {
            if (this.processVisitedStates()) {
                this.visitedStates.clear();
            }
        }

        //this.visitedStates.add(newState);
    }

    /**
     * check if s is the last State of one of our prefered paths
     */
    protected boolean endOfPreferedPath(State s) {
        for (int i = 0; i < preferedPaths.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("comparing " + s + " and  " +
                    preferedPaths[i].getLast());
            }

            if (preferedPaths[i].getLast().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public String getVisitedStates(boolean time) {
        return visitedStates.toString();
    }

    /**
    *  we check wether the path followed is the one we wanted
    */
    public boolean processVisitedStates() {
        boolean result = false;

//        if (logger.isDebugEnabled()) {
//            logger.debug("processVisitedStates");
//            logger.debug("comparing ");
//            logger.debug(this.visitedStates + " Reference");
//        }

        //
        for (int i = 0; i < preferedPaths.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug(this.preferedPaths[i] + " i =  " + i);
            }
            if (isPreferedPath(this.visitedStates,this.preferedPaths[i])) {
//				if (isPartialPreferedPath(this.visitedStates,this.preferedPaths[i])) {
                if (logger.isDebugEnabled()) {
                    logger.debug("it's a match " +
                        (this.currentTime -
                        this.visitedStates.get(0).getTime()));
                }
                result = true;

                this.preferedPaths[i].addTime((this.currentTime -
                    ((State) visitedStates.get(0)).getTime()));
                this.averagatorPreferedPath.add((this.currentTime -
                    ((State) visitedStates.get(0)).getTime()));
            }
        }
        return result;
    }

    /**
     * Checks wether path matches the reference path
     * @param reference
     * @param path
     * @return boolean
     */
    protected boolean isPreferedPath(Path path, Path reference) {
        return reference.equals(path);
    }

    /**
     * path is a partial prefered path the subpath built with its first, last to one and last
     * elements is equals to the reference path.
     * @param path
     * @param reference
     * @return boolean
     */
    protected boolean isPartialPreferedPath(Path path, Path reference) {
        // boolean result = false;
        Path tmpPath = new Path();
        tmpPath.add(path.get(0));
        tmpPath.add(path.get(path.size() - 2));
        tmpPath.add(path.get(path.size() - 1));
//System.out.println(tmpPath);
//System.out.println(reference);
        return reference.equals(tmpPath);
    }

    protected void displayTimeBetweenVisitedStates() {
        Object[] array = this.visitedStates.toArray();

        for (int i = 0; i < (array.length - 1); i++) {
            logger.debug((State) array[i] + "->" + (State) array[i + 1] + " " +
                (((State) array[i + 1]).getTime() -
                ((State) array[i]).getTime()));
        }

        //  logger.info(
        //         (State)array[array.length - 1] + "->1,0,0 " +
        //        (this.currentTime - ((State)array[array.length - 1]).getTime()));
    }

    //    public Path[] generatePreferedPathsFinal() {
    //        Path[] tmp = new Path[7];
    //        tmp[0] = new Path();
    //        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[0].add(new State(new int[] { 1, 1, 0, 0 }));
    //        tmp[0].add(new State(new int[] { 2, 0, 0, 0 }));
    //        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[0].add(new State(new int[] { 1, 0, 1, 0 }));
    //
    //        //        tmp[0].add(new State(new int[] { 0, 0, 1, 0 }));
    //        tmp[1] = new Path();
    //        tmp[1].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[1].add(new State(new int[] { 1, 1, 0, 0 }));
    //        tmp[1].add(new State(new int[] { 1, 1, 1, 0 }));
    //        tmp[1].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[1].add(new State(new int[] { 1, 0, 1, 0 }));
    //
    //        //    tmp[2].add(new State(new int[] { 0, 0, 1, 0 }));
    //        tmp[2] = new Path();
    //        tmp[2].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[2].add(new State(new int[] { 1, 0, 1, 1 }));
    //        tmp[2].add(new State(new int[] { 1, 1, 1, 0 }));
    //        tmp[2].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[2].add(new State(new int[] { 1, 0, 1, 0 }));
    //
    //        //			 tmp[1].add(new State(new int[] { 0, 0, 1, 0 }));
    //        tmp[3] = new Path();
    //        tmp[3].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[3].add(new State(new int[] { 1, 1, 0, 0 }));
    //        tmp[3].add(new State(new int[] { 2, 0, 0, 0 }));
    //        tmp[3].add(new State(new int[] { 2, 1, 0, 0 }));
    //        tmp[3].add(new State(new int[] { 2, 1, 1, 0 }));
    //        tmp[3].add(new State(new int[] { 1, 1, 1, 0 }));
    //
    //        tmp[4] = new Path();
    //        tmp[4].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[4].add(new State(new int[] { 1, 1, 0, 0 }));
    //        tmp[4].add(new State(new int[] { 2, 0, 0, 0 }));
    //        tmp[4].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[4].add(new State(new int[] { 2, 1, 1, 0 }));
    //        tmp[4].add(new State(new int[] { 1, 1, 1, 0 }));
    //
    //        tmp[5] = new Path();
    //        tmp[5].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[5].add(new State(new int[] { 1, 1, 0, 0 }));
    //        tmp[5].add(new State(new int[] { 1, 1, 1, 0 }));
    //        tmp[5].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[5].add(new State(new int[] { 2, 1, 1, 0 }));
    //        tmp[5].add(new State(new int[] { 1, 1, 1, 0 }));
    //
    //        tmp[6] = new Path();
    //        tmp[6].add(new State(new int[] { 1, 0, 0, 0 }));
    //        tmp[6].add(new State(new int[] { 1, 0, 1, 1 }));
    //        tmp[6].add(new State(new int[] { 1, 1, 1, 0 }));
    //        tmp[6].add(new State(new int[] { 2, 0, 1, 0 }));
    //        tmp[6].add(new State(new int[] { 2, 1, 1, 0 }));
    //        tmp[6].add(new State(new int[] { 1, 1, 1, 0 }));
    //
    //        return tmp;
    //    }
    public Path[] generatePreferedPathsFinal() {
        // Path[] tmp = new Path[7];
        Vector tmp = new Vector();
        Path path = null;

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 0, 1, 1 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 1, 0, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 0, 0 }));
        path.add(new State(new int[] { 2, 0, 0, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 1, 0, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 1, 0, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 0, 1, 1 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 2, 1, 1, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 0, 0 }));
        path.add(new State(new int[] { 2, 0, 0, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 2, 1, 1, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        path.add(new State(new int[] { 2, 0, 1, 0 }));
        path.add(new State(new int[] { 2, 1, 1, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        tmp.add(path);

        path = new Path();
        path.add(new State(new int[] { 1, 0, 0, 0 }));
        path.add(new State(new int[] { 1, 1, 0, 0 }));
        path.add(new State(new int[] { 2, 0, 0, 0 }));
        path.add(new State(new int[] { 2, 1, 0, 0 }));
        path.add(new State(new int[] { 2, 1, 1, 0 }));
        path.add(new State(new int[] { 1, 1, 1, 0 }));
        tmp.add(path);

        return (Path[]) tmp.toArray(new Path[1]);
    }

    public Path[] generatePreferedPathsi01n() {
        Path[] tmp = new Path[2];
        tmp[0] = new Path();
        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 0, 1, 1 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[0].add(new State(new int[] { 1, 0, 1, 0 }));
        tmp[1] = new Path();
        tmp[1].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 1, 1, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[1].add(new State(new int[] { 1, 0, 1, 0 }));

        return tmp;
    }

    public Path[] generatePreferedPathsi01d() {
        Path[] tmp = new Path[1];
        tmp[0] = new Path();
        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[0].add(new State(new int[] { 1, 0, 1, 0 }));

        return tmp;
    }

    public Path[] generatePartialPreferedPathsi01d() {
        Path[] tmp = new Path[1];
        tmp[0] = new Path();
        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[0].add(new State(new int[] { 1, 0, 1, 0 }));

        return tmp;
    }

    public Path[] generatePreferedPathsi11n() {
        Path[] tmp = new Path[2];
        tmp[0] = new Path();
        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 0, 1, 1 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[1] = new Path();
        tmp[1].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 1, 1, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[1].add(new State(new int[] { 2, 1, 1, 0 }));
        tmp[1].add(new State(new int[] { 1, 1, 1, 0 }));

        return tmp;
    }

    public Path[] generatePreferedPathsi11d() {
        Path[] tmp = new Path[2];
        tmp[0] = new Path();
        tmp[0].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 0, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 0, 1, 0 }));
        tmp[0].add(new State(new int[] { 2, 1, 1, 0 }));
        tmp[0].add(new State(new int[] { 1, 1, 1, 0 }));
        tmp[1] = new Path();
        tmp[1].add(new State(new int[] { 1, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 1, 1, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 0, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 1, 0, 0 }));
        tmp[1].add(new State(new int[] { 2, 1, 1, 0 }));
        tmp[1].add(new State(new int[] { 1, 1, 1, 0 }));

        return tmp;
    }

    public Path[] generatePreferedPaths() {
        //        Path[] tmp =//  new Path[3];
//      Path[] tmp = this.generatePreferedPathsFinal();
Path[] tmp = new Path[0];
//                Path[] tmp = this.generatePreferedPathsi01n();
//         Path[] tmp = this.generatePreferedPathsi01d();
//		Path[] tmp = this.generatePartialPreferedPathsi01d();
        //		Path[] tmp = this.generatePreferedPathsi11n();
        //        Path[] tmp = this.generatePreferedPathsi11d();
        logger.info("Prefered paths:");

        for (int i = 0; i < tmp.length; i++) {
            logger.info(tmp[i]);
        }

        return tmp;
    }

    public void end() {
        this.sourceArray[0].end();
        this.agentArray[0].end();
        this.forwarderChainArray[0].end();
        this.server.end();
        Simulator.logger.info(" XXXX total first " + totalFirst);
        logger.info("########## PATHS ##################");

        for (int i = 0; i < preferedPaths.length; i++) {
            this.preferedPaths[i].end();
        }
        Simulator.logger.info("Time prefered path = " +
            this.averagatorPreferedPath.average() + " " +
            this.averagatorPreferedPath.getCount());
        Simulator.logger.info("Probability prefered path = " +
            ((double) this.averagatorPreferedPath.getCount() / totalFirst));
        Simulator.logger.info("Total First = " + totalFirst);
    }

    public void addEvent(Event e) {
        this.calendar.addEvent(e);
    }

    public boolean removeEvent(Event event) {
        return this.calendar.removeEvent(event);
    }

    /**
    *  works only when using forwarding scheme
    */
    public String getStateAsString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(forwarderChainArray[0].getNumberOfHops()).append(",");

        if (this.agentArray[0].getState() == Agent.WAITING) {
            tmp.append("0,");
        } else {
            tmp.append("1,");
        }
        tmp.append(this.sourceArray[0].getStateAsString());

        //  if (tmp.toString().equals("1,1,1*")) {
        //      return "1,1,1";
        //   }
        return tmp.toString();
    }

    /**
     * returns the state of the system as 4 int
     * the 3 first are related to the markov chain
     * the 4th represents the "*" symbol
     */
    public int[] getState() {
        String tmpString = null;
        int[] tmp = new int[4];
        tmp[0] = forwarderChainArray[0].getNumberOfHops();

        if (this.agentArray[0].getState() == Agent.WAITING) {
            tmp[1] = 0;
        } else {
            tmp[1] = 1;
        }
        tmpString = this.sourceArray[0].getStateAsString();
        tmp[3] = 0;

        if (tmpString.equals("0")) {
            tmp[2] = 0;
        } else if (tmpString.equals("1")) {
            tmp[2] = 1;
        } else {
            tmp[2] = 1;
            tmp[3] = 1;
        }

        return tmp;
    }

    public static void main(String[] args) {
        //        Simulator s = new Simulator(1, 1, 10, 100, 100, 1000, 1000, 100000000,
        //                                    100000000, 1, 100000);
        //        s.initialise();
        //        s.simulate();
        //        System.exit(0);
        if (args.length < 11) {
            System.err.println("Usage: java " + Simulator.class.getName() +
                " <lambda> <nu> <delta> <gamma1> <gamma2> " +
                " <mu1> <mu2>  <alpha> <migration> <couples> <length> ");
            System.exit(-1);
        }

        //   BasicConfigurator.configure();
        if (logger.isEnabledFor(Priority.INFO)) {
            logger.info("Starting Simulator");
            logger.info("     lambda = " + args[0]);
            logger.info("     nu = " + args[1]);
            logger.info("     delta = " + args[2]);
            logger.info("     gamma1 = " + args[3]);
            logger.info("     gamma2 = " + args[4]);
            logger.info("     mu1 = " + args[5]);
            logger.info("     mu2 = " + args[6]);
            logger.info("     ttl = " + args[7]);
            logger.info("     max migrations = " + args[8]);
            logger.info("     couples = " + args[9]);
            logger.info("     length = " + args[10]);
        }

        Simulator simulator = new Simulator(Double.parseDouble(args[0]),
                Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                Double.parseDouble(args[3]), Double.parseDouble(args[4]),
                Double.parseDouble(args[5]), Double.parseDouble(args[6]),
                Double.parseDouble(args[7]), Integer.parseInt(args[8]),
                Integer.parseInt(args[9]), Double.parseDouble(args[10]));
        simulator.initialise();
        simulator.simulate();
    }
}
