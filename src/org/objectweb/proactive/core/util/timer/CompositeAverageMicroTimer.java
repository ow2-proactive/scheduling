package org.objectweb.proactive.core.util.timer;

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
    private HashMap timerMap = new HashMap();
    private Timer activeTimer = null;

    private CompositeAverageMicroTimer() {
    }

    public CompositeAverageMicroTimer(String name) {
        this.name = name;
    }

    public void start() {
        this.activeTimer.start();
    }

    public void resume() {
        this.activeTimer.resume();
    }

    public void pause() {
        this.activeTimer.pause();
    }

    public void stop() {
        if (this.activeTimer != null) {
            this.activeTimer.stop();
        }
    }

    public void reset() {
        this.activeTimer.reset();
    }

    public long getCumulatedTime() {
        long time = 0;
        Iterator it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = ((Timer) it.next());
            time += t.getCumulatedTime();
        }
        return time;
    }

    public int getNumberOfValues() {
        int values = 0;
        Iterator it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = ((Timer) it.next());
            values += t.getNumberOfValues();
        }
        return values;
    }

    /**
     * return the average time of all the timers
     */
    public double getAverage() {
        int values = 0;
        long time = 0;
        Iterator it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = ((Timer) it.next());
            values += t.getNumberOfValues();
            time += t.getCumulatedTime();
        }
        return ((values > 0) ? ((double) time / values) : (-1));
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();

        tmp.append("Number of measures: ").append(this.getNumberOfValues());
        tmp.append("\nTotal time measured: ").append(this.getCumulatedTime());
        tmp.append("\nAverage time: ").append(this.getAverage()).append("\n");
        //  Now we deal with the internal timers
        Iterator it = timerMap.values().iterator();
        while (it.hasNext()) {
            Timer t = (Timer) it.next();
            tmp.append("    ").append(t.getName()).append("\n");
            tmp.append("        ").append("Number of measures: ").append(t.getNumberOfValues());
            tmp.append("\n        ").append("Total time measured: ").append(t.getCumulatedTime());
            tmp.append("\n        ").append("Average time: ")
               .append(t.getAverage()).append("\n");
        }

        return tmp.toString();
    }

    public void dump() {
        int ln = name.length();
        StringBuffer tmp = new StringBuffer();
        tmp.append("------- ").append(name).append(" -------\n");
        tmp.append(this.toString());
        for (int i = 0; i <= (ln + 16); i++) {
            tmp.append("-");
        }
        System.out.println(tmp.append("\n").toString());
    }

    public void setTimer(String name) {
        String realName = this.name + "." + name;
        this.activeTimer = (Timer) timerMap.get(realName);
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
