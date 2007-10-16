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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.objectweb.proactive.core.util.profiling.PAProfilerEngine;
import org.objectweb.proactive.core.util.profiling.Timer;


/**
 * A CompositeMicroTimer maintains many timer at once. To switch between them, use the setTimer method
 * It is advised that only one time may be used at a time to avoid side effects in the measurements.
 * @author Fabrice Huet
 *
 */
public class CompositeAverageMicroTimer extends AverageMicroTimer
    implements Timer, Serializable {
    private HashMap<String, Timer> timerMap = new HashMap<String, Timer>();
    private Timer activeTimer = null;

    private CompositeAverageMicroTimer() {
    }

    public CompositeAverageMicroTimer(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        this.activeTimer.start();
    }

    @Override
    public void resume() {
        this.activeTimer.resume();
    }

    @Override
    public void pause() {
        this.activeTimer.pause();
    }

    @Override
    public void stop() {
        if (this.activeTimer != null) {
            this.activeTimer.stop();
        }
    }

    @Override
    public void reset() {
        this.activeTimer.reset();
    }

    @Override
    public long getCumulatedTime() {
        long time = 0;
        Iterator<Timer> it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = it.next();
            time += t.getCumulatedTime();
        }
        return time;
    }

    @Override
    public int getNumberOfValues() {
        int values = 0;
        Iterator<Timer> it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = it.next();
            values += t.getNumberOfValues();
        }
        return values;
    }

    /**
     * return the average time of all the timers
     */
    @Override
    public double getAverage() {
        int values = 0;
        long time = 0;
        Iterator<Timer> it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = it.next();
            values += t.getNumberOfValues();
            time += t.getCumulatedTime();
        }
        return ((values > 0) ? ((double) time / values) : (-1));
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();

        tmp.append("Number of measures: ").append(this.getNumberOfValues());
        tmp.append("\nTotal time measured: ").append(this.getCumulatedTime());
        tmp.append("\nAverage time: ").append(this.getAverage()).append("\n");

        //  Now we deal with the internal timers
        Iterator<Timer> it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = it.next();
            tmp.append("    ").append(t.getName()).append("\n");
            tmp.append("        ").append("Number of measures: ")
               .append(t.getNumberOfValues());
            tmp.append("\n        ").append("Total time measured: ")
               .append(t.getCumulatedTime());
            tmp.append("\n        ").append("Average time: ")
               .append(t.getAverage()).append("\n");
        }

        return tmp.toString();
    }

    @Override
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

    public void setTimer(String name) {
        String realName = this.name + "." + name;
        this.activeTimer = timerMap.get(realName);
        if (this.activeTimer == null) {
            //need to create a new timer
            this.activeTimer = new AverageMicroTimer(realName);
            timerMap.put(realName, this.activeTimer);
        }
    }

    public Timer getActiveTimer() {
        return this.activeTimer;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.stop();
        this.dump();
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        System.out.println("CompositeAverageMicroTimer.readObject()");

        PAProfilerEngine.registerTimer(this);
    }

    public static void main(String[] args) {
        CompositeAverageMicroTimer timer = new CompositeAverageMicroTimer(
                "Test");
        PAProfilerEngine.registerTimer(timer);
        System.out.println("Using sub-timer 1");
        timer.setTimer("1");
        Random rand = new Random();
        try {
            for (int i = 0; i < 2; i++) {
                timer.start();
                Thread.sleep(400 + rand.nextInt(1000));
                timer.stop();
            }
            System.out.println("Using sub-timer 2");
            timer.setTimer("2");
            for (int i = 0; i < 2; i++) {
                timer.start();
                Thread.sleep(400 + rand.nextInt(1000));
                timer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
