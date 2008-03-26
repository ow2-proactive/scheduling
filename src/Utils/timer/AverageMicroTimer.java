/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package timer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.objectweb.proactive.core.util.profiling.Timer;


/**
 * @author The ProActive Team
 *
 */
public class AverageMicroTimer implements Timer, Serializable {
    protected String name;

    //the number of values in tis timer so far
    protected int nbrValues;

    //the total time measured by this timer
    protected long total;

    //temporary counter used to measure time when pausing/resuming
    protected long currentElapsed;
    transient protected MicroTimer timer = new MicroTimer();

    //used to check that start has been pressed prior to a stop
    protected boolean running;

    public AverageMicroTimer() {
        this(AverageMicroTimer.class.getName());
    }

    public AverageMicroTimer(String name) {
        this.name = name;
    }

    public void start() {
        currentElapsed = 0;
        running = true;
        timer.start();
    }

    public void resume() {
        timer.start();
    }

    public void pause() {
        timer.stop();
        currentElapsed += timer.getCumulatedTime();
    }

    /**
     * stop the timer and use the cumulated time to compute the average
     */
    public void stop() {
        //System.out.println("AverageMicroTimer.stop()");
        if (running) {
            timer.stop();
            currentElapsed += timer.getCumulatedTime();
            this.total += currentElapsed;
            //    	        if (tmp >= 0) {
            this.nbrValues++;
            //    	        }
            currentElapsed = 0;
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
    public double getAverage() {
        return ((nbrValues > 0) ? ((double) total / nbrValues) : (-1));
    }

    public void dump() {
        int ln = name.length();
        StringBuilder tmp = new StringBuilder();
        tmp.append("------- ").append(name).append(" -------\n");
        tmp.append(this.toString());
        for (int i = 0; i <= (ln + 16); i++) {
            tmp.append("-");
        }
        System.out.println(tmp.append("\n").toString());
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append("Number of measures: ").append(this.getNumberOfValues());
        tmp.append("\nTotal time measured: ").append(this.getCumulatedTime());
        tmp.append("\nAverage time: ").append(this.getAverage()).append("\n");
        return tmp.toString();
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.timer = new MicroTimer();
    }

    public void reset() {
        this.currentElapsed = 0;
        this.nbrValues = 0;
        this.total = 0;
    }
}
