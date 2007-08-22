package org.objectweb.proactive.examples.masterslave;

import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extra.masterslave.ProActiveMaster;
import org.objectweb.proactive.extra.masterslave.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


public class PIExample {
    public static final long NUMBER_OF_EXPERIENCES = 1000000;
    public static final int NUMBER_OF_TASKS = 30;

    public static void main(String[] args)
        throws TaskAlreadySubmittedException, TaskException {
        // creation of the master
        ProActiveMaster<ComputePIMonteCarlo, Long> master = new ProActiveMaster<ComputePIMonteCarlo, Long>();

        // adding resources
        master.addResources(PIExample.class.getResource(
                "/org/objectweb/proactive/examples/masterslave/WorkersLocal.xml"));

        // defining tasks
        Vector<ComputePIMonteCarlo> tasks = new Vector<ComputePIMonteCarlo>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            tasks.add(new ComputePIMonteCarlo());
        }

        // adding tasks to the queue
        master.solve(tasks);

        // waiting for results
        List<Long> successesList = master.waitAllResults();

        // computing PI using the results
        long sumSuccesses = 0;

        for (long successes : successesList) {
            sumSuccesses += successes;
        }

        double pi = (4 * sumSuccesses) / ((double) NUMBER_OF_EXPERIENCES * NUMBER_OF_TASKS);

        System.out.println("Computed PI by Monte-Carlo method : " + pi);

        master.terminate(true);
    }

    /**
     * Task which creates randomly a set of points belonging to the [0, 1[x[0, 1[ interval<br>
     * and tests how many points are inside the uniter circle.
     * @author fviale
     *
     */
    public static class ComputePIMonteCarlo implements Task<Long> {
        public ComputePIMonteCarlo() {
        }

        public Long run(SlaveMemory memory) throws Exception {
            long remaining = NUMBER_OF_EXPERIENCES;
            long successes = 0;
            while (remaining > 0) {
                remaining--;
                if (experience()) {
                    successes++;
                }
            }
            return successes;
        }

        public boolean experience() {
            double x = Math.random();
            double y = Math.random();
            return Math.hypot(x, y) < 1;
        }
    }
}
