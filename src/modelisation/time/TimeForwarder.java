package modelisation.time;

/**
 * @author fhuet
 *
 * 
 *
 */
public class TimeForwarder {
    protected double gamma;
    protected double delta;
    protected double lambda;
    protected double nu;

    public TimeForwarder() {
    }

    public TimeForwarder(double lambda, double nu, double delta, double gamma) {
        this.lambda = lambda;
        this.nu = nu;
        this.delta = delta;
        this.gamma = gamma;
    }

    public double evaluate() {
        double N1 = (lambda + delta) * (lambda + nu) - gamma * nu;
        double N2 = (lambda + delta) * (lambda + nu) - 
                    gamma * (nu + lambda + delta);
        double N3 = (lambda + delta) * (lambda + nu + delta);
        double N4 = delta * N2;
        double N5 = (lambda + delta) * (lambda + nu) * (lambda * (lambda + nu) + 
                    nu * (gamma + nu)) + delta * gamma * nu * lambda;
        double N6 = lambda * (lambda + nu) * (gamma + nu) * N2;
        double ell = (-(nu + delta - gamma) + 
                     Math.sqrt(Math.pow((nu + delta + gamma), 2) - 
                                   4 * nu * delta)) / 2;
        double TP0 = (ell + nu + delta) / (delta * ell) + 1 / gamma;
        double T = N1 / N2 * TP0 - N3 / N4 - N5 / N6;
        return T * 1000;
    }

    /**
     * Returns the gamma.
     * @return double
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Sets the gamma.
     * @param gamma The gamma to set
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     * Returns the delta.
     * @return double
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Sets the delta.
     * @param delta The delta to set
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    /**
     * Returns the lambda.
     * @return double
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Returns the nu.
     * @return double
     */
    public double getNu() {
        return nu;
    }

    /**
     * Sets the lambda.
     * @param lambda The lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Sets the nu.
     * @param nu The nu to set
     */
    public void setNu(double nu) {
        this.nu = nu;
    }

    public static void main(String[] args) {
        TimeForwarder tf = new TimeForwarder(Double.parseDouble(args[0]), 
                                             Double.parseDouble(args[1]), 
                                             Double.parseDouble(args[2]), 
                                             Double.parseDouble(args[3]));
        System.out.println(tf.evaluate());
    }
}