/*
 * Created on 5 mai 2003
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
public class ExpoAverageWithMemory extends ExpoAverage {

    protected static int DEFAULT_MEMORY_SIZE;
    protected int memorySize;
    protected int headPosition;
    protected boolean maxCapacityReached;

    // protected double[] memory;
    protected MemoryArray memory;

    public ExpoAverageWithMemory(double alpha) {
        this(alpha, DEFAULT_MEMORY_SIZE);
    }

    public ExpoAverageWithMemory(double alpha, int memorySize) {
        super(alpha);
        this.memorySize = memorySize;
        this.memory = new MemoryArray(memorySize);
    }

    public void add(double value) {
        //System.err.println("adding " + value);
        if (this.number == 0) {
            this.average = value;
            this.number++;
        } else {
            this.average = (this.average * this.alpha) +
                ((1 - this.alpha) * value);
            this.number++;
        }
        this.memory.add(average);
    }

    public double[] getMemory() {
        return this.memory.getMemory();
    }

    public double[] getFirstLastValues() {
        return this.memory.getFirstLast();
    }

    public double getLastValue() {
        return this.memory.getLast();
    }

    public double getLastCounter() {
        return this.memory.getLastCounter();
    }

    public String toString() {
        return this.memory.toString();
    }

    public double calculateRate() {
        return this.memory.calculateRate();
    }

    public static void main(String[] arguments) {

        int memorySize = 4;
        ExpoAverageWithMemory expo = new ExpoAverageWithMemory(0.99, memorySize);
        for (int i = 0; i < arguments.length; i++) {
            expo.add(Double.parseDouble(arguments[i]));
            if ((i % (memorySize * 2)) == 0) {
                //	System.out.println("xxx = " + expo);
                System.out.println(expo.calculateRate() + " position " +
                    expo.getLastCounter() + " using " + expo);
            }

            //            System.out.println(expo);
            //            System.out.println("== head  " + expo.headPosition);
            //            System.out.println(">> First  " + expo.getFirstLast()[0]);
            //            System.out.println(">> Last " + expo.getFirstLast()[1]);
        }
    }
}
