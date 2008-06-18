package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.Experience;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * GeometricBrownianMotion
 *
 * @author The ProActive Team
 */
/**
 * @author vddoan
 *
 */
/**
 * @author vddoan
 *
 */
public class GeometricBrownianMotion implements Experience {

    private double s0, d, mu, sigma, t;
    private int N;

    /**
     * @param s0 initial value
     * @param d  dividend
     * @param mu drift
     * @param sigma volatility
     * @param t time
     * @param N number of Monte Carlo simulations
     */
    public GeometricBrownianMotion(double s0, double d, double mu, double sigma, double t, int n) {
        super();
        this.s0 = s0;
        this.d = d;
        this.mu = mu;
        this.sigma = sigma;
        this.t = t;
        N = n;
    }

    public ArrayList<Double> simulate(Random rng) {
        final ArrayList<Double> answer = new ArrayList<Double>(N);
        for (int i = 0; i < N; i++) {
            answer.add(s0 *
                Math.exp(((mu-d) - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * rng.nextGaussian()));
        }
        return answer;
    }
}
