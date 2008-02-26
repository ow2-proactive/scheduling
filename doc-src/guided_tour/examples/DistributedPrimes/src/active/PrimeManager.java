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

import java.io.Serializable;
import java.util.Vector;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

public class PrimeManager implements Serializable{
	private Vector<PrimeWorker> workers = new Vector<PrimeWorker>();
	public PrimeManager () {} ////empty no-arg constructor needed by ProActive
	//1. send number to all workers and if all of them say the
	//number is prime then it is
	//2. send the number randomly to one worker if prime
	//3. try the next number
	public void startComputation(long maxNumber){
		boolean prime;//true after checking if a number is prime 
		int futureIndex;//updated future index;
		long primeCheck = 2; //start number
		long primeCounter = 1;
		int k=0;
		Vector<BooleanWrapper> answers = new Vector<BooleanWrapper>();
		while (primeCounter < maxNumber){
			//1. send request to all workers
			for (PrimeWorker worker : workers)
				// Non blocking (asynchronous method call)
				// adds the futures to the vector
				answers.add(worker.isPrime(primeCheck)); 	
			//2. wait for all the answers, or an answer that says NO 
			prime = true;
			while (!answers.isEmpty() && prime)  {//repeat until a worker says no or all the workers responded (i.e. vector is emptied)
			    // Will block until a new response is available
				futureIndex=PAFuture.waitForAny(answers); //blocks until a future is updated 
				prime = answers.get(futureIndex).booleanValue(); //check the answer
				answers.remove(futureIndex); //remove the updated  future
			}// end while check for primes
			if (prime) { //print if prime
				sendPrime(primeCheck);	
				System.out.print(primeCheck + ", ");
				primeCounter++;//prime number found 
				//flush print buffer every 20 numbers
				if (k % 20 == 0) System.out.println("\n");
				k++;
			}
			primeCheck++;
		}//end while number loop
	}// end StartComputation
	
	//add a workers to the worker Vector
	public void addWorker(PrimeWorker worker){
		workers.add(worker);
	}
	
	//sends the prime numbers found  to one worker randomly
	public void sendPrime(long number){
		int destination = (int)Math.round(Math.random()* (workers.size()-1));
		workers.get(destination).addPrime(new Long(number));
	}

}
