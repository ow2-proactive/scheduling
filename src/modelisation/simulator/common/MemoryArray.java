/*
 * Created on 7 mai 2003
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
public class MemoryArray {
    protected static int DEFAULT_MEMORY_SIZE = 3;

    // protected int memorySize;
    protected int headPosition;
    protected boolean maxCapacityReached;
    protected int internalCounter;
    protected double[] memory;
    protected double[] position;

    public MemoryArray() {
        this(DEFAULT_MEMORY_SIZE);
    }

    public MemoryArray(int memorySize) {
        //    this.memorySize = memorySize;
        this.memory = new double[memorySize];
        this.position = new double[memorySize];
    }

    public void add(double value) {
        //System.err.println("adding " + value);
        // if (!maxCapacityReached) {
        // 	this.memory[headPosition++] = value;        	
        if (this.memory.length == (headPosition + 1)) {
            //we have reached the capacity
            this.maxCapacityReached = true;
        }

        // } else {
        this.memory[headPosition] = value;
        this.position[headPosition] = this.internalCounter++;
        headPosition = (headPosition + 1) % memory.length;
        //System.out.println("Average = " + average + " memory  = " + this);
        //  }
    }

    public double[] getMemory() {
        double[] tmp;
        if (this.maxCapacityReached) {
            tmp = new double[memory.length];
            for (int i = 0; i < memory.length; i++) {
                //tmp.append(memory[(headPosition + i) % memory.length]).append(",");
                tmp[i] = memory[(headPosition + i) % memory.length];
            }
        } else {
            tmp = new double[headPosition];
            for (int i = 0; i < headPosition; i++) {
                //tmp.append(memory[i]).append(",");
                tmp[i] = memory[i];
            }
        }
        return tmp;

        //		if (this.maxCapacityReached) {
        //					for (int i = 0; i < memory.length; i++) {
        //						tmpMemory.append(memory[(headPosition + i) % memory.length])
        //								 .append(" ");
        //						tmpCounter.append(position[(headPosition + i) % memory.length])
        //								  .append(" ");
        //					}
        //				} else {
        //					for (int i = 0; i < headPosition; i++) {
        //						tmpMemory.append(memory[i]).append(" ");
        //						tmpCounter.append(position[i]).append(" ");
        //					}
        //				}
    }

    public double[] getCounter() {
        double[] tmp;
        if (this.maxCapacityReached) {
            tmp = new double[position.length];
            for (int i = 0; i < position.length; i++) {
                //tmp.append(position[(headPosition + i) % position.length]).append(",");
                tmp[i] = position[(headPosition + i) % position.length];
            }
        } else {
            tmp = new double[headPosition + 1];
            for (int i = 0; i < headPosition; i++) {
                //tmp.append(position[i]).append(",");
                tmp[i] = position[i];
            }
        }
        return tmp;
    }

    public double[][] getMemoryAndCounter() {
        double[][] tmp = null;
        tmp = new double[2][];
        tmp[0] = this.getMemory();
        tmp[1] = this.getCounter();
        return tmp;
    }

    public double[] getFirstLast() {
        double[] tmp = new double[2];
        if (this.maxCapacityReached) {
            tmp[0] = memory[headPosition];
            tmp[1] = memory[((headPosition + memory.length) - 1) % memory.length];
        } else {
            tmp[0] = memory[0];
            tmp[1] = memory[headPosition - 1];
        }
        return tmp;
    }

    public double getLast() {
        if (this.maxCapacityReached) {
            return memory[((headPosition + memory.length) - 1) % memory.length];
        } else {
            return memory[headPosition - 1];
        }
    }

    public double getLastCounter() {
        if (this.maxCapacityReached) {
            return position[((headPosition + memory.length) - 1) % memory.length];
        } else {
            return position[headPosition - 1];
        }
    }

    public double calculateRate() {
        double[] memory = this.getMemory();
        double b = 0;
        double sumX = (memory.length * (memory.length + 1)) / 2;
        double sumY = 0;
        double sumX2 = 0;
        double sumXY = 0;

        for (int i = 0; i < memory.length; i++) {
            sumY += memory[i];
        }

        for (int i = 1; i <= memory.length; i++) {
            sumX2 += (i * i);
        }

        for (int i = 0; i < memory.length; i++) {
            sumXY += ((i + 1) * memory[i]);
        }

        double result = ((memory.length * sumXY) - (sumX * sumY)) / ((memory.length * sumX2) -
            Math.pow(sumX, 2));
        if (Double.isNaN(result)) {
            return 0;
        } else {
            return result;
        }
    }

    public String prettyPrint() {
        StringBuffer tmpMemory = new StringBuffer();
        StringBuffer tmpCounter = new StringBuffer();
        if (this.maxCapacityReached) {
            for (int i = 0; i < memory.length; i++) {
                tmpMemory.append(memory[(headPosition + i) % memory.length])
                         .append(" ");
                tmpCounter.append(position[(headPosition + i) % memory.length])
                          .append(" ");
            }
        } else {
            for (int i = 0; i < headPosition; i++) {
                tmpMemory.append(memory[i]).append(" ");
                tmpCounter.append(position[i]).append(" ");
            }
        }
        return tmpMemory.append("\n").append(tmpCounter).append("\n").toString();
    }

    public String toString() {
        double[] mem = this.getMemory();
        StringBuffer tmp = new StringBuffer();

        //        if (this.maxCapacityReached) {
        //            for (int i = 0; i < memory.length; i++) {
        //                tmp.append(memory[(headPosition + i) % memory.length]).append(" ");
        //            }
        //        } else {
        //            for (int i = 0; i < headPosition; i++) {
        //                tmp.append(memory[i]).append(" ");
        //            }
        //        }
        for (int i = 0; i < mem.length; i++) {
            tmp.append(mem[i] + " ");
        }
        return tmp.toString();
    }

    public static void main(String[] args) {
        MemoryArray m = new MemoryArray(5);
        for (int i = 0; i < args.length; i++) {
            m.add(Double.parseDouble(args[i]));
            System.out.println(m);
        }
    }
}
