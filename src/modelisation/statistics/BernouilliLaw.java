package modelisation.statistics;

import java.util.Random;


public class BernouilliLaw implements RandomNumberGenerator {
    //extends GenericDistribution {
    protected double p;
    protected Random random;
    protected double value1;
    protected double value2;

    public BernouilliLaw() {
    }

    public BernouilliLaw(String variableName) {
        p = Double.parseDouble(System.getProperties().getProperty(variableName +
                    ".bernouilli.p"));
        value1 = Double.parseDouble(System.getProperties().getProperty(variableName +
                    ".bernouilli.value1"));
        value2 = Double.parseDouble(System.getProperties().getProperty(variableName +
                    ".bernouilli.value2"));
    }

    public BernouilliLaw(double p) throws IllegalArgumentException {
        random = new Random();
        if (p > 0) {
            this.p = p;
        } else {
            throw new IllegalArgumentException("p <= 0");
        }
    }

    //    public static double moyenne(double[] values) {
    //        double total = 0;
    //        for (int i = 0; i < values.length; i++) {
    //            total += values[i];
    //        }
    //        return total / values.length;
    //    }
    //  
    public void initialize(double parameter) {
        this.initialize(parameter, System.currentTimeMillis());
    }

    public void initialize(double parameter, long seed) {
        random = new Random(seed);
    }

    public double next() {
        double value = random.nextDouble();
        //System.out.println("Value = " + value);
        if (value < p) {
            return 1.0/value1;
        } else {
            return 1.0/value2;
        }

        //return 0;
    }

    public static void main(String[] args) {
        //        if (args.length < 2) {
        //            System.err.println(
        //                "Usage: java statistics.BernouilliLaw <p> <number>");
        //            System.exit(-1);
        //        }
        //        BernouilliLaw expo = new BernouilliLaw(Double.parseDouble(args[0]));
        //        double total = 0;
        //        double totalSquare = 0;
        //        int max = Integer.parseInt(args[1]);
        //        double moyenne;
        //        double moyenne2;
        //        double[] values = new double[max];
        //        double[] values2 = new double[max];
        //
        //        for (int i = 0; i < max; i++) {
        //            values[i] = expo.rand();
        //        }
        //
        //        moyenne = BernouilliLaw.moyenne(values);
        //        System.out.println(" Moyenne = " + moyenne);
        //
        //        for (int i = 0; i < max; i++) {
        //            values2[i] = Math.pow(values[i] - moyenne, 2);
        //        }
        //
        //        moyenne2 = BernouilliLaw.moyenne(values2);
        //        System.out.println(" Moyenne2Square = " + moyenne2);
        //        System.out.println("Sqrt(moyenne2) = " + Math.sqrt(moyenne2));
        //        //System.out.println(" MoyenneSquare = " + Math.pow(BernouilliLaw.moyenne(values2),2) );
        //        //	System.out.println(" Variance = " + (totalSquare/max - Math.pow(total/max,2) ) );
        //    }
    }
}
