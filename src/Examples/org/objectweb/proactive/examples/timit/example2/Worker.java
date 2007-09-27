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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.timit.example2;

import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.benchmarks.timit.util.TimItStore;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.core.group.spmd.ProSPMD;


/**
 * A simple distributed application that use TimIt.<br>
 * The application have three classes : Launcher, Worker and Root<br>
 * Launcher will deploy some Workers to do a job. Theses Workers will use a Root
 * instance to do it.
 *
 * See the source code of these classes to know how use TimIt
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */

// Launcher must implements Startable and Workers must extends Timed
public class Worker extends Timed {

    /**
     *
     */
    private static final long serialVersionUID = 1190090440152932457L;
    private int rank;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_TOTAL;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_INIT;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_WORK;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_END;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_SET;

    // Here, you have to declare all timer counters you want to use in this class :
    public TimerCounter T_ADD;

    // You can also declare some counters of events :
    public EventObserver E_COMM;

    // You can also declare some counters of events :
    public EventObserver E_RAND1;

    // You can also declare some counters of events :
    public EventObserver E_RAND2;
    public CommEventObserver E_PATTERN;
    public CommEventObserver E_SIZE;
    private int groupSize;
    private Worker workers;
    private Worker[] workersArray;

    // An empty no args constructor, as needed by ProActive
    public Worker() {
    }

    // The entry point for all workers
    public void start() {
        this.rank = ProSPMD.getMyRank();
        this.workers = (Worker) ProSPMD.getSPMDGroup();
        this.workersArray = (Worker[]) ProGroup.getGroup(this.workers)
                                               .toArray(new Worker[0]);
        this.groupSize = ProSPMD.getMySPMDGroupSize();

        this.msg("Ready");

        // First you have to get an instance of TimerStore for this
        // active object.
        // As in ExampleRoot, you can create counters with this TimerStore
        // for distribute theses counters to every class in this active
        // object.
        // IMPORTANT : TimerStore instance is note integrated in the active
        // object so problems may occur with Fault Tolerance and migration.
        // If you have to do that, do not use TimerStore and pass Counters
        // by another way.
        TimItStore ts = TimItStore.getInstance(this);
        // Add timer counters to the TimItStore
        this.T_TOTAL = ts.addTimerCounter(new TimerCounter("total"));
        this.T_INIT = ts.addTimerCounter(new TimerCounter("init"));
        this.T_WORK = ts.addTimerCounter(new TimerCounter("work"));
        this.T_END = ts.addTimerCounter(new TimerCounter("end"));
        this.T_ADD = ts.addTimerCounter(new TimerCounter("add"));
        this.T_SET = ts.addTimerCounter(new TimerCounter("set"));

        // Add event observers to the TimItStore
        this.E_RAND1 = ts.addEventObserver(new DefaultEventObserver("rand1"));
        this.E_RAND2 = ts.addEventObserver(new DefaultEventObserver("rand2"));
        this.E_COMM = ts.addEventObserver(new DefaultEventObserver("nbComms"));
        this.E_PATTERN = (CommEventObserver) ts.addEventObserver(new CommEventObserver(
                    "commPattern", this.groupSize, this.rank));
        this.E_SIZE = (CommEventObserver) ts.addEventObserver(new CommEventObserver(
                    "densityPattern", this.groupSize, this.rank));

        Root root = new Root();

        // Now you can use start() and stop() methods on each counter.
        // Take care about starting and stopping counter in the right order :
        // do not start() two times the same counter or stop a counter without
        // starting it.
        // IMPORTANT: You can imbricate up to 3 start()/stop() calls
        try {
            // ************** INITIALIZATION
            this.msg("Initialization");
            ts.activation(); //important : after new Root()
            this.T_TOTAL.start();

            this.T_INIT.start();
            root.foo();
            this.T_INIT.stop();

            // Simulate some communications
            int destRank;

            for (int i = 0; i < 10; i++) {
                destRank = (this.rank + 1) % this.groupSize;
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.E_PATTERN, destRank, 1));
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.E_SIZE, destRank, 2));
                super.getEventObservable()
                     .notifyObservers(new Event(this.E_COMM, 1.0));
                this.workersArray[destRank].toto(i);
            }

            for (int i = 0; i < 13; i++) {
                if (this.rank == 0) {
                    destRank = 3;
                } else {
                    destRank = this.rank - 1;
                }
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.E_PATTERN, destRank, 1));
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.E_SIZE, destRank, 160));
                super.getEventObservable()
                     .notifyObservers(new Event(this.E_COMM, 1));
                this.workersArray[destRank].toto(i);
            }
            super.getEventObservable()
                 .notifyObservers(new Event(this.E_RAND1, Math.random()));
            super.getEventObservable()
                 .notifyObservers(new Event(this.E_RAND2, Math.random()));

            // ************** WORKING
            this.msg("Working");
            this.T_WORK.start();
            Thread.sleep(314);

            root.foo();
            Thread.sleep(314);
            root.foo();

            // This example show the cost of System.currentTimeMillis().
            // If T_BAR counter is activated, it will make a currentTimeMillis()
            // on each call.
            // Take care about it !
            for (int i = 0; i < 9; i++) {
                root.bar();
                T_ADD.addValue(67);
            }
            T_SET.setValue(123456);
            this.T_WORK.stop();

            // ************** CLEANUP
            this.msg("Cleanup");
            this.T_END.start();
            Thread.sleep(314);
            root.foo();
            this.T_END.stop();

            this.T_TOTAL.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ************** END
        this.msg("End");

        // Finally, you have to say that timing is done by using finalizeTimer()
        // method. You can specify some textual informations about this worker.
        // This information will be shown in final XML result file.
        // Take care when using it with many nodes... :)
        super.finalizeTimed(this.rank, "Worker" + this.rank + " is OK.");
    }

    private void toto(int x) {
        return;
    }

    // A simple messaging method
    private void msg(String str) {
        if (this.rank == 0) {
            System.out.println("\t*" + rank + "*--------> " + str);
        } else {
            //             System.out.println( "\t " + rank + " --------> " + str );
        }
    }
}
