/**
 * This class was originally written By
 * Cesar Jalpa Villanueva as part of the Wagon
 * software
 * See http://www-sop.inria.fr/mistral
 */
package modelisation.statistics;

import java.io.Serializable;
import java.util.Random;

public class ExponentialLaw implements RandomNumberGenerator, Serializable {

    //extends GenericDistribution {

    private double parameter;
    private Random random;


    public ExponentialLaw() {

    }


    public ExponentialLaw(double parameter) throws IllegalArgumentException {
        random = new Random();
        if (parameter > 0) {
            this.parameter = parameter;
        } else {
            throw new IllegalArgumentException("parameter <= 0");
        }
    }

    public ExponentialLaw(double parameter, long seed) throws IllegalArgumentException {
        random = new Random(seed);
        if (parameter > 0) {
            this.parameter = parameter;
        } else {
            throw new IllegalArgumentException("parameter <= 0");
        }
    }

    public void initialize(double parameter) {

        this.initialize(parameter, System.currentTimeMillis());
    }


    public void initialize(double parameter, long seed) {
                  System.out.println("ExponentialLaw.initialize: " + parameter);
        random = new Random(seed);
        this.parameter = parameter;
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


    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: java statistics.ExponentialLaw <parameter> <number>");
            System.exit(-1);
        }
        ExponentialLaw expo = new ExponentialLaw(Double.parseDouble(args[0]));
        double total = 0;
        double totalSquare = 0;
        int max = Integer.parseInt(args[1]);
        double moyenne;
        double moyenne2;
        double values[] = new double[max];
        double values2[] = new double[max];

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
        //	System.out.println(" Variance = " + (totalSquare/max - Math.pow(total/max,2) ) );

    }
}
