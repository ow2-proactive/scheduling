/**
 *
 */
package org.objectweb.proactive.extra.montecarlo.basic;

import java.util.ArrayList;
import java.util.Random;

import org.objectweb.proactive.extra.montecarlo.Experience;

/**
 * @author vddoan
 *
 */
public class BrownianBridge implements Experience {

    private double w0,wT,t,T;
    /**
     * @param w0 Known fixed at time 0 value of the Brownian motion
     * @param wT Know fixed at time T value of the Brownian motion
     * @param t  A given time
     * @param tT Maturity date
     */
    public BrownianBridge(double w0, double wT, double t, double T) {
        super();
        this.w0 = w0;
        this.wT = wT;
        this.t = t;
        this.T = T;
    }

    /**
     * The Brownian Bridge is used to generate new samples between two known samples of a Brownian motion path.
     * i.e generate sample at time t using sample at time 0 and at T, with 0<t<T
     */
    public ArrayList<Double> simulate(Random rng) {
        // TODO Auto-generated method stub
        final ArrayList<Double> answer = new ArrayList<Double>(1);
        answer.add(w0 + (t/T)*(wT-w0) + Math.sqrt(t*(T-t)/T)*rng.nextGaussian());
        return answer;
    }

}
