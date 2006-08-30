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

import java.io.Serializable;

public interface Stats extends Serializable{

	/**
	 * @return Returns the time elapsed since the creation of the task
	 * until this task is finished.
	 */
	public long getWallClockTime();
	
	/**
	 * The computation time will ideally correspond to the CPU Time or
	 * the best aproximation possible.
	 * In comparison with the processing time, this time does not
	 * include the network and other overheads.
	 * 
	 * @return Returns the computation time spent by this task.
	 */
	public long getComputationTime();
	
	/**
	 * @return Returns the number of leafs in this tree.
	 */
	public int getNumberLeafs();
	
	/**
	 * @return Returns the number of inner nodes in this tree.
	 */
	public int getNumberInnerNodes();
	
	/**
	 * @return Returns the ratio of inner nodes and leafs.
	 */
	public float ratioInnerLeaf();

	/**
	 * @return Returns the average number of branches for an internal node.
	 * This value is computed as the ratio between the tree size -1
	 * (minus the root) and the number of inner nodes. 
	 */
	public float avgNumBranches();
	
	/**
	 * @return Returns the accumulated processing time for all the subtree nodes.
	 */
	public long getSubTreeProcessingTime();

	public long getSubTreeReadyTime();
	public long getSubTreeWaitingTime();
	public long getSubTreeResultsTime();
	public long getSubTreeComputationTime();
	public long getSubTreeWallClockTime();
	
	/**
	 * @return Returns the number of directly referenced sub nodes for this level of the tree.
	 */
	public int getNumberDirectSubNodes();
	
	/**
	 * The processing time represents the time this node was asigned to some
	 * resource for computation. This time includes the time the node took to
	 * travel through the network.
	 * @return Returns the time spent by this node in processing state.
	 */
	public long getProcessingTime();

	/**
	 * The waiting time represent the time this node spent waiting for other
	 * related nodes to finish. In particular for sub-nodes spawned from this one.
	 * @return Returns the time this node spent in waiting state.
	 */
	public long getWaitingTime();
	
	/**
	 * The ready time represents the time this node was ready for execution waiting
	 * for an available resource.
	 * @return Returns the time spent by this node in ready state.
	 */
	public long getReadyTime();
	
	/**
	 * The results time represents the time since the node is considered finished,
	 * and the time the client actually asks (and gets) the result.
	 * @return Returns the time spent by this node in results state.
	 */
	public long getResultsTime();

	/**
	 * @return Returns the number of nodes in this tree (including this node).
	 */
	public int getTreeSize();
}
