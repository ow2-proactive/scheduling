package org.objectweb.proactive.core.util.profiling;

import org.objectweb.proactive.core.util.timer.MicroTimer;


/**
 * @author Fabrice Huet
 *
 */
public class AverageMicroTimer implements Timer {
    //the number of values in tis timer so far
    private int nbrValues;

    //the total time measured by this timer
    private long total;

    //temporary counter used to measure time when pausing/resuming
    private long tmp;
    
    private MicroTimer timer = new MicroTimer();

    public AverageMicroTimer() {
    }

    public void start() {
        tmp = 0;
        timer.start();
    }

    public void resume() {
        timer.start();
    }

    public void pause() {
        timer.stop();
        tmp += timer.getCumulatedTime();
    }

  
    /**
     * stop the timer and use the cumulated time to compute the average
     */
    public void stop() {
    	//System.out.println("AverageMicroTimer.stop()");
        timer.stop();
       	tmp+=timer.getCumulatedTime();
        this.total += tmp;
        this.nbrValues++;
        tmp = 0;
    }

    /**
     * returns the total time measured so far
     */
    public long getCumulatedTime() {
        return total;
    }
    
    public int getNumberOfValues() {
    	return this.nbrValues;
    }
    
    /**
     * return the average time measured so far
     * @return the average time in microseconds
    *                   -1 if  NaN
	 */
    public long getAverage() {
    	return ((nbrValues>0) ? (total/nbrValues) : -1);
    }
    
    public void dump() {
        System.out.println("--------- AverageTimeProfiler.dump() --------");
        System.out.println("Number of measures: " + nbrValues);
        System.out.println("Total time measured: " + total);
        System.out.println("Average time: " +this.getAverage());
        System.out.println("---------------------------------------------------------");
    }
    
}
