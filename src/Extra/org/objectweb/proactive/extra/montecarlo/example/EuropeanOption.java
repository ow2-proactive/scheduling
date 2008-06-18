package org.objectweb.proactive.extra.montecarlo.example;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;


public class EuropeanOption implements EngineTask {

    private double spotPrice, strikePrice, dividend, interestRate, volatilityRate, maturityDate;
    private int N, M;

    /**
     * @param spotPrice
     * @param strikePrice
     * @param dividend
     * @param interestRate
     * @param volatilityRate
     * @param maturityDate
     * @param n
     * @param m
     */
    public EuropeanOption(double spotPrice, double strikePrice, double dividend, double interestRate,
            double volatilityRate, double maturityDate, int n, int m) {
        super();
        this.spotPrice = spotPrice;
        this.strikePrice = strikePrice;
        this.dividend = dividend;
        this.interestRate = interestRate;
        this.volatilityRate = volatilityRate;
        this.maturityDate = maturityDate;
        N = n;
        M = m;
    }

    public Serializable run(Simulator simulator, Executor executor) {
        // TODO Auto-generated method stub
        List<ExperienceSet> sets = new ArrayList<ExperienceSet>(M);
        for (int i = 0; i < M; i++) {
            sets.add(new GeometricBrownianMotion(spotPrice, interestRate, volatilityRate, maturityDate, N));
        }
        ArrayList<Double> simulatedPrice = null;
        try {
            simulatedPrice = simulator.solve(sets);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }

        double payoffCall = 0;
        double payoffPut = 0;
        for (int j = 0; j < N * M; j++) {
            payoffCall += Math.max(simulatedPrice.get(j) - this.strikePrice, 0);
            payoffPut += Math.max(this.strikePrice - simulatedPrice.get(j), 0);
        }

        double call, put;

        call = payoffCall * Math.exp(-this.maturityDate * this.interestRate) / (N * M);
        put = payoffPut * Math.exp(-this.maturityDate * this.interestRate) / (N * M);

        return new double[] { call, put };
    }

    public static void main(String[] args) throws ProActiveException, TaskException {
        URL descriptor = EuropeanOption.class.getResource("WorkersApplication.xml");
        PAMonteCarlo mc = new PAMonteCarlo(descriptor, null, "Workers");

        EuropeanOption option = new EuropeanOption(100.0, 100.0, 0.1, 0.05, 0.2, 1, 1000, 1000);
        double[] price = (double[]) mc.run(option);
        System.out.println("Call: " + price[0] + " Put : " + price[1]);

    }

}
