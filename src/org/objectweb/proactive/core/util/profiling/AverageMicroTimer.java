package org.objectweb.proactive.core.util.profiling;

import org.objectweb.proactive.core.util.timer.MicroTimer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * @author Fabrice Huet
 *
 */
public class AverageMicroTimer implements Timer, Serializable {
    protected String name;

    //the number of values in tis timer so far
    private int nbrValues;

    //the total time measured by this timer
    private long total;

    //temporary counter used to measure time when pausing/resuming
    private long tmp;
    transient private MicroTimer timer = new MicroTimer();

    //used to check that start has been pressed prior to a stop
    private boolean running; 
	
    public AverageMicroTimer() {
        this(AverageMicroTimer.class.getName());
    }

    public AverageMicroTimer(String name) {
        this.name = name;
    }

    public void start() {
        tmp = 0;
        running = true;
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
    	if (running) {
    		  timer.stop();
    	        tmp += timer.getCumulatedTime();
    	        this.total += tmp;
//    	        if (tmp >= 0) {
    	            this.nbrValues++;
//    	        }
    	        tmp = 0;
    	        running = false;
    	}
      
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
        return ((nbrValues > 0) ? (total / nbrValues) : (-1));
    }

    public void dump() {
        System.out.println(this);
    }

    public String toString() {
        int ln = name.length();
        StringBuffer tmp = new StringBuffer();
        tmp.append("------- ").append(name).append(" -------\n");
        tmp.append("Number of measures: ").append(this.getNumberOfValues());
        tmp.append("\nTotal time measured: ").append(this.getCumulatedTime());
        tmp.append("\nAverage time: ").append(this.getAverage()).append("\n");
        //  StringBuffer end = new StringBuffer();
        for (int i = 0; i <= (ln + 16); i++) {
            tmp.append("-");
        }
        return tmp.append("\n").toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.stop();
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.timer = new MicroTimer();
    }
}
