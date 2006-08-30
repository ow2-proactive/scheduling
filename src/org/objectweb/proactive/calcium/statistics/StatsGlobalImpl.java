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
package org.objectweb.proactive.calcium.statistics;

import javax.print.attribute.standard.Finishings;

import org.objectweb.proactive.calcium.Task;

/**
 * This class contains a snapshot of the current Skernel statistics.
 * Among others, the following information is kept:
 * 
 * Number of solved root tasks, Number of solved tasks, 
 * Length of the ready queue, Wallclock time, Computation time
 * 
 * @author mleyton
 */
public class StatsGlobalImpl implements java.io.Serializable, StatsGlobal{

	private long initTime, finitTime, computationTime;
	private int processingQueueLength, waitingQueueLength, readyQueueLength, resultsQueueLength;
	private int solvedNumberTasks, solvedRootTasks;
	private long processingTime, waitingTime, readyTime, resultsTime, wallclockTime;
	
	public StatsGlobalImpl(){
		solvedNumberTasks=solvedRootTasks=readyQueueLength=0;
		initTime=finitTime=computationTime=0;
	}
	
	public void setQueueLengths(int readyQ, int processingQ, int waitingQ, int resultsQ){
		this.readyQueueLength=readyQ;
		this.processingQueueLength=processingQ;
		this.waitingQueueLength=waitingQ;
		this.resultsQueueLength=resultsQ;
	}
		
	public void markStart(){
		if(initTime == 0){
			initTime=System.currentTimeMillis();
		}
	}
	
	public void markFinish(){
		if(finitTime == 0){
			finitTime=System.currentTimeMillis();
		}
	}
	
	public synchronized void increaseSolvedTasks(Task<?> task){
		solvedNumberTasks++;
		Stats taskStats=task.getStats();
		computationTime+=taskStats.getComputationTime();
		processingTime += taskStats.getProcessingTime();
		readyTime += taskStats.getReadyTime();
		resultsTime += taskStats.getResultsTime();
		waitingTime += taskStats.getResultsTime();
		wallclockTime += taskStats.getProcessingTime();
		
		if(task.isRootTask()){
			solvedRootTasks++;
		}
	}
	
	public String toString(){
		
		String ls=System.getProperty("line.separator");
		
		return 
		"Queue Lengths:  "+readyQueueLength+"R "+processingQueueLength+"P "+waitingQueueLength+"W "+resultsQueueLength+"F"+ls+
		"Average Times:  "+getAverageReadyTime()+"R "+getAverageProcessingTime()+"P "+
		getAverageWaitingTime()+"W "+getAverageResultsTime()+"F "+
		getAverageWallClockTime()+"L "+getAverageComputationTime()+"C [ms]"+ls+
		
		"Computed Tasks: "+getSolvedNumberOfRootTasks() +"[Root] " + getSolvedNumberOfTasks()+"[Total]";
	}

	//INTERFACE METHODS BEGIN HERE
	
	public int getReadyQueueLength(){
		return readyQueueLength;
	}
	
	public int getSolvedNumberOfTasks(){
		return solvedNumberTasks;
	}
	
	public int getSolvedNumberOfRootTasks(){
		return solvedRootTasks;
	}
	
	public long getUptime(){
		if(finitTime==0){
			return System.currentTimeMillis()-initTime;
		}
		return finitTime-initTime;
	}
	
	public int getProccessingQueueLength() {
		return processingQueueLength;
	}

	public int getWaitingQueueLength() {
		return waitingQueueLength;
	}

	public int getResultsQueueLength() {
		return resultsQueueLength;
	}

	public long getAverageWallClockTime() {
		return wallclockTime/solvedNumberTasks;
	}

	public long getAverageProcessingTime() {
		return processingTime/solvedNumberTasks;
	}

	public long getAverageWaitingTime() {
		return waitingTime/solvedNumberTasks;
	}

	public long getAverageResultsTime() {
		return resultsTime/solvedNumberTasks;
	}
	
	public long getAverageReadyTime() {
		return readyTime/solvedNumberTasks;
	}
	
	public long getAverageComputationTime(){
		return computationTime/solvedNumberTasks;
	}
}