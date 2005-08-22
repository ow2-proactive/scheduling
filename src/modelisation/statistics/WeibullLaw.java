package modelisation.statistics;

import java.util.Random;


public class WeibullLaw implements RandomNumberGenerator {
    protected double scale;
    protected double shape;
    protected String variableName;
    protected Random random;

    public WeibullLaw(String variableName) {
        this.variableName = variableName;
        try {
            scale = Double.parseDouble(System.getProperties().getProperty(variableName +
                        ".weibull.scale"));
        } catch (Exception e) {
            System.err.println("WeibullLaw: property " + variableName +
                ".weibull.scale not set");
        }
        try {
            shape = Double.parseDouble(System.getProperties().getProperty(variableName +
                        ".weibull.shape"));
        } catch (Exception e) {
            System.err.println("WeibullLaw: property " + variableName +
                ".weibull.shape not set");
        }
    }

    public void initialize(double parameter) {
        this.initialize(parameter, System.currentTimeMillis());
    }

    public void initialize(double parameter, long seed) {
        random = new Random(seed);
    }

    public double next() {
        double tmp = Math.log(1 / (1 - random.nextDouble()));
        return scale * Math.pow(tmp, 1 / shape);
    }
}
