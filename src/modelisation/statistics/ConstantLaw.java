/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Mar 14, 2002
 * Time: 10:33:10 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package modelisation.statistics;

public class ConstantLaw implements RandomNumberGenerator {
    protected double parameter;
    protected String variableName;

    public ConstantLaw(String variableName) {
        this.variableName = variableName;
    }

    public double next() {
        return 1 / parameter;
    }

    public void initialize(double parameter) {
        this.parameter = parameter;
    }

    public void initialize(double parameter, long seed) {
        this.initialize(parameter);
    }
}
