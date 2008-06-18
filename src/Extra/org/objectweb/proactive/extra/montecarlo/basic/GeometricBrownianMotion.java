package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.ArrayList;
import java.util.Random;


/**
 * GeometricBrownianMotion
 *
 * @author The ProActive Team
 */
public class GeometricBrownianMotion implements ExperienceSet {

    private double s0, mu, sigma, t;
    private int N;

    public GeometricBrownianMotion(double s0, double mu, double sigma, double t, int N) {
        this.s0 = s0;
        this.mu = mu;
        this.sigma = sigma;
        this.t = t;
    }

    public ArrayList<Double> simulate(Random rng) {
        final ArrayList<Double> answer = new ArrayList<Double>(N);
        for (int i = 0; i < N; i++) {
            answer.add(s0 *
                Math.exp((mu - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * rng.nextGaussian()));
        }

        return answer;
    }
}
