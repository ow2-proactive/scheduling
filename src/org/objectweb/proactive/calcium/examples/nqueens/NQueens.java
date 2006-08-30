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
package org.objectweb.proactive.calcium.examples.nqueens;

import java.io.Serializable;

import org.objectweb.proactive.calcium.*;
import org.objectweb.proactive.calcium.exceptions.ParameterException;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.interfaces.*;
import org.objectweb.proactive.calcium.proactive.ProActiveManager;
import org.objectweb.proactive.calcium.proactive.ProActiveThreadedManager;
import org.objectweb.proactive.calcium.skeletons.*;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.calcium.statistics.StatsGlobalImpl;

public class NQueens implements Serializable{

	public Skeleton root;
	
	public static void main(String[] args) {
		
		NQueens nq = new NQueens();
		nq.start(new Board(15,14));
	}
	
	public NQueens(){
		
		root= new DaC<Board>(new DivideBoard(), 
				new DivideBoard(), 
				new Seq<Board>(new SolveBoard()),
				new ConquerBoard());
	}
	
	public void start(Board board){
		
		String descriptor=
				NQueens.class.getResource("LocalDescriptor.xml")
				.getPath();
		//descriptor="/home/mleyton/workspace/ProActive/descriptors/examples/SSH_SGE_Example.xml";
		ResourceManager manager= 
			//new MonoThreadedManager();
			//new MultiThreadedManager(4);
		 	//new ProActiveThreadedManager(descriptor, "local");
			new ProActiveManager(descriptor, "local");
		
		Calcium<Board> calcium = new Calcium<Board>(manager, root);	
		calcium.inputParameter(board);
		calcium.eval();
		
		try {
			for(Board res = calcium.getResult(); res != null; res = calcium.getResult()){
				long total=0;
				for(int i=0;i<res.solutions.length;i++){
					System.out.print(res.solutions[i]+"|");
					total+=res.solutions[i];
				}
				System.out.println();
				System.out.println("Total="+total);				
				System.out.println(calcium.getStats(res));
			}
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (PanicException e) {
			e.printStackTrace();
		}

		StatsGlobal stats = calcium.getStatsGlobal();
		System.out.println(stats);
	}
}