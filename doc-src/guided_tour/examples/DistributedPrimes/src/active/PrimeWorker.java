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
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

public class PrimeWorker implements Serializable{
	private Vector<Long> primes = new Vector<Long>();
	public PrimeWorker(){}//empty no-arg constructor needed by ProActive
	//check for primes
	public BooleanWrapper isPrime(long number){
		if (primes.isEmpty()) //if no number has been received yet
			return new BooleanWrapper(true);
		else {
			int i=0;
			int size = primes.size(); //store the size of the vector so we don't call the size() method repeatedly
			long value = number; //store the value so we don't call the longValue() repeatedly
			while ((i < size) &&  //has not reached the end of the Vector
					(value % primes.get(i).longValue() != 0)) //does not divide
				i++;
			//if it goes through all the Vector then it is prime
			if (i == size) return  new BooleanWrapper(true);
				else return new BooleanWrapper(false);
		}
	}
	//add a prime to the Vector 
	public void addPrime(Long number){
		primes.add(number);
	}
}
