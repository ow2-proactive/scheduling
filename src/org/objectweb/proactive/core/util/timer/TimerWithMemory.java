package org.objectweb.proactive.core.util.timer;

import org.objectweb.proactive.core.util.profiling.Timer;

import java.io.Serializable;


/**
 * A timer which retains the values measured in addition to calculating the average
 *
 */
public class TimerWithMemory extends AverageMicroTimer implements Timer,
    Serializable {
    protected long[] memory;
    protected int position;

    public TimerWithMemory() {
        this.memory = new long[10];
        this.position = 0;
    }

    public TimerWithMemory(String name) {
        this();
        this.name = name;
    }

    public void stop() {
        if (running) {
            timer.stop();
            currentElapsed += timer.getCumulatedTime();
            this.total += currentElapsed;
            this.nbrValues++;
            this.addToMemory(currentElapsed);
            currentElapsed = 0;
            running = false;
        }
    }

    /**
     * Called only to perform some testing
     * @param time
     */
    protected void addToMemoryTest(long time) {
        this.total += time;
        this.nbrValues++;
    	this.addToMemory(time);
    }
    
    protected void addToMemory(long time) {
        if (this.position == this.memory.length) {
            //not enough space, need to increase the array
            long[] tmp = new long[2 * this.memory.length];
            System.arraycopy(memory, 0, tmp, 0, memory.length);
            this.memory = tmp;
        }
        this.memory[position] = time;
        this.position++;
    }

    /**
     * return a copy of the memory of this timer
     * @return a copy of the memory
     */
    public long[] getMemory() {
        if (this.position > 0) {
            long[] tmp = new long[this.position];
            System.arraycopy(memory, 0, tmp, 0, this.position);
            return tmp;
        } else {
            return null;
        }
    }

    public double getVariance() {
        double average = this.getAverage();
       double total2 =0;
        for (int i = 0; i < this.position; i++) {
            total2 += Math.pow(memory[i], 2);
        }
        return ((total2 / this.position)-Math.pow(average,2));
    }

    public double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
    }

    public void dump() {
        System.out.println("Dumping memory");
        for (int i = 0; i < this.position; i++) {
            System.out.print(memory[i] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        TimerWithMemory mt = new TimerWithMemory("Test");
        for (int i = 0; i < 10; i++) {
            mt.addToMemoryTest(i);
        }
        mt.dump();
        System.out.println("Requesting memory and displaying it");
        long[] m = mt.getMemory();
        for (int i = 0; i < m.length; i++) {
            System.out.print(m[i] + " ");
        }
        System.out.println();
        System.out.println("Average is " + mt.getAverage());
        System.out.println("Variance is " + mt.getVariance());
        
        System.out.println("Standard deviation is " + mt.getStandardDeviation());
    }
}
