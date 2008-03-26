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
package active;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


public class DistributedPrimesMW {
    private static final int NUMBER_OF_TASKS = 30;

    public static void main(String[] args) throws TaskAlreadySubmittedException, TaskException {
        //get the number of primes from the command line
    	int NUMBER_OF_PRIMES = Integer.parseInt(args[0]);
    	// creation of the master
        ProActiveMaster<ComputePrime, Long> master =
        		new ProActiveMaster<ComputePrime, Long>();

        // adding resources
        master.addResources(DistributedPrimesMW.class
                .getResource("/org/objectweb/proactive/examples/masterworker/WorkersLocal.xml"));

        // defining tasks
        Vector<ComputePrime> tasks = new Vector<ComputePrime>();
        
        // holds the number of primes found
        long found = 0;
        // number to start with 
        long checkedLimit =  2;
        //iterate until at least NUMBER_OF_PRIMES primes have been found
        while (found < NUMBER_OF_PRIMES){
        	// add a task for each number between checkedLimit and checkedLimit + NUMBER_OF_TASKS
        	for (long i = checkedLimit; i < checkedLimit + NUMBER_OF_TASKS; i++)
	            tasks.add(new ComputePrime(new Long(i)));

        	// start the computation
	        master.solve(tasks);

            // wait for results
            List<Long> primes = master.waitAllResults();

            //display the results and increment the number of primes found
            for (Long prime:primes)
            	if (prime != 0) {
            		System.out.print(prime.toString() + " ");
            		found++;
            	}
          //move the checking start point
          checkedLimit = checkedLimit + NUMBER_OF_TASKS;
          //clear the taks list since we will be reusing it
          tasks.clear();
        }
        //terminate the master and workers
        master.terminate(true);
      //  System.exit(0);
    }

    /**
     * Very simple task which calculates if a n is prime by using 
     * Euclid's sieve.
     * @author The ProActive Team
     */
    public static class ComputePrime implements Task<Long> {

    	// The number to be checked
    	private Long number;
        public ComputePrime(Long number) {
        	this.number = number;
        }
        // very simple euclid's sieve
        public Long run(WorkerMemory memory) throws Exception {
        	
        	Long limit = new Long(Math.round(Math.sqrt(number))+1);
        	Long divisor = new Long(2);
        	boolean prime = true;
        	while ((divisor < limit) && (prime)){
        		if ((number%divisor) == 0) prime = false;
        		divisor++;
        	}
        	//returns the number if it is prime otherwise return 0
        	if (prime) return number;
        	return new Long(0);
        	
        }
    }
}
