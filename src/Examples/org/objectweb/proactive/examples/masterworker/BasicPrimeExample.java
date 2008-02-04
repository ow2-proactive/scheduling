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
package org.objectweb.proactive.examples.masterworker;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.cli.HelpFormatter;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;


/**
 * This simple test class is an example on how to use the Master/Worker API
 * The tasks wait for a period between 15 and 20 seconds (by ex)
 * The main program displays statistics about the speedup due to parallelization
 * @author fviale
 *
 */
public class BasicPrimeExample extends AbstractExample {
    private static final long DEFAULT_PRIME_NUMBER = 1397812341;
    private static final int DEFAULT_NUMBER_OF_INTERVALS = 15;
    public static int number_of_intervals;
    public static long prime_to_find;
    public static ProActiveMaster<FindPrimeTask, Boolean> master;

    /**
     * Displays result of this test
     * @param results results of the test
     * @param startTime starting time of the test
     * @param endTime ending time of the test
     * @param nbWorkers number of workers used during the test
     */
    public static void displayResult(Collection<Boolean> results, long startTime, long endTime, int nbWorkers) {
        // Post processing, calculates the statistics
        boolean prime = true;

        for (Boolean result : results) {
            prime = prime && result;
        }

        long effective_time = (int) (endTime - startTime);
        //      Displaying the result
        System.out.println("" + prime_to_find + (prime ? " is prime." : " is not prime."));

        System.out.println("Calculation time (ms): " + effective_time);
    }

    /**
     * Creates the prime computation tasks to be solved
     * @return
     */
    public static List<FindPrimeTask> createTasks() {
        List<FindPrimeTask> tasks = new ArrayList<FindPrimeTask>();

        // We don't need to check numbers greater than the square-root of the candidate in this algorithm
        long square_root_of_candidate = (long) Math.ceil(Math.sqrt(prime_to_find));
        // 
        tasks.add(new FindPrimeTask(prime_to_find, 2, square_root_of_candidate / number_of_intervals));
        for (int i = 1; i < (number_of_intervals - 1); i++) {
            tasks.add(new FindPrimeTask(prime_to_find,
                ((square_root_of_candidate / number_of_intervals) * i) + 1,
                (square_root_of_candidate / number_of_intervals) * (i + 1)));
        }
        tasks.add(new FindPrimeTask(prime_to_find, (square_root_of_candidate / number_of_intervals) *
            (number_of_intervals - 1), square_root_of_candidate));
        return tasks;
    }

    /**
     * @param args
     * @throws TaskException
     * @throws MalformedURLException
     * @throws TaskAlreadySubmittedException
     * @throws ProActiveException 
     * @throws SchedulerException 
     * @throws LoginException 
     */
    public static void main(String[] args) throws TaskException, MalformedURLException,
            TaskAlreadySubmittedException, ProActiveException, SchedulerException, LoginException {
        //   Getting command line parameters and creating the master (see AbstractExample)
        init(args);

        // Creating the Master
        if (master_vn_name == null) {
            master = new ProActiveMaster<FindPrimeTask, Boolean>();
        } else {
            master = new ProActiveMaster<FindPrimeTask, Boolean>(descriptor_url, master_vn_name);
        }

        registerShutdownHook(new Runnable() {
            public void run() {
                master.terminate(true);
            }
        });

        // Adding ressources
        if (schedulerURL != null) {
            master.addResources(schedulerURL, login, password);
        } else if (vn_name == null) {
            master.addResources(descriptor_url);
        } else {
            master.addResources(descriptor_url, vn_name);
        }

        System.out.println("Primality test launched for n=" + prime_to_find + " with " + number_of_intervals +
            " intervals, using descriptor " + descriptor_url);

        long startTime = System.currentTimeMillis();
        // Creating and Submitting the tasks
        master.solve(createTasks());

        // Collecting the results
        List<Boolean> results = master.waitAllResults();
        long endTime = System.currentTimeMillis();

        // Displaying results, the slavepoolSize method displays the number of workers used by the master
        displayResult(results, startTime, endTime, master.workerpoolSize());

        System.exit(0);
    }

    protected static void init(String[] args) throws MalformedURLException {
        command_options.addOption("p", true, "number to check for primality");
        command_options.addOption("i", true, "number of dividing intervals");

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BasicPrimeExample", command_options);

        AbstractExample.init(args);

        String primeString = cmd.getOptionValue("p");
        if (primeString == null) {
            prime_to_find = DEFAULT_PRIME_NUMBER;
        } else {
            prime_to_find = Long.parseLong(primeString);
        }

        String intervalString = cmd.getOptionValue("i");
        if (intervalString == null) {
            number_of_intervals = DEFAULT_NUMBER_OF_INTERVALS;
        } else {
            number_of_intervals = Integer.parseInt(intervalString);
        }
    }

    /**
     * Task to find if any number in a specified interval divides the given candidate
     * @author fviale
     *
     */
    public static class FindPrimeTask implements Task<Boolean> {

        /**
         *
         */
        private long begin;
        private long end;
        private long candidate;

        public FindPrimeTask(long candidate, long begin, long end) {
            this.begin = begin;
            this.end = end;
            this.candidate = candidate;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task#run(org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory)
         */
        public Boolean run(WorkerMemory memory) {
            for (long divider = begin; divider < end; divider++) {
                if ((candidate % divider) == 0) {
                    return new Boolean(false);
                }
            }
            return new Boolean(true);
        }
    }
}
