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

import java.util.Random;


/**
 * A timer where we compute an exponentially weighted moving average
 * i.e average(n)=average(n-1)*alpha+(1-alpha)*value(n)
 *
 */
public class EWMATimer extends AverageMicroTimer {
    protected double alpha;
    protected double average;

    public EWMATimer(String s, double alpha) {
        super(s);
        this.alpha = alpha;
    }

    @Override
    public void stop() {
        if (running) {
            timer.stop();
            currentElapsed += timer.getCumulatedTime();
            this.total += currentElapsed;
            this.nbrValues++;
            this.average = ((this.average * this.alpha) + ((1 - this.alpha) * currentElapsed));
            currentElapsed = 0;
            running = false;
        }
    }

    @Override
    public double getAverage() {
        return this.average;
    }

    @Override
    public void reset() {
        super.reset();
        this.average = 0;
    }

    public static void main(String[] args) {
        int max = 100;
        Random r = new Random();
        long rValue;
        double[] ewmaMemory = new double[max];
        double[] ewmaMemory2 = new double[max];
        double[] tMemory = new double[max];

        //we use multilple  timers to show the difference between a classical average one and a EWMA one
        EWMATimer ewmat = new EWMATimer("EWMA", 0.9);
        EWMATimer ewmat2 = new EWMATimer("EWMA", 0.5);
        TimerWithMemory tm = new TimerWithMemory("Memory");
        AverageMicroTimer t = new AverageMicroTimer("MicroTimer");
        for (int i = 0; i < 100; i++) {
            ewmat.start();
            t.start();
            ewmat2.start();
            tm.start();
            try {
                Thread.sleep(r.nextInt(200));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ewmat.stop();
            t.stop();
            ewmat2.stop();
            tm.stop();
            ewmaMemory[i] = ewmat.getAverage();
            ewmaMemory2[i] = ewmat2.getAverage();
            tMemory[i] = t.getAverage();
        }
        long[] memory = tm.getMemory();
        System.err.println("Dumping memory ");
        for (int i = 0; i < ewmaMemory.length; i++) {
            System.out.println(tMemory[i] + " " + ewmaMemory[i] + " " + ewmaMemory2[i] + " " + memory[i]);
        }
        System.err
                .println("Command in gnuplot: plot 'file' using 1 title 'Average' with lp, 'file' using 2 title 'EMWA 0.9' with lp, 'file' using 3 title 'EWMA 0.5' with lp, 'file' using 4 title 'Raw' with lp");
    }
}
