/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.calcium.examples.findprimes;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.calcium.Calcium;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.calcium.Stream;
import org.objectweb.proactive.calcium.examples.nqueens.Board;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.futures.Future;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.proactive.ProActiveThreadedManager;
import org.objectweb.proactive.calcium.skeletons.DaC;
import org.objectweb.proactive.calcium.skeletons.Seq;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;

public class FindPrimes implements Serializable{

	public Skeleton<Challenge,Primes> root;

	
	public static void main(String[] args) throws InterruptedException, PanicException {
		
		FindPrimes st = new FindPrimes();
		st.solve();
	}
	
	public FindPrimes(){
		
		root= new DaC<Challenge, Primes>( new ChallengeDivide(),
				new ChallengeDivideCondition(), 
				new Seq<Challenge, Primes>(new SolveChallenge()),
				new ConquerChallenge());
	}
	
	public void solve() throws InterruptedException, PanicException{

		String descriptor=
				FindPrimes.class.getResource("LocalDescriptor.xml")
				.getPath();
		
		ResourceManager manager= 
			//new MonoThreadedManager();
			//new MultiThreadedManager(5);
			new ProActiveThreadedManager(descriptor, "local");
		    //new ProActiveManager(descriptor, "local");
		
		Calcium calcium = new Calcium(manager);
		
		Stream<Challenge,Primes> stream = calcium.newStream(root);
		
		Vector<Future<Primes>> futures = new Vector<Future<Primes>>(3);
		futures.add(stream.input(new Challenge(1,6400,300)));
		futures.add(stream.input(new Challenge(1,100,20)));
		futures.add(stream.input(new Challenge(1,640,64)));
		
		calcium.boot();
		
		try {
			for(Future<Primes> future:futures){
				Primes res=future.get();		
				for(Integer i: res.primes){
					System.out.print(i+" ");
				}
				System.out.println();
				System.out.println(future.getStats());
			}
		} catch (MuscleException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		StatsGlobal stats = calcium.getStatsGlobal();
		System.out.println(stats);
		calcium.shutdown();
	}
}