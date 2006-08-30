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

import org.objectweb.proactive.calcium.*;
import org.objectweb.proactive.calcium.exceptions.ParameterException;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.interfaces.*;
import org.objectweb.proactive.calcium.proactive.ProActiveManager;
import org.objectweb.proactive.calcium.skeletons.*;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.calcium.statistics.StatsGlobalImpl;

public class FindPrimes implements Serializable{

	public Skeleton root;

	
	public static void main(String[] args) {
		
		FindPrimes st = new FindPrimes();
		st.solve();
	}
	
	public FindPrimes(){
		
		root= new DaC<Challenge>( new ChallengeDivide(),
				new ChallengeDivideCondition(), 
				new Seq<Challenge>(new SolveChallenge()),
				new ConquerChallenge());
	}
	
	public void solve(){

		String descriptor=
				FindPrimes.class.getResource("LocalDescriptor.xml")
				.getPath();
		
		ResourceManager manager= 
			new MonoThreadedManager();
			//new MultiThreadedManager(5);
		    //new ProActiveManager(descriptor, "local");
		
		Calcium<Challenge> calcium = new Calcium<Challenge>(manager, root);
		
		calcium.inputParameter(new Challenge(1,6400,300));
		calcium.inputParameter(new Challenge(1,100,20));
		calcium.inputParameter(new Challenge(1,640,64));
		
		calcium.eval();
		
		try {
			for(Challenge res = calcium.getResult(); 
			res != null; res = calcium.getResult()){
				
				for(Integer i: res.primes){
					System.out.print(i+" ");
				}
				System.out.println();
				System.out.println(calcium.getStats(res));
			}
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (PanicException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		StatsGlobal stats = calcium.getStatsGlobal();
		System.out.println(stats);
	}
}