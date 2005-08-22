package modelisation.simulator.test;

import modelisation.statistics.ExponentialLaw;


public class SimulatorTest {
    protected ExponentialLaw expoA;
    protected ExponentialLaw expoB;
    protected ExponentialLaw expoC;
    protected double a;
    protected double b;
    protected double c;
    protected double length;
    protected int state;

    //    protected int previousState;
    // protected int nextState;
    public SimulatorTest() {
    }

    public SimulatorTest(double a, double b, double c, double length) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.length = length;
        this.state = 0;
    }

    public void simulate() {
        double currentTime = 0;
        double nextEventTime = 0;
        double startTime = 0;
        double endTime;
        while (currentTime < length) {
            System.out.println("State " + state + " at time " + currentTime);
            //            if ((this.state == 1) || (this.state == 2)) {
            if ((this.state == 2)) {
                System.out.println("Counter started at " + currentTime);
                startTime = currentTime;
            }
            if (this.state == 5) {
                if (startTime != 0) {
                    endTime = currentTime;
                    System.out.println("Counter ended at " + currentTime);
                    System.out.println("done after " + (endTime - startTime));
                    startTime = 0;
                }
            }
            nextEventTime = changeState();
            currentTime += nextEventTime;
        }
    }

    public double changeState() {
        double a;
        double b;
        switch (state) {
        case 0: {
            a = getNextA();
            //                    this.state = 1;
            //                    return a;
            b = getNextB();
            //                    System.out.println("" + a + " " + b);
            if (a == Math.min(a, b)) {
                this.state = 1;
                return a;
            } else {
                this.state = 6;
                return b;
            }
        }
        case 1: {
            this.state = 3;
            return getNextB();
        }
        case 2: {
            this.state = 4;
            return getNextA();
        }
        case 3: {
            a = getNextA();
            b = getNextB();
            c = getNextC();
            double min = Math.min(a, b);
            min = Math.min(min, c);
            if (a == min) {
                this.state = 4;
                return a;
            }
            if (b == min) {
                this.state = 5;
                return b;
            }
            if (c == min) {
                this.state = 2;
                return c;
            }
        }
        case 4: {
            a = getNextA();
            b = getNextB();
            c = getNextC();
            double min = Math.min(a, b);
            min = Math.min(min, c);
            if (a == min) {
                this.state = 5;
                return a;
            }
            if (b == min) {
                this.state = 3;
                return b;
            }
            if (c == min) {
                this.state = 1;
                return c;
            }
        }
        case 5: {
            a = getNextA();
            b = getNextB();
            System.out.println("" + a + " " + b);
            if (a == Math.min(a, b)) {
                this.state = 0;
                return a;
            } else {
                this.state = 6;
                return b;
            }
        }
        case 6: {
            a = getNextA();
            b = getNextB();
            //                    this.state = 2;
            //                    System.out.println("" + a + " " + b);
            if (a == Math.min(a, b)) {
                this.state = 0;
                return a;
            } else {
                this.state = 2;
                return b;
            }

            //                    return b;
        }
        case 11: {
            b = getNextB();
            this.state = 3;
            return b;
        }
        case 22: {
            a = getNextA();
            this.state = 4;
            return a;
        }
        }
        return 0;
    }

    protected double getNextA() {
        if (expoA == null) {
            this.expoA = new ExponentialLaw(a);
        }
        return expoA.next();
    }

    protected double getNextB() {
        if (expoB == null) {
            this.expoB = new ExponentialLaw(b,
                    System.currentTimeMillis() + 543245);
        }
        return expoB.next();
    }

    protected double getNextC() {
        if (expoC == null) {
            this.expoC = new ExponentialLaw(c,
                    System.currentTimeMillis() + 501865765);
        }
        return expoC.next();
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java " + SimulatorTest.class.getName() +
                " <a> <b> <c> <length>");
            System.exit(-1);
        }
        SimulatorTest test = new SimulatorTest(Double.parseDouble(args[0]),
                Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                Double.parseDouble(args[3]));
        test.simulate();
    }
}
