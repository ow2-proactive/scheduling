package modelisation.statistics;

public interface RandomNumberGenerator {
    public void initialize(double parameter);

    public void initialize(double parameter, long seed);

    public double next();
}
