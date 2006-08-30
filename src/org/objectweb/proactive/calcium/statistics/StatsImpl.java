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

public class StatsImpl implements Stats {
	
	private long computationTime;
	private long waitingTime, processingTime, readyTime, resultsTime;
	private long initTime, finitTime;
	private long currentStateStart;

	//sub task related stats
	private long subTreeProcessingTime,subTreeReadyTime, subTreeWaitingTime,
		subTreeResultsTime, subTreeComputationTime, subTreeWallClockTime;
	private int subTreeSize;
	private int numberDirectSubNodes;
	private int numberLeafs;
	
	public StatsImpl() {
		computationTime=0;
		waitingTime=processingTime=readyTime=resultsTime=0;
		initTime=System.currentTimeMillis();
		finitTime=0;
		currentStateStart=initTime;
		
		subTreeProcessingTime = subTreeSize = numberDirectSubNodes=numberLeafs=0;
	}
	
	public void addComputationTime(long time){
		computationTime+=time;
	}
	
	public long getComputationTime(){
		return computationTime;
	}
	
	public void exitReadyState() {
		readyTime += getStateElapsedTime();
	}

	public void exitProcessingState() {
		processingTime += getStateElapsedTime(); 
	}
	
	public void exitWaitingState() {
		waitingTime += getStateElapsedTime();
	}
	
	public void exitResultsState() {
		resultsTime += getStateElapsedTime();
	}
	
	private long getStateElapsedTime(){
		long initTime = currentStateStart;
		currentStateStart = System.currentTimeMillis();
		return currentStateStart - initTime ;
	}
	
	@Override
	public String toString(){
		String ls= " ";//System.getProperty("line.separator");
		return  
			"Time: "+processingTime + "/"+subTreeProcessingTime +"P "+ 
			                readyTime+"/"+subTreeReadyTime +"R " + 
			                waitingTime +"/"+subTreeWaitingTime+ "W "+
			                resultsTime +"/"+subTreeResultsTime+ "F "+ 
			                getWallClockTime()+"/"+subTreeWallClockTime+"L "
			                +getComputationTime()+"/"+subTreeComputationTime+"C[ms]"+ ls+
			"Nodes:" + +getNumberInnerNodes()+  "[Inner]/"+ getNumberLeafs() +"[Leafs] Ratio:"+ ratioInnerLeaf()+ ls+
			"Avg-Branches:" + avgNumBranches();
	}
	
	public void markFinishTime(){
		finitTime=System.currentTimeMillis();
	}
	
	public void addChildStats(StatsImpl stats) {

		this.subTreeProcessingTime +=stats.getProcessingTime()+stats.getSubTreeProcessingTime();
		this.subTreeWaitingTime +=stats.getWaitingTime()+stats.getSubTreeWaitingTime();
		this.subTreeReadyTime +=stats.getReadyTime()+stats.getSubTreeReadyTime();
		this.subTreeResultsTime +=stats.getResultsTime()+stats.getSubTreeResultsTime();
		this.subTreeWallClockTime +=stats.getWallClockTime()+stats.getSubTreeWallClockTime();
		this.subTreeComputationTime +=stats.getComputationTime()+stats.getSubTreeComputationTime();
		
		this.subTreeSize += stats.getTreeSize();
		this.numberLeafs += stats.getNumberLeafs();
		this.numberDirectSubNodes++;
	}
	
	/*
	 * INTERFACE METHODS
	 */
	public long getWallClockTime(){
		if(finitTime==0){
			return System.currentTimeMillis()-initTime;
		}
		return finitTime-initTime;
	}

	public int getNumberLeafs() {
		if (numberDirectSubNodes <=0 ) return 1;
		return numberLeafs;
	}
	
	public int getNumberInnerNodes(){
		return getTreeSize()-getNumberLeafs();
	}
	
	public float ratioInnerLeaf(){
		return ((float)getNumberInnerNodes())/getNumberLeafs();
	}

	public float avgNumBranches(){
		if(getNumberInnerNodes() == 0) return 0;
		return ((float)getTreeSize()-1)/getNumberInnerNodes();
	}

	/**
	 * @return Returns the accumulated processing time for all the subtree nodes.
	 */
	public long getSubTreeProcessingTime() {
		return subTreeProcessingTime;
	}

	/**
	 * @return Returns the number of branches for this level of the tree.
	 */
	public int getNumberDirectSubNodes() {
		return numberDirectSubNodes;
	}

	/**
	 * @return Returns the time spent in the processing state by this node..
	 */
	public long getProcessingTime() {
		return processingTime;
	}

	/**
	 * @return Returns the time spent in ready state by this node.
	 */
	public long getReadyTime() {
		return readyTime;
	}

	/**
	 * @return Returns the time this node spent in the results state.
	 */
	public long getResultsTime() {
		return resultsTime;
	}

	/**
	 * @return Returns the number of nodes in this subtree (including this node).
	 */
	public int getTreeSize() {
		return subTreeSize+1;
	}

	/**
	 * @return Returns the time this node spent in waiting state.
	 */
	public long getWaitingTime() {
		return waitingTime;
	}

	/**
	 * @return Returns the subTreeReadyTime.
	 */
	public long getSubTreeReadyTime() {
		return subTreeReadyTime;
	}

	/**
	 * @return Returns the subTreeResultsTime.
	 */
	public long getSubTreeResultsTime() {
		return subTreeResultsTime;
	}

	/**
	 * @return Returns the subTreeWallClockTime.
	 */
	public long getSubTreeWallClockTime() {
		return subTreeWallClockTime;
	}

	/**
	 * @return Returns the subTreeComputationTime.
	 */
	public long getSubTreeComputationTime() {
		return subTreeComputationTime;
	}

	/**
	 * @return Returns the subTreeWaitingTime.
	 */
	public long getSubTreeWaitingTime() {
		return subTreeWaitingTime;
	}
}
