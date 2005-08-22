/*
 * Created on 28 avr. 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package modelisation.simulator.common;


/**
 * @author fhuet
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExpoAverage {
    protected int number;
    protected double alpha;
    protected double average;

    public ExpoAverage(double alpha) {
        this.alpha = alpha;
        this.average = 0;
        this.number = 0;
    }

    public void add(double value) {
        if (this.number == 0) {
            this.average = value;
            this.number++;
        } else {
            this.average = (this.average * this.alpha) +
                ((1 - this.alpha) * value);
            this.number++;
        }
    }

    public double getAlpha() {
        return alpha;
    }

    public double getAverage() {
        return average;
    }

    public int getNumber() {
        return number;
    }
}
