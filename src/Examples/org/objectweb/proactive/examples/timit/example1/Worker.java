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
package org.objectweb.proactive.examples.timit.example1;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.core.group.spmd.ProSPMD;


/**
 * A simple distributed application that use TimIt.<br>
 * The application have three classes : Launcher, Worker and Root<br>
 * Launcher will deploy some Workers to do a job. Theses Workers will use a Root
 * instance to do it.<br>
 *
 * See the source code of these classes to know how use TimIt.<br>
 *
 * In this example the Worker uses 3 observers :<br> - One for counting the
 * total number of communications<br> - One for building the message density
 * distribution pattern<br> - One for building the data density distribution<br>
 *
 * @see org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultObserver
 * @see org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommObserver
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class Worker extends Timed {

    /**
     *
     */
    private static final long serialVersionUID = 2784624024382450983L;

    /** The number that identifies the worker in a group */
    private int rank;

    /** The size of the current group of workers */
    private int groupSize;

    /** A typed reference to the group */
    private Worker workers;

    /** The array of stubs to all workers of the group */
    private Worker[] workersArray;

    /** An default observer used to perform some basic operations */
    public DefaultEventObserver defaultObserver;

    /** An communication observer for the number of message density distribution */
    public CommEventObserver nbCommObserver;

    /** An communication observer for the data density distribution */
    public CommEventObserver commSizeObserver;

    /** An empty no args constructor, as needed by ProActive */
    public Worker() {
    }

    /**
     * This method is called by the Launcher to start the job.
     *
     * @see org.objectweb.proactive.benchmarks.timit.examples.example2.Launcher
     */
    public void start() {
        this.rank = ProSPMD.getMyRank();
        this.workers = (Worker) ProSPMD.getSPMDGroup();
        this.workersArray = (Worker[]) PAGroup.getGroup(this.workers)
                                              .toArray(new Worker[0]);
        this.groupSize = ProSPMD.getMySPMDGroupSize();

        // The defaultObserver will perform two
        // operations. The first operation is performed between
        // workers and second between notifications ( notifications
        // are of course internal to a worker )
        this.defaultObserver = new DefaultEventObserver("nbComms",
                DefaultEventData.SUM, DefaultEventData.SUM);
        // The nbCommObserver is used to build
        // the message density pattern, a chart will be built from
        // the gathered data.
        this.nbCommObserver = new CommEventObserver("commPattern",
                this.groupSize, this.rank);
        // The commSizeObserver comunication observer is used to build
        // the message density pattern, a chart will be built from
        // the gathered data.
        this.commSizeObserver = new CommEventObserver("densityPattern",
                this.groupSize, this.rank);

        // ************** READY
        this.msg("Ready");

        // Then, you have to specify all counters you want to activate. That
        // means all counters defined in this class, but also counter defined
        // in other class, thanks to the TimerStore instance.
        super.activate(new EventObserver[] {
                this.defaultObserver, this.nbCommObserver, this.commSizeObserver
            });

        try {
            // ************** INITIALIZATION
            this.msg("Initialization");

            // Simulate some communications
            int destRank;

            // In this example each worker will send 10 messages to its
            // neighbour
            for (int i = 0; i < 10; i++) {
                destRank = (this.rank + 1) % this.groupSize;
                // Notification of the nbCommObserver observer
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.nbCommObserver,
                        destRank, 1));
                // Notification of the commSizeObserver observer
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.commSizeObserver,
                        destRank, 2));
                // Notification of the defaultObserver observer
                super.getEventObservable()
                     .notifyObservers(new Event(this.defaultObserver, 1.0));
                // Perform the distant call
                this.workersArray[destRank].toto(i);
            }

            // Next 10 messages wil be sent to the next neighbour of the worker
            for (int i = 0; i < 10; i++) {
                destRank = (this.rank + 2) % this.groupSize;
                // Notification of the nbCommObserver observer
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.nbCommObserver,
                        destRank, 1));
                // Notification of the commSizeObserver observer
                super.getEventObservable()
                     .notifyObservers(new CommEvent(this.commSizeObserver,
                        destRank, 160));
                // Notification of the defaultObserver observer
                super.getEventObservable()
                     .notifyObservers(new Event(this.defaultObserver, 1));
                // Perform the distant call
                this.workersArray[destRank].toto(i);
            }

            Thread.sleep(314);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ************** END
        this.msg("End");

        // Finally, you have to say that timing is done by using finalizeTimed()
        // method. You can specify some textual informations about this worker.
        // This information will be shown in final XML result file.
        // Take care when using it with many nodes... :)
        super.finalizeTimed(this.rank, "Worker" + this.rank + " is OK.");
    }

    /**
     * Will be called distantly.
     *
     * @param x
     *            An arbitrary integer
     */
    public void toto(int x) {
        return;
    }

    /**
     * Called by Launcher to kill this active object
     *
     * @see org.objectweb.proactive.benchmarks.timit.examples.example2.Launcher
     */
    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }

    /** A simple messaging method */
    private void msg(String str) {
        if (this.rank == 0) {
            System.out.println("\t*" + this.rank + "*--------> " + str);
        } else {
            System.out.println("\t " + this.rank + " --------> " + str);
        }
    }
}
