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

import org.objectweb.proactive.calcium.interfaces.Condition;
import org.objectweb.proactive.calcium.interfaces.Divide;

public class DivideBoard implements Divide<Board>, Condition<Board>{

	public Vector<Board> divide(Board b) {

		Vector<Board> v= new Vector<Board>();
		if(b.isRootBoard()){
			v=b.divide();
		}
		else if(b.isBT1()){
			BoardBT1 board = (BoardBT1)b;
			v=board.divide();
		}
		else{
			BoardBT2 board = (BoardBT2)b;
			v=board.divide();
		}

		return v;
	}
	
	/**
	 * Divide while condition holds
	 * @param board
	 * @return true if board should be divided, false otherwise
	 */
	public boolean evalCondition(Board board) {
		
		if(board.isRootBoard()){
			return true;
		}
		else{
			BoardBT1 boardBT1=(BoardBT1)board;
			if(boardBT1.row+boardBT1.solvableSize<boardBT1.n){
				return true;
			}
		}
		
		return false;
	}
}
