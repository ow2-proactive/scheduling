package org.objectweb.proactive.core.util.timer;

import java.io.Serializable;

import org.objectweb.proactive.core.util.profiling.Timer;



/**
 * A timer which retains the values measured in addition to calculating the average
 *
 */
public class TimerWithMemory extends AverageMicroTimer implements Timer,
    Serializable {
    protected long[] memory;
    protected int position;

    protected TimerWithMemory() {
        this.memory = new long[10];
        this.position = 0;
    }

    protected TimerWithMemory(String name) {
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
    	if (this.position >0) {
        long[] tmp = new long[this.position];
        System.arraycopy(memory, 0, tmp, 0,this.position); 
    	return tmp;
    	} else {
    		return null;
    	}
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
        for (int i = 0; i < 200; i++) {
            mt.addToMemory(i);
        }
        mt.dump();
        System.out.println("Requesting memory and displaying it");
        long[] m = mt.getMemory();
for (int i = 0; i < m.length; i++) {
	System.out.print(m[i] +" ");
}
System.out.println();
	}
}
