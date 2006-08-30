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

import java.util.Vector;

public class Board implements java.io.Serializable {
	
	//board dimension
	public int n;
	public int solvableSize;
		
	//solutions vector
	public long solutions[];

	// the board
	public int board[];
	
	public Board (int n, int solvableSize){
		this.n=n;
		this.solvableSize=solvableSize;
		
		this.solutions = new long[n];
		this.board = new int[n];
		
		for(int i=0;i<n;i++){
			solutions[i]=0;
		}
	}
	
	public boolean isRootBoard(){
		return true;
	}
	
	public boolean isBT1(){
		return false;
	}
	
	public Vector<Board> divide(){
		
		Vector<Board> v = new Vector<Board>();
		v.addAll(initDivideBT1());
		v.addAll(initDivideBT2());
		return v;
	}
	
	private Vector<Board> initDivideBT1() {
		
		Vector<Board> v = new Vector<Board>();
		//We set row 0 and 1 for backtrack1
		for (int i = this.n - 2; i >= 2; i--) {
			int bit = 1 << i;
			v.add(new BoardBT1(this.n, solvableSize, 2, (2 | bit) << 1,	1 | bit, bit >> 1,i,null));
		}
		
		return v;
	}
	
	private Vector<Board> initDivideBT2() {
		
		Vector<Board> v = new Vector<Board>();
		
		int sidemask = (1 << (n - 1)) | 1;
		int lastmask = sidemask;
		int topbit = 1 << (n - 1);
		int mask = (1 << n) - 1;
		int endbit = topbit >> 1;

		for (int i = 1, j = n - 2; i < j; i++, j--) {
			//bound1 = i; //bound2 = j;
			int bit = 1 << i;
			
			v.add(
				new BoardBT2(n, solvableSize, 1, bit << 1, bit, bit >> 1, i, j, sidemask, lastmask,
					topbit,	mask, endbit, null));

			lastmask |= lastmask >> 1 | lastmask << 1;
			endbit >>= 1;
		}
		
		return v;
	}
	/*
	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public boolean equals(Object o){
		return hashCode() == o.hashCode();
	}
	*/
}