/**
 * This class was originally written By
 * Cesar Jalpa Villanueva as part of the Wagon
 * software
 * See http://www-sop.inria.fr/mistral
 */
package modelisation.statistics;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;

import modelisation.edu.cornell.lassp.houle.RngPack.RandomElement;
import modelisation.edu.cornell.lassp.houle.RngPack.Ranlux;


public class ExponentialLaw implements RandomNumberGenerator, Serializable {
    //extends GenericDistribution {
    private double parameter;
    private Random random;

    //    private SecureRandom random;
    protected RandomElement re;

    public ExponentialLaw() {
    }

    public ExponentialLaw(double parameter) throws IllegalArgumentException {
        //        random = this.createRandomGenerator();
        //        if (parameter > 0) {
        //            this.parameter = parameter;
        //        } else {
        //            throw new IllegalArgumentException("parameter <= 0");
        //        }
    }

    public ExponentialLaw(double parameter, long seed)
        throws IllegalArgumentException {
        //   random = this.createRandomGenerator(seed);
        // random = new Random(seed);
    }

    public Random createRandomGenerator() {
        return this.createRandomGenerator(System.currentTimeMillis());
        //  return _createRandomGenerator();
        // return this._createRandomGenerator();
        //return this._createRanluxGenerator();
    }

    public Random createRandomGenerator(long seed) {
        // Random tmpRandom = new Random(seed);
        Random tmpRandom = this._createRandomGenerator(seed);

        // Random tmpRandom = this._createRanluxGenerator(seed);
        //  this.random.setSeed(seed);
        return tmpRandom;
    }

    public void initialize(double parameter) {
        this.initialize(parameter, System.currentTimeMillis());
    }

    public void initialize(double parameter, long seed) {
        this.parameter = parameter;
        this.random = this.createRandomGenerator(seed);
        //  this.random = this.createRandomGenerator();
    }

    public double next() {
        return ((-1.0d / parameter) * Math.log(random.nextDouble()));
    }

    public static double moyenne(double[] values) {
        double total = 0;
        for (int i = 0; i < values.length; i++) {
            total += values[i];
        }
        return total / values.length;
    }

    protected Random _createSecureRandomGenerator() {
        SecureRandom tmpRandom = null;
        try {
            tmpRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");

            byte[] tmp = tmpRandom.generateSeed(10);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return tmpRandom;
    }

    protected Random _createRandomGenerator() {
        return new Random();
    }

    protected Random _createRandomGenerator(long l) {
        return new Random(l);
    }

    protected Random _createRanluxGenerator() {
        return this._createRanluxGenerator(System.currentTimeMillis());
        //  return new Ranlux();
    }

    private Random _createRanluxGenerator(long l) {
        return new Ranlux(l);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                "Usage: java statistics.ExponentialLaw <parameter> <number>");
            System.exit(-1);
        }

        ExponentialLaw expo = new ExponentialLaw(Double.parseDouble(args[0]));
        expo.initialize(Double.parseDouble(args[0]));
        double total = 0;
        double totalSquare = 0;
        int max = Integer.parseInt(args[1]);
        double moyenne;
        double moyenne2;
        double[] values = new double[max];
        double[] values2 = new double[max];
        for (int i = 0; i < max; i++) {
            values[i] = expo.next();
        }
        moyenne = ExponentialLaw.moyenne(values);
        System.out.println(" Moyenne = " + moyenne);
        for (int i = 0; i < max; i++) {
            values2[i] = Math.pow(values[i] - moyenne, 2);
        }
        moyenne2 = ExponentialLaw.moyenne(values2);
        System.out.println(" Moyenne2Square = " + moyenne2);
        System.out.println("Sqrt(moyenne2) = " + Math.sqrt(moyenne2));
        //System.out.println(" MoyenneSquare = " + Math.pow(ExponentialLaw.moyenne(values2),2) );
        //   System.out.println(" Variance = " + (totalSquare/max - Math.pow(total/max,2) ) );
    }
}
