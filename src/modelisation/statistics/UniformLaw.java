package modelisation.statistics;

import java.util.Random;


public class UniformLaw implements RandomNumberGenerator {
    protected double parameter;
    protected Random random;
    protected double min;
    protected double max;
    protected double interval;
    protected String variableName;

    public UniformLaw(String variableName) {
        this.variableName = variableName;

        try {
            interval = Double.parseDouble(System.getProperties().getProperty(variableName +
                        ".uniform.interval"));
        } catch (Exception e) {
            System.err.println("UniformLaw: property " + variableName +
                ".uniform.min not set");
        }
    }

    public void initialize(double parameter) {
        this.initialize(parameter, System.currentTimeMillis() + 273123);
    }

    public void initialize(double parameter, long seed) {
        this.parameter = parameter;
        this.random = new Random(seed);
        max = 1 / (parameter - (interval / 2));
        min = 1 / (parameter + (interval / 2));
    }

    public double next() {
        return min + ((max - min) * random.nextDouble());
    }
}
