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
import java.util.Vector;

import org.objectweb.proactive.calcium.Calcium;
import org.objectweb.proactive.calcium.MonoThreadedManager;
import org.objectweb.proactive.calcium.MultiThreadedManager;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.calcium.Stream;
import org.objectweb.proactive.calcium.examples.nqueens.bt1.DivideBT1;
import org.objectweb.proactive.calcium.examples.nqueens.bt1.SolveBT1;
import org.objectweb.proactive.calcium.examples.nqueens.bt2.DivideBT2;
import org.objectweb.proactive.calcium.examples.nqueens.bt2.SolveBT2;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.futures.Future;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.monitor.Monitor;
import org.objectweb.proactive.calcium.monitor.SimpleLogMonitor;
import org.objectweb.proactive.calcium.proactive.ProActiveManager;
import org.objectweb.proactive.calcium.skeletons.DaC;
import org.objectweb.proactive.calcium.skeletons.Fork;
import org.objectweb.proactive.calcium.skeletons.Pipe;
import org.objectweb.proactive.calcium.skeletons.Seq;

public class NQueens implements Serializable{

	public Skeleton<Board> root;
	
	public static void main(String[] args) throws InterruptedException, PanicException {
		
		NQueens nq = new NQueens();
		if(args.length != 5){
			nq.start(16,13,10,NQueens.class.getResource("LocalDescriptor.xml").getPath(),"local");
		}
		else{
			nq.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], args[4] );
		}
	}
	
	public NQueens(){
		
		Skeleton<Board> BT1 = new DaC<Board>(new DivideBT1(), 
				  new DivideCondition(), 
	              new Seq<Board>(new SolveBT1()),
	              new ConquerBoard());
	              
		Skeleton<Board> BT2 =  new DaC<Board>(new DivideBT2(), 
	   		      new DivideCondition(), 
			      new Seq<Board>(new SolveBT2()),
			      new ConquerBoard());

		root = new Fork<Board>(new ConquerBoard(), BT1, BT2);
		//root = new Pipe<Board>(BT1, BT2);
	}
	
	public void start(int boardSize, int solvableSize, int times, String descriptor, String virtualNode) throws InterruptedException, PanicException{
		
		ResourceManager manager= 
			//new MonoThreadedManager();
			//new MultiThreadedManager(10);
		 	//new ProActiveThreadedManager(descriptor, virtualNode);
			new ProActiveManager(descriptor, virtualNode);

		Calcium calcium = new Calcium(manager);
		Monitor monitor= new SimpleLogMonitor(calcium, 5);
		monitor.start();
		calcium.boot();
		
		Stream<Board> stream = calcium.newStream(root);
		Vector<Future<Board>> futures = new Vector<Future<Board>>(times);
		
		for(int i=0;i<times;i++){
			Future<Board> f= stream.input(new Board(boardSize,solvableSize));
			futures.add(f);
		}

		try {
			for(Future<Board> future:futures){
				Board res=future.get();
				long total=0;
				for(int i=0;i<res.solutions.length;i++){
					System.out.print(res.solutions[i]+"|");
					total+=res.solutions[i];
				}
				System.out.println();
				System.out.println("Total="+total);				
				System.out.println(future.getStats());
			}
		} catch (MuscleException e) {
			e.printStackTrace();
		}
		calcium.shutdown();
		monitor.stop();
	}
}