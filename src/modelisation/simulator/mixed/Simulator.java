package modelisation.simulator.mixed;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;

public class Simulator {

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

	protected boolean agentHasMigrated;

	protected String state;

	protected RandomNumberGenerator expoGamma1;
	protected RandomNumberGenerator expoGamma2;
	protected RandomNumberGenerator expoMu1;
	protected RandomNumberGenerator expoMu2;
	protected RandomNumberGenerator expoDelta;
	protected RandomNumberGenerator expoLambda;
	protected RandomNumberGenerator expoNu;
	protected RandomNumberGenerator expoAlpha;

	public Simulator() {
	};

	public Simulator(
		double lambda,
		double nu,
		double delta,
		double gamma1,
		double gamma2,
		double mu1,
		double mu2,
		double alpha,
		int maxMigration,
		double length) {

		this.length = length;
		this.gamma1 = gamma1;
		this.gamma2 = gamma2;
		this.mu1 = mu1;
		this.mu2 = mu2;
		this.delta = delta;
		this.nu = nu;
		this.lambda = lambda;
		this.alpha = alpha;

		System.out.println("Creating source");
		this.source = new Source(this, lambda);
		System.out.println("Creating Forwarder Chain");

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
	}

	public void initialise() {
		System.out.println("Bench, length is " + length);
		//        this.agent.waitBeforeMigration();
		//        this.source.waitBeforeCommunication();
	}

	public double getCommunicationTimeServer() {
		if (this.expoGamma2 == null) {
			this.expoGamma2 = RandomNumberFactory.getGenerator("gamma2");
			this.expoGamma2.initialize(
				gamma2,
				System.currentTimeMillis() + 276371);
			//            this.expoGamma2.initialize(gamma2, 276371);
		}
		return this.expoGamma2.next() * 1000;
		//        double tmp = this.expoGamma2.next() * 1000;
		//        System.out.println("expoGamma2 = " + tmp);
		//        return tmp;
	}

	public double getServiceTimeMu1() {
		if (this.expoMu1 == null) {
			this.expoMu1 = RandomNumberFactory.getGenerator("mu1");
			this.expoMu1.initialize(mu1, System.currentTimeMillis() + 120457);
			//           this.expoMu1.initialize(mu1, 120457);
		}
		return this.expoMu1.next() * 1000;
	}

	public double getServiceTimeMu2() {
		if (this.expoMu2 == null) {
			this.expoMu2 = RandomNumberFactory.getGenerator("mu2");
			this.expoMu2.initialize(mu2, System.currentTimeMillis() + 67457);
			//            this.expoMu2.initialize(mu2, 67457);
		}
		return this.expoMu2.next() * 1000;
	}

	public double getCommunicationTimeForwarder() {
		if (this.expoGamma1 == null) {
			this.expoGamma1 = RandomNumberFactory.getGenerator("gamma1");
			this.expoGamma1.initialize(
				gamma1,
				System.currentTimeMillis() + 372917);
			//            this.expoGamma1.initialize(gamma1, 372917);
		}
		//        double tmp = this.expoGamma1.next() * 1000;
		//        System.out.println("Gamma1 = " + tmp);
		//        return tmp;
		return this.expoGamma1.next() * 1000;

	}

	public double getMigrationTime() {
		if (this.expoDelta == null) {
			this.expoDelta = RandomNumberFactory.getGenerator("delta");
			this.expoDelta.initialize(
				delta,
				System.currentTimeMillis() + 395672917);
			//            this.expoDelta.initialize(delta, 58373435);
		}
		//        double tmp = expoDelta.next() * 1000;
		//        System.out.println("Agent: migration started, will last " + tmp);
		//        return tmp;
		return this.expoDelta.next() * 1000;
	}

	public double getAgentWaitingTime() {
		if (this.expoNu == null) {
			this.expoNu = RandomNumberFactory.getGenerator("nu");
			//            this.expoNu.initialize(nu, System.currentTimeMillis() + 39566417);
			this.expoNu.initialize(nu, 39566417);
		}
		return expoNu.next() * 1000;
	}

	public double getSourceWaitingTime() {
		if (this.expoLambda == null) {
			this.expoLambda = RandomNumberFactory.getGenerator("lambda");
			this.expoLambda.initialize(
				lambda,
				System.currentTimeMillis() + 8936917);
			//            this.expoLambda.initialize(lambda, 8936917);
		}
		return expoLambda.next() * 1000;
	}

	public double getForwarderLifeTime() {
		if (this.expoAlpha == null) {
			this.expoAlpha = RandomNumberFactory.getGenerator("alpha");
			this.expoAlpha.initialize(alpha, System.currentTimeMillis() + 5437);
			//            this.expoAlpha.initialize(alpha, 4251);
		}
		return expoAlpha.next() * 1000;
	}

	public double getCurrentTime() {
		return this.currentTime;
	}

	public void simulate() {
		double lengthOfState = 0;
		while (this.currentTime < length) {
			lengthOfState = this.updateTime();
			//            System.out.println("length of state = " + lengthOfState);
			//            System.out.println("\n -------------- Time " + this.currentTime + " ----------------------");
			//            System.out.println("*** states before update ***");
			//            System.out.println("STATE: " + this.server + "" + this.source
			//                               + "" + this.agent + " lasted " + lengthOfState);
			//            this.displayState();
			//            System.out.println("***********************");

			this.agent.update(this.currentTime);
			this.forwarderChain.update(this.currentTime);
			this.source.update(this.currentTime);
			this.server.update(this.currentTime);
			//            this.displayState();
			//            System.out.println("--------------------------------------------------------------------");
		}
		System.out.println("Simulator.simulate currentTime " + currentTime);
		// System.out.println("T1 is " + t1);
	}

	public void displayState() {
		System.out.println("Source: " + this.source);
		System.out.println("ForwarderChain " + this.forwarderChain);
		System.out.println("Agent: " + this.agent);
		System.out.println("Server: " + this.server);
	}

	public double updateTime() {
		double minTime = agent.getRemainingTime();
		minTime = Math.min(minTime, source.getRemainingTime());
		minTime = Math.min(minTime, forwarderChain.getRemainingTime());
		minTime = Math.min(minTime, server.getRemainingTime());
		this.source.decreaseRemainingTime(minTime);
		this.agent.decreaseRemainingTime(minTime);
		this.forwarderChain.decreaseRemainingTime(minTime);
		this.server.decreaseRemainingTime(minTime);
		this.currentTime += minTime;
		return minTime;
	}

	public void log(String s) {
		//	System.out.println(s + "     time " + this.currentTime);
			System.out.println(s );
	}

	public static void main(String args[]) {
		if (args.length < 10) {
			System.err.println(
				"Usage: java "
					+ Simulator.class.getName()
					+ " <lambda> <nu> <delta> <gamma1> <gamma2> "
					+ " <mu1> <mu2>  <alpha> <migration> <length>");
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
		System.out.println("      alpha = " + args[7]);
		System.out.println("   max migrations = " + args[8]);
		System.out.println("     length = " + args[9]);

		Simulator simulator =
			new Simulator(
				Double.parseDouble(args[0]),
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
