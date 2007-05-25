package org.objectweb.proactive.p2p.v2.service.util;

import java.util.Random;


public class NOAPowerLawGenerator {
    protected double a;
    protected double min;
    protected double max;
    protected Random random;

    public NOAPowerLawGenerator(int a) {
        this(0, 0, a);
    }

    public NOAPowerLawGenerator(double min, double max, double a) {
        random = new Random();
        this.a = a;
        this.min = min;
        this.max = max;
    }

    public int nextInt() {
        int result = 0;
        double tmp = Math.pow(max, a + 1.0) - Math.pow(min, a + 1.0);
        tmp = (tmp * random.nextDouble()) + Math.pow(min, a + 1.0);
        result = (int) Math.pow(tmp, 1.0 / (a + 1.0));
        return result;
    }

    public double nextDouble() {
        double tmp = Math.pow(max, a + 1.0) - Math.pow(min, a + 1.0);
        tmp = (tmp * random.nextDouble()) + Math.pow(min, a + 1.0);
        return Math.pow(tmp, 1.0 / (a + 1.0));
        //compute the shape value

        //		double shape = a/(a-1);
        //		return 1/Math.pow(random.nextDouble(),shape);
    }

    public static void main(String[] args) {
        NOAPowerLawGenerator l = new NOAPowerLawGenerator(4, 10, -3);
        for (int i = 0; i < 1000; i++) {
            System.out.println(l.nextInt());
        }
    }
}
